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
import org.osgi.framework.BundleException;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.log.LoggerFactory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.ops4j.pax.exam.OptionUtils.combine;

@RunWith(PaxExam.class)
public class Log4J1R7AuditIntegrationTest extends AbstractStdoutInterceptingIntegrationTestBase {

    @Inject
    private LoggerFactory osgiLoggerFactory;

    @Inject
    private ConfigurationAdmin cm;

    @Override
    public void hijackStdout() throws BundleException {
        super.hijackStdout();
        Helpers.restartPaxLoggingLog4j1(context, true);
    }

    @Configuration
    public Option[] configure() throws IOException {
        // even if default/fallback logger would write to a file (and actually Log4J1's LogLog class does that),
        // Log4J1's default configuration uses ConsoleAppender
        return combine(
                combine(baseConfigure(), defaultLoggingConfig()),

                paxLoggingApi(),
                paxLoggingLog4J1(),
                configAdmin(),
                eventAdmin()
        );
    }

    @Test
    public void auditWithDefaultConfiguration() throws IOException {
        Helpers.deleteLoggingConfig(context, cm);
        assertNotNull(osgiLoggerFactory);

        org.osgi.service.log.Logger log = osgiLoggerFactory.getLogger("my.logger");
        log.audit("This shall pass to console");

        List<String> lines = readLines();

        assertTrue(lines.contains("[main] AUDIT my.logger - This shall pass to console"));
    }

    @Test
    public void auditWithRestrictiveConfiguration() throws IOException {
        Helpers.updateLoggingConfig(context, cm, Helpers.LoggingLibrary.LOG4J1, "r7.audit");

        org.osgi.service.log.Logger log = osgiLoggerFactory.getLogger("my.logger");
        log.error("This shall not pass to console");
        log.audit("This shall pass to console");

        List<String> lines = readLines();

        assertFalse(lines.contains("[main] ERROR my.logger - This shall not pass to console"));
        assertTrue(lines.contains("[main] AUDIT my.logger - This shall pass to console"));
    }

}
