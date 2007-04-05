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
package org.ops4j.pax.logging.appenders.publish.internal;

import java.util.LinkedList;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.util.tracker.ServiceTracker;

/** This ServiceTracker is designed to implement the Event Admin service, but delegate
 * to the actual service if available.
 * If the Event Admin service is not available, this tracker will queue the Events until
 * the service becomes available.
 *
 * This design simplifies the PublishAppender, as it can assume that the service is always
 * available.
 */
public class EventAdminTracker extends ServiceTracker
    implements EventAdmin, Runnable
{
    private Thread m_thread;
    private final LinkedList m_queue;
    private BundleContext m_context;
    private EventAdmin m_service;

    public EventAdminTracker( BundleContext context )
    {
        super( context, EventAdmin.class.getName(), null );
        m_context = context;
        m_thread = new Thread( this );
        m_thread.start();
        m_queue = new LinkedList();
    }

    public void postEvent( Event event )
    {
        synchronized( m_queue )
        {
            m_queue.add( event );
            m_queue.notifyAll();
        }
    }

    public void sendEvent( Event event )
    {
        synchronized( m_queue )
        {
            m_queue.add( event );
            m_queue.notifyAll();
        }
    }

    public Object addingService( ServiceReference serviceReference )
    {
        m_service = (EventAdmin) m_context.getService( serviceReference );
        synchronized( this )
        {
            notifyAll();
            return m_service;
        }
    }

    public void removedService( ServiceReference serviceReference, Object object )
    {
        synchronized( this )
        {
            m_service = null;
            notifyAll();
        }
    }

    public void run()
    {
        try
        {
            //noinspection InfiniteLoopStatement
            while( true )
            {
                while( m_service == null )
                {
                    synchronized( this )
                    {
                        wait();
                    }
                }

                EventAdmin service;
                Event event;
                synchronized( m_queue )
                {
                    m_queue.wait();
                    service = m_service;
                    event = (Event) m_queue.remove( 0 );
                }
                service.postEvent( event );
            }
        } catch( InterruptedException e )
        {
            // Expected to terminate the thread.
        }
    }

    public synchronized void close()
    {
        m_thread.interrupt();
        super.close();
    }
}
