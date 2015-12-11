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
package org.ops4j.pax.logging.log4jv2;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.spi.AbstractLogger;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.apache.logging.log4j.spi.LoggerContext;
import org.ops4j.pax.logging.OSGIPaxLoggingManager;
import org.ops4j.pax.logging.PaxLoggingManager;
import org.osgi.framework.BundleContext;

/**
 *
 */
public class Log4jv2LoggerContext implements LoggerContext {

    private static final ConcurrentMap<String, Log4jv2Logger> loggers = new ConcurrentHashMap<String, Log4jv2Logger>();

    private static PaxLoggingManager paxLogging;

    public static void setBundleContext( BundleContext ctx )
    {
        paxLogging = new OSGIPaxLoggingManager( ctx );
        for (Log4jv2Logger logger : loggers.values()) {
            logger.setPaxLoggingManager(paxLogging);
        }
        paxLogging.open();
    }

    public static void dispose() {

    }

    public Log4jv2LoggerContext() {
    }

    @Override
    public ExtendedLogger getLogger(final String name) {
        return getLogger(name, null);
    }

    @Override
    public ExtendedLogger getLogger(final String name, final MessageFactory messageFactory) {
        final ExtendedLogger extendedLogger = loggers.get(name);
        if (extendedLogger != null) {
            AbstractLogger.checkMessageFactory(extendedLogger, messageFactory);
            return extendedLogger;
        }
        loggers.putIfAbsent(name, new Log4jv2Logger(name, messageFactory, paxLogging));
        return loggers.get(name);
    }

    @Override
    public boolean hasLogger(final String name) {
        return loggers.containsKey(name);
    }

    @Override
    public Object getExternalContext() {
        return null;
    }
}
