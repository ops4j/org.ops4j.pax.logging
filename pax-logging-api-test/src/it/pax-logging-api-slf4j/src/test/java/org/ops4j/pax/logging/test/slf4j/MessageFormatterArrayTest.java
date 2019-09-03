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

import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.logging.PaxContext;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingManager;
import org.ops4j.pax.logging.PaxLoggingService;
import org.ops4j.pax.logging.PaxMarker;
import org.ops4j.pax.logging.slf4j.Slf4jLoggerFactory;
import org.ops4j.pax.logging.spi.support.FormattingTriple;
import org.osgi.framework.Bundle;
import org.osgi.service.log.LogLevel;
import org.osgi.service.log.LoggerConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

public class MessageFormatterArrayTest {

    private String output;

    @Before
    public void setUp() throws Exception {
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
        assertEquals("warn:My message with 3 parameters : 1, 2, 3", output);
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

        @Override
        public boolean isTraceEnabled() {
            return true;
        }

        @Override
        public boolean isDebugEnabled() {
            return true;
        }

        @Override
        public boolean isWarnEnabled() {
            return true;
        }

        @Override
        public boolean isInfoEnabled() {
            return true;
        }

        @Override
        public boolean isErrorEnabled() {
            return true;
        }

        @Override
        public boolean isFatalEnabled() {
            return true;
        }

        @Override
        public boolean isTraceEnabled(PaxMarker marker) {
            return true;
        }

        @Override
        public boolean isDebugEnabled(PaxMarker marker) {
            return true;
        }

        @Override
        public boolean isWarnEnabled(PaxMarker marker) {
            return true;
        }

        @Override
        public boolean isInfoEnabled(PaxMarker marker) {
            return true;
        }

        @Override
        public boolean isErrorEnabled(PaxMarker marker) {
            return true;
        }

        @Override
        public boolean isFatalEnabled(PaxMarker marker) {
            return true;
        }

        @Override
        public void trace(String message) {
            output = "trace:" + message;
        }

        @Override
        public void trace(String format, Object arg) {
            trace(FormattingTriple.resolve(format, false, arg).getMessage());
        }

        @Override
        public void trace(String format, Object arg1, Object arg2) {
            trace(FormattingTriple.resolve(format, false, arg1, arg2).getMessage());
        }

        @Override
        public void trace(String format, Object... arguments) {
            trace(FormattingTriple.resolve(format, false, arguments).getMessage());
        }

        @Override
        public <E extends Exception> void trace(LoggerConsumer<E> consumer) throws E {
            consumer.accept(this);
        }

        @Override
        public void trace(PaxMarker marker, String message) {
            output = "trace:" + message;
        }

        @Override
        public void trace(PaxMarker marker, String format, Object arg) {
            trace(FormattingTriple.resolve(format, false, arg).getMessage());
        }

        @Override
        public void trace(PaxMarker marker, String format, Object arg1, Object arg2) {
            trace(FormattingTriple.resolve(format, false, arg1, arg2).getMessage());
        }

        @Override
        public void trace(PaxMarker marker, String format, Object... arguments) {
            trace(FormattingTriple.resolve(format, false, arguments).getMessage());
        }

        @Override
        public <E extends Exception> void trace(PaxMarker marker, LoggerConsumer<E> consumer) throws E {
            consumer.accept(this);
        }

        @Override
        public void debug(String message) {
            output = "debug:" + message;
        }

        @Override
        public void debug(String format, Object arg) {
            debug(FormattingTriple.resolve(format, false, arg).getMessage());
        }

        @Override
        public void debug(String format, Object arg1, Object arg2) {
            debug(FormattingTriple.resolve(format, false, arg1, arg2).getMessage());
        }

        @Override
        public void debug(String format, Object... arguments) {
            debug(FormattingTriple.resolve(format, false, arguments).getMessage());
        }

        @Override
        public <E extends Exception> void debug(LoggerConsumer<E> consumer) throws E {
            consumer.accept(this);
        }

        @Override
        public void debug(PaxMarker marker, String message) {
            output = "debug:" + message;
        }

        @Override
        public void debug(PaxMarker marker, String format, Object arg) {
            debug(FormattingTriple.resolve(format, false, arg).getMessage());
        }

        @Override
        public void debug(PaxMarker marker, String format, Object arg1, Object arg2) {
            debug(FormattingTriple.resolve(format, false, arg1, arg2).getMessage());
        }

        @Override
        public void debug(PaxMarker marker, String format, Object... arguments) {
            debug(FormattingTriple.resolve(format, false, arguments).getMessage());
        }

        @Override
        public <E extends Exception> void debug(PaxMarker marker, LoggerConsumer<E> consumer) throws E {
            consumer.accept(this);
        }

        @Override
        public void info(String message) {
            output = "info:" + message;
        }

        @Override
        public void info(String format, Object arg) {
            info(FormattingTriple.resolve(format, false, arg).getMessage());
        }

        @Override
        public void info(String format, Object arg1, Object arg2) {
            info(FormattingTriple.resolve(format, false, arg1, arg2).getMessage());
        }

        @Override
        public void info(String format, Object... arguments) {
            info(FormattingTriple.resolve(format, false, arguments).getMessage());
        }

        @Override
        public <E extends Exception> void info(LoggerConsumer<E> consumer) throws E {
            consumer.accept(this);
        }

        @Override
        public void info(PaxMarker marker, String message) {
            output = "info:" + message;
        }

        @Override
        public void info(PaxMarker marker, String format, Object arg) {
            info(FormattingTriple.resolve(format, false, arg).getMessage());
        }

        @Override
        public void info(PaxMarker marker, String format, Object arg1, Object arg2) {
            info(FormattingTriple.resolve(format, false, arg1, arg2).getMessage());
        }

        @Override
        public void info(PaxMarker marker, String format, Object... arguments) {
            info(FormattingTriple.resolve(format, false, arguments).getMessage());
        }

        @Override
        public <E extends Exception> void info(PaxMarker marker, LoggerConsumer<E> consumer) throws E {
            consumer.accept(this);
        }

        @Override
        public void warn(String message) {
            output = "warn:" + message;
        }

        @Override
        public void warn(String format, Object arg) {
            warn(FormattingTriple.resolve(format, false, arg).getMessage());
        }

        @Override
        public void warn(String format, Object arg1, Object arg2) {
            warn(FormattingTriple.resolve(format, false, arg1, arg2).getMessage());
        }

        @Override
        public void warn(String format, Object... arguments) {
            warn(FormattingTriple.resolve(format, false, arguments).getMessage());
        }

        @Override
        public <E extends Exception> void warn(LoggerConsumer<E> consumer) throws E {
            consumer.accept(this);
        }

        @Override
        public void warn(PaxMarker marker, String message) {
            output = "warn:" + message;
        }

        @Override
        public void warn(PaxMarker marker, String format, Object arg) {
            warn(FormattingTriple.resolve(format, false, arg).getMessage());
        }

        @Override
        public void warn(PaxMarker marker, String format, Object arg1, Object arg2) {
            warn(FormattingTriple.resolve(format, false, arg1, arg2).getMessage());
        }

        @Override
        public void warn(PaxMarker marker, String format, Object... arguments) {
            warn(FormattingTriple.resolve(format, false, arguments).getMessage());
        }

        @Override
        public <E extends Exception> void warn(PaxMarker marker, LoggerConsumer<E> consumer) throws E {
            consumer.accept(this);
        }

        @Override
        public void error(String message) {
            output = "error:" + message;
        }

        @Override
        public void error(String format, Object arg) {
            error(FormattingTriple.resolve(format, false, arg).getMessage());
        }

        @Override
        public void error(String format, Object arg1, Object arg2) {
            error(FormattingTriple.resolve(format, false, arg1, arg2).getMessage());
        }

        @Override
        public void error(String format, Object... arguments) {
            error(FormattingTriple.resolve(format, false, arguments).getMessage());
        }

        @Override
        public <E extends Exception> void error(LoggerConsumer<E> consumer) throws E {
            consumer.accept(this);
        }

        @Override
        public void error(PaxMarker marker, String message) {
            output = "error:" + message;
        }

        @Override
        public void error(PaxMarker marker, String format, Object arg) {
            error(FormattingTriple.resolve(format, false, arg).getMessage());
        }

        @Override
        public void error(PaxMarker marker, String format, Object arg1, Object arg2) {
            error(FormattingTriple.resolve(format, false, arg1, arg2).getMessage());
        }

        @Override
        public void error(PaxMarker marker, String format, Object... arguments) {
            error(FormattingTriple.resolve(format, false, arguments).getMessage());
        }

        @Override
        public <E extends Exception> void error(PaxMarker marker, LoggerConsumer<E> consumer) throws E {
            consumer.accept(this);
        }

        @Override
        public void fatal(String message) {
            output = "fatal:" + message;
        }

        @Override
        public void fatal(String format, Object arg) {
            fatal(FormattingTriple.resolve(format, false, arg).getMessage());
        }

        @Override
        public void fatal(String format, Object arg1, Object arg2) {
            fatal(FormattingTriple.resolve(format, false, arg1, arg2).getMessage());
        }

        @Override
        public void fatal(String format, Object... arguments) {
            fatal(FormattingTriple.resolve(format, false, arguments).getMessage());
        }

        @Override
        public <E extends Exception> void fatal(LoggerConsumer<E> consumer) throws E {
            consumer.accept(this);
        }

        @Override
        public void fatal(PaxMarker marker, String message) {
            output = "fatal:" + message;
        }

        @Override
        public void fatal(PaxMarker marker, String format, Object arg) {
            fatal(FormattingTriple.resolve(format, false, arg).getMessage());
        }

        @Override
        public void fatal(PaxMarker marker, String format, Object arg1, Object arg2) {
            fatal(FormattingTriple.resolve(format, false, arg1, arg2).getMessage());
        }

        @Override
        public void fatal(PaxMarker marker, String format, Object... arguments) {
            fatal(FormattingTriple.resolve(format, false, arguments).getMessage());
        }

        @Override
        public <E extends Exception> void fatal(PaxMarker marker, LoggerConsumer<E> consumer) throws E {
            consumer.accept(this);
        }

        @Override
        public void audit(String message) {
            output = "audit:" + message;
        }

        @Override
        public void audit(String format, Object arg) {
            audit(FormattingTriple.resolve(format, false, arg).getMessage());
        }

        @Override
        public void audit(String format, Object arg1, Object arg2) {
            audit(FormattingTriple.resolve(format, false, arg1, arg2).getMessage());
        }

        @Override
        public void audit(String format, Object... arguments) {
            audit(FormattingTriple.resolve(format, false, arguments).getMessage());
        }

        @Override
        public <E extends Exception> void audit(LoggerConsumer<E> consumer) throws E {
            consumer.accept(this);
        }

        @Override
        public void audit(PaxMarker marker, String message) {
            output = "audit:" + message;
        }

        @Override
        public void audit(PaxMarker marker, String format, Object arg) {
            audit(FormattingTriple.resolve(format, false, arg).getMessage());
        }

        @Override
        public void audit(PaxMarker marker, String format, Object arg1, Object arg2) {
            audit(FormattingTriple.resolve(format, false, arg1, arg2).getMessage());
        }

        @Override
        public void audit(PaxMarker marker, String format, Object... arguments) {
            audit(FormattingTriple.resolve(format, false, arguments).getMessage());
        }

        @Override
        public <E extends Exception> void audit(PaxMarker marker, LoggerConsumer<E> consumer) throws E {
            consumer.accept(this);
        }

        @Override
        public void fqtrace(String fqcn, String message) {
            trace(message);
        }

        @Override
        public void fqdebug(String fqcn, String message) {
            debug(message);
        }

        @Override
        public void fqinfo(String fqcn, String message) {
            info(message);
        }

        @Override
        public void fqwarn(String fqcn, String message) {
            warn(message);
        }

        @Override
        public void fqerror(String fqcn, String message) {
            error(message);
        }

        @Override
        public void fqfatal(String fqcn, String message) {
            fatal(message);
        }

        @Override
        public void fqtrace(String fqcn, PaxMarker marker, String message) {
            trace(marker, message);
        }

        @Override
        public void fqdebug(String fqcn, PaxMarker marker, String message) {
            debug(marker, message);
        }

        @Override
        public void fqinfo(String fqcn, PaxMarker marker, String message) {
            info(marker, message);
        }

        @Override
        public void fqwarn(String fqcn, PaxMarker marker, String message) {
            warn(marker, message);
        }

        @Override
        public void fqerror(String fqcn, PaxMarker marker, String message) {
            error(marker, message);
        }

        @Override
        public void fqfatal(String fqcn, PaxMarker marker, String message) {
            fatal(marker, message);
        }

        @Override
        public void fqtrace(String fqcn, String message, Throwable t) {
            trace(message, t);
        }

        @Override
        public void fqdebug(String fqcn, String message, Throwable t) {
            debug(message, t);
        }

        @Override
        public void fqinfo(String fqcn, String message, Throwable t) {
            info(message, t);
        }

        @Override
        public void fqwarn(String fqcn, String message, Throwable t) {
            warn(message, t);
        }

        @Override
        public void fqerror(String fqcn, String message, Throwable t) {
            error(message, t);
        }

        @Override
        public void fqfatal(String fqcn, String message, Throwable t) {
            fatal(message, t);
        }

        @Override
        public void fqtrace(String fqcn, PaxMarker marker, String message, Throwable t) {
            trace(marker, message, t);
        }

        @Override
        public void fqdebug(String fqcn, PaxMarker marker, String message, Throwable t) {
            debug(marker, message, t);
        }

        @Override
        public void fqinfo(String fqcn, PaxMarker marker, String message, Throwable t) {
            info(marker, message, t);
        }

        @Override
        public void fqwarn(String fqcn, PaxMarker marker, String message, Throwable t) {
            warn(marker, message, t);
        }

        @Override
        public void fqerror(String fqcn, PaxMarker marker, String message, Throwable t) {
            error(marker, message, t);
        }

        @Override
        public void fqfatal(String fqcn, PaxMarker marker, String message, Throwable t) {
            fatal(marker, message, t);
        }

        @Override
        public int getPaxLogLevel() {
            return 0;
        }

        @Override
        public LogLevel getLogLevel() {
            return LogLevel.AUDIT;
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

        @Override
        public PaxLogger getLogger(Bundle bundle, String category, String fqcn) {
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
