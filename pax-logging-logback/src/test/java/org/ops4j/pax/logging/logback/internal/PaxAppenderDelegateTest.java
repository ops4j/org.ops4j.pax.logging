package org.ops4j.pax.logging.logback.internal;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEventVO;
import ch.qos.logback.core.Context;
import org.ops4j.pax.logging.logback.appender.PaxAppenderDelegate;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.eclipse.osgi.framework.internal.core.FilterImpl;
import org.junit.Test;
import org.ops4j.pax.logging.spi.PaxAppender;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import static org.easymock.EasyMock.same;

/**
 * @author Chris Dolan
 * @since 6/14/11 10:59 AM
 */
public class PaxAppenderDelegateTest {

    @Test
    public void test() throws InvalidSyntaxException {
        String filterStr = "(&(objectClass=org.ops4j.pax.logging.spi.PaxAppender)(org.ops4j.pax.logging.appender.name=foo-pax-name))";

        ILoggingEvent evt = new LoggingEventVO();

        PaxAppender appender = EasyMock.createStrictMock(PaxAppender.class);
        appender.doAppend(new PaxLoggingEventForLogback(evt));
        EasyMock.expectLastCall().once();

        ServiceReference sr = EasyMock.createStrictMock(ServiceReference.class);

        BundleContext bundlecontext = EasyMock.createStrictMock(BundleContext.class);
        EasyMock.expect(bundlecontext.createFilter(filterStr)).andAnswer(new IAnswer<Filter>() {
            public Filter answer() throws Throwable {
                return FilterImpl.newInstance((String) EasyMock.getCurrentArguments()[0]);
            }
        }).once();
        EasyMock.expect(bundlecontext.getProperty(Constants.FRAMEWORK_VERSION)).andReturn("0").times(0,1); // it seems that different OSGi versions call this differently
        bundlecontext.addServiceListener(EasyMock.<ServiceListener>anyObject(), EasyMock.<String>isNull());
        EasyMock.expectLastCall().once();
        EasyMock.expect(bundlecontext.getServiceReferences((String) null, filterStr)).andReturn(new ServiceReference[]{sr}).once();
        EasyMock.expect(bundlecontext.getService(same(sr))).andReturn(appender).once();
        bundlecontext.removeServiceListener(EasyMock.<ServiceListener>anyObject());
        EasyMock.expectLastCall().once();
        EasyMock.expect(bundlecontext.ungetService(same(sr))).andReturn(true).once();

        Context context = EasyMock.createStrictMock(Context.class);
        EasyMock.expect(context.getObject(PaxLoggingServiceImpl.LOGGER_CONTEXT_BUNDLECONTEXT_KEY)).andReturn(bundlecontext).once();

        EasyMock.replay(context, bundlecontext, sr, appender);

        PaxAppenderDelegate delegate = new PaxAppenderDelegate();
        delegate.setContext(context);
        delegate.setPaxname("foo-pax-name");
        delegate.start();
        delegate.start(); // second start is a no-op
        try {
            delegate.doAppend(evt);
        } finally {
            delegate.stop();
            delegate.stop(); // second stop is a no-op
        }

        EasyMock.verify(context, bundlecontext, sr, appender);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testPropertyPaxnameNull() {
    	new PaxAppenderDelegate().setPaxname(null);
    }
}
