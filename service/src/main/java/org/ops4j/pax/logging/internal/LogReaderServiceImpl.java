/*  Copyright 2007 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.logging.internal;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;

/**
 * @noinspection SynchronizeOnNonFinalField
 */
public class LogReaderServiceImpl
    implements LogReaderService
{

    private ArrayList m_listeners;
    private Vector m_entries;

    public LogReaderServiceImpl()
    {
        m_listeners = new ArrayList();
        m_entries = new Vector();
    }

    public void addLogListener( LogListener logListener )
    {
        synchronized( m_listeners )
        {
            ArrayList clone = new ArrayList( m_listeners );
            clone.add( logListener );
            m_listeners = clone;
        }
    }

    public void removeLogListener( LogListener logListener )
    {
        synchronized( m_listeners )
        {
            ArrayList clone = new ArrayList( m_listeners );
            clone.remove( logListener );
            m_listeners = clone;
        }
    }

    public Enumeration getLog()
    {
        return m_entries.elements();
    }

    void fireEvent( LogEntry entry )
    {
        m_entries.addElement( entry );
        Iterator iterator = m_listeners.iterator();
        while( iterator.hasNext() )
        {
            LogListener listener = (LogListener) iterator.next();
            fire( listener, entry );
        }
    }

    private void fire( LogListener listener, LogEntry entry )
    {
        try
        {
            listener.logged( entry );
        } catch( Throwable e )
        {
            //TODO: Log that we are removing the LogListener, since it is throwing exception. For now System.err
            System.err.println( "'" + listener + "' is removed as a LogListener, since it threw an exception." );
            removeLogListener( listener );
        }
    }
}
