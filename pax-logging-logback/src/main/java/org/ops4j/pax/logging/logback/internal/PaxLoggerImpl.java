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

import ch.qos.logback.classic.Logger;
import org.ops4j.pax.logging.PaxContext;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.logback.internal.spi.PaxLevelForLogback;
import org.osgi.framework.Bundle;
import org.osgi.service.log.LogService;
import org.slf4j.MDC;
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

    // "the" delegate.
    private final Logger m_delegate;

    // FQCN to get location info
    private final String m_fqcn;
    // bundle associated with PaxLoggingService which is org.osgi.framework.ServiceFactory
    private final Bundle m_bundle;
    // actual PaxLoggingService
    private final PaxLoggingServiceImpl m_service;

    /**
     * @param bundle   The bundle that this PaxLogger belongs to.
     * @param delegate The logback delegate to receive the log message.
     * @param fqcn     The fully qualified class name of the client owning this logger.
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
    public void trace(String message, Throwable t) {
        if (isTraceEnabled()) {
            doLog(LocationAwareLogger.TRACE_INT, LogService.LOG_DEBUG, m_fqcn, message, t);
        }
    }

    @Override
    public void debug(String message, Throwable t) {
        if (isDebugEnabled()) {
            doLog(LocationAwareLogger.DEBUG_INT, LogService.LOG_DEBUG, m_fqcn, message, t);
        }
    }

    @Override
    public void inform(String message, Throwable t) {
        if (isInfoEnabled()) {
            doLog(LocationAwareLogger.INFO_INT, LogService.LOG_INFO, m_fqcn, message, t);
        }
    }

    @Override
    public void warn(String message, Throwable t) {
        if (isWarnEnabled()) {
            doLog(LocationAwareLogger.WARN_INT, LogService.LOG_WARNING, m_fqcn, message, t);
        }
    }

    @Override
    public void error(String message, Throwable t) {
        if (isErrorEnabled()) {
            doLog(LocationAwareLogger.ERROR_INT, LogService.LOG_ERROR, m_fqcn, message, t);
        }
    }

    @Override
    public void fatal(String message, Throwable t) {
        if (isFatalEnabled()) {
            doLog(LocationAwareLogger.ERROR_INT, LogService.LOG_ERROR, m_fqcn, message, t);
        }
    }

    @Override
    public void trace(String message, Throwable t, String fqcn) {
        if (isTraceEnabled()) {
            doLog(LocationAwareLogger.TRACE_INT, LogService.LOG_DEBUG, fqcn, message, t);
        }
    }

    @Override
    public void debug(String message, Throwable t, String fqcn) {
        if (isDebugEnabled()) {
            doLog(LocationAwareLogger.DEBUG_INT, LogService.LOG_DEBUG, fqcn, message, t);
        }
    }

    @Override
    public void inform(String message, Throwable t, String fqcn) {
        if (isInfoEnabled()) {
            doLog(LocationAwareLogger.INFO_INT, LogService.LOG_INFO, fqcn, message, t);
        }
    }

    @Override
    public void warn(String message, Throwable t, String fqcn) {
        if (isWarnEnabled()) {
            doLog(LocationAwareLogger.WARN_INT, LogService.LOG_WARNING, fqcn, message, t);
        }
    }

    @Override
    public void error(String message, Throwable t, String fqcn) {
        if (isErrorEnabled()) {
            doLog(LocationAwareLogger.ERROR_INT, LogService.LOG_ERROR, fqcn, message, t);
        }
    }

    @Override
    public void fatal(String message, Throwable t, String fqcn) {
        if (isFatalEnabled()) {
            doLog(LocationAwareLogger.ERROR_INT, LogService.LOG_ERROR, fqcn, message, t);
        }
    }

    @Override
    public int getLogLevel() {
        return new PaxLevelForLogback(m_delegate.getEffectiveLevel()).toInt();
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


    private void doLog(final int level, final int svcLevel, final String fqcn, final String message, final Throwable t) {
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
     * Most important pax-logging-logback log method that bridges pax-logging-api directly into Logback. EAch
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
    private void doLog0(final int level, final int svcLevel, final String fqcn, final String message, final Throwable t) {
        setDelegateContext();
        try {
            m_delegate.log(null, fqcn, level, message, null, t);
        } finally {
            clearDelegateContext();
        }
        m_service.handleEvents(m_bundle, null, svcLevel, message, t);
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
