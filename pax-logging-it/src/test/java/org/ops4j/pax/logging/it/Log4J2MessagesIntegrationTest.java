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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.message.StringMapMessage;
import org.apache.logging.log4j.message.StructuredDataMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingService;
import org.ops4j.pax.logging.PaxMarker;
import org.ops4j.pax.logging.it.support.Helpers;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMarkerFactory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.OptionUtils.combine;

/**
 * Testing appenders of classes included in log4j:log4j and log4j:apache-log4j-extras - possibly optimized
 * in pax-logging-service Private-Packaged versions of the above.
 *
 * Each {@code @Test} method generally changes configuration, waits for event admin notification and tests
 * the situation. There's no need to restart pax-logging-service after hijacking stdout, so {@link #hijackStdout()}
 * is not overriden here.
 */
@RunWith(PaxExam.class)
public class Log4J2MessagesIntegrationTest extends AbstractStdoutInterceptingIntegrationTestBase {

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
    public void loggingStrLookup() {
        Helpers.updateLoggingConfig(context, cm, Helpers.LoggingLibrary.LOG4J2_PROPERTIES, "sd");

        Logger logger = LogManager.getLogger("my.logger");

        logger.info(new StructuredDataMessage("1", "hello!", "typeX").with("key1", "sd1"));
        logger.info(new StringMapMessage().with("key1", "map1"));

        List<String> lines = readLines();
        assertTrue(lines.contains("my.logger/org.ops4j.pax.logging.it.Log4J2MessagesIntegrationTest typeX/sd1 [INFO] typeX [1 key1=\"sd1\"] hello!"));
        assertTrue(lines.contains("my.logger/org.ops4j.pax.logging.it.Log4J2MessagesIntegrationTest ${sd:type}/map1 [INFO] key1=\"map1\""));
    }

}
