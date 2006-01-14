/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * Copyright 2005 Niclas Hedhman
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
package org.ops4j.pax.logging.providers;

import org.apache.commons.logging.Log;

import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingService;

import org.osgi.util.tracker.ServiceTracker;

public class PaxLoggingServiceLog
    implements Log
{
    private ServiceTracker m_Tracker;
    private String m_CategoryName;

    public PaxLoggingServiceLog( ServiceTracker tracker, String categoryName )
    {
        m_Tracker = tracker;
        m_CategoryName = categoryName;
    }

    /**
     * <p> Is debug logging currently enabled? </p>
     *
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatenation)
     * when the log level is more than debug. </p>
     */
    public boolean isDebugEnabled()
    {
        PaxLogger logger = getLogger();
        if( logger == null )
        {
            return false;
        }
        return logger.isDebugEnabled();
    }

    /**
     * <p> Is error logging currently enabled? </p>
     *
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatenation)
     * when the log level is more than error. </p>
     */
    public boolean isErrorEnabled()
    {
        PaxLogger logger = getLogger();
        if( logger == null )
        {
            return false;
        }
        return logger.isErrorEnabled();
    }

    /**
     * <p> Is fatal logging currently enabled? </p>
     *
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatenation)
     * when the log level is more than fatal. </p>
     */
    public boolean isFatalEnabled()
    {
        PaxLogger logger = getLogger();
        if( logger == null )
        {
            return false;
        }
        return logger.isFatalEnabled();
    }
    /**
     * <p> Is info logging currently enabled? </p>
     *
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatenation)
     * when the log level is more than info. </p>
     */
    public boolean isInfoEnabled()
    {
        PaxLogger logger = getLogger();
        if( logger == null )
        {
            return false;
        }
        return logger.isInfoEnabled();
    }
    /**
     * <p> Is trace logging currently enabled? </p>
     *
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatenation)
     * when the log level is more than trace. </p>
     */
    public boolean isTraceEnabled()
    {
        PaxLogger logger = getLogger();
        if( logger == null )
        {
            return false;
        }
        return logger.isTraceEnabled();
    }
    /**
     * <p> Is warn logging currently enabled? </p>
     *
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatenation)
     * when the log level is more than warn. </p>
     */
    public boolean isWarnEnabled()
    {
        PaxLogger logger = getLogger();
        if( logger == null )
        {
            return false;
        }
        return logger.isWarnEnabled();
    }
    /**
     * <p> Log a message with trace log level. </p>
     *
     * @param message log this message
     */
    public void trace( Object message )
    {
        PaxLogger logger = getLogger();
        if( logger == null )
        {
            return;
        }
        logger.trace( message.toString(), null );
    }

    /**
     * <p> Log an error with trace log level. </p>
     *
     * @param message log this message
     * @param t log this cause
     */
    public void trace( Object message, Throwable t )
    {
        PaxLogger logger = getLogger();
        if( logger == null )
        {
            return;
        }
        logger.debug( message.toString(), t );
    }

    /**
     * <p> Log a message with debug log level. </p>
     *
     * @param message log this message
     */
    public void debug( Object message )
    {
        PaxLogger logger = getLogger();
        if( logger == null )
        {
            return;
        }
        logger.debug( message.toString(), null );
    }

    /**
     * <p> Log an error with debug log level. </p>
     *
     * @param message log this message
     * @param t log this cause
     */
    public void debug(Object message, Throwable t)
    {
        PaxLogger logger = getLogger();
        if( logger == null )
        {
            return;
        }
        logger.debug( message.toString(), t );
    }

    /**
     * <p> Log a message with info log level. </p>
     *
     * @param message log this message
     */
    public void info(Object message)
    {
        PaxLogger logger = getLogger();
        if( logger == null )
        {
            return;
        }
        logger.inform( message.toString(), null );
    }

    /**
     * <p> Log an error with info log level. </p>
     *
     * @param message log this message
     * @param t log this cause
     */
    public void info(Object message, Throwable t)
    {
        PaxLogger logger = getLogger();
        if( logger == null )
        {
            return;
        }
        logger.inform( message.toString(), t );
    }

    /**
     * <p> Log a message with warn log level. </p>
     *
     * @param message log this message
     */
    public void warn(Object message)
    {
        PaxLogger logger = getLogger();
        if( logger == null )
        {
            return;
        }
        logger.warn( message.toString(), null );
    }

    /**
     * <p> Log an error with warn log level. </p>
     *
     * @param message log this message
     * @param t log this cause
     */
    public void warn(Object message, Throwable t)
    {
        PaxLogger logger = getLogger();
        if( logger == null )
        {
            return;
        }
        logger.warn( message.toString(), t );
    }

    /**
     * <p> Log a message with error log level. </p>
     *
     * @param message log this message
     */
    public void error(Object message)
    {
        PaxLogger logger = getLogger();
        if( logger == null )
        {
            return;
        }
        logger.error( message.toString(), null );
    }

    /**
     * <p> Log an error with error log level. </p>
     *
     * @param message log this message
     * @param t log this cause
     */
    public void error(Object message, Throwable t)
    {
        PaxLogger logger = getLogger();
        if( logger == null )
        {
            return;
        }
        logger.error( message.toString(), t );
    }

    /**
     * <p> Log a message with fatal log level. </p>
     *
     * @param message log this message
     */
    public void fatal(Object message)
    {
        PaxLogger logger = getLogger();
        if( logger == null )
        {
            return;
        }
        logger.fatal( message.toString(), null );
    }

    /**
     * <p> Log an error with fatal log level. </p>
     *
     * @param message log this message
     * @param t log this cause
     */
    public void fatal(Object message, Throwable t)
    {
        PaxLogger logger = getLogger();
        if( logger == null )
        {
            return;
        }
        logger.debug( message.toString(), t );
    }

    private PaxLogger getLogger()
    {
        Object trackedservice = m_Tracker.getService();
        PaxLoggingService service = (PaxLoggingService) trackedservice;
        if( service == null )
        {
            return null;
        }
        return service.getLogger( m_CategoryName );
    }
}