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
package org.ops4j.pax.logging.test.slf4j;

import org.junit.Test;
import org.ops4j.pax.logging.spi.support.DefaultServiceLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class LoggingWithoutBackendTest {

    public static Logger LOG = LoggerFactory.getLogger(LoggingWithoutBackendTest.class);

    @Test
    public void justLog() {
        DefaultServiceLog.setLogLevel("TRACE");
        LOG.trace("trace message 1");
        LOG.error("error message 1");
        DefaultServiceLog.setLogLevel("ERROR");
        LOG.trace("trace message 2");
        LOG.error("error message 2");
    }

    @Test
    public void logWithMarkers() {
        Marker m = MarkerFactory.getMarker("m1");

        DefaultServiceLog.setLogLevel("TRACE");
        LOG.trace(m, "trace message 1");
        LOG.error(m, "error message 1");
        LOG.isTraceEnabled();
        LOG.isTraceEnabled(m);
        DefaultServiceLog.setLogLevel("ERROR");
        LOG.trace(m, "trace message 2");
        LOG.error(m, "error message 2");
    }

}
