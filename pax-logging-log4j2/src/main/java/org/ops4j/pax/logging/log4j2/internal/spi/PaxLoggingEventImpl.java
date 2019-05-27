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
package org.ops4j.pax.logging.log4j2.internal.spi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
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
    public Map<String, Object> getProperties() {
        // a copy!
        return event.getContextData() == null ? Collections.emptyMap() : new HashMap<>(event.getContextData().toMap());
    }

}
