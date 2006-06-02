/*
 * Copyright 2006 Niclas Hedhman.
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

/*
 * Copyright (c) 2003, KNOPFLERFISH project
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials
 *   provided with the distribution.
 *
 * - Neither the name of the KNOPFLERFISH project nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.knopflerfish.service.log;

import org.ops4j.pax.logging.providers.PaxLoggingProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.apache.commons.logging.Log;

/**
 * LogRef is an utility class that simplifies the use of the LogService.
 * <P> LogRef let you use the log without worrying about getting new service objects when the log service is restarted.
 * It also supplies methods with short names that does logging with all the different LogService severity types.
 * </P>
 * <P> To use the LogRef you need to import <code>org.knopflerfish.service.log.LogRef</code> and instantiate LogRef
 * with your bundle context as parameter. The bundle context is used for getting the LogService and adding a service
 * listener.
 * </P>
 * <H2>Example usage</H2>
 * The <code>if</code> statement that protects each call to the <code>LogRef</code> instance below is there to save the
 * effort required for creating the message string object in cases where the log will throw away the log entry due to
 * its unimportance. The user must have this <code>if</code>-test in his code since that is the only way to avoid
 * constructing the string object. Placing it in the wrapper (LogRef) will not help due to the design of the Java
 * programming language.
 *
 * <PRE>
 *  package org.knopflerfish.example.hello;
 *
 *  import org.osgi.framework.*;
 *  import org.knopflerfish.service.log.LogRef;
 *
 *  public class Hello implements BundleActivator
 *  {
 *      LogRef log;
 *
 *      public void start(BundleContext bundleContext)
 *      {
 *          log = new LogRef(bundleContext);
 *          if( log.doInfo() )
 *              log.info("Hello started.");
 *      }
 *
 *      public void stop(BundleContext bundleContext)
 *      {
 *          if (log.doDebug())
 *              log.debug("Hello stopped.");
 *      }
 *  }
 * </PRE>
 *
 * <b>NOTE!!!</b>
 * This class is mangled to support Knopflerfish Log in Pax Logging with the smallest
 * memory footprint possible. Pax Logging is now compatible with the standard KF Log usage
 * of;
 * <pre>
 *   LogRef logger = new LogRef( bundleContext );
 * </pre>
 *
 * @author Gatespace AB
 * @author Niclas Hedhman
 * @see org.osgi.service.log.LogService
 */

public class LogRef
{
    private Log m_logger;
    private PaxLoggingProvider m_provider;

    /**
     * Create new LogRef object for a given bundle.
     *
     * @param bc  the bundle context of the bundle that this log ref instance belongs too.
     * @param out On KF Log this means; "If true print messages on <code>System.out</code> when there is no log
     *            service.", but this feature is currently not supported in Pax Logging.
     */
    public LogRef( BundleContext bc, boolean out )
    {
        this( bc );
    }

    /**
     * * Create new LogRef object for a given bundle.
     *
     * @param bc the bundle context of the bundle that this log ref instance belongs too.
     */
    public LogRef( BundleContext bc )
    {
        m_provider = new PaxLoggingProvider( bc );
        String symbolicName = bc.getBundle().getSymbolicName();
        m_logger = m_provider.getLogger( symbolicName );
    }

    /**
     * * Close this LogRef object. Ungets the log service if active.
     */
    public void close()
    {
        m_provider.release();
    }

    /**
     * Returns the current log level.
     * There is no use to generate log entries with a severity level less than this value since such entries
     * will be thrown away by the log.
     *
     * @return the current severity log level for this bundle.
     */
    public int getLogLevel()
    {
        int logLevel = m_logger.getLogLevel();
        if( logLevel >= 35000 )
        {
            return LogService.LOG_ERROR;
        }
        else if( logLevel >= 25000 )
        {
            return LogService.LOG_WARNING;
        }
        else if( logLevel >= 15000 )
        {
            return LogService.LOG_INFO;
        }
        else
        {
            return LogService.LOG_DEBUG;
        }
    }

