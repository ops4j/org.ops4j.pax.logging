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
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
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
    private final LinkedList m_entries;
    private int m_maxEntries;

    public LogReaderServiceImpl( int maxEntries )
    {
        m_maxEntries = maxEntries;
        m_listeners = new ArrayList();
        m_entries = new LinkedList();
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
        return Collections.enumeration( m_entries );
    }

    final void fireEvent( LogEntry entry )
    {
        synchronized( m_entries )
        {
            m_entries.add( 0, entry );
        }
        Iterator iterator = m_listeners.iterator();
        while( iterator.hasNext() )
        {
            LogListener listener = (LogListener) iterator.next();
            fire( listener, entry );
        }
        cleanUp();
    }

    private void cleanUp()
    {
        synchronized( m_entries )
        {
            while( m_entries.size() > m_maxEntries )
            {
                m_entries.removeLast();
            }
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
