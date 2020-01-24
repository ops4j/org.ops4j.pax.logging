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
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.logging.PaxLoggingConstants;
import org.ops4j.pax.logging.it.support.Helpers;
import org.osgi.framework.BundleException;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.frameworkProperty;
import static org.ops4j.pax.exam.OptionUtils.combine;

@RunWith(PaxExam.class)
public class Log4J2WithoutConfigAdminWithExternalLog4J2ConfigurationIntegrationTest extends AbstractStdoutInterceptingIntegrationTestBase {

    private static File logFile1 = new File("target/configuration/Log4J2WithoutConfigAdminWithExternalLog4J2ConfigurationIntegrationTest1.properties");
    private static File logFile2 = new File("target/configuration/Log4J2WithoutConfigAdminWithExternalLog4J2ConfigurationIntegrationTest2.properties");

    @Configuration
    public Option[] configure() throws IOException {
        return combine(
                combine(baseConfigure(), defaultLoggingConfig()),

                paxLoggingApi(),
                paxLoggingLog4J2().noStart(),
                eventAdmin(),

                frameworkProperty(PaxLoggingConstants.LOGGING_CFG_PROPERTY_FILE).value(logFile1.getAbsolutePath() + "," + logFile2.getAbsolutePath())
        );
    }

    @Test
    public void log4j2ConfigurationWithoutConfigAdmin() throws IOException, InterruptedException, BundleException {
        // native properties spread across two different files
        Properties paxLoggingConfiguration1 = new Properties();
        Properties paxLoggingConfiguration2 = new Properties();
        paxLoggingConfiguration1.setProperty("appender.console.type", "Console");
        paxLoggingConfiguration1.setProperty("appender.console.name", "console");
        paxLoggingConfiguration1.setProperty("appender.console.layout.type", "PatternLayout");
        paxLoggingConfiguration1.setProperty("appender.console.layout.pattern", "(%c) --- [%p] %m%n");
        paxLoggingConfiguration2.setProperty("rootLogger.level", "info");
        paxLoggingConfiguration2.setProperty("rootLogger.appenderRef.console.ref", "console");
        logFile1.getParentFile().mkdirs();
        paxLoggingConfiguration1.store(new FileWriter(logFile1), null);
        paxLoggingConfiguration2.store(new FileWriter(logFile2), null);

        Helpers.awaitingConfigurationDone(1, context, () -> {
            try {
                Helpers.paxLoggingLog4j2(context).start();
            } catch (BundleException ignored) {
            }
        });

        // we should have default configuration
        LoggerFactory.getLogger("my.logger").info("After start()");

        List<String> lines = readLines();

        assertTrue(lines.contains("(my.logger) --- [INFO] After start()"));
    }

}
