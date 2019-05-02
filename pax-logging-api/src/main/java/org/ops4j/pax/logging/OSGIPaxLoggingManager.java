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
package org.ops4j.pax.logging;

import java.util.HashMap;
import java.util.Map;

import org.ops4j.pax.logging.internal.BundleHelper;
import org.ops4j.pax.logging.internal.TrackingLogger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * <p>{@link PaxLoggingManager} that acts as a singleton that delegates to tracked instance of {@link PaxLoggingService}</p>
 * <p>More precisely - it returns instances of {@link PaxLogger} that internally delegate to loggers obtained from
 * available {@link PaxLoggingService} or from fallback service when there's no implementation available.</p>
 */
public class OSGIPaxLoggingManager
        implements PaxLoggingManager, ServiceTrackerCustomizer<PaxLoggingService, PaxLoggingService> {

    private ServiceTracker<PaxLoggingService, PaxLoggingService> tracker;

    private PaxLoggingService m_logService;
    private ServiceReference<PaxLoggingService> m_logServiceRef;

    /**
     * Mapping between logger name and {@link TrackingLogger}. This map is shared between all logging facades.
     */
    private final Map<String, TrackingLogger> m_loggers;

    private BundleContext m_context;

    public OSGIPaxLoggingManager(BundleContext context) {
        tracker = new ServiceTracker<>(context, PaxLoggingService.class.getName(), this);
        tracker.open();

        m_loggers = new HashMap<String, TrackingLogger>();
        m_context = context;

        // retrieve the service if any exist at this point.
        ServiceReference<PaxLoggingService> ref = tracker.getServiceReference();
        if (ref != null) {
            m_logService = context.getService(ref);
        }
    }

    @Override
    public PaxLogger getLogger(String category, String fqcn) {
        Bundle bundle = BundleHelper.getCallerBundle(m_context.getBundle());
        return getLogger(bundle, category, fqcn);
    }

    @Override
    public PaxLogger getLogger(Bundle bundle, String category, String fqcn) {
        if (fqcn == null) {
            fqcn = PaxLogger.class.getName();
        }

        String key = fqcn + "#" + category + "#" + (bundle != null ? Long.toString(bundle.getBundleId()) : "0");
        synchronized (m_loggers) {
            TrackingLogger logger = m_loggers.get(key);
            if (logger == null) {
                logger = new TrackingLogger(m_logService, category, bundle, fqcn);
                m_loggers.put(key, logger);
            }
            return logger;
        }
    }

    @Override
    public PaxLoggingService getPaxLoggingService() {
        return m_logService;
    }

    @Override
    public void close() {
        tracker.close();
    }

    @Override
    public void dispose() {
        if (m_logServiceRef != null) {
            m_context.ungetService(m_logServiceRef);
            m_logServiceRef = null;
        }

        synchronized (m_loggers) {
            for (TrackingLogger logger : m_loggers.values()) {
                logger.removed();
            }
            m_loggers.clear();
        }

        m_context = null;
    }

    @Override
    public Bundle getBundle() {
        return m_context.getBundle();
    }

    @Override
    public PaxLoggingService addingService(ServiceReference<PaxLoggingService> reference) {
        m_logServiceRef = reference;
        m_logService = m_context.getService(m_logServiceRef);

        synchronized (m_loggers) {
            for (TrackingLogger logger : m_loggers.values()) {
                logger.added(m_logService);
            }
        }
        return m_logService;
    }

    @Override
    public void modifiedService(ServiceReference<PaxLoggingService> reference, PaxLoggingService service) {
    }

    @Override
    public void removedService(ServiceReference<PaxLoggingService> reference, PaxLoggingService service) {
        m_logService = null;
        if (m_logServiceRef != null) {
            m_context.ungetService(m_logServiceRef);
            m_logServiceRef = null;
        }

        synchronized (m_loggers) {
            for (TrackingLogger logger : m_loggers.values()) {
                logger.removed();
            }
        }
    }

}
