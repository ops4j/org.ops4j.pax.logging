/*
 * Copyright 2005 Niclas Hedhman.
 * Copyright 2011 Avid Technology, Inc.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.logging.logback.internal;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Dictionary;
import java.util.Hashtable;

import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.WarnStatus;
import org.ops4j.pax.logging.EventAdminPoster;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingConstants;
import org.ops4j.pax.logging.PaxLoggingService;
import org.ops4j.pax.logging.spi.support.BackendSupport;
import org.ops4j.pax.logging.spi.support.ConfigurationNotifier;
import org.ops4j.pax.logging.spi.support.DefaultServiceLog;
import org.ops4j.pax.logging.spi.support.FallbackLogFactory;
import org.ops4j.pax.logging.spi.support.LogReaderServiceImpl;
import org.ops4j.pax.logging.spi.support.RegisteredService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogReaderService;

/**
 * Starts the logback log services.
 *
 * <p>
 * This code was originally derived from org.ops4j.pax.logging.service.internal.Activator v1.6.0. Changes include:
 * <ul>
 *     <li>added a call to stop the service</li>
 *     <li>added more logging to detect failures at start, which may be otherwise lost in early boot</li>
 *     <li>unified in 1.11+ with other backends, huge code cleanup</li>
 * </ul>
 *
 * @author Chris Dolan -- some code derived from from pax-logging-service v1.6.0
 */
public class Activator implements BundleActivator {

    // PaxLoggingService implementation backed by Logback and its registration
    private ServiceRegistration<?> m_RegistrationPaxLogging;
    private PaxLoggingServiceImpl m_paxLogging;

    private RegisteredService<LogReaderService, LogReaderServiceImpl> logReaderInfo;
    private RegisteredService<EventAdminPoster, EventAdminPoster> eventAdminInfo;
    private RegisteredService<ConfigurationNotifier, ConfigurationNotifier> eventAdminConfigurationNotifierInfo;

    private PaxLogger logLog;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        sanityCheck();

        // Fallback PaxLogger configuration - has to be done in each backed, as org.ops4j.pax.logging.spi.support
        // package is private in all backends
        String levelName = BackendSupport.defaultLogLevel(bundleContext);
        DefaultServiceLog.setLogLevel(levelName);

        // OSGi Compendium 101.4: Log Reader Service
        logReaderInfo = BackendSupport.createAndRegisterLogReaderService(bundleContext);

        // OSGi Compendium 101.6.4: Log Events
        eventAdminInfo = BackendSupport.eventAdminSupport(bundleContext);

        // EventAdmin (or mock) service to notify about configuration changes
        eventAdminConfigurationNotifierInfo = BackendSupport.eventAdminConfigurationNotifier(bundleContext);

        logLog = FallbackLogFactory.createFallbackLog(bundleContext.getBundle(), "logback");

        boolean cm = BackendSupport.isConfigurationAdminAvailable();

        if (!cm) {
            logLog.info("Configuration Admin is not available.");
        }

        // OSGi Compendium 101.2: The Log Service Interface - register Logback specific Pax Logging service
        // it's not configured by default
        m_paxLogging = new PaxLoggingServiceImpl(bundleContext,
                logReaderInfo.getService(), eventAdminInfo.getService(),
                eventAdminConfigurationNotifierInfo.getService(), logLog);

        // PAXLOGGING-308 Check if there's external file specified to use instead of Configuration Admin PID
        // or when there's no Configuration Admin at all
        String externalFile = BackendSupport.externalFile(bundleContext, null);
        final Path configFilePath = externalFile == null ? null : Paths.get(externalFile);

        if (configFilePath == null || !configFilePath.toFile().isFile()) {
            // file is not available
            logLog.info("Initializing Logback using default configuration");

            m_paxLogging.configureDefaults();
        } else {
            logLog.info("Initializing Logback using " + configFilePath.toAbsolutePath());

            // in Logback, this is always native XML configuration
            Dictionary<String, String> config = new Hashtable<>();
            config.put(PaxLoggingConstants.PID_CFG_LOGBACK_CONFIG_FILE, configFilePath.toAbsolutePath().toString());

            m_paxLogging.setDefaultConfiguration(config);
            m_paxLogging.updated(null);
        }

        if (cm) {
            // registration of CM ManagedService for org.ops4j.pax.logging PID
            Dictionary<String, Object> serviceProperties = new Hashtable<>();
            serviceProperties.put(Constants.SERVICE_PID, PaxLoggingConstants.LOGGING_CONFIGURATION_PID);
            m_RegistrationPaxLogging = bundleContext.registerService("org.osgi.service.cm.ManagedService",
                    new LoggingManagedService(m_paxLogging), serviceProperties);
        }

        // registration of log service itself
        Dictionary<String, Object> serviceProperties = new Hashtable<>();
        serviceProperties.put(Constants.SERVICE_RANKING, BackendSupport.paxLoggingServiceRanking(bundleContext));
        m_RegistrationPaxLogging = bundleContext.registerService(PaxLoggingConstants.LOGGING_LOGSERVICE_NAMES,
                m_paxLogging, serviceProperties);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        if (eventAdminInfo != null) {
            eventAdminInfo.close();
        }
        if (logReaderInfo != null) {
            logReaderInfo.close();
        }
        if (eventAdminConfigurationNotifierInfo != null) {
            eventAdminConfigurationNotifierInfo.close();
        }

        m_RegistrationPaxLogging.unregister();
        m_RegistrationPaxLogging = null;

        // Shutdown Pax Logging to ensure appender file locks get released
        if (m_paxLogging != null) {
            m_paxLogging.shutdown();
            m_paxLogging = null;
        }

        FallbackLogFactory.cleanup();
    }

    /**
     * Ensure that some specific classes are loaded by pax-logging-logback classloader instead of
     * from pax-logging-api bundle.
     */
    private void sanityCheck() {
        Bundle paxLoggingApi = FrameworkUtil.getBundle(PaxLoggingService.class);
        Bundle paxLoggingLogback = FrameworkUtil.getBundle(this.getClass());

        // lets check and at the same time pre-load some classes
        FrameworkUtil.getBundle(ErrorStatus.class);
        FrameworkUtil.getBundle(WarnStatus.class);
        Bundle b1 = FrameworkUtil.getBundle(InfoStatus.class);
        if (paxLoggingLogback != b1) {
            String b1Bundle = b1 == null ? "system classloader" : b1.toString();
            throw new IllegalStateException("ch.qos.logback.core.status.InfoStatus class was loaded from " + b1Bundle +
                    ". It should be loaded from " + paxLoggingLogback + ".");
        }
    }

}
