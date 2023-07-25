/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ops4j.pax.logging.logback.internal;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.ops4j.pax.logging.PaxContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.osgi.service.log.LogLevel;
import org.slf4j.MDC;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Chris Dolan
 * @since 6/13/11 11:13 AM
 */
public class PaxLoggerImplTest {

    @Test
    public void test() {
        String fqcn = "blarg";
        LoggerContext context = new LoggerContext();
        context.setMDCAdapter(new Slf4jLogbackMDCAdapter());
        Logger logger = context.getLogger("foo");
        PaxContext paxContext = new PaxContext();

        Bundle bundle = makeBundle();
        PaxLoggingServiceImpl svc = mock(PaxLoggingServiceImpl.class);

        when(svc.getPaxContext()).thenReturn(paxContext);

        Appender<ILoggingEvent> appender = mock(LogAppender.class);
        MDC.put("bundle.name", "bundle1");
        MDC.put("bundle.id", "1");
        MDC.put("bundle.version", "1.2.3.4");

        logger.addAppender(appender);
        PaxLoggerImpl paxLogger = new PaxLoggerImpl(bundle, logger, fqcn, svc, false);

        Assert.assertEquals(LogLevel.DEBUG, paxLogger.getLogLevel());
        Assert.assertEquals("foo", paxLogger.getName());
        Assert.assertSame(paxContext, paxLogger.getPaxContext());

        paxLogger.trace("t"); // won't be logged, default level is DEBUG
        paxLogger.debug("d");
        paxLogger.info("i");
        paxLogger.warn("w");
        paxLogger.error("e");
        paxLogger.fatal("f");

        Assert.assertFalse(paxLogger.isTraceEnabled());
        Assert.assertTrue(paxLogger.isDebugEnabled());
        Assert.assertTrue(paxLogger.isInfoEnabled());
        Assert.assertTrue(paxLogger.isWarnEnabled());
        Assert.assertTrue(paxLogger.isErrorEnabled());
        Assert.assertTrue(paxLogger.isFatalEnabled());

        verify(appender).doAppend(eqLogEvent(logger, Level.DEBUG, "d"));
        verify(appender).doAppend(eqLogEvent(logger, Level.INFO, "i"));
        verify(appender).doAppend(eqLogEvent(logger, Level.WARN, "w"));
        verify(appender).doAppend(eqLogEvent(logger, Level.ERROR, "e"));
        verify(appender).doAppend(eqLogEvent(logger, Level.ERROR, "f"));
    }

    /**
     * The test() method was unexpectedly passing, so I made this additional test. I had a tough time getting the code
     * right in the LoggingEventMatcher class...
     */
    @Test
    public void testTestCode() {
        LoggerContext context = new LoggerContext();
        context.setMDCAdapter(new Slf4jLogbackMDCAdapter());
        Logger logger = context.getLogger("lg");
        LoggingEvent e1 = new LoggingEvent("foo", logger, Level.DEBUG, "bar", null, null); // MDC is null
        Assert.assertTrue(e1.toString(), new LoggingEventMatcher(e1).matches(e1));
        Assert.assertTrue(e1.toString(), new LoggingEventMatcher(e1).matches(new LoggingEvent("foo", logger, Level.DEBUG, "bar", null, null)));
        Assert.assertFalse(e1.toString(), new LoggingEventMatcher(e1).matches(new LoggingEvent("foo", logger, Level.INFO, "bar", null, null)));
        Assert.assertFalse(e1.toString(), new LoggingEventMatcher(e1).matches(new LoggingEvent("foo", logger, Level.DEBUG, "bar2", null, null)));
        Assert.assertFalse(e1.toString(), new LoggingEventMatcher(e1).matches(new LoggingEvent("foo", logger, Level.DEBUG, "bar", new Throwable(), null)));
        Assert.assertFalse(e1.toString(), new LoggingEventMatcher(e1).matches(new LoggingEvent("foo", logger, Level.DEBUG, "bar", null, new Object[] { "xx" })));
        MDC.put("arg", "blarg");
        Assert.assertFalse(e1.toString(), new LoggingEventMatcher(e1).matches(new LoggingEvent("foo", logger, Level.DEBUG, "bar", null, null)));
        MDC.clear();
    }

    private Bundle makeBundle() {
        Bundle bundle = mock(Bundle.class);
        when(bundle.getBundleId()).thenReturn(1L);
        when(bundle.getSymbolicName()).thenReturn("bundle1");
        when(bundle.getVersion()).thenReturn(new Version("1.2.3.4"));
        return bundle;
    }

    private LoggingEvent eqLogEvent(Logger logger, Level level, String msg) {
        return eqLogEvent(new LoggingEvent(null, logger, level, msg, null, new Object[0]));
    }

    private LoggingEvent eqLogEvent(final LoggingEvent le) {
        return ArgumentMatchers.argThat(argument -> new LoggingEventMatcher(argument).matches(le));
    }

    private interface LogAppender extends Appender<ILoggingEvent> {
    }

    private static class LoggingEventMatcher {

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
            Map<String, String> mdc = le.getMDCPropertyMap();
            Map<String, String> thatMDC = that.getMDCPropertyMap();
            if (!Objects.equals(mdc, thatMDC))
                return false;
            return true;
        }

        public void appendTo(StringBuffer buffer) {
            // not implemented
        }
    }

    public static Object getField(Object object, String fieldName) {
        try {
            Field f = object.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.get(object);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
