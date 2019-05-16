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
package org.ops4j.pax.logging.service.internal.spi;

import org.apache.log4j.Level;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.spi.PaxLevel;

/**
 * Log4J1 delegated {@link PaxLevel}. It's the easiest implementation, because {@link PaxLevel}
 * was inspired by Log4J1 itself.
 */
public class PaxLevelImpl implements PaxLevel {

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

    @Override
    public int getSyslogEquivalent() {
        return m_delegate.getSyslogEquivalent();
    }

    public String toString() {
        return m_delegate.toString();
    }

}
