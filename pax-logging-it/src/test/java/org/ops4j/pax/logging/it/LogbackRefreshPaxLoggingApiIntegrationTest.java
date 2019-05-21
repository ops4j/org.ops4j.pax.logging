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
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.logging.PaxLoggingConstants;
import org.ops4j.pax.logging.it.support.Helpers;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.wiring.FrameworkWiring;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.ops4j.pax.exam.CoreOptions.frameworkProperty;
import static org.ops4j.pax.exam.OptionUtils.combine;

/**
 * Refresh is different than restart, because classloader changes.
 */
@RunWith(PaxExam.class)
public class LogbackRefreshPaxLoggingApiIntegrationTest extends AbstractStdoutInterceptingIntegrationTestBase {

    @Inject
    private LogService osgiLog;

    private FrameworkWiring wiring;
    private Bundle paxLoggingApi;

    @Override
    public void hijackStdout() throws BundleException {
        super.hijackStdout();
        Helpers.restartPaxLoggingService(context, true);

        paxLoggingApi = Helpers.paxLoggingApi(context);
        wiring = paxLoggingApi.getBundleContext().getBundle(0L).adapt(FrameworkWiring.class);
    }

    @Configuration
    public Option[] configure() throws IOException {
        return combine(
                baseConfigure(),

                paxLoggingApi(),
                paxLoggingLogback(),
                configAdmin(),
                eventAdmin(),

                // this will make use of static LoggerContext, so loggers will be preserved between
                // restarts - this will make @Injected references not tied to old Logback LoggerContext
                frameworkProperty(PaxLoggingConstants.LOGGING_CFG_LOGBACK_USE_STATIC_CONTEXT).value("true"),
                
                frameworkProperty(PaxLoggingConstants.LOGGING_CFG_DEFAULT_LOG_LEVEL).value("DEBUG"),
                frameworkProperty(PaxLoggingConstants.LOGGING_CFG_FRAMEWORK_EVENTS_LOG_LEVEL).value("DISABLED")
        );
    }

    @Test
    public void restartPaxLoggingApi() throws BundleException, InterruptedException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        Logger log = LoggerFactory.getLogger(this.getClass());

        // through TTLL layout to System.out
        log.info("Before refreshing pax-logging-api");
        osgiLog.log(LogService.LOG_INFO, "Before refreshing pax-logging-api");

        refreshPaxLoggingApi();

        log.info("After refreshing pax-logging-api");

        Logger log1 = LoggerFactory.getLogger(this.getClass());
        log1.info("After refreshing pax-logging-api (log1)");

        // old reference using old pax-logging-logback, which was refreshed too!
        osgiLog.log(LogService.LOG_INFO, "After refreshing pax-logging-api");

        try {
            context.getServiceReference(LogService.class);
            fail("Should fail, because our context was also refreshed!");
        } catch (IllegalStateException ignored) {
        }

        // we need reflection, because org.osgi.service.log.LogService class is loaded by no longer
        // valid classloader (old pax-logging-api wiring)
        Bundle ourBundle = FrameworkUtil.getBundle(this.getClass());
        BundleContext newContext = ourBundle.getBundleContext();
        ServiceReference<?> ref = newContext.getServiceReference(LogService.class.getName());
        assertNotNull(ref);

        Object service = newContext.getService(ref);
        Class<?> logServiceClass = ourBundle.loadClass("org.osgi.service.log.LogService");
        Method m = logServiceClass.getDeclaredMethod("log", Integer.TYPE, String.class);
        m.invoke(service, LogService.LOG_INFO, "After refreshing pax-logging-logback (log service new ref)");

        // access through old linked classes
        Logger log2 = LoggerFactory.getLogger(this.getClass());
        log2.info("After refreshing pax-logging-api (log2)");

        // reflection-based logger factory
        Class<?> loggerFactoryClass = ourBundle.loadClass("org.slf4j.LoggerFactory");
        Class<?> loggerClass = ourBundle.loadClass("org.slf4j.Logger");
        Method getLoggerMethod = loggerFactoryClass.getDeclaredMethod("getLogger", Class.class);
        Method infoMethod = loggerClass.getDeclaredMethod("info", String.class);
        Object log3 = getLoggerMethod.invoke(null, this.getClass());
        infoMethod.invoke(log3, "After refreshing pax-logging-api (log3)");

        List<String> lines = readLines();
        List<String> lines2 = lines.stream().map(l -> l.length() > 13 ? l.substring(13) : l)
                .collect(Collectors.toList());

        assertTrue("line from TTLLLayout", lines2.contains("[main] INFO org.ops4j.pax.logging.it.LogbackRefreshPaxLoggingApiIntegrationTest - Before refreshing pax-logging-api"));
        assertTrue("line from TTLLLayout", lines2.contains("[main] INFO PaxExam-Probe - Before refreshing pax-logging-api"));
        assertTrue("Cascade refresh", lines.contains("org.ops4j.pax.logging.pax-logging-logback [logback] INFO : Logback configured using default configuration."));
        assertTrue("default layout because old class", lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.LogbackRefreshPaxLoggingApiIntegrationTest] INFO : After refreshing pax-logging-api"));
        assertTrue("default layout because old class", lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.LogbackRefreshPaxLoggingApiIntegrationTest] INFO : After refreshing pax-logging-api (log1)"));
        assertTrue("old reference", lines.contains("org.ops4j.pax.logging.pax-logging-logback [logback] WARN : No appenders present in context [default] for logger [PaxExam-Probe]."));
        assertTrue("new reference", lines2.contains("[main] INFO PaxExam-Probe - After refreshing pax-logging-logback (log service new ref)"));
        assertTrue("default layout because old class", lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.LogbackRefreshPaxLoggingApiIntegrationTest] INFO : After refreshing pax-logging-api (log2)"));
        assertTrue("TTLLLayout because new class", lines2.contains("[main] INFO org.ops4j.pax.logging.it.LogbackRefreshPaxLoggingApiIntegrationTest - After refreshing pax-logging-api (log3)"));
    }

    private void refreshPaxLoggingApi() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        wiring.refreshBundles(Collections.singletonList(paxLoggingApi), (FrameworkListener) event -> {
            if (event.getType() == FrameworkEvent.PACKAGES_REFRESHED) {
                latch.countDown();
            }
        });
        assertTrue(latch.await(10, TimeUnit.SECONDS));
    }

}
