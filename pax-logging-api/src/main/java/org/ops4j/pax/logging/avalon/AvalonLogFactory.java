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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import org.apache.avalon.framework.logger.Logger;
import org.ops4j.pax.logging.OSGIPaxLoggingManager;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingManager;
import org.ops4j.pax.logging.internal.FallbackLogFactory;
import org.osgi.framework.BundleContext;

public class AvalonLogFactory
{

    private static PaxLoggingManager m_paxLogging;
    private static Map<String, List<AvalonLogger>> m_loggers;

    static
    {
        m_loggers = new WeakHashMap<String, List<AvalonLogger>>();
    }

    public static void setBundleContext( BundleContext context )
    {
        synchronized (m_loggers) {
            m_paxLogging = new OSGIPaxLoggingManager( context );
            for (Entry<String, List<AvalonLogger>> entry : m_loggers.entrySet()) {
                String name = entry.getKey();
                List<AvalonLogger> loggers = entry.getValue();
                if (loggers != null) {
                    for (AvalonLogger logger : loggers) {
                        logger.setPaxLoggingManager( m_paxLogging, name );
                    }
                }
            }
            m_paxLogging.open();
            m_loggers.clear();
        }
    }

    /**
     * Lifecycle method to release any resources held.
     */
    public static void release()
    {
    }

    public static Logger getLogger( String name )
    {
        return getLogger( null, name );
    }

    public static Logger getLogger( AvalonLogger parent, String name )
    {
        String newName;
        if( parent == null )
        {
            newName = name;
        }
        else
        {
            newName = parent.getName() + "." + name;
        }
        PaxLogger paxLogger;
        if( m_paxLogging == null )
        {
            paxLogger = FallbackLogFactory.createFallbackLog( null, name );
        }
        else
        {
            paxLogger = m_paxLogging.getLogger( name, AvalonLogger.AVALON_FQCN );
        }
        PaxLogger logger = m_paxLogging.getLogger( newName, AvalonLogger.AVALON_FQCN );
        AvalonLogger avalonLogger = new AvalonLogger( logger );
        if (m_paxLogging == null) {
            synchronized (m_loggers) {
                if (!m_loggers.containsKey(newName)) {
                    m_loggers.put(newName, new LinkedList<AvalonLogger>());
                }
                m_loggers.get(newName).add(avalonLogger);
            }
            m_loggers.get(newName).add(avalonLogger);
        }
        return avalonLogger;
    }

    /** Pax Logging internal method. Should never be used directly. */
    public static void dispose()
    {
        m_paxLogging.close();
        m_paxLogging.dispose();
        m_paxLogging = null;
    }
}
