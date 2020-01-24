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
import org.apache.logging.log4j.status.StatusLogger;
import org.ops4j.pax.logging.EventAdminPoster;
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
 * Starts the Log4j log services.
 */
public class Activator implements BundleActivator {

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

        boolean cm = BackendSupport.isConfigurationAdminAvailable();

        if (!cm) {
            StatusLogger.getLogger().info("Configuration Admin is not available.");
        }

        // OSGi Compendium 101.2: The Log Service Interface - register Log4J2 specific Pax Logging service
        // it's not configured by default
        m_PaxLogging = new PaxLoggingServiceImpl(bundleContext,
                logReaderInfo.getService(), eventAdminInfo.getService(),
                eventAdminConfigurationNotifierInfo.getService());

        // PAXLOGGING-308 Check if there's external file specified to use instead of Configuration Admin PID
        // or when there's no Configuration Admin at all
        String externalFile = BackendSupport.externalFile(bundleContext, PaxLoggingConstants.PAX_LOGGING_PROPERTY_FILE);
        final Path configFilePath = Paths.get(externalFile);

        if (!configFilePath.toFile().isFile() && !externalFile.contains(",")) {
            // file is not available
            StatusLogger.getLogger().info("Initializing Log4j2 using default configuration");

            m_PaxLogging.configureDefaults();
        } else {
            // could be one or more (comma-separated) files
            StatusLogger.getLogger().info("Initializing Log4j2 using " + configFilePath.toAbsolutePath());

            boolean nativeConfiguration = true;
            // This configuration will be used instead of what could be found (or not) in Concifuration Admin
            final Dictionary<String, String> config = new Hashtable<>();

            if (configFilePath.toFile().isFile() && (
                    configFilePath.toFile().getName().endsWith(".properties")
                    || configFilePath.toFile().getName().endsWith(".config")
                    || configFilePath.toFile().getName().endsWith(".cfg"))) {
                // this may be either ConfigurationAdmin/FileInstall/Karaf properties file with properties
                // prefixed with "log4j2." or just properties file to be used directly by Log4j2 itself
                try (InputStream inputStream = new FileInputStream(configFilePath.toFile())) {
                    Properties properties = new Properties();
                    properties.load(inputStream);

                    boolean nonNative = properties.stringPropertyNames().stream().anyMatch(key -> key.startsWith("log4j2."));

                    if (nonNative) {
                        // there's at least one property with such prefix - so it's a file to be processed further
                        // it still may contain single property "org.ops4j.pax.logging.log4j2.config.file"
                        // pointing to native file though...
                        nativeConfiguration = false;

                        StrSubstitutor strSubstitutor = new StrSubstitutor(System.getProperties());
                        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                            String propValue = (String) entry.getValue();
                            propValue = strSubstitutor.replace(propValue);
                            config.put((String) entry.getKey(), propValue);
                        }
                    }

                    // let's not put this configuration to Configuration Admin (which may not be available)
//                    ServiceReference<ConfigurationAdmin> sr = bundleContext.getServiceReference(ConfigurationAdmin.class);
//                    if (sr != null) {
//                        ConfigurationAdmin configurationAdmin = bundleContext.getService(sr);
//                        if (configurationAdmin != null) {
//                            Configuration configuration = configurationAdmin.getConfiguration(PaxLoggingConstants.LOGGING_CONFIGURATION_PID, "?");
//                            configuration.update(config);
//                        }
//                    }
                }
            }

            if (nativeConfiguration) {
                // this file has to be passed directly to Log4j2 - it may contain comma or simply we let Log4j2
                // fail when file is not found.
                StatusLogger.getLogger().info("Passing " + configFilePath.toAbsolutePath() + " to Log4j2");
                config.put(PaxLoggingConstants.PID_CFG_LOG4J2_CONFIG_FILE, configFilePath.toAbsolutePath().toString());
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
     * Ensure that some specific classes are loaded by pax-logging-log4j2 classloader instead of
     * from pax-logging-api bundle.
     */
    private void sanityCheck() {
        Bundle paxLoggingApi = FrameworkUtil.getBundle(PaxLoggingService.class);
        Bundle paxLoggingLog4J2 = FrameworkUtil.getBundle(this.getClass());
    }

}
