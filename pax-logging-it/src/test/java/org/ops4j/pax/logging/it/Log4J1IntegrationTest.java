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
import java.util.List;
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
import org.ops4j.pax.logging.it.support.Helpers;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.FormatterLogger;
import org.osgi.service.log.LogService;
import org.osgi.service.log.LoggerFactory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.ops4j.pax.exam.OptionUtils.combine;

/**
 * <p>See org.ops4j.pax.logging.test.log4j1.Log4j1NativeApiTest in pax-logging-api-test project.</p>
 * <p>This test doesn't work from IDE - it requires failsafe-maven-plugin configuration of classpath.</p>
 */
@RunWith(PaxExam.class)
public class Log4J1IntegrationTest extends AbstractStdoutInterceptingIntegrationTestBase {

    @Inject
    private org.osgi.service.log.LogService osgiLogService;

    @Inject
    private org.osgi.service.log.LoggerFactory osgiLoggerFactory;

    @Override
    public void hijackStdout() throws BundleException {
        super.hijackStdout();
        Helpers.restartPaxLoggingService(context, true);
    }

    @Configuration
    public Option[] configure() throws IOException {
        // even if default/fallback logger would write to a file (and actually Log4J1's LogLog class does that),
        // Log4J1's default configuration uses ConsoleAppender
        return combine(
                combine(baseConfigure(), defaultLoggingConfig()),

                paxLoggingApi(),
                paxLoggingLog4J1(),
                configAdmin(),
                eventAdmin()
        );
    }

    @Test
    public void simplestUsage() throws IOException {
        Logger log = Logger.getLogger(Log4J1IntegrationTest.class);
        // MDC won't be printed because default TTCC layout doesn't handle it
        MDC.put("user", "me");
        MDC.put("country", "Equestria");
        // NDC won't be printed because pax-logging-api doesn't support it (yet?)
        NDC.push("layer42");
        NDC.push("layer43");

        log.info("simplestUsage - INFO");
        log.trace("simplestUsage - TRACE");

        Logger.getLogger("special").trace("simplestUsage - TRACE");

        List<String> lines = readLines();

        assertFalse(lines.stream().anyMatch(l -> l.contains("simplestUsage - TRACE")));
        assertTrue("Line should be printed without MDC",
                lines.stream().anyMatch(l -> l.contains("simplestUsage - INFO") && !l.contains("Equestria")));
    }

    @Test
    public void usageThroughJULAPI() throws IOException {
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Log4J1IntegrationTest.class.getName());
        logger.info("INFO through java.util.logging");
        logger.fine("FINE through java.util.logging");
        logger.finer("FINER through java.util.logging");
        logger.finest("FINEST through java.util.logging");

        List<String> lines = readLines();

