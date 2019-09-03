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
package org.ops4j.pax.logging.log4j2.internal;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.apache.logging.log4j.spi.StandardLevel;
import org.ops4j.pax.logging.PaxContext;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxMarker;
import org.ops4j.pax.logging.spi.support.FormattingTriple;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogLevel;
import org.osgi.service.log.LoggerConsumer;

/**
 * Log4J2 specific {@link PaxLogger} delegating directly to Log4J2's {@link ExtendedLogger}.
 */
public class PaxLoggerImpl implements PaxLogger {

    static String FQCN = PaxLoggerImpl.class.getName();

    // "the" delegate. org.apache.logging.log4j.spi.ExtendedLogger
    private ExtendedLogger m_delegate;

    // FQCN for Log42 to get location info
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
    PaxLoggerImpl(Bundle bundle, ExtendedLogger delegate, String fqcn, PaxLoggingServiceImpl service, boolean printfFormatting) {
        m_delegate = delegate;
        m_fqcn = fqcn;
        m_bundle = bundle;
        m_service = service;
        m_printfFormatting = printfFormatting;
    }

    public void setDelegate(ExtendedLogger m_delegate) {
        this.m_delegate = m_delegate;
    }

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
        return m_delegate.isFatalEnabled();
    }

    @Override
    public boolean isTraceEnabled(PaxMarker marker) {
        return m_delegate.isTraceEnabled(marker.log4j2Marker());
    }

    @Override
    public boolean isDebugEnabled(PaxMarker marker) {
        return m_delegate.isDebugEnabled(marker.log4j2Marker());
    }

    @Override
    public boolean isInfoEnabled(PaxMarker marker) {
        return m_delegate.isInfoEnabled(marker.log4j2Marker());
    }

    @Override
    public boolean isWarnEnabled(PaxMarker marker) {
        return m_delegate.isWarnEnabled(marker.log4j2Marker());
    }

    @Override
    public boolean isErrorEnabled(PaxMarker marker) {
        return m_delegate.isErrorEnabled(marker.log4j2Marker());
    }

    @Override
    public boolean isFatalEnabled(PaxMarker marker) {
        return m_delegate.isFatalEnabled(marker.log4j2Marker());
    }

    // R7: org.osgi.service.log.Logger

    @Override
    public void trace(String message) {
        if (isTraceEnabled()) {
            doLog(null, Level.TRACE, m_fqcn, message, null, null);
        }
    }

    @Override
    public void trace(String format, Object arg) {
        if (isTraceEnabled()) {
            if (m_printfFormatting) {
                // we have to do it ourselves
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg);
                doLog(null, Level.TRACE, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                // Log4J2 can do Slf4J formatting on its own, but we have to extract Throwable and/or ServiceReference
                FormattingTriple ft = FormattingTriple.discover(format, false, arg);
                doLog(null, Level.TRACE, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        if (isTraceEnabled()) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg1, arg2);
                doLog(null, Level.TRACE, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg1, arg2);
                doLog(null, Level.TRACE, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void trace(String format, Object... arguments) {
        if (isTraceEnabled()) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arguments);
                doLog(null, Level.TRACE, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arguments);
                doLog(null, Level.TRACE, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
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
            doLog(marker.log4j2Marker(), Level.TRACE, m_fqcn, message, null, null);
        }
    }

    @Override
    public void trace(PaxMarker marker, String format, Object arg) {
        if (isTraceEnabled(marker)) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg);
                doLog(marker.log4j2Marker(), Level.TRACE, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg);
                doLog(marker.log4j2Marker(), Level.TRACE, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void trace(PaxMarker marker, String format, Object arg1, Object arg2) {
        if (isTraceEnabled(marker)) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg1, arg2);
                doLog(marker.log4j2Marker(), Level.TRACE, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg1, arg2);
                doLog(marker.log4j2Marker(), Level.TRACE, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void trace(PaxMarker marker, String format, Object... arguments) {
        if (isTraceEnabled(marker)) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arguments);
                doLog(marker.log4j2Marker(), Level.TRACE, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arguments);
                doLog(marker.log4j2Marker(), Level.TRACE, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
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
            doLog(null, Level.DEBUG, m_fqcn, message, null, null);
        }
    }

    @Override
    public void debug(String format, Object arg) {
        if (isDebugEnabled()) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg);
                doLog(null, Level.DEBUG, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg);
                doLog(null, Level.DEBUG, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        if (isDebugEnabled()) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg1, arg2);
                doLog(null, Level.DEBUG, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg1, arg2);
                doLog(null, Level.DEBUG, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void debug(String format, Object... arguments) {
        if (isDebugEnabled()) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arguments);
                doLog(null, Level.DEBUG, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arguments);
                doLog(null, Level.DEBUG, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
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
            doLog(marker.log4j2Marker(), Level.DEBUG, m_fqcn, message, null, null);
        }
    }

    @Override
    public void debug(PaxMarker marker, String format, Object arg) {
        if (isDebugEnabled(marker)) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg);
                doLog(marker.log4j2Marker(), Level.DEBUG, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg);
                doLog(marker.log4j2Marker(), Level.DEBUG, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void debug(PaxMarker marker, String format, Object arg1, Object arg2) {
        if (isDebugEnabled(marker)) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg1, arg2);
                doLog(marker.log4j2Marker(), Level.DEBUG, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg1, arg2);
                doLog(marker.log4j2Marker(), Level.DEBUG, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void debug(PaxMarker marker, String format, Object... arguments) {
        if (isDebugEnabled(marker)) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arguments);
                doLog(marker.log4j2Marker(), Level.DEBUG, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arguments);
                doLog(marker.log4j2Marker(), Level.DEBUG, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
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
            doLog(null, Level.INFO, m_fqcn, message, null, null);
        }
    }

    @Override
    public void info(String format, Object arg) {
        if (isInfoEnabled()) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg);
                doLog(null, Level.INFO, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg);
                doLog(null, Level.INFO, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        if (isInfoEnabled()) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg1, arg2);
                doLog(null, Level.INFO, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg1, arg2);
                doLog(null, Level.INFO, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void info(String format, Object... arguments) {
        if (isInfoEnabled()) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arguments);
                doLog(null, Level.INFO, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arguments);
                doLog(null, Level.INFO, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
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
            doLog(marker.log4j2Marker(), Level.INFO, m_fqcn, message, null, null);
        }
    }

    @Override
    public void info(PaxMarker marker, String format, Object arg) {
        if (isInfoEnabled(marker)) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg);
                doLog(marker.log4j2Marker(), Level.INFO, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg);
                doLog(marker.log4j2Marker(), Level.INFO, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void info(PaxMarker marker, String format, Object arg1, Object arg2) {
        if (isInfoEnabled(marker)) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg1, arg2);
                doLog(marker.log4j2Marker(), Level.INFO, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg1, arg2);
                doLog(marker.log4j2Marker(), Level.INFO, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void info(PaxMarker marker, String format, Object... arguments) {
        if (isInfoEnabled(marker)) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arguments);
                doLog(marker.log4j2Marker(), Level.INFO, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arguments);
                doLog(marker.log4j2Marker(), Level.INFO, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
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
            doLog(null, Level.WARN, m_fqcn, message, null, null);
        }
    }

    @Override
    public void warn(String format, Object arg) {
        if (isWarnEnabled()) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg);
                doLog(null, Level.WARN, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg);
                doLog(null, Level.WARN, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        if (isWarnEnabled()) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg1, arg2);
                doLog(null, Level.WARN, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg1, arg2);
                doLog(null, Level.WARN, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void warn(String format, Object... arguments) {
        if (isWarnEnabled()) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arguments);
                doLog(null, Level.WARN, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arguments);
                doLog(null, Level.WARN, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
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
            doLog(marker.log4j2Marker(), Level.WARN, m_fqcn, message, null, null);
        }
    }

    @Override
    public void warn(PaxMarker marker, String format, Object arg) {
        if (isWarnEnabled(marker)) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg);
                doLog(marker.log4j2Marker(), Level.WARN, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg);
                doLog(marker.log4j2Marker(), Level.WARN, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void warn(PaxMarker marker, String format, Object arg1, Object arg2) {
        if (isWarnEnabled(marker)) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg1, arg2);
                doLog(marker.log4j2Marker(), Level.WARN, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg1, arg2);
                doLog(marker.log4j2Marker(), Level.WARN, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void warn(PaxMarker marker, String format, Object... arguments) {
        if (isWarnEnabled(marker)) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arguments);
                doLog(marker.log4j2Marker(), Level.WARN, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arguments);
                doLog(marker.log4j2Marker(), Level.WARN, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
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
            doLog(null, Level.ERROR, m_fqcn, message, null, null);
        }
    }

    @Override
    public void error(String format, Object arg) {
        if (isErrorEnabled()) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg);
                doLog(null, Level.ERROR, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg);
                doLog(null, Level.ERROR, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        if (isErrorEnabled()) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg1, arg2);
                doLog(null, Level.ERROR, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg1, arg2);
                doLog(null, Level.ERROR, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void error(String format, Object... arguments) {
        if (isErrorEnabled()) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arguments);
                doLog(null, Level.ERROR, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arguments);
                doLog(null, Level.ERROR, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
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
            doLog(marker.log4j2Marker(), Level.ERROR, m_fqcn, message, null, null);
        }
    }

    @Override
    public void error(PaxMarker marker, String format, Object arg) {
        if (isErrorEnabled(marker)) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg);
                doLog(marker.log4j2Marker(), Level.ERROR, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg);
                doLog(marker.log4j2Marker(), Level.ERROR, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void error(PaxMarker marker, String format, Object arg1, Object arg2) {
        if (isErrorEnabled(marker)) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg1, arg2);
                doLog(marker.log4j2Marker(), Level.ERROR, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg1, arg2);
                doLog(marker.log4j2Marker(), Level.ERROR, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void error(PaxMarker marker, String format, Object... arguments) {
        if (isErrorEnabled(marker)) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arguments);
                doLog(marker.log4j2Marker(), Level.ERROR, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arguments);
                doLog(marker.log4j2Marker(), Level.ERROR, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
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
            doLog(null, Level.FATAL, m_fqcn, message, null, null);
        }
    }

    @Override
    public void fatal(String format, Object arg) {
        if (isFatalEnabled()) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg);
                doLog(null, Level.FATAL, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg);
                doLog(null, Level.FATAL, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void fatal(String format, Object arg1, Object arg2) {
        if (isFatalEnabled()) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg1, arg2);
                doLog(null, Level.FATAL, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg1, arg2);
                doLog(null, Level.FATAL, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void fatal(String format, Object... arguments) {
        if (isFatalEnabled()) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arguments);
                doLog(null, Level.FATAL, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arguments);
                doLog(null, Level.FATAL, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
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
            doLog(marker.log4j2Marker(), Level.FATAL, m_fqcn, message, null, null);
        }
    }

    @Override
    public void fatal(PaxMarker marker, String format, Object arg) {
        if (isFatalEnabled(marker)) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg);
                doLog(marker.log4j2Marker(), Level.FATAL, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg);
                doLog(marker.log4j2Marker(), Level.FATAL, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void fatal(PaxMarker marker, String format, Object arg1, Object arg2) {
        if (isFatalEnabled(marker)) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arg1, arg2);
                doLog(marker.log4j2Marker(), Level.FATAL, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arg1, arg2);
                doLog(marker.log4j2Marker(), Level.FATAL, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void fatal(PaxMarker marker, String format, Object... arguments) {
        if (isFatalEnabled(marker)) {
            if (m_printfFormatting) {
                FormattingTriple ft = FormattingTriple.resolve(format, true, arguments);
                doLog(marker.log4j2Marker(), Level.FATAL, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
            } else {
                FormattingTriple ft = FormattingTriple.discover(format, false, arguments);
                doLog(marker.log4j2Marker(), Level.FATAL, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
            }
        }
    }

    @Override
    public void audit(String message) {
        doLog(null, Level.ALL, m_fqcn, message, null, null);
    }

    @Override
    public void audit(String format, Object arg) {
        if (m_printfFormatting) {
            FormattingTriple ft = FormattingTriple.resolve(format, true, arg);
            doLog(null, Level.ALL, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
        } else {
            FormattingTriple ft = FormattingTriple.discover(format, false, arg);
            doLog(null, Level.ALL, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
        }
    }

    @Override
    public void audit(String format, Object arg1, Object arg2) {
        if (m_printfFormatting) {
            FormattingTriple ft = FormattingTriple.resolve(format, true, arg1, arg2);
            doLog(null, Level.ALL, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
        } else {
            FormattingTriple ft = FormattingTriple.discover(format, false, arg1, arg2);
            doLog(null, Level.ALL, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
        }
    }

    @Override
    public void audit(String format, Object... arguments) {
        if (m_printfFormatting) {
            FormattingTriple ft = FormattingTriple.resolve(format, true, arguments);
            doLog(null, Level.ALL, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
        } else {
            FormattingTriple ft = FormattingTriple.discover(format, false, arguments);
            doLog(null, Level.ALL, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
        }
    }

    @Override
    public <E extends Exception> void audit(LoggerConsumer<E> consumer) throws E {
        consumer.accept(this);
    }

    @Override
    public void audit(PaxMarker marker, String message) {
        doLog(marker.log4j2Marker(), Level.ALL, m_fqcn, message, null, null);
    }

    @Override
    public void audit(PaxMarker marker, String format, Object arg) {
        if (m_printfFormatting) {
            FormattingTriple ft = FormattingTriple.resolve(format, true, arg);
            doLog(marker.log4j2Marker(), Level.ALL, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
        } else {
            FormattingTriple ft = FormattingTriple.discover(format, false, arg);
            doLog(marker.log4j2Marker(), Level.ALL, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
        }
    }

    @Override
    public void audit(PaxMarker marker, String format, Object arg1, Object arg2) {
        if (m_printfFormatting) {
            FormattingTriple ft = FormattingTriple.resolve(format, true, arg1, arg2);
            doLog(marker.log4j2Marker(), Level.ALL, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
        } else {
            FormattingTriple ft = FormattingTriple.discover(format, false, arg1, arg2);
            doLog(marker.log4j2Marker(), Level.ALL, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
        }
    }

    @Override
    public void audit(PaxMarker marker, String format, Object... arguments) {
        if (m_printfFormatting) {
            FormattingTriple ft = FormattingTriple.resolve(format, true, arguments);
            doLog(marker.log4j2Marker(), Level.ALL, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference());
        } else {
            FormattingTriple ft = FormattingTriple.discover(format, false, arguments);
            doLog(marker.log4j2Marker(), Level.ALL, m_fqcn, ft.getMessage(), ft.getThrowable(), ft.getServiceReference(), ft.getArgArray());
        }
    }

    @Override
    public <E extends Exception> void audit(PaxMarker marker, LoggerConsumer<E> consumer) throws E {
        consumer.accept(this);
    }

    @Override
    public <E extends Exception> void fatal(PaxMarker marker, LoggerConsumer<E> consumer) throws E {
        if (isFatalEnabled()) {
            consumer.accept(this);
        }
    }

    @Override
    public void fqtrace(String fqcn, String message) {
        if (isTraceEnabled()) {
            doLog(null, Level.TRACE, fqcn, message, null, null);
        }
    }

    @Override
    public void fqdebug(String fqcn, String message) {
        if (isDebugEnabled()) {
            doLog(null, Level.DEBUG, fqcn, message, null, null);
        }
    }

    @Override
    public void fqinfo(String fqcn, String message) {
        if (isInfoEnabled()) {
            doLog(null, Level.INFO, fqcn, message, null, null);
        }
    }

    @Override
    public void fqwarn(String fqcn, String message) {
        if (isWarnEnabled()) {
            doLog(null, Level.WARN, fqcn, message, null, null);
        }
    }

    @Override
    public void fqerror(String fqcn, String message) {
        if (isErrorEnabled()) {
            doLog(null, Level.ERROR, fqcn, message, null, null);
        }
    }

    @Override
    public void fqfatal(String fqcn, String message) {
        if (isFatalEnabled()) {
            doLog(null, Level.FATAL, fqcn, message, null, null);
        }
    }

    @Override
    public void fqtrace(String fqcn, PaxMarker marker, String message) {
        if (isTraceEnabled(marker)) {
            doLog(marker.log4j2Marker(), Level.TRACE, fqcn, message, null, null);
        }
    }

    @Override
    public void fqdebug(String fqcn, PaxMarker marker, String message) {
        if (isDebugEnabled(marker)) {
            doLog(marker.log4j2Marker(), Level.DEBUG, fqcn, message, null, null);
        }
    }

    @Override
    public void fqinfo(String fqcn, PaxMarker marker, String message) {
        if (isInfoEnabled(marker)) {
            doLog(marker.log4j2Marker(), Level.INFO, fqcn, message, null, null);
        }
    }

    @Override
    public void fqwarn(String fqcn, PaxMarker marker, String message) {
        if (isWarnEnabled(marker)) {
            doLog(marker.log4j2Marker(), Level.WARN, fqcn, message, null, null);
        }
    }

    @Override
    public void fqerror(String fqcn, PaxMarker marker, String message) {
        if (isErrorEnabled(marker)) {
            doLog(marker.log4j2Marker(), Level.ERROR, fqcn, message, null, null);
        }
    }

    @Override
    public void fqfatal(String fqcn, PaxMarker marker, String message) {
        if (isFatalEnabled(marker)) {
            doLog(marker.log4j2Marker(), Level.FATAL, fqcn, message, null, null);
        }
    }

    @Override
    public void fqtrace(String fqcn, String message, Throwable t) {
        if (isTraceEnabled()) {
            doLog(null, Level.TRACE, fqcn, message, t, null);
        }
    }

    @Override
    public void fqdebug(String fqcn, String message, Throwable t) {
        if (isDebugEnabled()) {
            doLog(null, Level.DEBUG, fqcn, message, t, null);
        }
    }

    @Override
    public void fqinfo(String fqcn, String message, Throwable t) {
        if (isInfoEnabled()) {
            doLog(null, Level.INFO, fqcn, message, t, null);
        }
    }

    @Override
    public void fqwarn(String fqcn, String message, Throwable t) {
        if (isWarnEnabled()) {
            doLog(null, Level.WARN, fqcn, message, t, null);
        }
    }

    @Override
    public void fqerror(String fqcn, String message, Throwable t) {
        if (isErrorEnabled()) {
            doLog(null, Level.ERROR, fqcn, message, t, null);
        }
    }

    @Override
    public void fqfatal(String fqcn, String message, Throwable t) {
        if (isFatalEnabled()) {
            doLog(null, Level.FATAL, fqcn, message, t, null);
        }
    }

    @Override
    public void fqtrace(String fqcn, PaxMarker marker, String message, Throwable t) {
        if (isTraceEnabled(marker)) {
            doLog(marker.log4j2Marker(), Level.TRACE, fqcn, message, t, null);
        }
    }

    @Override
    public void fqdebug(String fqcn, PaxMarker marker, String message, Throwable t) {
        if (isDebugEnabled(marker)) {
            doLog(marker.log4j2Marker(), Level.DEBUG, fqcn, message, t, null);
        }
    }

    @Override
    public void fqinfo(String fqcn, PaxMarker marker, String message, Throwable t) {
        if (isInfoEnabled(marker)) {
            doLog(marker.log4j2Marker(), Level.INFO, fqcn, message, t, null);
        }
    }

    @Override
    public void fqwarn(String fqcn, PaxMarker marker, String message, Throwable t) {
        if (isWarnEnabled(marker)) {
            doLog(marker.log4j2Marker(), Level.WARN, fqcn, message, t, null);
        }
    }

    @Override
    public void fqerror(String fqcn, PaxMarker marker, String message, Throwable t) {
        if (isErrorEnabled(marker)) {
            doLog(marker.log4j2Marker(), Level.ERROR, fqcn, message, t, null);
        }
    }

    @Override
    public void fqfatal(String fqcn, PaxMarker marker, String message, Throwable t) {
        if (isFatalEnabled(marker)) {
            doLog(marker.log4j2Marker(), Level.FATAL, fqcn, message, t, null);
        }
    }

    @Override
    public int getPaxLogLevel() {
        switch (m_delegate.getLevel().getStandardLevel()) {
            case TRACE:
                return LEVEL_TRACE;
            case DEBUG:
                return LEVEL_DEBUG;
            case INFO:
                return LEVEL_INFO;
            case WARN:
                return LEVEL_WARNING;
            default:
                return LEVEL_ERROR;
        }
    }

    @Override
    public LogLevel getLogLevel() {
        return getLogLevel(m_delegate.getLevel().getStandardLevel());
    }

    private LogLevel getLogLevel(StandardLevel level) {
        switch (level) {
            case OFF:
                return null;
            case ALL:
                return LogLevel.AUDIT;
            case TRACE:
                return LogLevel.TRACE;
            case DEBUG:
                return LogLevel.DEBUG;
            case INFO:
                return LogLevel.INFO;
            case WARN:
                return LogLevel.WARN;
            case FATAL:
            case ERROR:
            default:
                return LogLevel.ERROR;
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

    private void doLog(final Marker marker, final Level level, final String fqcn, final String message,
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
     * <p>Most important pax-logging-log4j2 log method that bridges pax-logging-api directly into Log4J2.</p>
     * <p>This method should be called only within {@code isXXXEnabled()} check, as it calls some heavy operations.</p>
     * @param marker
     * @param level
     * @param fqcn
     * @param message
     * @param t
     * @param ref
     * @param args
     */
    private void doLog0(Marker marker, Level level, String fqcn, String message,
                        Throwable t, final ServiceReference<?> ref,
                        Object... args) {
        setDelegateContext();
        try {
            Message msg = m_delegate.getMessageFactory().newMessage(message, args);
            m_delegate.logMessage(fqcn, level, marker, msg, t);
        } finally {
            clearDelegateContext();
        }
        LogLevel l = getLogLevel(level.getStandardLevel());
        m_service.handleEvents(getName(), m_bundle, ref, l, message, t);
    }

    private void setDelegateContext() {
        Map<String, Object> context = getPaxContext().getContext();
        if (context != null) {
            for (Map.Entry<String, Object> entry : context.entrySet()) {
                put(entry.getKey(), entry.getValue());
            }
        }
        if (m_bundle != null) {
            put("bundle.id", m_bundle.getBundleId());
            put("bundle.name", m_bundle.getSymbolicName());
            put("bundle.version", m_bundle.getVersion().toString());
        }
        m_service.lock(false);
    }

    private void put(String name, Object o) {
        if (o != null) {
            ThreadContext.put(name, o.toString());
        }
    }

    private void clearDelegateContext() {
        m_service.unlock(false);
        ThreadContext.remove("bundle.id");
        ThreadContext.remove("bundle.name");
        ThreadContext.remove("bundle.version");
    }

}
