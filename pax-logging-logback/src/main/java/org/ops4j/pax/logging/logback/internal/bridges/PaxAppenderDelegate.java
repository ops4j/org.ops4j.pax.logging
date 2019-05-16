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
package org.ops4j.pax.logging.logback.internal.bridges;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import org.ops4j.pax.logging.logback.internal.spi.PaxLoggingEventForLogback;
import org.ops4j.pax.logging.spi.support.PaxAppenderProxy;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/**
 * <p>
 * This is a Logback appender that forwards log messages to any services registered with OSGi with the interface
 * org.ops4j.pax.logging.spi.PaxAppender. That list of appender services is possibly filtered by the paxname setting.
 * </p>
 * <p>
 * For example, you can use it like this in your logback.xml file:
 * <pre>
 *   &lt;appender name="OSGI" class="org.ops4j.pax.logging.logback.appender.PaxAppenderDelegate"&gt;
 *       &lt;paxname&gt;*&lt;/paxname&gt;
 *   &lt;/appender&gt;
 * </pre>
 * The paxname argument is a filter for the bundle property "org.ops4j.pax.logging.appender.name". In this example,
 * the "*" means to accept all delegatees.
 * </p>
 * <p>
 * This class is inspired by PaxLoggingConfigurator.parseAppender(), PaxAppenderProxy and AppenderBridgeImpl in the
 * log4j implementation.
 * </p>
 * 
 * @author Chris Dolan
 * @since 6/14/11 9:32 AM
 */
public class PaxAppenderDelegate extends UnsynchronizedAppenderBase<ILoggingEvent> {
    private final Object lifeCycleLock = new Object();
    private PaxAppenderProxy proxy;
    private String paxname = "*";

    /**
     * Allows filtering on OSGi services on the bundle property "org.ops4j.pax.logging.appender.name"
     * @param paxname defaults to "*", can be any string that works as a value in {@link org.osgi.framework.Filter}.
     */
    public void setPaxname(String paxname) {
        if (null == paxname)
            throw new IllegalArgumentException("paxname cannot be null");
        this.paxname = paxname;
    }

    @Override
    public void start() {
        synchronized (lifeCycleLock) {
            if (isStarted())
                return;
            BundleContext bundleContext = (BundleContext) getContext().getObject("org.ops4j.pax.logging.logback.bundlecontext");
            if (bundleContext == null) {
                Bundle bundle = FrameworkUtil.getBundle(getClass());
                if (bundle != null) {
                    bundleContext = bundle.getBundleContext();
                }
                if (bundleContext == null) {
                    throw new IllegalArgumentException("missing BundleContext, expected in org.ops4j.pax.logging.logback.bundlecontext");
                }
            }

            super.start();
            proxy = new PaxAppenderProxy(bundleContext, paxname);
            proxy.open();
        }
    }

    @Override
    public void stop() {
        synchronized (lifeCycleLock) {
            if (!isStarted())
                return;
            if (proxy != null) {
                proxy.close();
                proxy = null;
            }
            super.stop();
        }
    }

    @Override
    protected void append(ILoggingEvent event) {
        PaxAppenderProxy p = proxy;
        if (p != null) {
            p.doAppend(new PaxLoggingEventForLogback(event));
        }
    }
}
