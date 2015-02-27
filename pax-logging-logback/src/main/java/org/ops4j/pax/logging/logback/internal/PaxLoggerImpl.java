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

import org.ops4j.pax.logging.PaxContext;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingService;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.service.log.LogService;
import org.slf4j.MDC;
import org.slf4j.spi.LocationAwareLogger;
import org.slf4j.spi.MDCAdapter;

import ch.qos.logback.classic.Logger;

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
 * </ul>
 *
 * @author Chris Dolan
 * @author Raul Kripalani
 */
public class PaxLoggerImpl
    implements PaxLogger
{

    private final Logger m_delegate;
    private final String m_fqcn;
    private final Bundle m_bundle;
    private final PaxLoggingService m_service;
    private final PaxEventHandler m_eventHandler;

    /**
     * @param bundle   The bundle that this PaxLogger belongs to.
     * @param delegate The logback delegate to receive the log message.
     * @param fqcn     The fully qualified class name of the client owning this logger.
     * @param service  The service to be used to handle the logging events.
     * @param eventHandler helper to process log events
     */
    PaxLoggerImpl( Bundle bundle, Logger delegate, String fqcn, PaxLoggingService service, PaxEventHandler eventHandler )
    {
        m_delegate = delegate;
        m_fqcn = fqcn;
        m_bundle = bundle;
        m_service = service;
        m_eventHandler = eventHandler;
    }

    public boolean isTraceEnabled()
    {
        return m_delegate.isTraceEnabled();
    }

    public boolean isDebugEnabled()
    {
        return m_delegate.isDebugEnabled();
    }

    public boolean isWarnEnabled()
    {
        return m_delegate.isWarnEnabled();
    }

    public boolean isInfoEnabled()
    {
        return m_delegate.isInfoEnabled();
    }

    public boolean isErrorEnabled()
    {
        return m_delegate.isErrorEnabled();
    }

    public boolean isFatalEnabled()
    {
        return m_delegate.isErrorEnabled();
    }

    private void setDelegateContext() 
    {
        // Logback's MDCConverter pulls in MDC properties through the slf4j's MDC class already. 
        // Therefore there's no need to bridge two MDC implementations, like in the log4j PaxLoggerImpl.
        // See PAXLOGGING-165.
        MDCAdapter adapter = MDC.getMDCAdapter();
        if (m_bundle != null && adapter != null) {
            adapter.put("bundle.id", String.valueOf(m_bundle.getBundleId()));
            adapter.put("bundle.name", m_bundle.getSymbolicName());
            adapter.put("bundle.version", m_bundle.getVersion().toString());
        }

    }

    private void clearDelegateContext() 
    {
        MDCAdapter adapter = MDC.getMDCAdapter();
        if (m_bundle != null && adapter != null) {
            adapter.remove("bundle.id");
            adapter.remove("bundle.name");
            adapter.remove("bundle.version");
        }

        // No need to clear the underlying MDC
        // See PAXLOGGING-165.
    }

    public void trace(String message, Throwable t) {
        if (isTraceEnabled()) {
            setDelegateContext();
            m_delegate.log(null, m_fqcn, LocationAwareLogger.TRACE_INT, message, null, t);
            clearDelegateContext();
            m_eventHandler.handleEvents(m_bundle, null, LogService.LOG_DEBUG, message, t);
        }
    }

    public void debug( String message, Throwable t ) {
        if (isDebugEnabled()) {
            setDelegateContext();
            m_delegate.log(null, m_fqcn, LocationAwareLogger.DEBUG_INT, message, null, t);
            clearDelegateContext();
            m_eventHandler.handleEvents(m_bundle, null, LogService.LOG_DEBUG, message, t);
        }
    }

    public void inform( String message, Throwable t ) {
        if (isInfoEnabled()) {
            setDelegateContext();
            m_delegate.log(null, m_fqcn, LocationAwareLogger.INFO_INT, message, null, t);
            clearDelegateContext();
            m_eventHandler.handleEvents(m_bundle, null, LogService.LOG_INFO, message, t);
        }
    }

    public void warn( String message, Throwable t ) {
        if (isWarnEnabled()) {
            setDelegateContext();
            m_delegate.log(null, m_fqcn, LocationAwareLogger.WARN_INT, message, null, t);
            clearDelegateContext();
            m_eventHandler.handleEvents(m_bundle, null, LogService.LOG_WARNING, message, t);
        }
    }

    public void error( String message, Throwable t ) {
        if (isErrorEnabled()) {
            setDelegateContext();
            m_delegate.log(null, m_fqcn, LocationAwareLogger.ERROR_INT, message, null, t);
            clearDelegateContext();
            m_eventHandler.handleEvents(m_bundle, null, LogService.LOG_ERROR, message, t);
        }
    }

    public void fatal( String message, Throwable t ) {
        if (isFatalEnabled()) {
            setDelegateContext();
            m_delegate.log(null, m_fqcn, LocationAwareLogger.ERROR_INT, message, null, t);
            clearDelegateContext();
            m_eventHandler.handleEvents(m_bundle, null, LogService.LOG_ERROR, message, t);
        }
    }

    public void trace(String message, Throwable t, String fqcn) {
        if (isTraceEnabled()) {
            setDelegateContext();
            m_delegate.log(null, fqcn, LocationAwareLogger.TRACE_INT, message, null, t);
            clearDelegateContext();
            m_eventHandler.handleEvents(m_bundle, null, LogService.LOG_DEBUG, message, t);
        }
    }

    public void debug(String message, Throwable t, String fqcn) {
        if (isDebugEnabled()) {
            setDelegateContext();
            m_delegate.log( null, fqcn, LocationAwareLogger.DEBUG_INT, message, null, t );
            clearDelegateContext();
            m_eventHandler.handleEvents(m_bundle, null, LogService.LOG_DEBUG, message, t);
        }
    }

    public void inform(String message, Throwable t, String fqcn) {
        if (isInfoEnabled()) {
            setDelegateContext();
            m_delegate.log(null, fqcn, LocationAwareLogger.INFO_INT, message, null, t);
            clearDelegateContext();
            m_eventHandler.handleEvents( m_bundle, null, LogService.LOG_INFO, message, t );
        }
    }

    public void warn(String message, Throwable t, String fqcn) {
        if (isWarnEnabled()) {
            setDelegateContext();
            m_delegate.log( null, fqcn, LocationAwareLogger.WARN_INT, message, null, t );
            clearDelegateContext();
            m_eventHandler.handleEvents( m_bundle, null, LogService.LOG_WARNING, message, t );
        }
    }

    public void error(String message, Throwable t, String fqcn) {
        if (isErrorEnabled()) {
            setDelegateContext();
            m_delegate.log( null, fqcn, LocationAwareLogger.ERROR_INT, message, null, t );
            clearDelegateContext();
            m_eventHandler.handleEvents( m_bundle, null, LogService.LOG_ERROR, message, t );
        }
    }

    public void fatal(String message, Throwable t, String fqcn) {
        if (isFatalEnabled()) {
            setDelegateContext();
            m_delegate.log( null, fqcn, LocationAwareLogger.ERROR_INT, message, null, t );
            clearDelegateContext();
            m_eventHandler.handleEvents( m_bundle, null, LogService.LOG_ERROR, message, t );
        }
    }

    public int getLogLevel()
    {
        return new PaxLevelForLogback(m_delegate.getEffectiveLevel()).toInt();
    }

    public String getName()
    {
        return m_delegate.getName();
    }

    public PaxContext getPaxContext()
    {
        return m_service.getPaxContext();
    }
}
