package org.ops4j.pax.logging.logback.internal;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.eclipse.osgi.framework.util.Headers;
import org.junit.Assert;
import org.junit.Test;
import org.ops4j.pax.logging.PaxContext;
import org.ops4j.pax.logging.PaxLoggingService;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.slf4j.MDC;

import java.util.Arrays;
import java.util.Map;

/**
 * @author Chris Dolan
 * @since 6/13/11 11:13 AM
 */
public class PaxLoggerImplTest {
    private interface LogAppender extends Appender<ILoggingEvent> {}

    @Test
    public void test() {
        String fqcn = "blarg";
        LoggerContext context = new LoggerContext();
        Logger logger = context.getLogger("foo");

        Bundle bundle = makeBundle();
        PaxLoggingService svc = EasyMock.createStrictMock(PaxLoggingService.class);
        PaxEventHandler eventHandler = EasyMock.createNiceMock(PaxEventHandler.class);
        EasyMock.expect(svc.getPaxContext()).andReturn(new PaxContext()).anyTimes();

        Appender<ILoggingEvent> appender = EasyMock.createStrictMock(LogAppender.class);
        MDC.put("bundle.name", "bundle1");
        MDC.put("bundle.id", "1");
        MDC.put("bundle.version", "1.2.3.4");
        appender.doAppend(eqLogEvent(fqcn, logger, Level.DEBUG, "d"));  EasyMock.expectLastCall();
        appender.doAppend(eqLogEvent(fqcn, logger, Level.INFO,  "i"));  EasyMock.expectLastCall();
        appender.doAppend(eqLogEvent(fqcn, logger, Level.WARN,  "w"));  EasyMock.expectLastCall();
        appender.doAppend(eqLogEvent(fqcn, logger, Level.ERROR, "e"));  EasyMock.expectLastCall();
        MDC.clear();

        EasyMock.replay(bundle, svc, eventHandler, appender);

        logger.addAppender(appender);
        PaxLoggerImpl paxLogger = new PaxLoggerImpl(bundle, logger, fqcn, svc, eventHandler);
        paxLogger.trace("t", null); // won't be logged, default level is DEBUG
        paxLogger.debug("d", null);
        paxLogger.inform("i", null);
        paxLogger.warn("w", null);
        paxLogger.error("e", null);

        EasyMock.verify(bundle, svc, eventHandler, appender);
    }

    /**
     * The test() method was unexpectedly passing, so I made this additional test. I had a tough time getting the code
     * right in the LoggingEventMatcher class...
     */
    @Test
    public void testTestCode() {
        LoggerContext context = new LoggerContext();
        Logger logger = context.getLogger("lg");
        LoggingEvent e1 = new LoggingEvent("foo", logger, Level.DEBUG, "bar", null, null); // MDC is null
        Assert.assertTrue(e1.toString(), new LoggingEventMatcher(e1).matches(e1));
        Assert.assertTrue(e1.toString(), new LoggingEventMatcher(e1).matches(new LoggingEvent("foo", logger, Level.DEBUG, "bar", null, null)));
        Assert.assertFalse(e1.toString(), new LoggingEventMatcher(e1).matches(new LoggingEvent("foo", logger, Level.INFO, "bar", null, null)));
        Assert.assertFalse(e1.toString(), new LoggingEventMatcher(e1).matches(new LoggingEvent("foo", logger, Level.DEBUG, "bar2", null, null)));
        Assert.assertFalse(e1.toString(), new LoggingEventMatcher(e1).matches(new LoggingEvent("foo", logger, Level.DEBUG, "bar", new Throwable(), null)));
        Assert.assertFalse(e1.toString(), new LoggingEventMatcher(e1).matches(new LoggingEvent("foo", logger, Level.DEBUG, "bar", null, new Object[] {"xx"})));
        MDC.put("arg", "blarg");
        Assert.assertFalse(e1.toString(), new LoggingEventMatcher(e1).matches(new LoggingEvent("foo", logger, Level.DEBUG, "bar", null, null)));
        MDC.clear();
    }

    private Bundle makeBundle() {
        Headers headers = new Headers(1);
        headers.put(Constants.BUNDLE_VERSION, "1.2.3.4");

        Bundle bundle = EasyMock.createMock(Bundle.class);
        EasyMock.expect(bundle.getBundleId()).andReturn(1L).anyTimes();
        EasyMock.expect(bundle.getSymbolicName()).andReturn("bundle1").anyTimes();
        EasyMock.expect(bundle.getHeaders()).andReturn(headers).anyTimes();
        return bundle;
    }

    private LoggingEvent eqLogEvent(String fqcn, Logger logger, Level level, String msg) {
        return eqLogEvent(new LoggingEvent(fqcn, logger, level, msg, null, null));
    }

    private LoggingEvent eqLogEvent(final LoggingEvent le) {
        EasyMock.reportMatcher(new LoggingEventMatcher(le));
        return null;
    }

    private static class LoggingEventMatcher implements IArgumentMatcher {
        private final LoggingEvent le;

        public LoggingEventMatcher(LoggingEvent le) {
            le.prepareForDeferredProcessing();
            this.le = le;
        }

        public boolean matches(Object o) {
            if (!(o instanceof LoggingEvent))
                return false;
            LoggingEvent that = (LoggingEvent) o;
            if (le.getLevel() != that.getLevel())
                return false;
            if (!le.getFormattedMessage().equals(that.getFormattedMessage()))
                return false;
            if (le.getMarker() != that.getMarker())
                return false;
            if (!le.getLoggerName().equals(that.getLoggerName()))
                return false;
            IThrowableProxy thrown = le.getThrowableProxy();
            IThrowableProxy thatThrown = that.getThrowableProxy();
            if (thrown == null ? thatThrown != null : thatThrown == null)
                return false;
            if (thrown != null) {
                if (!thrown.getMessage().equals(thatThrown.getMessage()))
                    return false;
                if (!thrown.getClassName().equals(thatThrown.getClassName()))
                    return false;
            }
            if (!Arrays.equals(le.getArgumentArray(), that.getArgumentArray()))
                return false;
            Map<String,String> mdc = le.getMDCPropertyMap();
            Map<String,String> thatMDC = that.getMDCPropertyMap();
            if (mdc == null ? thatMDC != null : !mdc.equals(thatMDC))
                return false;
            return true;
        }

        public void appendTo(StringBuffer buffer) {
            // not implemented
        }
    }
}
