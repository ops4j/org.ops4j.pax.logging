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
import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.async.AsyncLoggerConfig;
import org.apache.logging.log4j.core.async.AsyncLoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.plugins.util.PluginManager;
import org.apache.logging.log4j.core.config.properties.PropertiesConfigurationFactory;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.knopflerfish.service.log.LogService;
import org.ops4j.pax.logging.EventAdminPoster;
import org.ops4j.pax.logging.PaxContext;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingService;
import org.ops4j.pax.logging.log4j2.appender.PaxOsgiAppender;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.log.LogEntry;

public class PaxLoggingServiceImpl
    implements PaxLoggingService, LogService, ManagedService, ServiceFactory
{

    public static final String DEFAULT_SERVICE_LOG_LEVEL = "org.ops4j.pax.logging.DefaultServiceLog.level";
    public static final String MAX_ENTRIES = "pax.logging.entries.size";
    public static final String LOG4J2_CONFIG_FILE_KEY = "org.ops4j.pax.logging.log4j2.config.file";
    public static final String LOG4J2_ASYNC_KEY = "org.ops4j.pax.logging.log4j2.async";

    private static final String LOGGER_CONTEXT_NAME = "pax-logging";

    // see org.apache.logging.log4j.core.config.properties.PropertiesConfigurationBuilder.createLogger()
    private static final String LOG4J2_ROOT_LOGGER_LEVEL_PROPERTY = "log4j2.rootLogger.level";
    private static final String LOG4J2_LOGGER_PROPERTY_PREFIX = "log4j2.logger.";

    private final LogReaderServiceImpl m_logReader;
    private final EventAdminPoster m_eventAdmin;
    private final BundleContext m_bundleContext;
    private final PaxContext m_paxContext;
    private final String m_fqcn;


    private final ConcurrentMap<String, PaxLoggerImpl> m_loggers = new ConcurrentHashMap<String, PaxLoggerImpl>();
    private boolean m_async;
    private LoggerContext m_log4jContext;
    private int m_logLevel = LOG_DEBUG;
    private boolean closed;

    static {
        PluginManager.addPackage("org.apache.logging.log4j.core");
        PluginManager.addPackage(PaxOsgiAppender.class.getPackage().getName());
    }

    public PaxLoggingServiceImpl( BundleContext bundleContext, LogReaderServiceImpl logReader, EventAdminPoster eventAdmin )
    {
        m_fqcn = getClass().getName();

        if (bundleContext == null)
            throw new IllegalArgumentException("bundleContext cannot be null");
        m_bundleContext = bundleContext;

        if (logReader == null)
            throw new IllegalArgumentException("logReader cannot be null");
        m_logReader = logReader;

        if (eventAdmin == null)
            throw new IllegalArgumentException("eventAdmin cannot be null");
        m_eventAdmin = eventAdmin;

        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );
            m_paxContext = new PaxContext();
            configureDefaults();
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( old );
        }
    }

    /**
     * Shut down the Pax Logging service.  This will reset the logging configuration entirely, so it should only be
     * used just before disposing of the service instance.
     */
    protected synchronized void shutdown() {
        m_log4jContext.stop();
        closed = true;
    }

    public PaxLogger getLogger( Bundle bundle, String category, String fqcn )
    {
        String name = category == null ? LogManager.ROOT_LOGGER_NAME : category;
        PaxLoggerImpl logger = m_loggers.get( name );
        if (logger == null) {
            logger = new PaxLoggerImpl( bundle, m_log4jContext.getLogger(name), fqcn, this );
            m_loggers.putIfAbsent( name, logger );
        }
        return m_loggers.get( name );
    }

    public synchronized void updated( Dictionary<String,?> configuration ) throws ConfigurationException
    {
        if( closed )
        {
            return;
        }
        if( configuration == null )
        {
            configureDefaults();
            return;
        }

        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );
            doUpdate( configuration );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( old );
        }
    }

    protected void doUpdate( Dictionary<String,?> configuration ) throws ConfigurationException
    {
        boolean async = false;
        Object asyncObj = configuration.get(LOG4J2_ASYNC_KEY);
        if (asyncObj != null) {
            async = Boolean.parseBoolean(asyncObj.toString());
        }
        if (async) {
            try {
                getClass().getClassLoader().loadClass("com.lmax.disruptor.EventFactory");
            } catch (Exception e) {
                StatusLogger.getLogger().warn("Asynchronous loggers defined, but the disruptor library is not available.  Reverting to synchronous loggers.", e);
                async = false;
            }
        }
        if (async != m_async) {
            m_log4jContext.stop();
            if (async) {
                m_log4jContext = new AsyncLoggerContext(LOGGER_CONTEXT_NAME);
            } else {
                m_log4jContext = new LoggerContext(LOGGER_CONTEXT_NAME);
            }
        }

        Configuration config;
        Object configfile = configuration.get(LOG4J2_CONFIG_FILE_KEY);
     if (configfile != null) {

            // Set log4j.configurationFile here instead of passing the file location as the final parameter of
            // getConfiguration. This allows users to make use of log4j2's composite behaviour. This is due to it
            // only being handled/configured if the configuration file is set via property. See code at:
            // https://github.com/apache/logging-log4j2/blob/097426a2154a6079cddd897502b6fb3ce5d50338/log4j-core/src/main/java/org/apache/logging/log4j/core/config/ConfigurationFactory.java#L402
            System.setProperty("log4j.configurationFile", configfile.toString());
            config = ConfigurationFactory.getInstance().getConfiguration(m_log4jContext,
                      LOGGER_CONTEXT_NAME, null);
        } else {
            try {
                Properties props = new Properties();
                for (Enumeration<String> keys = configuration.keys(); keys.hasMoreElements();) {
                    String key = keys.nextElement();
                    props.setProperty(key, configuration.get(key).toString());
                }
                props = PropertiesUtil.extractSubset(props, "log4j2");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                props.store(baos, null);
                ConfigurationSource src = new ConfigurationSource(new ByteArrayInputStream(baos.toByteArray()));
                config = new PropertiesConfigurationFactory().getConfiguration(m_log4jContext, src);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


        m_log4jContext.start(config);

        if (async != m_async) {
            for (Map.Entry<String, PaxLoggerImpl> entry : m_loggers.entrySet()) {
                String name = entry.getKey();
                PaxLoggerImpl logger = entry.getValue();
                logger.setDelegate( m_log4jContext.getLogger( name ) );
            }
            m_async = async;
        }

        configurePax(configuration);
        if (configfile != null) {
            updateLevels(configuration);
        }
        setLevelToJavaLogging( configuration );
    }

    private void updateLevels(Dictionary<String, ?> config) {
        Configuration configuration = m_log4jContext.getConfiguration();
        for ( Enumeration<String> keys = config.keys(); keys.hasMoreElements(); )
        {
            String name = keys.nextElement();
            String loggerName;
            Level level = null;
            if ( name.equals( LOG4J2_ROOT_LOGGER_LEVEL_PROPERTY ) )
            {
                loggerName = LogManager.ROOT_LOGGER_NAME;
                level = Level.toLevel( (String) config.get( LOG4J2_ROOT_LOGGER_LEVEL_PROPERTY ) );
            }
            else if ( name.startsWith( LOG4J2_LOGGER_PROPERTY_PREFIX )
                    && name.endsWith ( ".name" ))
            {
                loggerName = (String) config.get( name );
                level = Level.toLevel( (String) config.get( name.replaceFirst("\\.name$", ".level") ) );
            }
            else
            {
                continue;
            }
            if ( level != null ) {
                LoggerConfig loggerConfig = configuration.getLoggerConfig( loggerName );
                loggerConfig.setLevel(level);
            }
        }
        m_log4jContext.updateLoggers();
    }

    private void configurePax(Dictionary<String, ?> config) {
        Object size = config.get(MAX_ENTRIES);
        if ( null != size )
        {
            try
            {
                m_logReader.setMaxEntries( Integer.parseInt( (String) size ) );
            }
            catch( Exception e )
            {
                e.printStackTrace();
            }
        }
    }

    private void configureDefaults()
    {
        if (m_log4jContext == null)
        {
            m_log4jContext = new LoggerContext(LOGGER_CONTEXT_NAME);
        }
        m_log4jContext.start(new DefaultConfiguration());

        String levelName;
        levelName = m_bundleContext.getProperty( DEFAULT_SERVICE_LOG_LEVEL );
        if( levelName == null )
        {
            levelName = "DEBUG";
        }
        else
        {
            levelName = levelName.trim();
        }
        m_logLevel = convertLevel( levelName );
    }

    public int getLogLevel()
    {
        return m_logLevel;
    }

    public void log( int level, String message )
    {
        log( level, message, null );
    }

    public void log( int level, String message, Throwable exception )
    {
        log( null, level, message, exception, m_fqcn );
    }

    public void log( ServiceReference sr, int level, String message )
    {
        log( sr == null ? null : sr.getBundle(), level, message, null, m_fqcn );
    }

    public void log( ServiceReference sr, int level, String message, Throwable exception )
    {
        log( sr == null ? null : sr.getBundle(), level, message, exception, m_fqcn );
    }

    /**
     * This method is used by the FrameworkHandler to log framework events.
     *
     * @param bundle    The bundle that caused the event.
     * @param level     The level to be logged as.
     * @param message   The message.
     * @param exception The exception, if any otherwise null.
     */
    void log( Bundle bundle, int level, String message, Throwable exception )
    {
        log( bundle, level, message, exception, m_fqcn );
    }

    private void log( Bundle bundle, int level, String message, Throwable exception, String fqcn )
    {
        String category = "[undefined]";
        if( bundle != null )
        {
            category = bundle.getSymbolicName();
            if( null == category )
            {
                category = "[bundle@" + bundle.getBundleId() + ']';
            }
        }

        PaxLogger logger = getLogger( bundle, category, fqcn );
        if( level < LOG_ERROR )
        {
            logger.fatal( message, exception );
        }
        else
        {
            switch (level)
            {
            case LOG_ERROR:
                logger.error( message, exception );
                break;
            case LOG_WARNING:
                logger.warn( message, exception );
                break;
            case LOG_INFO:
                logger.inform( message, exception );
                break;
            case LOG_DEBUG:
                logger.debug( message, exception );
                break;
            default:
                logger.trace( message, exception );
            }
        }
    }

    void handleEvents( Bundle bundle, ServiceReference sr, int level, String message, Throwable exception )
    {
        LogEntry entry = new LogEntryImpl( bundle, sr, level, message, exception );
        m_logReader.fireEvent(entry);
        m_eventAdmin.postEvent( bundle, level, entry, message, exception, sr, getPaxContext().getContext() );
    }

    /*
     * use local class to delegate calls to underlying instance while keeping bundle reference
     */
    public Object getService( final Bundle bundle, ServiceRegistration registration )
    {
        class ManagedPaxLoggingService
            implements PaxLoggingService, LogService, ManagedService
        {
            private final String fqcn = getClass().getName();

            public void log( int level, String message )
            {
                PaxLoggingServiceImpl.this.log(bundle, level, message, null, fqcn);
            }

            public void log( int level, String message, Throwable exception )
            {
                PaxLoggingServiceImpl.this.log(bundle, level, message, exception, fqcn);
            }

            public void log( ServiceReference sr, int level, String message )
            {
                Bundle b = bundle == null && sr != null ? sr.getBundle() : bundle;
                PaxLoggingServiceImpl.this.log(b, level, message, null, fqcn);
            }

            public void log( ServiceReference sr, int level, String message, Throwable exception )
            {
                Bundle b = bundle == null && sr != null ? sr.getBundle() : bundle;
                PaxLoggingServiceImpl.this.log(b, level, message, exception, fqcn);
            }

            public int getLogLevel()
            {
                return PaxLoggingServiceImpl.this.getLogLevel();
            }

            public PaxLogger getLogger( Bundle myBundle, String category, String fqcn )
            {
                return PaxLoggingServiceImpl.this.getLogger( myBundle, category, fqcn );
            }

            public void updated( Dictionary<String, ?> configuration )
                throws ConfigurationException
            {
                PaxLoggingServiceImpl.this.updated( configuration );
            }

            public PaxContext getPaxContext()
            {
                return PaxLoggingServiceImpl.this.getPaxContext();
            }
        }

        return new ManagedPaxLoggingService();
    }

    public void ungetService( Bundle bundle, ServiceRegistration registration, Object service )
    {
        // nothing to do...
    }

    public PaxContext getPaxContext()
    {
        return m_paxContext;
    }

    private static int convertLevel( String levelName )
    {
        if( "DEBUG".equals( levelName ) )
        {
            return LOG_DEBUG;
        }
        else if( "INFO".equals( levelName ) )
        {
            return LOG_INFO;
        }
        else if( "ERROR".equals( levelName ) )
        {
            return LOG_ERROR;
        }
        else if( "WARN".equals( levelName ) )
        {
            return LOG_WARNING;
        }
        else if ( "OFF".equals( levelName ) || "NONE".equals( levelName ) )
        {
            return 0;
        }
        else
        {
            return LOG_DEBUG;
        }
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
    private static void setLevelToJavaLogging( final Dictionary<String, ?> configuration )
    {
        for( Enumeration enum_ = java.util.logging.LogManager.getLogManager().getLoggerNames(); enum_.hasMoreElements();) {
            String name = (String) enum_.nextElement();
            java.util.logging.Logger.getLogger(name).setLevel( null );
        }

        for( Enumeration<String> keys = configuration.keys(); keys.hasMoreElements(); )
        {
            String name = keys.nextElement();
            if (name.equals( LOG4J2_ROOT_LOGGER_LEVEL_PROPERTY ))
            {
                String value = (String) configuration.get( LOG4J2_ROOT_LOGGER_LEVEL_PROPERTY );
                setJULLevel( java.util.logging.Logger.getLogger(""), value );
                // "global" comes from java.util.logging.Logger.GLOBAL_LOGGER_NAME, but that constant wasn't added until Java 1.6
                setJULLevel( java.util.logging.Logger.getLogger("global"), value );
            }

            if (name.startsWith( LOG4J2_LOGGER_PROPERTY_PREFIX )
                    && name.endsWith ( ".name" ))
            {
                String value = (String) configuration.get( name.replaceFirst("\\.name$", ".level") );
                String packageName = (String) configuration.get( name );
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
    private static void setJULLevel( java.util.logging.Logger logger, String log4jLevelConfig )
    {
        java.util.logging.Level level = log4jLevelToJULLevel( log4jLevelConfig );
        logger.setLevel( level );
    }

    private static java.util.logging.Level log4jLevelToJULLevel( final String levelProperty )
    {
        if( levelProperty == null ) {
            return java.util.logging.Level.INFO;
        }
        if( levelProperty.contains("OFF") )
        {
            return java.util.logging.Level.OFF;
        }
        else if( levelProperty.contains("FATAL") )
        {
            return java.util.logging.Level.SEVERE;
        }
        else if( levelProperty.contains("ERROR") )
        {
            return java.util.logging.Level.SEVERE;
        }
        else if( levelProperty.contains("WARN") )
        {
            return java.util.logging.Level.WARNING;
        }
        else if( levelProperty.contains("INFO") )
        {
            return java.util.logging.Level.INFO;
        }
        else if( levelProperty.contains("DEBUG") )
        {
            return java.util.logging.Level.FINE;
        }
        else if( levelProperty.contains("TRACE") )
        {
            return java.util.logging.Level.FINEST;
        }
        return java.util.logging.Level.INFO;
    }
}
