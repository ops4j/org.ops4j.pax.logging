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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.logging.PaxLoggingConstants;
import org.ops4j.pax.logging.it.support.Helpers;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.frameworkProperty;
import static org.ops4j.pax.exam.OptionUtils.combine;

@RunWith(PaxExam.class)
public class Log4J2RestartPaxLoggingLog4J2ServiceIntegrationTest extends AbstractStdoutInterceptingIntegrationTestBase {

    @Inject
    private LogService osgiLog;

    @Override
    public void hijackStdout() throws BundleException {
        super.hijackStdout();
        Helpers.restartPaxLoggingLog4j2(context, true);
    }

    @Configuration
    public Option[] configure() throws IOException {
        // explicit configuration that doesn't use file-based default/fallback logger
        return combine(
                baseConfigure(),

                paxLoggingApi(),
                paxLoggingLog4J2(),
                configAdmin(),
                eventAdmin(),

                frameworkProperty(PaxLoggingConstants.LOGGING_CFG_DEFAULT_LOG_LEVEL).value("DEBUG"),
                frameworkProperty(PaxLoggingConstants.LOGGING_CFG_FRAMEWORK_EVENTS_LOG_LEVEL).value("DISABLED")
        );
    }

    @Test
    public void restartPaxLoggingLog4j2() throws BundleException {
        Logger log = LoggerFactory.getLogger(this.getClass());
        Bundle paxLoggingLog4j2 = Helpers.paxLoggingLog4j2(context);

        // default pattern for default configuration is org.apache.logging.log4j.core.config.DefaultConfiguration.DEFAULT_PATTERN
        // i.e., %d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n

        // Differently than with pax-logging-service and pax-logging-logback (with static context), here, with
        // pax-logging-log4j2, stopping the bundle will effectively turn off all logging - all loggers will be
        // available, but with OFF level. See org.apache.logging.log4j.core.LoggerContext.stop(long, TimeUnit).
        
        // DEFAULT_PATTERN - everything's working
        log.info("Before restarting pax-logging-log4j2");
        // DEFAULT_PATTERN - not working, because @Injected reference is already old
        osgiLog.log(LogService.LOG_INFO, "Before restarting pax-logging-log4j2 (log service old)");

        ServiceReference<LogService> ref = context.getServiceReference(LogService.class);
        assertNotNull(ref);
        context.getService(ref).log(LogService.LOG_INFO, "Before restarting pax-logging-log4j2 (log service new)");

        paxLoggingLog4j2.stop(Bundle.STOP_TRANSIENT);

        // goes to System.out-based default/fallback logger because logger has delegate replaced using
        // pax-logging-api mechanisms
        log.info("When pax-logging-log4j2 is stopped");

        // loggers are reconfigured to use org.apache.logging.log4j.core.config.NullConfiguration with OFF level
        osgiLog.log(LogService.LOG_INFO, "When pax-logging-log4j2 is stopped (log service)");

        // LogService is no longer available
        assertNull(context.getServiceReference(LogService.class));

        // This logger was already cached by pax-logging-api, so no changes
        Logger log1 = LoggerFactory.getLogger(this.getClass());
        log1.info("When pax-logging-log4j2 is stopped (log1)");

        // Logger that wasn't cached - but normally delegating to DefaultServiceLog
        Logger log2 = LoggerFactory.getLogger(this.getClass().getName() + "Ex");
        log2.info("When pax-logging-log4j2 is stopped (log2)");

        paxLoggingLog4j2.start(Bundle.START_TRANSIENT);

        // These loggers immediately switched to delegate to Log4J2, because pax-logging-api
        // detected registration of pax-logging-log4j2 based PaxLoggingService
        log.info("After restarting pax-logging-log4j2");
        log1.info("After restarting pax-logging-log4j2 (log1)");
        // old reference, but switched to reconfigured Log4J2
        osgiLog.log(LogService.LOG_INFO, "After restarting pax-logging-log4j2 (log service old ref)");

        ref = context.getServiceReference(LogService.class);
        assertNotNull(ref);
        context.getService(ref).log(LogService.LOG_INFO, "After restarting pax-logging-log4j2 (log service new ref)");

        Logger log3 = LoggerFactory.getLogger(this.getClass());
        log3.info("After restarting pax-logging-log4j2 (log3)");

        List<String> lines = readLines();
        List<String> lines2 = readLines(13);

        assertTrue("DEFAULT_PATTERN", lines2.contains("[main] INFO  org.ops4j.pax.logging.it.Log4J2RestartPaxLog4J2ServiceIntegrationTest - Before restarting pax-logging-log4j2"));
        assertFalse("@Injected LogService reference should not work", lines.stream().anyMatch(l -> l.contains("Before restarting pax-logging-log4j2 (log service old)")));
        assertTrue("new LogService reference should work", lines.stream().anyMatch(l -> l.contains("Before restarting pax-logging-log4j2 (log service new)")));
        assertTrue("Default Logger", lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.Log4J2RestartPaxLog4J2ServiceIntegrationTest] INFO : When pax-logging-log4j2 is stopped"));
        assertFalse("old LogService reference should not work", lines.stream().anyMatch(l -> l.contains("When pax-logging-log4j2 is stopped (log service)")));
        assertTrue("Default Logger", lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.Log4J2RestartPaxLog4J2ServiceIntegrationTest] INFO : When pax-logging-log4j2 is stopped (log1)"));
        assertTrue("Default Logger", lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.Log4J2RestartPaxLog4J2ServiceIntegrationTestEx] INFO : When pax-logging-log4j2 is stopped (log2)"));
        assertTrue("Reconfiguration", lines.stream().anyMatch(l -> l.startsWith("org.ops4j.pax.logging.pax-logging-api [log4j2] INFO : Log4J2 configured using default configuration.")));
        assertTrue("DEFAULT_PATTERN", lines2.contains("[main] INFO  org.ops4j.pax.logging.it.Log4J2RestartPaxLog4J2ServiceIntegrationTest - After restarting pax-logging-log4j2"));
        assertTrue("DEFAULT_PATTERN", lines2.contains("[main] INFO  org.ops4j.pax.logging.it.Log4J2RestartPaxLog4J2ServiceIntegrationTest - After restarting pax-logging-log4j2 (log1)"));
        assertFalse("DEFAULT_PATTERN", lines2.contains("[main] INFO  PaxExam-Probe - After restarting pax-logging-log4j2 (log service old ref)"));
        assertTrue("DEFAULT_PATTERN", lines2.contains("[main] INFO  PaxExam-Probe - After restarting pax-logging-log4j2 (log service new ref)"));
        assertTrue("DEFAULT_PATTERN", lines2.contains("[main] INFO  org.ops4j.pax.logging.it.Log4J2RestartPaxLog4J2ServiceIntegrationTest - After restarting pax-logging-log4j2 (log3)"));
    }

}
