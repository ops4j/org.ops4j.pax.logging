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
package org.ops4j.pax.logging.jbosslogging;

import java.util.Map;

import org.jboss.logging.Logger;
import org.jboss.logging.LoggerProvider;
import org.ops4j.pax.logging.PaxContext;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingManager;
import org.ops4j.pax.logging.PaxLoggingService;
import org.ops4j.pax.logging.internal.Activator;
import org.ops4j.pax.logging.spi.support.FallbackLogFactory;
import org.osgi.framework.FrameworkUtil;

/**
 * {@link LoggerProvider} bridging JBoss Logging to Pax Logging.
 */
public class PaxLoggingLoggerProvider implements LoggerProvider {

    private static PaxLoggingManager paxLogging;

    /** {@link PaxContext} used when {@link org.ops4j.pax.logging.PaxLoggingService} is not available */
    private static PaxContext m_defaultContext = new PaxContext();
    /** {@link PaxContext} obtained from {@link org.ops4j.pax.logging.PaxLoggingService} */
    private static PaxContext m_context;

    public static void setPaxLoggingManager(PaxLoggingManager manager) {
        paxLogging = manager;
    }

    @Override
    public Logger getLogger(String name) {
        PaxLogger paxLogger;
        if (paxLogging == null) {
            paxLogger = FallbackLogFactory.createFallbackLog(FrameworkUtil.getBundle(Logger.class), name);
        } else {
            paxLogger = paxLogging.getLogger(name, JBossLoggingLogger.JBOSS_LOGGING_FQCN);
        }
        JBossLoggingLogger logger = new JBossLoggingLogger(name, paxLogger);
        if (paxLogging == null) {
            // just add the logger which PaxLoggingManager need to be replaced.
            synchronized (Activator.m_loggers) {
                Activator.m_loggers.add(logger);
            }
        }
        return logger;
    }

    @Override
    public void clearMdc() {
        getContext().clear();
    }

    @Override
    public Object putMdc(String key, Object value) {
        Object previous = getMdc(key);
        getContext().put(key, value);
        return previous;
    }

    @Override
    public Object getMdc(String key) {
        return getContext().get(key);
    }

    @Override
    public void removeMdc(String key) {
        getContext().remove(key);
    }

    @Override
    public Map<String, Object> getMdcMap() {
        return getContext().getCopyOfContextMap();
    }

    @Override
    public void clearNdc() {
        // not (yet) supported in pax-logging
    }

    @Override
    public String getNdc() {
        // not (yet) supported in pax-logging
        return null;
    }

    @Override
    public int getNdcDepth() {
        // not (yet) supported in pax-logging
        return 0;
    }

    @Override
    public String popNdc() {
        // not (yet) supported in pax-logging
        return null;
    }

    @Override
    public String peekNdc() {
        // not (yet) supported in pax-logging
        return null;
    }

    @Override
    public void pushNdc(String message) {
        // not (yet) supported in pax-logging
    }

    @Override
    public void setNdcMaxDepth(int maxDepth) {
        // not (yet) supported in pax-logging
    }

    private static PaxContext getContext() {
        PaxLoggingManager manager = PaxLoggingLoggerProvider.paxLogging;
        if (manager != null) {
            synchronized (PaxLoggingLoggerProvider.class) {
                PaxLoggingService service = manager.getPaxLoggingService();
                m_context = service != null ? service.getPaxContext() : null;
            }
        }
        return m_context != null ? m_context : m_defaultContext;
    }

}
