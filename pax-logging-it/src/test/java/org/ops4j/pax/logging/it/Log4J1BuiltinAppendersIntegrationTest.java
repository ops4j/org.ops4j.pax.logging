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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.swing.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.logging.PaxLoggingConstants;
import org.ops4j.pax.logging.it.support.Helpers;
import org.ops4j.pax.logging.spi.PaxFilter;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.OptionUtils.combine;

/**
 * <p>Testing appenders of classes included in log4j:log4j and log4j:apache-log4j-extras - possibly optimized
 * in pax-logging-service Private-Packaged versions of the above.</p>
 * <p>Each {@code @Test} method generally changes configuration, waits for event admin notification and tests
 * the situation. There's no need to restart pax-logging-service after hijacking stdout, so {@link #hijackStdout()}
 * is not overriden here.</p>
 */
@RunWith(PaxExam.class)
public class Log4J1BuiltinAppendersIntegrationTest extends AbstractStdoutInterceptingIntegrationTestBase {

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
        properties.put(PaxLoggingConstants.FILTER_NAME_PROPERTY, "timestampReplacer");
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
    public void listModelAppenderAppenderFromExtras() {
        Helpers.updateLoggingConfig(context, cm, Helpers.LoggingLibrary.LOG4J1, "builtin.list");

        Logger log = LoggerFactory.getLogger("my.logger");
        log.info("should be added to list");

        List<?> appenders = Helpers.getField(log, "m_delegate.m_delegate.m_delegate.aai.appenderList", List.class);
        ListModel model = Helpers.getField(appenders.get(0), "model", ListModel.class);
        Object obj = model.getElementAt(0);
        assertThat(obj.getClass().getName(), equalTo("org.apache.log4j.spi.LoggingEvent"));
        assertThat(Helpers.getField(obj, "message", String.class), equalTo("should be added to list"));
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
