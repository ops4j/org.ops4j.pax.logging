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
package org.ops4j.pax.logging;

import org.osgi.framework.Bundle;

/**
 * <p>While {@link PaxLoggingService} represents implementation-specific logging service, this interface
 * acts as a bridge between logging API specific implementation (like SLF4J LoggerFactory) and
 * actual implementation of {@link PaxLoggingService}. When given service is gone, Logging switches immediately
 * to non-dynamic, fallback implementation of {@link PaxLoggingService}.
 *
 * <p>{@code getLogger()} methods in this interface are generic, but low level methods that use 3 parameters to obtain
 * a logger:<ul>
 *     <li>bundle - to associate {@link PaxLogger logger} with a {@link Bundle bundle}</li>
 *     <li>category - to name a logger using well known, usually dot-separated, convention</li>
 *     <li>fqcn - fully qualified class name, which is not the same as category. It generally should be used to mark
 *     a class name in {@link StackTraceElement} of stack trace where application code enters logging infrastructure.
 *     This is used for example by Log4J1 to discover <em>a location</em> - class name, method name, file name
 *     and line number when pattern contains {@code %C} or {@code %F}.</li>
 * </ul>
 */
public interface PaxLoggingManager {

    /**
     * Obtains a {@link PaxLogger} from this manager. Implementation delegates to {@link PaxLoggingService} or
     * to fallback logger provider.
     *
     * This is the main method called inside any facade/bridge method (like SLF4J's {@code LoggerFactory.getLogger()}).
     *
     * {@code fqcn} parameter will be part of the returned {@link PaxLogger} to determine the <em>location</em>
     * where logging method is invoked (by analyzing stack/class trace).
     *
     * Each {@link PaxLogger} has associated {@link Bundle}, but {@code fqcn} <strong>won't be</strong> used
     * to determine the bundle. Bundle is determined statically when obtaining the {@link PaxLogger logger}
     * as first bundle that's not pax-logging-api and represents a bundle that created the logger - not a place where
     * this logger is used to log messages.
     *
     * Note that for java.util.logging, the logger is not directly obtained by "client" code, but rather
     * in pax-logging specific {@link java.util.logging.Handler}.
     *
     * @param category just name of the logger
     * @param fqcn fully qualified name for pax-logging-specific factory-like class to make it easy to mark where
     * (in the stack trace) user code calls logging code. It doesn't always make sense - mainly in dynamic scenarios
     * where logging is invoked via {@link org.osgi.service.log.LogService} and not through concrete <em>logger</em>
     * object
     * @return
     */
    PaxLogger getLogger(String category, String fqcn);

    /**
     * Obtains a {@link PaxLogger} from this manager for a specific {@link Bundle}. Implementation delegates
     * to {@link PaxLoggingService} or to fallback logger provider.
     *
     * {@code fqcn} parameter will be part of the returned {@link PaxLogger} to determine the <em>location</em>
     * where logging method is invoked (by analyzing stack/class trace).
     *
     * This method passes a {@link Bundle} to associate with returned {@link PaxLogger}.
     *
     * Note that for java.util.logging, the logger is not directly obtained by "client" code, but rather
     * in pax-logging specific {@link java.util.logging.Handler}.
     *
     * @param bundle {@link Bundle} associated with returned {@link PaxLogger}
     * @param category just name of the logger
     * @param fqcn fully qualified name for pax-logging-specific factory-like class to make it easy to mark where
     * (in the stack trace) user code calls logging code. It doesn't always make sense - mainly in dynamic scenarios
     * where logging is invoked via {@link org.osgi.service.log.LogService} and not through concrete <em>logger</em>
     * object
     * @return
     */
    PaxLogger getLogger(Bundle bundle, String category, String fqcn);

    /**
     * Returns actual, detected, dynamic {@link PaxLoggingService} that's currently used to obtain
     * {@link PaxLogger loggers}.
     * @return
     */
    PaxLoggingService getPaxLoggingService();

    /**
     * Closes {@link PaxLoggingService} service tracker in this manager.
     */
    void close();

    /**
     * Stops using associated {@link PaxLoggingService} reference.
     */
    void dispose();

    /**
     * Returns {@link Bundle} associated with this manager. Normally it's pax-logging-api bundle.
     * @return
     */
    Bundle getBundle();

}
