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

import org.apache.log4j.helpers.LogLog;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.logging.PaxLoggingConstants;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.frameworkProperty;
import static org.ops4j.pax.exam.OptionUtils.combine;

@RunWith(PaxExam.class)
public class Log4J1WithDefaultConfigurationAndNoLog4jDebugIntegrationTest extends AbstractControlledIntegrationTestBase {

    @Configuration
    public Option[] configure() throws IOException {
        return combine(
                combine(baseConfigure(), defaultLoggingConfig()),

                // disable debug logging for pax-logging (and as consequence - for Log4J1 itself)
                frameworkProperty(PaxLoggingConstants.LOGGING_CFG_DEFAULT_LOG_LEVEL).value("WARN"),

                paxLoggingApi(),
                paxLoggingLog4J1(),
                configAdmin(),
                eventAdmin()
        );
    }

    @Test
    public void logLog() {
        // threshold is controlled by PaxLoggingConstants.LOGGING_CFG_DEFAULT_LOG_LEVEL context property
        LogLog.debug("LogLog debug");
        LogLog.warn("LogLog warn");
        LogLog.error("LogLog error");

        List<String> lines = readLines();

        // verification of LogLog messages
        assertFalse(lines.stream().anyMatch(l -> l.contains("LogLog debug")));
        assertTrue(lines.contains("org.ops4j.pax.logging.pax-logging-api [log4j] WARN : LogLog warn"));
        assertTrue(lines.contains("org.ops4j.pax.logging.pax-logging-api [log4j] ERROR : LogLog error"));
        assertFalse(lines.stream().anyMatch(l -> l.contains("DEBUG : Trying to find [log4j.xml] using")));
    }

}
