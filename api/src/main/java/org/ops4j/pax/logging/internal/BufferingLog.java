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
package org.ops4j.pax.logging.internal;

import java.util.ArrayList;
import org.ops4j.pax.logging.PaxLogger;

/**
 * Experimental fallback strategy for non-availability.
 */
public class BufferingLog
    implements PaxLogger
{

    private enum LogType
    {
        trace, debug, info, warn, error, fatal
    }

    private ArrayList<LogPackage> m_queue;

    public BufferingLog()
    {
        m_queue = new ArrayList<LogPackage>();
    }

    void flush( PaxLogger destination )
    {
        for( LogPackage pack : m_queue )
        {
            Throwable throwable = pack.getException();
            String message = pack.getMessage();
            switch( pack.getType() )
            {
                case debug:
                    destination.debug( message, throwable );
                    break;
                case trace:
                    destination.trace( message, throwable );
                    break;
                case info:
                    destination.inform( message, throwable );
                    break;
                case warn:
                    destination.warn( message, throwable );
                    break;
                case error:
                    destination.error( message, throwable );
                    break;
                case fatal:
                    destination.fatal( message, throwable );
                    break;
            }
        }
    }

    public boolean isTraceEnabled()
    {
        return true;
    }

    public boolean isDebugEnabled()
    {
        return true;
    }

    public boolean isWarnEnabled()
    {
        return true;
    }

    public boolean isInfoEnabled()
    {
        return true;
    }

    public boolean isErrorEnabled()
    {
        return true;
    }

    public boolean isFatalEnabled()
    {
        return true;
    }

    public void trace( String message, Throwable t )
    {
        LogPackage p = new LogPackage( LogType.trace, message, t );
        m_queue.add( p );
    }

    public void debug( String message, Throwable t )
    {
        LogPackage p = new LogPackage( LogType.debug, message, t );
        m_queue.add( p );
    }

    public void inform( String message, Throwable t )
    {
        LogPackage p = new LogPackage( LogType.info, message, t );
        m_queue.add( p );
    }

    public void warn( String message, Throwable t )
    {
        LogPackage p = new LogPackage( LogType.warn, message, t );
        m_queue.add( p );
    }

    public void error( String message, Throwable t )
    {
        LogPackage p = new LogPackage( LogType.error, message, t );
        m_queue.add( p );
    }

    public void fatal( String message, Throwable t )
    {
        LogPackage p = new LogPackage( LogType.fatal, message, t );
        m_queue.add( p );
    }

    public int getLogLevel()
    {
        return PaxLogger.LEVEL_TRACE;
    }

    private static class LogPackage
    {

        private LogType m_type;
        private String m_message;
        private Throwable m_exception;

        public LogPackage( LogType type, String message, Throwable exception )
        {
            m_type = type;
            m_message = message;
            m_exception = exception;
        }

        public String getMessage()
        {
            return m_message;
        }

        public Throwable getException()
        {
            return m_exception;
        }

        public LogType getType()
        {
            return m_type;
        }
    }
}
