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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
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
        return m_delegate.isEnabledFor(Level.WARN);
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
            BundleRevision rev = m_bundle.adapt(BundleRevision.class);
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

    private void doLog( final Level level, final int svcLevel, final String fqcn, final String message, final Throwable t ) {
        if (System.getSecurityManager() != null) {
            AccessController.doPrivileged(
                    new PrivilegedAction<Void>() {
                        public Void run() {
                            doLog0( level, svcLevel, fqcn, message, t );
                            return null;
                        }
                    }
            );
        } else {
            doLog0( level, svcLevel, fqcn, message, t );
        }
    }

    private void doLog0( final Level level, final int svcLevel, final String fqcn, final String message, final Throwable t ) {
        setDelegateContext();
        try {
            m_delegate.log(fqcn, level, message, t);
        } finally {
            clearDelegateContext();
        }
        m_service.handleEvents(m_bundle, null, svcLevel, message, t);
    }

    public void trace( String message, Throwable t )
    {
        if( isTraceEnabled() )
        {
            doLog( Level.TRACE, LogService.LOG_DEBUG, m_fqcn, message, t );
        }
    }

    public void debug( String message, Throwable t )
    {
        if( isDebugEnabled() )
        {
            doLog( Level.DEBUG, LogService.LOG_DEBUG, m_fqcn, message, t );
        }
    }

    public void inform( String message, Throwable t )
    {
        if( isInfoEnabled() )
        {
            doLog( Level.INFO, LogService.LOG_INFO, m_fqcn, message, t );
        }
    }

    public void warn( String message, Throwable t )
    {
        if( isWarnEnabled() )
        {
            doLog( Level.WARN, LogService.LOG_WARNING, m_fqcn, message, t );
        }
    }

    public void error( String message, Throwable t )
    {
        if( isErrorEnabled() )
        {
            doLog( Level.ERROR, LogService.LOG_ERROR, m_fqcn, message, t );
        }
    }

    public void fatal( String message, Throwable t )
    {
        if( isFatalEnabled() )
        {
            doLog( Level.FATAL, LogService.LOG_ERROR, m_fqcn, message, t );
        }
    }

    public void trace( String message, Throwable t, String fqcn )
    {
        if( isTraceEnabled() )
        {
            doLog( Level.TRACE, LogService.LOG_DEBUG, fqcn, message, t );
        }
    }

    public void debug( String message, Throwable t, String fqcn )
    {
        if( isDebugEnabled() )
        {
            doLog( Level.DEBUG, LogService.LOG_DEBUG, fqcn, message, t );
        }
    }

    public void inform( String message, Throwable t, String fqcn )
    {
        if( isInfoEnabled() )
        {
            doLog( Level.INFO, LogService.LOG_INFO, fqcn, message, t );
        }
    }

    public void warn( String message, Throwable t, String fqcn )
    {
        if( isWarnEnabled() )
        {
            doLog( Level.WARN, LogService.LOG_WARNING, fqcn, message, t );
        }
    }

    public void error( String message, Throwable t, String fqcn )
    {
        if( isErrorEnabled() )
        {
            doLog( Level.ERROR, LogService.LOG_ERROR, fqcn, message, t );
        }
    }

    public void fatal( String message, Throwable t, String fqcn )
    {
        if( isFatalEnabled() )
        {
            doLog( Level.FATAL, LogService.LOG_ERROR, fqcn, message, t );
        }
    }

    public int getLogLevel()
    {

        Level level = m_delegate.getEffectiveLevel();

        if ( level == null )
            return LEVEL_ERROR;

        if ( Level.TRACE.isGreaterOrEqual( level ) )
            return LEVEL_TRACE;

        if ( Level.DEBUG.isGreaterOrEqual( level ) )
            return LEVEL_DEBUG;

        if ( Level.INFO.isGreaterOrEqual( level ) )
            return LEVEL_INFO;

        if ( Level.WARN.isGreaterOrEqual( level ) )
            return LEVEL_WARNING;

        return LEVEL_ERROR;

    }

    public String getName()
    {
        return m_delegate.getName();
    }

    public PaxContext getPaxContext()
    {
        return m_service.getPaxContext();
    }
}
