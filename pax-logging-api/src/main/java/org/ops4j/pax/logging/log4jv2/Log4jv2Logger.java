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
import org.ops4j.pax.logging.spi.support.FallbackLogFactory;
import org.osgi.framework.FrameworkUtil;

/**
 * This is the default logger that is used when no suitable logging implementation is available.
 */
public class Log4jv2Logger extends AbstractLogger {

    private static final String LOG4J_FQCN = Logger.class.getName();

    private volatile PaxLogger m_delegate;

    public Log4jv2Logger(String name, MessageFactory messageFactory, PaxLoggingManager paxLogging) {
        super(name, messageFactory);
        setPaxLoggingManager(paxLogging);
    }

    public void setPaxLoggingManager(PaxLoggingManager paxLoggingManager) {
        if (paxLoggingManager != null) {
            m_delegate = paxLoggingManager.getLogger(getName(), LOG4J_FQCN);
        } else {
            m_delegate = FallbackLogFactory.createFallbackLog( FrameworkUtil.getBundle(Log4jv2Logger.class), getName() );
        }
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, Message message, Throwable t) {
        return getLevel().intLevel() >= level.intLevel();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, Object message, Throwable t) {
        return getLevel().intLevel() >= level.intLevel();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Throwable t) {
        return getLevel().intLevel() >= level.intLevel();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message) {
        return getLevel().intLevel() >= level.intLevel();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object... params) {
        return getLevel().intLevel() >= level.intLevel();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, CharSequence message, Throwable t) {
        return getLevel().intLevel() >= level.intLevel();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0) {
        return getLevel().intLevel() >= level.intLevel();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1) {
        return getLevel().intLevel() >= level.intLevel();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2) {
        return getLevel().intLevel() >= level.intLevel();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3) {
        return getLevel().intLevel() >= level.intLevel();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        return getLevel().intLevel() >= level.intLevel();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        return getLevel().intLevel() >= level.intLevel();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        return getLevel().intLevel() >= level.intLevel();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
        return getLevel().intLevel() >= level.intLevel();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {
        return getLevel().intLevel() >= level.intLevel();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {
        return getLevel().intLevel() >= level.intLevel();
    }

    @Override
    public void logMessage(String fqcn, Level level, Marker marker, Message message, Throwable t) {
        // TODO: support marker
        if (level.intLevel() >= Level.TRACE.intLevel()) {
            m_delegate.trace(message.getFormattedMessage(), t, fqcn);
        }
        else if (level.intLevel() >= Level.DEBUG.intLevel()) {
            m_delegate.debug(message.getFormattedMessage(), t, fqcn);
        }
        else if (level.intLevel() >= Level.INFO.intLevel()) {
            m_delegate.inform(message.getFormattedMessage(), t, fqcn);
        }
        else if (level.intLevel() >= Level.WARN.intLevel()) {
            m_delegate.warn(message.getFormattedMessage(), t, fqcn);
        }
        else if (level.intLevel() >= Level.ERROR.intLevel()) {
            m_delegate.error(message.getFormattedMessage(), t, fqcn);
        }
        else if (level.intLevel() >= Level.FATAL.intLevel()) {
            m_delegate.fatal(message.getFormattedMessage(), t, fqcn);
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

}
