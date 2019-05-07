/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ops4j.pax.logging;

import java.util.logging.LogManager;

public interface PaxLoggingConstants {

    /**
     * The Managed Service PID for logging configuration. Format is specific to chosen backend.
     */
    String LOGGING_CONFIGURATION_PID = "org.ops4j.pax.logging";

    /**
     * Each backend will register its service under these {@code objectClass} names.
     */
    String[] LOGGING_LOGSERVICE_NAMES = {
            org.osgi.service.log.LogService.class.getName(),
            org.knopflerfish.service.log.LogService.class.getName(),
            PaxLoggingService.class.getName(),
            "org.osgi.service.cm.ManagedService"
    };

    /**
     * Context property that specifies service ranking for {@link PaxLoggingService}
     */
    String LOGGING_CFG_LOGSERVICE_RANKING = "org.ops4j.pax.logging.ranking";

    /**
     * Context or system property to skip Java Util Logging bridge registration.
     */
    String LOGGING_CFG_SKIP_JUL = "org.ops4j.pax.logging.skipJUL";

    /**
     * Context or system property to skip {@link LogManager#reset()} invocation when bridging JUL to Pax Logging.
     */
    String LOGGING_CFG_SKIP_JUL_RESET = "org.ops4j.pax.logging.skipJULReset";

    /**
     * Context or system property that overrides the level at which framework/bundle/service events are logged
     * according to OSGi Compendium 101.6 "Mapping of Events".
     */
    String LOGGING_CFG_FRAMEWORK_EVENTS_LOG_LEVEL = "org.ops4j.pax.logging.service.frameworkEventsLogLevel";

    /**
     * Context or system property that specifies threshold for fallback logger used behind all facades
     * handled by pax-logging-api.
     */
    String LOGGING_CFG_DEFAULT_LOG_LEVEL = "org.ops4j.pax.logging.DefaultServiceLog.level";

    /**
     * Context or system property that selects buffering fallback logger.
     */
    String LOGGING_CFG_USE_BUFFERING_FALLBACK_LOGGER = "org.ops4j.pax.logging.useBufferingLogFallback";

    /**
     * {@code org.osp4j.pax.logging} PID property to specify max size for {@link org.osgi.service.log.LogReaderService}
     */
    String LOGGING_CFG_LOG_READER_SIZE_LEGACY = "pax.logging.entries.size";

    /**
     * {@code org.osp4j.pax.logging} PID property to specify max size for {@link org.osgi.service.log.LogReaderService}
     */
    String LOGGING_CFG_LOG_READER_SIZE = "org.ops4j.pax.logging.logReaderServiceSize";

}
