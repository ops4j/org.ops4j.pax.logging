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
package org.ops4j.pax.logging.service.internal;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import org.ops4j.pax.logging.PaxLoggingService;

public class JdkHandler extends Handler
{

    private PaxLoggingService m_logService;

    public JdkHandler( PaxLoggingService logService )
    {
        m_logService = logService;
        setFormatter( new SimpleFormatter() );
    }

    /**
     * Close the <tt>Handler</tt> and free all associated resources.
     * <p>
     * The close method will perform a <tt>flush</tt> and then close the
     * <tt>Handler</tt>.   After close has been called this <tt>Handler</tt>
     * should no longer be used.  Method calls may either be silently
     * ignored or may throw runtime exceptions.
     *
     * @throws SecurityException if a security manager exists and if
     *                           the caller does not have <tt>LoggingPermission("control")</tt>.
     */
    public void close()
        throws SecurityException
    {
    }

    /**
     * Flush any buffered output.
     */
    public void flush()
    {
    }

    /**
     * Publish a <tt>LogRecord</tt>.
     * <p>
     * The logging request was made initially to a <tt>Logger</tt> object,
     * which initialized the <tt>LogRecord</tt> and forwarded it here.
     * <p>
     * The <tt>Handler</tt>  is responsible for formatting the message, when and
     * if necessary.  The formatting should include localization.
     *
     * @param record description of the log event
     */
    public void publish( LogRecord record )
    {
        Level level = record.getLevel();
        if( level.intValue() == Level.CONFIG.intValue() )
        {
            return;
        }
        String loggerName = record.getLoggerName();
        // TODO: Can't associate a bundle with the JDK logger. So how??
        String fqcn = java.util.logging.Logger.class.getName();
        PaxLoggerImpl logger = (PaxLoggerImpl) m_logService.getLogger( null, loggerName, fqcn );
        String message;
        try
        {
            message = getFormatter().formatMessage( record );
        }
        catch (Exception ex)
        {
            message = record.getMessage();
        }
        Throwable throwable = record.getThrown();
        org.apache.log4j.Level log4jlevel;
        if( level == Level.OFF )
        {
            log4jlevel = org.apache.log4j.Level.OFF;
        }
        else if( level == Level.FINE )
        {
            log4jlevel = org.apache.log4j.Level.DEBUG;
        }
        else if( level == Level.FINER || level == Level.FINEST )
        {
            log4jlevel = org.apache.log4j.Level.TRACE;
        }
        else if( level == Level.INFO )
        {
            log4jlevel = org.apache.log4j.Level.INFO;
        }
        else if( level == Level.WARNING )
        {
            log4jlevel = org.apache.log4j.Level.WARN;
        }
        else if( level == Level.SEVERE )
        {
            log4jlevel = org.apache.log4j.Level.ERROR;
        }
        else
        {
            log4jlevel = org.apache.log4j.Level.INFO;
        }
        //bug fixed here
        logger.log( log4jlevel, message, throwable );
    }

}
