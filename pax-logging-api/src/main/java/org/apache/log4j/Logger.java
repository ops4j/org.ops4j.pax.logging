/*
 * Copyright 1999,2004 The Apache Software Foundation.
 * Copyright 2006, Niclas Hedhman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.log4j;

import org.apache.log4j.internal.MessageFormatter;
import org.apache.log4j.spi.LoggerFactory;
import org.ops4j.pax.logging.internal.DefaultServiceLog;
import org.ops4j.pax.logging.internal.FallbackLogFactory;
import org.ops4j.pax.logging.OSGIPaxLoggingManager;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingManager;
import org.osgi.framework.BundleContext;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * This is the central class in the log4j package. Most logging
 * operations, except configuration, are done through this class.
 * <p>
 *
 * <b>NOTE: This is NOT the original file of Log4J Logger, and is only here to provide a static mapping
 * to the Pax Logging system running under OSGi.</b>
 * </p>
 *
 * <p>
 * The client code that wishes to use this adaptation of Log4J and have the log output
 * to be directed to the Pax Logging Service backend, which is driven by the real Log4J, it is necessary to;
 * <ul>
 * <li>ensure that log4j.jar is <b>NOT</b> included in your bundle jar.</li>
 * <li>include the pax-logging-client.jar into the client bundle.</li>
 * <li>update your Manifest.MF to import the org.ops4j.pax.logging package.</li>
 * <li>Initiate this class by setting the Bundle Context.</li>
 * </ul>
 * </p>
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author Niclas Hedhman
 */
public class Logger extends Category
{

    private static final String LOG4J_FQCN = Logger.class.getName();

    private static PaxLoggingManager m_paxLogging;
    private static Map m_loggers;

    static
    {
        m_loggers = Collections.synchronizedMap( new WeakHashMap() );
    }

    public static void setBundleContext( BundleContext ctx )
    {
        m_paxLogging = new OSGIPaxLoggingManager( ctx );
        // We need to instruct all loggers to ensure the SimplePaxLoggingManager is replaced.
        Set entrySet = m_loggers.entrySet();
        Iterator iterator = entrySet.iterator();
        while( iterator.hasNext() )
        {
            Map.Entry entry = (Entry) iterator.next();
            Logger logger = (Logger) entry.getKey();
            String name = (String) entry.getValue();
            logger.setPaxLoggingManager( m_paxLogging, name );
        }
        m_paxLogging.open();
    }

    private void setPaxLoggingManager( PaxLoggingManager loggingManager, String name )
    {
        m_delegate = loggingManager.getLogger( name, LOG4J_FQCN );
    }

    /**
     * Lifecycle method to release any resources held.
     */
    public static void release()
    {
    }

    /**
     * This constructor creates a new <code>Logger</code> instance and sets
     * its name.
     *
     * <p>
     * It is intended to be used by sub-classes only. You should not getLogger
     * loggers directly.
     * </p>
     *
     * @param delegate The logger that bridge to the Pax Logging system.
     */
    private Logger( PaxLogger delegate )
    {
        super( delegate );
    }

    /**
     * Retrieve a logger by name. If the named logger already exists, then the
     * existing instance will be reutrned. Otherwise, a new instance is created.
     *
     * <p>By default, loggers do not have a set level but inherit it from their
     * ancestors. This is one of the central features of log4j.
     * </p>
     *
     * @param name The name of the logger to retrieve.
     *
     * @return the Logger instance of the given name.
     */
    public static Logger getLogger( String name )
    {
        PaxLogger paxLogger;
        if( m_paxLogging == null )
        {
            paxLogger = FallbackLogFactory.createFallbackLog( null, name );
        }
        else
        {
            paxLogger = m_paxLogging.getLogger( name, LOG4J_FQCN );
        }
        Logger logger = new Logger( paxLogger );
        m_loggers.put( logger, name );
        return logger;
    }

    /**
     * Shorthand for <code>{@link #getLogger(Class) getLogger(clazz.getName())}</code>.
     *
     * @param clazz The name of <code>clazz</code> will be used as the name of
     *              the logger to retrieve.  See {@link #getLogger(String)} for
     *              more detailed information.
     *
     * @return the Logger instance for the given class.
     */
    public static Logger getLogger( Class clazz )
    {
        return getLogger( clazz.getName() );
    }

    /**
     * Return the root of logger for the current hierarchy.
     *
     * <p>The root logger is always instantiated and available. It's name is
     * "root".
     * </p>
     *
     * <p>Nevertheless, note that calling <code>Logger.getLogger("root")</code>
     * does not retrieve the root logger but a logger just under root named
     * "root".
     * </p>
     *
     * @return the top-most Logger instance, which does not have a name.
     */
    public static Logger getRootLogger()
    {
        return getLogger( "" );
    }

