/*  Copyright 2009 Guillaume Nodet.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.logging;

import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogLevel;

/**
 * Interface to implement by logging framework specific provider, to pass logging events to Event Admin
 * according to OSGi Compendium "101.6.4. Log Events"
 */
public interface EventAdminPoster extends AutoCloseable {

    /**
     * Sends an event to EventAdmin (if available)
     * @param bundle
     * @param level
     * @param entry
     * @param message
     * @param exception
     * @param sr
     * @param context
     */
    void postEvent(Bundle bundle, LogLevel level, LogEntry entry, String message,
                   Throwable exception, ServiceReference<?> sr, Map<String, ?> context);

    /**
     * Stops the poster.
     */
    void destroy();

}
