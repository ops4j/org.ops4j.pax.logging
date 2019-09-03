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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.inject.Inject;
import javax.swing.*;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingConstants;
import org.ops4j.pax.logging.PaxLoggingService;
import org.ops4j.pax.logging.it.support.Helpers;
import org.ops4j.pax.logging.it.support.OnDemandLogger;
import org.ops4j.pax.logging.it.support.OnDemandLoggerActivator;
import org.ops4j.pax.logging.spi.PaxFilter;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;
import org.ops4j.pax.tinybundles.core.TinyBundles;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.OptionUtils.combine;

/**
 * <p>Testing appenders of classes included in log4j:log4j and log4j:apache-log4j-extras - possibly optimized
 * in pax-logging-log4j1 Private-Packaged versions of the above.</p>
 * <p>Each {@code @Test} method generally changes configuration, waits for event admin notification and tests
 * the situation. There's no need to restart pax-logging-log4j1 after hijacking stdout, so {@link #hijackStdout()}
 * is not overriden here.</p>
 */
@RunWith(PaxExam.class)
public class Log4J1BuiltinAppendersIntegrationTest extends AbstractStdoutInterceptingIntegrationTestBase {

    @Inject
    private ConfigurationAdmin cm;

    @Configuration
    public Option[] configure() throws IOException {
        prepareBundles();

        return combine(
                combine(baseConfigure(), defaultLoggingConfig()),

                paxLoggingApi(),
                paxLoggingLog4J1(),
                configAdmin(),
                eventAdmin()
        );
    }

    /**
     * Method called by surefire/failsafe, not to be used by @Test method inside OSGi container (even if native)
     * @throws IOException
     */
    public static void prepareBundles() throws IOException {
        InputStream bundle1 = TinyBundles.bundle()
                .set("Bundle-ManifestVersion", "2")
                .set("Bundle-SymbolicName", "b1")
                .set("Bundle-Activator", "org.ops4j.pax.logging.it.support.OnDemandLoggerActivator")
                .set("Import-Package", "org.osgi.framework,org.slf4j,org.apache.logging.log4j")
                .add(OnDemandLogger.class)
                .add(OnDemandLoggerActivator.class)
                .build();
        new File("target/bundles").mkdirs();
        IOUtils.copy(bundle1, new FileOutputStream("target/bundles/mdc-bundle1.jar"));

        InputStream bundle2 = TinyBundles.bundle()
                .set("Bundle-ManifestVersion", "2")
                .set("Bundle-SymbolicName", "b2")
                .set("Bundle-Activator", "org.ops4j.pax.logging.it.support.OnDemandLoggerActivator")
                .set("Import-Package", "org.osgi.framework,org.slf4j,org.apache.logging.log4j")
                .add(OnDemandLogger.class)
                .add(OnDemandLoggerActivator.class)
                .build();
        new File("target/bundles").mkdirs();
        IOUtils.copy(bundle2, new FileOutputStream("target/bundles/mdc-bundle2.jar"));
    }

    @Test
    public void plainFileAppender() {
        Helpers.updateLoggingConfig(context, cm, Helpers.LoggingLibrary.LOG4J1, "builtin.file");

        Logger log = LoggerFactory.getLogger("my.logger");
        log.info("Hello into FileAppender");

        List<String> lines = readLines("target/logs-log4j1/file-appender.log");

        assertTrue(lines.contains("my.logger/org.ops4j.pax.logging.it.Log4J1BuiltinAppendersIntegrationTest [INFO] Hello into FileAppender"));
    }

    @Test
    public void dailyZipRollingFileAppender() {
        Hashtable<String, Object> properties = new Hashtable<>();
        properties.put(PaxLoggingConstants.SERVICE_PROPERTY_FILTER_NAME_PROPERTY, "timestampReplacer");
        context.registerService(PaxFilter.class, new MyPaxFilter(), properties);

        Helpers.updateLoggingConfig(context, cm, Helpers.LoggingLibrary.LOG4J1, "builtin.dailyZip");

        Logger log = LoggerFactory.getLogger("my.logger");
        log.info("before");
        log.info("after");

        // there should simply be two files
        File[] files = new File("target/logs-log4j1").listFiles((dir, name) -> name.startsWith("dz-appender.log."));
        assertNotNull(files);
        assertThat(files.length, equalTo(2));
    }

