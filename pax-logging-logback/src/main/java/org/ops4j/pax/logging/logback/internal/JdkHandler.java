/*
 * Copyright 2006 Niclas Hedhman.
 * Copyright 2011 Avid Technology, Inc.
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
package org.ops4j.pax.logging.logback.internal;

import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingService;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * @author Chris Dolan -- adapted from pax-logging-service v1.6.0
 */
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
        String loggerName = record.getLoggerName();
        // TODO: Can't associate a bundle with the JDK logger. So how??
        String fqcn = java.util.logging.Logger.class.getName();
        PaxLogger logger = m_logService.getLogger( null, loggerName, fqcn );
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
        int levelInt = level.intValue();
        if (levelInt <= Level.FINEST.intValue())
            logger.trace(message, throwable);
        else if (levelInt <= Level.FINE.intValue())
            logger.debug(message, throwable);
        else if (levelInt <= Level.INFO.intValue())
            logger.inform(message, throwable);
        else if (levelInt <= Level.WARNING.intValue())
            logger.warn(message, throwable);
        else
            logger.error(message, throwable);
    }
}
