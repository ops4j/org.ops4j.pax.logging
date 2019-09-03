/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * Copyright 2005 Niclas Hedhman
 * Copyright 2007 Hiram Chirino
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.logging.spi.support;

import java.io.PrintStream;

import org.ops4j.pax.logging.PaxContext;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxMarker;
import org.osgi.framework.Bundle;
import org.osgi.service.log.LogLevel;
import org.osgi.service.log.LoggerConsumer;

/**
 * <p>This Logger will be used when the Pax Logging Service is not (yet) available.</p>
 *
 * <p>Default threshold is DEBUG but can be changed if the {@link org.ops4j.pax.logging.PaxLoggingConstants#LOGGING_CFG_DEFAULT_LOG_LEVEL}
 * system or context property is set to on of the following: TRACE, DEBUG, INFO, WARN, ERROR, FATAL, or NONE,
 * by calling the static method {@link #setLogLevel(String)}, where <b>threshold</b> is one of the same strings.</p>
 *
 * <p>Since R7, This logger always uses Slf4J kind of formatting.</p>
 */
public class DefaultServiceLog implements PaxLogger {

    public static final String[] levels = {
            "TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL", "AUDIT", "NONE"
    };

    /** A threshold level for default log service */
    private static int threshold;

    private Bundle m_bundle;
    private String m_categoryName;
    private PaxContext m_context = new PaxContext();

    DefaultServiceLog(Bundle bundle, String categoryName) {
        m_bundle = bundle;
        m_categoryName = categoryName;
    }

    public static int getStaticLogLevel() {
        return threshold;
    }

    private static int convertLevel(String levelName) {
        if ("TRACE".equalsIgnoreCase(levelName) || "FINER".equalsIgnoreCase(levelName)) {
            return PaxLogger.LEVEL_TRACE;
        } else if ("DEBUG".equalsIgnoreCase(levelName) || "FINE".equalsIgnoreCase(levelName)) {
            return PaxLogger.LEVEL_DEBUG;
        } else if ("INFO".equalsIgnoreCase(levelName) || "CONFIG".equalsIgnoreCase(levelName) || "CONF".equalsIgnoreCase(levelName)) {
            return PaxLogger.LEVEL_INFO;
        } else if ("WARN".equalsIgnoreCase(levelName) || "WARNING".equalsIgnoreCase(levelName)) {
            return PaxLogger.LEVEL_WARNING;
        } else if ("ERROR".equalsIgnoreCase(levelName) || "SEVERE".equalsIgnoreCase(levelName)) {
            return PaxLogger.LEVEL_ERROR;
        } else if ("FATAL".equalsIgnoreCase(levelName)) {
            return PaxLogger.LEVEL_FATAL;
        } else if ("NONE".equalsIgnoreCase(levelName) || "OFF".equalsIgnoreCase(levelName) || "DISABLED".equalsIgnoreCase(levelName)) {
            return PaxLogger.LEVEL_NONE;
        } else {
            return PaxLogger.LEVEL_DEBUG;
        }
    }

    @Override
    public boolean isTraceEnabled() {
        return threshold <= PaxLogger.LEVEL_TRACE;
    }

    @Override
    public boolean isDebugEnabled() {
        return threshold <= PaxLogger.LEVEL_DEBUG;
    }

    @Override
    public boolean isInfoEnabled() {
        return threshold <= PaxLogger.LEVEL_INFO;
    }

    @Override
    public boolean isWarnEnabled() {
        return threshold <= PaxLogger.LEVEL_WARNING;
    }

    @Override
    public boolean isErrorEnabled() {
        return threshold <= PaxLogger.LEVEL_ERROR;
    }

    @Override
    public boolean isFatalEnabled() {
        return threshold <= PaxLogger.LEVEL_FATAL;
    }

    @Override
    public boolean isTraceEnabled(PaxMarker marker) {
        return threshold <= PaxLogger.LEVEL_TRACE;
    }

    @Override
    public boolean isDebugEnabled(PaxMarker marker) {
        return threshold <= PaxLogger.LEVEL_DEBUG;
    }

    @Override
    public boolean isInfoEnabled(PaxMarker marker) {
        return threshold <= PaxLogger.LEVEL_INFO;
    }

    @Override
    public boolean isWarnEnabled(PaxMarker marker) {
        return threshold <= PaxLogger.LEVEL_WARNING;
    }

    // R7: org.osgi.service.log.Logger

    @Override
    public boolean isErrorEnabled(PaxMarker marker) {
        return threshold <= PaxLogger.LEVEL_ERROR;
    }

    @Override
    public boolean isFatalEnabled(PaxMarker marker) {
        return threshold <= PaxLogger.LEVEL_FATAL;
    }

