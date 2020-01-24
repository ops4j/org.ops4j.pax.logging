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
public class LogbackWithoutConfigAdminWithExternalLogbackConfigurationIntegrationTest extends AbstractStdoutInterceptingIntegrationTestBase {

    private static File logFile = new File("target/configuration/LogbackWithoutConfigAdminWithExternalLogbackConfigurationIntegrationTest.xml");

    @Configuration
    public Option[] configure() throws IOException {
        return combine(
                combine(baseConfigure(), defaultLoggingConfig()),

                paxLoggingApi(),
                paxLoggingLogback().noStart(),
                eventAdmin(),

                frameworkProperty(PaxLoggingConstants.LOGGING_CFG_PROPERTY_FILE).value(logFile.getAbsolutePath())
        );
    }

    @Test
    public void logbackConfigurationWithoutConfigAdmin() throws IOException, InterruptedException, BundleException {
        logFile.getParentFile().mkdirs();

        String xml = "<configuration>\n" +
                "    <appender name=\"console\" class=\"ch.qos.logback.core.ConsoleAppender\">\n" +
                "        <encoder>\n" +
                "            <pattern>(%logger\\) --- [%level] %msg%n</pattern>\n" +
                "        </encoder>\n" +
                "    </appender>\n" +
                "\n" +
                "    <root level=\"trace\">\n" +
                "        <appender-ref ref=\"console\" />\n" +
                "    </root>\n" +
                "</configuration>\n";
        try (FileWriter fw = new FileWriter(logFile)) {
            fw.write(xml);
        }

        Helpers.awaitingConfigurationDone(1, context, () -> {
            try {
                Helpers.paxLoggingLogback(context).start();
            } catch (BundleException ignored) {
            }
        });

        // we should have default configuration
        LoggerFactory.getLogger("my.logger").info("After start()");

        List<String> lines = readLines();

        assertTrue(lines.contains("(my.logger) --- [INFO] After start()"));
    }

}
