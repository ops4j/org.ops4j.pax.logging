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
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.logging.it.support.Helpers;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ops4j.pax.exam.OptionUtils.combine;

@RunWith(PaxExam.class)
public class Log4J1ThreadingIntegrationTest extends AbstractControlledIntegrationTestBase {

    @Inject
    private ConfigurationAdmin cm;

    @Configuration
    public Option[] configure() throws IOException {
        return combine(
                combine(baseConfigure(), defaultLoggingConfig()),

                paxLoggingApi(),
                paxLoggingLog4J1(),
                configAdmin(),
                eventAdmin()
        );
    }

    @Test
    public void multipleThreads() throws Exception {
        Helpers.updateLoggingConfig(context, cm, Helpers.LoggingLibrary.LOG4J1, "threads", d -> {
            d.put("irrelevant", UUID.randomUUID().toString());
        });

        final Logger log = LoggerFactory.getLogger("my.logger");
        final CountDownLatch latch = new CountDownLatch(41);

        Runnable r1 = () -> {
            Thread.currentThread().setName("Thread " + UUID.randomUUID().toString());
            for (int i = 0; i < 100; i++) {
                log.info("Message {}", i);
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
            latch.countDown();
        };
        Runnable r2 = () -> {
            Thread.currentThread().setName("Config Updater Thread");
            for (int i = 0; i < 30; i++) {
                Helpers.updateLoggingConfig(context, cm, Helpers.LoggingLibrary.LOG4J1, "threads", d -> {
                    d.put("irrelevant", UUID.randomUUID().toString());
                });
                try {
                    Thread.sleep(85);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
            latch.countDown();
        };
        new Thread(r2).start();
        for (int t = 0; t < 40; t++) {
            new Thread(r1).start();
        }

        latch.await();
    }

}