    /**
     * Returns true if messages with severity debug or higher are saved by the log.
     *
     * @return <code>true</code> if messages with severity LOG_DEBUG * or
     *         higher are included in the log, otherwise <code>false</code>.
     */
    public boolean doDebug()
    {
        return m_logger.isDebugEnabled();
    }

    /**
     * Returns true if messages with severity warning or higher are saved by the log.
     *
     * @return <code>true</code> if messages with severity LOG_WARNING * or
     *         higher are included in the log, otherwise <code>false</code>.
     */
    public boolean doWarn()
    {
        return m_logger.isWarnEnabled();
    }

    /**
     * Returns true if messages with severity info or higher are saved by the log.
     *
     * @return <code>true</code> if messages with severity LOG_INFO * or
     *         higher are included in the log, otherwise <code>false</code>.
     */
    public boolean doInfo()
    {
        return m_logger.isInfoEnabled();
    }

    /**
     * Returns true if messages with severity error or higher * are saved by
     * the log.
     *
     * @return <code>true</code> if messages with severity LOG_ERROR * or
     *         higher are included in the log, otherwise <code>false</code>.
     */
    public boolean doError()
    {
        return m_logger.isErrorEnabled();
    }

    /**
     * Log a debug level message
     *
     * @param msg Log message.
     */
    public void debug( String msg )
    {
        m_logger.debug( msg );
    }

    /**
     * Log a debug level message.
     *
     * @param msg Log message
     * @param sr  The <code>ServiceReference</code> of the service that this message is associated with.
     */
    public void debug( String msg, ServiceReference sr )
    {
        String symbolicName = sr.getBundle().getSymbolicName();
        Log logger = m_provider.getLogger( symbolicName );
        logger.debug( msg );
    }

    /**
     * * Log a debug level message. * *
     *
     * @param msg Log message *
     * @param e   The exception that reflects the condition.
     */
    public void debug( String msg, Throwable e )
    {
        m_logger.debug( msg, e );
    }

    /**
     * * Log a debug level message. * *
     *
     * @param msg Log message *
     * @param sr  The <code>ServiceReference</code> of the service * that this
     *            message is associated with. *
     * @param e   The exception that reflects the condition.
     */
    public void debug( String msg, ServiceReference sr, Throwable e )
    {
        String symbolicName = sr.getBundle().getSymbolicName();
        Log logger = m_provider.getLogger( symbolicName );
        logger.debug( msg, e );
    }

    /**
     * * Log an info level message. * *
     *
     * @param msg Log message
     */
    public void info( String msg )
    {
        m_logger.info( msg );
    }

    /**
     * * Log an info level message. * *
     *
     * @param msg Log message *
     * @param sr  The <code>ServiceReference</code> of the service * that this
     *            message is associated with.
     */
    public void info( String msg, ServiceReference sr )
    {
        String symbolicName = sr.getBundle().getSymbolicName();
        Log logger = m_provider.getLogger( symbolicName );
        logger.info( msg );
    }

    /**
     * * Log an info level message. * *
     *
     * @param msg Log message *
     * @param e   The exception that reflects the condition.
     */
    public void info( String msg, Throwable e )
    {
        m_logger.info( msg, e );
    }

    /**
     * * Log an info level message. * *
     *
     * @param msg Log message *
     * @param sr  The <code>ServiceReference</code> of the service * that this
     *            message is associated with. *
     * @param e   The exception that reflects the condition.
     */
    public void info( String msg, ServiceReference sr, Throwable e )
    {
        String symbolicName = sr.getBundle().getSymbolicName();
        Log logger = m_provider.getLogger( symbolicName );
        logger.info( msg, e );
    }

    /**
     * * Log a warning level message. * *
     *
     * @param msg Log message
     */
    public void warn( String msg )
    {
        m_logger.warn( msg );
    }

    /**
     * * Log a warning level message. * *
     *
     * @param msg Log message *
     * @param sr  The <code>ServiceReference</code> of the service * that this
     *            message is associated with.
     */
    public void warn( String msg, ServiceReference sr )
    {
        String symbolicName = sr.getBundle().getSymbolicName();
        Log logger = m_provider.getLogger( symbolicName );
        logger.warn( msg );
    }

