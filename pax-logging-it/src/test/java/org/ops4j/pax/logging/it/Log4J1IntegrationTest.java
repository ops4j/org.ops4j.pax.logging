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
import javax.inject.Inject;

import org.apache.log4j.Level;
import org.apache.log4j.LogMF;
import org.apache.log4j.LogManager;
import org.apache.log4j.LogSF;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.NDC;
import org.apache.log4j.helpers.Loader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.BundleContext;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.ops4j.pax.exam.OptionUtils.combine;

/**
 * See org.ops4j.pax.logging.test.log4j1.Log4j1NativeApiTest in pax-logging-api-test project.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class Log4J1IntegrationTest extends AbstractControlledIntegrationTestBase {

    @Inject
    private BundleContext context;

    @Configuration
    public Option[] configure() throws IOException {
        return combine(
                combine(baseConfigure(), defaultLoggingConfig()),

                paxLoggingApi(),
                paxLoggingLog4J1(),
                configAdmin()
        );
    }

    @Test
    public void simplestUsage() {
        Logger log = Logger.getLogger(Log4J1IntegrationTest.class);
        // MDC won't be printed because default TTCL layout doesn't handle it
        MDC.put("user", "me");
        MDC.put("country", "Equestria");
        // NDC won't be printed because pax-logging-api doesn't support it (yet?)
        NDC.push("layer42");
        NDC.push("layer43");

        log.info("simplestUsage - INFO");
        log.trace("simplestUsage - TRACE");

        Logger.getLogger("special").trace("simplestUsage - TRACE");
    }

    @Test
    public void loggerAPI() {
        Logger log = Logger.getLogger(Log4J1IntegrationTest.class);

        log.info("loggerAPI - INFO1");
        log.trace("loggerAPI - TRACE1");
        assertFalse(log.isTraceEnabled());

        Level l = log.getLevel();
        log.setLevel(Level.ALL);
        log.info("loggerAPI - INFO2");
        log.trace("loggerAPI - TRACE2");
        assertFalse("setLevel doesn't affect actual delegate logger", log.isTraceEnabled());

        assertNull("This isn't handled by pax-logging", log.getParent());

        try {
            log.addAppender(null);
            fail("Should've thrown " + UnsupportedOperationException.class.getName());
        } catch (UnsupportedOperationException ignored) {
        }

        assertFalse("This isn't handled by pax-logging", log.isAttached(null));

        try {
            log.removeAppender("anything");
            fail("Should've thrown " + UnsupportedOperationException.class.getName());
        } catch (UnsupportedOperationException ignored) {
        }
    }

    @Test
    public void logManagerAPI() {
        assertNotSame("Underlying delegates are the same, but not wrapping TrackingLoggers", LogManager.getLogger(Log4J1IntegrationTest.class), Logger.getLogger(Log4J1IntegrationTest.class));

        try {
            LogManager.getLoggerRepository();
            fail("Should've thrown " + UnsupportedOperationException.class.getName());
        } catch (UnsupportedOperationException ignored) {
        }

        try {
            LogManager.getCurrentLoggers();
            fail("Should've thrown " + UnsupportedOperationException.class.getName());
        } catch (UnsupportedOperationException ignored) {
        }
    }

    @Test
    public void loaderApi() throws Exception {
        Thread.currentThread().setContextClassLoader(null);
        // org.apache.log4j.helpers.Loader is exported by pax-logging-api and is related to this bundle
        // and its classloader
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(null);
            assertNotNull(Loader.loadClass("org.ops4j.pax.logging.PaxLogger"));
            assertNotNull(Loader.getResource("org/ops4j/pax/logging/PaxLoggingConstants.class"));
            assertNotNull(Loader.getResource("org/apache/log4j/Logger.class"));
        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }

    /**
     * Test using {@link java.text.MessageFormat} formatting syntax.
     * @throws Exception
     */
    @Test
    public void logMFApi() throws Exception {
        LogMF.info(Logger.getLogger("logXFApi"), "MF: {0}, {0} {1}!", "Hello", "World");
    }

    /**
     * Test using SLF4J like pattern formatting syntax.
     * @throws Exception
     */
    @Test
    public void logSFApi() throws Exception {
        LogSF.info(Logger.getLogger("logSFApi"), "SF: {} {}!", "Hello", "World");
    }

}
