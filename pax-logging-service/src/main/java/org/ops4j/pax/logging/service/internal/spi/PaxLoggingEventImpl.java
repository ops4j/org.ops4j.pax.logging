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

import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.ops4j.pax.logging.spi.PaxLevel;
import org.ops4j.pax.logging.spi.PaxLocationInfo;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;

// Contributors:   Nelson Minar <nelson@monkey.org>
//                 Wolf Siberski
//                 Anders Kristensen <akristensen@dynamicsoft.com>

/**
 * <p>The internal representation of logging events. When an affirmative
 * decision is made to log then a <code>LoggingEvent</code> instance
 * is created. This instance is passed around to the different log4j
 * components.</p>
 *
 * <p>This class is of concern to those wishing to extend log4j.</p>
 *
 * <p>Log4J1 delegated {@link PaxLoggingEvent}. It's the easiest implementation, because {@link PaxLoggingEvent}
 * was inspired by Log4J1 itself.</p>
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author James P. Cakalic
 * @author Niclas Hedhman
 * @since 0.8.2
 */
public class PaxLoggingEventImpl implements java.io.Serializable, PaxLoggingEvent {

    private final LoggingEvent m_delegate;

    public PaxLoggingEventImpl(LoggingEvent event) {
        m_delegate = event;
    }

    @Override
    public PaxLocationInfo getLocationInformation() {
        LocationInfo info = m_delegate.getLocationInformation();
        return new PaxLocationInfoImpl(info);
    }

    @Override
    public PaxLevel getLevel() {
        Level level = m_delegate.getLevel();
        return new PaxLevelImpl(level);
    }

    @Override
    public String getLoggerName() {
        return m_delegate.getLoggerName();
    }

    @Override
    public String getMessage() {
        return (String) m_delegate.getMessage();
    }

    @Override
    public String getRenderedMessage() {
        return m_delegate.getRenderedMessage();
    }

    @Override
    public String getThreadName() {
        return m_delegate.getThreadName();
    }

    @Override
    public String[] getThrowableStrRep() {
        return m_delegate.getThrowableStrRep();
    }

    @Override
    public final boolean locationInformationExists() {
        return m_delegate.locationInformationExists();
    }

    @Override
    public final long getTimeStamp() {
        return m_delegate.getTimeStamp();
    }

    @Override
    public String getFQNOfLoggerClass() {
        return m_delegate.getFQNOfLoggerClass();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getProperties() {
        return (Map<String, Object>) m_delegate.getProperties();
    }
}
