package org.ops4j.pax.logging.logback.internal;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.easymock.EasyMock;
import org.junit.Test;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingService;
import org.osgi.framework.Bundle;

import javax.annotation.CheckForNull;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.isA;

/**
 * @author Chris Dolan
 * @since 6/10/11 10:38 AM
 */
public class JdkHandlerTest {
    @Test
    public void test() {
        final Bundle bundle = makeBundle();

        PaxLogger logger = EasyMock.createStrictMock(PaxLogger.class);
        logger.trace("all", null); EasyMock.expectLastCall().once();
        logger.trace("fff", null); EasyMock.expectLastCall().once();
        logger.debug("ff", null);  EasyMock.expectLastCall().once();
        logger.debug("f", null);   EasyMock.expectLastCall().once();
        logger.inform("c", null);  EasyMock.expectLastCall().once(); // pax-logging-log4j skips this, we don't
        logger.inform("i", null);  EasyMock.expectLastCall().once();
        logger.warn("w", null);    EasyMock.expectLastCall().once();
        logger.error("s", null);   EasyMock.expectLastCall().once();
        logger.error("off", null); EasyMock.expectLastCall().once();
        logger.error(eq("s"), isA(Throwable.class));   EasyMock.expectLastCall().once();

        PaxLoggingService logService = EasyMock.createStrictMock(PaxLoggingService.class);
        EasyMock.expect(logService.getLogger(null, null, "java.util.logging.Logger")).andReturn(logger).anyTimes();

        EasyMock.replay(bundle, logger, logService);

        JdkHandler handler = new JdkHandler(logService);
        try {
            handler.publish(mkRecord(Level.ALL, "all", null));
            handler.publish(mkRecord(Level.FINEST, "fff", null));
            handler.publish(mkRecord(Level.FINER, "ff", null));
            handler.publish(mkRecord(Level.FINE, "f", null));
            handler.publish(mkRecord(Level.CONFIG, "c", null));
            handler.publish(mkRecord(Level.INFO, "i", null));
            handler.publish(mkRecord(Level.WARNING, "w", null));
            handler.publish(mkRecord(Level.SEVERE, "s", null));
            handler.publish(mkRecord(Level.OFF, "off", null));
            handler.publish(mkRecord(Level.SEVERE, "s", new Throwable()));

            handler.flush(); // no-op
        } finally {
            handler.close();
        }

        EasyMock.verify(bundle, logger, logService);
    }

    private LogRecord mkRecord(@NonNull Level lvl, @NonNull String msg, @CheckForNull Throwable t) {
        LogRecord record = new LogRecord(lvl, msg);
        if (t != null)
            record.setThrown(t);
        return record;
    }

    private Bundle makeBundle() {
        Bundle bundle = EasyMock.createMock(Bundle.class);
        EasyMock.expect(bundle.getBundleId()).andReturn(1L).anyTimes();
        EasyMock.expect(bundle.getSymbolicName()).andReturn("bundle1").anyTimes();
        return bundle;
    }
}
