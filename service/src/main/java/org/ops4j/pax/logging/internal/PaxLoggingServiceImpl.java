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

import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingService;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.cm.ConfigurationException;
import org.knopflerfish.service.log.LogService;
import org.apache.log4j.Logger;
import java.util.Dictionary;

public class PaxLoggingServiceImpl
    implements PaxLoggingService, LogService, ManagedService
{

    public PaxLogger getLogger( String category )
    {
        Logger log4jLogger = Logger.getLogger( category );
        return new PaxLoggerImpl( log4jLogger );
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
        log( null, level, message, exception );
    }

    public void log( ServiceReference sr, int level, String message )
    {
        log( sr, level, message, null );
    }

    public void log( ServiceReference sr, int level, String message, Throwable exception )
    {
        String category;
        if( sr == null )
        {
            category = "[undefined]";
        }
        else
        {
            category = sr.getBundle().getSymbolicName();
        }
        PaxLogger logger = getLogger( category );
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
    }

    public void updated( Dictionary dictionary )
        throws ConfigurationException
    {
        System.out.println( "I AM CALLED" );
    }
}