    /**
     * This method is equivalent to <code>getLogger( String name )</code> and the
     * LoggerFactory is ignored. The method exists only to improve compatibility with
     * Log4J.
     *
     * @param name    The name of the logger to retrieve.
     * @param factory <b>Ignored!</b>
     *
     * @return the Logger instance of the given name.
     *
     * @since Pax Logging 0.9.5
     */
    public static Logger getLogger( String name, LoggerFactory factory )
    {
        return getLogger( name );
    }

    /**
     * Log a message object with the TRACE level.
     *
     * @param message the message object to log.
     *
     * @see #debug(Object) for an explanation of the logic applied.
     * @since 1.2.12
     */
    public void trace( Object message )
    {
        if( m_delegate.isTraceEnabled() && message != null )
        {
            m_delegate.trace( message.toString(), null );
        }
    }

    /**
     * Log a message object with the <code>TRACE</code> level including the
     * stack trace of the {@link Throwable}<code>t</code> passed as parameter.
     *
     * <p>
     * See {@link #debug(Object)} form for more detailed information.
     * </p>
     *
     * @param message the message object to log.
     * @param t       the exception to log, including its stack trace.
     *
     * @since 1.2.12
     */
    public void trace( Object message, Throwable t )
    {
        if( m_delegate.isTraceEnabled() )
        {
            if( message != null )
            {
                m_delegate.trace( message.toString(), t );
            }
            else
            {
                m_delegate.trace( null, t );
            }
        }
    }

    /**
     * Log a message with the <code>TRACE</code> level with message formatting
     * done according to the value of <code>messagePattern</code> and
     * <code>arg</code> parameters.
     * <p>
     * This form avoids superflous parameter construction. Whenever possible,
     * you should use this form instead of constructing the message parameter
     * using string concatenation.
     *
     * @param messagePattern The message pattern which will be parsed and formatted
     * @param arg            The argument to replace the formatting element, i,e,
     *                       the '{}' pair within <code>messagePattern</code>.
     *
     * @since 1.3
     */
    public void trace( Object messagePattern, Object arg )
    {
        if( m_delegate.isTraceEnabled() && messagePattern != null )
        {
            String msgStr = (String) messagePattern;
            msgStr = MessageFormatter.format( msgStr, arg );
            m_delegate.trace( msgStr, null );
        }
    }

    /**
     * Log a message with the <code>TRACE</code> level with message formatting
     * done according to the messagePattern and the arguments arg1 and arg2.
     * <p>
     * This form avoids superflous parameter construction. Whenever possible,
     * you should use this form instead of constructing the message parameter
     * using string concatenation.
     *
     * @param messagePattern The message pattern which will be parsed and formatted
     * @param arg1           The first argument to replace the first formatting element
     * @param arg2           The second argument to replace the second formatting element
     *
     * @since 1.3
     */
    public void trace( String messagePattern, Object arg1, Object arg2 )
    {
        if( m_delegate.isTraceEnabled() )
        {
            String msgStr = MessageFormatter.format( messagePattern, arg1, arg2 );
            m_delegate.trace( msgStr, null );
        }
    }

    /**
     * Log a message with the <code>FATAL</code> level with message formatting
     * done according to the messagePattern and the arguments arg1 and arg2.
     * <p>
     * This form avoids superflous parameter construction. Whenever possible,
     * you should use this form instead of constructing the message parameter
     * using string concatenation.
     *
     * @param messagePattern The message pattern which will be parsed and formatted
     * @param arg1           The first argument to replace the first formatting element
     * @param arg2           The second argument to replace the second formatting element
     *
     * @since 1.3
     */
    public void fatal( String messagePattern, Object arg1, Object arg2 )
    {
        if( m_delegate.isFatalEnabled() )
        {
            String msgStr = MessageFormatter.format( messagePattern, arg1, arg2 );
            m_delegate.fatal( msgStr, null );
        }
    }

    /**
     * Log a message with the <code>DEBUG</code> level with message formatting
     * done according to the value of <code>messagePattern</code> and
     * <code>arg</code> parameters.
     * <p>
     * This form avoids superflous parameter construction. Whenever possible,
     * you should use this form instead of constructing the message parameter
     * using string concatenation.
     *
     * @param messagePattern The message pattern which will be parsed and formatted
     * @param arg            The argument to replace the formatting element, i,e,
     *                       the '{}' pair within <code>messagePattern</code>.
     *
     * @since 1.3
     */
    public void debug( Object messagePattern, Object arg )
    {
        if( m_delegate.isDebugEnabled() )
        {
            String msgStr = (String) messagePattern;
            msgStr = MessageFormatter.format( msgStr, arg );
            m_delegate.debug( msgStr, null );
        }
    }