        assertTrue(lines.contains("[main] INFO org.ops4j.pax.logging.it.Log4J1IntegrationTest - INFO through java.util.logging"));
        assertTrue(lines.contains("[main] DEBUG org.ops4j.pax.logging.it.Log4J1IntegrationTest - FINE through java.util.logging"));
        assertFalse(lines.stream().anyMatch(l -> l.contains("FINER through java.util.logging")));
        assertFalse(lines.stream().anyMatch(l -> l.contains("FINEST through java.util.logging")));
    }

    @Test
    public void usageThroughOsgi() throws IOException {
        ServiceReference<LogService> sr = context.getServiceReference(org.osgi.service.log.LogService.class);
        LogService log = context.getService(sr);
        log.log(LogService.LOG_INFO, "INFO1 through org.osgi.service.log");
        log.log(LogService.LOG_DEBUG, "DEBUG1 through org.osgi.service.log");

        osgiLogService.log(LogService.LOG_INFO, "INFO2 through org.osgi.service.log");
        osgiLogService.log(LogService.LOG_DEBUG, "DEBUG2 through org.osgi.service.log");

        List<String> lines = readLines();

        assertTrue(lines.contains("[main] INFO PaxExam-Probe - INFO1 through org.osgi.service.log"));
        assertTrue(lines.contains("[main] DEBUG PaxExam-Probe - DEBUG1 through org.osgi.service.log"));
        assertTrue(lines.contains("[main] INFO PaxExam-Probe - INFO2 through org.osgi.service.log"));
        assertTrue(lines.contains("[main] DEBUG PaxExam-Probe - DEBUG2 through org.osgi.service.log"));
    }

    @Test
    public void usageThroughOsgiR7() throws IOException {
        assertNotNull(osgiLoggerFactory);

        ServiceReference<LoggerFactory> sr = context.getServiceReference(org.osgi.service.log.LoggerFactory.class);
        LoggerFactory loggerFactory = context.getService(sr);

        org.osgi.service.log.Logger log = loggerFactory.getLogger("my.logger");
        log.info("INFO1 through org.osgi.service.log.Logger");
        log.debug("DEBUG1 through org.osgi.service.log.Logger");
        log.warn("WARN1 through org.osgi.service.log.Logger {}", "arg1");

        log = loggerFactory.getLogger("my.logger", FormatterLogger.class);
        log.info("INFO2 through org.osgi.service.log.Logger");
        log.debug("DEBUG2 through org.osgi.service.log.Logger");
        log.warn("WARN2 through org.osgi.service.log.Logger %s", "arg1");

        log.error("ERROR1 through org.osgi.service.log.Logger", new Exception("Hello"));

        List<String> lines = readLines();

        assertTrue(lines.contains("[main] INFO my.logger - INFO1 through org.osgi.service.log.Logger"));
        assertTrue(lines.contains("[main] DEBUG my.logger - DEBUG1 through org.osgi.service.log.Logger"));
        assertTrue(lines.contains("[main] WARN my.logger - WARN1 through org.osgi.service.log.Logger arg1"));
        assertTrue(lines.contains("[main] INFO my.logger - INFO2 through org.osgi.service.log.Logger"));
        assertTrue(lines.contains("[main] DEBUG my.logger - DEBUG2 through org.osgi.service.log.Logger"));
        assertTrue(lines.contains("[main] WARN my.logger - WARN2 through org.osgi.service.log.Logger arg1"));
        assertTrue(lines.contains("[main] ERROR my.logger - ERROR1 through org.osgi.service.log.Logger"));
        assertTrue(lines.contains("java.lang.Exception: Hello"));
    }

    @Test
    public void usageThroughOtherAPIs() throws IOException {
        String name = Log4J1IntegrationTest.class.getName();

        org.slf4j.Logger slf4jLogger = org.slf4j.LoggerFactory.getLogger(name);
        slf4jLogger.info("INFO through SLF4J");
        slf4jLogger.trace("TRACE through SLF4J");

        org.apache.commons.logging.Log commonsLogger = org.apache.commons.logging.LogFactory.getLog(name);
        commonsLogger.info("INFO through Apache Commons Logging");
        commonsLogger.trace("TRACE through Apache Commons Logging");

        org.apache.juli.logging.Log juliLogger = org.apache.juli.logging.LogFactory.getLog(name);
        juliLogger.info("INFO through JULI Logging");
        juliLogger.trace("TRACE through JULI Logging");

        org.apache.avalon.framework.logger.Logger avalonLogger = org.ops4j.pax.logging.avalon.AvalonLogFactory.getLogger(name);
        avalonLogger.info("INFO through Avalon Logger API");
        avalonLogger.debug("DEBUG through Avalon Logger API");

        org.jboss.logging.Logger jbossLogger = org.jboss.logging.Logger.getLogger(name);
        jbossLogger.info("INFO through JBoss Logging Logger API");
        jbossLogger.trace("TRACE through JBoss Logging Logger API");

//        // Knopflerfish - the bundle associated with the "logger" will be the bundle used to obtain
//        // service reference - here, PaxExam-Probe
//        LogRef lr = new LogRef(context);
//        lr.info("INFO1 through Knopflerfish");
//        lr.debug("DEBUG1 through Knopflerfish");
//
//        fishLogService.log(LogService.LOG_INFO, "INFO2 through Knopflerfish");
//        fishLogService.log(LogService.LOG_DEBUG, "DEBUG2 through Knopflerfish");

        org.apache.logging.log4j.Logger log4j2Logger = org.apache.logging.log4j.LogManager.getLogger(name);
        log4j2Logger.info("INFO through Log4J v2 API");
        log4j2Logger.trace("TRACE through Log4J v2 API");

        List<String> lines = readLines();

        assertTrue(lines.contains("[main] INFO org.ops4j.pax.logging.it.Log4J1IntegrationTest - INFO through SLF4J"));
        assertFalse(lines.contains("[main] TRACE org.ops4j.pax.logging.it.Log4J1IntegrationTest - TRACE through SLF4J"));
        assertTrue(lines.contains("[main] INFO org.ops4j.pax.logging.it.Log4J1IntegrationTest - INFO through Apache Commons Logging"));
        assertFalse(lines.contains("[main] TRACE org.ops4j.pax.logging.it.Log4J1IntegrationTest - TRACE through Apache Commons Logging"));
        assertTrue(lines.contains("[main] INFO org.ops4j.pax.logging.it.Log4J1IntegrationTest - INFO through JULI Logging"));
        assertFalse(lines.contains("[main] TRACE org.ops4j.pax.logging.it.Log4J1IntegrationTest - TRACE through JULI Logging"));
        assertTrue(lines.contains("[main] INFO org.ops4j.pax.logging.it.Log4J1IntegrationTest - INFO through Avalon Logger API"));
        assertTrue(lines.contains("[main] DEBUG org.ops4j.pax.logging.it.Log4J1IntegrationTest - DEBUG through Avalon Logger API"));
        assertTrue(lines.contains("[main] INFO org.ops4j.pax.logging.it.Log4J1IntegrationTest - INFO through JBoss Logging Logger API"));
        assertFalse(lines.contains("[main] TRACE org.ops4j.pax.logging.it.Log4J1IntegrationTest - TRACE through JBoss Logging Logger API"));
//        !!assertTrue(lines.contains("[main] INFO PaxExam-Probe - INFO1 through Knopflerfish"));
//        !!assertTrue(lines.contains("[main] DEBUG PaxExam-Probe - DEBUG1 through Knopflerfish"));
//        !!assertTrue(lines.contains("[main] INFO PaxExam-Probe - INFO2 through Knopflerfish"));
//        !!assertTrue(lines.contains("[main] DEBUG PaxExam-Probe - DEBUG2 through Knopflerfish"));
        assertTrue(lines.contains("[main] INFO org.ops4j.pax.logging.it.Log4J1IntegrationTest - INFO through Log4J v2 API"));
        assertFalse(lines.contains("[main] TRACE org.ops4j.pax.logging.it.Log4J1IntegrationTest - TRACE through Log4J v2 API"));
    }

    @Test
    public void loggerAPI() throws IOException {
        Logger log = Logger.getLogger(Log4J1IntegrationTest.class);

        log.info("loggerAPI - INFO1");
        log.trace("loggerAPI - TRACE1");
        assertFalse(log.isTraceEnabled());

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

        List<String> lines = readLines();

        assertTrue(lines.stream().anyMatch(l -> l.contains("loggerAPI - INFO1")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("loggerAPI - INFO2")));
        assertFalse(lines.stream().anyMatch(l -> l.contains("loggerAPI - TRACE1")));
        assertFalse(lines.stream().anyMatch(l -> l.contains("loggerAPI - TRACE2")));
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

        List<String> lines = readLines();

        assertTrue(lines.stream().anyMatch(l -> l.contains("MF: Hello, Hello World!")));
    }

    /**
     * Test using SLF4J like pattern formatting syntax.
     * @throws Exception
     */
    @Test
    public void logSFApi() throws Exception {
        LogSF.info(Logger.getLogger("logSFApi"), "SF: {} {}!", "Hello", "World");

        List<String> lines = readLines();

        assertTrue(lines.stream().anyMatch(l -> l.contains("SF: Hello World!")));
    }

}
