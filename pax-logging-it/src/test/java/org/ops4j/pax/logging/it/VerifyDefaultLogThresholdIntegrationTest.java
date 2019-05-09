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
public class VerifyDefaultLogThresholdIntegrationTest extends AbstractVerifyIntegrationTestBase {

    @Test
    public void logVerification() throws IOException, InterruptedException {
        List<String> lines = awaitReport("org.ops4j.pax.logging.it.DefaultLogThresholdIntegrationTest-output.txt");

        // verification of logging BundleEvents
        assertFalse(lines.contains("MySpecialBundleThatShouldTriggerBundleEventWhenInstalling [org.osgi.framework.BundleEvent] DEBUG : BundleEvent INSTALLED"));
        assertFalse(lines.contains("MySpecialBundleThatShouldTriggerBundleEventWhenInstalling [org.osgi.framework.BundleEvent] DEBUG : BundleEvent STARTED"));
        assertFalse(lines.contains("AnotherBundle [org.osgi.framework.BundleEvent] DEBUG : BundleEvent INSTALLED"));
        assertFalse(lines.contains("AnotherBundle [org.osgi.framework.BundleEvent] DEBUG : BundleEvent STARTED"));

        // verification of log levels with fallback logger (stdout, but redirected to file)
        assertTrue(lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.DefaultLogThresholdIntegrationTest] ERROR : DefaultLogThresholdIntegrationTest/ERROR"));
        assertTrue(lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.DefaultLogThresholdIntegrationTest] WARN : DefaultLogThresholdIntegrationTest/WARN"));
        assertTrue(lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.DefaultLogThresholdIntegrationTest] INFO : DefaultLogThresholdIntegrationTest/INFO"));
        assertFalse(lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.DefaultLogThresholdIntegrationTest] DEBUG : DefaultLogThresholdIntegrationTest/DEBUG"));
        assertFalse(lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.DefaultLogThresholdIntegrationTest] TRACE : DefaultLogThresholdIntegrationTest/TRACE"));
    }

}
