/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package org.ops4j.pax.logging.log4j;

import java.util.Dictionary;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.LoggerContext;
import org.apache.logging.log4j.status.StatusLogger;
import org.ops4j.pax.logging.PaxContext;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

/**
 *
 */
public class Log4jPaxLoggingService implements PaxLoggingService, ManagedService, ServiceFactory<PaxLoggingService> {

    private static final String DEFAULT_LOGGER_NAME = "[undefined]";
    private static final String DEFAULT_BUNDLE_LOGGER_NAME_FORMAT = "[bundle@%d]";

    private static final Logger LOGGER = StatusLogger.getLogger();

    private final BundleContext bundleContext;
    private final LoggerContext loggerContext;
    private final PaxContext paxContext;

    private int logLevel = LOG_DEBUG;

    public Log4jPaxLoggingService(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        loggerContext = LogManager.getContext(new BundleDelegatingClassLoader(bundleContext.getBundle()),
            false, bundleContext);
        paxContext = new PaxContext();
    }

    @Override
    public PaxLogger getLogger(Bundle bundle, String category, String fqcn) {
        final Logger logger = loggerContext.getLogger(category == null ? LogManager.ROOT_LOGGER_NAME : category);
        return new Log4jPaxLogger(logger, bundle, this);
    }

    @Override
    public int getLogLevel() {
        return logLevel;
    }

    @Override
    public PaxContext getPaxContext() {
        return paxContext;
    }

    @Override
    public void log(int level, String message) {
        logInternal(null, level, message, null);
    }

    @Override
    public void log(int level, String message, Throwable exception) {
        logInternal(null, level, message, exception);
    }

    @Override
    public void log(ServiceReference sr, int level, String message) {
        logInternal(sr == null ? null : sr.getBundle(), level, message, null);
    }

    @Override
    public void log(ServiceReference sr, int level, String message, Throwable exception) {
        logInternal(sr == null ? null : sr.getBundle(), level, message, exception);
    }

    private void logInternal(final Bundle bundle, final int level, final String message, final Throwable exception) {
        try {
            final PaxLogger logger = getLogger(bundle, determineLoggerName(bundle), null);
            switch (level) {
                case LOG_DEBUG:
                    logger.debug(message, exception);
                    break;
                case LOG_INFO:
                    logger.inform(message, exception);
                    break;
                case LOG_WARNING:
                    logger.warn(message, exception);
                    break;
                case LOG_ERROR:
                    logger.error(message, exception);
                    break;
                default:
                    if (logger.isWarnEnabled()) {
                        logger.warn("Undefined Level: " + level + " : " + message, exception);
                    }
                    break;
            }
        } catch (final RuntimeException e) {
            LOGGER.catching(e);
        }
    }

    private static String determineLoggerName(final Bundle bundle) {
        if (bundle == null) {
            return DEFAULT_LOGGER_NAME;
        }
        final String symbolicName = bundle.getSymbolicName();
        return symbolicName == null ? String.format(DEFAULT_BUNDLE_LOGGER_NAME_FORMAT,
            bundle.getBundleId()) : symbolicName;
    }

    @Override
    public void updated(Dictionary properties) throws ConfigurationException {

    }

    @Override
    public PaxLoggingService getService(Bundle bundle, ServiceRegistration<PaxLoggingService> registration) {
        return new Log4jPaxLoggingService(bundle.getBundleContext());
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration<PaxLoggingService> registration,
                             PaxLoggingService service) {

    }
}
