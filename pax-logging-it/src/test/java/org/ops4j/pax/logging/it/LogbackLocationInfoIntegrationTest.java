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
import org.ops4j.pax.logging.PaxLoggingService;
import org.ops4j.pax.logging.it.support.Helpers;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.log.LogService;
import org.osgi.service.log.LoggerFactory;

import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.OptionUtils.combine;

/**
 * Checking if we get proper location info (class/method/file/line) through different logging API.
 *
 * It heavily depends on proper handling of Fully Qualified Class Name in loggers.
 */
@RunWith(PaxExam.class)
public class LogbackLocationInfoIntegrationTest extends AbstractStdoutInterceptingIntegrationTestBase {

    @Inject
    private ConfigurationAdmin cm;

    @Inject
    private LogService osgiLogService;

    @Configuration
    public Option[] configure() throws IOException {
        return combine(
                combine(baseConfigure(), defaultLoggingConfig()),

                paxLoggingApi(),
                paxLoggingLogback(),
                configAdmin(),
                eventAdmin()
        );
    }

    @Test
    public void locationInfo() {
        Helpers.updateLoggingConfig(context, cm, Helpers.LoggingLibrary.LOGBACK, "location");

        String name = "my.logger";

        // 1. SLF4j
        org.slf4j.LoggerFactory.getLogger(name).info("INFO using SLF4J");
        // 2. Commons Logging
        org.apache.commons.logging.LogFactory.getLog(name).info("INFO using Commons Logging");
        // 3. JULI Logging
        org.apache.juli.logging.LogFactory.getLog(name).info("INFO using Juli Logging");
        // 4. Avalon Logging
        org.ops4j.pax.logging.avalon.AvalonLogFactory.getLogger(name).info("INFO using Avalon Logging");
        // 5. JBoss Logging
        org.jboss.logging.Logger.getLogger(name).info("INFO using JBoss Logging");
        // 6. Log4J1
        org.apache.log4j.Logger.getLogger(name).info("INFO using Log4Jv1");
        // 7. Logback - only behind SLF4J
        //
        // 8. Log4J2
        org.apache.logging.log4j.LogManager.getLogger(name).info("INFO using Log4Jv2");
        // 9. JUL - extra handling without a pax-logging specific facade and shadowing. Only handler redirection
        java.util.logging.Logger.getLogger(name).info("INFO using java.util.logging");
        // 10. OSGi
        osgiLogService.log(LogService.LOG_INFO, "INFO using LogService");
        // 11. PaxLoggingService itself
        ServiceReference<PaxLoggingService> sr = context.getServiceReference(PaxLoggingService.class);
        PaxLoggingService loggingService = context.getService(sr);
        loggingService.log(LogService.LOG_INFO, "INFO using PaxLoggingService");
        // 12. OSGi R7
        ServiceReference<LoggerFactory> sr2 = context.getServiceReference(LoggerFactory.class);
        LoggerFactory lf = context.getService(sr2);
        lf.getLogger(name).info("INFO using OSGi R7");

        List<String> lines = readLines("target/logs-logback/location-file-appender.log");

        // sorry, but it's important ;) I want these line numbers
        assertTrue(lines.contains("org.ops4j.pax.logging.it.LogbackLocationInfoIntegrationTest | locationInfo | LogbackLocationInfoIntegrationTest.java | 73 : [INFO] INFO using SLF4J"));
        assertTrue(lines.contains("org.ops4j.pax.logging.it.LogbackLocationInfoIntegrationTest | locationInfo | LogbackLocationInfoIntegrationTest.java | 75 : [INFO] INFO using Commons Logging"));
        assertTrue(lines.contains("org.ops4j.pax.logging.it.LogbackLocationInfoIntegrationTest | locationInfo | LogbackLocationInfoIntegrationTest.java | 77 : [INFO] INFO using Juli Logging"));
        assertTrue(lines.contains("org.ops4j.pax.logging.it.LogbackLocationInfoIntegrationTest | locationInfo | LogbackLocationInfoIntegrationTest.java | 79 : [INFO] INFO using Avalon Logging"));
        assertTrue(lines.contains("org.ops4j.pax.logging.it.LogbackLocationInfoIntegrationTest | locationInfo | LogbackLocationInfoIntegrationTest.java | 81 : [INFO] INFO using JBoss Logging"));
        assertTrue(lines.contains("org.ops4j.pax.logging.it.LogbackLocationInfoIntegrationTest | locationInfo | LogbackLocationInfoIntegrationTest.java | 83 : [INFO] INFO using Log4Jv1"));
        // logback is skipped
        assertTrue(lines.contains("org.ops4j.pax.logging.it.LogbackLocationInfoIntegrationTest | locationInfo | LogbackLocationInfoIntegrationTest.java | 87 : [INFO] INFO using Log4Jv2"));
        assertTrue(lines.contains("org.ops4j.pax.logging.it.LogbackLocationInfoIntegrationTest | locationInfo | LogbackLocationInfoIntegrationTest.java | 89 : [INFO] INFO using java.util.logging"));
        assertTrue(lines.contains("org.ops4j.pax.logging.it.LogbackLocationInfoIntegrationTest | locationInfo | LogbackLocationInfoIntegrationTest.java | 91 : [INFO] INFO using LogService"));
        assertTrue(lines.contains("org.ops4j.pax.logging.it.LogbackLocationInfoIntegrationTest | locationInfo | LogbackLocationInfoIntegrationTest.java | 95 : [INFO] INFO using PaxLoggingService"));
        assertTrue(lines.contains("org.ops4j.pax.logging.it.LogbackLocationInfoIntegrationTest | locationInfo | LogbackLocationInfoIntegrationTest.java | 99 : [INFO] INFO using OSGi R7"));
    }

}
