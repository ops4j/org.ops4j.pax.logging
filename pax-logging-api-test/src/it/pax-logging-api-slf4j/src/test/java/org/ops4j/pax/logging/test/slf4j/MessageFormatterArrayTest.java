/*
 * Copyright 2006 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.logging.test.slf4j;

import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.logging.PaxContext;
import org.ops4j.pax.logging.PaxLoggingManager;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingService;
import org.ops4j.pax.logging.slf4j.Slf4jLoggerFactory;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;

public class MessageFormatterArrayTest {

    private String output;

    @Before
    protected void setUp() throws Exception {
        output = "habba";
        PaxLoggingManager manager = new TestLoggingManager();
        Field member = Slf4jLoggerFactory.class.getDeclaredField("m_paxLogging");
        member.setAccessible(true);
        member.set(null, manager);
    }

    @Test
    public void testTrace() {
        Logger logger = LoggerFactory.getLogger(MessageFormatterArrayTest.class);
        logger.trace("My message with 3 parameters : {}, {}, {}", "1", "2", "3");
        assertEquals("trace:My message with 3 parameters : 1, 2, 3", output);
    }

    @Test
    public void testDebug() {
        Logger logger = LoggerFactory.getLogger(MessageFormatterArrayTest.class);
        logger.debug("My message with 3 parameters : {}, {}, {}", "1", "2", "3");
        assertEquals("debug:My message with 3 parameters : 1, 2, 3", output);
    }

    @Test
    public void testWarn() {
        Logger logger = LoggerFactory.getLogger(MessageFormatterArrayTest.class);
        logger.warn("My message with 3 parameters : {}, {}, {}", "1", "2", "3");
        assertEquals("warning:My message with 3 parameters : 1, 2, 3", output);
    }

    @Test
    public void testInfo() {
        Logger logger = LoggerFactory.getLogger(MessageFormatterArrayTest.class);
        logger.info("My message with 3 parameters : {}, {}, {}", "1", "2", "3");
        assertEquals("info:My message with 3 parameters : 1, 2, 3", output);
    }

    @Test
    public void testError() {
        Logger logger = LoggerFactory.getLogger(MessageFormatterArrayTest.class);
        logger.error("My message with 3 parameters : {}, {}, {}", "1", "2", "3");
        assertEquals("error:My message with 3 parameters : 1, 2, 3", output);
    }

    public class TestLogger implements PaxLogger {

        public boolean isTraceEnabled() {
            return true;
        }

        public boolean isDebugEnabled() {
            return true;
        }

        public boolean isWarnEnabled() {
            return true;
        }

        public boolean isInfoEnabled() {
            return true;
        }

        public boolean isErrorEnabled() {
            return true;
        }

        public boolean isFatalEnabled() {
            return true;
        }

        public void trace(String message, Throwable t) {
            output = "trace:" + message;
        }

        public void debug(String message, Throwable t) {
            output = "debug:" + message;
        }

        public void inform(String message, Throwable t) {
            output = "info:" + message;
        }

        public void warn(String message, Throwable t) {
            output = "warning:" + message;
        }

        public void error(String message, Throwable t) {
            output = "error:" + message;
        }

        public void fatal(String message, Throwable t) {
            output = message;
        }

        public void trace(String message, Throwable t, String fqcn) {
            trace(message, t);
        }

        public void debug(String message, Throwable t, String fqcn) {
            trace(message, t);
        }

        public void inform(String message, Throwable t, String fqcn) {
            trace(message, t);
        }

        public void warn(String message, Throwable t, String fqcn) {
            trace(message, t);
        }

        public void error(String message, Throwable t, String fqcn) {
            trace(message, t);
        }

        public void fatal(String message, Throwable t, String fqcn) {
            trace(message, t);
        }

        public int getLogLevel() {
            return 0;
        }

        public String getName() {
            return null;
        }

        public PaxContext getPaxContext() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public class TestLoggingManager implements PaxLoggingManager {

        public PaxLogger getLogger(String category, String fqcn) {
            return new TestLogger();
        }

        public PaxLoggingService getPaxLoggingService() {
            return null;
        }

        public void open() {
        }

        public void close() {
        }

        public void dispose() {
        }

        public Bundle getBundle() {
            return null;
        }
    }

}
