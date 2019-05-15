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
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.logging.PaxLoggingConstants;
import org.ops4j.pax.logging.it.support.Helpers;
import org.osgi.framework.BundleException;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.log.LogService;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.OptionUtils.combine;

@RunWith(PaxExam.class)
public class Log4J1WithConfigAdminIntegrationTest extends AbstractStdoutInterceptingIntegrationTestBase {

    @Inject
    private LogService osgiLogService;

    @Inject
    private ConfigurationAdmin cm;

    @Override
    public void hijackStdout() throws BundleException {
        super.hijackStdout();
        Helpers.restartPaxLoggingService(context, true);
    }

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
    public void defaultConfigurationButUsingConfigAdmin() throws IOException, InterruptedException {
        org.osgi.service.cm.Configuration c = cm.getConfiguration(PaxLoggingConstants.LOGGING_CONFIGURATION_PID, null);

        final CountDownLatch latch = new CountDownLatch(1);
        EventHandler handler = event -> {
            latch.countDown();
        };
        Dictionary<String, Object> props = new Hashtable<>();
        props.put(EventConstants.EVENT_TOPIC, PaxLoggingConstants.LOGGING_EVENT_ADMIN_CONFIGURATION_TOPIC);
        context.registerService(EventHandler.class, handler, props);

        LoggerFactory.getLogger("defaultConfigurationButUsingConfigAdmin").info("Before org.osgi.service.cm.Configuration.update()");
        // no properties will switch to default configuration.
        props = c.getProperties();
        c.update(props);

        // there should be no reconfiguration when new properties are empty
        assertFalse(latch.await(1, TimeUnit.SECONDS));

        // after the above update, we should keep the default configuration with TTCCLayout
        LoggerFactory.getLogger("defaultConfigurationButUsingConfigAdmin").info("After org.osgi.service.cm.Configuration.update()");

        List<String> lines = readLines();

        // verification of LogLog messages
        assertTrue(lines.contains("[main] INFO defaultConfigurationButUsingConfigAdmin - Before org.osgi.service.cm.Configuration.update()"));
        assertTrue(lines.contains("[main] INFO defaultConfigurationButUsingConfigAdmin - After org.osgi.service.cm.Configuration.update()"));
    }

}
