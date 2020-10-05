/*
 * Copyright 2005 Niclas Hedhman.
 * Copyright 2011 Avid Technology, Inc.
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
package org.ops4j.pax.logging.logback.internal;

import java.security.AccessController;
import java.security.PrivilegedAction;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.ops4j.pax.logging.PaxContext;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingConstants;
import org.ops4j.pax.logging.PaxMarker;
import org.ops4j.pax.logging.spi.support.FormattingTriple;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogLevel;
import org.osgi.service.log.LoggerConsumer;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.spi.LocationAwareLogger;
import org.slf4j.spi.MDCAdapter;

/**
 * A logger implementation specialized for Logback.
 *
 * <p>
 * This code was originally derived from org.ops4j.pax.logging.service.internal.PaxLoggerImpl v1.6.0.
 * Changes include:
 * <ul>
 *     <li>tweaks for logback API instead of log4j API</li>
 *     <li>no longer needed special log(level, message, exception) call</li>
 *     <li>send events to a separate eventHandler instead of assuming the service is also the event handler</li>
 *     <li>generics</li>
 *     <li>Unification of logging backends in 1.11+</li>
 * </ul>
 *
 * @author Chris Dolan
 * @author Raul Kripalani
 */
public class PaxLoggerImpl implements PaxLogger {

    static String FQCN = PaxLoggerImpl.class.getName();

    // "the" delegate.
    private final Logger m_delegate;

    // FQCN to get location info
    private final String m_fqcn;
    // bundle associated with PaxLoggingService which is org.osgi.framework.ServiceFactory
    private final Bundle m_bundle;
    // actual PaxLoggingService
    private final PaxLoggingServiceImpl m_service;
    // if true, loggers use printf formatting on passed arguments
    private boolean m_printfFormatting;

