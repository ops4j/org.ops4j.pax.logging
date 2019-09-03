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

/**
 * <p>Different constants used across Pax Logging.</p>
 * <p>Constants names use the following prefixes:<ul>
 *     <li>{@code LOGGING_CFG_} - for system or context property names</li>
 *     <li>{@code PID_CFG_} - for property names found in {@code org.ops4j.pax.logging} PID</li>
 *     <li>{@code SERVICE_PROPERTY_} - for names of OSGi service properties</li>
 * </ul></p>
 */
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
            org.osgi.service.log.LoggerFactory.class.getName(),
            PaxLoggingService.class.getName(),
            "org.osgi.service.cm.ManagedService"
    };

    /**
     * System or context (in that order) property that specifies service ranking for {@link PaxLoggingService}
     */
    String LOGGING_CFG_LOGSERVICE_RANKING = "org.ops4j.pax.logging.ranking";

    /**
     * System or context (in that order) property to skip Java Util Logging bridge registration.
     */
    String LOGGING_CFG_SKIP_JUL = "org.ops4j.pax.logging.skipJUL";

    /**
     * System or context (in that order) property to skip {@link LogManager#reset()} invocation when bridging JUL
     * to Pax Logging.
     */
    String LOGGING_CFG_SKIP_JUL_RESET = "org.ops4j.pax.logging.skipJULReset";

    /**
     * System or context (in that order) property that overrides the level at which framework/bundle/service events
     * are logged
     * according to OSGi Compendium 101.6 "Mapping of Events".
     */
    String LOGGING_CFG_FRAMEWORK_EVENTS_LOG_LEVEL = "org.ops4j.pax.logging.service.frameworkEventsLogLevel";

    /**
     * System or context (in that order) property that specifies threshold for fallback logger used behind all
     * facades
     * handled by pax-logging-api.
     */
    String LOGGING_CFG_DEFAULT_LOG_LEVEL = "org.ops4j.pax.logging.DefaultServiceLog.level";

    /**
     * System or context (in that order) property that selects buffering fallback logger.
     */
    String LOGGING_CFG_USE_BUFFERING_FALLBACK_LOGGER = "org.ops4j.pax.logging.useBufferingLogFallback";

    /**
     * System or context (in that order) property that selects file-based fallback logger. The value should be
     * writable filename. Turning on this logger will enable synchronization and register singleton <em>stream</em>
     * used by all instances of {@link org.ops4j.pax.logging.spi.support.FileServiceLog}.
     */
    String LOGGING_CFG_USE_FILE_FALLBACK_LOGGER = "org.ops4j.pax.logging.useFileLogFallback";

    /**
     * {@code org.osp4j.pax.logging} PID property to specify max size for
     * {@link org.osgi.service.log.LogReaderService}
     */
    String PID_CFG_LOG_READER_SIZE_LEGACY = "pax.logging.entries.size";

    /**
     * {@code org.osp4j.pax.logging} PID property to specify max size for
     * {@link org.osgi.service.log.LogReaderService}
     */
    String PID_CFG_LOG_READER_SIZE = "org.ops4j.pax.logging.logReaderServiceSize";

    /**
     * Service property to filter {@link org.ops4j.pax.logging.spi.PaxAppender} OSGi services
     */
    String SERVICE_PROPERTY_APPENDER_NAME_PROPERTY = "org.ops4j.pax.logging.appender.name";

    /**
     * Service property to filter {@link org.ops4j.pax.logging.spi.PaxLayout} OSGi services
     */
    String SERVICE_PROPERTY_LAYOUT_NAME_PROPERTY = "org.ops4j.pax.logging.layout.name";

    /**
     * Service property to filter {@link org.ops4j.pax.logging.spi.PaxFilter} OSGi services
     */
    String SERVICE_PROPERTY_FILTER_NAME_PROPERTY = "org.ops4j.pax.logging.filter.name";

    /**
     * Service property to filter {@link org.ops4j.pax.logging.spi.PaxErrorHandler} OSGi services
     */
    String SERVICE_PROPERTY_ERRORHANDLER_NAME_PROPERTY = "org.ops4j.pax.logging.errorhandler.name";

    /**
     * EventAdmin topic name to publish events related to (re)configuration of logging backend.
     */
    String EVENT_ADMIN_CONFIGURATION_TOPIC = "org/ops4j/pax/logging/Configuration";

    /**
     * Context property that switches from private {@code ch.qos.logback.classic.LoggerContext} to one
     * managed by {@code org.slf4j.impl.StaticLoggerBinder#defaultLoggerContext} (where {@code StaticLoggerBinder}
     * is the one available in logback-classic).
     */
    String LOGGING_CFG_LOGBACK_USE_STATIC_CONTEXT = "org.ops4j.pax.logging.StaticLogbackContext";

    /**
     * System or context (in that order) property that sets Logback configuration file (not generic URI - just a path).
     * See https://logback.qos.ch/manual/configuration.html
     */
    String LOGGING_CFG_LOGBACK_CONFIGURATION_FILE = "org.ops4j.pax.logging.StaticLogbackFile";

    /**
     * <p>System or context property that can indicate alternative file with properties used instead of
     * the properties from Configuration Admin. Useful in tests probably.</p>
     * <p>Currently this is handled only by pax-logging-log4j2.</p>
     */
    String LOGGING_CFG_PROPERTY_FILE = "org.ops4j.pax.logging.property.file";

    /**
     * <p>System or context property that can indicate whether to throw exceptions from log4j2 configuration
     * methods when ERROR occurs. Normally ERRORs are only printed to org.apache.logging.log4j.status.StatusLogger
     * so by default this value is {@code false}.</p>
     */
    String LOGGING_CFG_LOG4J2_ERRORS_AS_EXCEPTION = "org.ops4j.pax.logging.log4j2.errorsAsExceptions";

    /**
     * {@code org.osp4j.pax.logging} PID property to specify XML file with Logback configuration
     */
    String PID_CFG_LOGBACK_CONFIG_FILE = "org.ops4j.pax.logging.logback.config.file";

    /**
     * {@code org.osp4j.pax.logging} PID property to specify XML file with Log4J2 configuration
     */
    String PID_CFG_LOG4J2_CONFIG_FILE = "org.ops4j.pax.logging.log4j2.config.file";

    /**
     * {@code org.osp4j.pax.logging} PID property to specify whether async context should be used. Used both
     * for case where properties are taken directly from PID configuration or when
     * {@link #PID_CFG_LOG4J2_CONFIG_FILE} is used.
     */
    String PID_CFG_LOG4J2_ASYNC = "org.ops4j.pax.logging.log4j2.async";

    /**
     * {@code org.osp4j.pax.logging} PID property to specify whether to use locks during (re)configuration.
     * By default, log operations <strong>use locks</strong> to prevent reconfiguration during actual logging. But
     * there are special cases (see: https://ops4j1.jira.com/browse/PAXLOGGING-191) when reconfiguration may lead
     * to calling custom appender code, which start some threads that call yet another code which call log statements.
     * This option may be turned off (value: {@code false}) to disable locks.
     */
    String PID_CFG_USE_LOCKS = "org.ops4j.pax.logging.useLocks";

    /**
     * <p>System or context property to configure locking just like {@link #PID_CFG_USE_LOCKS}.</p>
     */
    String LOGGING_CFG_USE_LOCKS = PID_CFG_USE_LOCKS;

}
