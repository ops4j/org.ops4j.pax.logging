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

import org.apache.log4j.Layout;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.ops4j.pax.logging.PaxLoggingConstants;
import org.ops4j.pax.logging.service.internal.spi.PaxLoggingEventImpl;
import org.ops4j.pax.logging.spi.PaxLayout;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Bridge from Log4J to pax-logging. Event is formatted using {@link PaxLayout} OSGi service.
 * Internal tracker is closed when pax-logging-service bundle stops.
 */
public class LayoutBridgeImpl extends Layout {

    private ServiceTracker<PaxLayout, PaxLayout> m_tracker;
    private Layout m_fallback;

    public LayoutBridgeImpl(BundleContext bundleContext, String name, Layout fallback) {
        m_tracker = new ServiceTracker<>(bundleContext, createFilter(bundleContext, name), null);
        m_tracker.open();
        m_fallback = fallback != null ? fallback : new SimpleLayout();
    }

    /**
     * Filter in the form of {@code (&(objectClass=org.ops4j.pax.logging.spi.PaxLayout)(org.ops4j.pax.logging.layout.name=NAME))},
     * where {@code NAME} comes from {@link org.apache.log4j.PaxLoggingConfigurator#OSGI_PREFIX osgi:} prefixed
     * references from logging configuration.
     * @param bundleContext
     * @param name
     * @return
     */
    public static Filter createFilter(BundleContext bundleContext, String name) {
        try {
            return bundleContext.createFilter(
                    "(&(" + Constants.OBJECTCLASS + "=" + PaxLayout.class.getName() + ")" +
                            "(" + PaxLoggingConstants.SERVICE_PROPERTY_LAYOUT_NAME_PROPERTY + "=" + name + "))");
        } catch (InvalidSyntaxException e) {
            throw new IllegalStateException("unable to create layout tracker", e);
        }
    }

    @Override
    public String format(LoggingEvent event) {
        PaxLayout layout = m_tracker.getService();
        if (layout != null) {
            return layout.doLayout(new PaxLoggingEventImpl(event));
        }
        return m_fallback.format(event);
    }

    @Override
    public boolean ignoresThrowable() {
        return true;
    }

    @Override
    public void activateOptions() {
    }

    @Override
    public String getContentType() {
        PaxLayout layout = m_tracker.getService();
        if (layout != null) {
            String contentType = layout.getContentType();
            if (contentType != null) {
                return contentType;
            }
        }
        return m_fallback.getContentType();
    }

    @Override
    public String getHeader() {
        PaxLayout layout = m_tracker.getService();
        if (layout != null) {
            return layout.getHeader();
        }
        return m_fallback.getHeader();
    }

    @Override
    public String getFooter() {
        PaxLayout layout = m_tracker.getService();
        if (layout != null) {
            return layout.getFooter();
        }
        return m_fallback.getFooter();
    }
}
