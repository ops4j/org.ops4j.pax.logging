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
 * While {@link PaxLoggingService} represents implementation-specific logging service, this interface
 * acts as a bridge between logging API specific implementation (like SLF4J LoggerFactory) and
 * actual implementation of {@link PaxLoggingService}. When given service is gone, Logging switches immediately
 * to non-dynamic, fallback implementation of {@link PaxLoggingService}.
 */
public interface PaxLoggingManager {

    /**
     * Obtains a {@link PaxLogger} from this manager. Implementation delegates to {@link PaxLoggingService} or
     * to fallback logger provider.
     * @param category
     * @param fqcn
     * @return
     */
    PaxLogger getLogger(String category, String fqcn);

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
     * Returns {@link Bundle} associated with this manager. Normally it's bundle of pax-logging-api.
     * @return
     */
    Bundle getBundle();

}
