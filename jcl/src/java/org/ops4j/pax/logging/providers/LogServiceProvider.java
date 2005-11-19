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
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

public class LogServiceProvider
    implements Log, LogProvider
{
    private ServiceTracker m_Tracker;

    public LogServiceProvider( BundleContext context )
    {
        String logService = LogService.class.getName();
        m_Tracker = new ServiceTracker( context, logService, null );
    }

    public Log getLogger( String categoryName )
    {
        return this;
    }

    public void release()
    {
        m_Tracker.close();
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
        LogService service = getLogService();
        return service != null;
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
        LogService service = getLogService();
        return service != null;
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
        LogService service = getLogService();
        return service != null;
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
        LogService service = getLogService();
        return service != null;
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
        LogService service = getLogService();
        return service != null;
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
        LogService service = getLogService();
        return service != null;
    }
    /**
     * <p> Log a message with trace log level. </p>
     *
     * @param message log this message
     */
    public void trace(Object message)
    {
        LogService service = getLogService();
        if( service == null )
        {
            return;
        }
        service.log( LogService.LOG_DEBUG, message.toString() );
    }
    /**
     * <p> Log an error with trace log level. </p>
     *
     * @param message log this message
     * @param t log this cause
     */
    public void trace(Object message, Throwable t)
    {
        LogService service = getLogService();
        if( service == null )
        {
            return;
        }
        service.log( LogService.LOG_DEBUG, message.toString() );
    }

    /**
     * <p> Log a message with debug log level. </p>
     *
     * @param message log this message
     */
    public void debug(Object message)
    {
        LogService service = getLogService();
        if( service == null )
        {
            return;
        }
        service.log( LogService.LOG_DEBUG, message.toString() );
    }

    /**
     * <p> Log an error with debug log level. </p>
     *
     * @param message log this message
     * @param t log this cause
     */
    public void debug(Object message, Throwable t)
    {
        LogService service = getLogService();
        if( service == null )
        {
            return;
        }
        service.log( LogService.LOG_DEBUG, message.toString() );
    }

    /**
     * <p> Log a message with info log level. </p>
     *
     * @param message log this message
     */
    public void info(Object message)
    {
        LogService service = getLogService();
        if( service == null )
        {
            return;
        }
        service.log( LogService.LOG_INFO, message.toString() );
    }

    /**
     * <p> Log an error with info log level. </p>
     *
     * @param message log this message
     * @param t log this cause
     */
    public void info(Object message, Throwable t)
    {
        LogService service = getLogService();
        if( service == null )
        {
            return;
        }
        service.log( LogService.LOG_INFO, message.toString() );
    }

    /**
     * <p> Log a message with warn log level. </p>
     *
     * @param message log this message
     */
    public void warn(Object message)
    {
        LogService service = getLogService();
        if( service == null )
        {
            return;
        }
        service.log( LogService.LOG_WARNING, message.toString() );
    }

    /**
     * <p> Log an error with warn log level. </p>
     *
     * @param message log this message
     * @param t log this cause
     */
    public void warn(Object message, Throwable t)
    {
        LogService service = getLogService();
        if( service == null )
        {
            return;
        }
        service.log( LogService.LOG_WARNING, message.toString() );
    }

    /**
     * <p> Log a message with error log level. </p>
     *
     * @param message log this message
     */
    public void error(Object message)
    {
        LogService service = getLogService();
        if( service == null )
        {
            return;
        }
        service.log( LogService.LOG_ERROR, message.toString() );
    }

    /**
     * <p> Log an error with error log level. </p>
     *
     * @param message log this message
     * @param t log this cause
     */
    public void error(Object message, Throwable t)
    {
        LogService service = getLogService();
        if( service == null )
        {
            return;
        }
        service.log( LogService.LOG_ERROR, message.toString() );
    }

    /**
     * <p> Log a message with fatal log level. </p>
     *
     * @param message log this message
     */
    public void fatal(Object message)
    {
        LogService service = getLogService();
        if( service == null )
        {
            return;
        }
        service.log( LogService.LOG_ERROR, message.toString() );
    }

    /**
     * <p> Log an error with fatal log level. </p>
     *
     * @param message log this message
     * @param t log this cause
     */
    public void fatal(Object message, Throwable t)
    {
        LogService service = getLogService();
        if( service == null )
        {
            return;
        }
        service.log( LogService.LOG_ERROR, message.toString() );
    }

    private LogService getLogService()
    {
        LogService service = (LogService) m_Tracker.getService();
        return service;
    }
}
