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

/**
 * Framework library agnostic representation of a filter that may decide
 * whether to process (pass) logging event or not.
 */
public interface PaxFilter {

    /**
     * The log event must be dropped immediately without consulting
     * with the remaining filters, if any, in the chain.
     */
    int DENY = -1;

    /**
     * This filter is neutral with respect to the log event. The
     * remaining filters, if any, should be consulted for a final decision.
     */
    int NEUTRAL = 0;

    /**
     * The log event must be logged immediately without consulting with
     * the remaining filters, if any, in the chain.
     */
    int ACCEPT = 1;

    /**
     * <p>If the decision is <code>DENY</code>, then the event will be
     * dropped. If the decision is <code>NEUTRAL</code>, then the next
     * filter, if any, will be invoked. If the decision is ACCEPT then
     * the event will be logged without consulting with other filters in
     * the chain.
     *
     * @param event The LoggingPaxLoggingEventEvent to decide upon.
     * @return decision The decision of the filter.
     */
    int doFilter(PaxLoggingEvent event);

}
