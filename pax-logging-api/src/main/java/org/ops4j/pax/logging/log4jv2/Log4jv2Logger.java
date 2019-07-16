/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package org.ops4j.pax.logging.log4jv2;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.spi.AbstractLogger;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingManager;
import org.ops4j.pax.logging.PaxLoggingManagerAwareLogger;
import org.ops4j.pax.logging.PaxMarker;
import org.ops4j.pax.logging.spi.support.FallbackLogFactory;
import org.osgi.framework.FrameworkUtil;

/**
 * This is the default logger that is used when no suitable logging implementation is available.
 */
public class Log4jv2Logger extends AbstractLogger implements PaxLoggingManagerAwareLogger {

    static final String LOG4J_FQCN = Logger.class.getName();

    private String m_name;
    private volatile PaxLogger m_delegate;

    public Log4jv2Logger(String name, MessageFactory messageFactory, PaxLogger delegate) {
        super(name, messageFactory);
        m_name = name;
        m_delegate = delegate;
    }

    @Override
    public void setPaxLoggingManager(PaxLoggingManager paxLoggingManager) {
        if (paxLoggingManager == null) {
            m_delegate = FallbackLogFactory.createFallbackLog(FrameworkUtil.getBundle(Log4jv2Logger.class), m_name);
        } else {
            m_delegate = paxLoggingManager.getLogger(m_name, LOG4J_FQCN);
        }
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, Message message, Throwable t) {
        boolean mf = marker == null || markerDecision(level, marker);
        return getLevel().intLevel() >= level.intLevel() && mf;
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, Object message, Throwable t) {
        boolean mf = marker == null || markerDecision(level, marker);
        return getLevel().intLevel() >= level.intLevel() && mf;
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Throwable t) {
        boolean mf = marker == null || markerDecision(level, marker);
        return getLevel().intLevel() >= level.intLevel() && mf;
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message) {
        boolean mf = marker == null || markerDecision(level, marker);
        return getLevel().intLevel() >= level.intLevel() && mf;
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object... params) {
        boolean mf = marker == null || markerDecision(level, marker);
        return getLevel().intLevel() >= level.intLevel() && mf;
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, CharSequence message, Throwable t) {
        boolean mf = marker == null || markerDecision(level, marker);
        return getLevel().intLevel() >= level.intLevel() && mf;
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0) {
        boolean mf = marker == null || markerDecision(level, marker);
        return getLevel().intLevel() >= level.intLevel() && mf;
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1) {
        boolean mf = marker == null || markerDecision(level, marker);
        return getLevel().intLevel() >= level.intLevel() && mf;
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2) {
        boolean mf = marker == null || markerDecision(level, marker);
        return getLevel().intLevel() >= level.intLevel() && mf;
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3) {
        boolean mf = marker == null || markerDecision(level, marker);
        return getLevel().intLevel() >= level.intLevel() && mf;
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        boolean mf = marker == null || markerDecision(level, marker);
        return getLevel().intLevel() >= level.intLevel() && mf;
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        boolean mf = marker == null || markerDecision(level, marker);
        return getLevel().intLevel() >= level.intLevel() && mf;
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        boolean mf = marker == null || markerDecision(level, marker);
        return getLevel().intLevel() >= level.intLevel() && mf;
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
        boolean mf = marker == null || markerDecision(level, marker);
        return getLevel().intLevel() >= level.intLevel() && mf;
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {
        boolean mf = marker == null || markerDecision(level, marker);
        return getLevel().intLevel() >= level.intLevel() && mf;
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {
        boolean mf = marker == null || markerDecision(level, marker);
        return getLevel().intLevel() >= level.intLevel() && mf;
    }

    @Override
    public void logMessage(String fqcn, Level level, Marker marker, Message message, Throwable t) {
        if (marker != null) {
            PaxMarker paxMarker = new PaxMarker(marker);
            if (level.intLevel() >= Level.TRACE.intLevel()) {
                m_delegate.trace(paxMarker, message.getFormattedMessage(), t, fqcn);
            } else if (level.intLevel() >= Level.DEBUG.intLevel()) {
                m_delegate.debug(paxMarker, message.getFormattedMessage(), t, fqcn);
            } else if (level.intLevel() >= Level.INFO.intLevel()) {
                m_delegate.inform(paxMarker, message.getFormattedMessage(), t, fqcn);
            } else if (level.intLevel() >= Level.WARN.intLevel()) {
                m_delegate.warn(paxMarker, message.getFormattedMessage(), t, fqcn);
            } else if (level.intLevel() >= Level.ERROR.intLevel()) {
                m_delegate.error(paxMarker, message.getFormattedMessage(), t, fqcn);
            } else if (level.intLevel() >= Level.FATAL.intLevel()) {
                m_delegate.fatal(paxMarker, message.getFormattedMessage(), t, fqcn);
            }
        } else {
            if (level.intLevel() >= Level.TRACE.intLevel()) {
                m_delegate.trace(message.getFormattedMessage(), t, fqcn);
            } else if (level.intLevel() >= Level.DEBUG.intLevel()) {
                m_delegate.debug(message.getFormattedMessage(), t, fqcn);
            } else if (level.intLevel() >= Level.INFO.intLevel()) {
                m_delegate.inform(message.getFormattedMessage(), t, fqcn);
            } else if (level.intLevel() >= Level.WARN.intLevel()) {
                m_delegate.warn(message.getFormattedMessage(), t, fqcn);
            } else if (level.intLevel() >= Level.ERROR.intLevel()) {
                m_delegate.error(message.getFormattedMessage(), t, fqcn);
            } else if (level.intLevel() >= Level.FATAL.intLevel()) {
                m_delegate.fatal(message.getFormattedMessage(), t, fqcn);
            }
        }
    }

    @Override
    public Level getLevel() {
        switch (m_delegate.getLogLevel()) {
            case PaxLogger.LEVEL_TRACE:
                return Level.TRACE;
            case PaxLogger.LEVEL_DEBUG:
                return Level.DEBUG;
            case PaxLogger.LEVEL_INFO:
                return Level.INFO;
            case PaxLogger.LEVEL_WARNING:
                return Level.WARN;
            case PaxLogger.LEVEL_ERROR:
                return Level.ERROR;
            default:
                return Level.OFF;
        }
    }

    private boolean markerDecision(Level level, Marker marker) {
        PaxMarker m = new PaxMarker(marker);
        if (level.intLevel() >= Level.TRACE.intLevel()) {
            return m_delegate.isTraceEnabled(m);
        } else if (level.intLevel() >= Level.DEBUG.intLevel()) {
            return m_delegate.isDebugEnabled(m);
        } else if (level.intLevel() >= Level.INFO.intLevel()) {
            return m_delegate.isInfoEnabled(m);
        } else if (level.intLevel() >= Level.WARN.intLevel()) {
            return m_delegate.isWarnEnabled(m);
        } else if (level.intLevel() >= Level.ERROR.intLevel()) {
            return m_delegate.isErrorEnabled(m);
        } else if (level.intLevel() >= Level.FATAL.intLevel()) {
            return m_delegate.isFatalEnabled(m);
        }
        return false;
    }

}
