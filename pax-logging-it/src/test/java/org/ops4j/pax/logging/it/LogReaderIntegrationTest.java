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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.logging.it.support.Helpers;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogReaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.OptionUtils.combine;

@RunWith(PaxExam.class)
public class LogReaderIntegrationTest extends AbstractControlledIntegrationTestBase {

    @Configuration
    public Option[] configure() throws IOException {
        return combine(
                combine(baseConfigure(), defaultLoggingConfig()),

                paxLoggingApi(),
                paxLoggingLogback().noStart(),
                paxLoggingLog4J2().noStart(),
                configAdmin(),
                eventAdmin()
        );
    }

    @Test
    public void logReaderServiceFromAll3Backends() throws Exception {
        Logger logger = LoggerFactory.getLogger(LogReaderIntegrationTest.class);

        ServiceReference<LogReaderService> sr = context.getServiceReference(LogReaderService.class);
        assertNull(sr);

        Bundle paxLoggingLogback = Helpers.paxLoggingLogback(context);
        Bundle paxLoggingLog4J2 = Helpers.paxLoggingLog4j2(context);

        paxLoggingLogback.start();
        sr = context.getServiceReference(LogReaderService.class);
        assertNotNull(sr);
        LogReaderService lrs = context.getService(sr);
        CountDownLatch latch2 = new CountDownLatch(1);
        final LogEntry[] pEntry = new LogEntry[1];
        lrs.addLogListener(entry -> {
            pEntry[0] = entry;
            latch2.countDown();
        });

        logger.info("checking logback");
        assertTrue(latch2.await(1, TimeUnit.SECONDS));
        assertThat(pEntry[0].getMessage(), equalTo("checking logback"));

        paxLoggingLogback.stop();
        sr = context.getServiceReference(LogReaderService.class);
        assertNull(sr);

        paxLoggingLog4J2.start();
        sr = context.getServiceReference(LogReaderService.class);
        assertNotNull(sr);
        lrs = context.getService(sr);
        CountDownLatch latch3 = new CountDownLatch(1);
        pEntry[0] = null;
        lrs.addLogListener(entry -> {
            pEntry[0] = entry;
            latch3.countDown();
        });

        logger.info("checking log4j2");
        assertTrue(latch3.await(1, TimeUnit.SECONDS));
        assertThat(pEntry[0].getMessage(), equalTo("checking log4j2"));
    }

}
