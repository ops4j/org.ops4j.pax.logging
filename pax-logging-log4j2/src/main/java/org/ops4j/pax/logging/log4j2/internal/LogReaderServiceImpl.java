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
package org.ops4j.pax.logging.log4j2.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;

/**
 * @noinspection SynchronizeOnNonFinalField
 */
public class LogReaderServiceImpl
    implements LogReaderService
{

    private final List<LogListener> m_listeners = new CopyOnWriteArrayList<LogListener>();
    private final Deque<LogEntry> m_entries;
    private int m_maxEntries;

    public LogReaderServiceImpl(int maxEntries)
    {
        m_maxEntries = maxEntries;
        m_entries = new LinkedList<LogEntry>();
    }

    public void addLogListener( LogListener logListener )
    {
        m_listeners.add(logListener);
    }

    public void removeLogListener( LogListener logListener )
    {
        m_listeners.remove(logListener);
    }

    public Enumeration getLog()
    {
        // Need to do a copy to avoid a ConcurrentModificationException if
        // a new event is logged while the enumeration is iterated.
        synchronized (m_entries)
        {
            return Collections.enumeration( new ArrayList<LogEntry>( m_entries ) );
        }
    }

    private void cleanUp()
    {
        // caller must synchronize on m_entries
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
        final List<LogListener> listeners = m_listeners;
        for (LogListener listener : listeners) {
            fire(listener, entry);
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
        synchronized (m_entries)
        {
            m_maxEntries = maxSize;
        }
    }
}
