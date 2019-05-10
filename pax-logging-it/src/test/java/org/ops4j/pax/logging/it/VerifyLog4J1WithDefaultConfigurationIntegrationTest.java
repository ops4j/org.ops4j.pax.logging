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
import org.ops4j.pax.logging.spi.support.DefaultServiceLog;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VerifyLog4J1WithDefaultConfigurationIntegrationTest extends AbstractVerifyIntegrationTestBase {

    @Test
    public void logVerification() throws IOException, InterruptedException {
        List<String> lines = awaitReport("org.ops4j.pax.logging.it.Log4J1WithDefaultConfigurationIntegrationTest-output.txt");

        // verification of LogLog messages
        assertTrue(lines.stream().anyMatch(l -> l.contains("LogLog debug")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("LogLog warn")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("LogLog error")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("[log4j] DEBUG : Trying to find [log4j.xml] using org.ops4j.pax.logging.pax-logging-service")));

        // verification of API usage default TTCL layout
        assertTrue(lines.contains("[main] ERROR org.ops4j.pax.logging.it.Log4J1WithDefaultConfigurationIntegrationTest - Log4J1WithDefaultConfigurationIntegrationTest/ERROR"));
        assertTrue(lines.contains("[main] WARN org.ops4j.pax.logging.it.Log4J1WithDefaultConfigurationIntegrationTest - Log4J1WithDefaultConfigurationIntegrationTest/WARN"));
        assertTrue(lines.contains("[main] INFO org.ops4j.pax.logging.it.Log4J1WithDefaultConfigurationIntegrationTest - Log4J1WithDefaultConfigurationIntegrationTest/INFO"));
        assertTrue(lines.contains("[main] DEBUG org.ops4j.pax.logging.it.Log4J1WithDefaultConfigurationIntegrationTest - Log4J1WithDefaultConfigurationIntegrationTest/DEBUG"));
        assertFalse(lines.contains("[main] TRACE org.ops4j.pax.logging.it.Log4J1WithDefaultConfigurationIntegrationTest - Log4J1WithDefaultConfigurationIntegrationTest/TRACE"));
    }

}
