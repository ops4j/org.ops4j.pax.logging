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
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.NDC;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.HierarchyEventListener;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Differently than with {@code org.ops4j.pax.logging.test:log4j} here, the same log4j1 API usage may lead to different
 * results, because pax-logging implements some classes differently and some methods are dummy.
 * The most important logging methods delegate to pax-logging mechanisms.
 */
public class Log4j1PaxLoggingApiTest {

    @Test
    public void simplestUsage() {
        Logger log = Logger.getLogger(Log4j1PaxLoggingApiTest.class);
        MDC.put("user", "me");
        MDC.put("country", "Equestria");
        NDC.push("layer42");
        NDC.push("layer43");

        log.info("INFO");
        log.trace("TRACE");

        Logger.getLogger("special").trace("TRACE");
    }

    @Test
    public void logLog() {
        LogLog.setInternalDebugging(true);
        LogLog.debug("LogLog debug1");
        LogLog.error("LogLog error1");
        LogLog.setInternalDebugging(false);
        LogLog.debug("LogLog debug2");
        LogLog.error("LogLog error2");
    }

    @Test
    @Ignore("for now - until non logging methods from org.apache.log4j.Logger are handled well")
    public void logManagerAPI() {
        assertSame(LogManager.getLogger(Log4j1PaxLoggingApiTest.class), Logger.getLogger(Log4j1PaxLoggingApiTest.class));
        assertSame(LogManager.getLoggerRepository().getRootLogger(), Logger.getRootLogger());
        boolean found = false;
        for (Enumeration<?> e = LogManager.getCurrentLoggers(); e.hasMoreElements(); ) {
            Logger logger = (Logger) e.nextElement();
            if (!found && Log4j1PaxLoggingApiTest.class.getName().equals(logger.getName())) {
                found = true;
            }
            System.out.println("logger: " + logger.getName());
        }
        assertTrue("Should've found logger by name in the repository", found);
    }

    @Test
    @Ignore("for now - until non logging methods from org.apache.log4j.Logger are handled well")
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
    }

}
