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
import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.logging.it.support.Helpers;
import org.osgi.service.cm.ConfigurationAdmin;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.OptionUtils.combine;

@RunWith(PaxExam.class)
public class Log4J2UpdateJULLoggerLevelsIntegrationTest extends AbstractStdoutInterceptingIntegrationTestBase {

    @Inject
    private ConfigurationAdmin cm;

    @Configuration
    public Option[] configure() throws IOException {
        return combine(
                combine(baseConfigure(), defaultLoggingConfig()),

                paxLoggingApi(),
                paxLoggingLog4J2(),
                configAdmin(),
                eventAdmin()
        );
    }

    @Test
    public void julLevels() {
        Helpers.updateLoggingConfig(context, cm, Helpers.LoggingLibrary.LOG4J2_PROPERTIES, "update.jul");

        java.util.logging.Logger l1 = java.util.logging.Logger.getLogger("l1");
        java.util.logging.Logger l2 = java.util.logging.Logger.getLogger("l2");
        java.util.logging.Logger l3s = java.util.logging.Logger.getLogger("l3.sub");

        l1.info("INFO using l1 before");
        l2.info("INFO using l2 before");
        l3s.fine("FINE using l3.sub before");

        Helpers.updateLoggingConfig(context, cm, Helpers.LoggingLibrary.LOG4J2_PROPERTIES, "update.jul", props -> {
            // swap the levels
            props.put("log4j2.logger.l1.level", "debug");
            props.put("log4j2.logger.l2.level", "warn");
            props.put("log4j2.logger.l3.level", "warn");
        });

        l1.info("INFO using l1 after");
        l2.info("INFO using l2 after");
        l3s.fine("FINE using l3.sub after");
        java.util.logging.Logger l3s2 = java.util.logging.Logger.getLogger("l3.sub2");
        l3s2.fine("FINE using l3.sub2 after");

        List<String> lines = readLines();

        assertFalse(lines.contains("l1/org.ops4j.pax.logging.it.Log4J2UpdateJULLoggerLevelsIntegrationTest [INFO] INFO using l1 before"));
        assertTrue(lines.contains("l2/org.ops4j.pax.logging.it.Log4J2UpdateJULLoggerLevelsIntegrationTest [INFO] INFO using l2 before"));
        assertTrue(lines.contains("l3.sub/org.ops4j.pax.logging.it.Log4J2UpdateJULLoggerLevelsIntegrationTest [DEBUG] FINE using l3.sub before"));
        assertTrue(lines.contains("l1/org.ops4j.pax.logging.it.Log4J2UpdateJULLoggerLevelsIntegrationTest [INFO] INFO using l1 after"));
        assertFalse(lines.contains("l2/org.ops4j.pax.logging.it.Log4J2UpdateJULLoggerLevelsIntegrationTest [INFO] INFO using l2 after"));
        assertFalse(lines.contains("l3.sub/org.ops4j.pax.logging.it.Log4J2UpdateJULLoggerLevelsIntegrationTest [DEBUG] FINE using l3.sub after"));
        assertFalse(lines.contains("l3.sub2/org.ops4j.pax.logging.it.Log4J2UpdateJULLoggerLevelsIntegrationTest [DEBUG] FINE using l3.sub2 after"));
    }

}
