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
import java.util.Iterator;
import java.util.Map;

import org.ops4j.pax.logging.FqcnIgnoringPaxLogger;
import org.osgi.framework.Bundle;
import org.ops4j.pax.logging.PaxContext;
import org.ops4j.pax.logging.PaxLogger;

/**
 * Experimental fallback strategy for non-availability.
 */
public class BufferingLog extends FqcnIgnoringPaxLogger
{

    private static class LogType
    {

        private static final int TRACE_INT = 0;
        private static final int DEBUG_INT = 1;
        private static final int INFO_INT = 2;
        private static final int WARN_INT = 3;
        private static final int ERROR_INT = 4;
        private static final int FATAL_INT = 5;

        private static LogType trace = new LogType( TRACE_INT );
        private static LogType debug = new LogType( DEBUG_INT );
        private static LogType info = new LogType( INFO_INT );
        private static LogType warn = new LogType( WARN_INT );
        private static LogType error = new LogType( ERROR_INT );
        private static LogType fatal = new LogType( FATAL_INT );

        private final int m_type;

        private LogType( int type )
        {
            m_type = type;
        }

        private int getType()
        {
            return m_type;
        }
    }

    private ArrayList m_queue;
    private PaxContext m_context = new PaxContext();

    public BufferingLog( Bundle bundle, String categoryName )
    {
        m_queue = new ArrayList();
    }

    void flush( PaxLogger destination )
    {
        Iterator iterator = m_queue.iterator();
        while( iterator.hasNext() )
        {
            LogPackage pack = (LogPackage) iterator.next();
            Throwable throwable = pack.getException();
            String message = pack.getMessage();
            getPaxContext().putAll( pack.getContext() );
            LogType logType = pack.getType();
            int logTypeAsInt = logType.getType();
            switch( logTypeAsInt )
            {
                case LogType.DEBUG_INT:
                    destination.debug( message, throwable );
                    break;
                case LogType.TRACE_INT:
                    destination.trace( message, throwable );
                    break;
                case LogType.INFO_INT:
                    destination.inform( message, throwable );
                    break;
                case LogType.WARN_INT:
                    destination.warn( message, throwable );
                    break;
                case LogType.ERROR_INT:
                    destination.error( message, throwable );
                    break;
                case LogType.FATAL_INT:
                    destination.fatal( message, throwable );
                    break;
            }
            getPaxContext().clear();
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
        LogPackage p = new LogPackage( LogType.trace, message, t, getPaxContext().getContext() );
        m_queue.add( p );
    }

    public void debug( String message, Throwable t )
    {
        LogPackage p = new LogPackage( LogType.debug, message, t, getPaxContext().getContext() );
        m_queue.add( p );
    }

    public void inform( String message, Throwable t )
    {
        LogPackage p = new LogPackage( LogType.info, message, t, getPaxContext().getContext() );
        m_queue.add( p );
    }

    public void warn( String message, Throwable t )
    {
        LogPackage p = new LogPackage( LogType.warn, message, t, getPaxContext().getContext() );
        m_queue.add( p );
    }

    public void error( String message, Throwable t )
    {
        LogPackage p = new LogPackage( LogType.error, message, t, getPaxContext().getContext() );
        m_queue.add( p );
    }

    public void fatal( String message, Throwable t )
    {
        LogPackage p = new LogPackage( LogType.fatal, message, t, getPaxContext().getContext() );
        m_queue.add( p );
    }

    public int getLogLevel()
    {
        return PaxLogger.LEVEL_TRACE;
    }

    public String getName()
    {
        return "";
    }

    public PaxContext getPaxContext()
    {
        return m_context;
    }

    private static class LogPackage
    {

        private LogType m_type;
        private String m_message;
        private Throwable m_exception;
        private Map m_context;

        public LogPackage( LogType type, String message, Throwable exception, Map context )
        {
            m_type = type;
            m_message = message;
            m_exception = exception;
            m_context = context;
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

        public Map getContext()
        {
            return m_context;
        }
    }
}
