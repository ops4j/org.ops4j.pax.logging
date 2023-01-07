/*
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

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Locale;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import ch.qos.logback.classic.BasicConfigurator;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.classic.spi.LogbackServiceProvider;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.status.WarnStatus;
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
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogLevel;

/**
 * An implementation of PaxLoggingService that delegates to Logback.
 *
 * This implementation is registered with the
 * OSGi ConfigAdmin with a configuration PID of "org.ops4j.pax.logging". That configuration should have a property
 * "org.ops4j.pax.logging.logback.config.file" which should be a path to a Logback Joran XML configuration file.
 *
 * This class has a fair bit of code copied from from org.ops4j.pax.logging.service.internal.PaxLoggingServiceImpl v1.6.0.
 * Changes include:
 * <ul>
 *     <li>massive overhaul for logback vs. log4j</li>
 *     <li>configuration is completely different</li>
 *     <li>removed setLevelToJavaLogging() because logback already has it's own support for synchronizing with JUL.
 *         See below!</li>
 *     <li>Unification of logging backends in 1.11+</li>
 * </ul>
 *
 * To sync java.util.logging logger levels with Logback logger levels, be sure to include this in your logback.xml:
 * <pre>
 *    &lt;contextListener class="ch.qos.logback.classic.jul.LevelChangePropagator"&gt;
 *        &lt;resetJUL&gt;true&lt;/resetJUL&gt;
 *    &lt;/contextListener&gt;
 * </pre>
 * This is an important performance optimization, as discussed in the <a href="http://logback.qos.ch/manual/configuration.html#LevelChangePropagator">Logback docs</a>
 *
 * @author Chris Dolan
 */
public class PaxLoggingServiceImpl implements PaxLoggingService, ServiceFactory<Object> { // if you add an interface here, add it to the ManagedService below too

    // pax-logging-logback-only key to find BundleContext
    public static final String LOGGER_CONTEXT_BUNDLECONTEXT_KEY = "org.ops4j.pax.logging.logback.bundlecontext";

    private static LogbackServiceProvider logbackServiceProvider;

    private final BundleContext m_bundleContext;

    private volatile ReadWriteLock m_configLock;

    // LogReaderService registration as defined by org.osgi.service.log package
    private final LogReaderServiceImpl m_logReader;

    // pax-logging-logback specific PaxContext for all MDC access
    private final PaxContext m_paxContext;

    // optional bridging into Event Admin service
    private final EventAdminPoster m_eventAdmin;

    // optional notification mechanism for configuration events
    private final ConfigurationNotifier m_configNotifier;

    // Log level (actually a threashold) for this entire service.
    private LogLevel m_r7LogLevel = LogLevel.DEBUG;

    // choose between LoggerContext managed here or managed inside logback-classic's
    // org.slf4j.impl.StaticLoggerBinder#defaultLoggerContext
    private final boolean m_useStaticContext;

    // if static context is not used, here's the one we use
    private final LoggerContext m_logbackContext;

    // static configuration file URL when not using Configuration Admin
    private final String m_staticConfigFile;

    // there's no need to run configureDefaults() more than once. That was happening in constructor
    // and millisecond later during registration of ManagedService, upon receiving empty org.ops4j.pax.logging
    // configuration
    private final AtomicBoolean emptyConfiguration = new AtomicBoolean(false);

    private final PaxLogger logLog;

    private final String fqcn = getClass().getName();

    private Dictionary<String, String> defaultConfiguration = null;

