/*
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
package org.ops4j.pax.logging.logback.internal.spi;

import ch.qos.logback.classic.Level;
import org.ops4j.pax.logging.spi.PaxLevel;
import org.osgi.service.log.LogLevel;

/**
 * <p>A straightforward facade to make a Logback Level instance look like a PaxLevel.</p>
 * <p>Logback doesn't refer to Syslog like Log4J1, so we have to do it ourselves.</p>
 * @author Chris Dolan
 */
public class PaxLevelImpl implements PaxLevel {

    // this data comes from the Log4J1 org.apache.log4j.Level class
    public static final int SYSLOG_ALL = 7;
    public static final int SYSLOG_TRACE = 7;
    public static final int SYSLOG_DEBUG = 7;
    public static final int SYSLOG_INFO = 6;
    public static final int SYSLOG_WARN = 4;
    public static final int SYSLOG_ERROR = 3;
    public static final int SYSLOG_FATAL = 0;
    public static final int SYSLOG_OFF = 0;

    private Level m_delegate;

    public PaxLevelImpl(Level delegate) {
        m_delegate = delegate;
    }

    @Override
    public boolean isGreaterOrEqual(PaxLevel r) {
        if (r instanceof PaxLevelImpl) {
            PaxLevelImpl impl = (PaxLevelImpl) r;
            return m_delegate.isGreaterOrEqual(impl.m_delegate);
        } else {
            // fallback case: the syslog numbers are portable
            return getSyslogEquivalent() <= r.getSyslogEquivalent();
        }
    }

    @Override
    public LogLevel toLevel() {
        // see integer values in ch.qos.logback.classic.Level
        if (m_delegate.isGreaterOrEqual(Level.ERROR))
            return LogLevel.ERROR;
        if (m_delegate.isGreaterOrEqual(Level.WARN))
            return LogLevel.WARN;
        if (m_delegate.isGreaterOrEqual(Level.INFO))
            return LogLevel.INFO;
        if (m_delegate.isGreaterOrEqual(Level.DEBUG))
            return LogLevel.DEBUG;
        return LogLevel.TRACE;
    }

    @Override
    public int getSyslogEquivalent() {
        if (m_delegate == Level.TRACE)
            return SYSLOG_DEBUG;
        if (m_delegate == Level.DEBUG)
            return SYSLOG_DEBUG;
        if (m_delegate == Level.INFO)
            return SYSLOG_INFO;
        if (m_delegate == Level.WARN)
            return SYSLOG_WARN;
        if (m_delegate == Level.ERROR)
            return SYSLOG_ERROR;
        if (m_delegate == Level.OFF)
            return SYSLOG_OFF;
        if (m_delegate == Level.ALL)
            return SYSLOG_TRACE;
        return SYSLOG_DEBUG; // fallback case...
    }

    @Override
    public String toString() {
        return m_delegate.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PaxLevelImpl that = (PaxLevelImpl) o;

        if (!m_delegate.equals(that.m_delegate)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return m_delegate.hashCode();
    }
}
