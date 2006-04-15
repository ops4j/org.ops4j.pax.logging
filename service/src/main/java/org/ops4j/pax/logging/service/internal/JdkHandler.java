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
import org.ops4j.pax.logging.PaxLoggingService;

public class JdkHandler extends Handler
{
    private PaxLoggingService m_logService;
    private static final org.apache.log4j.Level[] LOG4J_MAPPING;

    static
    {
        int max = max( -1000000, Level.OFF.intValue() );
        max = max( max, Level.FINE.intValue() );
        max = max( max, Level.FINER.intValue() );
        max = max( max, Level.FINEST.intValue() );
        max = max( max, Level.INFO.intValue() );
        max = max( max, Level.WARNING.intValue() );
        max = max( max, Level.SEVERE.intValue() );
        LOG4J_MAPPING = new org.apache.log4j.Level[ max ];
        LOG4J_MAPPING[ Level.OFF.intValue() ] = org.apache.log4j.Level.OFF;
        LOG4J_MAPPING[ Level.FINE.intValue() ] = org.apache.log4j.Level.DEBUG;
        LOG4J_MAPPING[ Level.FINER.intValue() ] = org.apache.log4j.Level.TRACE;
        LOG4J_MAPPING[ Level.FINEST.intValue() ] = org.apache.log4j.Level.TRACE;
        LOG4J_MAPPING[ Level.INFO.intValue() ] = org.apache.log4j.Level.INFO;
        LOG4J_MAPPING[ Level.WARNING.intValue() ] = org.apache.log4j.Level.WARN;
        LOG4J_MAPPING[ Level.SEVERE.intValue() ] = org.apache.log4j.Level.ERROR;
    }

    public JdkHandler( PaxLoggingService logService )
    {
        m_logService = logService;
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
        PaxLoggerImpl logger = (PaxLoggerImpl) m_logService.getLogger( loggerName );
        String message = record.getMessage();
        Throwable throwable = record.getThrown();
        int severity = level.intValue();
        org.apache.log4j.Level log4jlevel = LOG4J_MAPPING[ severity ];
        String callerFQCN = record.getSourceClassName();
        logger.log( callerFQCN, log4jlevel, message, throwable );
    }

    static private int max( int i1, int i2 )
    {
        return i1 > i2 ? i1 : i2;
    }

}
