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
import org.osgi.service.log.LogLevel;
import org.osgi.service.log.LogService;

/**
 * This interface extends {@link org.osgi.service.log.LogService}.
 * It should be implemented by specific logging provider (Log4j, Logback, ...).
 *
 * It's role is to provide methods to obtain {@link PaxLogger} and {@link PaxContext} that are specific to
 * one of pax-logging-* implementations.
 *
 * Since OSGi
 */
public interface PaxLoggingService extends LogService {

    /**
     * Obtains {@link PaxLogger} instance - implementation-specific logger hidden under {@link PaxLogger} interface.
     * This method is not used directly, but rather through {@link org.ops4j.pax.logging.PaxLoggingManager}
     *
     * Since R7 (Pax Logging 2.0.0), similar methods to obtain a logger come directly from {@link org.osgi.service.log.LoggerFactory}
     * interface. Some of these methods may configure returned logger to use printf or Slf4J style of formatting. This
     * methods returns loggers that use Slf4J formatting.
     *
     * This method should be called by framework-specific facades (like {@code org.ops4j.pax.logging.slf4j.Slf4jLogger})
     * which pass proper {@code fqcn}.
     */
    PaxLogger getLogger(Bundle bundle, String category, String fqcn);

    /**
     * Returns R7 {@link LogLevel} (actually, a threahold) associated with entire logging service. Usually individual
     * loggers may have different levels specified.
     *
     * @since 2.0.0
     */
    LogLevel getLogLevel();

    /**
     * Returns {@link PaxContext} of this service that gives access to thread-bound MDC context.
     * @return
     */
    PaxContext getPaxContext();

}
