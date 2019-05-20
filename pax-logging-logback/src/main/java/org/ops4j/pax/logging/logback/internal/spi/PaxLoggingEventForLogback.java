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

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.CoreConstants;
import org.ops4j.pax.logging.spi.PaxLevel;
import org.ops4j.pax.logging.spi.PaxLocationInfo;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A facade to make a Logback event look like a Pax Logging event.
 * This is straightforward code, except for getFQNOfLoggerClass() which couldn't be implemented properly and
 * getThrowableStrRep() which requires stringifying the exception.
 *
 * @author Chris Dolan
 * @since 6/14/11 9:59 AM
 */
public class PaxLoggingEventForLogback implements PaxLoggingEvent {
    private final ILoggingEvent event;

    public PaxLoggingEventForLogback(ILoggingEvent event) {
        this.event = event;
    }

    public PaxLocationInfo getLocationInformation() {
        return new PaxLocationInfoForLogback(event.getCallerData());
    }

    public PaxLevel getLevel() {
        return new PaxLevelForLogback(event.getLevel());
    }

    public String getLoggerName() {
        return event.getLoggerName();
    }

    public String getMessage() {
        return event.getMessage();
    }

    public String getRenderedMessage() {
        return event.getFormattedMessage();
    }

    public String getThreadName() {
        return event.getThreadName();
    }

    public String[] getThrowableStrRep() {
        StringBuilder sb = new StringBuilder();
        for (IThrowableProxy tp = event.getThrowableProxy(); tp != null; tp = tp.getCause()) {
            ThrowableProxyUtil.subjoinFirstLine(sb, tp);
            sb.append(CoreConstants.LINE_SEPARATOR);
            ThrowableProxyUtil.subjoinSTEPArray(sb, ThrowableProxyUtil.REGULAR_EXCEPTION_INDENT, tp);
        }
        return sb.toString().split(CoreConstants.LINE_SEPARATOR);
    }

    public boolean locationInformationExists() {
        return event.hasCallerData();
    }

    public long getTimeStamp() {
        return event.getTimeStamp();
    }

    public String getFQNOfLoggerClass() {
        // This is only a guess... The actual FQCN is not available.
        return Logger.class.getName();
    }

    @Override
    public Map<String, Object> getProperties() {
        // a copy!
        return new HashMap<>(event.getMDCPropertyMap());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PaxLoggingEventForLogback that = (PaxLoggingEventForLogback) o;

        return Objects.equals(event, that.event);
    }

    @Override
    public int hashCode() {
        return event != null ? event.hashCode() : 0;
    }
}
