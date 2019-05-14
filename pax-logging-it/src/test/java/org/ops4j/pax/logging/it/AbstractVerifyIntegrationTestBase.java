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

import static org.ops4j.pax.logging.it.AbstractControlledIntegrationTestBase.LOG_DIR;

/**
 * Base test class for tests that verify output of real pax-exam tests.
 */
public class AbstractVerifyIntegrationTestBase {

    public static Logger LOG = LoggerFactory.getLogger(AbstractVerifyIntegrationTestBase.class);

    @Rule
    public TestName testName = new TestName();

    @Before
    public void beforeEach() {
        LOG.info("========== Running {}.{}() ==========", getClass().getName(), testName.getMethodName());
    }

    @After
    public void afterEach() {
        LOG.info("========== Finished {}.{}() ==========", getClass().getName(), testName.getMethodName());
    }

    /**
     * Reads assumed log output from default/fallback logger configured to write to file. File name is assumed
     * to be this class' name without {@code Verify} prefix
     * @return
     */
    protected List<String> readLogs() throws IOException {
        File reportFile = new File(LOG_DIR, getClass().getSimpleName().replaceAll("^Verify", "") + ".log");
        return Files.readAllLines(reportFile.toPath());
    }

}
