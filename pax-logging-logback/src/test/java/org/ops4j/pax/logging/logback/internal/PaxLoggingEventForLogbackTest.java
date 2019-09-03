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
package org.ops4j.pax.logging.logback.internal;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import org.junit.Test;
import org.ops4j.pax.logging.logback.internal.spi.PaxLevelImpl;
import org.ops4j.pax.logging.logback.internal.spi.PaxLoggingEventForLogback;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author cdolan
 * @since 5/2/12 1:55 PM
 */
public class PaxLoggingEventForLogbackTest {

    @Test
    public void test() {
        long before = System.currentTimeMillis();

        LoggerContext context = new LoggerContext();
        Logger logger = context.getLogger("mylogger");
        LoggingEvent levent = new LoggingEvent("myfqcn", logger, Level.INFO, "foo {} baz", null, new Object[] { "bar" });

        PaxLoggingEventForLogback event = new PaxLoggingEventForLogback(levent);
        assertEquals(new PaxLevelImpl(Level.INFO), event.getLevel());
        assertEquals("foo bar baz", event.getRenderedMessage());
        assertEquals("foo {} baz", event.getMessage());
        assertEquals("mylogger", event.getLoggerName());
        assertEquals(Logger.class.getName(), event.getFQNOfLoggerClass());
        assertEquals(Thread.currentThread().getName(), event.getThreadName());
        assertFalse(event.locationInformationExists());
        assertTrue(before <= event.getTimeStamp());
        assertTrue(System.currentTimeMillis() >= event.getTimeStamp());
    }

}