    @Override
    public void trace(String message) {
        if (isTraceEnabled()) {
            output(levels[PaxLogger.LEVEL_TRACE], message, null);
        }
    }

    @Override
    public void trace(String format, Object arg) {
        if (isTraceEnabled()) {
            FormattingTriple ft = FormattingTriple.resolve(format, false, arg);
            output(levels[PaxLogger.LEVEL_TRACE], ft.getMessage(), ft.getThrowable());
        }
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        if (isTraceEnabled()) {
            FormattingTriple ft = FormattingTriple.resolve(format, false, arg1, arg2);
            output(levels[PaxLogger.LEVEL_TRACE], ft.getMessage(), ft.getThrowable());
        }
    }

    @Override
    public void trace(String format, Object... arguments) {
        if (isTraceEnabled()) {
            FormattingTriple ft = FormattingTriple.resolve(format, false, arguments);
            output(levels[PaxLogger.LEVEL_TRACE], ft.getMessage(), ft.getThrowable());
        }
    }

    @Override
    public <E extends Exception> void trace(LoggerConsumer<E> consumer) throws E {
        if (isTraceEnabled()) {
            consumer.accept(this);
        }
    }

    @Override
    public void trace(PaxMarker marker, String message) {
        trace(message);
    }

    @Override
    public void trace(PaxMarker marker, String format, Object arg) {
        trace(format, arg);
    }

    @Override
    public void trace(PaxMarker marker, String format, Object arg1, Object arg2) {
        trace(format, arg1, arg2);
    }

    @Override
    public void trace(PaxMarker marker, String format, Object... arguments) {
        trace(format, arguments);
    }

    @Override
    public <E extends Exception> void trace(PaxMarker marker, LoggerConsumer<E> consumer) throws E {
        trace(consumer);
    }

    @Override
    public void debug(String message) {
        if (isDebugEnabled()) {
            output(levels[PaxLogger.LEVEL_DEBUG], message, null);
        }
    }

    @Override
    public void debug(String format, Object arg) {
        if (isDebugEnabled()) {
            FormattingTriple ft = FormattingTriple.resolve(format, false, arg);
            output(levels[PaxLogger.LEVEL_DEBUG], ft.getMessage(), ft.getThrowable());
        }
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        if (isDebugEnabled()) {
            FormattingTriple ft = FormattingTriple.resolve(format, false, arg1, arg2);
            output(levels[PaxLogger.LEVEL_DEBUG], ft.getMessage(), ft.getThrowable());
        }
    }

    @Override
    public void debug(String format, Object... arguments) {
        if (isDebugEnabled()) {
            FormattingTriple ft = FormattingTriple.resolve(format, false, arguments);
            output(levels[PaxLogger.LEVEL_DEBUG], ft.getMessage(), ft.getThrowable());
        }
    }

    @Override
    public <E extends Exception> void debug(LoggerConsumer<E> consumer) throws E {
        if (isDebugEnabled()) {
            consumer.accept(this);
        }
    }

    @Override
    public void debug(PaxMarker marker, String message) {
        debug(message);
    }

    @Override
    public void debug(PaxMarker marker, String format, Object arg) {
        debug(format, arg);
    }

    @Override
    public void debug(PaxMarker marker, String format, Object arg1, Object arg2) {
        debug(format, arg1, arg2);
    }

    @Override
    public void debug(PaxMarker marker, String format, Object... arguments) {
        debug(format, arguments);
    }

    @Override
    public <E extends Exception> void debug(PaxMarker marker, LoggerConsumer<E> consumer) throws E {
        debug(consumer);
    }

    @Override
    public void info(String message) {
        if (isInfoEnabled()) {
            output(levels[PaxLogger.LEVEL_INFO], message, null);
        }
    }

    @Override
    public void info(String format, Object arg) {
        if (isInfoEnabled()) {
            FormattingTriple ft = FormattingTriple.resolve(format, false, arg);
            output(levels[PaxLogger.LEVEL_INFO], ft.getMessage(), ft.getThrowable());
        }
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        if (isInfoEnabled()) {
            FormattingTriple ft = FormattingTriple.resolve(format, false, arg1, arg2);
            output(levels[PaxLogger.LEVEL_INFO], ft.getMessage(), ft.getThrowable());
        }
    }

    @Override
    public void info(String format, Object... arguments) {
        if (isInfoEnabled()) {
            FormattingTriple ft = FormattingTriple.resolve(format, false, arguments);
            output(levels[PaxLogger.LEVEL_INFO], ft.getMessage(), ft.getThrowable());
        }
    }

    @Override
    public <E extends Exception> void info(LoggerConsumer<E> consumer) throws E {
        if (isInfoEnabled()) {
            consumer.accept(this);
        }
    }

