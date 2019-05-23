/*
 * Copyright 2014 Guillaume Nodet.
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

import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.spi.PaxLevel;
import org.ops4j.pax.logging.spi.PaxLocationInfo;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;

public class PaxLoggingEventImpl implements PaxLoggingEvent {

    static {
        // Force the two classes to be loaded in case the bundle is refreshed
        new PaxLocationInfoImpl(null);
        new PaxLevelImpl(Level.DEBUG);
    }

    private final LogEvent event;

    public PaxLoggingEventImpl(LogEvent event) {
        this.event = event;
    }

    @Override
    public PaxLocationInfo getLocationInformation() {
        return new PaxLocationInfoImpl(event.getSource());
    }

    @Override
    public PaxLevel getLevel() {
        return new PaxLevelImpl(event.getLevel());
    }

    @Override
    public String getLoggerName() {
        return event.getLoggerName();
    }

    @Override
    public String getMessage() {
        return event.getMessage().getFormattedMessage();
    }

    @Override
    public String getRenderedMessage() {
        return event.getMessage().getFormattedMessage();
    }

    @Override
    public String getThreadName() {
        return event.getThreadName();
    }

    @Override
    public String[] getThrowableStrRep() {
        ThrowableProxy t = event.getThrownProxy();
        return t != null ? t.getExtendedStackTraceAsString("").split("\n") : null;
    }

    @Override
    public boolean locationInformationExists() {
        return event.isIncludeLocation();
    }

    @Override
    public long getTimeStamp() {
        return event.getTimeMillis();
    }

    @Override
    public String getFQNOfLoggerClass() {
        return event.getLoggerFqcn();
    }

    @Override
    public Map getProperties() {
        return event.getContextMap();
    }

    static class PaxLocationInfoImpl implements PaxLocationInfo {

        private final StackTraceElement source;

        public PaxLocationInfoImpl(StackTraceElement source) {
            this.source = source;
        }

        @Override
        public String getFileName() {
            String s = source != null ? source.getFileName() : null;
            return s != null ? s : "?";
        }

        @Override
        public String getClassName() {
            String s = source != null ? source.getClassName() : null;
            return s != null ? s : "?";
        }

        @Override
        public String getLineNumber() {
            return source != null ? Integer.toString(source.getLineNumber()) : "?";
        }

        @Override
        public String getMethodName() {
            String s = source != null ? source.getMethodName() : null;
            return s != null ? s : "?";
        }
    }

    static class PaxLevelImpl implements PaxLevel {

        // this data comes from the log4j level class
        public static final int SYSLOG_DEBUG = 7;
        public static final int SYSLOG_INFO = 6;
        public static final int SYSLOG_WARN = 4;
        public static final int SYSLOG_ERROR = 3;
        public static final int SYSLOG_OFF = 0;

        private final Level level;

        public PaxLevelImpl(Level level) {
            this.level = level;
        }

        @Override
        public boolean isGreaterOrEqual(PaxLevel r) {
            return getSyslogEquivalent() <= r.getSyslogEquivalent();
        }

        @Override
        public int toInt() {
            int lvl = level.intLevel();
            if (lvl <= Level.ERROR.intLevel()) {
                return PaxLogger.LEVEL_ERROR;
            }
            if (lvl <= Level.WARN.intLevel()) {
                return PaxLogger.LEVEL_WARNING;
            }
            if (lvl <= Level.INFO.intLevel()) {
                return PaxLogger.LEVEL_INFO;
            }
            if (lvl <= Level.DEBUG.intLevel()) {
                return PaxLogger.LEVEL_DEBUG;
            }
            return PaxLogger.LEVEL_TRACE;
        }

        @Override
        public int getSyslogEquivalent() {
            int lvl = level.intLevel();
            if (lvl <= Level.OFF.intLevel()) {
                return SYSLOG_OFF;
            }
            if (lvl <= Level.ERROR.intLevel()) {
                return SYSLOG_ERROR;
            }
            if (lvl <= Level.WARN.intLevel()) {
                return SYSLOG_WARN;
            }
            if (lvl <= Level.INFO.intLevel()) {
                return SYSLOG_INFO;
            }
            return SYSLOG_DEBUG;
        }

        @Override
        public String toString() {
            return level.toString();
        }
    }
}
