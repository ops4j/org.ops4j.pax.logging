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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VerifyLog4J1IntegrationTest extends AbstractVerifyIntegrationTestBase {

    @Test
    public void logVerification() throws IOException, InterruptedException {
        List<String> lines = awaitReport("org.ops4j.pax.logging.it.Log4J1IntegrationTest-output.txt");

        assertFalse(lines.stream().anyMatch(l -> l.contains("simplestUsage - TRACE")));
        assertTrue("Line should be printed without MDC",
                lines.stream().anyMatch(l -> l.contains("simplestUsage - INFO") && !l.contains("Equestria")));

        assertTrue(lines.stream().anyMatch(l -> l.contains("loggerAPI - INFO1")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("loggerAPI - INFO2")));
        assertFalse(lines.stream().anyMatch(l -> l.contains("loggerAPI - TRACE1")));
        assertFalse(lines.stream().anyMatch(l -> l.contains("loggerAPI - TRACE2")));

        assertTrue(lines.stream().anyMatch(l -> l.contains("MF: Hello, Hello World!")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("SF: Hello World!")));
    }

}
