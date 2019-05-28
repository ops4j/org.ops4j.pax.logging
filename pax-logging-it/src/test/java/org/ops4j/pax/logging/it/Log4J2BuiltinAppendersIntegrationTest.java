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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingService;
import org.ops4j.pax.logging.it.support.Helpers;
import org.ops4j.pax.tinybundles.core.TinyBundles;
import org.osgi.framework.Bundle;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.OptionUtils.combine;

@RunWith(PaxExam.class)
public class Log4J2BuiltinAppendersIntegrationTest extends AbstractStdoutInterceptingIntegrationTestBase {

    @Inject
    private ConfigurationAdmin cm;

    @Configuration
    public Option[] configure() throws IOException {
        prepareBundles();

        return combine(
                combine(baseConfigure(), defaultLoggingConfig()),

                paxLoggingApi(),
                paxLoggingLog4J2(),
                configAdmin(),
                eventAdmin(),

                mavenBundle("org.ops4j.pax.logging", "pax-logging-sample-fragment-log4j2").versionAsInProject().noStart()
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
                .build();
        new File("target/bundles").mkdirs();
        IOUtils.copy(bundle1, new FileOutputStream("target/bundles/mdc-bundle1.jar"));

        InputStream bundle2 = TinyBundles.bundle()
                .set("Bundle-ManifestVersion", "2")
                .set("Bundle-SymbolicName", "b2")
                .build();
        new File("target/bundles").mkdirs();
        IOUtils.copy(bundle2, new FileOutputStream("target/bundles/mdc-bundle2.jar"));
    }

    @Test
    public void plainFileAppender() {
        Helpers.updateLoggingConfig(context, cm, Helpers.LoggingLibrary.LOG4J2_PROPERTIES, "builtin.file");

        Logger log = LoggerFactory.getLogger("my.logger");
        log.info("Hello into FileAppender");

        List<String> lines = readLines("target/logs-log4j2/file-appender.log");

        assertTrue(lines.contains("my.logger/org.ops4j.pax.logging.it.Log4J2BuiltinAppendersIntegrationTest [INFO] Hello into FileAppender"));
    }

    @Test
    public void rollingFileAppender() {
        Helpers.updateLoggingConfig(context, cm, Helpers.LoggingLibrary.LOG4J2_XML, "builtin.rolling");

        Logger log = LoggerFactory.getLogger("my.logger");
        log.info("before");
        log.info("after");

        // there should simply be two files
        File[] files = new File("target/logs-log4j2").listFiles((dir, name) -> name.startsWith("rf-appender.log"));
        assertNotNull(files);
        assertThat(files.length, equalTo(2));
    }


    @Test
    public void socketAppender() throws Exception {
        final ServerSocket ss = new ServerSocket(0);
        final int port = ss.getLocalPort();
        final CountDownLatch latch1 = new CountDownLatch(1);
        final CountDownLatch latch2 = new CountDownLatch(1);

        Thread t = new Thread(() -> {
            try {
                Socket s = ss.accept();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int read = 0;
                InputStream is = s.getInputStream();
                latch2.await();
                while (is.available() > 0) {
                    read = is.read(buf);
                    out.write(buf, 0, read);
                }

                // in Log4j1 we had ObjectInputStream. Now it's byt array
                // from org.apache.logging.log4j.core.layout.PatternLayout.encode()
                assertTrue(new String(out.toByteArray()).startsWith("my.logger/org.ops4j.pax.logging.it.Log4J2BuiltinAppendersIntegrationTest [INFO] socket message"));

                s.close();
                latch1.countDown();
            } catch (Throwable e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });
        t.start();

        Helpers.updateLoggingConfig(context, cm, Helpers.LoggingLibrary.LOG4J2_PROPERTIES, "builtin.socket", props -> {
            props.put("log4j2.appender.socket.port", Integer.toString(port));
        });

        Logger log = LoggerFactory.getLogger("my.logger");
        log.info("socket message");
        latch2.countDown();

        assertTrue(latch1.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void listAppender() throws InvalidSyntaxException {
        Helpers.updateLoggingConfig(context, cm, Helpers.LoggingLibrary.LOG4J2_PROPERTIES, "builtin.list");

        Logger log = LoggerFactory.getLogger("my.logger");
        log.info("should be added to list");

        Collection<ServiceReference<List>> srs = context.getServiceReferences(List.class, "(name=l)");
        assertThat(srs.size(), equalTo(1));
        ServiceReference<List> sr = srs.iterator().next();
        List<?> list = context.getService(sr);
        Object obj = list.get(0);

        assertThat(obj.getClass().getName(), equalTo("org.apache.logging.log4j.core.impl.Log4jLogEvent"));
        assertThat(Helpers.getField(obj, "message.message", String.class), equalTo("should be added to list"));
    }

    @Test
    public void mdcSiftingAppender() throws Exception {
        File f1 = new File("target/bundles/mdc-bundle1.jar");
        File f2 = new File("target/bundles/mdc-bundle2.jar");
        Bundle b1 = context.installBundle(f1.toURI().toURL().toString(), new FileInputStream(f1));
        Bundle b2 = context.installBundle(f2.toURI().toURL().toString(), new FileInputStream(f2));
        b1.start();
        b2.start();

        Helpers.updateLoggingConfig(context, cm, Helpers.LoggingLibrary.LOG4J2_XML, "mdc.appender");

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

        l1.inform("Hello from b1/l1", null);
        l1a.inform("Hello from b1/l2", null);
        l2.inform("Hello from b2/l1", null);
        l2a.inform("Hello from b2/l2", null);

        List<String> linesB1 = readLines("target/logs-log4j2/b1-file-appender.log");
        List<String> linesB2 = readLines("target/logs-log4j2/b2-file-appender.log");

        assertTrue(linesB1.stream().allMatch(l -> l.contains("bundle.name=b1") && l.contains("Hello from b1/")));
        assertTrue(linesB2.stream().allMatch(l -> l.contains("bundle.name=b2") && l.contains("Hello from b2/")));
    }

    @Test
    public void mdcSiftingAppenderWithNonBundleKey() throws Exception {
        Helpers.updateLoggingConfig(context, cm, Helpers.LoggingLibrary.LOG4J2_PROPERTIES, "mdc.appender2");

        Logger log = LoggerFactory.getLogger("my.logger");

        log.info("Hello into FileAppender 1");
        MDC.put("my.key", "f1");
        log.info("Hello into FileAppender 2");
        MDC.put("my.key", "f2");
        log.info("Hello into FileAppender 3");
        MDC.remove("my.key");
        log.info("Hello into FileAppender 4");

        List<String> lines1 = readLines("target/logs-log4j2/default-file-appender.log");
        List<String> lines2 = readLines("target/logs-log4j2/f1-file-appender.log");
        List<String> lines3 = readLines("target/logs-log4j2/f2-file-appender.log");

        assertTrue(lines1.stream().anyMatch(l -> l.contains("Hello into FileAppender 1")));
        assertTrue(lines2.stream().anyMatch(l -> l.contains("Hello into FileAppender 2")));
        assertTrue(lines3.stream().anyMatch(l -> l.contains("Hello into FileAppender 3")));
        assertTrue(lines1.stream().anyMatch(l -> l.contains("Hello into FileAppender 4")));
    }

}
