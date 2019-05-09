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

/**
 * This test relies heavily on {@code <runOrder>alphabetical</runOrder>} and
 * {@code <redirectTestOutputToFile>true</redirectTestOutputToFile>} settings of maven-failsafe plugin.
 * It reads the output after we're sure it's flushed to file. {@link DefaultLogIntegrationTest} uses pax-logging-api
 * bundle only, so {@link DefaultServiceLog} writing to stdout is used. We just
 * read the file and verify that it contains proper entries.
 */
public class VerifyAllLoggingFacadesIntegrationTest extends AbstractVerifyIntegrationTestBase {

    @Test
    public void logVerification() throws IOException, InterruptedException {
        List<String> lines = awaitReport("org.ops4j.pax.logging.it.AllLoggingFacadesIntegrationTest-output.txt");

        assertTrue(lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.test] INFO : INFO using SLF4J"));
        assertTrue(lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.test] INFO : INFO using Commons Logging"));
        assertTrue(lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.test] INFO : INFO using Juli Logging"));
        assertTrue(lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.test] INFO : INFO using Avalon Logging"));
        // here, FQCN is passed, which is explicitly added to message by DefaultServiceLog
        assertTrue(lines.stream().anyMatch(l -> l.startsWith("PaxExam-Probe [org.ops4j.pax.logging.it.test] INFO : INFO using JBoss Logging")));
        assertTrue(lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.test] INFO : INFO using Log4Jv1"));
        // here, FQCN is passed, which is explicitly added to message by DefaultServiceLog
        assertTrue(lines.stream().anyMatch(l -> l.startsWith("PaxExam-Probe [org.ops4j.pax.logging.it.test] INFO : INFO using Log4Jv2")));
        assertTrue(lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.test] INFO : INFO using java.util.logging"));
    }

}
