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
package org.ops4j.pax.logging.logback.internal;

import ch.qos.logback.classic.Level;

import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.spi.PaxLevel;

/**
 * A straightforward facade to make a Logback Level instance look like a PaxLevel.
 * @author Chris Dolan
 */
public class PaxLevelForLogback implements PaxLevel {
    // this data comes from the log4j level class
    public static final int SYSLOG_DEBUG = 7;
    public static final int SYSLOG_INFO = 6;
    public static final int SYSLOG_WARN = 4;
    public static final int SYSLOG_ERROR = 3;
    public static final int SYSLOG_OFF = 0;

    private Level m_delegate;

    public PaxLevelForLogback(Level delegate) {
        m_delegate = delegate;
    }

    public boolean isGreaterOrEqual( PaxLevel r ) {
        if (r instanceof PaxLevelForLogback) {
            return m_delegate.isGreaterOrEqual( ((PaxLevelForLogback) r).m_delegate );
        } else {
            // fallback case: the syslog numbers are portable
            return getSyslogEquivalent() <= r.getSyslogEquivalent();
        }
    }

    public int toInt() {
    	if (m_delegate.isGreaterOrEqual(Level.ERROR))
    		return PaxLogger.LEVEL_ERROR;
    	if (m_delegate.isGreaterOrEqual(Level.WARN))
    		return PaxLogger.LEVEL_WARNING;
    	if (m_delegate.isGreaterOrEqual(Level.INFO))
    		return PaxLogger.LEVEL_INFO;
    	if (m_delegate.isGreaterOrEqual(Level.DEBUG))
    		return PaxLogger.LEVEL_DEBUG;
    	return PaxLogger.LEVEL_TRACE;
    }

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
        //noinspection IfStatementWithIdenticalBranches
        if (m_delegate == Level.ALL)
            return SYSLOG_DEBUG;
        return SYSLOG_DEBUG; // fallback case...
    }

    @Override
    public String toString() {
        return m_delegate.toString();
    }
}
