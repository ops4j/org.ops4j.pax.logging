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
public class Log4J1WithoutConfigAdminWithExternalPaxLoggingConfigurationIntegrationTest extends AbstractStdoutInterceptingIntegrationTestBase {

    private static File logFile = new File("target/configuration/Log4J1WithoutConfigAdminWithExternalPaxLoggingConfigurationIntegrationTest.properties");

    @Configuration
    public Option[] configure() throws IOException {
        return combine(
                combine(baseConfigure(), defaultLoggingConfig()),

                paxLoggingApi(),
                paxLoggingLog4J1().noStart(),
                eventAdmin(),

                frameworkProperty(PaxLoggingConstants.LOGGING_CFG_PROPERTY_FILE).value(logFile.getAbsolutePath())
        );
    }

    @Test
    public void paxLoggingConfigurationWithoutConfigAdmin() throws IOException, InterruptedException, BundleException {
        // prefixed properties
        Properties paxLoggingConfiguration = new Properties();
        paxLoggingConfiguration.setProperty("log4j.appender.console", "org.apache.log4j.ConsoleAppender");
        paxLoggingConfiguration.setProperty("log4j.appender.console.layout", "org.apache.log4j.PatternLayout");
        paxLoggingConfiguration.setProperty("log4j.appender.console.layout.ConversionPattern", "(%c) --- [%p] %m%n");
        paxLoggingConfiguration.setProperty("log4j.rootLogger", "DEBUG, console");
        logFile.getParentFile().mkdirs();
        paxLoggingConfiguration.store(new FileWriter(logFile), null);

        Helpers.awaitingConfigurationDone(1, context, () -> {
            try {
                Helpers.paxLoggingService(context).start();
            } catch (BundleException ignored) {
            }
        });

        // we should have default configuration
        LoggerFactory.getLogger("my.logger").info("After start()");

        List<String> lines = readLines();

        assertTrue(lines.contains("(my.logger) --- [INFO] After start()"));
    }

}
