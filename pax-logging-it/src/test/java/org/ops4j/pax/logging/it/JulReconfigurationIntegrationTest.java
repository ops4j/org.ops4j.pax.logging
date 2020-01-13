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
import org.ops4j.pax.logging.PaxLoggingConstants;
import org.ops4j.pax.logging.it.support.Helpers;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.frameworkProperty;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.OptionUtils.combine;

@RunWith(PaxExam.class)
public class JulReconfigurationIntegrationTest extends AbstractStdoutInterceptingIntegrationTestBase {

    public static Logger LOG = LoggerFactory.getLogger(JulReconfigurationIntegrationTest.class);

    @Inject
    private ConfigurationAdmin cm;

    @Configuration
    public Option[] configure() throws IOException {
        return combine(
                baseConfigure(),
                mavenBundle("org.ops4j.pax.logging", "pax-logging-api").versionAsInProject(),

                configAdmin(),
                eventAdmin(),
                paxLoggingLog4J1().noStart(),
                paxLoggingLogback().noStart(),
                paxLoggingLog4J2().noStart(),

                // default log level is propagated to JUL level for root logger
                frameworkProperty(PaxLoggingConstants.LOGGING_CFG_DEFAULT_LOG_LEVEL).value("DEBUG")
        );
    }

    @Test
    public void julLogging() throws BundleException, InterruptedException {
        String name = "org.ops4j.pax.logging.it.test";

        Bundle paxLoggingLog4J1 = Helpers.paxLoggingService(context);
        Bundle paxLoggingLogback = Helpers.paxLoggingLogback(context);
        Bundle paxLoggingLog4J2 = Helpers.paxLoggingLog4j2(context);

        // we have only pax-logging-api installed now, so JUL default level should be configured using
        // "org.ops4j.pax.logging.DefaultServiceLog.level" context property set to WARN

        java.util.logging.Logger.getLogger(name).warning("WARN using java.util.logging 1");
        java.util.logging.Logger.getLogger(name).info("INFO using java.util.logging 1");
        java.util.logging.Logger.getLogger(name).finer("FINER using java.util.logging 1");

        List<String> lines = readLines();
        stdout.println("[[[[ default logging:\n" + buffer.toString() + "]]]]");

        assertTrue(lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.test] WARN : WARN using java.util.logging 1"));
        assertTrue(lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.test] INFO : INFO using java.util.logging 1"));
        assertFalse(lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.test] TRACE : FINER using java.util.logging 1"));

        buffer.reset();
        // start backend bundle waiting for TWO "configuration done" events - one related to "default configuration"
        // and one related to registration of CM Managed Service
        Helpers.awaitingConfigurationDone(2, context, () -> {
            try {
                paxLoggingLog4J1.start();
            } catch (BundleException ignored) {
            }
        });
        Helpers.updateLoggingConfig(context, cm, Helpers.LoggingLibrary.LOG4J1, "jul");

        java.util.logging.Logger.getLogger(name).info("INFO using java.util.logging 2");
        java.util.logging.Logger.getLogger(name).fine("FINE using java.util.logging 2");
        java.util.logging.Logger.getLogger(name).finer("FINER using java.util.logging 2");

        lines = readLines();
        stdout.println("[[[[ log4j1 logging:\n" + buffer.toString() + "]]]]");

        assertTrue(lines.contains("LOG4J1> [main] INFO org.ops4j.pax.logging.it.test - INFO using java.util.logging 2"));
        assertTrue(lines.contains("LOG4J1> [main] DEBUG org.ops4j.pax.logging.it.test - FINE using java.util.logging 2"));
        assertFalse(lines.contains("LOG4J1> [main] TRACE org.ops4j.pax.logging.it.test - FINER using java.util.logging 2"));

        paxLoggingLog4J1.stop();

        buffer.reset();
        // start backend bundle waiting for TWO "configuration done" events - one related to "default configuration"
        // and one related to registration of CM Managed Service
        Helpers.awaitingConfigurationDone(2, context, () -> {
            try {
                paxLoggingLogback.start();
            } catch (BundleException ignored) {
            }
        });
        Helpers.updateLoggingConfig(context, cm, Helpers.LoggingLibrary.LOGBACK, "jul");

        java.util.logging.Logger.getLogger(name).info("INFO using java.util.logging 3");
        java.util.logging.Logger.getLogger(name).fine("FINE using java.util.logging 3");
        java.util.logging.Logger.getLogger(name).finer("FINER using java.util.logging 3");
        java.util.logging.Logger.getLogger(name).finest("FINEST using java.util.logging 3");

        lines = readLines();
        stdout.println("[[[[ logback logging:\n" + buffer.toString() + "]]]]");

        assertTrue(lines.contains("LOGBACK> [main] INFO  org.ops4j.pax.logging.it.test - INFO using java.util.logging 3"));
        assertTrue(lines.contains("LOGBACK> [main] DEBUG org.ops4j.pax.logging.it.test - FINE using java.util.logging 3"));
        assertTrue(lines.contains("LOGBACK> [main] TRACE org.ops4j.pax.logging.it.test - FINER using java.util.logging 3"));
        assertTrue(lines.stream().noneMatch(l -> l.endsWith("FINEST using java.util.logging 3")));

        paxLoggingLogback.stop();

        buffer.reset();
        // start backend bundle waiting for TWO "configuration done" events - one related to "default configuration"
        // and one related to registration of CM Managed Service
        Helpers.awaitingConfigurationDone(2, context, () -> {
            try {
                paxLoggingLog4J2.start();
            } catch (BundleException ignored) {
            }
        });
        Helpers.updateLoggingConfig(context, cm, Helpers.LoggingLibrary.LOG4J2_PROPERTIES, "jul");

        java.util.logging.Logger.getLogger(name).severe("SEVERE using java.util.logging 4");
        java.util.logging.Logger.getLogger(name).warning("WARN using java.util.logging 4");
        java.util.logging.Logger.getLogger(name).info("INFO using java.util.logging 4");

        Thread.sleep(500);
        lines = readLines();
        stdout.println("[[[[ log4j2 logging:\n" + buffer.toString() + "]]]]");

        assertTrue(lines.contains("LOG4J2> org.ops4j.pax.logging.it.test/org.ops4j.pax.logging.it.JulReconfigurationIntegrationTest [ERROR] SEVERE using java.util.logging 4"));
        assertFalse(lines.contains("LOG4J2> org.ops4j.pax.logging.it.test/org.ops4j.pax.logging.it.JulReconfigurationIntegrationTest [WARN] WARN using java.util.logging 4"));
        assertFalse(lines.contains("LOG4J2> org.ops4j.pax.logging.it.test/org.ops4j.pax.logging.it.JulReconfigurationIntegrationTest [INFO] INFO using java.util.logging 4"));
    }

}