    @Test
    public void socketAppender() throws Exception {
        final ServerSocket ss = new ServerSocket(0);
        final int port = ss.getLocalPort();
        final CountDownLatch latch = new CountDownLatch(1);

        Thread t = new Thread(() -> {
            try {
                Socket s = ss.accept();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
                Object obj = ois.readObject();
                assertThat(obj.getClass().getName(), equalTo("org.apache.log4j.spi.LoggingEvent"));
                assertThat(Helpers.getField(obj, "renderedMessage", String.class), equalTo("socket message"));
                s.close();
                latch.countDown();
            } catch (Throwable e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });
        t.start();

        Helpers.updateLoggingConfig(context, cm, Helpers.LoggingLibrary.LOG4J1, "builtin.socket", props -> {
            props.put("log4j.appender.s.port", Integer.toString(port));
        });

        Logger log = LoggerFactory.getLogger("my.logger");
        log.info("socket message");

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void listModelAppenderFromExtras() {
        Helpers.updateLoggingConfig(context, cm, Helpers.LoggingLibrary.LOG4J1, "builtin.list");

        Logger log = LoggerFactory.getLogger("my.logger");
        log.info("should be added to list");

        List<?> appenders = Helpers.getField(log, "m_delegate.m_delegate.m_delegate.aai.appenderList", List.class);
        ListModel model = Helpers.getField(appenders.get(0), "model", ListModel.class);
        Object obj = model.getElementAt(0);
        assertThat(obj.getClass().getName(), equalTo("org.apache.log4j.spi.LoggingEvent"));
        assertThat(Helpers.getField(obj, "message", String.class), equalTo("should be added to list"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void mdcSiftingAppender() throws Exception {
        File f1 = new File("target/bundles/mdc-bundle1.jar");
        File f2 = new File("target/bundles/mdc-bundle2.jar");
        Bundle b1 = context.installBundle(f1.toURI().toURL().toString(), new FileInputStream(f1));
        Bundle b2 = context.installBundle(f2.toURI().toURL().toString(), new FileInputStream(f2));
        b1.start();
        b2.start();

        Helpers.updateLoggingConfig(context, cm, Helpers.LoggingLibrary.LOG4J1, "mdc.appender");

        Logger log = LoggerFactory.getLogger("my.logger");
        log.info("Hello into FileAppender"); // should go to PaxExam-Probe-file-appender.log

        // it's interesting to check if loggers from the same category, but obtained using different bundles
        // will correctly handle MDC
        ServiceReference<PaxLoggingService> sr = context.getServiceReference(PaxLoggingService.class);
        PaxLoggingService paxLoggingService = context.getService(sr);

        PaxLogger l1 = paxLoggingService.getLogger(b1, "com.example.l1", paxLoggingService.getClass().getName());
        String fqcn = l1.getClass().getName();
        // again - with better FQCN
        l1 = paxLoggingService.getLogger(b1, "com.example.l1", fqcn);
        PaxLogger l1a = paxLoggingService.getLogger(b1, "com.example.l2", fqcn);
        PaxLogger l2 = paxLoggingService.getLogger(b2, "com.example.l1", fqcn);
        PaxLogger l2a = paxLoggingService.getLogger(b2, "com.example.l2", fqcn);

        l1.info("Hello from b1/l1");
        l1a.info("Hello from b1/l2");
        l2.info("Hello from b2/l1");
        l2a.info("Hello from b2/l2");

        List<String> linesB1 = readLines("target/logs-log4j1/" + b1.getBundleId() + "-file-appender.log");
        List<String> linesB2 = readLines("target/logs-log4j1/" + b2.getBundleId() + "-file-appender.log");

        assertTrue(linesB1.stream().allMatch(l -> l.contains("{bundle.name,b1}") && l.contains("Hello from b1/")));
        assertTrue(linesB2.stream().allMatch(l -> l.contains("{bundle.name,b2}") && l.contains("Hello from b2/")));

        ServiceReference<Consumer> src1 = (ServiceReference<Consumer>) context.getServiceReferences(Consumer.class.getName(), "(sn=b1)")[0];
        ServiceReference<Consumer> src2 = (ServiceReference<Consumer>) context.getServiceReferences(Consumer.class.getName(), "(sn=b2)")[0];
        Consumer<String> c1 = context.getService(src1);
        Consumer<String> c2 = context.getService(src2);

        c1.accept("hello through c1");
        c2.accept("hello through c2");

        linesB1 = readLines("target/logs-log4j1/" + b1.getBundleId() + "-file-appender.log");
        linesB2 = readLines("target/logs-log4j1/" + b2.getBundleId() + "-file-appender.log");

        assertTrue(linesB1.stream().anyMatch(l ->
                l.contains("org.ops4j.test/org.ops4j.pax.logging.it.support.OnDemandLogger")
                && l.contains("bundle.name,b1")
                && l.contains("slf4j: sn=b1, message=hello through c1"))
        );
        assertTrue(linesB1.stream().anyMatch(l ->
                l.contains("org.ops4j.test/org.ops4j.pax.logging.it.support.OnDemandLogger")
                && l.contains("bundle.name,b1")
                && l.contains("log4j2: sn=b1, message=hello through c1"))
        );
        assertTrue(linesB1.stream().noneMatch(l -> l.contains("bundle.name,b2")));
        assertTrue(linesB2.stream().anyMatch(l ->
                l.contains("org.ops4j.test/org.ops4j.pax.logging.it.support.OnDemandLogger")
                && l.contains("bundle.name,b2")
                && l.contains("slf4j: sn=b2, message=hello through c2"))
        );
        assertTrue(linesB2.stream().anyMatch(l ->
                l.contains("org.ops4j.test/org.ops4j.pax.logging.it.support.OnDemandLogger")
                && l.contains("bundle.name,b2")
                && l.contains("log4j2: sn=b2, message=hello through c2"))
        );
        assertTrue(linesB2.stream().noneMatch(l -> l.contains("bundle.name,b1")));
    }

    @Test
    public void mdcSiftingAppenderWithNonBundleKey() throws Exception {
        Helpers.updateLoggingConfig(context, cm, Helpers.LoggingLibrary.LOG4J1, "mdc.appender2");

        Logger log = LoggerFactory.getLogger("my.logger");

        log.info("Hello into FileAppender 1");
        MDC.put("my.key", "f1");
        log.info("Hello into FileAppender 2");
        MDC.put("my.key", "f2");
        log.info("Hello into FileAppender 3");
        MDC.remove("my.key");
        log.info("Hello into FileAppender 4");

        List<String> lines1 = readLines("target/logs-log4j1/default-file-appender.log");
        List<String> lines2 = readLines("target/logs-log4j1/f1-file-appender.log");
        List<String> lines3 = readLines("target/logs-log4j1/f2-file-appender.log");

        assertTrue(lines1.stream().anyMatch(l -> l.contains("Hello into FileAppender 1")));
        assertTrue(lines2.stream().anyMatch(l -> l.contains("Hello into FileAppender 2")));
        assertTrue(lines3.stream().anyMatch(l -> l.contains("Hello into FileAppender 3")));
        assertTrue(lines1.stream().anyMatch(l -> l.contains("Hello into FileAppender 4")));
    }

    private static class MyPaxFilter implements PaxFilter {

        private DateFormat TS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        private long beforeTS = 0L;
        private long afterTS = 0L;

        @Override
        public int doFilter(PaxLoggingEvent event) {
            String message = Helpers.getField(Helpers.getField(event, "m_delegate"), "message", String.class);
            if (message.equals("before")) {
                beforeTS = (long) Helpers.getField(Helpers.getField(event, "m_delegate"), "timeStamp");
            }
            if (message.equals("after")) {
                // two months later
                afterTS = beforeTS + (2L * 31L * 24L * 60L * 60L * 1000L);
                Helpers.setField(Helpers.getField(event, "m_delegate"), "timeStamp", afterTS);
            }
            return PaxFilter.NEUTRAL;
        }

        public long getAfterTS() {
            return afterTS;
        }

    }

}
