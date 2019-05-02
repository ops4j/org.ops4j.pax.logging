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
package org.ops4j.pax.logging.test.log4j1;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.LogMF;
import org.apache.log4j.LogManager;
import org.apache.log4j.LogSF;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.NDC;
import org.apache.log4j.helpers.Loader;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.HierarchyEventListener;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * <p>This unit test shows different log4j1 API usages. It's hard to distinguish public vs non public API with log4j1.
 * OSGi manifest of original bundle may provide some hints, but comments in source code itself shows that some classes,
 * even in {@code org.apache.log4j} package (the main one) should rather not be used directly.</p>
 * <p>{@link Logger} class is the most commonly used, but it has generally 3 responsibilities:<ul>
 *     <li>{@link Logger#getLogger(String)} and similar static methods used as factory methods</li>
 *     <li>{@link Logger#info(Object)} and similar instance methods used to log messages/throwables</li>
 *     <li>Methods related to setting level, priority, additivity and appenders, which should be rarely used or rather
 *     not through {@link Logger} <em>interface</em>...</li>
 * </ul></p>
 */
public class Log4j1NativeApiTest {

    @Test
    public void simplestUsage() {
        Logger log = Logger.getLogger(Log4j1NativeApiTest.class);
        MDC.put("user", "me");
        MDC.put("country", "Equestria");
        NDC.push("layer42");
        NDC.push("layer43");

        log.info("simplestUsage - INFO");
        log.trace("simplestUsage - TRACE");

        Logger.getLogger("special").trace("simplestUsage - TRACE");
    }

    @Test
    public void loggerAPI() {
        Logger log = Logger.getLogger(Log4j1NativeApiTest.class);

        log.info("loggerAPI - INFO1");
        log.trace("loggerAPI - TRACE1");
        assertFalse(log.isTraceEnabled());

        Level l = log.getLevel();
        log.setLevel(Level.ALL);
        log.info("loggerAPI - INFO2");
        log.trace("loggerAPI - TRACE2");
        assertTrue(log.isTraceEnabled());
        log.setLevel(l);

        boolean found = false;
        for (Enumeration<?> e = log.getParent().getAllAppenders(); e.hasMoreElements(); ) {
            found = true;
            Appender a = (Appender) e.nextElement();
            log.info("Appender: " + a);
            assertTrue(log.getParent().isAttached(a));
        }
        assertTrue("Should've found any appender", found);

        final List<LoggingEvent> events = new LinkedList<>();
        AppenderSkeleton newAppender = new AppenderSkeleton() {
            @Override
            protected void append(LoggingEvent event) {
                events.add(event);
            }

            @Override
            public void close() {
            }

            @Override
            public boolean requiresLayout() {
                return false;
            }
        };
        log.addAppender(newAppender);

        assertThat(events.size(), equalTo(0));
        log.info("just checking");
        assertThat(events.size(), equalTo(1));

        assertTrue(log.isAttached(newAppender));
        log.removeAppender(newAppender);
        assertFalse(log.isAttached(newAppender));

        log.info("just checking");
        assertThat(events.size(), equalTo(1));
    }

    @Test
    public void logManagerAPI() {
        assertSame(LogManager.getLogger(Log4j1NativeApiTest.class), Logger.getLogger(Log4j1NativeApiTest.class));
        assertSame(LogManager.getLoggerRepository().getRootLogger(), Logger.getRootLogger());
        boolean found = false;
        for (Enumeration<?> e = LogManager.getCurrentLoggers(); e.hasMoreElements(); ) {
            Logger logger = (Logger) e.nextElement();
            if (!found && Log4j1NativeApiTest.class.getName().equals(logger.getName())) {
                found = true;
            }
            System.out.println("logger: " + logger.getName());
        }
        assertTrue("Should've found logger by name in the repository", found);
    }

    @Test
    public void loggerRepositoryAndAppenderAPI() {
        final boolean[] appenderAdded = { false };
        final boolean[] appenderRemoved = { false };

        LoggerRepository repo = LogManager.getLoggerRepository();
        repo.addHierarchyEventListener(new HierarchyEventListener() {
            @Override
            public void addAppenderEvent(Category cat, Appender appender) {
                appenderAdded[0] = true;
            }

            @Override
            public void removeAppenderEvent(Category cat, Appender appender) {
                appenderRemoved[0] = true;
            }
        });

        final List<LoggingEvent> events = new LinkedList<>();
        AppenderSkeleton newAppender = new AppenderSkeleton() {
            @Override
            public void close() {
            }

            @Override
            public boolean requiresLayout() {
                return false;
            }

            @Override
            protected void append(LoggingEvent event) {
                events.add(event);
            }
        };
        repo.getRootLogger().addAppender(newAppender);

        Logger.getRootLogger().info("Hello");
        Logger.getRootLogger().trace("Hello");
        assertThat(events.size(), equalTo(1));
        assertTrue(appenderAdded[0] && !appenderRemoved[0]);

        repo.getRootLogger().removeAppender(newAppender);

        Logger.getRootLogger().info("Hello");
        Logger.getRootLogger().trace("Hello");
        assertThat(events.size(), equalTo(1));
        assertTrue(appenderAdded[0] && appenderRemoved[0]);

        Level t = repo.getThreshold();
        assertFalse(repo.isDisabled(Level.TRACE_INT));
        repo.setThreshold("WARN");
        assertTrue(repo.isDisabled(Level.TRACE_INT));
        repo.setThreshold(t);
    }

    @Test
    public void loaderApi() throws Exception {
        Thread.currentThread().setContextClassLoader(null);
        // check some internal class and resources from log4j:log4j
        assertNotNull(Loader.loadClass("org.apache.log4j.lf5.LF5Appender"));
        assertNotNull(Loader.getResource("org/apache/log4j/xml/log4j.dtd"));
        assertNotNull(Loader.getResource("META-INF/maven/log4j/log4j/pom.xml"));
    }

    @Test
    public void logLogApi() throws Exception {
        LogLog.setQuietMode(false);
        LogLog.setInternalDebugging(true);
        LogLog.warn("log log warning1", null);
        LogLog.debug("log log debug1", null);
        LogLog.setInternalDebugging(false);
        LogLog.warn("log log warning2", null);
        LogLog.debug("log log debug2", null);
    }

    /**
     * Test using {@link java.text.MessageFormat} formatting syntax.
     * @throws Exception
     */
    @Test
    public void logXFApi() throws Exception {
        LogMF.info(Logger.getLogger("logXFApi"), "{0}, {0} {1}!", "Hello", "World");
    }

    /**
     * Test using SLF4J like pattern formatting syntax.
     * @throws Exception
     */
    @Test
    public void logSFApi() throws Exception {
        LogSF.info(Logger.getLogger("logSFApi"), "{} {}!", "Hello", "World");
    }

}
