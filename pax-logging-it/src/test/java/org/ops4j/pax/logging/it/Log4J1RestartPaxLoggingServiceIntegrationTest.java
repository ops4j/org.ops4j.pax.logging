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
public class Log4J1RestartPaxLoggingServiceIntegrationTest extends AbstractStdoutInterceptingIntegrationTestBase {

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
                paxLoggingLog4J1(),
                configAdmin(),
                eventAdmin(),

                frameworkProperty(PaxLoggingConstants.LOGGING_CFG_DEFAULT_LOG_LEVEL).value("DEBUG"),
                frameworkProperty(PaxLoggingConstants.LOGGING_CFG_FRAMEWORK_EVENTS_LOG_LEVEL).value("DISABLED")
        );
    }

    @Test
    public void restartPaxLoggingService() throws BundleException {
        Logger log = LoggerFactory.getLogger(this.getClass());
        Bundle paxLoggingService = Helpers.paxLoggingService(context);

        // TTCCLayout - everything's working
        log.info("Before restarting pax-logging-service");
        // TTCCLayout - everything's working
        osgiLog.log(LogService.LOG_INFO, "Before restarting pax-logging-service");

        paxLoggingService.stop(Bundle.STOP_TRANSIENT);

        // goes to System.out-based default/fallback logger because logger has delegate replaced using
        // pax-logging-api mechanisms
        log.info("When pax-logging-service is stopped");

        // to file, but with
        // "org.ops4j.pax.logging.pax-logging-service [log4j] WARN : No appenders could be found for logger (PaxExam-Probe)."
        // message, because LogService is obtained earlier is still delegating directly to Log4J1
        // which is not unconfigured
        osgiLog.log(LogService.LOG_INFO, "When pax-logging-service is stopped (log service)");

        // LogService is no longer available
        assertNull(context.getServiceReference(LogService.class));

        // This logger was already cached by pax-logging-api, so no changes
        Logger log1 = LoggerFactory.getLogger(this.getClass());
        log1.info("When pax-logging-service is stopped (log1)");

        // Logger that wasn't cached - but normally delegating to DefaultServiceLog
        Logger log2 = LoggerFactory.getLogger(this.getClass().getName() + "Ex");
        log2.info("When pax-logging-service is stopped (log2)");

        paxLoggingService.start(Bundle.START_TRANSIENT);

        // These loggers immediately switched to delegate to Log4J1, because pax-logging-api
        // detected registration of pax-logging-service based PaxLoggingService
        log.info("After restarting pax-logging-service");
        log1.info("After restarting pax-logging-service (log1)");
        // old reference, but switched to reconfigured Log4J1
        osgiLog.log(LogService.LOG_INFO, "After restarting pax-logging-service (log service old ref)");

        ServiceReference<LogService> ref = context.getServiceReference(LogService.class);
        assertNotNull(ref);
        context.getService(ref).log(LogService.LOG_INFO, "After restarting pax-logging-service (log service new ref)");

        Logger log3 = LoggerFactory.getLogger(this.getClass());
        log3.info("After restarting pax-logging-service (log3)");

        List<String> lines = readLines();

        assertTrue("TTCCLayout", lines.contains("[main] INFO org.ops4j.pax.logging.it.Log4J1RestartPaxLoggingServiceIntegrationTest - Before restarting pax-logging-service"));
        assertTrue("TTCCLayout", lines.contains("[main] INFO PaxExam-Probe - Before restarting pax-logging-service"));
        assertTrue("Default Logger", lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.Log4J1RestartPaxLoggingServiceIntegrationTest] INFO : When pax-logging-service is stopped"));
        assertFalse("old LogService reference should not work", lines.stream().anyMatch(l -> l.contains("When pax-logging-service is stopped (log service)")));
        assertTrue("Default Logger", lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.Log4J1RestartPaxLoggingServiceIntegrationTest] INFO : When pax-logging-service is stopped (log1)"));
        assertTrue("Default Logger", lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.Log4J1RestartPaxLoggingServiceIntegrationTestEx] INFO : When pax-logging-service is stopped (log2)"));
        assertTrue("Reconfiguration", lines.contains("org.ops4j.pax.logging.pax-logging-service [log4j] DEBUG : Finished configuring."));
        assertTrue("TTCCLayout", lines.contains("[main] INFO org.ops4j.pax.logging.it.Log4J1RestartPaxLoggingServiceIntegrationTest - After restarting pax-logging-service"));
        assertTrue("TTCCLayout", lines.contains("[main] INFO org.ops4j.pax.logging.it.Log4J1RestartPaxLoggingServiceIntegrationTest - After restarting pax-logging-service (log1)"));
        assertTrue("TTCCLayout", lines.contains("[main] INFO PaxExam-Probe - After restarting pax-logging-service (log service old ref)"));
        assertTrue("TTCCLayout", lines.contains("[main] INFO PaxExam-Probe - After restarting pax-logging-service (log service new ref)"));
        assertTrue("TTCCLayout", lines.contains("[main] INFO org.ops4j.pax.logging.it.Log4J1RestartPaxLoggingServiceIntegrationTest - After restarting pax-logging-service (log3)"));
    }

}
