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
package org.ops4j.pax.logging.spi.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ops4j.pax.logging.PaxContext;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxMarker;
import org.osgi.framework.Bundle;
import org.osgi.service.log.LogLevel;
import org.osgi.service.log.LoggerConsumer;

/**
 * Experimental fallback strategy for non-availability.
 */
public class BufferingLog implements PaxLogger {

    private final List<LogPackage> m_queue;
    private final String m_fqcn;
    private PaxContext m_context = new PaxContext();
    public BufferingLog(Bundle bundle, String categoryName) {
        m_fqcn = getClass().getName();
        m_queue = new ArrayList<LogPackage>();
    }

    void flush(PaxLogger destination) {
        for (LogPackage pack : m_queue) {
            String fqcn = pack.getFqcn();
            Throwable throwable = pack.getException();
            String message = pack.getMessage();
            getPaxContext().putAll(pack.getContext());
            LogType logType = pack.getType();
            int logTypeAsInt = logType.getType();
            switch (logTypeAsInt) {
                case LogType.DEBUG_INT:
                    destination.debug(message, throwable);
                    break;
                case LogType.TRACE_INT:
                    destination.trace(message, throwable);
                    break;
                case LogType.INFO_INT:
                    destination.info(message, throwable);
                    break;
                case LogType.WARN_INT:
                    destination.warn(message, throwable);
                    break;
                case LogType.ERROR_INT:
                    destination.error(message, throwable);
                    break;
                case LogType.FATAL_INT:
                    destination.fatal(message, throwable);
                    break;
            }
            getPaxContext().clear();
        }
    }

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
    public boolean isInfoEnabled(PaxMarker marker) {
        return true;
    }