    /**
     * * Log a warning level message. * *
     *
     * @param msg Log message *
     * @param e   The exception that reflects the condition.
     */
    public void warn( String msg, Throwable e )
    {
        m_logger.warn( msg, e );
    }

    /**
     * * Log a warning level message. * *
     *
     * @param msg Log message *
     * @param sr  The <code>ServiceReference</code> of the service * that this
     *            message is associated with. *
     * @param e   The exception that reflects the condition.
     */
    public void warn( String msg, ServiceReference sr, Throwable e )
    {
        String symbolicName = sr.getBundle().getSymbolicName();
        Log logger = m_provider.getLogger( symbolicName );
        logger.warn( msg, e );
    }

    /**
     * * Log an error level message. * *
     *
     * @param msg Log message
     */
    public void error( String msg )
    {
        m_logger.error( msg );
    }

    /**
     * * Log an error level message. * *
     *
     * @param msg Log message *
     * @param sr  The <code>ServiceReference</code> of the service * that this
     *            message is associated with.
     */
    public void error( String msg, ServiceReference sr )
    {
        String symbolicName = sr.getBundle().getSymbolicName();
        Log logger = m_provider.getLogger( symbolicName );
        logger.error( msg );
    }

    /**
     * * Log an error level message. * *
     *
     * @param msg Log message *
     * @param e   The exception that reflects the condition.
     */
    public void error( String msg, Throwable e )
    {
        m_logger.error( msg, e );
    }

    /**
     * * Log an error level message. * *
     *
     * @param msg Log message *
     * @param sr  The <code>ServiceReference</code> of the service * that this
     *            message is associated with. *
     * @param e   The exception that reflects the condition.
     */
    public void error( String msg, ServiceReference sr, Throwable e )
    {
        String symbolicName = sr.getBundle().getSymbolicName();
        Log logger = m_provider.getLogger( symbolicName );
        logger.error( msg, e );
    }

    /**
     * * Log a message. * The ServiceDescription field and the Throwable * field
     * of the LogEntry will be set to null. *
     *
     * @param level   The severity of the message. (Should be one of the * four
     *                predefined severities.) *
     * @param message Human readable string describing the condition.
     */
    public void log( int level, String message )
    {
        log( level, message, null );
    }

    /**
     * * Log a message with an exception. * The ServiceDescription field of the
     * LogEntry will be set to null. *
     *
     * @param level     The severity of the message. (Should be one of the * four
     *                  predefined severities.) *
     * @param message   Human readable string describing the condition. *
     * @param exception The exception that reflects the condition.
     */
    public void log( int level, String message, Throwable exception )
    {
        Log logger = m_logger;
        log( logger, level, message, exception );
    }

    /**
     * * Log a message associated with a specific Service. * The Throwable field
     * of the LogEntry will be set to null. *
     *
     * @param sr      The <code>ServiceReference</code> of the service that * this
     *                message is associated with. *
     * @param level   The severity of the message. (Should be one of the * four
     *                predefined severities.) *
     * @param message Human readable string describing the condition.
     */
    public void log( ServiceReference sr, int level, String message )
    {
        log( sr, level, message, null );
    }

    /**
     * * Log a message with an exception associated with a specific Service. *
     *
     * @param sr        The <code>ServiceReference</code> of the service that * this
     *                  message is associated with. *
     * @param level     The severity of the message. (Should be one of the * four
     *                  predefined severities.) *
     * @param message   Human readable string describing the condition. *
     * @param exception The exception that reflects the condition.
     */
    public void log( ServiceReference sr, int level, String message,
                     Throwable exception )
    {
        String symbolicName = sr.getBundle().getSymbolicName();
        Log logger = m_provider.getLogger( symbolicName );
        log( logger, level, message, exception );
    }

    private void log( Log logger, int level, String message, Throwable exception )
    {
        if( level == LogService.LOG_DEBUG )
        {
            logger.debug( message, exception );
        }
        else if( level == LogService.LOG_INFO )
        {
            logger.info( message, exception );
        }
        else if( level == LogService.LOG_WARNING )
        {
            logger.warn( message, exception );
        }
        else if( level == LogService.LOG_ERROR )
        {
            logger.error( message, exception );
        }
    }
}
