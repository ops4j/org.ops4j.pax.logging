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

import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.apache.logging.log4j.spi.LoggerContext;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingManager;
import org.ops4j.pax.logging.internal.Activator;
import org.ops4j.pax.logging.spi.support.FallbackLogFactory;
import org.osgi.framework.FrameworkUtil;

/**
 * This is the class used to obtain the loggers. Returned loggers have Log4J2 interface ({@link ExtendedLogger}), but
 * delegate the work to underlying {@link org.ops4j.pax.logging.PaxLogger}.
 */
public class Log4jv2LoggerContext implements LoggerContext {

    static PaxLoggingManager paxLogging;

    public static void setPaxLoggingManager(PaxLoggingManager manager) {
        paxLogging = manager;
    }

    @Override
    public ExtendedLogger getLogger(final String name) {
        return getLogger(name, null);
    }

    @Override
    public ExtendedLogger getLogger(final String name, final MessageFactory messageFactory) {
        PaxLogger paxLogger;
        if (paxLogging == null) {
            paxLogger = FallbackLogFactory.createFallbackLog(FrameworkUtil.getBundle(ExtendedLogger.class), name);
        } else {
            paxLogger = paxLogging.getLogger(name, Log4jv2Logger.LOG4J_FQCN);
        }
        Log4jv2Logger logger = new Log4jv2Logger(name, messageFactory, paxLogger);
        if (paxLogging == null) {
            // just add the logger which PaxLoggingManager need to be replaced.
            synchronized (Activator.m_loggers) {
                Activator.m_loggers.add(logger);
            }
        }
        return logger;
    }

    @Override
    public boolean hasLogger(final String name) {
        // we don't know in pax-logging... Because org.apache.logging.log4j.spi.LoggerContext.hasLogger()
        // API may actually be bridged to Logback or Log4j1 or no backend at all.
        // also, same "name" may be related to different loggers (associated with different bundles)
        throw new UnsupportedOperationException("Operation not supported in pax-logging");
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
