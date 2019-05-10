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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base test class for tests that verify output of real pax-exam tests.
 */
public class AbstractVerifyIntegrationTestBase {

    public static Logger LOG = LoggerFactory.getLogger(AbstractVerifyIntegrationTestBase.class);

    @Rule
    public TestName testName = new TestName();

    // location of failsafe test reports - I know it's environment awareness,
    // but it's not a pure unit test after all
    protected File base = new File("target/failsafe-reports");

    @Before
    public void beforeEach() {
        LOG.info("========== Running {}.{}() ==========", getClass().getName(), testName.getMethodName());
    }

    @After
    public void afterEach() {
        LOG.info("========== Finished {}.{}() ==========", getClass().getName(), testName.getMethodName());
    }

    /**
     * Actively waits till failsafe report is available (knowing a bit about the location and format,
     * but hey, we're testing logging framework after all)
     * @param report
     * @return
     */
    protected List<String> awaitReport(String report) throws IOException {
        File reportFile = new File(base, report);
        while (true) {
            try {
                List<String> lines = Files.readAllLines(reportFile.toPath());
                if (lines.get(lines.size() - 1).contains("org.ops4j.pax.exam.spi.reactors.ReactorManager - suite finished")) {
                    return lines;
                }
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted", e);
            }
        }
    }

}
