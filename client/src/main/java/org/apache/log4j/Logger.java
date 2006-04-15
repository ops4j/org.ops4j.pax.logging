/*
 * Copyright 1999,2004 The Apache Software Foundation.
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

import org.apache.commons.logging.Log;
import org.apache.log4j.helpers.MessageFormatter;
import org.ops4j.pax.logging.providers.DefaultLogProvider;
import org.ops4j.pax.logging.providers.LogProvider;
import org.ops4j.pax.logging.providers.PaxLoggingProvider;
import org.osgi.framework.BundleContext;

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
 * Typical code looks like this;
 * <code><pre>
 * import org.apache.commons.logging.LogFactory;
 * import org.apache.commons.logging.Log;
 *
 * public class Activator
 *     implements BundleActivator
 * {
 *     public void start( BundleContext context )
 *         throws Exception
 *     {
 *         LogFactory.getFactory().setBundleContext( context );
 *     }
 * }
 * </pre></code>
 * </p>
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author Niclas Hedhman
 */
public class Logger
{

    private static LogProvider m_provider;
    private Log m_delegate;

    static
    {
        m_provider = new DefaultLogProvider();
    }

    public static void setBundleContext( BundleContext ctx )
    {
        m_provider = new PaxLoggingProvider( ctx );
    }

