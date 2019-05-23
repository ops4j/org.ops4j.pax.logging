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
import org.ops4j.pax.logging.PaxLoggingManager;

/**
 * This is the class used to obtain the loggers. Returned loggers have Log4J2 interface ({@link ExtendedLogger}), but
 * delegate the work to underlying {@link org.ops4j.pax.logging.PaxLogger}.
 */
public class Log4jv2LoggerContext implements LoggerContext {

    static PaxLoggingManager paxLogging;

    public static void setPaxLoggingManager(PaxLoggingManager manager) {
        paxLogging = manager;
    }

    private static final ConcurrentMap<String, Log4jv2Logger> loggers = new ConcurrentHashMap<String, Log4jv2Logger>();

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
    public boolean hasLogger(String name, MessageFactory messageFactory) {
        return hasLogger(name);
    }

    @Override
    public boolean hasLogger(String name, Class<? extends MessageFactory> messageFactoryClass) {
        return hasLogger(name);
    }

    @Override
    public Object getExternalContext() {
        return null;
    }
}
