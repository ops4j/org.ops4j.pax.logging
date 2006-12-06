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
package org.ops4j.pax.logging.appenders.publish;

import java.util.ArrayList;
import java.util.Hashtable;
import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.util.tracker.ServiceTracker;

/**
 * This appender publishes the Log events as OSGi events according to the
 * OSGi Event Admin specification (ch 113 in Compendium).
 */
public class PublishAppender extends AppenderSkeleton
    implements Appender
{
    private BundleContext m_context;
    private ServiceTracker m_tracker;
    private ArrayList<Event> m_buffer;

    public PublishAppender( BundleContext context )
    {
        m_buffer = new ArrayList<Event>();
        m_context = context;
        m_tracker = new ServiceTracker( m_context, EventAdmin.class.getName(), null );
        m_tracker.open();
    }

    protected void append( LoggingEvent loggingEvent )
    {
        Hashtable props = new Hashtable();
        ThrowableInformation information = loggingEvent.getThrowableInformation();
        Throwable throwable = information.getThrowable();
        props.put( EventConstants.EXCEPTION, throwable );
        props.put( EventConstants.EXCEPTION_MESSAGE, loggingEvent.getMessage() );
        props.put( EventConstants.EXECPTION_CLASS, throwable.getClass() );
        props.put( "category.name", loggingEvent.fqnOfCategoryClass);
        props.put( "thread.name", loggingEvent.getThreadName() );
        props.put( EventConstants.TIMESTAMP, loggingEvent.timeStamp );
        Event event = new Event( "org/ops4j/pax/logging", props );
        synchronized( m_buffer )
        {
            m_buffer.add( event );
        }
        fireEvents();
    }

    private void fireEvents()
    {
        ServiceReference ref = m_tracker.getServiceReference();
        EventAdmin admin = (EventAdmin) m_context.getService( ref );
        ArrayList<Event> clone = null;
        synchronized( m_buffer )
        {
            clone = new ArrayList();
            clone.addAll( m_buffer );
            m_buffer.clear();
        }
        for( Event event : clone )
        {
            admin.postEvent( event );
        }
        m_context.ungetService( ref );
    }

    public boolean requiresLayout()
    {
        return false;
    }

    public void close()
    {
        m_tracker.close();
    }
}
