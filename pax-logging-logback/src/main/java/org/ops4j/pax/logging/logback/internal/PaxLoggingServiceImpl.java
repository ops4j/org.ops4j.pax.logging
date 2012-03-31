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

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusListener;
import ch.qos.logback.core.status.WarnStatus;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.ops4j.pax.logging.EventAdminPoster;
import org.ops4j.pax.logging.PaxContext;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Dictionary;
import java.util.Locale;

/**
 * An implementation of PaxLoggingService that delegates to Logback.
 *
 * <p>
 * This implementation is registered with the
 * OSGi ConfigAdmin with a configuration PID of "org.ops4j.pax.logging". That configuration should have a property
 * "org.ops4j.pax.logging.logback.config.file" which should be a path to a Logback Joran XML configuration file.
 *
 * <p>
 * This class has a fair bit of code copied from from org.ops4j.pax.logging.service.internal.PaxLoggingServiceImpl v1.6.0.
 * Changes include:
 * <ul>
 *     <li>massive overhaul for logback vs. log4j</li>
 *     <li>configuration is completely different</li>
 *     <li>removed setLevelToJavaLogging() because logback already has it's own support for synchronizing with JUL.
 *         See below!</li>
 * </ul>
 *
 * <p>
 * To sync java.util.logging logger levels with Logback logger levels, be sure to include this in your logback.xml:
 * <pre>
 *    &lt;contextListener class="ch.qos.logback.classic.jul.LevelChangePropagator"&gt;
 *        &lt;resetJUL&gt;true&lt;/resetJUL&gt;
 *    &lt;/contextListener&gt;
 * </pre>
 * This is an important performance optimization, as discussed in the <a href="http://logback.qos.ch/manual/configuration.html#LevelChangePropagator"></a>Logback docs</a>
 * </p>
 *
 * @author Chris Dolan
 */
