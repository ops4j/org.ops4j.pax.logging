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
package org.ops4j.pax.logging.log4j1.internal.bridges;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.ops4j.pax.logging.log4j1.internal.spi.PaxLoggingEventImpl;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;
import org.ops4j.pax.logging.spi.support.PaxAppenderProxy;

/**
 * Bridge from Log4J to pax-logging. Event is passed to tracked (through {@link PaxAppenderProxy})
 * {@link org.ops4j.pax.logging.spi.PaxAppender} OSGi services.
 */
public class AppenderBridgeImpl extends AppenderSkeleton implements Appender {

    private PaxAppenderProxy m_delegate;

    public AppenderBridgeImpl(PaxAppenderProxy delegate) {
        m_delegate = delegate;
    }

    /**
     * PAXLOGGING-182 - method doesn't have to be synchronized
     * @param event
     */
    @Override
    public void doAppend(LoggingEvent event) {
        if (closed) {
            LogLog.error("Attempted to append to closed appender named [" + name + "].");
            return;
        }

        if (!isAsSevereAsThreshold(event.getLevel())) {
            return;
        }

        Filter f = this.headFilter;

        FILTER_LOOP:
        while (f != null) {
            switch (f.decide(event)) {
                case Filter.DENY:
                    return;
                case Filter.ACCEPT:
                    break FILTER_LOOP;
                case Filter.NEUTRAL:
                    f = f.getNext();
            }
        }

        this.append(event);
    }

    @Override
    protected void append(LoggingEvent event) {
        PaxLoggingEvent paxEvent = new PaxLoggingEventImpl(event);
        m_delegate.doAppend(paxEvent);
    }

    @Override
    public void close() {
        m_delegate.close();
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
}
