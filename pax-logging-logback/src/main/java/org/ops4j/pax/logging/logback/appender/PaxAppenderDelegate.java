package org.ops4j.pax.logging.logback.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import org.ops4j.pax.logging.logback.internal.PaxAppenderProxy;
import org.ops4j.pax.logging.logback.internal.PaxLoggingEventForLogback;
import org.osgi.framework.BundleContext;

/**
 * <p>
 * Forwards log messages to any services registered with OSGi with the interface org.ops4j.pax.logging.spi.PaxAppender.
 * That list of appender services is possibly filtered by the paxname setting.
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
            throw new NullPointerException("paxname cannot be null");
        this.paxname = paxname;
    }

    @Override
    public void start() {
        synchronized (lifeCycleLock) {
            if (isStarted())
                return;
            BundleContext bundleContext = (BundleContext) getContext().getObject("org.ops4j.pax.logging.logback.bundlecontext");
            if (bundleContext == null)
                throw new NullPointerException("missing BundleContext, expected in org.ops4j.pax.logging.logback.bundlecontext");
            super.start();
            proxy = new PaxAppenderProxy(bundleContext, paxname);
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