    @Override
    public boolean isWarnEnabled(PaxMarker marker) {
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
        LogPackage p = new LogPackage(m_fqcn, LogType.trace, message, null, getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void trace(String format, Object arg) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arg);
        LogPackage p = new LogPackage(m_fqcn, LogType.trace, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arg1, arg2);
        LogPackage p = new LogPackage(m_fqcn, LogType.trace, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void trace(String format, Object... arguments) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arguments);
        LogPackage p = new LogPackage(m_fqcn, LogType.trace, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public <E extends Exception> void trace(LoggerConsumer<E> consumer) throws E {
        consumer.accept(this);
    }

    @Override
    public void trace(PaxMarker marker, String message) {
        LogPackage p = new LogPackage(m_fqcn, LogType.trace, message, null, getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void trace(PaxMarker marker, String format, Object arg) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arg);
        LogPackage p = new LogPackage(m_fqcn, LogType.trace, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void trace(PaxMarker marker, String format, Object arg1, Object arg2) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arg1, arg2);
        LogPackage p = new LogPackage(m_fqcn, LogType.trace, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void trace(PaxMarker marker, String format, Object... arguments) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arguments);
        LogPackage p = new LogPackage(m_fqcn, LogType.trace, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public <E extends Exception> void trace(PaxMarker marker, LoggerConsumer<E> consumer) throws E {
        consumer.accept(this);
    }

    @Override
    public void debug(String message) {
        LogPackage p = new LogPackage(m_fqcn, LogType.debug, message, null, getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void debug(String format, Object arg) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arg);
        LogPackage p = new LogPackage(m_fqcn, LogType.debug, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arg1, arg2);
        LogPackage p = new LogPackage(m_fqcn, LogType.debug, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void debug(String format, Object... arguments) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arguments);
        LogPackage p = new LogPackage(m_fqcn, LogType.debug, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public <E extends Exception> void debug(LoggerConsumer<E> consumer) throws E {
        consumer.accept(this);
    }

    @Override
    public void debug(PaxMarker marker, String message) {
        LogPackage p = new LogPackage(m_fqcn, LogType.debug, message, null, getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void debug(PaxMarker marker, String format, Object arg) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arg);
        LogPackage p = new LogPackage(m_fqcn, LogType.debug, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void debug(PaxMarker marker, String format, Object arg1, Object arg2) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arg1, arg2);
        LogPackage p = new LogPackage(m_fqcn, LogType.debug, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void debug(PaxMarker marker, String format, Object... arguments) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arguments);
        LogPackage p = new LogPackage(m_fqcn, LogType.debug, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public <E extends Exception> void debug(PaxMarker marker, LoggerConsumer<E> consumer) throws E {
        consumer.accept(this);
    }
    
    @Override
    public void info(String message) {
        LogPackage p = new LogPackage(m_fqcn, LogType.info, message, null, getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void info(String format, Object arg) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arg);
        LogPackage p = new LogPackage(m_fqcn, LogType.info, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arg1, arg2);
        LogPackage p = new LogPackage(m_fqcn, LogType.info, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void info(String format, Object... arguments) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arguments);
        LogPackage p = new LogPackage(m_fqcn, LogType.info, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public <E extends Exception> void info(LoggerConsumer<E> consumer) throws E {
        consumer.accept(this);
    }

    @Override
    public void info(PaxMarker marker, String message) {
        LogPackage p = new LogPackage(m_fqcn, LogType.info, message, null, getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void info(PaxMarker marker, String format, Object arg) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arg);
        LogPackage p = new LogPackage(m_fqcn, LogType.info, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void info(PaxMarker marker, String format, Object arg1, Object arg2) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arg1, arg2);
        LogPackage p = new LogPackage(m_fqcn, LogType.info, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void info(PaxMarker marker, String format, Object... arguments) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arguments);
        LogPackage p = new LogPackage(m_fqcn, LogType.info, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public <E extends Exception> void info(PaxMarker marker, LoggerConsumer<E> consumer) throws E {
        consumer.accept(this);
    }
    
    @Override
    public void warn(String message) {
        LogPackage p = new LogPackage(m_fqcn, LogType.warn, message, null, getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void warn(String format, Object arg) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arg);
        LogPackage p = new LogPackage(m_fqcn, LogType.warn, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arg1, arg2);
        LogPackage p = new LogPackage(m_fqcn, LogType.warn, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void warn(String format, Object... arguments) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arguments);
        LogPackage p = new LogPackage(m_fqcn, LogType.warn, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public <E extends Exception> void warn(LoggerConsumer<E> consumer) throws E {
        consumer.accept(this);
    }

    @Override
    public void warn(PaxMarker marker, String message) {
        LogPackage p = new LogPackage(m_fqcn, LogType.warn, message, null, getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void warn(PaxMarker marker, String format, Object arg) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arg);
        LogPackage p = new LogPackage(m_fqcn, LogType.warn, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void warn(PaxMarker marker, String format, Object arg1, Object arg2) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arg1, arg2);
        LogPackage p = new LogPackage(m_fqcn, LogType.warn, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void warn(PaxMarker marker, String format, Object... arguments) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arguments);
        LogPackage p = new LogPackage(m_fqcn, LogType.warn, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public <E extends Exception> void warn(PaxMarker marker, LoggerConsumer<E> consumer) throws E {
        consumer.accept(this);
    }
    
    @Override
    public void error(String message) {
        LogPackage p = new LogPackage(m_fqcn, LogType.error, message, null, getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void error(String format, Object arg) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arg);
        LogPackage p = new LogPackage(m_fqcn, LogType.error, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arg1, arg2);
        LogPackage p = new LogPackage(m_fqcn, LogType.error, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void error(String format, Object... arguments) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arguments);
        LogPackage p = new LogPackage(m_fqcn, LogType.error, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public <E extends Exception> void error(LoggerConsumer<E> consumer) throws E {
        consumer.accept(this);
    }

    @Override
    public void error(PaxMarker marker, String message) {
        LogPackage p = new LogPackage(m_fqcn, LogType.error, message, null, getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void error(PaxMarker marker, String format, Object arg) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arg);
        LogPackage p = new LogPackage(m_fqcn, LogType.error, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void error(PaxMarker marker, String format, Object arg1, Object arg2) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arg1, arg2);
        LogPackage p = new LogPackage(m_fqcn, LogType.error, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void error(PaxMarker marker, String format, Object... arguments) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arguments);
        LogPackage p = new LogPackage(m_fqcn, LogType.error, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public <E extends Exception> void error(PaxMarker marker, LoggerConsumer<E> consumer) throws E {
        consumer.accept(this);
    }
    
    @Override
    public void fatal(String message) {
        LogPackage p = new LogPackage(m_fqcn, LogType.fatal, message, null, getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void fatal(String format, Object arg) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arg);
        LogPackage p = new LogPackage(m_fqcn, LogType.fatal, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void fatal(String format, Object arg1, Object arg2) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arg1, arg2);
        LogPackage p = new LogPackage(m_fqcn, LogType.fatal, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void fatal(String format, Object... arguments) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arguments);
        LogPackage p = new LogPackage(m_fqcn, LogType.fatal, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public <E extends Exception> void fatal(LoggerConsumer<E> consumer) throws E {
        consumer.accept(this);
    }

    @Override
    public void fatal(PaxMarker marker, String message) {
        LogPackage p = new LogPackage(m_fqcn, LogType.fatal, message, null, getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void fatal(PaxMarker marker, String format, Object arg) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arg);
        LogPackage p = new LogPackage(m_fqcn, LogType.fatal, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void fatal(PaxMarker marker, String format, Object arg1, Object arg2) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arg1, arg2);
        LogPackage p = new LogPackage(m_fqcn, LogType.fatal, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void fatal(PaxMarker marker, String format, Object... arguments) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arguments);
        LogPackage p = new LogPackage(m_fqcn, LogType.fatal, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public <E extends Exception> void fatal(PaxMarker marker, LoggerConsumer<E> consumer) throws E {
        consumer.accept(this);
    }
    
    @Override
    public void audit(String message) {
        LogPackage p = new LogPackage(m_fqcn, LogType.audit, message, null, getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void audit(String format, Object arg) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arg);
        LogPackage p = new LogPackage(m_fqcn, LogType.audit, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void audit(String format, Object arg1, Object arg2) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arg1, arg2);
        LogPackage p = new LogPackage(m_fqcn, LogType.audit, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void audit(String format, Object... arguments) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arguments);
        LogPackage p = new LogPackage(m_fqcn, LogType.audit, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public <E extends Exception> void audit(LoggerConsumer<E> consumer) throws E {
        consumer.accept(this);
    }

    @Override
    public void audit(PaxMarker marker, String message) {
        LogPackage p = new LogPackage(m_fqcn, LogType.audit, message, null, getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void audit(PaxMarker marker, String format, Object arg) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arg);
        LogPackage p = new LogPackage(m_fqcn, LogType.audit, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void audit(PaxMarker marker, String format, Object arg1, Object arg2) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arg1, arg2);
        LogPackage p = new LogPackage(m_fqcn, LogType.audit, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void audit(PaxMarker marker, String format, Object... arguments) {
        FormattingTriple ft = FormattingTriple.resolve(format, false, arguments);
        LogPackage p = new LogPackage(m_fqcn, LogType.audit, ft.getMessage(), ft.getThrowable(), getPaxContext().getContext());
        m_queue.add(p);
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
        trace(message, marker, t);
    }

    @Override
    public void fqdebug(String fqcn, PaxMarker marker, String message, Throwable t) {
        debug(message, marker, t);
    }

    @Override
    public void fqinfo(String fqcn, PaxMarker marker, String message, Throwable t) {
        info(message, marker, t);
    }

    @Override
    public void fqwarn(String fqcn, PaxMarker marker, String message, Throwable t) {
        warn(message, marker, t);
    }

    @Override
    public void fqerror(String fqcn, PaxMarker marker, String message, Throwable t) {
        error(message, marker, t);
    }

    @Override
    public void fqfatal(String fqcn, PaxMarker marker, String message, Throwable t) {
        fatal(message, marker, t);
    }

    @Override
    public int getPaxLogLevel() {
        return LogType.TRACE_INT;
    }

    @Override
    public LogLevel getLogLevel() {
        return LogLevel.TRACE;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public PaxContext getPaxContext() {
        return m_context;
    }

    private static class LogType {

        private static final int TRACE_INT = 0;
        private static final int DEBUG_INT = 1;
        private static final int INFO_INT = 2;
        private static final int WARN_INT = 3;
        private static final int ERROR_INT = 4;
        private static final int FATAL_INT = 5;
        private static final int AUDIT_INT = 6;

        private static LogType trace = new LogType(TRACE_INT);
        private static LogType debug = new LogType(DEBUG_INT);
        private static LogType info = new LogType(INFO_INT);
        private static LogType warn = new LogType(WARN_INT);
        private static LogType error = new LogType(ERROR_INT);
        private static LogType fatal = new LogType(FATAL_INT);
        private static LogType audit = new LogType(AUDIT_INT);

        private final int m_type;

        private LogType(int type) {
            m_type = type;
        }

        private int getType() {
            return m_type;
        }
    }

    private static class LogPackage {

        private final String m_fqcn;
        private final LogType m_type;
        private final String m_message;
        private final Throwable m_exception;
        private final Map<String, Object> m_context;

        public LogPackage(String fqcn, LogType type, String message, Throwable exception, Map<String, Object> context) {
            m_fqcn = fqcn;
            m_type = type;
            m_message = message;
            m_exception = exception;
            m_context = context;
            if (m_exception != null)
                m_exception.fillInStackTrace();
        }

        public String getFqcn() {
            return m_fqcn;
        }

        public String getMessage() {
            return m_message;
        }

        public Throwable getException() {
            return m_exception;
        }

        public LogType getType() {
            return m_type;
        }

        public Map<String, Object> getContext() {
            return m_context;
        }
    }

}