public class PaxLoggingServiceImpl implements PaxLoggingService, org.knopflerfish.service.log.LogService,
        ManagedService, ServiceFactory { // if you add an interface here, add it to the ManagedService below too

    private final LogReaderServiceAccess m_logReader;
    private final EventAdminPoster m_eventAdmin;
    private final BundleContext m_bundleContext;
    private final PaxContext m_paxContext;
    private final LoggerContext m_logbackContext;
    private final String m_fqcn;

    private int m_logLevel = LOG_DEBUG;
    private static final String DEFAULT_SERVICE_LOG_LEVEL = "org.ops4j.pax.logging.DefaultServiceLog.level";
    private static final String LOGBACK_CONFIG_FILE_KEY = "org.ops4j.pax.logging.logback.config.file";
    public static final String LOGGER_CONTEXT_BUNDLECONTEXT_KEY = "org.ops4j.pax.logging.logback.bundlecontext";

    public PaxLoggingServiceImpl(@NonNull BundleContext bundleContext, @NonNull LogReaderServiceAccess logReader,
                                 @NonNull EventAdminPoster eventAdmin)
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

        m_paxContext = new PaxContext();
        //m_logbackContext = ContextSelectorStaticBinder.getSingleton().getContextSelector().getLoggerContext();
        m_logbackContext = new LoggerContext();
        m_logbackContext.start();

        // not strictly necessary because org.apache.felix.cm.impl.ConfigurationManager will configure us, but this
        // is a safe precaution. In a typical run, we will reset the logback configuration four times:
        //  1) here, in the constructor
        //  2) via Felix when the service is added: updated(null)
        //  3) again from Felix when the config file is discovered: updated(non-null)
        //  4) from stop()
        configureDefaults();
    }

    public PaxLogger getLogger( Bundle bundle, String category, String fqcn )
    {
        Logger logger = m_logbackContext.getLogger(category == null ? org.slf4j.Logger.ROOT_LOGGER_NAME : category);
        return new PaxLoggerImpl( bundle, logger, fqcn, this, new PaxEventHandler() {
            public void handleEvents( Bundle bundle, @Nullable ServiceReference sr, int level, String message, Throwable exception ) {
                LogEntry entry = new LogEntryImpl( bundle, sr, level, message, exception );
                m_logReader.fireEvent( entry );
                m_eventAdmin.postEvent( bundle, level, entry, message, exception, sr, getPaxContext().getContext() );
            }
        } );
    }

    public int getLogLevel()
    {
        return m_logLevel;
    }

    public void log( int level, String message )
    {
        logImpl( null, level, message, null, m_fqcn );
    }

    public void log( int level, String message, @Nullable Throwable exception )
    {
        logImpl( null, level, message, exception, m_fqcn );
    }

    public void log( ServiceReference sr, int level, String message )
    {
        logImpl( sr == null ? null : sr.getBundle(), level, message, null, m_fqcn );
    }

    public void log( ServiceReference sr, int level, String message, @Nullable Throwable exception )
    {
        logImpl( sr == null ? null : sr.getBundle(), level, message, exception, m_fqcn );
    }

    /**
     * This method is used by the FrameworkHandler to log framework events.
     *
     * @param bundle    The bundle that caused the event.
     * @param level     The level to be logged as.
     * @param message   The message.
     * @param exception The exception, if any otherwise null.
     */
    void log( @Nullable Bundle bundle, int level, String message, @Nullable Throwable exception )
    {
        logImpl( bundle, level, message, exception, m_fqcn );
    }

    private void logImpl( @Nullable Bundle bundle, int level, String message,
                      @Nullable Throwable exception, String fqcn )
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
        try{
            PaxLogger logger = getLogger( bundle, category, fqcn );
            switch( level )
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
                    logger.warn( "Undefined Level: " + level + " : " + message, exception );
            }
        } catch (RuntimeException e) {
            m_logbackContext.getStatusManager().add(new WarnStatus("Runtime logging failure", m_logbackContext, e));
        }
    }

    public void updated( Dictionary configuration )
        throws ConfigurationException
    {

        if( configuration == null )
        {
            configureDefaults();
            return;
        }

        Object configfile = configuration.get(LOGBACK_CONFIG_FILE_KEY);
        if (configfile != null) {
            File f = new File(configfile.toString());
            if (f.exists()) {
                try {
                    InputStream is = new FileInputStream(f);
                    try {
                        configureLogback(is);
                    } finally {
                        is.close();
                    }
                } catch (IOException e) {
                    m_logbackContext.getStatusManager().add(new WarnStatus("Error loading Logback configuration from '" + f + "'", m_logbackContext, e));
                }
            } else {
                m_logbackContext.getStatusManager().add(new WarnStatus("Configuration said to load '" + f + "' but that file does not exist", m_logbackContext));
                configureLogback(null);
            }
        } else {
            configureLogback(null);
        }

        configurePax(configuration);
    }

    private void configureDefaults()
    {
        ConsoleAppender<ILoggingEvent> consoleAppender = configureLogbackDefaults();
        consoleAppender.addInfo("default: setting up console logging at WARN level");

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

    private void configureLogback(@Nullable InputStream configFile) {
        ConsoleAppender<ILoggingEvent> consoleAppender = configureLogbackDefaults();

        if (configFile != null) {

            // get a better representation of the hostname than what Logback provides in the HOSTNAME property
            try {
                String hostName = InetAddress.getLocalHost().getCanonicalHostName();
                int n = hostName.indexOf('.');
                if(n >= 0)
                    hostName = hostName.substring(0, n);
                m_logbackContext.putProperty("HOSTNAMENONCANON", hostName.toLowerCase(Locale.ENGLISH));
            } catch (UnknownHostException e) {
                // ignore
            }

            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(m_logbackContext);
            try {
                configurator.doConfigure(configFile);
                m_logbackContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).detachAppender(consoleAppender);
            } catch (JoranException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private ConsoleAppender<ILoggingEvent> configureLogbackDefaults() {
        // simplest possible useful configuration, make sure there's minimal time when there are no appenders!
        ConsoleAppender<ILoggingEvent> consoleAppender = makeConsoleAppender();

        //System.err.println("reset"); new Exception().printStackTrace(System.err);
        m_logbackContext.reset();
        // minimize time between these two lines of code
        m_logbackContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).addAppender(consoleAppender);

        m_logbackContext.putObject(LOGGER_CONTEXT_BUNDLECONTEXT_KEY, m_bundleContext);
        m_logbackContext.getStatusManager().add(new StatusListener() {
            public void addStatusEvent(Status status) {
                if (status.getLevel() == Status.ERROR || status.getLevel() == Status.WARN) {
                    System.err.println(status);
                    Throwable t = status.getThrowable();
                    if (t == null)
                        t = new Exception();
                    t.printStackTrace(System.err);
                }
            }
        });

        return consoleAppender;
    }

    private ConsoleAppender<ILoggingEvent> makeConsoleAppender() {
        // This code is similar to ch.qos.logback.classic.BasicConfigurator, but adds a filter

        ThresholdFilter filter = new ThresholdFilter();
        filter.setContext(m_logbackContext);
        filter.setLevel("WARN");
        filter.start();

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(m_logbackContext);
        encoder.setPattern("%d %-5level [%file:%line] %msg - %logger{20}%n");
        encoder.start();

        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<ILoggingEvent>();
        consoleAppender.setContext(m_logbackContext);
        consoleAppender.setName("DEFAULT-CONSOLE");
        consoleAppender.addFilter(filter);
        consoleAppender.setEncoder(encoder);
        consoleAppender.start();

        return consoleAppender;
    }

    private void configurePax(Dictionary config) {
        Object size = config.get("pax.logging.entries.size");
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
                PaxLoggingServiceImpl.this.logImpl( bundle, level, message, null, fqcn );
            }

            public void log( int level, String message, @Nullable Throwable exception )
            {
                PaxLoggingServiceImpl.this.logImpl( bundle, level, message, exception, fqcn );
            }

            public void log( ServiceReference sr, int level, String message )
            {
                Bundle b = bundle == null && sr != null ? sr.getBundle() : bundle;
                PaxLoggingServiceImpl.this.logImpl( b, level, message, null, fqcn );
            }

            public void log( ServiceReference sr, int level, String message, Throwable exception )
            {
                Bundle b = bundle == null && sr != null ? sr.getBundle() : bundle;
                PaxLoggingServiceImpl.this.logImpl( b, level, message, exception, fqcn );
            }

            public int getLogLevel()
            {
                return PaxLoggingServiceImpl.this.getLogLevel();
            }

            public PaxLogger getLogger( Bundle myBundle, String category, String fqcn )
            {
                return PaxLoggingServiceImpl.this.getLogger( myBundle, category, fqcn );
            }

            public void updated( Dictionary configuration )
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
        else
        {
            return LOG_DEBUG;
        }
    }

    public void stop() {
        m_logbackContext.putObject(LOGGER_CONTEXT_BUNDLECONTEXT_KEY, null);
        m_logbackContext.stop();
    }

}