    public PaxLoggingServiceImpl(BundleContext bundleContext, LogReaderServiceImpl logReader,
                                 EventAdminPoster eventAdmin, ConfigurationNotifier configNotifier,
                                 PaxLogger logLog) {
        if (bundleContext == null)
            throw new IllegalArgumentException("bundleContext cannot be null");
        m_bundleContext = bundleContext;

        m_logReader = logReader;

        m_eventAdmin = eventAdmin;

        m_configNotifier = configNotifier;

        this.logLog = logLog;

        m_paxContext = new PaxContext();

        String useLocks = OsgiUtil.systemOrContextProperty(bundleContext, PaxLoggingConstants.LOGGING_CFG_USE_LOCKS);
        if (!"false".equalsIgnoreCase(useLocks)) {
            // do not use locks ONLY if the property is "false". Otherwise (or if not set at all), use the locks
            m_configLock = new ReentrantReadWriteLock();
        }

        m_useStaticContext = Boolean.parseBoolean(bundleContext.getProperty(PaxLoggingConstants.LOGGING_CFG_LOGBACK_USE_STATIC_CONTEXT));
        if (m_useStaticContext) {
            // org.slf4j.impl.StaticLoggerBinder is included in logback-classic and private-packaged in
            // pax-logging-logback - it's not the same class as the one included in pax-logging-api
            if (logbackServiceProvider == null) {
                logbackServiceProvider = new LogbackServiceProvider();
                logbackServiceProvider.initialize();
                if (CoreConstants.SCHEDULED_EXECUTOR_POOL_SIZE == 1) {
                    // https://jira.qos.ch/browse/LOGBACK-1716
                    ScheduledThreadPoolExecutor executor = (ScheduledThreadPoolExecutor) ((LoggerContext) logbackServiceProvider.getLoggerFactory()).getScheduledExecutorService();
                    executor.setCorePoolSize(2);
                }
            }
            m_logbackContext = (LoggerContext) logbackServiceProvider.getLoggerFactory();
        } else {
            m_logbackContext = new LoggerContext();
            if (CoreConstants.SCHEDULED_EXECUTOR_POOL_SIZE == 1) {
                // https://jira.qos.ch/browse/LOGBACK-1716
                ScheduledThreadPoolExecutor executor = (ScheduledThreadPoolExecutor) m_logbackContext.getScheduledExecutorService();
                executor.setCorePoolSize(2);
            }
            m_logbackContext.start();
        }

        m_staticConfigFile = OsgiUtil.systemOrContextProperty(bundleContext, PaxLoggingConstants.LOGGING_CFG_LOGBACK_CONFIGURATION_FILE);
    }

    // org.ops4j.pax.logging.PaxLoggingService

    /**
     * Shut down the Pax Logging service. Cleans up {@link LoggerContext}.
     */
    public void shutdown() {
        m_logbackContext.removeObject(LOGGER_CONTEXT_BUNDLECONTEXT_KEY);
        if (!m_useStaticContext) {
            m_logbackContext.stop();
        } else {
            // static context should be reset
            m_logbackContext.reset();

            // but let's add status listener, so we detect (and log through fallback/default logger)
            // attempts to log through unconfigured loggers - that's *only* for static logback context
            m_logbackContext.getStatusManager().add(this::logLogbackStatus);
        }
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
        return getLogger(bundle, category, fqcn, false);
    }

    @Override
    public LogLevel getLogLevel() {
        return m_r7LogLevel;
    }

    // org.osgi.service.log.LogService
    // these methods are actually never called directly (except in tests), because the actual published
    // methods come from service factory produced object

    @Override
    public void log(int level, String message) {
        logImpl(null, level, message, null, fqcn);
    }

    @Override
    public void log(int level, String message, Throwable exception) {
        logImpl(null, level, message, exception, fqcn);
    }

    @Override
    public void log(ServiceReference sr, int level, String message) {
        logImpl(sr == null ? null : sr.getBundle(), level, message, null, fqcn);
    }

    @Override
    public void log(ServiceReference sr, int level, String message, Throwable exception) {
        logImpl(sr == null ? null : sr.getBundle(), level, message, exception, fqcn);
    }

    @Override
    public PaxContext getPaxContext() {
        return m_paxContext;
    }

    // org.osgi.service.log.LoggerFactory

    @Override
    public org.osgi.service.log.Logger getLogger(String name) {
        return getLogger(null, name, PaxLoggerImpl.FQCN);
    }

    @Override
    public org.osgi.service.log.Logger getLogger(Class<?> clazz) {
        return getLogger(null, clazz.getName(), PaxLoggerImpl.FQCN);
    }

    @Override
    public <L extends org.osgi.service.log.Logger> L getLogger(String name, Class<L> loggerType) {
        return getLogger(null, name, loggerType);
    }

    @Override
    public <L extends org.osgi.service.log.Logger> L getLogger(Class<?> clazz, Class<L> loggerType) {
        return getLogger(null, clazz.getName(), loggerType);
    }

    @Override
    public <L extends org.osgi.service.log.Logger> L getLogger(Bundle bundle, String name, Class<L> loggerType) {
        return getLogger(bundle, name, loggerType, PaxLoggerImpl.FQCN);
    }

    private <L extends org.osgi.service.log.Logger> L getLogger(Bundle bundle, String name, Class<L> loggerType, String fqcn) {
        if (loggerType == org.osgi.service.log.Logger.class) {
            return loggerType.cast(getLogger(bundle, name, fqcn, false));
        } else if (loggerType == org.osgi.service.log.FormatterLogger.class) {
            return loggerType.cast(getLogger(bundle, name, fqcn, true));
        }
        throw new IllegalArgumentException("Can't obtain logger with type " + loggerType);
    }

