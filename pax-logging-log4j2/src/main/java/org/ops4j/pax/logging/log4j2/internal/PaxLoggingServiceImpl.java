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
package org.ops4j.pax.logging.log4j2.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.async.AsyncLoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.config.plugins.util.PluginManager;
import org.apache.logging.log4j.core.config.properties.PropertiesConfigurationFactory;
import org.apache.logging.log4j.layout.template.json.JsonTemplateLayout;
import org.apache.logging.log4j.layout.template.json.resolver.LoggerResolverFactory;
import org.apache.logging.log4j.layout.template.json.util.RecyclerFactoryConverter;
import org.apache.logging.log4j.status.StatusData;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.PaxPropertySource;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.ops4j.pax.logging.EventAdminPoster;
import org.ops4j.pax.logging.PaxContext;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingConstants;
import org.ops4j.pax.logging.PaxLoggingService;
import org.ops4j.pax.logging.log4j2.internal.bridges.PaxOsgiAppender;
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

public class PaxLoggingServiceImpl implements PaxLoggingService, ServiceFactory<Object> {

    private static final String LOGGER_CONTEXT_NAME = "pax-logging";

    static {
//        PluginManager.addPackage("org.apache.logging.log4j.core");
        // We don't have to add "org.apache.logging.log4j.core", because this package will be handled
        // using default cache file "/META-INF/org/apache/logging/log4j/core/config/plugins/Log4j2Plugins.dat"
        // taken unchanged from "org.apache.logging.log4j:log4j-core".
        PluginManager.addPackage(PaxOsgiAppender.class.getPackage().getName());
        PluginManager.addPackage(JsonTemplateLayout.class.getPackage().getName());
        PluginManager.addPackage(LoggerResolverFactory.class.getPackage().getName());
        PluginManager.addPackage(RecyclerFactoryConverter.class.getPackage().getName());
    }

    private final BundleContext m_bundleContext;

    private ReadWriteLock m_configLock;

    // LogReaderService registration as defined by org.osgi.service.log package
    private final LogReaderServiceImpl m_logReader;

    // pax-logging-log4j2 specific PaxContext for all MDC access
    private final PaxContext m_paxContext;

    // optional bridging into Event Admin service
    private final EventAdminPoster m_eventAdmin;

    // optional notification mechanism for configuration events
    private final ConfigurationNotifier m_configNotifier;

    // Log level (actually a threashold) for this entire service.
    private LogLevel m_r7LogLevel = LogLevel.DEBUG;

    // the main org.apache.logging.log4j.core.LoggerContext
    private LoggerContext m_log4jContext;

    // there's no need to run configureDefaults() more than once. That was happening in constructor
    // and millisecond later during registration of ManagedService, upon receiving empty org.ops4j.pax.logging
    // configuration
    private final AtomicBoolean emptyConfiguration = new AtomicBoolean(false);

    private volatile boolean closed;

    private volatile boolean errorsAsException = false;

    private final String fqcn = getClass().getName();

    private boolean m_async;
    private Dictionary<String, String> defaultConfiguration = null;

    public PaxLoggingServiceImpl(BundleContext bundleContext, LogReaderServiceImpl logReader, EventAdminPoster eventAdmin, ConfigurationNotifier configNotifier) {
        if (bundleContext == null)
            throw new IllegalArgumentException("bundleContext cannot be null");
        m_bundleContext = bundleContext;

        m_logReader = logReader;

        m_eventAdmin = eventAdmin;

        m_configNotifier = configNotifier;

        m_paxContext = new PaxContext();

        String useLocks = OsgiUtil.systemOrContextProperty(bundleContext, PaxLoggingConstants.LOGGING_CFG_USE_LOCKS);
        if (!"false".equalsIgnoreCase(useLocks)) {
            // do not use locks ONLY if the property is "false". Otherwise (or if not set at all), use the locks
            m_configLock = new ReentrantReadWriteLock();
        }

        String errorsAsExceptionValue = OsgiUtil.systemOrContextProperty(bundleContext, PaxLoggingConstants.LOGGING_CFG_LOG4J2_ERRORS_AS_EXCEPTION);
        if ("true".equalsIgnoreCase(errorsAsExceptionValue)) {
            errorsAsException = true;
        }
    }

    // org.ops4j.pax.logging.PaxLoggingService

