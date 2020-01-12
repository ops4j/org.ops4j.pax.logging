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
import org.osgi.service.log.LogLevel;
import org.osgi.service.log.LoggerConsumer;

/**
 * A {@link PaxLogger} that's delegating to real {@link PaxLoggingService} when one's available and falls back
 * to {@link FallbackLogFactory} when the service is gone.
 *
 * This class itself doesn't track {@link PaxLoggingService} - {@link org.ops4j.pax.logging.OSGIPaxLoggingManager}
 * does that and calls {@link #added(PaxLoggingService)} and {@link #removed()} methods.
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

    // R7: org.osgi.service.log.Logger

    @Override
    public void trace(String message) {
        m_delegate.trace(message);
    }

    @Override
    public void trace(String format, Object arg) {
        m_delegate.trace(format, arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        m_delegate.trace(format, arg1, arg2);
    }

    @Override
    public void trace(String format, Object... arguments) {
        m_delegate.trace(format, arguments);
    }

    @Override
    public <E extends Exception> void trace(LoggerConsumer<E> consumer) throws E {
        m_delegate.trace(consumer);
    }

    @Override
    public void trace(PaxMarker marker, String message) {
        m_delegate.trace(marker, message);
    }

    @Override
    public void trace(PaxMarker marker, String format, Object arg) {
        m_delegate.trace(marker, format, arg);
    }

    @Override
    public void trace(PaxMarker marker, String format, Object arg1, Object arg2) {
        m_delegate.trace(marker, format, arg1, arg2);
    }

    @Override
    public void trace(PaxMarker marker, String format, Object... arguments) {
        m_delegate.trace(marker, format, arguments);
    }

    @Override
    public <E extends Exception> void trace(PaxMarker marker, LoggerConsumer<E> consumer) throws E {
        m_delegate.trace(marker, consumer);
    }

    @Override
    public void debug(String message) {
        m_delegate.debug(message);
    }

    @Override
    public void debug(String format, Object arg) {
        m_delegate.debug(format, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        m_delegate.debug(format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object... arguments) {
        m_delegate.debug(format, arguments);
    }

    @Override
    public <E extends Exception> void debug(LoggerConsumer<E> consumer) throws E {
        m_delegate.debug(consumer);
    }

    @Override
    public void debug(PaxMarker marker, String message) {
        m_delegate.debug(marker, message);
    }

    @Override
    public void debug(PaxMarker marker, String format, Object arg) {
        m_delegate.debug(marker, format, arg);
    }

    @Override
    public void debug(PaxMarker marker, String format, Object arg1, Object arg2) {
        m_delegate.debug(marker, format, arg1, arg2);
    }

    @Override
    public void debug(PaxMarker marker, String format, Object... arguments) {
        m_delegate.debug(marker, format, arguments);
    }

    @Override
    public <E extends Exception> void debug(PaxMarker marker, LoggerConsumer<E> consumer) throws E {
        m_delegate.debug(marker, consumer);
    }
    
    @Override
    public void info(String message) {
        m_delegate.info(message);
    }

    @Override
    public void info(String format, Object arg) {
        m_delegate.info(format, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        m_delegate.info(format, arg1, arg2);
    }

    @Override
    public void info(String format, Object... arguments) {
        m_delegate.info(format, arguments);
    }

    @Override
    public <E extends Exception> void info(LoggerConsumer<E> consumer) throws E {
        m_delegate.info(consumer);
    }

    @Override
    public void info(PaxMarker marker, String message) {
        m_delegate.info(marker, message);
    }

    @Override
    public void info(PaxMarker marker, String format, Object arg) {
        m_delegate.info(marker, format, arg);
    }

    @Override
    public void info(PaxMarker marker, String format, Object arg1, Object arg2) {
        m_delegate.info(marker, format, arg1, arg2);
    }

    @Override
    public void info(PaxMarker marker, String format, Object... arguments) {
        m_delegate.info(marker, format, arguments);
    }

    @Override
    public <E extends Exception> void info(PaxMarker marker, LoggerConsumer<E> consumer) throws E {
        m_delegate.info(marker, consumer);
    }
    
    @Override
    public void warn(String message) {
        m_delegate.warn(message);
    }

    @Override
    public void warn(String format, Object arg) {
        m_delegate.warn(format, arg);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        m_delegate.warn(format, arg1, arg2);
    }

    @Override
    public void warn(String format, Object... arguments) {
        m_delegate.warn(format, arguments);
    }

    @Override
    public <E extends Exception> void warn(LoggerConsumer<E> consumer) throws E {
        m_delegate.warn(consumer);
    }

    @Override
    public void warn(PaxMarker marker, String message) {
        m_delegate.warn(marker, message);
    }

    @Override
    public void warn(PaxMarker marker, String format, Object arg) {
        m_delegate.warn(marker, format, arg);
    }

    @Override
    public void warn(PaxMarker marker, String format, Object arg1, Object arg2) {
        m_delegate.warn(marker, format, arg1, arg2);
    }

    @Override
    public void warn(PaxMarker marker, String format, Object... arguments) {
        m_delegate.warn(marker, format, arguments);
    }

    @Override
    public <E extends Exception> void warn(PaxMarker marker, LoggerConsumer<E> consumer) throws E {
        m_delegate.warn(marker, consumer);
    }
    
    @Override
    public void error(String message) {
        m_delegate.error(message);
    }

    @Override
    public void error(String format, Object arg) {
        m_delegate.error(format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        m_delegate.error(format, arg1, arg2);
    }

    @Override
    public void error(String format, Object... arguments) {
        m_delegate.error(format, arguments);
    }

    @Override
    public <E extends Exception> void error(LoggerConsumer<E> consumer) throws E {
        m_delegate.error(consumer);
    }

    @Override
    public void error(PaxMarker marker, String message) {
        m_delegate.error(marker, message);
    }

    @Override
    public void error(PaxMarker marker, String format, Object arg) {
        m_delegate.error(marker, format, arg);
    }

    @Override
    public void error(PaxMarker marker, String format, Object arg1, Object arg2) {
        m_delegate.error(marker, format, arg1, arg2);
    }

    @Override
    public void error(PaxMarker marker, String format, Object... arguments) {
        m_delegate.error(marker, format, arguments);
    }

    @Override
    public <E extends Exception> void error(PaxMarker marker, LoggerConsumer<E> consumer) throws E {
        m_delegate.error(marker, consumer);
    }
    
    @Override
    public void fatal(String message) {
        m_delegate.fatal(message);
    }

    @Override
    public void fatal(String format, Object arg) {
        m_delegate.fatal(format, arg);
    }

    @Override
    public void fatal(String format, Object arg1, Object arg2) {
        m_delegate.fatal(format, arg1, arg2);
    }

    @Override
    public void fatal(String format, Object... arguments) {
        m_delegate.fatal(format, arguments);
    }

    @Override
    public <E extends Exception> void fatal(LoggerConsumer<E> consumer) throws E {
        m_delegate.fatal(consumer);
    }

    @Override
    public void fatal(PaxMarker marker, String message) {
        m_delegate.fatal(marker, message);
    }

    @Override
    public void fatal(PaxMarker marker, String format, Object arg) {
        m_delegate.fatal(marker, format, arg);
    }

    @Override
    public void fatal(PaxMarker marker, String format, Object arg1, Object arg2) {
        m_delegate.fatal(marker, format, arg1, arg2);
    }

    @Override
    public void fatal(PaxMarker marker, String format, Object... arguments) {
        m_delegate.fatal(marker, format, arguments);
    }

    @Override
    public <E extends Exception> void fatal(PaxMarker marker, LoggerConsumer<E> consumer) throws E {
        m_delegate.fatal(marker, consumer);
    }

    @Override
    public void audit(String message) {
        m_delegate.audit(message);
    }

    @Override
    public void audit(String format, Object arg) {
        m_delegate.audit(format, arg);
    }

    @Override
    public void audit(String format, Object arg1, Object arg2) {
        m_delegate.audit(format, arg1, arg2);
    }

    @Override
    public void audit(String format, Object... arguments) {
        m_delegate.audit(format, arguments);
    }

    @Override
    public <E extends Exception> void audit(LoggerConsumer<E> consumer) throws E {
        m_delegate.audit(consumer);
    }

    @Override
    public void audit(PaxMarker marker, String message) {
        m_delegate.audit(marker, message);
    }

    @Override
    public void audit(PaxMarker marker, String format, Object arg) {
        m_delegate.audit(marker, format, arg);
    }

    @Override
    public void audit(PaxMarker marker, String format, Object arg1, Object arg2) {
        m_delegate.audit(marker, format, arg1, arg2);
    }

    @Override
    public void audit(PaxMarker marker, String format, Object... arguments) {
        m_delegate.audit(marker, format, arguments);
    }

    @Override
    public <E extends Exception> void audit(PaxMarker marker, LoggerConsumer<E> consumer) throws E {
        m_delegate.audit(marker, consumer);
    }

    @Override
    public void fqtrace(String fqcn, String message) {
        m_delegate.fqtrace(fqcn, message);
    }

    @Override
    public void fqdebug(String fqcn, String message) {
        m_delegate.fqdebug(fqcn, message);
    }

    @Override
    public void fqinfo(String fqcn, String message) {
        m_delegate.fqinfo(fqcn, message);
    }

    @Override
    public void fqwarn(String fqcn, String message) {
        m_delegate.fqwarn(fqcn, message);
    }

    @Override
    public void fqerror(String fqcn, String message) {
        m_delegate.fqerror(fqcn, message);
    }

    @Override
    public void fqfatal(String fqcn, String message) {
        m_delegate.fqfatal(fqcn, message);
    }

    @Override
    public void fqtrace(String fqcn, PaxMarker marker, String message) {
        m_delegate.fqtrace(fqcn, marker, message);
    }

    @Override
    public void fqdebug(String fqcn, PaxMarker marker, String message) {
        m_delegate.fqdebug(fqcn, marker, message);
    }

    @Override
    public void fqinfo(String fqcn, PaxMarker marker, String message) {
        m_delegate.fqinfo(fqcn, marker, message);
    }

    @Override
    public void fqwarn(String fqcn, PaxMarker marker, String message) {
        m_delegate.fqwarn(fqcn, marker, message);
    }

    @Override
    public void fqerror(String fqcn, PaxMarker marker, String message) {
        m_delegate.fqerror(fqcn, marker, message);
    }

    @Override
    public void fqfatal(String fqcn, PaxMarker marker, String message) {
        m_delegate.fqfatal(fqcn, marker, message);
    }

    @Override
    public void fqtrace(String fqcn, String message, Throwable t) {
        m_delegate.fqtrace(fqcn, message, t);
    }

    @Override
    public void fqdebug(String fqcn, String message, Throwable t) {
        m_delegate.fqdebug(fqcn, message, t);
    }

    @Override
    public void fqinfo(String fqcn, String message, Throwable t) {
        m_delegate.fqinfo(fqcn, message, t);
    }

    @Override
    public void fqwarn(String fqcn, String message, Throwable t) {
        m_delegate.fqwarn(fqcn, message, t);
    }

    @Override
    public void fqerror(String fqcn, String message, Throwable t) {
        m_delegate.fqerror(fqcn, message, t);
    }

    @Override
    public void fqfatal(String fqcn, String message, Throwable t) {
        m_delegate.fqfatal(fqcn, message, t);
    }

    @Override
    public void fqtrace(String fqcn, PaxMarker marker, String message, Throwable t) {
        m_delegate.fqtrace(fqcn,marker, message, t);
    }

    @Override
    public void fqdebug(String fqcn, PaxMarker marker, String message, Throwable t) {
        m_delegate.fqdebug(fqcn,marker, message, t);
    }

    @Override
    public void fqinfo(String fqcn, PaxMarker marker, String message, Throwable t) {
        m_delegate.fqinfo(fqcn,marker, message, t);
    }

    @Override
    public void fqwarn(String fqcn, PaxMarker marker, String message, Throwable t) {
        m_delegate.fqwarn(fqcn,marker, message, t);
    }

    @Override
    public void fqerror(String fqcn, PaxMarker marker, String message, Throwable t) {
        m_delegate.fqerror(fqcn,marker, message, t);
    }

    @Override
    public void fqfatal(String fqcn, PaxMarker marker, String message, Throwable t) {
        m_delegate.fqfatal(fqcn,marker, message, t);
    }

    @Override
    public int getPaxLogLevel() {
        return m_delegate.getPaxLogLevel();
    }

    @Override
    public LogLevel getLogLevel() {
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
