/*
 * Copyright 2005 Niclas Hedhman.
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
package org.ops4j.pax.logging.log4j1.internal;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.Loader;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.xml.XMLLayout;
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
import org.osgi.service.cm.ManagedService;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;

/**
 * Activator for one of different <em>backends</em> supporting pax-logging-api multi-facade.
 *
 * The most important tasks are:
 * <ul>
 *     <li>register {@link LogService}/{@link PaxLoggingService}
 *     service specific to Log4J1</li>
 *     <li>register {@link ManagedService} to track {@code org.ops4j.pax.logging} PID</li>
 * </ul>
 */
public class Activator implements BundleActivator {

    // PaxLoggingService implementation backed by Log4J1 and its registration
    private ServiceRegistration<?> m_RegistrationPaxLogging;
    private PaxLoggingServiceImpl m_PaxLogging;

    private RegisteredService<LogReaderService, LogReaderServiceImpl> logReaderInfo;
    private RegisteredService<EventAdminPoster, EventAdminPoster> eventAdminInfo;
    private RegisteredService<ConfigurationNotifier, ConfigurationNotifier> eventAdminConfigurationNotifierInfo;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        sanityCheck();

        // Fallback PaxLogger configuration - has to be done in each backed, as org.ops4j.pax.logging.spi.support
        // package is private in all backends
        String levelName = BackendSupport.defaultLogLevel(bundleContext);
        DefaultServiceLog.setLogLevel(levelName);
        if (DefaultServiceLog.getStaticLogLevel() <= PaxLogger.LEVEL_DEBUG) {
            // Log4j1 internal debug
            LogLog.setInternalDebugging(true);
        }

        // OSGi Compendium 101.4: Log Reader Service
        logReaderInfo = BackendSupport.createAndRegisterLogReaderService(bundleContext);

        // OSGi Compendium 101.6.4: Log Events
        eventAdminInfo = BackendSupport.eventAdminSupport(bundleContext);

        // EventAdmin (or mock) service to notify about configuration changes
        eventAdminConfigurationNotifierInfo = BackendSupport.eventAdminConfigurationNotifier(bundleContext);

        boolean cm = BackendSupport.isConfigurationAdminAvailable();

        if (!cm) {
            LogLog.debug("Configuration Admin is not available.");
        }

        // OSGi Compendium 101.2: The Log Service Interface - register Log4J1 specific Pax Logging service
        // it's not configured by default
        m_PaxLogging = new PaxLoggingServiceImpl(bundleContext,
                logReaderInfo.getService(), eventAdminInfo.getService(),
                eventAdminConfigurationNotifierInfo.getService());

        // PAXLOGGING-308 Check if there's external file specified to use instead of Configuration Admin PID
        // or when there's no Configuration Admin at all
        String externalFile = BackendSupport.externalFile(bundleContext, PaxLoggingConstants.PAX_LOGGING_PROPERTY_FILE);
        final Path configFilePath = Paths.get(externalFile);

        if (!configFilePath.toFile().isFile()) {
            // file is not available
            LogLog.debug("Initializing Log4j1 using default configuration");

            m_PaxLogging.configureDefaults();
        } else {
            LogLog.debug("Initializing Log4j1 using " + configFilePath.toAbsolutePath());

            // This configuration will be used instead of what could be found (or not) in Concifuration Admin
            final Dictionary<String, String> config = new Hashtable<>();

            // this may be either ConfigurationAdmin/FileInstall/Karaf properties file with properties
            // prefixed with "log4j2." or just properties file to be used directly by Log4j2 itself
            try (InputStream inputStream = new FileInputStream(configFilePath.toFile())) {
                Properties properties = new Properties();
                properties.load(inputStream);
                for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                    String propValue = (String) entry.getValue();
                    config.put((String) entry.getKey(), propValue);
                }
            }

            m_PaxLogging.updated(config);
        }

        if (cm) {
            // registration of CM ManagedService for org.ops4j.pax.logging PID
            Dictionary<String, Object> serviceProperties = new Hashtable<>();
            serviceProperties.put(Constants.SERVICE_PID, PaxLoggingConstants.LOGGING_CONFIGURATION_PID);
            m_RegistrationPaxLogging = bundleContext.registerService("org.osgi.service.cm.ManagedService",
                    new LoggingManagedService(m_PaxLogging), serviceProperties);
        }

        // registration of log service itself
        Dictionary<String, Object> serviceProperties = new Hashtable<>();
        serviceProperties.put(Constants.SERVICE_RANKING, BackendSupport.paxLoggingServiceRanking(bundleContext));
        m_RegistrationPaxLogging = bundleContext.registerService(PaxLoggingConstants.LOGGING_LOGSERVICE_NAMES,
                m_PaxLogging, serviceProperties);
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
        if (m_PaxLogging != null) {
            m_PaxLogging.shutdown();
            m_PaxLogging = null;
        }

        FallbackLogFactory.cleanup();
    }

    /**
     * Ensure that some specific classes are loaded by pax-logging-log4j1 classloader instead of
     * from pax-logging-api bundle.
     */
    private void sanityCheck() {
        Bundle paxLoggingApi = FrameworkUtil.getBundle(PaxLoggingService.class);
        Bundle paxLoggingService = FrameworkUtil.getBundle(this.getClass());

        // pax-logging-api has own versions of these classes because they're part of the API
        // and exported packages
        Bundle b1 = FrameworkUtil.getBundle(Logger.class);
        if (paxLoggingService != b1) {
            String b1Bundle = b1 == null ? "system classloader" : b1.toString();
            throw new IllegalStateException("org.apache.log4j.Logger class was loaded from " + b1Bundle +
                    ". It should be loaded from " + paxLoggingService + ".");
        }
        Bundle b2 = FrameworkUtil.getBundle(LogManager.class);
        if (paxLoggingService != b2) {
            String b2Bundle = b2 == null ? "system classloader" : b2.toString();
            throw new IllegalStateException("org.apache.log4j.LogManager class was loaded from " + b2Bundle +
                    ". It should be loaded from " + paxLoggingService + ".");
        }

        // org.apache.log4j.helpers.Loader should be taken from pax-logging-log4j1 even if pax-logging-api
        // has own version.
        // pax-logging-api also needs this class, because it's part of exported package.
        Bundle b3 = FrameworkUtil.getBundle(Loader.class);
        if (paxLoggingService != b3) {
            String b3Bundle = b3 == null ? "system classloader" : b3.toString();
            throw new IllegalStateException("org.apache.log4j.helpers.Loader class was loaded from " + b3Bundle +
                    ". It should be loaded from " + paxLoggingService + ".");
        }

        Bundle b4 = FrameworkUtil.getBundle(XMLLayout.class);
        if (paxLoggingApi != b4) {
            String b4Bundle = b4 == null ? "system classloader" : b4.toString();
            throw new IllegalStateException("org.apache.log4j.xml.XMLLayout class was loaded from " + b4Bundle +
                    ". It should be loaded from " + paxLoggingApi + ".");
        }
    }

}
