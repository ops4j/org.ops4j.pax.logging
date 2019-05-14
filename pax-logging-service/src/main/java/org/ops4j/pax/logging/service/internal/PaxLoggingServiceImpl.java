/*
 * Copyright 2005-2009 Niclas Hedhman.
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
package org.ops4j.pax.logging.service.internal;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PaxLoggingConfigurator;
import org.apache.log4j.helpers.LogLog;
import org.knopflerfish.service.log.LogService;
import org.ops4j.pax.logging.EventAdminPoster;
import org.ops4j.pax.logging.PaxContext;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingConstants;
import org.ops4j.pax.logging.PaxLoggingService;
import org.ops4j.pax.logging.service.internal.spi.PaxAppenderProxy;
import org.ops4j.pax.logging.spi.support.BackendSupport;
import org.ops4j.pax.logging.spi.support.ConfigurationNotifier;
import org.ops4j.pax.logging.spi.support.LogEntryImpl;
import org.ops4j.pax.logging.spi.support.LogReaderServiceImpl;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.log.LogEntry;

/**
 * Log4J1 specific implementation of {@link PaxLoggingService}. It's a {@link ServiceFactory}, so each
 * bundle will get own instance of this service.
 */
public class PaxLoggingServiceImpl
        implements PaxLoggingService, LogService, ManagedService, ServiceFactory {

    private BundleContext m_bundleContext;
    private List<java.util.logging.Logger> m_julLoggers;

    private ReadWriteLock m_configLock;

    // LogReaderService registration as defined by org.osgi.service.log package
    private LogReaderServiceImpl m_logReader;

    // pax-logging-service specific PaxContext for all MDC access
    private PaxContext m_context;

    // optional bridging into Event Admin service
    private EventAdminPoster m_eventAdmin;

    // optional notification mechanism for configuration events
    private final ConfigurationNotifier m_configNotifier;

    // Log level (actually a threashold) for this entire service.
    private int m_logLevel = org.osgi.service.log.LogService.LOG_DEBUG;

    // there's no need to run configureDefaults() more than once. That was happening in constructor
    // and millisecond later during registration of ManagedService, upon receiving empty org.ops4j.pax.logging
    // configuration
    private AtomicBoolean emptyConfiguration = new AtomicBoolean(false);

    public PaxLoggingServiceImpl(BundleContext context, LogReaderServiceImpl logReader, EventAdminPoster eventAdmin, ConfigurationNotifier configNotifier) {
        m_bundleContext = context;
        m_logReader = logReader;
        m_eventAdmin = eventAdmin;
        m_configNotifier = configNotifier;
        m_context = new PaxContext();
        m_configLock = new ReentrantReadWriteLock();
        m_julLoggers = new LinkedList<>();

        configureDefaults();
    }

    /**
     * Shut down the Pax Logging service. This will reset the logging configuration entirely, so it should only be
     * used just before disposing of the service instance.
     */
    protected void shutdown() {
        LogManager.resetConfiguration();
        // TODO: shouldn't we explicitly close the PaxAppenderProxy trackers? (I know they're closed anywye on bundle stop...)
    }

    ReadWriteLock getConfigLock() {
        return m_configLock;
    }

    // org.ops4j.pax.logging.PaxLoggingService

    @Override
    public PaxLogger getLogger(Bundle bundle, String category, String fqcn) {
        // obtain org.apache.log4j.Logger from unshaded and unchanged log4j:log4j class.
        // pax-logging-api uses shaded org.apache.log4j.Logger class, but here we need real
        // implementation
        Logger log4jLogger;
        if (category == null) {
            // Anonymous Logger in JDK Util Logging will have a category of null.
            log4jLogger = Logger.getRootLogger();
        } else {
            log4jLogger = Logger.getLogger(category);
        }
        return new PaxLoggerImpl(bundle, log4jLogger, fqcn, this);
    }

    // org.knopflerfish.service.log.LogService

    @Override
    public int getLogLevel() {
        return m_logLevel;
    }

    // org.osgi.service.log.LogService

    @Override
    public void log(int level, String message) {
        log(level, message, null);
    }

    @Override
    public void log(int level, String message, Throwable exception) {
        log((ServiceReference) null, level, message, exception);
    }

    @Override
    public void log(ServiceReference sr, int level, String message) {
        log(sr, level, message, null);
    }

    @Override
    public void log(ServiceReference sr, int level, String message, Throwable exception) {
        log(null, sr, level, message, exception);
    }

    @Override
    public PaxContext getPaxContext() {
        return m_context;
    }

    // org.osgi.service.cm.ManagedService

    @Override
    public void updated(Dictionary configuration) throws ConfigurationException {
        if (configuration == null) {
            configureDefaults();
            return;
        }

        Properties extracted = extractKeys(configuration);

        getConfigLock().writeLock().lock();
        ClassLoader loader = null;
        List<PaxAppenderProxy> proxies = null;
        try {
            loader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            LogManager.resetConfiguration();
            // If the updated() method is called without any log4j properties,
            // then keep the default/previous configuration.
            if (extracted.size() == 0) {
                configureDefaults();
                return;
            }
            try {
                PaxLoggingConfigurator configurator = new PaxLoggingConfigurator(m_bundleContext);
                configurator.doConfigure(extracted, LogManager.getLoggerRepository());
                proxies = configurator.getProxies();
                emptyConfiguration.set(false);
                m_configNotifier.configurationDone();
            } catch (Exception e) {
                LogLog.error("Configuration problem: " + e.getMessage(), e);
                m_configNotifier.configurationError(e);
            }
        } finally {
            getConfigLock().writeLock().unlock();
            Thread.currentThread().setContextClassLoader(loader);
        }
        // Avoid holding the configuration lock when starting proxies
        // It could cause deadlock if opening the service trackers block on the log because
        // the service itself wants to log anything
        if (proxies != null) {
            for (PaxAppenderProxy proxy : proxies) {
                proxy.open();
            }
        }

        List<java.util.logging.Logger> loggers = setLevelToJavaLogging(configuration);
        m_julLoggers.clear();
        m_julLoggers.addAll(loggers);
    }

    /**
     * This method is used by the FrameworkHandler to log framework events.
     *
     * @param bundle    The bundle that caused the event.
     * @param level     The level to be logged as.
     * @param message   The message.
     * @param exception The exception, if any otherwise null.
     */
    void log(Bundle bundle, int level, String message, Throwable exception) {
        log(bundle, null, level, message, exception);
    }

    private void log(Bundle bundle, ServiceReference sr, int level, String message, Throwable exception) {
        // failsafe in case bundle is null
        if (null == bundle && null != sr) {
            bundle = sr.getBundle();
        }

        String category = BackendSupport.category(bundle);

        PaxLogger logger = getLogger(bundle, category, "");
        if (level < LOG_ERROR) {
            logger.fatal(message, exception);
        } else {
            switch (level) {
                case LOG_ERROR:
                    logger.error(message, exception);
                    break;
                case LOG_WARNING:
                    logger.warn(message, exception);
                    break;
                case LOG_INFO:
                    logger.inform(message, exception);
                    break;
                case LOG_DEBUG:
                    logger.debug(message, exception);
                    break;
                default:
                    logger.trace(message, exception);
            }
        }
    }

    void handleEvents(Bundle bundle, ServiceReference sr, int level, String message, Throwable exception) {
        LogEntry entry = new LogEntryImpl(bundle, sr, level, message, exception);
        m_logReader.fireEvent(entry);

        // This should only be null for TestCases.
        if (m_eventAdmin != null) {
            m_eventAdmin.postEvent(bundle, level, entry, message, exception, sr, getPaxContext().getContext());
        }
    }

    private Properties extractKeys(Dictionary configuration) {
        Properties extracted = new Properties();
        Enumeration list = configuration.keys();
        while (list.hasMoreElements()) {
            Object obj = list.nextElement();
            if (obj instanceof String) {
                extractKey(extracted, configuration, obj);
            }
        }
        return extracted;
    }

    private void extractKey(Properties extracted, Dictionary configuration, Object obj) {
        String key = (String) obj;
        Object value = configuration.get(obj);
        if (key.startsWith("log4j")) {
            extracted.put(key, value);
        } else if (key.startsWith("pax.")) {
            if (PaxLoggingConstants.LOGGING_CFG_LOG_READER_SIZE_LEGACY.equals(key)
                    || PaxLoggingConstants.LOGGING_CFG_LOG_READER_SIZE.equals(key)) {
                try {
                    m_logReader.setMaxEntries(Integer.parseInt((String) value));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Default configuration, when Configuration Admin is not (yet) available.
     */
    private void configureDefaults() {
        if (!emptyConfiguration.compareAndSet(false, true)) {
            return;
        }
        try {
            String levelName = BackendSupport.defaultLogLevel(m_bundleContext);
            Level julLevel = BackendSupport.toJULLevel(levelName);

            m_logLevel = BackendSupport.convertLogServiceLevel(levelName);

            PaxLoggingConfigurator configurator = new PaxLoggingConfigurator(m_bundleContext);

            Properties defaultProperties = new Properties();
            defaultProperties.put("log4j.rootLogger", julLevel.getName() + ", A1");
            defaultProperties.put("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
            // "Time, Thread, Category, nested Context layout"
            defaultProperties.put("log4j.appender.A1.layout", "org.apache.log4j.TTCCLayout");

            // Extract System Properties prefixed with "pax.log4j", and drop the "pax." and include these
            extractSystemProperties(defaultProperties);

            configurator.doConfigure(defaultProperties, LogManager.getLoggerRepository());

            final java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");
            rootLogger.setLevel(julLevel);
            m_configNotifier.configurationDone();
        } catch (Exception e) {
            LogLog.error("Configuration problem: " + e.getMessage(), e);
            m_configNotifier.configurationError(e);
        }
    }

    private void extractSystemProperties(Properties output) {
        // ConcurrentModificationException friendly approach
        Properties properties = System.getProperties();
        for (String key : properties.stringPropertyNames()) {
            if (key.startsWith("pax.log4j")) {
                String value = properties.getProperty(key);
                key = key.substring(4);
                output.put(key, value);
            }
        }
    }

    // org.osgi.framework.ServiceFactory

    /**
     * <p>Use local class to delegate calls to underlying instance while keeping bundle reference.</p>
     * <p>We don't need anything special from bundle-scoped service ({@link ServiceFactory}) except the
     * reference to client bundle.</p>
     */
    public Object getService(final Bundle bundle, ServiceRegistration registration) {
        class ManagedPaxLoggingService
                implements PaxLoggingService, LogService, ManagedService {

            public void log(int level, String message) {
                PaxLoggingServiceImpl.this.log(bundle, null, level, message, null);
            }

            public void log(int level, String message, Throwable exception) {
                PaxLoggingServiceImpl.this.log(bundle, null, level, message, exception);
            }

            public void log(ServiceReference sr, int level, String message) {
                PaxLoggingServiceImpl.this.log(bundle, sr, level, message, null);
            }

            public void log(ServiceReference sr, int level, String message, Throwable exception) {
                PaxLoggingServiceImpl.this.log(bundle, sr, level, message, exception);
            }

            public int getLogLevel() {
                return PaxLoggingServiceImpl.this.getLogLevel();
            }

            public PaxLogger getLogger(Bundle myBundle, String category, String fqcn) {
                return PaxLoggingServiceImpl.this.getLogger(myBundle, category, fqcn);
            }

            public void updated(Dictionary configuration)
                    throws ConfigurationException {
                PaxLoggingServiceImpl.this.updated(configuration);
            }

            public PaxContext getPaxContext() {
                return PaxLoggingServiceImpl.this.getPaxContext();
            }
        }

        return new ManagedPaxLoggingService();
    }

    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service) {
        // nothing to do...
    }

    /**
     * Configure Java Util Logging according to the provided configuration.
     * Convert the log4j configuration to JUL config.
     *
     * It's necessary to do that, because with pax logging, JUL loggers are not replaced.
     * So we need to configure JUL loggers in order that log messages goes correctly to log Handlers.
     *
     * @param configuration    Properties coming from the configuration.
     */
    private static List<java.util.logging.Logger> setLevelToJavaLogging(final Dictionary configuration) {
        for (Enumeration enum_ = java.util.logging.LogManager.getLogManager().getLoggerNames(); enum_.hasMoreElements(); ) {
            String name = (String) enum_.nextElement();
            java.util.logging.Logger.getLogger(name).setLevel(null);
        }

        List<java.util.logging.Logger> loggers = new LinkedList<>();
        for (Enumeration keys = configuration.keys(); keys.hasMoreElements(); ) {
            String name = (String) keys.nextElement();
            String value = (String) configuration.get(name);
            if (name.equals("log4j.rootLogger")) {
                setJULLevel(java.util.logging.Logger.getLogger(""), value);
                // "global" comes from java.util.logging.Logger.GLOBAL_LOGGER_NAME, but that constant wasn't added until Java 1.6
                setJULLevel(java.util.logging.Logger.getLogger("global"), value);
            }

            if (name.startsWith("log4j.logger.")) {
                String packageName = name.substring("log4j.logger.".length());
                java.util.logging.Logger logger = java.util.logging.Logger.getLogger(packageName);
                setJULLevel(logger, value);
                loggers.add(logger);
            }
        }
        return loggers;
    }

    /**
     * Set the log level to the specified JUL logger.
     *
     * @param logger            The logger to configure
     * @param log4jLevelConfig    The value contained in the property file. (For example: "ERROR, file")
     */
    private static void setJULLevel(java.util.logging.Logger logger, String log4jLevelConfig) {
        String[] crumb = log4jLevelConfig.split("\\s*,\\s*");
        if (crumb.length > 0) {
            Level level = BackendSupport.toJULLevel(crumb[0].trim());
            logger.setLevel(level);
        }
    }

}
