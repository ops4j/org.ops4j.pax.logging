/*
 * Copyright 2011 Avid Technology, Inc.
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
package org.ops4j.pax.logging.log4j2.appender;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.status.StatusLogger;
import org.ops4j.pax.logging.log4j2.internal.PaxAppenderProxy;
import org.ops4j.pax.logging.log4j2.internal.PaxLoggingEventImpl;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/**
 * <p>
 * This is a Logback appender that forwards log messages to any services registered with OSGi with the interface
 * org.ops4j.pax.logging.spi.PaxAppender. That list of appender services is possibly filtered by the filter setting.
 * </p>
 */
@Plugin(name = "PaxOsgi", category = "Core", elementType = "appender", printObject = true)
public class PaxOsgiAppender extends AbstractAppender {

    private final String filter;
    private PaxAppenderProxy proxy;

    public PaxOsgiAppender(String name, String filter) {
        super(name, null, null);
        this.filter = (filter == null || filter.isEmpty()) ? "*" : filter;
    }

    @Override
    public void start() {
        // TODO: use correct bundle context
        BundleContext bundleContext = null;
        Bundle bundle = FrameworkUtil.getBundle(getClass());
        if (bundle != null) {
            bundleContext = bundle.getBundleContext();
        }
        if (bundleContext == null) {
            throw new IllegalArgumentException("missing BundleContext, expected in org.ops4j.pax.logging.log4j2.bundlecontext");
        }
        proxy = new PaxAppenderProxy(bundleContext, filter);
        proxy.open();
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        if (proxy != null) {
            proxy.close();
            proxy = null;
        }
    }

    @Override
    public void append(LogEvent event) {
        PaxAppenderProxy p = proxy;
        if (p != null) {
            p.doAppend(new PaxLoggingEventImpl(event));
        }
    }

    /**
     * Create a Pax Osgi Appender.
     * @param name The name of the Appender.
     * @param filter defaults to "*", can be any string that works as a value in {@link org.osgi.framework.Filter}
     * @param config The Configuration
     * @return The FileAppender.
     */
    @PluginFactory
    public static PaxOsgiAppender createAppender(
            // @formatter:off
            @PluginAttribute("name") final String name,
            @PluginAttribute("filter") final String filter,
            @PluginConfiguration final Configuration config) {
            // @formatter:on

        if (name == null) {
            StatusLogger.getLogger().error("No name provided for PaxOsgiAppender");
            return null;
        }
        return new PaxOsgiAppender(name, filter);
    }
}
