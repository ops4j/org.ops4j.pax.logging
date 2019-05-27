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

import org.apache.logging.log4j.status.StatusLogger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;

import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.OptionUtils.combine;

@RunWith(PaxExam.class)
public class Log4J2WithDefaultConfigurationIntegrationTest extends AbstractControlledIntegrationTestBase {

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
    public void logLog() {
        // threshold is controlled by PaxLoggingConstants.LOGGING_CFG_DEFAULT_LOG_LEVEL context property
        StatusLogger logLog = StatusLogger.getLogger();
        logLog.debug("StatusLogger debug");
        logLog.warn("StatusLogger warn");
        logLog.error("StatusLogger error");

        List<String> lines = readLines();

        // we used org.apache.logging.log4j.status.StatusLogger directly, so the bundle in log record is pax-logging-api
        assertTrue(lines.stream().anyMatch(l -> l.contains("org.ops4j.pax.logging.pax-logging-api [log4j2] DEBUG : StatusLogger debug")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("org.ops4j.pax.logging.pax-logging-api [log4j2] WARN : StatusLogger warn")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("org.ops4j.pax.logging.pax-logging-api [log4j2] ERROR : StatusLogger error")));

        assertTrue(lines.contains("org.ops4j.pax.logging.pax-logging-api [log4j2] INFO : Log4J2 configured using default configuration. Ignored FQCN: org.apache.logging.log4j.spi.AbstractLogger"));
    }

}