    /**
     * Shut down the Pax Logging service.  This will reset the logging configuration entirely, so it should only be
     * used just before disposing of the service instance.
     */
    public synchronized void shutdown() {
        StatusLogger.getLogger().reset();
        m_log4jContext.stop();
        closed = true;
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
        Logger log4j2Logger;
        if (category == null) {
            log4j2Logger = m_log4jContext.getRootLogger();
        } else {
            log4j2Logger = m_log4jContext.getLogger(category);
        }
        return new PaxLoggerImpl(bundle, log4j2Logger, fqcn, this, printfFormatting);
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
    synchronized void updated(Dictionary<String, ?> configuration) {
        if (closed) {
            return;
        }
        if (configuration == null && defaultConfiguration == null) {
            configureDefaults();
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

        Object configfile = configuration.get(PaxLoggingConstants.PID_CFG_LOG4J2_CONFIG_FILE);

        // async property choses org.apache.logging.log4j.core.LoggerContext, so it has to be
        // extracted early
        Object asyncProperty = configuration.get(PaxLoggingConstants.PID_CFG_LOG4J2_ASYNC);
        boolean async = asyncProperty != null && Boolean.parseBoolean(asyncProperty.toString());
        if (async) {
            try {
                getClass().getClassLoader().loadClass("com.lmax.disruptor.EventFactory");
            } catch (Exception e) {
                StatusLogger.getLogger().warn("Asynchronous loggers defined, but the disruptor library is not available.  Reverting to synchronous loggers.", e);
                async = false;
            }
        }

        if (configfile instanceof String) {
            // configure using external (XML or properties) file
            configureLog4J2(async, (String) configfile, null);
        } else {
            // configure using inline (in org.ops4j.pax.logging PID) configuration
            configureLog4J2(async, null, configuration);
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
     * Default configuration, when Configuration Admin is not (yet) available.
     */
    void configureDefaults() {
        String levelName = BackendSupport.defaultLogLevel(m_bundleContext);
        java.util.logging.Level julLevel = BackendSupport.toJULLevel(levelName);

        m_r7LogLevel = BackendSupport.convertR7LogLevel(levelName, LogLevel.DEBUG);

        final java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");
        rootLogger.setLevel(julLevel);

        configureLog4J2(false, null, null);
    }

    /**
     * Configure using external XML/properties file name or properties. When neither are specified, default
     * configuration is used.
     * @param async
     * @param configFileName
     * @param configuration
     */
    private void configureLog4J2(boolean async, String configFileName, Dictionary<String, ?> configuration) {
        Throwable problem = null;

        File file = null;
        if (configFileName != null) {
            file = new File(configFileName);
        }
        if (file != null && !file.isFile()) {
            if (configFileName.contains(",")) {
                // PAXLOGGING-308 can be explicitly set to multiple files
                file = null;
            } else {
                StatusLogger.getLogger().warn("Configuration file '" + file + "' is not available. Default configuration will be used.");
                file = null;
                configFileName = null;
            }
        }

        if (configFileName == null && configuration == null && !emptyConfiguration.compareAndSet(false, true)) {
            // no need to reconfigure default configuration
            m_configNotifier.configurationDone();
            return;
        }

        // check if there are empty properties to shortcut this path
        Properties props = null;
        if (configuration != null) {
            // properties passed directly
            props = new Properties();
            Object defaultsFile = configuration.get(PaxLoggingConstants.PID_CFG_LOG4J2_DEFAULTS_FILE);
            if (defaultsFile != null) {
                // merge with properties from defaults file.
                try (InputStream inputStream = new FileInputStream(defaultsFile.toString())) {
                    props.load(inputStream);
                } catch (IOException e) {
                    StatusLogger.getLogger().error("Error reading defaults file.", e);
                }
            }
            for (Enumeration<String> keys = configuration.keys(); keys.hasMoreElements(); ) {
                String key = keys.nextElement();
                props.setProperty(key, configuration.get(key).toString());
            }
            props = PropertiesUtil.extractSubset(props, "log4j2");

            if (props.size() == 0 && emptyConfiguration.get()) {
                // no need to even stop current context
                m_configNotifier.configurationDone();
                return;
            }
        }

        try {
            lock(true);

            if (m_log4jContext != null) {
                // Logback: ch.qos.logback.classic.LoggerContext.reset()
                // Log4J2: org.apache.logging.log4j.core.AbstractLifeCycle.stop()
                m_log4jContext.stop();
            }

            if (m_log4jContext == null || async != m_async) {
                m_log4jContext = async ? new AsyncLoggerContext(LOGGER_CONTEXT_NAME) : new LoggerContext(LOGGER_CONTEXT_NAME);
                m_async = async;
            }

            ClassLoader old = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

                if (props != null) {
                    if (props.size() == 0) {
                        configureDefaults();
                        return;
                    }

                    emptyConfiguration.set(false);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    props.store(baos, null);
                    ConfigurationSource src = new ConfigurationSource(new ByteArrayInputStream(baos.toByteArray()));
                    Configuration config = new PropertiesConfigurationFactory().getConfiguration(m_log4jContext, src);

                    m_log4jContext.start(config);

                    StatusLogger.getLogger().info("Log4J2 configured using configuration from passed properties");
                } else if (configFileName != null) {
                    // configuration using externally specified file. This is the way to make Karaf's
                    // etc/org.ops4j.pax.logging.cfg much simpler without this cumbersome properties
                    // file format.
                    // file may have been specified as system/context property "org.ops4j.pax.logging.property.file"
                    // or as single, "org.ops4j.pax.logging.log4j2.config.file" property in etc/org.ops4j.pax.logging.cfg
                    emptyConfiguration.set(false);

                    ConfigurationFactory factory = ConfigurationFactory.getInstance();
                    // ".json", ".jsn": org.apache.logging.log4j.core.config.json.JsonConfigurationFactory
                    // ".properties": org.apache.logging.log4j.core.config.properties.PropertiesConfigurationFactory
                    // ".xml", "*": org.apache.logging.log4j.core.config.xml.XmlConfigurationFactory
                    // ".yml", ".yaml": org.apache.logging.log4j.core.config.yaml.YamlConfigurationFactory

                    // File name (or comma-separated file names) is not passed to getConfiguration, but is made
                    // available in high-priority org.apache.logging.log4j.util.PropertySource
                    PaxPropertySource.updateFileConfiguration(configFileName);
                    // reload() is needed to let the source apply the settings
                    PropertiesUtil.getProperties().reload();
                    Configuration config = factory.getConfiguration(m_log4jContext, LOGGER_CONTEXT_NAME, null);

                    m_log4jContext.start(config);

                    StatusLogger.getLogger().info("Log4J2 configured using file '" + configFileName + "'.");
                } else {
                    // default configuration - Log4J2 specific.
                    // even if LoggerContext by default has DefaultConfiguration set, it's necessary to pass
                    // new DefaultConfiguration during start. Otherwise
                    // org.apache.logging.log4j.core.LoggerContext.reconfigure() will be called with empty
                    // org.apache.logging.log4j.core.config.properties.PropertiesConfiguration
                    m_log4jContext.start(new DefaultConfiguration());

                    Level l = Level.getLevel(m_r7LogLevel.name());
                    if (l == null) {
                        l = Level.DEBUG;
                    }

                    m_log4jContext.getConfiguration().getLoggerConfig(LogManager.ROOT_LOGGER_NAME).setLevel(l);

                    StatusLogger.getLogger().info("Log4J2 configured using default configuration.");
                }

                if (errorsAsException) {
                    // in addition to printing (which happens automatically), throw excception if there's some
                    // ERROR inside StatusData
                    for (StatusData sd : StatusLogger.getLogger().getStatusData()) {
                        if (sd.getLevel().isMoreSpecificThan(Level.ERROR) && sd.getThrowable() != null) {
                            throw sd.getThrowable();
                        }
                    }
                }
            } catch (Throwable e) {
                StatusLogger.getLogger().error("Log4J2 configuration problem: " + e.getMessage(), e);
                problem = e;
            } finally {
                Thread.currentThread().setContextClassLoader(old);
            }

            m_log4jContext.updateLoggers();
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
    }

    /**
     * Configure Java Util Logging according to the provided configuration.
     * Convert the log4j configuration to JUL config.
     *
     * It's necessary to do that, because with pax logging, JUL loggers are not replaced.
     * So we need to configure JUL loggers in order that log messages goes correctly to log Handlers.
     */
    private void setLevelToJavaLogging() {
        for (Enumeration<String> enum_ = java.util.logging.LogManager.getLogManager().getLoggerNames(); enum_.hasMoreElements(); ) {
            String name = enum_.nextElement();
            java.util.logging.Logger.getLogger(name).setLevel(null);
        }

        for (Logger logger : m_log4jContext.getLoggers()) {
            if (logger != null) {
                Level l = logger.getLevel();
                java.util.logging.Level julLevel = BackendSupport.toJULLevel(l.name());
                if (logger.getName().equals(LogManager.ROOT_LOGGER_NAME)) {
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
