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
package org.ops4j.pax.logging.spi.support;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Level;

import org.ops4j.pax.logging.EventAdminPoster;
import org.ops4j.pax.logging.PaxLoggingConstants;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogLevel;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;

/**
 * Some helper methods for backends (Log4J2, Logback) to share common
 * internal classes.
 */
public class BackendSupport {

    private BackendSupport() {}

    /**
     * Returns configured service ranking for pax-logging services.
     * @return
     */
    public static int paxLoggingServiceRanking(BundleContext context) {
        int ranking = 1;
        String rankingProperty = context.getProperty(PaxLoggingConstants.LOGGING_CFG_LOGSERVICE_RANKING);
        if (rankingProperty != null) {
            ranking = Integer.parseInt(rankingProperty);
        }
        return ranking;
    }

    /**
     * Registers {@link LogReaderService} and makes it available for actual
     * {@link org.ops4j.pax.logging.PaxLoggingService} implementation
     * @param context
     * @return
     */
    public static RegisteredService<LogReaderService, LogReaderServiceImpl> createAndRegisterLogReaderService(BundleContext context) {
        // register org.osgi.service.log.LogReaderService
        LogReaderServiceImpl logReader = new LogReaderServiceImpl(100, FallbackLogFactory.createFallbackLog(context.getBundle(), "pax-logging"));
        Dictionary<String, Object> serviceProperties = new Hashtable<>();
        serviceProperties.put(Constants.SERVICE_RANKING, paxLoggingServiceRanking(context));
        ServiceRegistration<LogReaderService> registration = context.registerService(LogReaderService.class, logReader, serviceProperties);

        return new RegisteredService<>(logReader, registration);
    }

    /**
     * Prepares (without registration) {@link EventAdminPoster} regardles of availability of
     * Event Admin service
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static RegisteredService<EventAdminPoster, EventAdminPoster> eventAdminSupport(BundleContext context) {
        EventAdminPoster eventAdmin = new EventAdminPoster() {
            @Override
            public void postEvent(Bundle bundle, LogLevel level, LogEntry entry, String message, Throwable exception,
                    ServiceReference sr, Map context) {
            }

            @Override
            public void destroy() {
            }

            @Override
            public void close() {
            }
        };

        String enabled = OsgiUtil.systemOrContextProperty(context, PaxLoggingConstants.LOGGING_CFG_ENABLE_EVENT_ADMIN);
        if ("false".equalsIgnoreCase(enabled)) {
            // regardless of Event Admin availability
            return new RegisteredService<>(eventAdmin, null);
        }

        try {
            eventAdmin = new EventAdminTracker(context);
        } catch (NoClassDefFoundError e) {
            // If we hit a NCDFE, this means the event admin package is not available,
            // so use a dummy poster
        }

        return new RegisteredService<>(eventAdmin, null);
    }

    /**
     * Prepares (without registration) {@link ConfigurationNotifier} regardles of availability of
     * Event Admin service
     * @return
     */
    public static RegisteredService<ConfigurationNotifier, ConfigurationNotifier> eventAdminConfigurationNotifier(BundleContext context) {
        ConfigurationNotifier notifier = new ConfigurationNotifier() {
            @Override
            public void configurationDone() {
            }

            @Override
            public void configurationError(Throwable t) {
            }

            @Override
            public void close() {
            }
        };

        String enabled = OsgiUtil.systemOrContextProperty(context, PaxLoggingConstants.LOGGING_CFG_ENABLE_EVENT_ADMIN);
        if ("false".equalsIgnoreCase(enabled)) {
            // regardless of Event Admin availability
            return new RegisteredService<>(notifier, null);
        }

        try {
            notifier = new EventAdminConfigurationNotifier(context);
        } catch (NoClassDefFoundError ignored) {
        }

        return new RegisteredService<>(notifier, null);
    }

    /**
     * Gets the default level name as configured for pax-logging. The name may not be valid. Simply value
     * specified by user (in system or context properties) is returned (or default {@code DEBUG} value).
     * @param bundleContext
     * @return
     */
    public static String defaultLogLevel(BundleContext bundleContext) {
        String level = OsgiUtil.systemOrContextProperty(bundleContext, PaxLoggingConstants.LOGGING_CFG_DEFAULT_LOG_LEVEL);
        if (level == null || "".equals(level.trim())) {
            return "DEBUG";
        }
        return level;
    }