    /** Lifecycle method to release any resources held.
     *
     */
    public static void release()
    {
        if( m_provider != null )
        {
            m_provider.release();
            m_provider = null;
        }
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
    private Logger( Log delegate )
    {
        m_delegate = delegate;
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
     */
    public static Logger getLogger( String name )
    {
        Log logger = m_provider.getLogger( name );
        return new Logger( logger );
    }

    /**
     * Shorthand for <code>{@link #getLogger(Class) getLogger(clazz.getName())}</code>.
     *
     * @param clazz The name of <code>clazz</code> will be used as the name of
     *              the logger to retrieve.  See {@link #getLogger(String)} for
     *              more detailed information.
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
     */
    public static Logger getRootLogger()
    {
        return getLogger( "" );
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
        if( m_delegate.isTraceEnabled() )
        {
            m_delegate.trace( message );
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
            m_delegate.trace( message, t );
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
        if( m_delegate.isTraceEnabled() )
        {
            String msgStr = (String) messagePattern;
            msgStr = MessageFormatter.format( msgStr, arg );
            m_delegate.trace( msgStr );
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
            String msgStr = (String) messagePattern;
            msgStr = MessageFormatter.format( msgStr, arg1, arg2 );
            m_delegate.trace( msgStr );
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
            String msgStr = (String) messagePattern;
            msgStr = MessageFormatter.format( msgStr, arg1, arg2 );
            m_delegate.fatal( msgStr );
        }
    }

    /**
     * Log a message object with the DEBUG level.
     *
     * <p>
     * This method first checks if this category is <code>DEBUG</code> enabled
     * by comparing the level of this category with the
     * DEBUG level. If this category is <code>DEBUG</code> enabled, then it
     * converts the message object (passed as parameter) to a string by
     * invoking the appropriate org.apache.log4j.or.ObjectRenderer. It
     * then proceeds to call all the registered appenders in this category and
     * also higher in the hierarchy depending on the value of the additivity
     * flag.
     * </p>
     *
     * <p>
     * <b>WARNING</b> Note that passing a {@link Throwable} to this method will
     * print the name of the <code>Throwable</code> but no stack trace. To
     * print a stack trace use the {@link #debug(Object, Throwable)} form
     * instead.
     * </p>
     *
     * @param message the message object to log.
     */
    public void debug( Object message )
    {
        if( m_delegate.isDebugEnabled() )
        {
            m_delegate.debug( message );
        }
    }

    /**
     * Log a message object with the <code>DEBUG</code> level including the
     * stack trace of the {@link Throwable}<code>t</code> passed as parameter.
     *
     * <p>
     * See {@link #debug(Object)} form for more detailed information.
     * </p>
     *
     * @param message the message object to log.
     * @param t       the exception to log, including its stack trace.
     */
    public void debug( Object message, Throwable t )
    {
        if( m_delegate.isDebugEnabled() )
        {
            m_delegate.debug( message, t );
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
            m_delegate.debug( msgStr );
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
            String msgStr = (String) messagePattern;
            msgStr = MessageFormatter.format( msgStr, arg1, arg2 );
            m_delegate.debug( msgStr );
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
     * Log a message object with the ERROR Level.
     *
     * <p>
     * This method first checks if this category is <code>ERROR</code> enabled
     * by comparing the level of this category with ERROR
     * Level. If this category is <code>ERROR</code> enabled, then it converts
     * the message object passed as parameter to a string by invoking the
     * appropriate org.apache.log4j.or.ObjectRenderer. It proceeds to
     * call all the registered appenders in this category and also higher in
     * the hierarchy depending on the value of the additivity flag.
     * </p>
     *
     * <p>
     * <b>WARNING</b> Note that passing a {@link Throwable} to this method will
     * print the name of the <code>Throwable</code> but no stack trace. To
     * print a stack trace use the {@link #error(Object, Throwable)} form
     * instead.
     * </p>
     *
     * @param message the message object to log
     */
    public void error( Object message )
    {
        if( m_delegate.isErrorEnabled() )
        {
            m_delegate.error( message );
        }
    }

    /**
     * Log a message object with the <code>ERROR</code> level including the
     * stack trace of the {@link Throwable}<code>t</code> passed as parameter.
     *
     * <p>
     * See {@link #error(Object)} form for more detailed information.
     * </p>
     *
     * @param message the message object to log.
     * @param t       the exception to log, including its stack trace.
     */
    public void error( Object message, Throwable t )
    {
        if( m_delegate.isErrorEnabled() )
        {
            m_delegate.error( message, t );
        }
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
            m_delegate.error( msgStr );
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
            String msgStr = (String) messagePattern;
            msgStr = MessageFormatter.format( msgStr, arg1, arg2 );
            m_delegate.error( msgStr );
        }
    }

    /**
     * Log a message object with the FATAL Level.
     *
     * <p>
     * This method first checks if this category is <code>FATAL</code> enabled
     * by comparing the level of this category with FATAL
     * Level. If the category is <code>FATAL</code> enabled, then it converts
     * the message object passed as parameter to a string by invoking the
     * appropriate org.apache.log4j.or.ObjectRenderer. It proceeds to
     * call all the registered appenders in this category and also higher in
     * the hierarchy depending on the value of the additivity flag.
     * </p>
     *
     * <p>
     * <b>WARNING</b> Note that passing a {@link Throwable} to this method will
     * print the name of the Throwable but no stack trace. To print a stack
     * trace use the {@link #fatal(Object, Throwable)} form instead.
     * </p>
     *
     * @param message the message object to log
     */
    public void fatal( Object message )
    {
        if( m_delegate.isFatalEnabled() )
        {
            m_delegate.fatal( message );
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
            m_delegate.fatal( msgStr );
        }
    }

    /**
     * Log a message object with the <code>FATAL</code> level including the
     * stack trace of the {@link Throwable}<code>t</code> passed as parameter.
     *
     * <p>
     * See {@link #fatal(Object)} for more detailed information.
     * </p>
     *
     * @param message the message object to log.
     * @param t       the exception to log, including its stack trace.
     */
    public void fatal( Object message, Throwable t )
    {
        if( m_delegate.isFatalEnabled() )
        {
            m_delegate.fatal( message, t );
        }
    }

    /**
     * Log a message object with the INFO Level.
     *
     * <p>
     * This method first checks if this category is <code>INFO</code> enabled by
     * comparing the level of this category with INFO Level.
     * If the category is <code>INFO</code> enabled, then it converts the
     * message object passed as parameter to a string by invoking the
     * appropriate org.apache.log4j.or.ObjectRenderer. It proceeds to
     * call all the registered appenders in this category and also higher in
     * the hierarchy depending on the value of the additivity flag.
     * </p>
     *
     * <p>
     * <b>WARNING</b> Note that passing a {@link Throwable} to this method will
     * print the name of the Throwable but no stack trace. To print a stack
     * trace use the {@link #info(Object, Throwable)} form instead.
     * </p>
     *
     * @param message the message object to log
     */
    public void info( Object message )
    {
        if( m_delegate.isInfoEnabled() )
        {
            m_delegate.info( message );
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
            m_delegate.info( msgStr );
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
            String msgStr = (String) messagePattern;
            msgStr = MessageFormatter.format( msgStr, arg1, arg2 );
            m_delegate.info( msgStr );
        }
    }

    /**
     * Log a message object with the <code>INFO</code> level including the stack
     * trace of the {@link Throwable}<code>t</code> passed as parameter.
     *
     * <p>
     * See {@link #info(Object)} for more detailed information.
     * </p>
     *
     * @param message the message object to log.
     * @param t       the exception to log, including its stack trace.
     */
    public void info( Object message, Throwable t )
    {
        if( m_delegate.isInfoEnabled() )
        {
            m_delegate.info( message, t );
        }
    }

    /**
     * Check whether this category is enabled for the <code>DEBUG</code> Level.
     *
     * <p>
     * This function is intended to lessen the computational cost of disabled
     * log debug statements.
     * </p>
     *
     * <p>
     * For some <code>cat</code> Category object, when you write,
     * <pre>
     *      cat.debug("This is entry number: " + i );
     *  </pre>
     * </p>
     *
     * <p>
     * You incur the cost constructing the message, concatenatiion in this case,
     * regardless of whether the message is logged or not.
     * </p>
     *
     * <p>
     * If you are worried about speed, then you should write
     * <pre>
     *          if(cat.isDebugEnabled()) {
     *            cat.debug("This is entry number: " + i );
     *          }
     *  </pre>
     * </p>
     *
     * <p>
     * This way you will not incur the cost of parameter construction if
     * debugging is disabled for <code>cat</code>. On the other hand, if the
     * <code>cat</code> is debug enabled, you will incur the cost of evaluating
     * whether the category is debug enabled twice. Once in
     * <code>isDebugEnabled</code> and once in the <code>debug</code>.  This is
     * an insignificant overhead since evaluating a category takes about 1%% of
     * the time it takes to actually log.
     * </p>
     *
     * @return boolean - <code>true</code> if this category is debug enabled,
     *         <code>false</code> otherwise.
     */
    public boolean isDebugEnabled()
    {
        return m_delegate.isDebugEnabled();
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
     * Check whether this category is enabled for the info Level. See also
     * {@link #isDebugEnabled()}.
     *
     * @return boolean - <code>true</code> if this category is enabled for level
     *         info, <code>false</code> otherwise.
     */
    public boolean isInfoEnabled()
    {
        return m_delegate.isInfoEnabled();
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
     * Log a message object with the WARN Level.
     *
     * <p>
     * This method first checks if this category is <code>WARN</code> enabled by
     * comparing the level of this category with WARN Level.
     * If the category is <code>WARN</code> enabled, then it converts the
     * message object passed as parameter to a string by invoking the
     * appropriate org.apache.log4j.or.ObjectRenderer. It proceeds to
     * call all the registered appenders in this category and also higher in
     * the hieararchy depending on the value of the additivity flag.
     * </p>
     *
     * <p>
     * <b>WARNING</b> Note that passing a {@link Throwable} to this method will
     * print the name of the Throwable but no stack trace. To print a stack
     * trace use the {@link #warn(Object, Throwable)} form instead.
     * </p>
     *
     * <p></p>
     *
     * @param message the message object to log.
     */
    public void warn( Object message )
    {
        if( m_delegate.isWarnEnabled() )
        {
            m_delegate.warn( message );
        }
    }

    /**
     * Log a message with the <code>WARN</code> level including the stack trace
     * of the {@link Throwable}<code>t</code> passed as parameter.
     *
     * <p>
     * See {@link #warn(Object)} for more detailed information.
     * </p>
     *
     * @param message the message object to log.
     * @param t       the exception to log, including its stack trace.
     */
    public void warn( Object message, Throwable t )
    {
        if( m_delegate.isWarnEnabled() )
        {
            m_delegate.warn( message, t );
        }
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
            m_delegate.warn( msgStr );
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
            String msgStr = (String) messagePattern;
            msgStr = MessageFormatter.format( msgStr, arg1, arg2 );
            m_delegate.warn( msgStr );
        }
    }
}
