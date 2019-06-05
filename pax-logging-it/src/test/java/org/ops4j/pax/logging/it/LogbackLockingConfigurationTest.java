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
package org.ops4j.pax.logging.it;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Consumer;
import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.logging.PaxLoggingConstants;
import org.ops4j.pax.logging.PaxLoggingService;
import org.ops4j.pax.logging.it.support.Helpers;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.frameworkProperty;
import static org.ops4j.pax.exam.OptionUtils.combine;

@RunWith(PaxExam.class)
public class LogbackLockingConfigurationTest extends AbstractStdoutInterceptingIntegrationTestBase {

    @Inject
    private ConfigurationAdmin cm;

    @Override
    public void hijackStdout() throws BundleException {
        super.hijackStdout();
        Helpers.restartPaxLoggingLogback(context, true);
    }

    @Configuration
    public Option[] configure() throws IOException {
        return combine(
                combine(baseConfigure(), defaultLoggingConfig()),

                paxLoggingApi(),
                paxLoggingLogback(),
                configAdmin(),
                eventAdmin(),

                frameworkProperty(PaxLoggingConstants.LOGGING_CFG_USE_LOCKS).value("false")
        );
    }

    @Test
    public void turnOnLocks() throws IOException {
        ServiceReference<PaxLoggingService> sr = context.getServiceReference(PaxLoggingService.class);
        PaxLoggingService paxLogging = context.getService(sr);
        assertNotNull(paxLogging);

        final Object[] paxLoggingServiceImpl = new Object[1];
        Arrays.stream(paxLogging.getClass().getDeclaredFields()).forEach(new FieldConsumer(paxLoggingServiceImpl, paxLogging));
        assertNull(Helpers.getField(paxLoggingServiceImpl[0], "m_configLock", ReadWriteLock.class));

        LoggerFactory.getLogger(LogbackLockingConfigurationTest.class).info("Hello without locking");

        Helpers.updateLoggingConfig(context, cm, Helpers.LoggingLibrary.LOGBACK, "locks", props -> {
            props.put("org.ops4j.pax.logging.useLocks", "true");
        });

        sr = context.getServiceReference(PaxLoggingService.class);
        paxLogging = context.getService(sr);
        assertNotNull(paxLogging);

        paxLoggingServiceImpl[0] = null;
        Arrays.stream(paxLogging.getClass().getDeclaredFields()).forEach(new FieldConsumer(paxLoggingServiceImpl, paxLogging));
        assertNotNull(Helpers.getField(paxLoggingServiceImpl[0], "m_configLock", ReadWriteLock.class));

        LoggerFactory.getLogger(LogbackLockingConfigurationTest.class).info("Hello with locking");

        List<String> lines = readLines(13);
        assertTrue(lines.contains("[main] INFO org.ops4j.pax.logging.it.LogbackLockingConfigurationTest - Hello without locking"));
        assertTrue(lines.contains("[main] INFO org.ops4j.pax.logging.it.LogbackLockingConfigurationTest - Hello with locking"));
    }

    private static class FieldConsumer implements Consumer<Field> {

        private final Object[] paxLoggingServiceImpl;
        private final PaxLoggingService paxLogging;

        public FieldConsumer(Object[] paxLoggingServiceImpl, PaxLoggingService paxLogging) {
            this.paxLoggingServiceImpl = paxLoggingServiceImpl;
            this.paxLogging = paxLogging;
        }

        @Override
        public void accept(Field f) {
            if (f.getType().getName().equals("org.ops4j.pax.logging.logback.internal.PaxLoggingServiceImpl")) {
                f.setAccessible(true);
                try {
                    paxLoggingServiceImpl[0] = f.get(paxLogging);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        }
    }

}
