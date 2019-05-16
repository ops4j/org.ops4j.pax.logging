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
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;

import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.OptionUtils.combine;

@RunWith(PaxExam.class)
public class LogbackWithDefaultConfigurationIntegrationTest extends AbstractControlledIntegrationTestBase {

    @Configuration
    public Option[] configure() throws IOException {
        return combine(
                combine(baseConfigure(), defaultLoggingConfig()),

                paxLoggingApi(),
                paxLoggingLogback(),
                configAdmin(),
                eventAdmin()
        );
    }

    @Test
    public void logLog() {
        List<String> lines = super.readLines();

        // PaxLoggingService implementation from pax-logging-logback doesn't use helper classes
        // like org.apache.log4j.helpers.LogLog, which in pax-logging-api is changed to delegate to fallback
        // logger. Instead, pax-logging-logback directly calls default/fallback logger to print
        // what was collected inside ch.qos.logback.core.ContextBase.getStatusManager()
        assertTrue(lines.contains("org.ops4j.pax.logging.pax-logging-logback [logback] INFO : Setting up default configuration."));
        assertTrue(lines.contains("org.ops4j.pax.logging.pax-logging-logback [logback] INFO : Logback configured using default configuration."));
    }

}