    /**
     * The only method that creates new instance of {@link PaxLoggerImpl}. Used by log() methods from R6 and directly
     * by getLogger() methods from R7.
     * @param bundle
     * @param category
     * @param fqcn
     * @param printfFormatting whether to use Slf4J ({@code "{}"} - {@code false}) or printf formatting ({@code "%s"} - {@code true}).
     * @return
     */
    private PaxLogger getLogger(Bundle bundle, String category, String fqcn, boolean printfFormatting) {
        Logger logbackLogger;
        if (category == null) {
            logbackLogger = m_logbackContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        } else {
            logbackLogger = m_logbackContext.getLogger(category);
        }
        return new PaxLoggerImpl(bundle, logbackLogger, fqcn, this, printfFormatting);
    }

    /**
     * When there's system/context property specified using {@link PaxLoggingConstants#LOGGING_CFG_PROPERTY_FILE},
     * and ConfigurationAdmin is available, Pax Logging may first get null configuration. When "default configuration"
     * is set before that, we'll use it instead of empty configuration.
     * @param config
     */
    public void setDefaultConfiguration(Dictionary<String, String> config) {
        this.defaultConfiguration = config;
    }

    /**
     * ManagedService-like method but not requiring Configuration Admin
     * @param configuration
     */
    public void updated(Dictionary<String, ?> configuration) {
        if (configuration == null && defaultConfiguration == null) {
            // maintain the existing configuration if there's such file set
            if (m_staticConfigFile == null) {
                configureDefaults();
            }
            return;
        }
        if (configuration == null) {
            configuration = defaultConfiguration;
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

        Object configfile = configuration.get(PaxLoggingConstants.PID_CFG_LOGBACK_CONFIG_FILE);

        if (m_staticConfigFile != null && (configfile == null || m_staticConfigFile.equals(configfile))) {
            // maintain the existing configuration
            return;
        } else {
            configureLogback(configfile instanceof String ? (String) configfile : null);
        }

        // pick up pax-specific configuration of LogReader
        configurePax(configuration);
    }

    /**
     * Actual logging work is done here
     * @param bundle
     * @param level
     * @param message
     * @param exception
     * @param fqcn
     */
    @SuppressWarnings("deprecation")
    private void logImpl(Bundle bundle, int level, String message, Throwable exception, String fqcn) {
        String category = BackendSupport.category(bundle);

        try {
            PaxLogger logger = getLogger(bundle, category, fqcn);
            if (exception != null) {
                if (level < LOG_ERROR) {
                    logger.audit(message, exception);
                } else {
                    switch (level) {
                        case LOG_ERROR:
                            logger.error(message, exception);
                            break;
                        case LOG_WARNING:
                            logger.warn(message, exception);
                            break;
                        case LOG_INFO:
                            logger.info(message, exception);
                            break;
                        case LOG_DEBUG:
                            logger.debug(message, exception);
                            break;
                        default:
                            logger.trace(message, exception);
                    }
                }
            } else {
                if (level < LOG_ERROR) {
                    logger.audit(message);
                } else {
                    switch (level) {
                        case LOG_ERROR:
                            logger.error(message);
                            break;
                        case LOG_WARNING:
                            logger.warn(message);
                            break;
                        case LOG_INFO:
                            logger.info(message);
                            break;
                        case LOG_DEBUG:
                            logger.debug(message);
                            break;
                        default:
                            logger.trace(message);
                    }
                }
            }
        } catch (RuntimeException e) {
            m_logbackContext.getStatusManager().add(new WarnStatus("Runtime logging failure", m_logbackContext, e));
        }
    }

    void handleEvents(String name, Bundle bundle, ServiceReference<?> sr, LogLevel level, String message, Throwable exception) {
        LogEntry entry = m_logReader != null || m_eventAdmin != null
                ? new LogEntryImpl(name, bundle, sr, level, message, exception) : null;
        if (m_logReader != null) {
            m_logReader.fireEvent(entry);
        }

        if (m_eventAdmin != null) {
            m_eventAdmin.postEvent(bundle, level, entry, message, exception, sr, getPaxContext().getContext());
        }
    }

    /**
     * Default configuration, when Configuration Admin is not (yet) available. May choose
     * staticly configured Logback XML file or just plain defaults (which are used if file is not accessible).
     */
    void configureDefaults() {
        String levelName = BackendSupport.defaultLogLevel(m_bundleContext);
        java.util.logging.Level julLevel = BackendSupport.toJULLevel(levelName);

        m_r7LogLevel = BackendSupport.convertR7LogLevel(levelName, LogLevel.DEBUG);

        final java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");
        rootLogger.setLevel(julLevel);

        configureLogback(m_staticConfigFile);
    }

    /**
     * Main configuration method using XML file - specified either as static file configured as context
     * property or in {@code org.ops4j.pax.logging} PID.
     * @param configFileName
     */
    private void configureLogback(String configFileName) {
        m_logbackContext.getStatusManager().clear();

        lock(true);

        Throwable problem = null;

        try {
            File file = null;
            if (configFileName != null) {
                file = new File(configFileName);
            }
            if (file != null && !file.isFile()) {
                Status warn = new WarnStatus("Configuration file '" + file + "' is not available. Default configuration will be used.", null);
                m_logbackContext.getStatusManager().add(warn);
                file = null;
            }

            if (file == null && !emptyConfiguration.compareAndSet(false, true)) {
                // no need to reconfigure default configuration
                m_configNotifier.configurationDone();
                return;
            }

            // Fortunately logback has ch.qos.logback.classic.LoggerContext.reset()
            m_logbackContext.reset();

            m_logbackContext.putObject(LOGGER_CONTEXT_BUNDLECONTEXT_KEY, m_bundleContext);

            try {
                if (file == null) {
                    Configurator configurator = new BasicConfigurator();
                    configurator.setContext(m_logbackContext);
                    configurator.configure(m_logbackContext);

                    Level l = Level.toLevel(m_r7LogLevel.name(), Level.DEBUG);

                    Logger rootLogger = m_logbackContext.getLogger(Logger.ROOT_LOGGER_NAME);
                    rootLogger.setLevel(l);

                    InfoStatus info = new InfoStatus("Logback configured using default configuration.", this);
                    m_logbackContext.getStatusManager().add(info);
                } else {
                    // get a better representation of the hostname than what Logback provides in the HOSTNAME property
                    try {
                        String hostName = InetAddress.getLocalHost().getCanonicalHostName();
                        int n = hostName.indexOf('.');
                        if (n >= 0)
                            hostName = hostName.substring(0, n);
                        m_logbackContext.putProperty("HOSTNAMENONCANON", hostName.toLowerCase(Locale.ENGLISH));
                    } catch (UnknownHostException ignored) {
                    }

                    // This is where the Logback magic happens
                    JoranConfigurator configurator = new JoranConfigurator();
                    configurator.setContext(m_logbackContext);
                    ClassLoader tccl = Thread.currentThread().getContextClassLoader();
                    try {
                        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
                        configurator.doConfigure(file);
                        emptyConfiguration.set(false);
                    } finally {
                        Thread.currentThread().setContextClassLoader(tccl);
                    }

                    InfoStatus info = new InfoStatus("Logback configured using file '" + file + "'.", this);
                    m_logbackContext.getStatusManager().add(info);
                }
            } catch (Throwable e) {
                Status error = new ErrorStatus("Logback configuration problem: " + e.getMessage(), e);
                m_logbackContext.getStatusManager().add(error);
                problem = e;
            }
        } finally {
            unlock(true);
        }

        setLevelToJavaLogging();

        // do it outside of the lock
        if (problem == null) {
            m_configNotifier.configurationDone();
        } else {
            m_configNotifier.configurationError(problem);
        }

        // do what Logback does with ch.qos.logback.core.util.StatusPrinter.print()
        logbackStatus();

        // now we're ready to log - let's clear the status
        m_logbackContext.getStatusManager().clear();

        // register listener to catch status errors
        m_logbackContext.getStatusManager().add(this::logLogbackStatus);
    }

    private Level extractLevel(String log4jLevelConfig) {
        String[] config = log4jLevelConfig.split("\\s*,\\s*");
        return Level.toLevel(config[0]);
    }

    /**
     * Uses current {@link LoggerContext} and updates JUL log levels
     */
    private void setLevelToJavaLogging() {
        for (Enumeration<String> enum_ = java.util.logging.LogManager.getLogManager().getLoggerNames(); enum_.hasMoreElements(); ) {
            String name = enum_.nextElement();
            java.util.logging.Logger.getLogger(name).setLevel(null);
        }

        for (Logger logger : m_logbackContext.getLoggerList()) {
            if (logger != null) {
                Level l = logger.getEffectiveLevel();
                java.util.logging.Level julLevel = BackendSupport.toJULLevel(l.toString());
                if (org.slf4j.Logger.ROOT_LOGGER_NAME.equals(logger.getName())
                        || "".equals(logger.getName())) {
                    java.util.logging.Logger.getGlobal().setLevel(julLevel);
                    java.util.logging.Logger.getLogger("").setLevel(julLevel);
                    // "global" comes from java.util.logging.Logger.GLOBAL_LOGGER_NAME, but that constant wasn't added until Java 1.6
                    java.util.logging.Logger.getLogger("global").setLevel(julLevel);
                } else {
                    java.util.logging.Logger.getLogger(logger.getName()).setLevel(julLevel);
                }
            }
        }
    }

    private void configurePax(Dictionary<String, ?> config) {
        Object size = config.get(PaxLoggingConstants.PID_CFG_LOG_READER_SIZE);
        if (size == null) {
            size = config.get(PaxLoggingConstants.PID_CFG_LOG_READER_SIZE_LEGACY);
        }
        if (null != size && m_logReader != null) {
            try {
                m_logReader.setMaxEntries(Integer.parseInt((String) size));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Outputs the collected status (from {@link ch.qos.logback.core.status.StatusManager})
     */
    private void logbackStatus() {
        StatusManager sm = m_logbackContext.getStatusManager();
        if (sm != null && logLog.isDebugEnabled()) {
            for (Status status : sm.getCopyOfStatusList()) {
                logLogbackStatus(status);
            }
        }
    }

    private void logLogbackStatus(Status status) {
        Throwable t = status.getThrowable();
        if (t != null) {
            switch (status.getLevel()) {
                case Status.ERROR:
                    logLog.error(status.getMessage(), status.getThrowable());
                    break;
                case Status.WARN:
                    logLog.warn(status.getMessage(), status.getThrowable());
                    break;
                case Status.INFO:
                    logLog.info(status.getMessage(), status.getThrowable());
                    break;
            }
        } else {
            switch (status.getLevel()) {
                case Status.ERROR:
                    logLog.error(status.getMessage());
                    break;
                case Status.WARN:
                    logLog.warn(status.getMessage());
                    break;
                case Status.INFO:
                    logLog.info(status.getMessage());
                    break;
            }
        }
    }

    // org.osgi.framework.ServiceFactory

    /**
     * Use local class to delegate calls to underlying instance while keeping bundle reference.
     *
     * We don't need anything special from bundle-scoped service ({@link ServiceFactory}) except the
     * reference to client bundle.
     */
    @Override
    public Object getService(final Bundle bundle, ServiceRegistration registration) {
        class ManagedPaxLoggingService implements PaxLoggingService {

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
                Bundle b = bundle == null && sr != null ? sr.getBundle() : bundle;
                PaxLoggingServiceImpl.this.logImpl(b, level, message, null, FQCN);
            }

            @Override
            public void log(ServiceReference sr, int level, String message, Throwable exception) {
                Bundle b = bundle == null && sr != null ? sr.getBundle() : bundle;
                PaxLoggingServiceImpl.this.logImpl(b, level, message, exception, FQCN);
            }

            @Override
            public LogLevel getLogLevel() {
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

            @Override
            public org.osgi.service.log.Logger getLogger(String name) {
                return PaxLoggingServiceImpl.this.getLogger(bundle, name, PaxLoggerImpl.FQCN);
            }

            @Override
            public org.osgi.service.log.Logger getLogger(Class<?> clazz) {
                return PaxLoggingServiceImpl.this.getLogger(bundle, clazz.getName(), PaxLoggerImpl.FQCN);
            }

            @Override
            public <L extends org.osgi.service.log.Logger> L getLogger(String name, Class<L> loggerType) {
                return PaxLoggingServiceImpl.this.getLogger(bundle, name, loggerType, PaxLoggerImpl.FQCN);
            }

            @Override
            public <L extends org.osgi.service.log.Logger> L getLogger(Class<?> clazz, Class<L> loggerType) {
                return PaxLoggingServiceImpl.this.getLogger(bundle, clazz.getName(), loggerType, PaxLoggerImpl.FQCN);
            }

            @Override
            public <L extends org.osgi.service.log.Logger> L getLogger(Bundle bundle, String name, Class<L> loggerType) {
                return PaxLoggingServiceImpl.this.getLogger(bundle, name, loggerType, PaxLoggerImpl.FQCN);
            }
        }

        return new ManagedPaxLoggingService();
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service) {
        // nothing to do...
    }

}
