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
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.log.LoggerFactory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.OptionUtils.combine;

@RunWith(PaxExam.class)
public class LogbackR7AuditIntegrationTest extends AbstractStdoutInterceptingIntegrationTestBase {

    @Inject
    private ConfigurationAdmin cm;

    @Override
    public void hijackStdout() throws BundleException {
        super.hijackStdout();
        Helpers.restartPaxLoggingLogback(context, true);
    }

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
    public void auditWithDefaultConfiguration() throws IOException {
        Helpers.deleteLoggingConfig(context, cm);
        ServiceReference<LoggerFactory> sr = context.getServiceReference(LoggerFactory.class);
        LoggerFactory lf = context.getService(sr);
        org.osgi.service.log.Logger log = lf.getLogger("my.logger");
        log.audit("This shall pass to console");

        List<String> lines = readLines(13);

        // Logback doesn't support custom log levels... see ch.qos.logback.classic.Level.fromLocationAwareLoggerInteger
        assertTrue(lines.contains("[main] ERROR my.logger - This shall pass to console"));
    }

    @Test
    public void auditWithRestrictiveConfiguration() throws IOException {
        Helpers.updateLoggingConfig(context, cm, Helpers.LoggingLibrary.LOGBACK, "r7.audit");

        ServiceReference<LoggerFactory> sr = context.getServiceReference(LoggerFactory.class);
        LoggerFactory lf = context.getService(sr);
        org.osgi.service.log.Logger log = lf.getLogger("my.logger");
        log.audit("This shall pass to console {} {}", "arg1", "arg2");

        List<String> lines = readLines();

        assertTrue(lines.contains("my.logger/org.ops4j.pax.logging.it.LogbackR7AuditIntegrationTest [ERROR] This shall pass to console arg1 arg2"));
    }

}
