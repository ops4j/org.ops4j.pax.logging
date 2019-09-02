/*
 * Copyright 2005 Niclas Hedhman.
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
 * <p>This interface extends Knopflerfish' LogService which extends original {@link org.osgi.service.log.LogService}.
 * It should be implemented by specific logging provider (Log4j, Logback, ...).</p>
 * <p>It's role is to provide methods to obtain {@link PaxLogger} and {@link PaxContext} that are specific to
 * one of pax-logging-* implementations.</p>
 */
public interface PaxLoggingService extends LogService {

    /**
     * Obtains {@link PaxLogger} instance - implementation-specific logger hidden under {@link PaxLogger} interface.
     * This method is not used directly, but rather through {@link org.ops4j.pax.logging.PaxLoggingManager}
     * @param bundle
     * @param category
     * @param fqcn
     * @return
     */
    PaxLogger getLogger(Bundle bundle, String category, String fqcn);

    /**
     * <p>Returns log level (actually, a threahold) associated with entire logging service. Usually individual
     * loggers may have different levels specified.</p>
     * @return
     */
    int getLogLevel();

    /**
     * Returns {@link PaxContext} of this service that gives access to thread-bound MDC context.
     * @return
     */
    PaxContext getPaxContext();

}
