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

import java.util.Iterator;
import java.util.Map;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.Priority;
import org.ops4j.pax.logging.PaxContext;
import org.ops4j.pax.logging.PaxLogger;
import org.osgi.framework.Bundle;
import org.osgi.service.log.LogService;

public class PaxLoggerImpl
        implements PaxLogger 
{

    private org.apache.log4j.Logger m_delegate;
    private String m_fqcn;
    private Bundle m_bundle;
    private PaxLoggingServiceImpl m_service;
  
    /**
     * @param bundle The bundle that this PaxLogger belongs to.
     * @param delegate The Log4J delegate to receive the log message.
     * @param fqcn     The fully qualified classname of the client owning this logger.
     * @param service The service to be used to handle the logging events.
     */
    PaxLoggerImpl( Bundle bundle, Logger delegate, String fqcn, PaxLoggingServiceImpl service ) 
    {
        m_delegate = delegate;
        m_fqcn = fqcn;
        m_bundle = bundle;
        m_service = service;        
    }

    public boolean isTraceEnabled() 
    {
        return m_delegate.isTraceEnabled();
    }

    public boolean isDebugEnabled() 
    {
        return m_delegate.isDebugEnabled();
    }

    public boolean isWarnEnabled() 
    {
        return m_delegate.isEnabledFor( Level.WARN );
    }

    public boolean isInfoEnabled() 
    {
        return m_delegate.isInfoEnabled();
    }

    public boolean isErrorEnabled() 
    {
        return m_delegate.isEnabledFor( Level.ERROR );
    }

    public boolean isFatalEnabled() 
    {
        return m_delegate.isEnabledFor( Level.FATAL );
    }

    private void setDelegateContext() 
    {
        Map context = getPaxContext().getContext();
        if (context != null) {
            for (Iterator keys = context.keySet().iterator(); keys.hasNext();) {
                String key = (String) keys.next();
                Object value = context.get(key);
                MDC.put(key, value);
            }
        }
    }

    private void clearDelegateContext() 
    {
        if (MDC.getContext() != null) {
            MDC.getContext().clear();
        }
    }

    public void trace( String message, Throwable t ) 
    {
        setDelegateContext();
        m_delegate.log( m_fqcn, Level.TRACE, message, t );
        clearDelegateContext();
        m_service.handleEvents( m_bundle, null, LogService.LOG_DEBUG, message, t );
    }

    public void debug( String message, Throwable t ) 
    {
        setDelegateContext();
        m_delegate.log( m_fqcn, Level.DEBUG, message, t );
        clearDelegateContext();
        m_service.handleEvents( m_bundle, null, LogService.LOG_DEBUG, message, t );
    }

    public void inform( String message, Throwable t ) 
    {
        setDelegateContext();
        m_delegate.log( m_fqcn, Level.INFO, message, t );
        clearDelegateContext();
        m_service.handleEvents( m_bundle, null, LogService.LOG_INFO, message, t );
    }

    public void warn( String message, Throwable t ) 
    {
        setDelegateContext();
        m_delegate.log( m_fqcn, Level.WARN, message, t );
        clearDelegateContext();
        m_service.handleEvents( m_bundle, null, LogService.LOG_WARNING, message, t );
    }

    public void error( String message, Throwable t ) 
    {
        setDelegateContext();
        m_delegate.log( m_fqcn, Level.ERROR, message, t );
        clearDelegateContext();
        m_service.handleEvents( m_bundle, null, LogService.LOG_ERROR, message, t );
    }

    public void fatal( String message, Throwable t ) 
    {
        setDelegateContext();
        m_delegate.log( m_fqcn, Level.FATAL, message, t );
        clearDelegateContext();
        m_service.handleEvents( m_bundle, null, LogService.LOG_ERROR, message, t );
    }

    public int getLogLevel() 
    {
        return m_delegate.getLevel().toInt();
    }

    public String getName() 
    {
        return m_delegate.getName();
    }

    public void log( String callerFQCN, Priority level, Object message, Throwable t ) 
    {
        if( callerFQCN == null ) 
        {
            callerFQCN = m_fqcn;
        }
        m_delegate.log( callerFQCN, level, message, t );
    }
    
    public PaxContext getPaxContext() 
    {
        return m_service.getPaxContext();
    }
}
