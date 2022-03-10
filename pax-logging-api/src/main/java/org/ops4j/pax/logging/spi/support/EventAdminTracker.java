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
package org.ops4j.pax.logging.spi.support;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;

import org.ops4j.pax.logging.EventAdminPoster;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogLevel;
import org.osgi.util.tracker.ServiceTracker;

/**
 * This ServiceTracker is designed to implement the Event Admin service, but delegate
 * to the actual service if available.
 *
 * If the Event Admin service is not available, this tracker will queue the Events until
 * the service becomes available.
 */
public class EventAdminTracker extends ServiceTracker<EventAdmin, EventAdmin>
        implements EventAdminPoster {

    private final LinkedList<Event> m_queue;
    private BundleContext m_context;
    private EventAdmin m_service;

    private int m_maxSize;

    public EventAdminTracker(BundleContext context) {
        super(context, EventAdmin.class, null);
        m_context = context;
        m_queue = new LinkedList<Event>();
        m_maxSize = 50;
        open();
    }

    public void postEvent(Bundle bundle, LogLevel level, LogEntry entry, String message,
                          Throwable exception, ServiceReference<?> sr, Map<String, ?> context) {
        Event event = createEvent(bundle, level, entry, message, exception, sr, context);
        synchronized (m_queue) {
            m_queue.add(event);
        }
        deliver();
        cleanup();
    }

    public void destroy() {
        close();
    }

    public EventAdmin addingService(ServiceReference<EventAdmin> serviceReference) {
        m_service = m_context.getService(serviceReference);
        deliver();
        return m_service;
    }

    public void removedService(ServiceReference<EventAdmin> serviceReference, EventAdmin object) {
        m_service = null;
    }

    private void deliver() {
        EventAdmin forDelivery = m_service;
        if (forDelivery == null) {
            return;
        }
        while (m_queue.size() > 0) // Peter Doornbosch: Still not ok: this must be volatile or sync'd too!
        // Niclas: volatile is meaningless semantics on a final reference.
        //         I can't see while synchronized would be needed, since sync have
        //         just happened, and if size is slightly incorrectly computed, nothing
        //         will be harmed by it.
        {
            Event event = null;
            synchronized (m_queue) {
                // Make sure queue is still not empty (due to race conditions)
                if (m_queue.size() > 0) {
                    event = m_queue.remove(0);
                }
            }
            if (event != null) {
                try {
                    forDelivery.postEvent(event);
                } catch (IllegalStateException e) {
                    synchronized (m_queue) {
                        m_queue.add(event);
                    }
                }
            }
        }
    }

    public int getMaxSize() {
        return m_maxSize;
    }

    public void setMaxSize(int maxSize) {

        if (maxSize < 0) {
            throw new IllegalArgumentException("MaxSize must not be a negative number: " + maxSize);
        }
        if (maxSize > 1000) {
            String message = "MaxSize must not be a larger than 1000 for memory constraint reasons: " + maxSize;
            throw new IllegalArgumentException(message);

        }
        m_maxSize = maxSize;
    }

    private void cleanup() {
        while (m_queue.size() > m_maxSize) {
            synchronized (m_queue) {
                m_queue.remove(0);
            }
        }
    }

    static Event createEvent(Bundle bundle, LogLevel level, LogEntry entry, String message,
                             Throwable exception, ServiceReference<?> sr, Map<String, ?> context) {
        String type;
        switch (level) {
            case AUDIT:
                type = "LOG_AUDIT";
                break;
            case ERROR:
                type = "LOG_ERROR";
                break;
            case WARN:
                type = "LOG_WARNING";
                break;
            case INFO:
                type = "LOG_INFO";
                break;
            case DEBUG:
                type = "LOG_DEBUG";
                break;
            case TRACE:
            default:
                type = "LOG_OTHER";
        }
        String topic = "org/osgi/service/log/LogEntry/" + type;
        Dictionary<String, Object> props = new Hashtable<>();
        if (bundle != null) {
            props.put("bundle", bundle);
            Long bundleId = bundle.getBundleId();
            props.put("bundle.id", bundleId);
            String symbolicName = bundle.getSymbolicName();
            if (symbolicName != null) {
                props.put("bundle.symbolicname", symbolicName);
            }
        }
        props.put("log.level", level);
        props.put("log.entry", entry);
        if (null != message) {
            props.put("message", message);
        }
        props.put("timestamp", System.currentTimeMillis());
        if (exception != null) {
            props.put("exception", exception);
            props.put("exception.class", exception.getClass());
            // Only save message if message is not null otherwise NPE is thrown
            if (exception.getMessage() != null) {
                props.put("exception.message", exception.getMessage());
            }
        }
        if (sr != null) {
            props.put("service", sr);
            Long id = (Long) sr.getProperty(Constants.SERVICE_ID);
            props.put("service.id", id);
            String pid = (String) sr.getProperty(Constants.SERVICE_PID);
            if (pid != null) {
                props.put("service.pid", pid);
            }
            String[] objClass = (String[]) sr.getProperty(Constants.OBJECTCLASS);
            props.put("service.objectClass", objClass);
        }
        if (context != null) {
            for (String key : context.keySet()) {
                Object value = context.get(key);
                if (value != null) {
                    props.put(key, value);
                }
            }
        }
        return new Event(topic, props);
    }

}
