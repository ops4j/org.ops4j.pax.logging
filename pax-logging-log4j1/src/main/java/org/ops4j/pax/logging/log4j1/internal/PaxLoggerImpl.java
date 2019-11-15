/*
 * Copyright 2005 Niclas Hedhman.
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
package org.ops4j.pax.logging.log4j1.internal;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;

import org.apache.log4j.AuditLevel;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.ops4j.pax.logging.PaxContext;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingConstants;
import org.ops4j.pax.logging.PaxMarker;
import org.ops4j.pax.logging.spi.support.FormattingTriple;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogLevel;
import org.osgi.service.log.LoggerConsumer;

/**
 * Log4J1 specific {@link PaxLogger} delegating directly to Log4J1's {@link Logger}.
 */
public class PaxLoggerImpl implements PaxLogger {

    static String FQCN = PaxLoggerImpl.class.getName();

    // "the" delegate. org.apache.log4j.Logger should be loaded from pax-logging-log4j1 bundle and
    // not from pax-logging-api bundle
    private org.apache.log4j.Logger m_delegate;

    // FQCN for Log4J to get location info
    private String m_fqcn;
    // bundle associated with PaxLoggingService which is org.osgi.framework.ServiceFactory
    private Bundle m_bundle;
    // actual PaxLoggingService
    private PaxLoggingServiceImpl m_service;
    // if true, loggers use printf formatting on passed arguments
    private boolean m_printfFormatting;

    /**
     * @param bundle   The bundle that this PaxLogger belongs to.
     * @param delegate The Log4J delegate to receive the log message.
     * @param fqcn     The fully qualified classname of the client owning this logger.
     * @param service  The service to be used to handle the logging events.
     * @param printfFormatting Whether to use printf or Slf4J formatting
     */
    PaxLoggerImpl(Bundle bundle, Logger delegate, String fqcn, PaxLoggingServiceImpl service, boolean printfFormatting) {
        m_delegate = delegate;
        m_fqcn = fqcn;
        m_bundle = bundle;
        m_service = service;
        m_printfFormatting = printfFormatting;
    }

    // isXXXEnabled() from org.osgi.service.log.Logger and org.ops4j.pax.logging.PaxLogger

    @Override
    public boolean isTraceEnabled() {
        return m_delegate.isTraceEnabled();
    }

    @Override
    public boolean isDebugEnabled() {
        return m_delegate.isDebugEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return m_delegate.isInfoEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return m_delegate.isEnabledFor(Level.WARN);
    }

    @Override
    public boolean isErrorEnabled() {
        return m_delegate.isEnabledFor(Level.ERROR);
    }

    @Override
    public boolean isFatalEnabled() {
        return m_delegate.isEnabledFor(Level.FATAL);
    }

    @Override
    public boolean isTraceEnabled(PaxMarker marker) {
        return m_delegate.isTraceEnabled();
    }

    @Override
    public boolean isDebugEnabled(PaxMarker marker) {
        return m_delegate.isDebugEnabled();
    }

    @Override
    public boolean isInfoEnabled(PaxMarker marker) {
        return m_delegate.isInfoEnabled();
    }

    @Override
    public boolean isWarnEnabled(PaxMarker marker) {
        return m_delegate.isEnabledFor(Level.WARN);
    }

    @Override
    public boolean isErrorEnabled(PaxMarker marker) {
        return m_delegate.isEnabledFor(Level.ERROR);
    }

    @Override
    public boolean isFatalEnabled(PaxMarker marker) {
        return m_delegate.isEnabledFor(Level.FATAL);
    }

    // R7: org.osgi.service.log.Logger
    // that's a bit tricky, as Throwable and/or ServiceReference may be hidden withing varargs

    @Override
    public void trace(String message) {
        if (isTraceEnabled()) {
            doLog(Level.TRACE, m_fqcn, message, null, null);
        }
    }

