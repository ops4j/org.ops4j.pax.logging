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
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.frameworkProperty;
import static org.ops4j.pax.exam.OptionUtils.combine;

@RunWith(PaxExam.class)
public class Log4J2MetaIntegrationTest extends AbstractStdoutInterceptingIntegrationTestBase {

    @Inject
    private ConfigurationAdmin cm;

    @Configuration
    public Option[] configure() throws IOException {
        return combine(
                combine(baseConfigure(), defaultLoggingConfig()),

                // every log with level higher or equal to DEBUG (i.e., not TRACE) will be logged
                frameworkProperty(PaxLoggingConstants.LOGGING_CFG_DEFAULT_LOG_LEVEL).value("DEBUG"),

                paxLoggingApi(),
                paxLoggingLog4J2(),
                configAdmin(),
                eventAdmin()
        );
    }

    @Test
    public void statusTrace() {
        Helpers.updateLoggingConfig(context, cm, Helpers.LoggingLibrary.LOG4J2_PROPERTIES, "meta1");

        LoggerFactory.getLogger(getClass().getName()).info("Checking Log4J2 almost empty configuration");

        // logs from FileServiceLog (StatusLogger and default/fallback logging)
        List<String> linesFile = readLines("target/logs-default/Log4J2MetaIntegrationTest.log");
        // logs from captured stdout used by default console appender from (almost) empty log4j2 configuration
        List<String> linesStdout = readLines(13);

        assertTrue(linesStdout.contains("[main] INFO  org.ops4j.pax.logging.it.Log4J2MetaIntegrationTest - Checking Log4J2 almost empty configuration"));
        assertTrue(linesFile.stream().anyMatch(l -> l.startsWith("org.ops4j.pax.logging.pax-logging-api [log4j2] INFO : Log4J2 configured using configuration from org.ops4j.pax.logging PID.")));
        assertTrue(linesFile.stream().anyMatch(l -> l.contains("org.ops4j.pax.logging.pax-logging-api [log4j2] WARN : No Root logger was configured, creating default ERROR-level Root logger with Console appender")));

        // Now let's change the configuration to one where status=warn.
        Helpers.updateLoggingConfig(context, cm, Helpers.LoggingLibrary.LOG4J2_PROPERTIES, "meta2");

        LoggerFactory.getLogger(getClass().getName()).info("Checking Log4J2 almost empty configuration");

        // logs from FileServiceLog (StatusLogger and default/fallback logging)
        List<String> linesFile2 = readLines("target/logs-default/Log4J2MetaIntegrationTest.log");
        linesFile2 = linesFile2.subList(linesFile.size(), linesFile2.size());
        // logs from captured stdout used by default console appender from (almost) empty log4j2 configuration
        List<String> linesStdout2 = readLines(13);
        linesFile2 = linesStdout2.subList(linesStdout.size(), linesStdout2.size());

        assertTrue(linesStdout2.contains("[main] INFO  org.ops4j.pax.logging.it.Log4J2MetaIntegrationTest - Checking Log4J2 almost empty configuration"));
        assertFalse(linesFile2.stream().anyMatch(l -> l.startsWith("org.ops4j.pax.logging.pax-logging-api [log4j2] INFO : Log4J2 configured using configuration from org.ops4j.pax.logging PID.")));
        assertTrue(linesFile.stream().anyMatch(l -> l.contains("org.ops4j.pax.logging.pax-logging-api [log4j2] WARN : No Root logger was configured, creating default ERROR-level Root logger with Console appender")));
    }

}
