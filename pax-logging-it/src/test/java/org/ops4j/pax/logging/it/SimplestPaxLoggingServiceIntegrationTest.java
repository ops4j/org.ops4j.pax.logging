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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.logging.PaxContext;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingService;
import org.ops4j.pax.logging.it.support.MockPaxLogger;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.OptionUtils.combine;

/**
 * This test installs implementation of {@link PaxLoggingService} straight from the test itself. This service
 * should be picked up by pax-logging-api and <strong>existing</strong> <em>logger</em> (for example obtained via
 * SLF4J) should immediately start logging through the loggers from our new {@link PaxLoggingService}.
 */
@RunWith(PaxExam.class)
public class SimplestPaxLoggingServiceIntegrationTest extends AbstractControlledIntegrationTestBase {

    @Configuration
    public Option[] configure() throws IOException {
        return combine(
                combine(baseConfigure(), defaultLoggingConfig()),
                mavenBundle("org.ops4j.pax.logging", "pax-logging-api").versionAsInProject()
        );
    }

    @Test
    public void registerInlinePaxLoggingService() {
        Logger logger = LoggerFactory.getLogger(SimplestPaxLoggingServiceIntegrationTest.class);
        logger.info("Before registration of PaxLoggingService");

        final MockPaxLogger paxLogger = new MockPaxLogger();
        context.registerService(PaxLoggingService.class, new PaxLoggingService() {
            @Override
            public PaxLogger getLogger(Bundle bundle, String category, String fqcn) {
                return paxLogger;
            }

            @Override
            public int getLogLevel() {
                return 0;
            }

            @Override
            public PaxContext getPaxContext() {
                return null;
            }

            @Override
            public void log(int level, String message) {
            }

            @Override
            public void log(int level, String message, Throwable exception) {
            }

            @Override
            public void log(ServiceReference sr, int level, String message) {
            }

            @Override
            public void log(ServiceReference sr, int level, String message, Throwable exception) {
            }
        }, null);

        assertFalse("This message shouldn't have passed through mock logger",
                paxLogger.getMessages().contains("Before registration of PaxLoggingService"));

        logger.info("After registration of PaxLoggingService");
        assertTrue("This message shouldn've passed through mock logger",
                paxLogger.getMessages().contains("After registration of PaxLoggingService"));
    }

}
