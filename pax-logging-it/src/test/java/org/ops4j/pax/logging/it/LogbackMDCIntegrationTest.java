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

import org.apache.logging.log4j.ThreadContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.logging.PaxLoggingService;
import org.ops4j.pax.logging.it.support.Helpers;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.OptionUtils.combine;

@RunWith(PaxExam.class)
public class LogbackMDCIntegrationTest extends AbstractStdoutInterceptingIntegrationTestBase {

    @Inject
    private ConfigurationAdmin cm;

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
    public void mdc() {
        Helpers.updateLoggingConfig(context, cm, Helpers.LoggingLibrary.LOGBACK, "mdc");

        Logger log = LoggerFactory.getLogger("my.logger");
        MDC.put("country", "Equestria");
        log.info("Hello");

        // these should be available only when logging
        assertNull(MDC.get("bundle.id"));
        assertNull(MDC.get("bundle.name"));

        // this should not be cleared
        assertThat(MDC.get("country"), equalTo("Equestria"));

        // this should be available also through different logging API!

        // Log4J1 API
        assertThat(org.apache.log4j.MDC.get("country"), equalTo("Equestria"));
        // Log4J2
        assertThat(ThreadContext.get("country"), equalTo("Equestria"));
        // JBoss Logging
        assertThat(org.jboss.logging.MDC.get("country"), equalTo("Equestria"));

        // through PaxLoggingService.getPaxContext()
        ServiceReference<PaxLoggingService> sr = context.getServiceReference(PaxLoggingService.class);
        assertNotNull(sr);
        PaxLoggingService loggingService = context.getService(sr);
        assertNotNull(loggingService);
        assertThat(loggingService.getPaxContext().get("country"), equalTo("Equestria"));

        List<String> lines = readLines("target/logs-logback/mdc-file-appender.log");

        assertTrue(lines.stream().anyMatch(l ->
                l.contains("my.logger/org.ops4j.pax.logging.it.LogbackMDCIntegrationTest")
                        && l.contains("bundle.name=PaxExam-Probe")
                        && l.contains("country=Equestria")
        ));
    }

}
