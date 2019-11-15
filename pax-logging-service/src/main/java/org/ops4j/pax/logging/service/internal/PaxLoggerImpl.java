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
package org.ops4j.pax.logging.service.internal;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.ops4j.pax.logging.PaxContext;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingConstants;
import org.ops4j.pax.logging.PaxMarker;
import org.osgi.framework.Bundle;
import org.osgi.service.log.LogService;

/**
 * Log4J1 specific {@link PaxLogger} delegating directly to Log4J1's {@link Logger}.
 */
public class PaxLoggerImpl implements PaxLogger {

    // "the" delegate. org.apache.log4j.Logger should be loaded from pax-logging-service bundle and
    // not from pax-logging-api bundle
    private org.apache.log4j.Logger m_delegate;

    // FQCN for Log4J to get location info
    private String m_fqcn;
    // bundle associated with PaxLoggingService which is org.osgi.framework.ServiceFactory
    private Bundle m_bundle;
    // actual PaxLoggingService
    private PaxLoggingServiceImpl m_service;

    /**
     * @param bundle   The bundle that this PaxLogger belongs to.
     * @param delegate The Log4J delegate to receive the log message.
     * @param fqcn     The fully qualified classname of the client owning this logger.
     * @param service  The service to be used to handle the logging events.
     */
    PaxLoggerImpl(Bundle bundle, Logger delegate, String fqcn, PaxLoggingServiceImpl service) {
        m_delegate = delegate;
        m_fqcn = fqcn;
        m_bundle = bundle;
        m_service = service;
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

    @Override
    public void trace(String message, Throwable t) {
        if (isTraceEnabled()) {
            doLog(Level.TRACE, LogService.LOG_DEBUG, m_fqcn, message, t);
        }
    }

    @Override
    public void debug(String message, Throwable t) {
        if (isDebugEnabled()) {
            doLog(Level.DEBUG, LogService.LOG_DEBUG, m_fqcn, message, t);
        }
    }

    @Override
    public void inform(String message, Throwable t) {
        if (isInfoEnabled()) {
            doLog(Level.INFO, LogService.LOG_INFO, m_fqcn, message, t);
        }
    }

    @Override
    public void warn(String message, Throwable t) {
        if (isWarnEnabled()) {
            doLog(Level.WARN, LogService.LOG_WARNING, m_fqcn, message, t);
        }
    }

    @Override
    public void error(String message, Throwable t) {
        if (isErrorEnabled()) {
            doLog(Level.ERROR, LogService.LOG_ERROR, m_fqcn, message, t);
        }
    }

    @Override
    public void fatal(String message, Throwable t) {
        if (isFatalEnabled()) {
            doLog(Level.FATAL, LogService.LOG_ERROR, m_fqcn, message, t);
        }
    }

    @Override
    public void trace(String message, Throwable t, String fqcn) {
        if (isTraceEnabled()) {
            doLog(Level.TRACE, LogService.LOG_DEBUG, fqcn, message, t);
        }
    }

    @Override
    public void debug(String message, Throwable t, String fqcn) {
        if (isDebugEnabled()) {
            doLog(Level.DEBUG, LogService.LOG_DEBUG, fqcn, message, t);
        }
    }

    @Override
    public void inform(String message, Throwable t, String fqcn) {
        if (isInfoEnabled()) {
            doLog(Level.INFO, LogService.LOG_INFO, fqcn, message, t);
        }
    }

    @Override
    public void warn(String message, Throwable t, String fqcn) {
        if (isWarnEnabled()) {
            doLog(Level.WARN, LogService.LOG_WARNING, fqcn, message, t);
        }
    }

    @Override
    public void error(String message, Throwable t, String fqcn) {
        if (isErrorEnabled()) {
            doLog(Level.ERROR, LogService.LOG_ERROR, fqcn, message, t);
        }
    }

    @Override
    public void fatal(String message, Throwable t, String fqcn) {
        if (isFatalEnabled()) {
            doLog(Level.FATAL, LogService.LOG_ERROR, fqcn, message, t);
        }
    }

    @Override
    public void trace(PaxMarker marker, String message, Throwable t) {
        trace(message, t);
    }

    @Override
    public void debug(PaxMarker marker, String message, Throwable t) {
        debug(message, t);
    }

    @Override
    public void inform(PaxMarker marker, String message, Throwable t) {
        inform(message, t);
    }

    @Override
    public void warn(PaxMarker marker, String message, Throwable t) {
        warn(message, t);
    }

    @Override
    public void error(PaxMarker marker, String message, Throwable t) {
        error(message, t);
    }

    @Override
    public void fatal(PaxMarker marker, String message, Throwable t) {
        fatal(message, t);
    }

    @Override
    public void trace(PaxMarker marker, String message, Throwable t, String fqcn) {
        trace(message, t, fqcn);
    }

    @Override
    public void debug(PaxMarker marker, String message, Throwable t, String fqcn) {
        debug(message, t, fqcn);
    }

    @Override
    public void inform(PaxMarker marker, String message, Throwable t, String fqcn) {
        inform(message, t, fqcn);
    }

    @Override
    public void warn(PaxMarker marker, String message, Throwable t, String fqcn) {
        warn(message, t, fqcn);
    }

    @Override
    public void error(PaxMarker marker, String message, Throwable t, String fqcn) {
        error(message, t, fqcn);
    }

    @Override
    public void fatal(PaxMarker marker, String message, Throwable t, String fqcn) {
        fatal(message, t, fqcn);
    }

    @Override
    public int getLogLevel() {
        // org.apache.log4j.Priority.isGreaterOrEqual checks if "this" level's numerical value
        // is >= than passed value - TRACE < DEBUG and DEBUG >= TRACE
        // because Log4J1 has higher num value for more severe level (opposite to Syslog and OSGi)
        Level level = m_delegate.getEffectiveLevel();

        if (level == null)
            return LEVEL_ERROR;

        if (Level.TRACE.isGreaterOrEqual(level))
            return LEVEL_TRACE;

        if (Level.DEBUG.isGreaterOrEqual(level))
            return LEVEL_DEBUG;

        if (Level.INFO.isGreaterOrEqual(level))
            return LEVEL_INFO;

        if (Level.WARN.isGreaterOrEqual(level))
            return LEVEL_WARNING;

        return LEVEL_ERROR;
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

    private void doLog(final Level level, final int svcLevel, final String fqcn, final String message, final Throwable t) {
        if (System.getSecurityManager() != null) {
            AccessController.doPrivileged(
                    (PrivilegedAction<Void>) () -> {
                        doLog0(level, svcLevel, fqcn, message, t);
                        return null;
                    }
            );
        } else {
            doLog0(level, svcLevel, fqcn, message, t);
        }
    }

    /**
     * <p>Most important pax-logging-service log method that bridges pax-logging-api directly into Log4J1. Each
     * log invocation is wrapped with MDC configuration, where the following keys are always available:<ul>
     *     <li>{@code bundle.id} - from {@link Bundle#getBundleId()}</li>
     *     <li>{@code bundle.name} - from {@link Bundle#getSymbolicName()}</li>
     *     <li>{@code bundle.version} - from {@link Bundle#getVersion()}</li>
     * </ul></p>
     *
     * @param level
     * @param svcLevel
     * @param fqcn
     * @param message
     * @param t
     */
    private void doLog0(final Level level, final int svcLevel, final String fqcn, final String message, final Throwable t) {
        setDelegateContext();
        try {
            m_delegate.log(fqcn, level, message, t);
        } finally {
            clearDelegateContext();
        }
        m_service.handleEvents(m_bundle, null, svcLevel, message, t);
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