    /**
     * Log a message with the <code>DEBUG</code> level with message formatting
     * done according to the messagePattern and the arguments arg1 and arg2.
     * <p>
     * This form avoids superflous parameter construction. Whenever possible,
     * you should use this form instead of constructing the message parameter
     * using string concatenation.
     *
     * @param messagePattern The message pattern which will be parsed and formatted
     * @param arg1           The first argument to replace the first formatting element
     * @param arg2           The second argument to replace the second formatting element
     *
     * @since 1.3
     */
    public void debug( String messagePattern, Object arg1, Object arg2 )
    {
        if( m_delegate.isDebugEnabled() )
        {
            String msgStr = MessageFormatter.format( messagePattern, arg1, arg2 );
            m_delegate.debug( msgStr, null );
        }
    }

    /**
     * Check whether this category is enabled for the ERROR Level. See also
     * {@link #isDebugEnabled()}.
     *
     * @return boolean - <code>true</code> if this category is enabled for level
     *         ERROR, <code>false</code> otherwise.
     */
    public boolean isErrorEnabled()
    {
        return m_delegate.isErrorEnabled();
    }

    /**
     * Log a message with the <code>ERROR</code> level with message formatting
     * done according to the value of <code>messagePattern</code> and
     * <code>arg</code> parameters.
     * <p>
     * This form avoids superflous parameter construction. Whenever possible,
     * you should use this form instead of constructing the message parameter
     * using string concatenation.
     *
     * @param messagePattern The message pattern which will be parsed and formatted
     * @param arg            The argument to replace the formatting element, i,e,
     *                       the '{}' pair within <code>messagePattern</code>.
     *
     * @since 1.3
     */
    public void error( Object messagePattern, Object arg )
    {
        if( m_delegate.isErrorEnabled() )
        {
            String msgStr = (String) messagePattern;
            msgStr = MessageFormatter.format( msgStr, arg );
            m_delegate.error( msgStr, null );
        }
    }

    /**
     * Log a message with the <code>ERROR</code> level with message formatting
     * done according to the messagePattern and the arguments arg1 and arg2.
     * <p>
     * This form avoids superflous parameter construction. Whenever possible,
     * you should use this form instead of constructing the message parameter
     * using string concatenation.
     *
     * @param messagePattern The message pattern which will be parsed and formatted
     * @param arg1           The first argument to replace the first formatting element
     * @param arg2           The second argument to replace the second formatting element
     *
     * @since 1.3
     */
    public void error( String messagePattern, Object arg1, Object arg2 )
    {
        if( m_delegate.isErrorEnabled() )
        {
            String msgStr = MessageFormatter.format( messagePattern, arg1, arg2 );
            m_delegate.error( msgStr, null );
        }
    }

    /**
     * Log a message with the <code>FATAL</code> level with message formatting
     * done according to the value of <code>messagePattern</code> and
     * <code>arg</code> parameters.
     * <p>
     * This form avoids superflous parameter construction. Whenever possible,
     * you should use this form instead of constructing the message parameter
     * using string concatenation.
     *
     * @param messagePattern The message pattern which will be parsed and formatted
     * @param arg            The argument to replace the formatting element, i,e,
     *                       the '{}' pair within <code>messagePattern</code>.
     *
     * @since 1.3
     */
    public void fatal( Object messagePattern, Object arg )
    {
        if( m_delegate.isFatalEnabled() )
        {
            String msgStr = (String) messagePattern;
            msgStr = MessageFormatter.format( msgStr, arg );
            m_delegate.fatal( msgStr, null );
        }
    }

    /**
     * Log a message with the <code>INFO</code> level with message formatting
     * done according to the value of <code>messagePattern</code> and
     * <code>arg</code> parameters.
     * <p>
     * This form avoids superflous parameter construction. Whenever possible,
     * you should use this form instead of constructing the message parameter
     * using string concatenation.
     *
     * @param messagePattern The message pattern which will be parsed and formatted
     * @param arg            The argument to replace the formatting element, i,e,
     *                       the '{}' pair within <code>messagePattern</code>.
     *
     * @since 1.3
     */
    public void info( Object messagePattern, Object arg )
    {
        if( m_delegate.isInfoEnabled() )
        {
            String msgStr = (String) messagePattern;
            msgStr = MessageFormatter.format( msgStr, arg );
            m_delegate.inform( msgStr, null );
        }
    }

