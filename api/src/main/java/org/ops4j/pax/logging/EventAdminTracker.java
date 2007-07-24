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
package org.ops4j.pax.logging;

import java.util.LinkedList;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.util.tracker.ServiceTracker;

/**
 * This ServiceTracker is designed to implement the Event Admin service, but delegate
 * to the actual service if available.
 * If the Event Admin service is not available, this tracker will queue the Events until
 * the service becomes available.
 *
 * This design simplifies the PublishAppender, as it can assume that the service is always
 * available.
 */
public class EventAdminTracker extends ServiceTracker
    implements EventAdmin
{
    private final LinkedList m_queue;
    private BundleContext m_context;
    private EventAdmin m_service;

    private int m_maxSize;

    public EventAdminTracker( BundleContext context )
    {
        super( context, EventAdmin.class.getName(), null );
        m_context = context;
        m_queue = new LinkedList();
        m_maxSize = 50;
    }

    public void postEvent( Event event )
    {
        synchronized( m_queue )
        {
            m_queue.add( event );
            deliver();
            cleanup();
        }
    }

    public void sendEvent( Event event )
    {
        synchronized( m_queue )
        {
            m_queue.add( event );
            deliver();
            cleanup();
        }
    }

    public Object addingService( ServiceReference serviceReference )
    {
        m_service = (EventAdmin) m_context.getService( serviceReference );
        synchronized( m_queue )
        {
            deliver();
            return m_service;
        }
    }

    public void removedService( ServiceReference serviceReference, Object object )
    {
        synchronized( m_queue )
        {
            m_service = null;
        }
    }

    private void deliver()
    {
        synchronized( m_queue )
        {
            if( m_service == null )
            {
                return;
            }
            while( m_queue.size() > 0 )
            {
                Event event = (Event) m_queue.remove(0);
                m_service.postEvent( event );
            }
        }
    }

    public int getMaxSize()
    {
        return m_maxSize;
    }

    public void setMaxSize( int maxSize )
    {

        if( maxSize < 0 )
        {
            throw new IllegalArgumentException( "MaxSize must not be a negative number: " + maxSize );
        }
        if( maxSize > 1000 )
        {
            String message = "MaxSize must not be a larger than 1000 for memory constraint reasons: " + maxSize;
            throw new IllegalArgumentException( message );

        }
        m_maxSize = maxSize;
    }

    private void cleanup()
    {
        while( m_queue.size() > m_maxSize )
        {
            m_queue.remove( 0 );
        }
    }
}
