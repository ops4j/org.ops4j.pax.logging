/*
 * Copyright 2006 Niclas Hedhman.
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
package org.ops4j.pax.logging.slf4j;

import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingManager;
import org.ops4j.pax.logging.internal.Activator;
import org.ops4j.pax.logging.spi.support.FallbackLogFactory;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

/**
 * <p>pax-logging specific {@link ILoggerFactory} returned from {@link org.slf4j.impl.StaticLoggerBinder}</p>
 */
public class Slf4jLoggerFactory implements ILoggerFactory {

    static PaxLoggingManager m_paxLogging;

    public static void setPaxLoggingManager(PaxLoggingManager manager) {
        m_paxLogging = manager;
    }

    /**
     * <p>Return an appropriate {@link org.slf4j.Logger} instance as specified by the <code>name</code> parameter.</p>
     * <p>Null-valued name arguments are considered invalid.</p>
     *
     * @param name the name of the Logger to return
     */
    @Override
    public Logger getLogger(String name) {
        PaxLogger paxLogger;
        if (m_paxLogging == null) {
            paxLogger = FallbackLogFactory.createFallbackLog(null, name);
        } else {
            paxLogger = m_paxLogging.getLogger(name, Slf4jLogger.SLF4J_FQCN);
        }
        Slf4jLogger logger = new Slf4jLogger(name, paxLogger);
        if (m_paxLogging == null) {
            // just add the logger which PaxLoggingManager need to be replaced.
            synchronized (Activator.m_loggers) {
                Activator.m_loggers.put(name, logger);
            }
        }
        return logger;
    }

}