    @Override
    public void trace(String format, Object arg) {
        if (isTraceEnabled()) {
            FormattingTriple ft = FormattingTriple.resolve(format, m_printfFormatting, arg);
            doLog(Level.TRACE, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
        }
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        if (isTraceEnabled()) {
            FormattingTriple ft = FormattingTriple.resolve(format, m_printfFormatting, arg1, arg2);
            doLog(Level.TRACE, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
        }
    }

    @Override
    public void trace(String format, Object... arguments) {
        if (isTraceEnabled()) {
            FormattingTriple ft = FormattingTriple.resolve(format, m_printfFormatting, arguments);
            doLog(Level.TRACE, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
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
            doLog(Level.DEBUG, m_fqcn, message, null, null);
        }
    }

    @Override
    public void debug(String format, Object arg) {
        if (isDebugEnabled()) {
            FormattingTriple ft = FormattingTriple.resolve(format, m_printfFormatting, arg);
            doLog(Level.DEBUG, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
        }
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        if (isDebugEnabled()) {
            FormattingTriple ft = FormattingTriple.resolve(format, m_printfFormatting, arg1, arg2);
            doLog(Level.DEBUG, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
        }
    }

    @Override
    public void debug(String format, Object... arguments) {
        if (isDebugEnabled()) {
            FormattingTriple ft = FormattingTriple.resolve(format, m_printfFormatting, arguments);
            doLog(Level.DEBUG, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
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
            doLog(Level.INFO, m_fqcn, message, null, null);
        }
    }

    @Override
    public void info(String format, Object arg) {
        if (isInfoEnabled()) {
            FormattingTriple ft = FormattingTriple.resolve(format, m_printfFormatting, arg);
            doLog(Level.INFO, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
        }
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        if (isInfoEnabled()) {
            FormattingTriple ft = FormattingTriple.resolve(format, m_printfFormatting, arg1, arg2);
            doLog(Level.INFO, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
        }
    }

    @Override
    public void info(String format, Object... arguments) {
        if (isInfoEnabled()) {
            FormattingTriple ft = FormattingTriple.resolve(format, m_printfFormatting, arguments);
            doLog(Level.INFO, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
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
            doLog(Level.WARN, m_fqcn, message, null, null);
        }
    }

    @Override
    public void warn(String format, Object arg) {
        if (isWarnEnabled()) {
            FormattingTriple ft = FormattingTriple.resolve(format, m_printfFormatting, arg);
            doLog(Level.WARN, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
        }
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        if (isWarnEnabled()) {
            FormattingTriple ft = FormattingTriple.resolve(format, m_printfFormatting, arg1, arg2);
            doLog(Level.WARN, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
        }
    }

    @Override
    public void warn(String format, Object... arguments) {
        if (isWarnEnabled()) {
            FormattingTriple ft = FormattingTriple.resolve(format, m_printfFormatting, arguments);
            doLog(Level.WARN, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
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
            doLog(Level.ERROR, m_fqcn, message, null, null);
        }
    }

    @Override
    public void error(String format, Object arg) {
        if (isErrorEnabled()) {
            FormattingTriple ft = FormattingTriple.resolve(format, m_printfFormatting, arg);
            doLog(Level.ERROR, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
        }
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        if (isErrorEnabled()) {
            FormattingTriple ft = FormattingTriple.resolve(format, m_printfFormatting, arg1, arg2);
            doLog(Level.ERROR, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
        }
    }

    @Override
    public void error(String format, Object... arguments) {
        if (isErrorEnabled()) {
            FormattingTriple ft = FormattingTriple.resolve(format, m_printfFormatting, arguments);
            doLog(Level.ERROR, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
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
            doLog(Level.FATAL, m_fqcn, message, null, null);
        }
    }

    @Override
    public void fatal(String format, Object arg) {
        if (isFatalEnabled()) {
            FormattingTriple ft = FormattingTriple.resolve(format, m_printfFormatting, arg);
            doLog(Level.FATAL, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
        }
    }

    @Override
    public void fatal(String format, Object arg1, Object arg2) {
        if (isFatalEnabled()) {
            FormattingTriple ft = FormattingTriple.resolve(format, m_printfFormatting, arg1, arg2);
            doLog(Level.FATAL, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
        }
    }

    @Override
    public void fatal(String format, Object... arguments) {
        if (isFatalEnabled()) {
            FormattingTriple ft = FormattingTriple.resolve(format, m_printfFormatting, arguments);
            doLog(Level.FATAL, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
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
        doLog(AuditLevel.AUDIT, m_fqcn, message, null, null);
    }

    @Override
    public void audit(String format, Object arg) {
        FormattingTriple ft = FormattingTriple.resolve(format, m_printfFormatting, arg);
        doLog(AuditLevel.AUDIT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
    }

    @Override
    public void audit(String format, Object arg1, Object arg2) {
        FormattingTriple ft = FormattingTriple.resolve(format, m_printfFormatting, arg1, arg2);
        doLog(AuditLevel.AUDIT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
    }

    @Override
    public void audit(String format, Object... arguments) {
        FormattingTriple ft = FormattingTriple.resolve(format, m_printfFormatting, arguments);
        doLog(AuditLevel.AUDIT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
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
        if (isTraceEnabled()) {
            doLog(Level.TRACE, fqcn, message, null, null);
        }
    }

    @Override
    public void fqdebug(String fqcn, String message) {
        if (isDebugEnabled()) {
            doLog(Level.DEBUG, fqcn, message, null, null);
        }
    }

    @Override
    public void fqinfo(String fqcn, String message) {
        if (isInfoEnabled()) {
            doLog(Level.INFO, fqcn, message, null, null);
        }
    }

    @Override
    public void fqwarn(String fqcn, String message) {
        if (isWarnEnabled()) {
            doLog(Level.WARN, fqcn, message, null, null);
        }
    }

    @Override
    public void fqerror(String fqcn, String message) {
        if (isErrorEnabled()) {
            doLog(Level.ERROR, fqcn, message, null, null);
        }
    }

    @Override
    public void fqfatal(String fqcn, String message) {
        if (isFatalEnabled()) {
            doLog(Level.FATAL, fqcn, message, null, null);
        }
    }

    @Override
    public void fqtrace(String fqcn, PaxMarker marker, String message) {
        fqtrace(fqcn, message);
    }

    @Override
    public void fqdebug(String fqcn, PaxMarker marker, String message) {
        fqdebug(fqcn, message);
    }

    @Override
    public void fqinfo(String fqcn, PaxMarker marker, String message) {
        fqinfo(fqcn, message);
    }

    @Override
    public void fqwarn(String fqcn, PaxMarker marker, String message) {
        fqwarn(fqcn, message);
    }

    @Override
    public void fqerror(String fqcn, PaxMarker marker, String message) {
        fqerror(fqcn, message);
    }

    @Override
    public void fqfatal(String fqcn, PaxMarker marker, String message) {
        fqfatal(fqcn, message);
    }

    @Override
    public void fqtrace(String fqcn, String message, Throwable t) {
        if (isTraceEnabled()) {
            doLog(Level.TRACE, fqcn, message, t, null);
        }
    }

    @Override
    public void fqdebug(String fqcn, String message, Throwable t) {
        if (isDebugEnabled()) {
            doLog(Level.DEBUG, fqcn, message, t, null);
        }
    }

    @Override
    public void fqinfo(String fqcn, String message, Throwable t) {
        if (isInfoEnabled()) {
            doLog(Level.INFO, fqcn, message, t, null);
        }
    }

    @Override
    public void fqwarn(String fqcn, String message, Throwable t) {
        if (isWarnEnabled()) {
            doLog(Level.WARN, fqcn, message, t, null);
        }
    }

    @Override
    public void fqerror(String fqcn, String message, Throwable t) {
        if (isErrorEnabled()) {
            doLog(Level.ERROR, fqcn, message, t, null);
        }
    }

    @Override
    public void fqfatal(String fqcn, String message, Throwable t) {
        if (isFatalEnabled()) {
            doLog(Level.FATAL, fqcn, message, t, null);
        }
    }

    @Override
    public void fqtrace(String fqcn, PaxMarker marker, String message, Throwable t) {
        fqtrace(fqcn, message, t);
    }

    @Override
    public void fqdebug(String fqcn, PaxMarker marker, String message, Throwable t) {
        fqdebug(fqcn, message, t);
    }

    @Override
    public void fqinfo(String fqcn, PaxMarker marker, String message, Throwable t) {
        fqinfo(fqcn, message, t);
    }

    @Override
    public void fqwarn(String fqcn, PaxMarker marker, String message, Throwable t) {
        fqwarn(fqcn, message, t);
    }

    @Override
    public void fqerror(String fqcn, PaxMarker marker, String message, Throwable t) {
        fqerror(fqcn, message, t);
    }

    @Override
    public void fqfatal(String fqcn, PaxMarker marker, String message, Throwable t) {
        fqfatal(fqcn, message, t);
    }

    @Override
    public int getPaxLogLevel() {
        // org.apache.log4j.Priority.isGreaterOrEqual checks if "this" level's numerical value
        // is >= than passed value - TRACE < DEBUG and DEBUG >= TRACE
        // because Log4J1 has higher num value for more severe level (opposite to Syslog and OSGi)
        Level level = m_delegate.getEffectiveLevel();

        if (level == null) {
            return LEVEL_NONE;
        }

        if (Level.TRACE.isGreaterOrEqual(level)) {
            return LEVEL_TRACE;
        }

        if (Level.DEBUG.isGreaterOrEqual(level)) {
            return LEVEL_DEBUG;
        }

        if (Level.INFO.isGreaterOrEqual(level)) {
            return LEVEL_INFO;
        }

        if (Level.WARN.isGreaterOrEqual(level)) {
            return LEVEL_WARNING;
        }

        if (Level.ERROR.isGreaterOrEqual(level)) {
            return LEVEL_ERROR;
        }

        if (Level.FATAL.isGreaterOrEqual(level)) {
            return LEVEL_FATAL;
        }

        return LEVEL_AUDIT;
    }

    @Override
    public LogLevel getLogLevel() {
        Level level = m_delegate.getEffectiveLevel();
        return getLogLevel(level);
    }

    private LogLevel getLogLevel(Level level) {
        // org.apache.log4j.Priority.isGreaterOrEqual checks if "this" level's numerical value
        // is >= than passed value - TRACE < DEBUG and DEBUG >= TRACE
        // because Log4J1 has higher num value for more severe level (opposite to Syslog and OSGi)
        if (level == null) {
            return LogLevel.AUDIT;
        }

        if (Level.TRACE.isGreaterOrEqual(level)) {
            return LogLevel.TRACE;
        }

        if (Level.DEBUG.isGreaterOrEqual(level)) {
            return LogLevel.DEBUG;
        }

        if (Level.INFO.isGreaterOrEqual(level)) {
            return LogLevel.INFO;
        }

        if (Level.WARN.isGreaterOrEqual(level)) {
            return LogLevel.WARN;
        }

        return LogLevel.ERROR;
    }

    @Override
    public String getName() {
        return m_delegate.getName();
    }

    @Override
    public PaxContext getPaxContext() {
        return m_service.getPaxContext();
    }

    // private methods

    private void doLog(final Level level, final String fqcn, final String message, final Throwable t, final ServiceReference<?> ref) {
        if (System.getSecurityManager() != null) {
            AccessController.doPrivileged(
                    (PrivilegedAction<Void>) () -> {
                        doLog0(level, fqcn, message, t, ref);
                        return null;
                    }
            );
        } else {
            doLog0(level, fqcn, message, t, ref);
        }
    }

    /**
     * <p>Most important pax-logging-log4j1 log method that bridges pax-logging-api directly into Log4J1. Each
     * log invocation is wrapped with MDC configuration, where the following keys are always available:<ul>
     *     <li>{@code bundle.id} - from {@link Bundle#getBundleId()}</li>
     *     <li>{@code bundle.name} - from {@link Bundle#getSymbolicName()}</li>
     *     <li>{@code bundle.version} - from {@link Bundle#getVersion()}</li>
     * </ul></p>
     * <p>This method should be called only within {@code isXXXEnabled()} check, as it calls some heavy operations.</p>
     *
     * @param level
     * @param fqcn
     * @param message already formatted message coming from various R7 log() methods accepting arguments
     * @param t
     * @param ref a reference associated only with {@link org.osgi.service.log.LogEntry}, not needed by Log4J1
     */
    private void doLog0(final Level level, final String fqcn, final String message, final Throwable t, final ServiceReference<?> ref) {
        setDelegateContext();
        try {
            m_delegate.log(fqcn, level, message, t);
        } finally {
            clearDelegateContext();
        }
        LogLevel l = getLogLevel(level);
        m_service.handleEvents(getName(), m_bundle, ref, l, message, t);
    }

    private void setDelegateContext() {
        Map<String, Object> context = getPaxContext().getContext();
        if (context != null) {
            for (Object o : context.keySet()) {
                String key = (String) o;
                Object value = context.get(key);
                MDC.put(key, value);
            }
            // remove this potential value to not pollute MDC
            context.remove(PaxLoggingConstants._LOG4J2_MESSAGE);
        }
        if (m_bundle != null) {
            put("bundle.id", String.valueOf(m_bundle.getBundleId()));
            put("bundle.name", m_bundle.getSymbolicName());
            put("bundle.version", m_bundle.getVersion().toString());
        }
        m_service.lock(false);
    }

    private void put(String name, Object o) {
        if (o != null) {
            MDC.put(name, o);
        }
    }

    private void clearDelegateContext() {
        m_service.unlock(false);
        if (MDC.getContext() != null) {
            MDC.getContext().clear();
        }
    }
}
