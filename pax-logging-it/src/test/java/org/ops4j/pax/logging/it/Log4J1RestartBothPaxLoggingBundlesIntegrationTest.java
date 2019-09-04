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
public class Log4J1RestartBothPaxLoggingBundlesIntegrationTest extends AbstractStdoutInterceptingIntegrationTestBase {

    @Inject
    private LogService osgiLog;

    @Override
    public void hijackStdout() throws BundleException {
        super.hijackStdout();
        Helpers.restartPaxLoggingLog4j1(context, true);
    }

    @Configuration
    public Option[] configure() throws IOException {
        // explicit configuration that doesn't use file-based default/fallback logger
        return combine(
                baseConfigure(),

                paxLoggingApi(),
                paxLoggingLog4J1(),
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
        Bundle paxLoggingService = Helpers.paxLoggingLog4j1(context);

        // TTCCLayout - everything's working
        log.info("Before restarting");
        osgiLog.log(LogService.LOG_INFO, "Before restarting"); // TTCC

        paxLoggingApi.stop(Bundle.STOP_TRANSIENT);
        paxLoggingService.stop(Bundle.STOP_TRANSIENT);

        // goes to System.out-based default/fallback logger because logger has delegate replaced using
        // pax-logging-api mechanisms - it's no longer kept in static maps in pax-logging-api, so it won't be
        // reconnected to any backend
        log.info("When bundles are stopped");

        // to file, but with
        // "org.ops4j.pax.logging.pax-logging-log4j1 [log4j] WARN : No appenders could be found for logger
        // (PaxExam-Probe)."
        // message, because LogService is obtained earlier is still delegating directly to Log4J1
        // which is not unconfigured
        osgiLog.log(LogService.LOG_INFO, "When bundles are stopped (log service)");

        // LogService is no longer available
        assertNull(context.getServiceReference(LogService.class));

        // pax-logging-api no longer has manager (only static maps), so System.out-based default/fallback
        // logger will be returned
        Logger log1 = LoggerFactory.getLogger(this.getClass());
        log1.info("When bundles are stopped (log1)");

        paxLoggingService.start(Bundle.START_TRANSIENT);
        paxLoggingApi.start(Bundle.START_TRANSIENT);

        // This logger won't be reconnected to backend
        log.info("After restarting bundles");
        // But this will be reconnected
        log1.info("After restarting bundles (log1)");
        // old reference, but switched to reconfigured Log4J1
        osgiLog.log(LogService.LOG_INFO, "After restarting bundles (log service old ref)");

        ServiceReference<LogService> ref = context.getServiceReference(LogService.class);
        assertNotNull(ref);
        context.getService(ref).log(LogService.LOG_INFO, "After restarting bundles (log service new ref)");

        Logger log3 = LoggerFactory.getLogger(this.getClass());
        log3.info("After restarting bundles (log3)");

        List<String> lines = readLines();

        assertTrue("TTCCLayout", lines.contains("[main] INFO org.ops4j.pax.logging.it.Log4J1RestartBothPaxLoggingBundlesIntegrationTest - Before restarting"));
        assertTrue("TTCCLayout", lines.contains("[main] INFO PaxExam-Probe - Before restarting"));
        assertTrue("TTCCLayout", lines.contains("[main] INFO org.ops4j.pax.logging.internal.Activator - Disabling SLF4J API support."));
        assertTrue("Default Logger", lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.Log4J1RestartBothPaxLoggingBundlesIntegrationTest] INFO : When bundles are stopped"));
        assertFalse("old LogService reference should not work", lines.contains("When pax-logging-log4j1 is stopped (log service)"));
        assertTrue("old LogService reference should not work", lines.contains("org.ops4j.pax.logging.pax-logging-log4j1 [log4j] WARN : No appenders could be found for logger (PaxExam-Probe)."));
        assertTrue("Default Logger", lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.Log4J1RestartBothPaxLoggingBundlesIntegrationTest] INFO : When bundles are stopped (log1)"));
        assertTrue("Reconfiguration", lines.contains("org.ops4j.pax.logging.pax-logging-log4j1 [log4j] DEBUG : Finished configuring."));
        assertTrue("TTCCLayout, startup of pax-logging-api", lines.contains("[main] INFO org.ops4j.pax.logging.internal.Activator - Enabling Java Util Logging API support."));
        assertTrue("Default Logger", lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.Log4J1RestartBothPaxLoggingBundlesIntegrationTest] INFO : After restarting bundles"));
        assertTrue("TTCCLayout", lines.contains("[main] INFO org.ops4j.pax.logging.it.Log4J1RestartBothPaxLoggingBundlesIntegrationTest - After restarting bundles (log1)"));
        assertTrue("TTCCLayout", lines.contains("[main] INFO PaxExam-Probe - After restarting bundles (log service old ref)"));
        assertTrue("TTCCLayout", lines.contains("[main] INFO PaxExam-Probe - After restarting bundles (log service new ref)"));
        assertTrue("TTCCLayout", lines.contains("[main] INFO org.ops4j.pax.logging.it.Log4J1RestartBothPaxLoggingBundlesIntegrationTest - After restarting bundles (log3)"));
    }

}
