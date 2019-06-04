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
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.knopflerfish.service.log.LogRef;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.logging.it.support.Helpers;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.OptionUtils.combine;

@RunWith(PaxExam.class)
public class Log4J2IntegrationTest extends AbstractStdoutInterceptingIntegrationTestBase {

    @Override
    public void hijackStdout() throws BundleException {
        super.hijackStdout();
        Helpers.restartPaxLoggingLog4j2(context, true);
    }

    @Configuration
    public Option[] configure() throws IOException {
        return combine(
                combine(baseConfigure(), defaultLoggingConfig()),

                paxLoggingApi(),
                paxLoggingLog4J2(),
                configAdmin(),
                eventAdmin()
        );
    }

    @Test
    public void simplestUsage() throws IOException {
        Logger log = LoggerFactory.getLogger(Log4J2IntegrationTest.class);
        // MDC won't be printed because org.apache.logging.log4j.core.config.DefaultConfiguration.DEFAULT_PATTERN
        // doesn't handle it
        // TTLL layout is %d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
        MDC.put("user", "me");
        MDC.put("country", "Equestria");

        log.info("simplestUsage - INFO");
        log.trace("simplestUsage - TRACE");

        List<String> lines = readLines();
        lines = lines.stream().map(l -> l.substring(13)).collect(Collectors.toList());

        assertTrue(lines.contains("[main] INFO  org.ops4j.pax.logging.it.Log4J2IntegrationTest - simplestUsage - INFO"));
        assertTrue("Line should be printed without MDC", lines.stream().noneMatch(l -> l.contains("Equestria")));
    }

    @Test
    public void usageThroughJULAPI() throws IOException {
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Log4J2IntegrationTest.class.getName());
        logger.info("INFO through java.util.logging");
        logger.fine("FINE through java.util.logging");
        logger.finer("FINER through java.util.logging");
        logger.finest("FINEST through java.util.logging");

        List<String> lines = readLines();
        lines = lines.stream().map(l -> l.substring(13)).collect(Collectors.toList());

        assertTrue(lines.contains("[main] INFO  org.ops4j.pax.logging.it.Log4J2IntegrationTest - INFO through java.util.logging"));
        assertTrue(lines.contains("[main] DEBUG org.ops4j.pax.logging.it.Log4J2IntegrationTest - FINE through java.util.logging"));
        assertFalse(lines.stream().anyMatch(l -> l.contains("FINER through java.util.logging")));
        assertFalse(lines.stream().anyMatch(l -> l.contains("FINEST through java.util.logging")));
    }

    @Test
    public void usageThroughOsgi() throws IOException {
        ServiceReference<LogService> sr = context.getServiceReference(LogService.class);
        LogService log = context.getService(sr);
        log.log(LogService.LOG_INFO, "INFO1 through org.osgi.service.log");
        log.log(LogService.LOG_DEBUG, "DEBUG1 through org.osgi.service.log");

        List<String> lines = readLines();
        lines = lines.stream().map(l -> l.substring(13)).collect(Collectors.toList());

        assertTrue(lines.contains("[main] INFO  PaxExam-Probe - INFO1 through org.osgi.service.log"));
        assertTrue(lines.contains("[main] DEBUG PaxExam-Probe - DEBUG1 through org.osgi.service.log"));
    }

    @Test
    public void usageThroughOtherAPIs() throws IOException {
        String name = Log4J2IntegrationTest.class.getName();

        Logger slf4jLogger = LoggerFactory.getLogger(name);
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

        // Knopflerfish - the bundle associated with the "logger" will be the bundle used to obtain
        // service reference - here, PaxExam-Probe
        LogRef lr = new LogRef(context);
        lr.info("INFO1 through Knopflerfish");
        lr.debug("DEBUG1 through Knopflerfish");

        ServiceReference<org.knopflerfish.service.log.LogService> sr = context.getServiceReference(org.knopflerfish.service.log.LogService.class);
        org.knopflerfish.service.log.LogService fishLogService = context.getService(sr);
        fishLogService.log(LogService.LOG_INFO, "INFO2 through Knopflerfish");
        fishLogService.log(LogService.LOG_DEBUG, "DEBUG2 through Knopflerfish");

        org.apache.logging.log4j.Logger log4j2Logger = org.apache.logging.log4j.LogManager.getLogger(name);
        log4j2Logger.info("INFO through Log4J v2 API");
        log4j2Logger.trace("TRACE through Log4J v2 API");

        List<String> lines = readLines();
        lines = lines.stream().map(l -> l.substring(13)).collect(Collectors.toList());

        assertTrue(lines.contains("[main] INFO  org.ops4j.pax.logging.it.Log4J2IntegrationTest - INFO through SLF4J"));
        assertFalse(lines.contains("[main] TRACE org.ops4j.pax.logging.it.Log4J2IntegrationTest - TRACE through SLF4J"));
        assertTrue(lines.contains("[main] INFO  org.ops4j.pax.logging.it.Log4J2IntegrationTest - INFO through Apache Commons Logging"));
        assertFalse(lines.contains("[main] TRACE org.ops4j.pax.logging.it.Log4J2IntegrationTest - TRACE through Apache Commons Logging"));
        assertTrue(lines.contains("[main] INFO  org.ops4j.pax.logging.it.Log4J2IntegrationTest - INFO through JULI Logging"));
        assertFalse(lines.contains("[main] TRACE org.ops4j.pax.logging.it.Log4J2IntegrationTest - TRACE through JULI Logging"));
        assertTrue(lines.contains("[main] INFO  org.ops4j.pax.logging.it.Log4J2IntegrationTest - INFO through Avalon Logger API"));
        assertTrue(lines.contains("[main] DEBUG org.ops4j.pax.logging.it.Log4J2IntegrationTest - DEBUG through Avalon Logger API"));
        assertTrue(lines.contains("[main] INFO  org.ops4j.pax.logging.it.Log4J2IntegrationTest - INFO through JBoss Logging Logger API"));
        assertFalse(lines.contains("[main] TRACE org.ops4j.pax.logging.it.Log4J2IntegrationTest - TRACE through JBoss Logging Logger API"));
        assertTrue(lines.contains("[main] INFO  PaxExam-Probe - INFO1 through Knopflerfish"));
        assertTrue(lines.contains("[main] DEBUG PaxExam-Probe - DEBUG1 through Knopflerfish"));
        assertTrue(lines.contains("[main] INFO  PaxExam-Probe - INFO2 through Knopflerfish"));
        assertTrue(lines.contains("[main] DEBUG PaxExam-Probe - DEBUG2 through Knopflerfish"));
        assertTrue(lines.contains("[main] INFO  org.ops4j.pax.logging.it.Log4J2IntegrationTest - INFO through Log4J v2 API"));
        assertFalse(lines.contains("[main] TRACE org.ops4j.pax.logging.it.Log4J2IntegrationTest - TRACE through Log4J v2 API"));
    }

}
