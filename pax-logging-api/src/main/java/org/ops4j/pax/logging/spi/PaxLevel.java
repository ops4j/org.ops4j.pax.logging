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
package org.ops4j.pax.logging.spi;

import org.osgi.service.log.LogLevel;

/**
 * Framework library agnostic representation of <em>logging level</em>. There are two uses of <em>level</em>
 * concept:
 * <ul>
 *     <li>detail level or <em>importance</em> of <em>logging event</em></li>
 *     <li>threshold of the logging service, appender or destination, that allows to process or reject logging events
 *     with some level</li>
 * </ul>
 *
 * Terms like <em>higher</em> or <em>lower</em> may be confusing at first glance. Each framework may use different
 * numerical levels than other. Syslog and {@link org.osgi.service.log.LogService} use higher numerical values for less
 * important logging events. Log4J1 and {@code java.util.logging} use higher numerical values for more important events.
 *
 * This interface is based on Log4J1 and:
 * <ul>
 *     <li>the higher value (numerically) the more important the event is (higher severity)</li>
 *     <li>{@code INFO} is <em>higher</em> than {@code DEBUG}</li>
 *     <li>when used as <em>threshold</em>, PaxLevel=INFO rejects events with level=DEBUG and level=TRACE.</li>
 *     <li>also, the higher the <em>threshold</em> the more events are rejected (less are processed).</li>
 * </ul>
 */
public interface PaxLevel {

    /**
     * Returns <code>true</code> if this level has a higher or equal level (is more important,
     * has bigger severity) than the level passed as argument, <code>false</code> otherwise.
     *
     * @param r the PaxLevel to compare with.
     * @return true if this level has a higher or equal level than the level passed as argument, <code>false</code> otherwise.
     */
    boolean isGreaterOrEqual(PaxLevel r);

    /**
     * Returns {@link LogLevel} representation of this level. In {@link LogLevel} enum, the higher the numerical
     * value (ordinal of the enum), the less <em>important</em> is the logging event with given level. When used as
     * <em>threshold</em>, the higher the numerical value, the less events are rejected (i.e., high <em>threshold</em>
     * means <em>process even less important events</em>).
     */
    LogLevel toLevel();

    /**
     * Return the syslog equivalent of this priority as an integer. In Syslog
     * (https://en.wikipedia.org/wiki/Syslog#Severity_level), higher numerical values indicate <em>less important</em>
     * logging events.
     *
     * @return the syslog equivalent of this priority as an integer.
     */
    int getSyslogEquivalent();

}
