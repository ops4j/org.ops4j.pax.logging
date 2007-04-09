/*
 * Copyright 2005 Niclas Hedhman.
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
package org.ops4j.pax.logging.internal;

import java.util.Dictionary;
import org.apache.log4j.Logger;
import org.knopflerfish.service.log.LogService;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingService;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.log.LogEntry;

public class PaxLoggingServiceImpl
    implements PaxLoggingService, LogService, ManagedService
{

    private LogReaderServiceImpl m_logReader;

    public PaxLoggingServiceImpl( LogReaderServiceImpl logReader )
    {
        m_logReader = logReader;
    }

    public PaxLogger getLogger( Bundle bundle, String category )
    {
        Logger log4jLogger = Logger.getLogger( category );
        return new PaxLoggerImpl( bundle, log4jLogger );
    }

    public int getLogLevel()
    {
        return LOG_DEBUG;
    }

    public void log( int level, String message )
    {
        log( level, message, null );
    }

    public void log( int level, String message, Throwable exception )
    {
        log( (ServiceReference) null, level, message, exception );
    }

    public void log( ServiceReference sr, int level, String message )
    {
        log( sr, level, message, null );
    }

    public void log( ServiceReference sr, int level, String message, Throwable exception )
    {
        log( null, sr, level, message, exception );
    }

    /** This method is used by the FrameworkHandler to log framework events.
     *
     * @param bundle The bundle that caused the event.
     * @param level The level to be logged as.
     * @param message The message.
     * @param exception The exception, if any otherwise null.
     */
    void log( Bundle bundle, int level, String message, Throwable exception )
    {
        log( bundle, null, level, message, exception );
    }

    private void log( Bundle bundle, ServiceReference sr, int level, String message, Throwable exception )
    {
        String category = "";
        if( sr == null )
        {
            category = "[undefined]";
        }
        else
        {
            if( bundle != null )
            {
                bundle = sr.getBundle();
            }
            if( bundle != null )
            {
                category = bundle.getSymbolicName();
            }
        }
        PaxLogger logger = getLogger( bundle, category );
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
        LogEntry entry = new LogEntryImpl( bundle, sr, level, message, exception );
        m_logReader.fireEvent( entry );
    }

    public void updated( Dictionary dictionary )
        throws ConfigurationException
    {
        System.out.println( "I AM CALLED" );
    }
}
