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
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.apache.log4j.helpers.LogLog;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.BundleContext;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.OptionUtils.combine;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class Log4J1WithDefaultConfigurationIntegrationTest extends AbstractControlledIntegrationTestBase {

    @Inject
    private BundleContext context;

    @Configuration
    public Option[] configure() throws IOException {
        return combine(
                combine(baseConfigure(), defaultLoggingConfig()),

                paxLoggingApi(),
                paxLoggingLog4J1(),
                configAdmin()
        );
    }

    @Test
    public void logLog() {
        // threshold is controlled by PaxLoggingConstants.LOGGING_CFG_DEFAULT_LOG_LEVEL context property
        LogLog.debug("LogLog debug");
        LogLog.warn("LogLog warn");
        LogLog.error("LogLog error");
    }

    @Test
    public void logUsingLog4J1API() {
        Logger log = Logger.getLogger(Log4J1WithDefaultConfigurationIntegrationTest.class);

        log.error("Log4J1WithDefaultConfigurationIntegrationTest/ERROR");
        log.warn("Log4J1WithDefaultConfigurationIntegrationTest/WARN");
        log.info("Log4J1WithDefaultConfigurationIntegrationTest/INFO");
        log.debug("Log4J1WithDefaultConfigurationIntegrationTest/DEBUG");
        log.trace("Log4J1WithDefaultConfigurationIntegrationTest/TRACE");
    }

}
