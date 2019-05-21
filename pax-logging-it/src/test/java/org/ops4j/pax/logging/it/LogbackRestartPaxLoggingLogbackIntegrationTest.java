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
public class LogbackRestartPaxLoggingLogbackIntegrationTest extends AbstractStdoutInterceptingIntegrationTestBase {

    @Inject
    private LogService osgiLog;

    @Override
    public void hijackStdout() throws BundleException {
        super.hijackStdout();
        Helpers.restartPaxLoggingService(context, true);
    }

    @Configuration
    public Option[] configure() throws IOException {
        // explicit configuration that doesn't use file-based default/fallback logger
        return combine(
                baseConfigure(),

                paxLoggingApi(),
                paxLoggingLogback(),
                configAdmin(),
                eventAdmin(),

                frameworkProperty(PaxLoggingConstants.LOGGING_CFG_DEFAULT_LOG_LEVEL).value("DEBUG"),
                frameworkProperty(PaxLoggingConstants.LOGGING_CFG_FRAMEWORK_EVENTS_LOG_LEVEL).value("DISABLED"),

                // this will make use of static LoggerContext, so loggers will be preserved between
                // restarts - this will make @Injected references not tied to old Logback LoggerContext
                frameworkProperty(PaxLoggingConstants.LOGGING_CFG_LOGBACK_USE_STATIC_CONTEXT).value("true")
        );
    }

    @Test
    public void restartPaxLoggingLogback() throws BundleException {
        Logger log = LoggerFactory.getLogger(this.getClass());
        Bundle paxLoggingLogback = Helpers.paxLoggingLogback(context);

        // TTLLLayout - everything's working
        log.info("Before restarting pax-logging-logback");
        // TTLLLayout - everything's working
        osgiLog.log(LogService.LOG_INFO, "Before restarting pax-logging-logback");

        paxLoggingLogback.stop(Bundle.STOP_TRANSIENT);

        // goes to System.out-based default/fallback logger because logger has delegate replaced using
        // pax-logging-api mechanisms
        log.info("When pax-logging-logback is stopped");

        // to file, but with
        // "org.ops4j.pax.logging.pax-logging-logback [logback] WARN : No appenders present in context [default] for logger [PaxExam-Probe]."
        // message logged through ch.qos.logback.core.status.StatusListener registered on stopped
        // static ch.qos.logback.classic.LoggerContext
        osgiLog.log(LogService.LOG_INFO, "When pax-logging-logback is stopped (log service)");

        // LogService is no longer available
        assertNull(context.getServiceReference(LogService.class));

        // This logger was already cached by pax-logging-api, so no changes
        Logger log1 = LoggerFactory.getLogger(this.getClass());
        log1.info("When pax-logging-logback is stopped (log1)");

        // Logger that wasn't cached - but normally delegating to DefaultServiceLog
        Logger log2 = LoggerFactory.getLogger(this.getClass().getName() + "Ex");
        log2.info("When pax-logging-logback is stopped (log2)");

        paxLoggingLogback.start(Bundle.START_TRANSIENT);

        // These loggers immediately switched to delegate to Logback, because pax-logging-api
        // detected registration of pax-logging-logback based PaxLoggingService
        log.info("After restarting pax-logging-logback");
        log1.info("After restarting pax-logging-logback (log1)");
        // old reference, but switched to reconfigured Logback
        osgiLog.log(LogService.LOG_INFO, "After restarting pax-logging-logback (log service old ref)");

        ServiceReference<LogService> ref = context.getServiceReference(LogService.class);
        assertNotNull(ref);
        context.getService(ref).log(LogService.LOG_INFO, "After restarting pax-logging-logback (log service new ref)");

        Logger log3 = LoggerFactory.getLogger(this.getClass());
        log3.info("After restarting pax-logging-logback (log3)");

        List<String> lines = readLines();
        List<String> lines2 = lines.stream().map(l -> l.length() > 13 ? l.substring(13) : l)
                .collect(Collectors.toList());

        assertTrue("TTLLLayout", lines2.contains("[main] INFO org.ops4j.pax.logging.it.LogbackRestartPaxLoggingLogbackIntegrationTest - Before restarting pax-logging-logback"));
        assertTrue("TTLLLayout", lines2.contains("[main] INFO PaxExam-Probe - Before restarting pax-logging-logback"));
        assertTrue("Default Logger", lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.LogbackRestartPaxLoggingLogbackIntegrationTest] INFO : When pax-logging-logback is stopped"));
        assertFalse("old LogService reference should not work", lines.stream().anyMatch(l -> l.contains("When pax-logging-logback is stopped (log service)")));
        assertTrue("Default Logger", lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.LogbackRestartPaxLoggingLogbackIntegrationTest] INFO : When pax-logging-logback is stopped (log1)"));
        assertTrue("Default Logger", lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.LogbackRestartPaxLoggingLogbackIntegrationTestEx] INFO : When pax-logging-logback is stopped (log2)"));
        assertTrue("Reconfiguration", lines.contains("org.ops4j.pax.logging.pax-logging-logback [logback] INFO : Logback configured using default configuration."));
        assertTrue("TTLLLayout", lines2.contains("[main] INFO org.ops4j.pax.logging.it.LogbackRestartPaxLoggingLogbackIntegrationTest - After restarting pax-logging-logback"));
        assertTrue("TTLLLayout", lines2.contains("[main] INFO org.ops4j.pax.logging.it.LogbackRestartPaxLoggingLogbackIntegrationTest - After restarting pax-logging-logback (log1)"));
        assertTrue("TTLLLayout", lines2.contains("[main] INFO PaxExam-Probe - After restarting pax-logging-logback (log service old ref)"));
        assertTrue("TTLLLayout", lines2.contains("[main] INFO PaxExam-Probe - After restarting pax-logging-logback (log service new ref)"));
        assertTrue("TTLLLayout", lines2.contains("[main] INFO org.ops4j.pax.logging.it.LogbackRestartPaxLoggingLogbackIntegrationTest - After restarting pax-logging-logback (log3)"));
    }

}
