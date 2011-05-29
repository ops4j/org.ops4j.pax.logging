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
package org.ops4j.pax.logging.log4j.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;

/**
 * @noinspection SynchronizeOnNonFinalField
 */
public class LogReaderServiceImpl
    implements LogReaderService
{

    private List m_listeners;
    private final LinkedList m_entries;
    private int m_maxEntries;

    public LogReaderServiceImpl( int maxEntries )
    {
        m_maxEntries = maxEntries;
        m_entries = new LinkedList();
    }

    public void addLogListener( LogListener logListener )
    {
        synchronized( this )
        {
            // Do not update the list directly to avoid
            // the cost of a synchronized block when iterating
            // listeners in fireEvent()
            ArrayList clone;
            if( m_listeners == null )
            {
                clone = new ArrayList();
            }
            else
            {
                clone = new ArrayList( m_listeners );
            }
            clone.add( logListener );
            m_listeners = clone;
        }
    }

    public void removeLogListener( LogListener logListener )
    {
        synchronized( this )
        {
            // Do not update the list directly to avoid
            // the cost of a synchronized block when iterating
            // listeners in fireEvent()
            if( m_listeners == null )
            {
                return;
            }
            ArrayList clone = new ArrayList( m_listeners );
            clone.remove( logListener );
            if( clone.size() == 0 )
            {
                m_listeners = null;
            }
            else
            {
                m_listeners = clone;
            }
        }
    }

    public Enumeration getLog()
    {
        // Need to do a copy to avoid a ConcurrentModificationException if
        // a new event is logged while the enumeration is iterated.
        return Collections.enumeration( new ArrayList( m_entries ) );
    }

    private void cleanUp()
    {
        while( m_entries.size() > m_maxEntries )
        {
            m_entries.removeLast();
        }
    }

    final void fireEvent( LogEntry entry )
    {
        synchronized( m_entries )
        {
            m_entries.addFirst( entry );
            cleanUp();
        }
        final List listeners = m_listeners;
        if( listeners == null )
        {
            return;
        }
        Iterator iterator = listeners.iterator();
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
        }
        catch( Throwable e )
        {
            //TODO: Log that we are removing the LogListener, since it is throwing exception. For now System.err
            System.err.println( "'" + listener + "' is removed as a LogListener, since it threw an exception." );
            removeLogListener( listener );
        }
    }

    /**
     * Sets the max number of entries that should be allowed in the LogReader buffer.
     *
     * @param maxSize the maximum number of entries in the LogReader buffer.
     */
    final void setMaxEntries( int maxSize )
    {
        m_maxEntries = maxSize;
    }
}
