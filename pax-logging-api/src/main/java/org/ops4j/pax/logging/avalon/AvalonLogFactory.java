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
package org.ops4j.pax.logging.avalon;

import org.apache.avalon.framework.logger.Logger;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingManager;
import org.ops4j.pax.logging.internal.Activator;
import org.ops4j.pax.logging.spi.support.FallbackLogFactory;
import org.osgi.framework.FrameworkUtil;

/**
 * Unlike with modern (sic!) Logging frameworks like SLF4J and Commons Logging, there's no Avalon-specific
 * factory for its loggers. To make it consistent with other supported facades, this implementation is
 * a factory for loggers.
 */
public class AvalonLogFactory {

    private static PaxLoggingManager m_paxLogging;

    public static void setPaxLoggingManager(PaxLoggingManager manager) {
        m_paxLogging = manager;
    }

    /**
     * Main static factory method to return instance of {@link Logger}.
     * @param name
     * @return
     */
    public static Logger getLogger(String name) {
        return getLogger(null, name);
    }

    public static Logger getLogger(AvalonLogger parent, String name) {
        String newName = parent == null ? name : parent.getName() + "." + name;
        PaxLogger logger;
        if (m_paxLogging == null) {
            logger = FallbackLogFactory.createFallbackLog(FrameworkUtil.getBundle(Logger.class), name);
        } else {
            logger = m_paxLogging.getLogger(newName, AvalonLogger.AVALON_FQCN);
        }
        AvalonLogger avalonLogger = new AvalonLogger(newName, logger);
        if (m_paxLogging == null) {
            synchronized (Activator.m_loggers) {
                Activator.m_loggers.add(avalonLogger);
            }
        }
        return avalonLogger;
    }

}
