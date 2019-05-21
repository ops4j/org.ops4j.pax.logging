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
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.frameworkProperty;
import static org.ops4j.pax.exam.OptionUtils.combine;

@RunWith(PaxExam.class)
public class LogbackRestartPaxLoggingApiIntegrationTest extends AbstractStdoutInterceptingIntegrationTestBase {

    @Inject
    private LogService osgiLog;

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

                // this will make use of static LoggerContext, so loggers will be preserved between
                // restarts - this will make @Injected references not tied to old Logback LoggerContext
                frameworkProperty(PaxLoggingConstants.LOGGING_CFG_LOGBACK_USE_STATIC_CONTEXT).value("true")
        );
    }

    @Test
    public void restartPaxLoggingApi() throws BundleException {
        Logger log = LoggerFactory.getLogger(this.getClass());
        Bundle paxLoggingApi = Helpers.paxLoggingApi(context);

        // through TTLL layout to System.out
        log.info("Before restarting pax-logging-api");
        osgiLog.log(LogService.LOG_INFO, "Before restarting pax-logging-api");

        paxLoggingApi.stop(Bundle.STOP_TRANSIENT);

        // tracked loggers in pax-logging-api's manager had their
        // org.ops4j.pax.logging.internal.TrackingLogger.removed() called - thus switching to DefaultServiceLog
        // even of context property could indicate FileServiceLog
        log.info("When pax-logging-api is stopped");
        osgiLog.log(LogService.LOG_INFO, "When pax-logging-api is stopped");

        // org.slf4j.ILoggerFactory is still from pax-logging-api,
        // but this bundle is stopped, so it always returns default/fallback loggers writing to
        // System.out - even if bundle context may have file-based default/fallback logger configured.
        // That's needed, because file-logger has to have PrintStream registered as OSGi service
        Logger log1 = LoggerFactory.getLogger(this.getClass());
        log1.info("When pax-logging-api is stopped (log1)");

        // when pax-logging-api bundle is started, OSGIPaxLoggingManager immediately tracks PaxLoggingService
        // registered by pax-logging-logback, so it immediately starts printing through Logback
        paxLoggingApi.start(Bundle.START_TRANSIENT);

        // but logging through logger obtained from previous life of pax-logging-api will never get connected
        // to tracking logger again
        log.info("After restarting pax-logging-api");
        // however, logger obtained when pax-logging-api was stopped will be connected
        log1.info("After restarting pax-logging-api (log1)");
        osgiLog.log(LogService.LOG_INFO, "After restarting pax-logging-api");

        // another logger obtained is normally bridged to Logback
        Logger log2 = LoggerFactory.getLogger(this.getClass());
        log2.info("After restarting pax-logging-api (log2)");

        List<String> lines = readLines();
        List<String> lines2 = lines.stream().map(l -> l.length() > 13 ? l.substring(13) : l)
                .collect(Collectors.toList());

        assertTrue("line from TTLLLayout", lines2.contains("[main] INFO org.ops4j.pax.logging.it.LogbackRestartPaxLoggingApiIntegrationTest - Before restarting pax-logging-api"));
        assertTrue("line from TTLLLayout (log service)", lines2.contains("[main] INFO PaxExam-Probe - Before restarting pax-logging-api"));
        assertTrue("line from TTLLLayout when pax-logging-api is being stopped", lines2.contains("[main] INFO org.ops4j.pax.logging.internal.Activator - Disabling SLF4J API support."));
        assertTrue("line from DefaultServiceLog when pax-logging-api is stopped", lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.LogbackRestartPaxLoggingApiIntegrationTest] INFO : When pax-logging-api is stopped"));
        assertTrue("line from DefaultServiceLog when pax-logging-api is stopped (new logger)", lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.LogbackRestartPaxLoggingApiIntegrationTest] INFO : When pax-logging-api is stopped (log1)"));
        assertTrue("line from TTLLLayout when pax-logging-api is stopped (log service)", lines2.contains("[main] INFO PaxExam-Probe - When pax-logging-api is stopped"));
        assertTrue("line from TTLLLayout when pax-logging-api is being started", lines2.contains("[main] INFO org.ops4j.pax.logging.internal.Activator - Enabling SLF4J API support."));
        assertTrue("line from DefaultServiceLog when pax-logging-api is started", lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.LogbackRestartPaxLoggingApiIntegrationTest] INFO : After restarting pax-logging-api"));
        assertTrue("line from TTLLLayout when pax-logging-api is started (new logger)", lines2.contains("[main] INFO org.ops4j.pax.logging.it.LogbackRestartPaxLoggingApiIntegrationTest - After restarting pax-logging-api (log1)"));
        assertTrue("line from TTLLLayout when pax-logging-api is started", lines2.contains("[main] INFO org.ops4j.pax.logging.it.LogbackRestartPaxLoggingApiIntegrationTest - After restarting pax-logging-api (log2)"));
        assertTrue("line from TTLLLayout when pax-logging-api is started (log service)", lines2.contains("[main] INFO PaxExam-Probe - After restarting pax-logging-api"));
    }

}
