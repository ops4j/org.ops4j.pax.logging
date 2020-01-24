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
import org.ops4j.pax.logging.spi.support.BackendSupport;
import org.ops4j.pax.logging.spi.support.ConfigurationNotifier;
import org.ops4j.pax.logging.spi.support.LogEntryImpl;
import org.ops4j.pax.logging.spi.support.LogReaderServiceImpl;
import org.ops4j.pax.logging.spi.support.OsgiUtil;
import org.ops4j.pax.logging.spi.support.PaxAppenderProxy;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogEntry;

/**
 * Log4J1 specific implementation of {@link PaxLoggingService}. It's a {@link ServiceFactory}, so each
 * bundle will get own instance of this service.
 */
public class PaxLoggingServiceImpl implements PaxLoggingService, LogService, ServiceFactory<Object> {

    private BundleContext m_bundleContext;

    private volatile ReadWriteLock m_configLock;
    private boolean locking = true;

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
        if (context == null)
            throw new IllegalArgumentException("bundleContext cannot be null");
        m_bundleContext = context;

        if (logReader == null)
            throw new IllegalArgumentException("logReader cannot be null");
        m_logReader = logReader;

        if (eventAdmin == null)
            throw new IllegalArgumentException("eventAdmin cannot be null");
        m_eventAdmin = eventAdmin;

        m_configNotifier = configNotifier;

        m_context = new PaxContext();

