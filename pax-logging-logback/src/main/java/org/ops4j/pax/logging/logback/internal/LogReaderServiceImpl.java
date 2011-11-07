/*  Copyright 2007 Niclas Hedhman.
 * Copyright 2011 Avid Technology, Inc.
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
package org.ops4j.pax.logging.logback.internal;

import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Implements LogReaderService: holds the last N log messages in memory for clients to access later.
 * 
 * <p>
 * This code was originally derived from org.ops4j.pax.logging.service.internal.LogReaderServiceImpl v1.6.0.
 * Changes include:
 * <ul>
 *     <li>converted listener array to CopyOnWriteArrayList for brevity</li>
 *     <li>generics</li>
 * </ul>
 *
 * @author Chris Dolan
 * @noinspection SynchronizeOnNonFinalField
 */
public class LogReaderServiceImpl
    implements LogReaderService
{

    private List<LogListener> m_listeners = new CopyOnWriteArrayList<LogListener>();
    private final LinkedList<LogEntry> m_entries;
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
        synchronized( m_entries )
        {
            // Need to do a copy to avoid a ConcurrentModificationException if
            // a new event is logged while the enumeration is iterated.
            return Collections.enumeration( new ArrayList<LogEntry>( m_entries ) );
        }
    }

    private void cleanUp()
    {
        while( m_entries.size() > m_maxEntries )
        {
            m_entries.removeLast();
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

    LogReaderServiceAccess getAccessDelegate()
    {
    	return new LogReaderServiceAccess()
    	{
			public void fireEvent(LogEntry entry)
			{
		        synchronized( m_entries )
		        {
		            m_entries.addFirst( entry );
		            cleanUp();
		        }
		        for (LogListener listener : m_listeners) {
		            fire(listener, entry);
		        }
			}
			public void setMaxEntries(int maxSize)
			{
		        m_maxEntries = maxSize;
			}
		};
    }
}
