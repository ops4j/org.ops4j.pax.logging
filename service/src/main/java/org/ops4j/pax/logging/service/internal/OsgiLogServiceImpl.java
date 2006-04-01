/*
 * Copyright 2005 Makas Tzavellas.
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

import org.apache.log4j.Logger;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

/**
 * Default logger service using log4j underneath implementing the LogService
 * interface.
 */
public class OsgiLogServiceImpl
    implements LogService
{
    /**
     * Log4J logger
     */
    private Logger m_Logger;

    /**
     * Instantiates a Log4jService for a particular bundle requesting this
     * service instance.
     *
     * @param loggerName
     *            Bundle requesting this service instance.
     */
    public OsgiLogServiceImpl( String loggerName )
    {
        m_Logger = Logger.getLogger( loggerName );
    }

    /**
     * @see org.osgi.service.log.LogService#log(int,java.lang.String)
     */
    public void log( int level, String message )
    {
        log( level, message, null );
    }

    /**
     * @see org.osgi.service.log.LogService#log(int,java.lang.String,java.lang.Throwable)
     */
    public void log( int level, String message, Throwable exception )
    {
        log( null, level, message, exception );
    }

    /**
     * @see org.osgi.service.log.LogService#log(org.osgi.framework.ServiceReference,int,java.lang.String)
     */
    public void log( ServiceReference sr, int level, String message )
    {
        log( sr, level, message, null );
    }

    /**
     * @see org.osgi.service.log.LogService#log(org.osgi.framework.ServiceReference,int,java.lang.String,java.lang.Throwable)
     */
    public void log( ServiceReference sr, int level, String message, Throwable exception )
    {
        if ( level == LOG_DEBUG )
        {
            m_Logger.debug( message, exception );
        } else if ( level == LOG_INFO )
        {
            m_Logger.info( message, exception );
        } else if ( level == LOG_ERROR )
        {
            m_Logger.error( message, exception );
        } else
        {
            m_Logger.warn( message, exception );
        }
    }

    /**
     * Removes all the resources held by this service instance.
     *
     */
    public void dispose()
    {
        m_Logger = null;
    }
}
