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

import org.junit.Test;
import org.ops4j.pax.logging.EventAdminPoster;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingService;
import org.ops4j.pax.logging.spi.support.ConfigurationNotifier;
import org.ops4j.pax.logging.spi.support.LogReaderServiceImpl;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogLevel;
import org.osgi.service.log.LogService;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Chris Dolan
 * @since 6/10/11 11:13 AM
 */
public class PaxLoggingServiceImplTest {

    /**
     * Tests the main functionality of the logging service.
     */
    @SuppressWarnings("deprecation")
    @Test
    public void test() {
        BundleContext bundleContext = mock(BundleContext.class);
        EventAdminPoster eventPoster = mock(EventAdminPoster.class);

        final Bundle mockBundle = makeBundle();
        ServiceReference<?> serviceReference = mock(ServiceReference.class);
        when(serviceReference.getBundle()).thenReturn(mockBundle);

        ConfigurationNotifier notifier = mock(ConfigurationNotifier.class);

        final PaxLogger logger0 = mock(PaxLogger.class);
        final PaxLogger logger1 = mock(PaxLogger.class);
        final PaxLogger logger2 = mock(PaxLogger.class);

        PaxLoggingServiceImpl service = new PaxLoggingServiceImpl(bundleContext, new LogReaderServiceImpl(0, null), eventPoster, notifier, logger0) {
            public PaxLogger getLogger(Bundle bundle, String category, String fqcn) {
                assertEquals(getClass().getName(), fqcn);
                if (bundle == null && "undefined".equals(category))
                    return logger1;
                if (bundle == mockBundle && "bundle1".equals(category))
                    return logger2;
                throw new AssertionError("bundle: " + bundle + ", category: " + category);
            }
        };

        try {
            assertEquals(LogLevel.DEBUG, service.getLogLevel());

            service.log(LogService.LOG_DEBUG, "d");
            service.log(LogService.LOG_INFO, "i");
            service.log(LogService.LOG_WARNING, "w");
            service.log(LogService.LOG_ERROR, "e");
            service.log(LogService.LOG_ERROR, "e", new Throwable());
            service.log(serviceReference, LogService.LOG_INFO, "ib", null);
            service.log(serviceReference, LogService.LOG_INFO, "isr", null);
            service.log(serviceReference, LogService.LOG_INFO, "isr2");
        } finally {
            service.shutdown();
        }

        verify(logger1).debug("d");
        verify(logger1).info("i");
        verify(logger1).warn("w");
        verify(logger1).error("e");
        verify(logger1).error(eq("e"), isA(Throwable.class));

        verify(logger2).info("ib");
        verify(logger2).info("isr");
        verify(logger2).info("isr2");
    }

    /**
     * Tests the ManagedService inner class.
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testInner() {
        BundleContext bundleContext = mock(BundleContext.class);
        EventAdminPoster eventPoster = mock(EventAdminPoster.class);

        final Bundle mockBundle = makeBundle();

        ConfigurationNotifier notifier = mock(ConfigurationNotifier.class);

        final PaxLogger logger0 = mock(PaxLogger.class);
        final PaxLogger logger = mock(PaxLogger.class);

        PaxLoggingServiceImpl service = new PaxLoggingServiceImpl(bundleContext, new LogReaderServiceImpl(0, null), eventPoster, notifier, logger0) {
            public PaxLogger getLogger(Bundle bundle, String category, String fqcn) {
                assertEquals(PaxLoggingServiceImpl.class.getName() + "$1ManagedPaxLoggingService", fqcn);
                if (bundle == mockBundle && "bundle1".equals(category))
                    return logger;
                throw new AssertionError("bundle: " + bundle + ", category: " + category);
            }
        };

        PaxLoggingService innerService = (PaxLoggingService) service.getService(mockBundle, null);
        try {
            assertEquals(LogLevel.DEBUG, innerService.getLogLevel());
            innerService.log(LogService.LOG_DEBUG, "d");
            innerService.log(LogService.LOG_INFO, "i");
            innerService.log(LogService.LOG_WARNING, "w");
            innerService.log(LogService.LOG_ERROR, "e");
            innerService.log(LogService.LOG_ERROR, "e", new Throwable());
        } finally {
            service.shutdown();
        }

        verify(logger).debug("d");
        verify(logger).info("i");
        verify(logger).warn("w");
        verify(logger).error("e");
        verify(logger).error(eq("e"), isA(Throwable.class));
    }

    private Bundle makeBundle() {
        Bundle bundle = mock(Bundle.class);
        when(bundle.getBundleId()).thenReturn(1L);
        when(bundle.getSymbolicName()).thenReturn("bundle1");

        return bundle;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullBundleContext() {
        new PaxLoggingServiceImpl(null, mock(LogReaderServiceImpl.class), mock(EventAdminPoster.class), mock(ConfigurationNotifier.class), mock(PaxLogger.class));
    }

}
