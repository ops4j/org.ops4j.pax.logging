package org.ops4j.pax.logging.logback.internal;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.ops4j.pax.logging.EventAdminPoster;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.isA;

/**
 * @author Chris Dolan
 * @since 6/10/11 11:13 AM
 */
public class PaxLoggingServiceImplTest {
    @Test
    public void test() {
        BundleContext bundleContext = EasyMock.createNiceMock(BundleContext.class);
        EventAdminPoster eventPoster = EasyMock.createNiceMock(EventAdminPoster.class);

        final Bundle mockBundle = makeBundle();
        ServiceReference serviceReference = EasyMock.createStrictMock(ServiceReference.class);
        EasyMock.expect(serviceReference.getBundle()).andReturn(mockBundle).anyTimes();

        final PaxLogger logger1 = EasyMock.createStrictMock(PaxLogger.class);
        logger1.debug("d", null);   EasyMock.expectLastCall().once();
        logger1.inform("i", null);  EasyMock.expectLastCall().once();
        logger1.warn("w", null);    EasyMock.expectLastCall().once();
        logger1.error("e", null);   EasyMock.expectLastCall().once();
        logger1.error(eq("e"), isA(Throwable.class));   EasyMock.expectLastCall().once();

        final PaxLogger logger2 = EasyMock.createStrictMock(PaxLogger.class);
        logger2.inform("ib", null);  EasyMock.expectLastCall().once();
        logger2.inform("isr", null);  EasyMock.expectLastCall().once();
        logger2.inform("isr2", null);  EasyMock.expectLastCall().once();

        EasyMock.replay(bundleContext, eventPoster, mockBundle, serviceReference, logger1, logger2);

        PaxLoggingServiceImpl service = new PaxLoggingServiceImpl(bundleContext, new LogReaderServiceImpl(0), eventPoster) {
            public PaxLogger getLogger(Bundle bundle, String category, String fqcn) {
                Assert.assertEquals(getClass().getName(), fqcn);
                if (bundle == null && "[undefined]".equals(category))
                    return logger1;
                if (bundle == mockBundle && "bundle1".equals(category))
                    return logger2;
                throw new AssertionError("bundle: " + bundle + ", category: " + category);
            }
        };
        try {
            Assert.assertEquals(LogService.LOG_DEBUG, service.getLogLevel());
            service.log(LogService.LOG_DEBUG, "d");
            service.log(LogService.LOG_INFO, "i");
            service.log(LogService.LOG_WARNING, "w");
            service.log(LogService.LOG_ERROR, "e");
            service.log(LogService.LOG_ERROR, "e", new Throwable());
            service.log(mockBundle, LogService.LOG_INFO, "ib", null);
            service.log(serviceReference, LogService.LOG_INFO, "isr", null);
            service.log(serviceReference, LogService.LOG_INFO, "isr2");
        } finally {
            service.stop();
        }

        EasyMock.verify(bundleContext, eventPoster, mockBundle, serviceReference, logger1, logger2);
    }

    @Test
    public void testInner() {
        BundleContext bundleContext = EasyMock.createNiceMock(BundleContext.class);
        EventAdminPoster eventPoster = EasyMock.createNiceMock(EventAdminPoster.class);

        final Bundle mockBundle = makeBundle();

        final PaxLogger logger = EasyMock.createStrictMock(PaxLogger.class);
        logger.debug("d", null);   EasyMock.expectLastCall().once();
        logger.inform("i", null);  EasyMock.expectLastCall().once();
        logger.warn("w", null);    EasyMock.expectLastCall().once();
        logger.error("e", null);   EasyMock.expectLastCall().once();
        logger.error(eq("e"), isA(Throwable.class));   EasyMock.expectLastCall().once();

        EasyMock.replay(mockBundle, logger);

        PaxLoggingServiceImpl service = new PaxLoggingServiceImpl(bundleContext, new LogReaderServiceImpl(0), eventPoster) {
            public PaxLogger getLogger(Bundle bundle, String category, String fqcn) {
                Assert.assertEquals(PaxLoggingServiceImpl.class.getName() + "$1ManagedPaxLoggingService", fqcn);
                if (bundle == mockBundle && "bundle1".equals(category))
                    return logger;
                throw new AssertionError("bundle: " + bundle + ", category: " + category);
            }
        };

        PaxLoggingService innerService = (PaxLoggingService) service.getService(mockBundle, null);
        try {
            Assert.assertEquals(LogService.LOG_DEBUG, innerService.getLogLevel());
            innerService.log(LogService.LOG_DEBUG, "d");
            innerService.log(LogService.LOG_INFO, "i");
            innerService.log(LogService.LOG_WARNING, "w");
            innerService.log(LogService.LOG_ERROR, "e");
            innerService.log(LogService.LOG_ERROR, "e", new Throwable());
        } finally {
            service.stop();
        }

        EasyMock.verify(mockBundle, logger);
    }

    private Bundle makeBundle() {
        Bundle bundle = EasyMock.createMock(Bundle.class);
        EasyMock.expect(bundle.getBundleId()).andReturn(1L).anyTimes();
        EasyMock.expect(bundle.getSymbolicName()).andReturn("bundle1").anyTimes();
        return bundle;
    }
}
