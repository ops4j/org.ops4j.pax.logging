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
package org.ops4j.pax.logging.avalon;

import org.apache.avalon.framework.logger.Logger;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingManager;

public class AvalonLogger
    implements Logger
{

    static final String AVALON_FQCN = AvalonLogger.class.getName();
    
    private PaxLogger m_delegate;

    public AvalonLogger( PaxLogger logger )
    {
        m_delegate = logger;
    }

    public void debug( String string )
    {
        m_delegate.debug( string, null );
    }

    public void debug( String string, Throwable throwable )
    {
        m_delegate.debug( string, throwable );
    }

    public boolean isDebugEnabled()
    {
        return m_delegate.isDebugEnabled();
    }

    public void info( String string )
    {
        m_delegate.inform( string, null );
    }

    public void info( String string, Throwable throwable )
    {
        m_delegate.inform( string, throwable );
    }

    public boolean isInfoEnabled()
    {
        return m_delegate.isInfoEnabled();
    }

    public void warn( String string )
    {
        m_delegate.warn( string, null );
    }

    public void warn( String string, Throwable throwable )
    {
        m_delegate.warn( string, throwable );
    }

    public boolean isWarnEnabled()
    {
        return m_delegate.isWarnEnabled();
    }

    public void error( String string )
    {
        m_delegate.error( string, null );
    }

    public void error( String string, Throwable throwable )
    {
        m_delegate.error( string, throwable );
    }

    public boolean isErrorEnabled()
    {
        return m_delegate.isErrorEnabled();
    }

    public void fatalError( String string )
    {
        m_delegate.fatal( string, null );
    }

    public void fatalError( String string, Throwable throwable )
    {
        m_delegate.fatal( string, throwable );
    }

    public boolean isFatalErrorEnabled()
    {
        return m_delegate.isFatalEnabled();
    }

    public Logger getChildLogger( String name )
    {
        return AvalonLogFactory.getLogger( this, name );
    }

    public String getName()
    {
        return m_delegate.getName();
    }

    void setPaxLoggingManager( PaxLoggingManager paxLoggingManager, String name )
    {
        m_delegate = paxLoggingManager.getLogger( name, AVALON_FQCN );
    }
}