    /**
     * @param bundle   The bundle that this PaxLogger belongs to.
     * @param delegate The logback delegate to receive the log message.
     * @param fqcn     The fully qualified class name of the client owning this logger.
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
        return m_delegate.isWarnEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return m_delegate.isErrorEnabled();
    }

    @Override
    public boolean isFatalEnabled() {
        return m_delegate.isErrorEnabled();
    }

    @Override
    public boolean isTraceEnabled(PaxMarker marker) {
        return m_delegate.isTraceEnabled(marker.slf4jMarker());
    }

    @Override
    public boolean isDebugEnabled(PaxMarker marker) {
        return m_delegate.isDebugEnabled(marker.slf4jMarker());
    }

    @Override
    public boolean isInfoEnabled(PaxMarker marker) {
        return m_delegate.isInfoEnabled(marker.slf4jMarker());
    }

    @Override
    public boolean isWarnEnabled(PaxMarker marker) {
        return m_delegate.isWarnEnabled(marker.slf4jMarker());
    }

    @Override
    public boolean isErrorEnabled(PaxMarker marker) {
        return m_delegate.isErrorEnabled(marker.slf4jMarker());
    }

    @Override
    public boolean isFatalEnabled(PaxMarker marker) {
        return m_delegate.isErrorEnabled(marker.slf4jMarker());
    }

    // R7: org.osgi.service.log.Logger

    @Override
    public void trace(String message) {
        if (isTraceEnabled()) {
            doLog(null, LocationAwareLogger.TRACE_INT, m_fqcn, message, null, null);
        }
    }

    @Override
    public void trace(String format, Object arg) {
        if (isTraceEnabled()) {
            if (m_printfFormatting) {
                // we have to do it ourselves
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg);
                doLog(null, LocationAwareLogger.TRACE_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                // Logback can do Slf4J formatting on its own, but we have to extract Throwable and/or ServiceReference
                FormattingTriple ft = FormattingTriple.discover(format, false, arg);
                doLog(null, LocationAwareLogger.TRACE_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        if (isTraceEnabled()) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg1, arg2);
                doLog(null, LocationAwareLogger.TRACE_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg1, arg2);
                doLog(null, LocationAwareLogger.TRACE_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void trace(String format, Object... arguments) {
        if (isTraceEnabled()) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arguments);
                doLog(null, LocationAwareLogger.TRACE_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arguments);
                doLog(null, LocationAwareLogger.TRACE_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
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
        if (isTraceEnabled(marker)) {
            doLog(marker.slf4jMarker(), LocationAwareLogger.TRACE_INT, m_fqcn, message, null, null);
        }
    }

    @Override
    public void trace(PaxMarker marker, String format, Object arg) {
        if (isTraceEnabled(marker)) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg);
                doLog(marker.slf4jMarker(), LocationAwareLogger.TRACE_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg);
                doLog(marker.slf4jMarker(), LocationAwareLogger.TRACE_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void trace(PaxMarker marker, String format, Object arg1, Object arg2) {
        if (isTraceEnabled(marker)) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg1, arg2);
                doLog(marker.slf4jMarker(), LocationAwareLogger.TRACE_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg1, arg2);
                doLog(marker.slf4jMarker(), LocationAwareLogger.TRACE_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void trace(PaxMarker marker, String format, Object... arguments) {
        if (isTraceEnabled(marker)) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arguments);
                doLog(marker.slf4jMarker(), LocationAwareLogger.TRACE_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arguments);
                doLog(marker.slf4jMarker(), LocationAwareLogger.TRACE_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public <E extends Exception> void trace(PaxMarker marker, LoggerConsumer<E> consumer) throws E {
        if (isTraceEnabled(marker)) {
            consumer.accept(this);
        }
    }

    @Override
    public void debug(String message) {
        if (isDebugEnabled()) {
            doLog(null, LocationAwareLogger.DEBUG_INT, m_fqcn, message, null, null);
        }
    }

    @Override
    public void debug(String format, Object arg) {
        if (isDebugEnabled()) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg);
                doLog(null, LocationAwareLogger.DEBUG_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg);
                doLog(null, LocationAwareLogger.DEBUG_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        if (isDebugEnabled()) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg1, arg2);
                doLog(null, LocationAwareLogger.DEBUG_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg1, arg2);
                doLog(null, LocationAwareLogger.DEBUG_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void debug(String format, Object... arguments) {
        if (isDebugEnabled()) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arguments);
                doLog(null, LocationAwareLogger.DEBUG_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arguments);
                doLog(null, LocationAwareLogger.DEBUG_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
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
        if (isDebugEnabled(marker)) {
            doLog(marker.slf4jMarker(), LocationAwareLogger.DEBUG_INT, m_fqcn, message, null, null);
        }
    }

    @Override
    public void debug(PaxMarker marker, String format, Object arg) {
        if (isDebugEnabled(marker)) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg);
                doLog(marker.slf4jMarker(), LocationAwareLogger.DEBUG_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg);
                doLog(marker.slf4jMarker(), LocationAwareLogger.DEBUG_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void debug(PaxMarker marker, String format, Object arg1, Object arg2) {
        if (isDebugEnabled(marker)) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg1, arg2);
                doLog(marker.slf4jMarker(), LocationAwareLogger.DEBUG_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg1, arg2);
                doLog(marker.slf4jMarker(), LocationAwareLogger.DEBUG_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void debug(PaxMarker marker, String format, Object... arguments) {
        if (isDebugEnabled(marker)) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arguments);
                doLog(marker.slf4jMarker(), LocationAwareLogger.DEBUG_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arguments);
                doLog(marker.slf4jMarker(), LocationAwareLogger.DEBUG_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public <E extends Exception> void debug(PaxMarker marker, LoggerConsumer<E> consumer) throws E {
        if (isDebugEnabled(marker)) {
            consumer.accept(this);
        }
    }

    @Override
    public void info(String message) {
        if (isInfoEnabled()) {
            doLog(null, LocationAwareLogger.INFO_INT, m_fqcn, message, null, null);
        }
    }

    @Override
    public void info(String format, Object arg) {
        if (isInfoEnabled()) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg);
                doLog(null, LocationAwareLogger.INFO_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg);
                doLog(null, LocationAwareLogger.INFO_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        if (isInfoEnabled()) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg1, arg2);
                doLog(null, LocationAwareLogger.INFO_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg1, arg2);
                doLog(null, LocationAwareLogger.INFO_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void info(String format, Object... arguments) {
        if (isInfoEnabled()) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arguments);
                doLog(null, LocationAwareLogger.INFO_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arguments);
                doLog(null, LocationAwareLogger.INFO_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
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
        if (isInfoEnabled(marker)) {
            doLog(marker.slf4jMarker(), LocationAwareLogger.INFO_INT, m_fqcn, message, null, null);
        }
    }

    @Override
    public void info(PaxMarker marker, String format, Object arg) {
        if (isInfoEnabled(marker)) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg);
                doLog(marker.slf4jMarker(), LocationAwareLogger.INFO_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg);
                doLog(marker.slf4jMarker(), LocationAwareLogger.INFO_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void info(PaxMarker marker, String format, Object arg1, Object arg2) {
        if (isInfoEnabled(marker)) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg1, arg2);
                doLog(marker.slf4jMarker(), LocationAwareLogger.INFO_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg1, arg2);
                doLog(marker.slf4jMarker(), LocationAwareLogger.INFO_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void info(PaxMarker marker, String format, Object... arguments) {
        if (isInfoEnabled(marker)) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arguments);
                doLog(marker.slf4jMarker(), LocationAwareLogger.INFO_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arguments);
                doLog(marker.slf4jMarker(), LocationAwareLogger.INFO_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public <E extends Exception> void info(PaxMarker marker, LoggerConsumer<E> consumer) throws E {
        if (isInfoEnabled(marker)) {
            consumer.accept(this);
        }
    }

    @Override
    public void warn(String message) {
        if (isWarnEnabled()) {
            doLog(null, LocationAwareLogger.WARN_INT, m_fqcn, message, null, null);
        }
    }

    @Override
    public void warn(String format, Object arg) {
        if (isWarnEnabled()) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg);
                doLog(null, LocationAwareLogger.WARN_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg);
                doLog(null, LocationAwareLogger.WARN_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        if (isWarnEnabled()) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg1, arg2);
                doLog(null, LocationAwareLogger.WARN_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg1, arg2);
                doLog(null, LocationAwareLogger.WARN_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void warn(String format, Object... arguments) {
        if (isWarnEnabled()) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arguments);
                doLog(null, LocationAwareLogger.WARN_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arguments);
                doLog(null, LocationAwareLogger.WARN_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
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
        if (isWarnEnabled(marker)) {
            doLog(marker.slf4jMarker(), LocationAwareLogger.WARN_INT, m_fqcn, message, null, null);
        }
    }

    @Override
    public void warn(PaxMarker marker, String format, Object arg) {
        if (isWarnEnabled(marker)) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg);
                doLog(marker.slf4jMarker(), LocationAwareLogger.WARN_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg);
                doLog(marker.slf4jMarker(), LocationAwareLogger.WARN_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void warn(PaxMarker marker, String format, Object arg1, Object arg2) {
        if (isWarnEnabled(marker)) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg1, arg2);
                doLog(marker.slf4jMarker(), LocationAwareLogger.WARN_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg1, arg2);
                doLog(marker.slf4jMarker(), LocationAwareLogger.WARN_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void warn(PaxMarker marker, String format, Object... arguments) {
        if (isWarnEnabled(marker)) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arguments);
                doLog(marker.slf4jMarker(), LocationAwareLogger.WARN_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arguments);
                doLog(marker.slf4jMarker(), LocationAwareLogger.WARN_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public <E extends Exception> void warn(PaxMarker marker, LoggerConsumer<E> consumer) throws E {
        if (isWarnEnabled(marker)) {
            consumer.accept(this);
        }
    }

    @Override
    public void error(String message) {
        if (isErrorEnabled()) {
            doLog(null, LocationAwareLogger.ERROR_INT, m_fqcn, message, null, null);
        }
    }

    @Override
    public void error(String format, Object arg) {
        if (isErrorEnabled()) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg);
                doLog(null, LocationAwareLogger.ERROR_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg);
                doLog(null, LocationAwareLogger.ERROR_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        if (isErrorEnabled()) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg1, arg2);
                doLog(null, LocationAwareLogger.ERROR_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg1, arg2);
                doLog(null, LocationAwareLogger.ERROR_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void error(String format, Object... arguments) {
        if (isErrorEnabled()) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arguments);
                doLog(null, LocationAwareLogger.ERROR_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arguments);
                doLog(null, LocationAwareLogger.ERROR_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
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
        if (isErrorEnabled(marker)) {
            doLog(marker.slf4jMarker(), LocationAwareLogger.ERROR_INT, m_fqcn, message, null, null);
        }
    }

    @Override
    public void error(PaxMarker marker, String format, Object arg) {
        if (isErrorEnabled(marker)) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg);
                doLog(marker.slf4jMarker(), LocationAwareLogger.ERROR_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg);
                doLog(marker.slf4jMarker(), LocationAwareLogger.ERROR_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void error(PaxMarker marker, String format, Object arg1, Object arg2) {
        if (isErrorEnabled(marker)) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg1, arg2);
                doLog(marker.slf4jMarker(), LocationAwareLogger.ERROR_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg1, arg2);
                doLog(marker.slf4jMarker(), LocationAwareLogger.ERROR_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void error(PaxMarker marker, String format, Object... arguments) {
        if (isErrorEnabled(marker)) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arguments);
                doLog(marker.slf4jMarker(), LocationAwareLogger.ERROR_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arguments);
                doLog(marker.slf4jMarker(), LocationAwareLogger.ERROR_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public <E extends Exception> void error(PaxMarker marker, LoggerConsumer<E> consumer) throws E {
        if (isErrorEnabled(marker)) {
            consumer.accept(this);
        }
    }

    @Override
    public void fatal(String message) {
        if (isFatalEnabled()) {
            doLog(null, LocationAwareLogger.ERROR_INT, m_fqcn, message, null, null);
        }
    }

    @Override
    public void fatal(String format, Object arg) {
        if (isFatalEnabled()) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg);
                doLog(null, LocationAwareLogger.ERROR_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg);
                doLog(null, LocationAwareLogger.ERROR_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void fatal(String format, Object arg1, Object arg2) {
        if (isFatalEnabled()) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg1, arg2);
                doLog(null, LocationAwareLogger.ERROR_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg1, arg2);
                doLog(null, LocationAwareLogger.ERROR_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void fatal(String format, Object... arguments) {
        if (isFatalEnabled()) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arguments);
                doLog(null, LocationAwareLogger.ERROR_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arguments);
                doLog(null, LocationAwareLogger.ERROR_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
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
        if (isFatalEnabled(marker)) {
            doLog(marker.slf4jMarker(), LocationAwareLogger.ERROR_INT, m_fqcn, message, null, null);
        }
    }

    @Override
    public void fatal(PaxMarker marker, String format, Object arg) {
        if (isFatalEnabled(marker)) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg);
                doLog(marker.slf4jMarker(), LocationAwareLogger.ERROR_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg);
                doLog(marker.slf4jMarker(), LocationAwareLogger.ERROR_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void fatal(PaxMarker marker, String format, Object arg1, Object arg2) {
        if (isFatalEnabled(marker)) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg1, arg2);
                doLog(marker.slf4jMarker(), LocationAwareLogger.ERROR_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg1, arg2);
                doLog(marker.slf4jMarker(), LocationAwareLogger.ERROR_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void fatal(PaxMarker marker, String format, Object... arguments) {
        if (isFatalEnabled(marker)) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arguments);
                doLog(marker.slf4jMarker(), LocationAwareLogger.ERROR_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arguments);
                doLog(marker.slf4jMarker(), LocationAwareLogger.ERROR_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public <E extends Exception> void fatal(PaxMarker marker, LoggerConsumer<E> consumer) throws E {
        if (isFatalEnabled(marker)) {
            consumer.accept(this);
        }
    }

    @Override
    public void audit(String message) {
        doLog(null, LocationAwareLogger.ERROR_INT, m_fqcn, message, null, null);
    }

    @Override
    public void audit(String format, Object arg) {
        if (m_printfFormatting) {
            FormattingTriple ft = FormattingTriple.resolve(format, true, arg);
            doLog(null, LocationAwareLogger.ERROR_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
        } else {
            FormattingTriple ft = FormattingTriple.discover(format, false, arg);
            doLog(null, LocationAwareLogger.ERROR_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
        }
    }

    @Override
    public void audit(String format, Object arg1, Object arg2) {
        if (m_printfFormatting) {
            FormattingTriple ft = FormattingTriple.resolve(format, true, arg1, arg2);
            doLog(null, LocationAwareLogger.ERROR_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
        } else {
            FormattingTriple ft = FormattingTriple.discover(format, false, arg1, arg2);
            doLog(null, LocationAwareLogger.ERROR_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
        }
    }

    @Override
    public void audit(String format, Object... arguments) {
        if (m_printfFormatting) {
            FormattingTriple ft = FormattingTriple.resolve(format, true, arguments);
            doLog(null, LocationAwareLogger.ERROR_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
        } else {
            FormattingTriple ft = FormattingTriple.discover(format, false, arguments);
            doLog(null, LocationAwareLogger.ERROR_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
        }
    }

    @Override
    public <E extends Exception> void audit(LoggerConsumer<E> consumer) throws E {
        consumer.accept(this);
    }

    @Override
    public void audit(PaxMarker marker, String message) {
        doLog(marker.slf4jMarker(), LocationAwareLogger.ERROR_INT, m_fqcn, message, null, null);
    }

    @Override
    public void audit(PaxMarker marker, String format, Object arg) {
        if (m_printfFormatting) {
            FormattingTriple ft = FormattingTriple.resolve(format, true, arg);
            doLog(marker.slf4jMarker(), LocationAwareLogger.ERROR_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
        } else {
            FormattingTriple ft = FormattingTriple.discover(format, false, arg);
            doLog(marker.slf4jMarker(), LocationAwareLogger.ERROR_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
        }
    }

    @Override
    public void audit(PaxMarker marker, String format, Object arg1, Object arg2) {
        if (m_printfFormatting) {
            FormattingTriple ft = FormattingTriple.resolve(format, true, arg1, arg2);
            doLog(marker.slf4jMarker(), LocationAwareLogger.ERROR_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
        } else {
            FormattingTriple ft = FormattingTriple.discover(format, false, arg1, arg2);
            doLog(marker.slf4jMarker(), LocationAwareLogger.ERROR_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
        }
    }

    @Override
    public void audit(PaxMarker marker, String format, Object... arguments) {
        if (m_printfFormatting) {
            FormattingTriple ft = FormattingTriple.resolve(format, true, arguments);
            doLog(marker.slf4jMarker(), LocationAwareLogger.ERROR_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
        } else {
            FormattingTriple ft = FormattingTriple.discover(format, false, arguments);
            doLog(marker.slf4jMarker(), LocationAwareLogger.ERROR_INT, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
        }
    }

    @Override
    public <E extends Exception> void audit(PaxMarker marker, LoggerConsumer<E> consumer) throws E {
        consumer.accept(this);
    }

    @Override
    public void fqtrace(String fqcn, String message) {
        if (isTraceEnabled()) {
            doLog(null, LocationAwareLogger.TRACE_INT, fqcn, message, null, null);
        }
    }

    @Override
    public void fqdebug(String fqcn, String message) {
        if (isDebugEnabled()) {
            doLog(null, LocationAwareLogger.DEBUG_INT, fqcn, message, null, null);
        }
    }

    @Override
    public void fqinfo(String fqcn, String message) {
        if (isInfoEnabled()) {
            doLog(null, LocationAwareLogger.INFO_INT, fqcn, message, null, null);
        }
    }

    @Override
    public void fqwarn(String fqcn, String message) {
        if (isWarnEnabled()) {
            doLog(null, LocationAwareLogger.WARN_INT, fqcn, message, null, null);
        }
    }

    @Override
    public void fqerror(String fqcn, String message) {
        if (isErrorEnabled()) {
            doLog(null, LocationAwareLogger.ERROR_INT, fqcn, message, null, null);
        }
    }

    @Override
    public void fqfatal(String fqcn, String message) {
        if (isFatalEnabled()) {
            doLog(null, LocationAwareLogger.ERROR_INT, fqcn, message, null, null);
        }
    }

    @Override
    public void fqtrace(String fqcn, PaxMarker marker, String message) {
        if (isTraceEnabled(marker)) {
            doLog(marker.slf4jMarker(), LocationAwareLogger.TRACE_INT, fqcn, message, null, null);
        }
    }

    @Override
    public void fqdebug(String fqcn, PaxMarker marker, String message) {
        if (isDebugEnabled(marker)) {
            doLog(marker.slf4jMarker(), LocationAwareLogger.DEBUG_INT, fqcn, message, null, null);
        }
    }

    @Override
    public void fqinfo(String fqcn, PaxMarker marker, String message) {
        if (isInfoEnabled(marker)) {
            doLog(marker.slf4jMarker(), LocationAwareLogger.INFO_INT, fqcn, message, null, null);
        }
    }

    @Override
    public void fqwarn(String fqcn, PaxMarker marker, String message) {
        if (isWarnEnabled(marker)) {
            doLog(marker.slf4jMarker(), LocationAwareLogger.WARN_INT, fqcn, message, null, null);
        }
    }

    @Override
    public void fqerror(String fqcn, PaxMarker marker, String message) {
        if (isErrorEnabled(marker)) {
            doLog(marker.slf4jMarker(), LocationAwareLogger.ERROR_INT, fqcn, message, null, null);
        }
    }

    @Override
    public void fqfatal(String fqcn, PaxMarker marker, String message) {
        if (isFatalEnabled(marker)) {
            doLog(marker.slf4jMarker(), LocationAwareLogger.ERROR_INT, fqcn, message, null, null);
        }
    }

    @Override
    public void fqtrace(String fqcn, String message, Throwable t) {
        if (isTraceEnabled()) {
            doLog(null, LocationAwareLogger.TRACE_INT, fqcn, message, t, null);
        }
    }

    @Override
    public void fqdebug(String fqcn, String message, Throwable t) {
        if (isDebugEnabled()) {
            doLog(null, LocationAwareLogger.DEBUG_INT, fqcn, message, t, null);
        }
    }

    @Override
    public void fqinfo(String fqcn, String message, Throwable t) {
        if (isInfoEnabled()) {
            doLog(null, LocationAwareLogger.INFO_INT, fqcn, message, t, null);
        }
    }

    @Override
    public void fqwarn(String fqcn, String message, Throwable t) {
        if (isWarnEnabled()) {
            doLog(null, LocationAwareLogger.WARN_INT, fqcn, message, t, null);
        }
    }

    @Override
    public void fqerror(String fqcn, String message, Throwable t) {
        if (isErrorEnabled()) {
            doLog(null, LocationAwareLogger.ERROR_INT, fqcn, message, t, null);
        }
    }

    @Override
    public void fqfatal(String fqcn, String message, Throwable t) {
        if (isFatalEnabled()) {
            doLog(null, LocationAwareLogger.ERROR_INT, fqcn, message, t, null);
        }
    }

    @Override
    public void fqtrace(String fqcn, PaxMarker marker, String message, Throwable t) {
        if (isTraceEnabled(marker)) {
            doLog(marker.slf4jMarker(), LocationAwareLogger.TRACE_INT, fqcn, message, t, null);
        }
    }

    @Override
    public void fqdebug(String fqcn, PaxMarker marker, String message, Throwable t) {
        if (isDebugEnabled(marker)) {
            doLog(marker.slf4jMarker(), LocationAwareLogger.DEBUG_INT, fqcn, message, t, null);
        }
    }

    @Override
    public void fqinfo(String fqcn, PaxMarker marker, String message, Throwable t) {
        if (isInfoEnabled(marker)) {
            doLog(marker.slf4jMarker(), LocationAwareLogger.INFO_INT, fqcn, message, t, null);
        }
    }

    @Override
    public void fqwarn(String fqcn, PaxMarker marker, String message, Throwable t) {
        if (isWarnEnabled(marker)) {
            doLog(marker.slf4jMarker(), LocationAwareLogger.WARN_INT, fqcn, message, t, null);
        }
    }

    @Override
    public void fqerror(String fqcn, PaxMarker marker, String message, Throwable t) {
        if (isErrorEnabled(marker)) {
            doLog(marker.slf4jMarker(), LocationAwareLogger.ERROR_INT, fqcn, message, t, null);
        }
    }

    @Override
    public void fqfatal(String fqcn, PaxMarker marker, String message, Throwable t) {
        if (isFatalEnabled(marker)) {
            doLog(marker.slf4jMarker(), LocationAwareLogger.ERROR_INT, fqcn, message, t, null);
        }
    }

    @Override
    public int getPaxLogLevel() {
        switch (m_delegate.getEffectiveLevel().toInt()) {
            case Level.ALL_INT:
                return LEVEL_AUDIT;
            case Level.TRACE_INT:
                return LEVEL_TRACE;
            case Level.DEBUG_INT:
                return LEVEL_DEBUG;
            case Level.INFO_INT:
                return LEVEL_INFO;
            case Level.WARN_INT:
                return LEVEL_WARNING;
            case Level.ERROR_INT:
                return LEVEL_ERROR;
            case Level.OFF_INT:
            default:
                return LEVEL_NONE;
        }
    }

    @Override
    public LogLevel getLogLevel() {
        Level level = m_delegate.getEffectiveLevel();
        if (level == null || level == Level.ALL) {
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

    private LogLevel getLogLevel(int levelFromLocationAwareLogger) {
        switch (levelFromLocationAwareLogger) {
            case LocationAwareLogger.TRACE_INT:
                return LogLevel.TRACE;
            case LocationAwareLogger.DEBUG_INT:
                return LogLevel.DEBUG;
            case LocationAwareLogger.INFO_INT:
                return LogLevel.INFO;
            case LocationAwareLogger.WARN_INT:
                return LogLevel.WARN;
            case LocationAwareLogger.ERROR_INT:
                return LogLevel.ERROR;
            default:
                return LogLevel.AUDIT;
        }
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

    private void doLog(final Marker marker, final int level, final String fqcn, final String message,
                       final Throwable t, final ServiceReference<?> ref,
                       final Object... args) {
        if (System.getSecurityManager() != null) {
            AccessController.doPrivileged(
                    (PrivilegedAction<Void>) () -> {
                        doLog0(marker, level, fqcn, message, t, ref, args);
                        return null;
                    }
            );
        } else {
            doLog0(marker, level, fqcn, message, t, ref, args);
        }
    }

    /**
     * Most important pax-logging-logback log method that bridges pax-logging-api directly into Logback. EAch
     * log invocation is wrapped with MDC configuration, where the following keys are always available:
     * <ul>
     *     <li>{@code bundle.id} - from {@link Bundle#getBundleId()}</li>
     *     <li>{@code bundle.name} - from {@link Bundle#getSymbolicName()}</li>
     *     <li>{@code bundle.version} - from {@link Bundle#getVersion()}</li>
     * </ul>
     *
     * @param marker
     * @param level int from {@link Level}
     * @param fqcn
     * @param message
     * @param t
     * @param ref
     * @param args
     */
    private void doLog0(Marker marker, final int level, final String fqcn, final String message,
                        final Throwable t, final ServiceReference<?> ref,
                        final Object... args) {
        setDelegateContext();
        try {
            m_delegate.log(marker, fqcn, level, message, args, t);
        } finally {
            clearDelegateContext();
        }
        LogLevel l = getLogLevel(level);
        m_service.handleEvents(getName(), m_bundle, ref, l, message, t);
    }

    private void setDelegateContext() {
        // Logback's MDCConverter pulls in MDC properties through the slf4j's MDC class already. 
        // Therefore there's no need to bridge two MDC implementations, like in the log4j PaxLoggerImpl.
        // See PAXLOGGING-165.
        MDCAdapter adapter = MDC.getMDCAdapter();
        if (m_bundle != null && adapter != null) {
            adapter.put("bundle.id", String.valueOf(m_bundle.getBundleId()));
            adapter.put("bundle.name", m_bundle.getSymbolicName());
            adapter.put("bundle.version", m_bundle.getVersion().toString());
        }
        if (adapter != null) {
            // remove this potential value to not pollute MDC
            adapter.remove(PaxLoggingConstants._LOG4J2_MESSAGE);
        }
        m_service.lock(false);
    }

    private void clearDelegateContext() {
        m_service.unlock(false);
        MDCAdapter adapter = MDC.getMDCAdapter();
        if (m_bundle != null && adapter != null) {
            adapter.remove("bundle.id");
            adapter.remove("bundle.name");
            adapter.remove("bundle.version");
        }

        // No need to clear the underlying MDC
        // See PAXLOGGING-165.
    }

}
