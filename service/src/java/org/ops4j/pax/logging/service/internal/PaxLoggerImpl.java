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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class PaxLoggerImpl
    implements org.ops4j.pax.logging.service.PaxLogger
{
    private org.apache.log4j.Logger m_Delegate;

    public PaxLoggerImpl( Logger delegate )
    {
        m_Delegate = delegate;
    }

    public boolean isTraceEnabled()
    {
        return m_Delegate.isEnabledFor( Level.TRACE );
    }

    public boolean isDebugEnabled()
    {
        return m_Delegate.isEnabledFor( Level.DEBUG );
    }

    public boolean isWarnEnabled()
    {
        return m_Delegate.isEnabledFor( Level.WARN );
    }

    public boolean isInfoEnabled()
    {
        return m_Delegate.isEnabledFor( Level.INFO );
    }

    public boolean isErrorEnabled()
    {
        return m_Delegate.isEnabledFor( Level.ERROR );
    }

    public boolean isFatalEnabled()
    {
        return m_Delegate.isEnabledFor( Level.FATAL );
    }

    public void trace( String message, Throwable t )
    {
        m_Delegate.trace( message, t );
    }

    public void debug( String message, Throwable t )
    {
        m_Delegate.debug( message, t );
    }

    public void inform( String message, Throwable t )
    {
        m_Delegate.info( message, t );
    }

    public void warn( String message, Throwable t )
    {
        m_Delegate.warn( message, t );
    }

    public void error( String message, Throwable t )
    {
        m_Delegate.error( message, t );
    }

    public void fatal( String message, Throwable t )
    {
        m_Delegate.fatal( message, t );
    }
}


