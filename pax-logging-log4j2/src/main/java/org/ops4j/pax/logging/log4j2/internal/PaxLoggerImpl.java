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
import org.ops4j.pax.logging.PaxContext;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingConstants;
import org.ops4j.pax.logging.PaxMarker;
import org.osgi.framework.Bundle;
import org.osgi.service.log.LogService;

/**
 * Log4J2 specific {@link PaxLogger} delegating directly to Log4J2's {@link ExtendedLogger}.
 */
public class PaxLoggerImpl implements PaxLogger {

    // "the" delegate. org.apache.logging.log4j.spi.ExtendedLogger
    private ExtendedLogger m_delegate;

    // FQCN for Log42 to get location info
    private String m_fqcn;
    // bundle associated with PaxLoggingService which is org.osgi.framework.ServiceFactory
    private Bundle m_bundle;
    // actual PaxLoggingService
    private PaxLoggingServiceImpl m_service;

//    private BundleRevision m_bundleRevision;
//    private Long m_bundleId;
//    private String m_bundleSymbolicName;
//    private String m_bundleVersion;

    /**
     * @param bundle   The bundle that this PaxLogger belongs to.
     * @param delegate The Log4J delegate to receive the log message.
     * @param fqcn     The fully qualified classname of the client owning this logger.
     * @param service  The service to be used to handle the logging events.
     */
    PaxLoggerImpl(Bundle bundle, ExtendedLogger delegate, String fqcn, PaxLoggingServiceImpl service) {
        m_delegate = delegate;
        m_fqcn = fqcn;
        m_bundle = bundle;
        m_service = service;
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

    @Override
    public void trace(String message, Throwable t) {
        if (isTraceEnabled()) {
            doLog(null, Level.TRACE, LogService.LOG_DEBUG, m_fqcn, message, t);
        }
    }

    @Override
    public void debug(String message, Throwable t) {
        if (isDebugEnabled()) {
            doLog(null, Level.DEBUG, LogService.LOG_DEBUG, m_fqcn, message, t);
        }
    }

    @Override
    public void inform(String message, Throwable t) {
        if (isInfoEnabled()) {
            doLog(null, Level.INFO, LogService.LOG_INFO, m_fqcn, message, t);
        }
    }

    @Override
    public void warn(String message, Throwable t) {
        if (isWarnEnabled()) {
            doLog(null, Level.WARN, LogService.LOG_WARNING, m_fqcn, message, t);
        }
    }

    @Override
    public void error(String message, Throwable t) {
        if (isErrorEnabled()) {
            doLog(null, Level.ERROR, LogService.LOG_ERROR, m_fqcn, message, t);
        }
    }

    @Override
    public void fatal(String message, Throwable t) {
        if (isFatalEnabled()) {
            doLog(null, Level.FATAL, LogService.LOG_ERROR, m_fqcn, message, t);
        }
    }

    @Override
    public void trace(String message, Throwable t, String fqcn) {
        if (isTraceEnabled()) {
            doLog(null, Level.TRACE, LogService.LOG_DEBUG, fqcn, message, t);
        }
    }

    @Override
    public void debug(String message, Throwable t, String fqcn) {
        if (isDebugEnabled()) {
            doLog(null, Level.DEBUG, LogService.LOG_DEBUG, fqcn, message, t);
        }
    }

    @Override
    public void inform(String message, Throwable t, String fqcn) {
        if (isInfoEnabled()) {
            doLog(null, Level.INFO, LogService.LOG_INFO, fqcn, message, t);
        }
    }

    @Override
    public void warn(String message, Throwable t, String fqcn) {
        if (isWarnEnabled()) {
            doLog(null, Level.WARN, LogService.LOG_WARNING, fqcn, message, t);
        }
    }

    @Override
    public void error(String message, Throwable t, String fqcn) {
        if (isErrorEnabled()) {
            doLog(null, Level.ERROR, LogService.LOG_ERROR, fqcn, message, t);
        }
    }

    @Override
    public void fatal(String message, Throwable t, String fqcn) {
        if (isFatalEnabled()) {
            doLog(null, Level.FATAL, LogService.LOG_ERROR, fqcn, message, t);
        }
    }

    @Override
    public void trace(PaxMarker marker, String message, Throwable t, String fqcn) {
        if (isTraceEnabled(marker)) {
            doLog(marker.log4j2Marker(), Level.TRACE, LogService.LOG_DEBUG, fqcn, message, t);
        }
    }

    @Override
    public void debug(PaxMarker marker, String message, Throwable t, String fqcn) {
        if (isDebugEnabled(marker)) {
            doLog(marker.log4j2Marker(), Level.DEBUG, LogService.LOG_DEBUG, fqcn, message, t);
        }
    }

    @Override
    public void inform(PaxMarker marker, String message, Throwable t, String fqcn) {
        if (isInfoEnabled(marker)) {
            doLog(marker.log4j2Marker(), Level.INFO, LogService.LOG_INFO, fqcn, message, t);
        }
    }

    @Override
    public void warn(PaxMarker marker, String message, Throwable t, String fqcn) {
        if (isWarnEnabled(marker)) {
            doLog(marker.log4j2Marker(), Level.WARN, LogService.LOG_WARNING, fqcn, message, t);
        }
    }

    @Override
    public void error(PaxMarker marker, String message, Throwable t, String fqcn) {
        if (isErrorEnabled(marker)) {
            doLog(marker.log4j2Marker(), Level.ERROR, LogService.LOG_ERROR, fqcn, message, t);
        }
    }

    @Override
    public void fatal(PaxMarker marker, String message, Throwable t, String fqcn) {
        if (isFatalEnabled(marker)) {
            doLog(marker.log4j2Marker(), Level.FATAL, LogService.LOG_ERROR, fqcn, message, t);
        }
    }

    @Override
    public void trace(PaxMarker marker, String message, Throwable t) {
        if (isTraceEnabled(marker)) {
            doLog(marker.log4j2Marker(), Level.TRACE, LogService.LOG_DEBUG, m_fqcn, message, t);
        }
    }

    @Override
    public void debug(PaxMarker marker, String message, Throwable t) {
        if (isDebugEnabled(marker)) {
            doLog(marker.log4j2Marker(), Level.DEBUG, LogService.LOG_DEBUG, m_fqcn, message, t);
        }
    }

    @Override
    public void inform(PaxMarker marker, String message, Throwable t) {
        if (isInfoEnabled(marker)) {
            doLog(marker.log4j2Marker(), Level.INFO, LogService.LOG_INFO, m_fqcn, message, t);
        }
    }

    @Override
    public void warn(PaxMarker marker, String message, Throwable t) {
        if (isWarnEnabled(marker)) {
            doLog(marker.log4j2Marker(), Level.WARN, LogService.LOG_WARNING, m_fqcn, message, t);
        }
    }

    @Override
    public void error(PaxMarker marker, String message, Throwable t) {
        if (isErrorEnabled(marker)) {
            doLog(marker.log4j2Marker(), Level.ERROR, LogService.LOG_ERROR, m_fqcn, message, t);
        }
    }

    @Override
    public void fatal(PaxMarker marker, String message, Throwable t) {
        if (isFatalEnabled(marker)) {
            doLog(marker.log4j2Marker(), Level.FATAL, LogService.LOG_ERROR, m_fqcn, message, t);
        }
    }

    @Override
    public int getLogLevel() {
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
    public String getName() {
        return m_delegate.getName();
    }

    @Override
    public PaxContext getPaxContext() {
        return m_service.getPaxContext();
    }

    // private methods

    private void doLog(final Marker marker, final Level level, final int svcLevel, final String fqcn, final String message, final Throwable t) {
        if (System.getSecurityManager() != null) {
            AccessController.doPrivileged(
                    (PrivilegedAction<Void>) () -> {
                        doLog0(marker, level, svcLevel, fqcn, message, t);
                        return null;
                    }
            );
        } else {
            doLog0(marker, level, svcLevel, fqcn, message, t);
        }
    }

    /**
     * Most important pax-logging-service log method that bridges pax-logging-api directly into Log4J2.
     * @param marker
     * @param level
     * @param svcLevel
     * @param fqcn
     * @param message
     * @param t
     */
    private void doLog0(Marker marker, Level level, int svcLevel, String fqcn, String message, Throwable t) {
        Message originalMessage = setDelegateContext();
        try {
            Message msg = originalMessage == null ? m_delegate.getMessageFactory().newMessage(message)
                    : originalMessage;
            m_delegate.logMessage(fqcn, level, marker, msg, t);
        } finally {
            clearDelegateContext();
        }
        m_service.handleEvents(m_bundle, null, svcLevel, message, t);
    }

    private Message setDelegateContext() {
        Message originalMessage = null;
        Map<String, Object> context = getPaxContext().getContext();
        if (context != null) {
            originalMessage = (Message) context.remove(PaxLoggingConstants._LOG4J2_MESSAGE);
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

        return originalMessage;
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
