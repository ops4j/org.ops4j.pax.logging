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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.logging.PaxLoggingConstants;
import org.ops4j.pax.logging.it.support.Helpers;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceListener;
import org.osgi.service.cm.ConfigurationAdmin;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.frameworkProperty;
import static org.ops4j.pax.exam.OptionUtils.combine;

/**
 * Refresh is different than restart, because classloader changes.
 */
@RunWith(PaxExam.class)
public class LogbackFrameworkEventsIntegrationTest extends AbstractStdoutInterceptingIntegrationTestBase {

    @Inject
    private ConfigurationAdmin cm;

    @Override
    public void hijackStdout() throws BundleException {
        super.hijackStdout();
        Helpers.restartPaxLoggingLogback(context, true);
    }

    @Configuration
    public Option[] configure() throws IOException {
        return combine(
                baseConfigure(),

                paxLoggingApi(),
                paxLoggingLogback(),
                configAdmin(),
                eventAdmin(),

                frameworkProperty(PaxLoggingConstants.LOGGING_CFG_DEFAULT_LOG_LEVEL).value("DEBUG"),
                // having this property (which specifies threshold) at INFO allows us to see INFO/WARN/ERROR
                // bundle, service and framework events
                frameworkProperty(PaxLoggingConstants.LOGGING_CFG_FRAMEWORK_EVENTS_LOG_LEVEL).value("INFO")
        );
    }

    @Test
    public void handleFrameworkErrors() throws BundleException, InterruptedException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        Helpers.updateLoggingConfig(context, cm, Helpers.LoggingLibrary.LOGBACK, "frameworkEvents");

        final CountDownLatch latch1 = new CountDownLatch(1);
        final String[] message = new String[1];
        FrameworkListener listener1 = event -> {
            if (event.getThrowable() != null && event.getThrowable().getClass() == RuntimeException.class) {
                latch1.countDown();
                message[0] = event.getThrowable().getMessage();
            }
        };
        context.addFrameworkListener(listener1);

        final ServiceListener listener = event -> {
            // throwing this exception will be handled in several ways:
            // 1) directly in catch{} block in org.apache.felix.framework.EventDispatcher.fireEventImmediately()
            //    using org.apache.felix.framework.Logger.doLog()
            // 2) in org.apache.felix.framework.EventDispatcher.fireFrameworkEvent() where in this test there are
            //    5 listeners:
            //     - org.ops4j.pax.logging.spi.support.FrameworkHandler
            //     - org.ops4j.pax.exam.nat.internal.NativeTestContainer$1
            //     - org.ops4j.pax.exam.nat.internal.NativeTestContainer$2
            //     - org.apache.felix.eventadmin.impl.adapter.FrameworkEventAdapter
            //     - org.ops4j.pax.logging.it.Log4J2FrameworkEventsIntegrationTest$$Lambda$9.836386144
            throw new RuntimeException("Fake Exception");
        };
        context.addServiceListener(listener);

        context.registerService(List.class, new ArrayList<String>(), null);

        latch1.await();
        assertThat(message[0], equalTo("Fake Exception"));
        context.removeFrameworkListener(listener1);

        // but it may be the case that listener from org.ops4j.pax.logging.spi.support.FrameworkHandler
        // is called AFTER the listener that counts down the latch. So we have to be sure -
        // - we'll register another service and cause another RuntimeException

        final CountDownLatch latch2 = new CountDownLatch(1);
        FrameworkListener listener2 = event -> {
            if (event.getThrowable() != null && event.getThrowable().getClass() == RuntimeException.class) {
                latch2.countDown();
            }
        };
        context.addFrameworkListener(listener2);
        context.registerService(List.class, new ArrayList<String>(), null);
        latch2.await();
        context.removeServiceListener(listener);
        context.removeFrameworkListener(listener2);

        List<String> lines1 = readLines("target/logs-logback/framework-events.log");
        List<String> lines2 = readLines();
        List<String> lines3 = lines2.stream().map(l -> l.substring(13)).collect(Collectors.toList());

        // we should see start of pax-logging-log4j2 bundle
        assertTrue(lines3.contains("[main] INFO Events.Bundle - BundleEvent STARTED"));
        // we should see registration of our java.util.List service
        assertTrue(lines1.contains("{main} Events.Service/? [INFO] ServiceEvent REGISTERED - [java.util.List]"));
        // we should also see error event with stack trace
        int l = lines1.indexOf("{FelixDispatchQueue} Events.Framework/? [ERROR] FrameworkEvent ERROR");
        assertThat(lines1.get(l + 1), equalTo("java.lang.RuntimeException: Fake Exception"));
    }

}
