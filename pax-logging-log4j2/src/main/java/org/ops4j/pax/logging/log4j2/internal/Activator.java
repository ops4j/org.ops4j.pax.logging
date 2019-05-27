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
package org.ops4j.pax.logging.log4j2.internal;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.ops4j.pax.logging.EventAdminPoster;
import org.ops4j.pax.logging.PaxLoggingConstants;
import org.ops4j.pax.logging.PaxLoggingService;
import org.ops4j.pax.logging.spi.support.BackendSupport;
import org.ops4j.pax.logging.spi.support.ConfigurationNotifier;
import org.ops4j.pax.logging.spi.support.DefaultServiceLog;
import org.ops4j.pax.logging.spi.support.FallbackLogFactory;
import org.ops4j.pax.logging.spi.support.LogReaderServiceImpl;
import org.ops4j.pax.logging.spi.support.OsgiUtil;
import org.ops4j.pax.logging.spi.support.RegisteredService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.log.LogReaderService;

/**
 * Starts the Log4j log services.
 */
public class Activator implements BundleActivator {

    /** Default file looked up during startup to override Configuration Admin config */
    private static final String PAX_LOGGING_PROPERTY_FILE = "pax-logging.properties";

    // PaxLoggingService implementation backed by Log4J1 and its registration
    private ServiceRegistration<?> m_RegistrationPaxLogging;
    private PaxLoggingServiceImpl m_PaxLogging;

    private RegisteredService<LogReaderService, LogReaderServiceImpl> logReaderInfo;
    private RegisteredService<EventAdminPoster, EventAdminPoster> eventAdminInfo;
    private RegisteredService<ConfigurationNotifier, ConfigurationNotifier> eventAdminConfigurationNotifierInfo;

    private BundleContext bundleContext;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        sanityCheck();

        this.bundleContext = bundleContext;

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

        // OSGi Compendium 101.2: The Log Service Interface - register Log4J1 specific Pax Logging service
        m_PaxLogging = new PaxLoggingServiceImpl(bundleContext,
                logReaderInfo.getService(), eventAdminInfo.getService(),
                eventAdminConfigurationNotifierInfo.getService());

        // Special case (no PAXLOGGING Jira for that...) to ensure that we can override etc/org.ops4j.pax.logging.cfg
        // file with one specified via system properties.
        final Path configFilePath = Paths.get(externalFile());

        if (configFilePath.toFile().isFile()) {
            try (InputStream inputStream = new FileInputStream(configFilePath.toFile())) {
                Properties properties = new Properties();
                properties.load(inputStream);
                // This configuration will be used instead of what's in Configuration Admin
                final Dictionary<String, String> config = new Hashtable<>();
                StrSubstitutor strSubstitutor = new StrSubstitutor(System.getProperties());
                for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                    String propValue = (String) entry.getValue();
                    propValue = strSubstitutor.replace(propValue);
                    config.put((String) entry.getKey(), propValue);
                }

                ServiceReference<ConfigurationAdmin> sr = bundleContext.getServiceReference(ConfigurationAdmin.class);
                if (sr != null) {
                    ConfigurationAdmin configurationAdmin = bundleContext.getService(sr);
                    if (configurationAdmin != null) {
                        Configuration configuration = configurationAdmin.getConfiguration(PaxLoggingConstants.LOGGING_CONFIGURATION_PID, null);
                        configuration.update(config);
                    }
                }

                m_PaxLogging.updated(config);
            }
        }

        // registration of log service and CM ManagedService for org.ops4j.pax.logging PID
        Dictionary<String, Object> serviceProperties = new Hashtable<>();
        serviceProperties.put(Constants.SERVICE_PID, PaxLoggingConstants.LOGGING_CONFIGURATION_PID);
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
     * Ensure that some specific classes are loaded by pax-logging-log4j2 classloader instead of
     * from pax-logging-api bundle.
     */
    private void sanityCheck() {
        Bundle paxLoggingApi = FrameworkUtil.getBundle(PaxLoggingService.class);
        Bundle paxLoggingLog4J2 = FrameworkUtil.getBundle(this.getClass());
    }

    private String externalFile() {
        String property = OsgiUtil.systemOrContextProperty(bundleContext, PaxLoggingConstants.LOGGING_CFG_PROPERTY_FILE);
        return property == null ? PAX_LOGGING_PROPERTY_FILE : property;
    }

}
