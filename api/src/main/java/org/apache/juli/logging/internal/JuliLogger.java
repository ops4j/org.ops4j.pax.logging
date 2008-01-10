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
package org.apache.juli.logging.internal;

import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingManager;
import org.apache.juli.logging.Log;

public class JuliLogger
    implements Log
{

    public static final String JULI_FQCN = JuliLogger.class.getName();

    private PaxLogger m_delegate;

    public JuliLogger( PaxLogger delegate )
    {
        m_delegate = delegate;
    }

    public boolean isDebugEnabled()
    {
        return m_delegate.isDebugEnabled();
    }

    public boolean isErrorEnabled()
    {
        return m_delegate.isErrorEnabled();
    }

    public boolean isFatalEnabled()
    {
        return m_delegate.isFatalEnabled();
    }

    public boolean isInfoEnabled()
    {
        return m_delegate.isInfoEnabled();
    }

    public boolean isTraceEnabled()
    {
        return m_delegate.isTraceEnabled();
    }

    public boolean isWarnEnabled()
    {
        return m_delegate.isWarnEnabled();
    }

    public void trace( Object message )
    {
        if( message != null )
        {
            m_delegate.trace( message.toString(), null );
        }
    }

    public void trace( Object message, Throwable t )
    {
        if( message != null )
        {
            m_delegate.trace( message.toString(), t );
        }
        else
        {
            m_delegate.trace( null, t );
        }
    }

    public void debug( Object message )
    {
        if( message != null )
        {
            m_delegate.debug( message.toString(), null );
        }
    }

    public void debug( Object message, Throwable t )
    {
        if( message != null )
        {
            m_delegate.debug( message.toString(), t );
        }
        else
        {
            m_delegate.debug( null, t );
        }
    }

    public void info( Object message )
    {
        if( message != null )
        {
            m_delegate.inform( message.toString(), null );
        }
    }

    public void info( Object message, Throwable t )
    {
        if( message != null )
        {
            m_delegate.inform( message.toString(), null );
        }
    }

    public void warn( Object message )
    {
        if( message != null )
        {
            m_delegate.warn( message.toString(), null );
        }
    }

    public void warn( Object message, Throwable t )
    {
        if( message != null )
        {
            m_delegate.warn( message.toString(), t );
        }
        else
        {
            m_delegate.warn( null, t );
        }
    }

    public void error( Object message )
    {
        if( message != null )
        {
            m_delegate.error( message.toString(), null );
        }

    }

    public void error( Object message, Throwable t )
    {
        if( message != null )
        {
            m_delegate.error( message.toString(), t );
        }
        else
        {
            m_delegate.error( null, t );
        }
    }

    public void fatal( Object message )
    {
        if( message != null )
        {
            m_delegate.fatal( message.toString(), null );
        }
    }

    public void fatal( Object message, Throwable t )
    {
        if( message != null )
        {
            m_delegate.fatal( message.toString(), t );
        }
        else
        {
            m_delegate.fatal( null, t );
        }
    }

    public int getLogLevel()
    {
        return m_delegate.getLogLevel();
    }

    /**
     * Not a public method.
     *
     * @param paxLoggingManager TODO
     * @param name TODO
     */
    public void setPaxLoggingManager( PaxLoggingManager paxLoggingManager, String name )
    {
        m_delegate = paxLoggingManager.getLogger( name, JULI_FQCN );
    }
}
