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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.OptionUtils.combine;

/**
 * <p>Checking if we get proper location info (class/method/file/line) through different logging API.</p>
 * <p>It heavily depends on proper handling of Fully Qualified Class Name in loggers.</p>
 */
@RunWith(PaxExam.class)
public class Log4J2LocationInfoIntegrationTest extends AbstractStdoutInterceptingIntegrationTestBase {

    @Inject
    private ConfigurationAdmin cm;


    @Inject
    private org.knopflerfish.service.log.LogService fishLogService;

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
    public void locationInfo() {
        Helpers.updateLoggingConfig(context, cm, Helpers.LoggingLibrary.LOG4J2_PROPERTIES, "location");

        ServiceReference<LogService> ref = context.getServiceReference(LogService.class);
        assertNotNull(ref);
        LogService osgiLogService = context.getService(ref);

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
        // 6. Knopflerfish - no special facade
        fishLogService.log(LogService.LOG_INFO, "INFO using Knopflerfish");
        // 7. Log4J1 API
        org.apache.log4j.Logger.getLogger(name).info("INFO using Log4Jv1");
        // 8. Logback - only behind SLF4J
        //
        // 9. Log4J2
        org.apache.logging.log4j.LogManager.getLogger(name).info("INFO using Log4Jv2");
        // 10. JUL - extra handling without a pax-logging specific facade and shadowing. Only handler redirection
        java.util.logging.Logger.getLogger(name).info("INFO using java.util.logging");
        // 11. OSGi
        osgiLogService.log(LogService.LOG_INFO, "INFO using LogService");
        // 12. PaxLoggingService itself
        ServiceReference<PaxLoggingService> sr = context.getServiceReference(PaxLoggingService.class);
        PaxLoggingService loggingService = context.getService(sr);
        loggingService.log(LogService.LOG_INFO, "INFO using PaxLoggingService");

        List<String> lines = readLines("target/logs-log4j2/location-file-appender.log");

        // sorry, but it's important ;) I want these line numbers
        assertTrue(lines.contains("org.ops4j.pax.logging.it.Log4J2LocationInfoIntegrationTest | locationInfo | Log4J2LocationInfoIntegrationTest.java | 77 : [INFO] INFO using SLF4J"));
        assertTrue(lines.contains("org.ops4j.pax.logging.it.Log4J2LocationInfoIntegrationTest | locationInfo | Log4J2LocationInfoIntegrationTest.java | 79 : [INFO] INFO using Commons Logging"));
        assertTrue(lines.contains("org.ops4j.pax.logging.it.Log4J2LocationInfoIntegrationTest | locationInfo | Log4J2LocationInfoIntegrationTest.java | 81 : [INFO] INFO using Juli Logging"));
        assertTrue(lines.contains("org.ops4j.pax.logging.it.Log4J2LocationInfoIntegrationTest | locationInfo | Log4J2LocationInfoIntegrationTest.java | 83 : [INFO] INFO using Avalon Logging"));
        assertTrue(lines.contains("org.ops4j.pax.logging.it.Log4J2LocationInfoIntegrationTest | locationInfo | Log4J2LocationInfoIntegrationTest.java | 85 : [INFO] INFO using JBoss Logging"));
        assertTrue(lines.contains("org.ops4j.pax.logging.it.Log4J2LocationInfoIntegrationTest | locationInfo | Log4J2LocationInfoIntegrationTest.java | 87 : [INFO] INFO using Knopflerfish"));
        assertTrue(lines.contains("org.ops4j.pax.logging.it.Log4J2LocationInfoIntegrationTest | locationInfo | Log4J2LocationInfoIntegrationTest.java | 89 : [INFO] INFO using Log4Jv1"));
        // logback is skipped
        assertTrue(lines.contains("org.ops4j.pax.logging.it.Log4J2LocationInfoIntegrationTest | locationInfo | Log4J2LocationInfoIntegrationTest.java | 93 : [INFO] INFO using Log4Jv2"));
        assertTrue(lines.contains("org.ops4j.pax.logging.it.Log4J2LocationInfoIntegrationTest | locationInfo | Log4J2LocationInfoIntegrationTest.java | 95 : [INFO] INFO using java.util.logging"));
        assertTrue(lines.contains("org.ops4j.pax.logging.it.Log4J2LocationInfoIntegrationTest | locationInfo | Log4J2LocationInfoIntegrationTest.java | 97 : [INFO] INFO using LogService"));
        assertTrue(lines.contains("org.ops4j.pax.logging.it.Log4J2LocationInfoIntegrationTest | locationInfo | Log4J2LocationInfoIntegrationTest.java | 101 : [INFO] INFO using PaxLoggingService"));
    }

}
