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
package org.ops4j.pax.logging.service.internal;

import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.Priority;
import org.ops4j.pax.logging.PaxContext;
import org.ops4j.pax.logging.PaxLogger;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.service.log.LogService;

public class PaxLoggerImpl
    implements PaxLogger
{

    private org.apache.log4j.Logger m_delegate;
    private String m_fqcn;
    private Bundle m_bundle;
    private BundleRevision m_bundleRevision;
    private Long m_bundleId;
    private String m_bundleSymbolicName;
    private String m_bundleVersion;
    private PaxLoggingServiceImpl m_service;

    /**
     * @param bundle   The bundle that this PaxLogger belongs to.
     * @param delegate The Log4J delegate to receive the log message.
     * @param fqcn     The fully qualified classname of the client owning this logger.
     * @param service  The service to be used to handle the logging events.
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
        if( context != null )
        {
            for (Object o : context.keySet()) {
                String key = (String) o;
                Object value = context.get(key);
                MDC.put(key, value);
            }
        }
        if (m_bundle != null)
        {
            BundleRevision rev = (BundleRevision) m_bundle.adapt(BundleRevision.class);
            if (rev != m_bundleRevision) {
                m_bundleId = m_bundle.getBundleId();
                m_bundleSymbolicName = m_bundle.getSymbolicName();
                m_bundleVersion = m_bundle.getVersion().toString();
                m_bundleRevision = rev;
            }
            put("bundle.id", m_bundleId);
            put("bundle.name", m_bundleSymbolicName);
            put("bundle.version", m_bundleVersion);
        }
        m_service.getConfigLock().readLock().lock();
    }

    private void put(String name, Object o)
    {
        if (o != null)
        {
            MDC.put(name, o);
        }
    }

    private void clearDelegateContext()
    {
        m_service.getConfigLock().readLock().unlock();
        if( MDC.getContext() != null )
        {
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

    public void trace( String message, Throwable t, String fqcn )
    {
        setDelegateContext();
        m_delegate.log( fqcn, Level.TRACE, message, t);
        clearDelegateContext();
        m_service.handleEvents( m_bundle, null, LogService.LOG_DEBUG, message, t );
    }

    public void debug( String message, Throwable t, String fqcn )
    {
        setDelegateContext();
        m_delegate.log( fqcn, Level.DEBUG, message, t );
        clearDelegateContext();
        m_service.handleEvents( m_bundle, null, LogService.LOG_DEBUG, message, t );
    }

    public void inform( String message, Throwable t, String fqcn )
    {
        setDelegateContext();
        m_delegate.log( fqcn, Level.INFO, message, t );
        clearDelegateContext();
        m_service.handleEvents( m_bundle, null, LogService.LOG_INFO, message, t );
    }

    public void warn( String message, Throwable t, String fqcn )
    {
        setDelegateContext();
        m_delegate.log( fqcn, Level.WARN, message, t );
        clearDelegateContext();
        m_service.handleEvents( m_bundle, null, LogService.LOG_WARNING, message, t );
    }

    public void error( String message, Throwable t, String fqcn )
    {
        setDelegateContext();
        m_delegate.log( fqcn, Level.ERROR, message, t );
        clearDelegateContext();
        m_service.handleEvents( m_bundle, null, LogService.LOG_ERROR, message, t );
    }

    public void fatal( String message, Throwable t, String fqcn )
    {
        setDelegateContext();
        m_delegate.log( fqcn, Level.FATAL, message, t );
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

    //Fixed bug instead of the fully qualified class name of the logger was given the name of the caller
    public void log( Priority level, Object message, Throwable t )
    {
        setDelegateContext();
        m_delegate.log( m_fqcn, level, message, t );
        clearDelegateContext();
    }

    public PaxContext getPaxContext()
    {
        return m_service.getPaxContext();
    }
}
