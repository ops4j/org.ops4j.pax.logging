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
public class Log4J2RestartBothPaxLoggingBundlesIntegrationTest extends AbstractStdoutInterceptingIntegrationTestBase {

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
    public void restartBothBundles() throws BundleException {
        Logger log = LoggerFactory.getLogger(this.getClass());
        Bundle paxLoggingApi = Helpers.paxLoggingApi(context);
        Bundle paxLoggingLog4j2 = Helpers.paxLoggingLog4j2(context);

        // DEFAULT_PATTERN - everything's working
        log.info("Before restarting");
        ServiceReference<LogService> ref = context.getServiceReference(LogService.class);
        assertNotNull(ref);
        LogService osgiLog = context.getService(ref);
        osgiLog.log(LogService.LOG_INFO, "Before restarting"); // TTCC

        paxLoggingApi.stop(Bundle.STOP_TRANSIENT);
        paxLoggingLog4j2.stop(Bundle.STOP_TRANSIENT);

        // goes to System.out-based default/fallback logger because logger has delegate replaced using
        // pax-logging-api mechanisms - it's no longer kept in static maps in pax-logging-api, so it won't be
        // reconnected to any backend
        log.info("When bundles are stopped");

        // loggers are reconfigured to use org.apache.logging.log4j.core.config.NullConfiguration with OFF level
        osgiLog.log(LogService.LOG_INFO, "When bundles are stopped (log service)");

        // LogService is no longer available
        assertNull(context.getServiceReference(LogService.class));

        // pax-logging-api no longer has manager (only static maps), so System.out-based default/fallback
        // logger will be returned
        Logger log1 = LoggerFactory.getLogger(this.getClass());
        log1.info("When bundles are stopped (log1)");

        paxLoggingLog4j2.start(Bundle.START_TRANSIENT);
        paxLoggingApi.start(Bundle.START_TRANSIENT);

        // This logger won't be reconnected to backend
        log.info("After restarting bundles");
        // But this will be reconnected
        log1.info("After restarting bundles (log1)");
        // old reference, but switched to reconfigured Log4J2
        osgiLog.log(LogService.LOG_INFO, "After restarting bundles (log service old ref)");

        ref = context.getServiceReference(LogService.class);
        assertNotNull(ref);
        context.getService(ref).log(LogService.LOG_INFO, "After restarting bundles (log service new ref)");

        Logger log3 = LoggerFactory.getLogger(this.getClass());
        log3.info("After restarting bundles (log3)");

        List<String> lines = readLines();
        List<String> lines2 = readLines(13);

        assertTrue("DEFAULT_PATTERN", lines2.contains("[main] INFO  org.ops4j.pax.logging.it.Log4J2RestartBothPaxLoggingBundlesIntegrationTest - Before restarting"));
        assertTrue("DEFAULT_PATTERN", lines2.contains("[main] INFO  PaxExam-Probe - Before restarting"));
        assertTrue("DEFAULT_PATTERN", lines2.contains("[main] INFO  org.ops4j.pax.logging.internal.Activator - Disabling SLF4J API support."));
        assertTrue("Default Logger", lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.Log4J2RestartBothPaxLoggingBundlesIntegrationTest] INFO : When bundles are stopped"));
        assertFalse("old LogService reference should not work", lines.contains("When pax-logging-log4j2 is stopped (log service)"));
        assertTrue("Default Logger", lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.Log4J2RestartBothPaxLoggingBundlesIntegrationTest] INFO : When bundles are stopped (log1)"));
        assertTrue("Reconfiguration", lines.stream().anyMatch(l -> l.startsWith("org.ops4j.pax.logging.pax-logging-api [log4j2] INFO : Log4J2 configured using default configuration.")));
        assertTrue("DEFAULT_PATTERN, startup of pax-logging-api", lines2.contains("[main] INFO  org.ops4j.pax.logging.internal.Activator - Enabling Java Util Logging API support."));
        assertTrue("Default Logger", lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.Log4J2RestartBothPaxLoggingBundlesIntegrationTest] INFO : After restarting bundles"));
        assertTrue("DEFAULT_PATTERN", lines2.contains("[main] INFO  org.ops4j.pax.logging.it.Log4J2RestartBothPaxLoggingBundlesIntegrationTest - After restarting bundles (log1)"));
        assertFalse("DEFAULT_PATTERN", lines2.contains("[main] INFO  PaxExam-Probe - After restarting bundles (log service old ref)"));
        assertTrue("DEFAULT_PATTERN", lines2.contains("[main] INFO  PaxExam-Probe - After restarting bundles (log service new ref)"));
        assertTrue("DEFAULT_PATTERN", lines2.contains("[main] INFO  org.ops4j.pax.logging.it.Log4J2RestartBothPaxLoggingBundlesIntegrationTest - After restarting bundles (log3)"));
    }

}
