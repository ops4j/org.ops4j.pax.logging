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

import java.util.Map;

/**
 * <p>Framework library agnostic representation of <em>logging event</em> that may be filtered
 * and/or directed to an {@link PaxAppender appender}.</p>
 * <p>The methods are inspired by Log4J1 equivalents. Other frameworks don't necessarily have
 * all the information.</p>
 */
public interface PaxLoggingEvent {

    /**
     * Place where the logging event was created (e.g., {@code log.info("message")} was called).
     * @return
     */
    PaxLocationInfo getLocationInformation();

    /**
     * Severity/level/importance of the {@link PaxLevel logging event}.
     * @return
     */
    PaxLevel getLevel();

    /**
     * <em>Name</em> of the logger is its <em>category</em> (usually in dot-separated convention),
     * usually set in factory method of {@code logfactory.getLog("name")}.
     * @return
     */
    String getLoggerName();

    /**
     * <em>Fully qualified class name</em> (FQCN) helps identifying the <em>location</em>
     * of logging statement, by examining call trace to find where user code invoked logging method.
     * @return
     */
    String getFQNOfLoggerClass();

    /**
     * Actual message carried by given logging event.
     * @return
     */
    String getMessage();

    /**
     * When message is not a String, rendered message is Stringified version of
     * the message object.
     * @return
     */
    String getRenderedMessage();

    /**
     * Thread name associated with logging event. Usually related to thread that created the event
     * (called {@code logger.info()} method).
     * @return
     */
    String getThreadName();

    /**
     * Array of Strings representation of stack trace at the point where logging event was created.
     * @return
     */
    String[] getThrowableStrRep();

    /**
     * {@link #getLocationInformation()} may not always be available (e.g., when compiled without debug
     * information).
     * @return
     */
    boolean locationInformationExists();

    /**
     * Timestamp for the moment when the logging event was created.
     * @return
     */
    long getTimeStamp();

    /**
     * Properties associated with logging event - usually MDC.
     * @return
     */
    Map<String, Object> getProperties();

}