        String useLocks = OsgiUtil.systemOrContextProperty(context, PaxLoggingConstants.LOGGING_CFG_USE_LOCKS);
        if (!"false".equalsIgnoreCase(useLocks)) {
            // do not use locks ONLY if the property is "false". Otherwise (or if not set at all), use the locks
            m_configLock = new ReentrantReadWriteLock();
        }
    }

    /**
     * Shut down the Pax Logging service. This will reset the logging configuration entirely, so it should only be
     * used just before disposing of the service instance.
     */
    protected void shutdown() {
        LogManager.resetConfiguration();
        // TODO: shouldn't we explicitly close the PaxAppenderProxy trackers? (I know they're closed anywye on bundle stop...)
    }

    /**
     * Locks the configuration if needed
     * @param useWriteLock whether to use {@link ReadWriteLock#readLock()} ({@code false})
     * or {@link ReadWriteLock#writeLock()} ({@code true})
     */
    void lock(boolean useWriteLock) {
        ReadWriteLock lock = m_configLock;
        if (lock != null) {
            if (useWriteLock) {
                lock.writeLock().lock();
            } else {
                lock.readLock().lock();
            }
        }
    }

    /**
     * Unlocks the configuration if lock was used
     * @param useWriteLock whether to use {@link ReadWriteLock#readLock()} ({@code false})
     * or {@link ReadWriteLock#writeLock()} ({@code true})
     */
    void unlock(boolean useWriteLock) {
        ReadWriteLock lock = m_configLock;
        if (lock != null) {
            if (useWriteLock) {
                lock.writeLock().unlock();
            } else {
                lock.readLock().unlock();
            }
        }
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
    // these methods are actually never called directly, because the actual published
    // methods come from service factory produced object - which passes correct FQCN

    @Override
    public void log(int level, String message) {
        logImpl(null, level, message, null, "");
    }

    @Override
    public void log(int level, String message, Throwable exception) {
        logImpl(null, level, message, exception, "");
    }

    @Override
    public void log(ServiceReference sr, int level, String message) {
        logImpl(sr == null ? null : sr.getBundle(), level, message, null, "");
    }

    @Override
    public void log(ServiceReference sr, int level, String message, Throwable exception) {
        logImpl(sr == null ? null : sr.getBundle(), level, message, exception, "");
    }

    @Override
    public PaxContext getPaxContext() {
        return m_context;
    }

    /**
     * ManagedService-like method but not requiring Configuration Admin
     * @param configuration
     */
    public void updated(Dictionary<String, ?> configuration) {
        if (configuration == null) {
            // mind that there's no synchronization here
            configureDefaults();
            return;
        }

        Object useLocks = configuration.get(PaxLoggingConstants.PID_CFG_USE_LOCKS);
        if (!"false".equalsIgnoreCase(String.valueOf(useLocks))) {
            // do not use locks ONLY if the property is "false". Otherwise (or if not set at all), use the locks
            if (m_configLock == null) {
                m_configLock = new ReentrantReadWriteLock();
            }
        } else {
            m_configLock = null;
        }

        Properties extracted = extractKeys(configuration);

        lock(true);

        Exception problem = null;

        ClassLoader loader = null;
        List<PaxAppenderProxy> proxies = null;
        try {
            loader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            // If the updated() method is called without any log4j properties,
            // then keep the default/previous configuration.
            if (extracted.size() == 0) {
                configureDefaults();
                return;
            }
            try {
                LogManager.resetConfiguration();
                PaxLoggingConfigurator configurator = new PaxLoggingConfigurator(m_bundleContext);
                configurator.doConfigure(extracted, LogManager.getLoggerRepository());
                proxies = configurator.getProxies();
                emptyConfiguration.set(false);
            } catch (Exception e) {
                LogLog.error("Configuration problem: " + e.getMessage(), e);
                problem = e;
            }
        } finally {
            unlock(true);
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

        setLevelToJavaLogging(configuration);

        // do it outside of the lock
        if (problem == null) {
            m_configNotifier.configurationDone();
        } else {
            m_configNotifier.configurationError(problem);
        }
    }

    /**
     * Actual logging work is done here
     * @param bundle
     * @param level
     * @param message
     * @param exception
     * @param fqcn
     */
    private void logImpl(Bundle bundle, int level, String message, Throwable exception, String fqcn) {
        String category = BackendSupport.category(bundle);

        PaxLogger logger = getLogger(bundle, category, fqcn);
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

    void handleEvents(Bundle bundle, ServiceReference<?> sr, int level, String message, Throwable exception) {
        LogEntry entry = new LogEntryImpl(bundle, sr, level, message, exception);
        m_logReader.fireEvent(entry);

        // This should only be null for TestCases.
        if (m_eventAdmin != null) {
            m_eventAdmin.postEvent(bundle, level, entry, message, exception, sr, getPaxContext().getContext());
        }
    }

    private Properties extractKeys(Dictionary<String, ?> configuration) {
        Properties extracted = new Properties();
        Enumeration<String> list = configuration.keys();
        while (list.hasMoreElements()) {
            Object obj = list.nextElement();
            extractKey(extracted, configuration, obj);
        }
        return extracted;
    }

    private void extractKey(Properties extracted, Dictionary<String, ?> configuration, Object obj) {
        String key = (String) obj;
        Object value = configuration.get(obj);
        if (key.startsWith("log4j")) {
            extracted.put(key, value);
        } else if (key.startsWith("pax.")) {
            if (PaxLoggingConstants.PID_CFG_LOG_READER_SIZE_LEGACY.equals(key)
                    || PaxLoggingConstants.PID_CFG_LOG_READER_SIZE.equals(key)) {
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
    void configureDefaults() {
        if (!emptyConfiguration.compareAndSet(false, true)) {
            m_configNotifier.configurationDone();
            return;
        }
        try {
            LogManager.resetConfiguration();

            String levelName = BackendSupport.defaultLogLevel(m_bundleContext);
            Level julLevel = BackendSupport.toJULLevel(levelName);

            m_logLevel = BackendSupport.convertLogServiceLevel(levelName);

            PaxLoggingConfigurator configurator = new PaxLoggingConfigurator(m_bundleContext);

            Properties defaultProperties = new Properties();
            defaultProperties.put("log4j.rootLogger", BackendSupport.convertLogServiceLevel(m_logLevel) + ", A1");
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
    @Override
    public Object getService(final Bundle bundle, ServiceRegistration registration) {
        class ManagedPaxLoggingService implements PaxLoggingService, LogService {

            private final String FQCN = ManagedPaxLoggingService.class.getName();

            @Override
            public void log(int level, String message) {
                PaxLoggingServiceImpl.this.logImpl(bundle, level, message, null, FQCN);
            }

            @Override
            public void log(int level, String message, Throwable exception) {
                PaxLoggingServiceImpl.this.logImpl(bundle, level, message, exception, FQCN);
            }

            @Override
            public void log(ServiceReference sr, int level, String message) {
                PaxLoggingServiceImpl.this.logImpl(bundle, level, message, null, FQCN);
            }

            @Override
            public void log(ServiceReference sr, int level, String message, Throwable exception) {
                PaxLoggingServiceImpl.this.logImpl(bundle, level, message, exception, FQCN);
            }

            @Override
            public int getLogLevel() {
                return PaxLoggingServiceImpl.this.getLogLevel();
            }

            @Override
            public PaxLogger getLogger(Bundle myBundle, String category, String fqcn) {
                return PaxLoggingServiceImpl.this.getLogger(myBundle, category, fqcn);
            }

            @Override
            public PaxContext getPaxContext() {
                return PaxLoggingServiceImpl.this.getPaxContext();
            }
        }

        return new ManagedPaxLoggingService();
    }

    @Override
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
     * @param configuration Properties coming from the configuration.
     */
    private static void setLevelToJavaLogging(final Dictionary<String, ?> configuration) {
        for (Enumeration<String> enum_ = java.util.logging.LogManager.getLogManager().getLoggerNames(); enum_.hasMoreElements(); ) {
            String name = enum_.nextElement();
            java.util.logging.Logger.getLogger(name).setLevel(null);
        }

        for (Enumeration<String> keys = configuration.keys(); keys.hasMoreElements(); ) {
            String name = keys.nextElement();
            String value = (String) configuration.get(name);
            if (name.equals("log4j.rootLogger")) {
                setJULLevel(java.util.logging.Logger.getGlobal(), value);
                setJULLevel(java.util.logging.Logger.getLogger(""), value);
                // "global" comes from java.util.logging.Logger.GLOBAL_LOGGER_NAME, but that constant wasn't added until Java 1.6
                setJULLevel(java.util.logging.Logger.getLogger("global"), value);
            }

            if (name.startsWith("log4j.logger.")) {
                String packageName = name.substring("log4j.logger.".length());
                java.util.logging.Logger logger = java.util.logging.Logger.getLogger(packageName);
                setJULLevel(logger, value);
            }
        }
    }

    /**
     * Set the log level to the specified JUL logger.
     *
     * @param logger The logger to configure
     * @param log4jLevelConfig The value contained in the property file. (For example: "ERROR, file")
     */
    private static void setJULLevel(java.util.logging.Logger logger, String log4jLevelConfig) {
        String[] crumb = log4jLevelConfig.split("\\s*,\\s*");
        if (crumb.length > 0) {
            Level level = BackendSupport.toJULLevel(crumb[0].trim());
            logger.setLevel(level);
        }
    }

}
