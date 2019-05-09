/*  Copyright 2012 Guillaume Nodet.
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
package org.ops4j.pax.logging.service.internal.bridges;

import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.varia.DenyAllFilter;
import org.ops4j.pax.logging.PaxLoggingConstants;
import org.ops4j.pax.logging.service.internal.spi.PaxLoggingEventImpl;
import org.ops4j.pax.logging.spi.PaxFilter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Bridge from Log4J to pax-logging. Event is filtered using {@link PaxFilter} OSGi service.
 * Internal tracker is closed when pax-logging-service bundle stops.
 */
public class FilterBridgeImpl extends Filter {

    private ServiceTracker<PaxFilter, PaxFilter> m_tracker;
    private Filter m_fallback;

    public FilterBridgeImpl(BundleContext bundleContext, String name, Filter fallback) {
        m_tracker = new ServiceTracker<>(bundleContext, createFilter(bundleContext, name), null);
        m_tracker.open();
        m_fallback = fallback != null ? fallback : new DenyAllFilter();
    }

    /**
     * Filter in the form of {@code (&(objectClass=org.ops4j.pax.logging.spi.PaxFilter)(org.ops4j.pax.logging.filter.name=NAME))},
     * where {@code NAME} comes from {@link org.apache.log4j.PaxLoggingConfigurator#OSGI_PREFIX osgi:} prefixed
     * references from logging configuration.
     * @param bundleContext
     * @param name
     * @return
     */
    public static org.osgi.framework.Filter createFilter(BundleContext bundleContext, String name) {
        try {
            return bundleContext.createFilter(
                    "(&(" + Constants.OBJECTCLASS + "=" + PaxFilter.class.getName() + ")" +
                            "(" + PaxLoggingConstants.FILTER_NAME_PROPERTY + "=" + name + "))");
        } catch (InvalidSyntaxException e) {
            throw new IllegalStateException("unable to create filter tracker", e);
        }
    }

    @Override
    public int decide(LoggingEvent event) {
        PaxFilter filter = m_tracker.getService();
        if (filter != null) {
            return filter.doFilter(new PaxLoggingEventImpl(event));
        }
        return m_fallback.decide(event);
    }

}