    @Override
    public void info(PaxMarker marker, String message) {
        info(message);
    }

    @Override
    public void info(PaxMarker marker, String format, Object arg) {
        info(format, arg);
    }

    @Override
    public void info(PaxMarker marker, String format, Object arg1, Object arg2) {
        info(format, arg1, arg2);
    }

    @Override
    public void info(PaxMarker marker, String format, Object... arguments) {
        info(format, arguments);
    }

    @Override
    public <E extends Exception> void info(PaxMarker marker, LoggerConsumer<E> consumer) throws E {
        info(consumer);
    }

    @Override
    public void warn(String message) {
        if (isWarnEnabled()) {
            output(levels[PaxLogger.LEVEL_WARNING], message, null);
        }
    }

    @Override
    public void warn(String format, Object arg) {
        if (isWarnEnabled()) {
            FormattingTriple ft = FormattingTriple.resolve(format, false, arg);
            output(levels[PaxLogger.LEVEL_WARNING], ft.getMessage(), ft.getThrowable());
        }
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        if (isWarnEnabled()) {
            FormattingTriple ft = FormattingTriple.resolve(format, false, arg1, arg2);
            output(levels[PaxLogger.LEVEL_WARNING], ft.getMessage(), ft.getThrowable());
        }
    }

    @Override
    public void warn(String format, Object... arguments) {
        if (isWarnEnabled()) {
            FormattingTriple ft = FormattingTriple.resolve(format, false, arguments);
            output(levels[PaxLogger.LEVEL_WARNING], ft.getMessage(), ft.getThrowable());
        }
    }

    @Override
    public <E extends Exception> void warn(LoggerConsumer<E> consumer) throws E {
        if (isWarnEnabled()) {
            consumer.accept(this);
        }
    }

    @Override
    public void warn(PaxMarker marker, String message) {
        warn(message);
    }

    @Override
    public void warn(PaxMarker marker, String format, Object arg) {
        warn(format, arg);
    }

    @Override
    public void warn(PaxMarker marker, String format, Object arg1, Object arg2) {
        warn(format, arg1, arg2);
    }

    @Override
    public void warn(PaxMarker marker, String format, Object... arguments) {
        warn(format, arguments);
    }

    @Override
    public <E extends Exception> void warn(PaxMarker marker, LoggerConsumer<E> consumer) throws E {
        warn(consumer);
    }

    @Override
    public void error(String message) {
        if (isErrorEnabled()) {
            output(levels[PaxLogger.LEVEL_ERROR], message, null);
        }
    }

    @Override
    public void error(String format, Object arg) {
        if (isErrorEnabled()) {
            FormattingTriple ft = FormattingTriple.resolve(format, false, arg);
            output(levels[PaxLogger.LEVEL_ERROR], ft.getMessage(), ft.getThrowable());
        }
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        if (isErrorEnabled()) {
            FormattingTriple ft = FormattingTriple.resolve(format, false, arg1, arg2);
            output(levels[PaxLogger.LEVEL_ERROR], ft.getMessage(), ft.getThrowable());
        }
    }

    @Override
    public void error(String format, Object... arguments) {
        if (isErrorEnabled()) {
            FormattingTriple ft = FormattingTriple.resolve(format, false, arguments);
            output(levels[PaxLogger.LEVEL_ERROR], ft.getMessage(), ft.getThrowable());
        }
    }

    @Override
    public <E extends Exception> void error(LoggerConsumer<E> consumer) throws E {
        if (isErrorEnabled()) {
            consumer.accept(this);
        }
    }

    @Override
    public void error(PaxMarker marker, String message) {
        error(message);
    }

    @Override
    public void error(PaxMarker marker, String format, Object arg) {
        error(format, arg);
    }

    @Override
    public void error(PaxMarker marker, String format, Object arg1, Object arg2) {
        error(format, arg1, arg2);
    }

    @Override
    public void error(PaxMarker marker, String format, Object... arguments) {
        error(format, arguments);
    }

    @Override
    public <E extends Exception> void error(PaxMarker marker, LoggerConsumer<E> consumer) throws E {
        error(consumer);
    }

    @Override
    public void fatal(String message) {
        if (isFatalEnabled()) {
            output(levels[PaxLogger.LEVEL_FATAL], message, null);
        }
    }

    @Override
    public void fatal(String format, Object arg) {
        if (isFatalEnabled()) {
            FormattingTriple ft = FormattingTriple.resolve(format, false, arg);
            output(levels[PaxLogger.LEVEL_FATAL], ft.getMessage(), ft.getThrowable());
        }
    }

