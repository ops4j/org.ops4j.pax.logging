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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.osgi.framework.BundleException;

/**
 * Base class that allows replacing {@code System.out} and after test is run - restore it to previous value.
 * {@link #readLines()} gives access to what was written in between.
 */
public class AbstractStdoutInterceptingIntegrationTestBase extends AbstractControlledIntegrationTestBase {

    private PrintStream stdout;
    private ByteArrayOutputStream buffer;

    @Before
    public void hijackStdout() throws BundleException {
        stdout = System.out;
        buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer, true));
    }

    @After
    public void restoreStdout() {
        System.setOut(stdout);
    }

    /**
     * Reads log lines intercepted when writing to {@code System.out}.
     * @return
     */
    protected List<String> readLines() {
        return readLines(new ByteArrayInputStream(buffer.toByteArray()), 0);
    }

    /**
     * Reads log lines intercepted when writing to {@code System.out}.
     * @param trim how many chars from each line to trim?
     * @return
     */
    protected List<String> readLines(int trim) {
        return readLines(new ByteArrayInputStream(buffer.toByteArray()), trim);
    }

}
