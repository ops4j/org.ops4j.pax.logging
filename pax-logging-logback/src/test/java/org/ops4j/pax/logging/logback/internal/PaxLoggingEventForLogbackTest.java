package org.ops4j.pax.logging.logback.internal;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author cdolan
 * @since 5/2/12 1:55 PM
 */
public class PaxLoggingEventForLogbackTest {
    @Test
    public void test() {
        long before = System.currentTimeMillis();

        LoggerContext context = new LoggerContext();
        Logger logger = context.getLogger("mylogger");
        LoggingEvent levent = new LoggingEvent("myfqcn", logger, Level.INFO, "foo {} baz", null, new Object[] {"bar"});

        PaxLoggingEventForLogback event = new PaxLoggingEventForLogback(levent);
        Assert.assertEquals(new PaxLevelForLogback(Level.INFO), event.getLevel());
        Assert.assertEquals("foo bar baz", event.getRenderedMessage());
        Assert.assertEquals("foo {} baz", event.getMessage());
        Assert.assertEquals("mylogger", event.getLoggerName());
        Assert.assertEquals(Logger.class.getName(), event.getFQNOfLoggerClass());
        Assert.assertEquals(Thread.currentThread().getName(), event.getThreadName());
        Assert.assertFalse(event.locationInformationExists());
        Assert.assertTrue(before <= event.getTimeStamp());
        Assert.assertTrue(System.currentTimeMillis() >= event.getTimeStamp());
    }
}