    /**
     * Returns one of the log level enums from {@link LogLevel} based on level name (case insensitive).
     * If level is not recognized, {@code defaultLevel} is assumed.
     * @param levelName
     * @param defaultLevel
     * @return {@link LogLevel} for matching level name. May return {@code null} for {@code OFF} or {@code NONE} level names.
     * @since 2.0.0
     */
    public static LogLevel convertR7LogLevel(String levelName, LogLevel defaultLevel) {
        if ("ALL".equalsIgnoreCase(levelName) || "AUDIT".equalsIgnoreCase(levelName)) {
            return LogLevel.AUDIT;
        } else if ("TRACE".equalsIgnoreCase(levelName) || "FINER".equalsIgnoreCase(levelName) || "FINEST".equalsIgnoreCase(levelName)) {
            return LogLevel.TRACE;
        } else if ("DEBUG".equalsIgnoreCase(levelName)) {
            return LogLevel.DEBUG;
        } else if ("INFO".equalsIgnoreCase(levelName)) {
            return LogLevel.INFO;
        } else if ("ERROR".equalsIgnoreCase(levelName) || "SEVERE".equalsIgnoreCase(levelName) || "FATAL".equalsIgnoreCase(levelName)) {
            return LogLevel.ERROR;
        } else if ("WARN".equalsIgnoreCase(levelName)) {
            return LogLevel.WARN;
        } else if ("DISABLED".equalsIgnoreCase(levelName) || "OFF".equalsIgnoreCase(levelName) || "NONE".equalsIgnoreCase(levelName)) {
            // special case...
            return null;
        } else {
            return defaultLevel;
        }
    }

    /**
     * Converts pax-logging level name (anything that pax-logging supports, case-insensitive) to {@link Level}
     * from {@code java.util.logging}. If nothing can be parsed, {@link Level#INFO} is returned.
     * @param levelName
     * @return
     */
    public static Level toJULLevel(String levelName) {
        if (levelName == null || levelName.equalsIgnoreCase("OFF")) {
            return Level.OFF;
        } else if (levelName.equalsIgnoreCase("FATAL") || levelName.equalsIgnoreCase("SEVERE") || levelName.equalsIgnoreCase("ERROR")) {
            return Level.SEVERE;
        } else if (levelName.equalsIgnoreCase("WARN") || levelName.equalsIgnoreCase("WARNING")) {
            return Level.WARNING;
        } else if (levelName.equalsIgnoreCase("INFO") || levelName.equalsIgnoreCase("CONFIG") || levelName.equalsIgnoreCase("CONF")) {
            return Level.INFO;
        } else if (levelName.equalsIgnoreCase("DEBUG") || levelName.equalsIgnoreCase("FINE")) {
            return Level.FINE;
        } else if (levelName.equalsIgnoreCase("TRACE") || levelName.equalsIgnoreCase("FINER")) {
            return Level.FINER;
        } else if (levelName.equalsIgnoreCase("FINEST")) {
            return Level.FINEST;
        } else if (levelName.equalsIgnoreCase("ALL")) {
            return Level.ALL;
        }
        return Level.INFO;
    }

    /**
     * {@link LogService} doesn't know the concept of logger <em>name</em> (or <em>category</em>), so
     * we have to derive it from what we have - a bundle
     * @param bundle
     * @return
     */
    public static String category(Bundle bundle) {
        String category = "undefined";

        if (bundle != null) {
            category = bundle.getSymbolicName();
            if (category == null) {
                category = "bundle@" + bundle.getBundleId();
            }
        }

        return category;
    }

    public static boolean isConfigurationAdminAvailable() {
        try {
            BackendSupport.class.getClassLoader().loadClass("org.osgi.service.cm.ConfigurationAdmin");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static String externalFile(BundleContext bundleContext, String defaultFile) {
        String property = OsgiUtil.systemOrContextProperty(bundleContext, PaxLoggingConstants.LOGGING_CFG_PROPERTY_FILE);
        return property == null ? defaultFile : property;
    }

}
