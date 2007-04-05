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
import java.util.Iterator;
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
 *
 * <b>NOTE:</b> This is work in progress and not working yet.
 */
public class PublishAppender extends AppenderSkeleton
    implements Appender
{
    private final ArrayList m_buffer;
    private EventAdmin m_service;

    public PublishAppender( EventAdmin service )
    {
        m_service = service;
        m_buffer = new ArrayList();
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
        props.put( EventConstants.TIMESTAMP, new Long(loggingEvent.timeStamp) );
        Event event = new Event( "org/ops4j/pax/logging", props );
        synchronized( m_buffer )
        {
            m_buffer.add( event );
        }
        fireEvents();
    }

    public boolean requiresLayout()
    {
        return false;
    }

    public void close()
    {
    }

    private void fireEvents()
    {
        ArrayList clone;
        synchronized( m_buffer )
        {
            clone = new ArrayList();
            clone.addAll( m_buffer );
            m_buffer.clear();
        }
        Iterator list = clone.iterator();
        while( list.hasNext() )
        {
            Event event = (Event) list.next();
            m_service.postEvent( event );
        }
    }
}