    @Override
    public void fatal(String format, Object arg1, Object arg2) {
        if (isFatalEnabled()) {
            FormattingTriple ft = FormattingTriple.resolve(format, false, arg1, arg2);
            output(levels[PaxLogger.LEVEL_FATAL], ft.getMessage(), ft.getThrowable());
        }
    }

    @Override
    public void fatal(String format, Object... arguments) {
        if (isFatalEnabled()) {
            FormattingTriple ft = FormattingTriple.resolve(format, false, arguments);
            output(levels[PaxLogger.LEVEL_FATAL], ft.getMessage(), ft.getThrowable());
        }
    }

    @Override
    public <E extends Exception> void fatal(LoggerConsumer<E> consumer) throws E {
        if (isFatalEnabled()) {
            consumer.accept(this);
        }
    }

    @Override
    public void fatal(PaxMarker marker, String message) {
        fatal(message);
    }

    @Override
    public void fatal(PaxMarker marker, String format, Object arg) {
        fatal(format, arg);
    }

    @Override
    public void fatal(PaxMarker marker, String format, Object arg1, Object arg2) {
        fatal(format, arg1, arg2);
    }

    @Override
    public void fatal(PaxMarker marker, String format, Object... arguments) {
        fatal(format, arguments);
    }

    @Override
    public <E extends Exception> void fatal(PaxMarker marker, LoggerConsumer<E> consumer) throws E {
        fatal(consumer);
    }

    @Override
    public void audit(String message) {
        output(levels[PaxLogger.LEVEL_AUDIT], message, null);
    }

    @Override
    public void audit(String format, Object arg) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arg);
        output(levels[PaxLogger.LEVEL_AUDIT], ft.getMessage(), ft.getThrowable());
    }

    @Override
    public void audit(String format, Object arg1, Object arg2) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arg1, arg2);
        output(levels[PaxLogger.LEVEL_AUDIT], ft.getMessage(), ft.getThrowable());
    }

    @Override
    public void audit(String format, Object... arguments) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arguments);
        output(levels[PaxLogger.LEVEL_AUDIT], ft.getMessage(), ft.getThrowable());
    }

    @Override
    public <E extends Exception> void audit(LoggerConsumer<E> consumer) throws E {
        consumer.accept(this);
    }

    @Override
    public void audit(PaxMarker marker, String message) {
        audit(message);
    }

    @Override
    public void audit(PaxMarker marker, String format, Object arg) {
        audit(format, arg);
    }

    @Override
    public void audit(PaxMarker marker, String format, Object arg1, Object arg2) {
        audit(format, arg1, arg2);
    }

    @Override
    public void audit(PaxMarker marker, String format, Object... arguments) {
        audit(format, arguments);
    }

    @Override
    public <E extends Exception> void audit(PaxMarker marker, LoggerConsumer<E> consumer) throws E {
        audit(consumer);
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
        return threshold;
    }

    @Override
    public LogLevel getLogLevel() {
        switch (threshold) {
            case PaxLogger.LEVEL_AUDIT:
                return LogLevel.AUDIT;
            case PaxLogger.LEVEL_TRACE:
                return LogLevel.TRACE;
            case PaxLogger.LEVEL_DEBUG:
                return LogLevel.DEBUG;
            case PaxLogger.LEVEL_INFO:
                return LogLevel.INFO;
            case PaxLogger.LEVEL_WARNING:
                return LogLevel.WARN;
            case PaxLogger.LEVEL_ERROR:
            case PaxLogger.LEVEL_FATAL:
            default:
                return LogLevel.ERROR;
        }
    }

    /**
     * <p>Sets the threshold for this default/fallback logger. Events with level lower than given threshold
     * won't be logged.</p>
     * <p>Karaf sets this threshold to {@code ERROR} (in {@code etc/system.properties}).</p>
     * @param level
     */
    public static void setLogLevel(String level) {
        DefaultServiceLog.threshold = convertLevel(level);
    }

    public String getName() {
        return m_categoryName;
    }

    /**
     * Outputs logging <em>event</em> with preconfigured layout.
     * @param levelName
     * @param message
     * @param t
     */
    protected void output(String levelName, String message, Throwable t) {
        synchronized (this) {
            output(System.out, levelName, message, t);
        }
    }

    protected final void output(PrintStream out, String levelName, String message, Throwable t) {
        // Might be [null] if used by standard test cases.
        if (m_bundle != null) {
            out.print(m_bundle.getSymbolicName());
            out.print(" ");
        }

        out.print("[");
        out.print(m_categoryName);
        out.print("] ");
        out.print(levelName);
        out.print(" ");
        out.print(": ");
        out.println(message);

        if (t != null) {
            t.printStackTrace(out);
        }
    }

    @Override
    public PaxContext getPaxContext() {
        return m_context;
    }

}
