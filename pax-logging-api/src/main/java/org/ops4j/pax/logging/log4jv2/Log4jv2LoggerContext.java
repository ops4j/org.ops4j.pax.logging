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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.apache.logging.log4j.spi.LoggerContext;
import org.ops4j.pax.logging.OSGIPaxLoggingManager;
import org.ops4j.pax.logging.PaxLoggingManager;
import org.osgi.framework.BundleContext;

/**
 *
 */
public class Log4jv2LoggerContext implements LoggerContext {

    private static final Map<String, List<Log4jv2Logger>> m_loggers = new WeakHashMap<String, List<Log4jv2Logger>>();

    static PaxLoggingManager paxLogging;

    public static void setBundleContext( BundleContext ctx )
    {
        synchronized (m_loggers) {
            paxLogging = new OSGIPaxLoggingManager( ctx );
            for (List<Log4jv2Logger> loggers : m_loggers.values()) {
                for (Log4jv2Logger logger : loggers) {
                    logger.setPaxLoggingManager(paxLogging);
                }
            }
            paxLogging.open();
            m_loggers.clear();
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
        Log4jv2Logger log4jv2Logger = new Log4jv2Logger(name, messageFactory, paxLogging);
        if (paxLogging == null) {
            // just add the logger which PaxLoggingManager need to be replaced.
            synchronized (m_loggers) {
                if (!m_loggers.containsKey(name)) {
                    m_loggers.put(name, new LinkedList<Log4jv2Logger>());
                }
                m_loggers.get(name).add(log4jv2Logger);
            }
        }
        return log4jv2Logger;
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
