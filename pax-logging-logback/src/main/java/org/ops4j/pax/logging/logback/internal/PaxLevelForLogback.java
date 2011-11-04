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
import org.ops4j.pax.logging.spi.PaxLevel;

/**
 * A straightforward facade to make a Logback Level instance look like a PaxLevel.
 * @author Chris Dolan
 */
public class PaxLevelForLogback implements PaxLevel {
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
        return m_delegate.toInt();
    }

    public int getSyslogEquivalent() {
        // this data comes from the log4j level class
        if (m_delegate == Level.TRACE)
            return 7;
        if (m_delegate == Level.DEBUG)
            return 7;
        if (m_delegate == Level.INFO)
            return 6;
        if (m_delegate == Level.WARN)
            return 4;
        if (m_delegate == Level.ERROR)
            return 3;
        if (m_delegate == Level.OFF)
            return 0;
        //noinspection IfStatementWithIdenticalBranches
        if (m_delegate == Level.ALL)
            return 7;
        return 7; // fallback case...
    }

    @Override
    public String toString() {
        return m_delegate.toString();
    }
}