    /**
     * Log a message with the <code>INFO</code> level with message formatting
     * done according to the messagePattern and the arguments arg1 and arg2.
     * <p>
     * This form avoids superflous parameter construction. Whenever possible,
     * you should use this form instead of constructing the message parameter
     * using string concatenation.
     *
     * @param messagePattern The message pattern which will be parsed and formatted
     * @param arg1           The first argument to replace the first formatting element
     * @param arg2           The second argument to replace the second formatting element
     *
     * @since 1.3
     */
    public void info( String messagePattern, Object arg1, Object arg2 )
    {
        if( m_delegate.isInfoEnabled() )
        {
            String msgStr = MessageFormatter.format( messagePattern, arg1, arg2 );
            m_delegate.inform( msgStr, null );
        }
    }

    /**
     * Check whether this category is enabled for the TRACE  Level. See also
     * {@link #isDebugEnabled()}.
     *
     * @return boolean - <code>true</code> if this category is enabled for level
     *         TRACE, <code>false</code> otherwise.
     */
    public boolean isTraceEnabled()
    {
        return m_delegate.isTraceEnabled();
    }

    /**
     * Check whether this category is enabled for the WARN Level. See also
     * {@link #isDebugEnabled()}.
     *
     * @return boolean - <code>true</code> if this category is enabled for level
     *         WARN, <code>false</code> otherwise.
     */
    public boolean isWarnEnabled()
    {
        return m_delegate.isWarnEnabled();
    }

    /**
     * Check whether this category is enabled for the given Level.
     *
     * @param priority the level to check
     *
     * @return boolean - <code>true</code> if this category is enabled for the given
     *         level, <code>false</code> otherwise.
     */
    public boolean isEnabledFor( Priority priority )
    {
        if( priority == Level.FATAL )
        {
            return m_delegate.isFatalEnabled();
        }
        if( priority == Level.ERROR )
        {
            return m_delegate.isErrorEnabled();
        }
        if( priority == Level.WARN )
        {
            return m_delegate.isWarnEnabled();
        }
        if( priority == Level.INFO )
        {
            return m_delegate.isDebugEnabled();
        }
        if( priority == Level.DEBUG )
        {
            return m_delegate.isDebugEnabled();
        }
        if( priority == Level.ALL )
        {
            return m_delegate.isTraceEnabled();
        }
        return false;
    }

    /**
     * Log a message with the <code>WARN</code> level with message formatting
     * done according to the value of <code>messagePattern</code> and
     * <code>arg</code> parameters.
     * <p>
     * This form avoids superflous parameter construction. Whenever possible,
     * you should use this form instead of constructing the message parameter
     * using string concatenation.
     *
     * @param messagePattern The message pattern which will be parsed and formatted
     * @param arg            The argument to replace the formatting element, i,e,
     *                       the '{}' pair within <code>messagePattern</code>.
     *
     * @since 1.3
     */
    public void warn( Object messagePattern, Object arg )
    {
        if( m_delegate.isWarnEnabled() )
        {
            String msgStr = (String) messagePattern;
            msgStr = MessageFormatter.format( msgStr, arg );
            m_delegate.warn( msgStr, null );
        }
    }

    /**
     * Log a message with the <code>WARN</code> level with message formatting
     * done according to the messagePattern and the arguments arg1 and arg2.
     * <p>
     * This form avoids superflous parameter construction. Whenever possible,
     * you should use this form instead of constructing the message parameter
     * using string concatenation.
     *
     * @param messagePattern The message pattern which will be parsed and formatted
     * @param arg1           The first argument to replace the first formatting element
     * @param arg2           The second argument to replace the second formatting element
     *
     * @since 1.3
     */
    public void warn( String messagePattern, Object arg1, Object arg2 )
    {
        if( m_delegate.isWarnEnabled() )
        {
            String msgStr = MessageFormatter.format( messagePattern, arg1, arg2 );
            m_delegate.warn( msgStr, null );
        }
    }

  /*Here are added overriden methods from the Category class (all methods that can be potentially used for logging).
    It is needed, because Category class is included in the stack trace in which log4j backend is looking for the LocationInfo instead of Logger class.
    These methods just call their super methods in the Category class
  */

  public void debug(final Object message) {
    super.debug(message);
  }

  public void debug(final Object message, final Throwable t) {
    super.debug(message, t);
  }

  public void error(final Object message) {
    super.error(message);
  }

  public void error(final Object message, final Throwable t) {
    super.error(message, t);
  }

  public void fatal(final Object message) {
    super.fatal(message);
  }

  public void fatal(final Object message, final Throwable t) {
    super.fatal(message, t);
  }

  public void info(final Object message) {
    super.info(message);
  }

  public void info(final Object message, final Throwable t) {
    super.info(message, t);
  }

  public void warn(final Object message) {
    super.warn(message);
  }

  public void warn(final Object message, final Throwable t) {
    super.warn(message, t);
  }

    /** Pax Logging internal method. Should never be used directly. */
    public static void dispose()
    {
        m_paxLogging.close();
        m_paxLogging.dispose();
        m_paxLogging = null;
    }

}
