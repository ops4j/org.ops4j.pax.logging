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

import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxContext;
import org.ops4j.pax.logging.PaxLoggingService;
import org.ops4j.pax.logging.spi.support.DefaultServiceLog;
import org.osgi.framework.Bundle;

public class TrackingLogger
        implements PaxLogger
{

    private PaxLoggingService m_service;
    private String m_category;
    private Bundle m_bundle;
    private PaxLogger m_delegate;
    private String m_fqcn;

    public TrackingLogger( PaxLoggingService service, String category, Bundle bundle, String fqcn )
    {
        m_fqcn = fqcn;
        m_category = category;
        m_bundle = bundle;
        added( service );
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
        return m_delegate.isWarnEnabled();
    }

    public boolean isInfoEnabled()
    {
        return m_delegate.isInfoEnabled();
    }

    public boolean isErrorEnabled()
    {
        return m_delegate.isErrorEnabled();
    }

    public boolean isFatalEnabled()
    {
        return m_delegate.isFatalEnabled();
    }

    public void trace( String message, Throwable t )
    {
        m_delegate.trace( message, t );
    }

    public void debug( String message, Throwable t )
    {
        m_delegate.debug( message, t );
    }

    public void inform( String message, Throwable t )
    {
        m_delegate.inform( message, t );
    }

    public void warn( String message, Throwable t )
    {
        m_delegate.warn( message, t );
    }

    public void error( String message, Throwable t )
    {
        m_delegate.error( message, t );
    }

    public void fatal( String message, Throwable t )
    {
        m_delegate.fatal( message, t );
    }

    public void trace( String message, Throwable t, String fqcn )
    {
        m_delegate.trace( message, t, fqcn );
    }

    public void debug( String message, Throwable t, String fqcn  )
    {
        m_delegate.debug( message, t, fqcn );
    }

    public void inform( String message, Throwable t, String fqcn )
    {
        m_delegate.inform( message, t, fqcn );
    }

    public void warn( String message, Throwable t, String fqcn )
    {
        m_delegate.warn( message, t, fqcn );
    }

    public void error( String message, Throwable t, String fqcn )
    {
        m_delegate.error( message, t, fqcn );
    }

    public void fatal( String message, Throwable t, String fqcn )
    {
        m_delegate.fatal( message, t, fqcn );
    }

    public int getLogLevel()
    {
        return m_delegate.getLogLevel();
    }

    public String getName()
    {
        return m_delegate.getName();
    }

    public void added( PaxLoggingService service )
    {
        m_service = service;
        if( m_service != null )
        {
            m_delegate = m_service.getLogger( m_bundle, m_category, m_fqcn );
        }
        else
        {
            m_delegate = new DefaultServiceLog( m_bundle, m_category );
        }
    }

    /**
     * Called by the tracker when there is no service available, and the reference should
     * be dropped.
     */
    public void removed()
    {
        m_service = null;
        m_delegate = new DefaultServiceLog( m_bundle, m_category );
    }
    
    public PaxContext getPaxContext() {
        if (m_service != null) {
            return m_service.getPaxContext();
        } else {
            return m_delegate.getPaxContext();
        }
    }
}
