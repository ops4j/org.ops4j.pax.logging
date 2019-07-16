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
package org.ops4j.pax.logging.internal;

import org.ops4j.pax.logging.PaxContext;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingService;
import org.ops4j.pax.logging.PaxMarker;
import org.ops4j.pax.logging.spi.support.FallbackLogFactory;
import org.osgi.framework.Bundle;

/**
 * <p>A {@link PaxLogger} that's delegating to real {@link PaxLoggingService} when one's available and falls back
 * to {@link FallbackLogFactory} when the service is gone.</p>
 * <p>This class itself doesn't track {@link PaxLoggingService} - {@link org.ops4j.pax.logging.OSGIPaxLoggingManager}
 * does that and calls {@link #added(PaxLoggingService)} and {@link #removed()} methods.</p>
 */
public class TrackingLogger implements PaxLogger {

    private PaxLoggingService m_service;
    private String m_category;
    private Bundle m_bundle;
    private PaxLogger m_delegate;
    private String m_fqcn;

    public TrackingLogger(PaxLoggingService service, String category, Bundle bundle, String fqcn) {
        m_fqcn = fqcn;
        m_category = category;
        m_bundle = bundle;
        added(service);
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
        return m_delegate.isTraceEnabled(marker);
    }

    @Override
    public boolean isDebugEnabled(PaxMarker marker) {
        return m_delegate.isDebugEnabled(marker);
    }

    @Override
    public boolean isInfoEnabled(PaxMarker marker) {
        return m_delegate.isInfoEnabled(marker);
    }

    @Override
    public boolean isWarnEnabled(PaxMarker marker) {
        return m_delegate.isWarnEnabled(marker);
    }

    @Override
    public boolean isErrorEnabled(PaxMarker marker) {
        return m_delegate.isErrorEnabled(marker);
    }

    @Override
    public boolean isFatalEnabled(PaxMarker marker) {
        return m_delegate.isFatalEnabled(marker);
    }

    @Override
    public void trace(String message, Throwable t) {
        m_delegate.trace(message, t);
    }

    @Override
    public void debug(String message, Throwable t) {
        m_delegate.debug(message, t);
    }

    @Override
    public void inform(String message, Throwable t) {
        m_delegate.inform(message, t);
    }

    @Override
    public void warn(String message, Throwable t) {
        m_delegate.warn(message, t);
    }

    @Override
    public void error(String message, Throwable t) {
        m_delegate.error(message, t);
    }

    @Override
    public void fatal(String message, Throwable t) {
        m_delegate.fatal(message, t);
    }

    @Override
    public void trace(String message, Throwable t, String fqcn) {
        m_delegate.trace(message, t, fqcn);
    }

    @Override
    public void debug(String message, Throwable t, String fqcn) {
        m_delegate.debug(message, t, fqcn);
    }

    @Override
    public void inform(String message, Throwable t, String fqcn) {
        m_delegate.inform(message, t, fqcn);
    }

    public void warn(String message, Throwable t, String fqcn) {
        m_delegate.warn(message, t, fqcn);
    }

    @Override
    public void error(String message, Throwable t, String fqcn) {
        m_delegate.error(message, t, fqcn);
    }

    @Override
    public void fatal(String message, Throwable t, String fqcn) {
        m_delegate.fatal(message, t, fqcn);
    }

    @Override
    public void trace(PaxMarker marker, String message, Throwable t) {
        m_delegate.trace(marker, message, t);
    }

    @Override
    public void debug(PaxMarker marker, String message, Throwable t) {
        m_delegate.debug(marker, message, t);
    }

    @Override
    public void inform(PaxMarker marker, String message, Throwable t) {
        m_delegate.inform(marker, message, t);
    }

    @Override
    public void warn(PaxMarker marker, String message, Throwable t) {
        m_delegate.warn(marker, message, t);
    }

    @Override
    public void error(PaxMarker marker, String message, Throwable t) {
        m_delegate.error(marker, message, t);
    }

    @Override
    public void fatal(PaxMarker marker, String message, Throwable t) {
        m_delegate.fatal(marker, message, t);
    }

    @Override
    public void trace(PaxMarker marker, String message, Throwable t, String fqcn) {
        m_delegate.trace(marker, message, t, fqcn);
    }

    @Override
    public void debug(PaxMarker marker, String message, Throwable t, String fqcn) {
        m_delegate.debug(marker, message, t, fqcn);
    }

    @Override
    public void inform(PaxMarker marker, String message, Throwable t, String fqcn) {
        m_delegate.inform(marker, message, t, fqcn);
    }

    @Override
    public void warn(PaxMarker marker, String message, Throwable t, String fqcn) {
        m_delegate.warn(marker, message, t, fqcn);
    }

    @Override
    public void error(PaxMarker marker, String message, Throwable t, String fqcn) {
        m_delegate.error(marker, message, t, fqcn);
    }

    @Override
    public void fatal(PaxMarker marker, String message, Throwable t, String fqcn) {
        m_delegate.fatal(marker, message, t, fqcn);
    }

    @Override
    public int getLogLevel() {
        return m_delegate.getLogLevel();
    }

    @Override
    public String getName() {
        return m_delegate.getName();
    }

    @Override
    public PaxContext getPaxContext() {
        if (m_service != null) {
            return m_service.getPaxContext();
        } else {
            return m_delegate.getPaxContext();
        }
    }

    /**
     * {@link org.ops4j.pax.logging.OSGIPaxLoggingManager} sets an instance of real {@link PaxLoggingService}
     * when it's available. Logger can switch to real delegate.
     * @param service
     */
    public void added(PaxLoggingService service) {
        m_service = service;
        if (m_service != null) {
            m_delegate = m_service.getLogger(m_bundle, m_category, m_fqcn);
        } else {
            m_delegate = FallbackLogFactory.createFallbackLog(m_bundle, m_category);
        }
    }

    /**
     * Called by the tracker when there is no service available, and the reference should
     * be dropped. Delegate is switched to fallback logger.
     */
    public void removed() {
        m_service = null;
        m_delegate = FallbackLogFactory.createFallbackLog(m_bundle, m_category);
    }

}
