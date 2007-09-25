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

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import org.ops4j.pax.logging.DefaultServiceLog;
import org.ops4j.pax.logging.OSGIPaxLoggingManager;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingManager;
import org.osgi.framework.BundleContext;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

public class Slf4jLoggerFactory
    implements ILoggerFactory
{

    private static PaxLoggingManager m_paxLogging;
    private static WeakHashMap m_loggers;

    static
    {
        m_loggers = new WeakHashMap();
    }

    public static void setBundleContext( BundleContext context )
    {
        m_paxLogging = new OSGIPaxLoggingManager( context );
        // We need to instruct all loggers to ensure the SimplePaxLoggingManager is replaced.
        Set entrySet = m_loggers.entrySet();
        Iterator iterator = entrySet.iterator();
        while( iterator.hasNext() )
        {
            Map.Entry entry = (Entry) iterator.next();
            String name = (String) entry.getValue();
            Slf4jLogger logger = (Slf4jLogger) entry.getKey();
            logger.setPaxLoggingManager( m_paxLogging, name );
        }
        m_paxLogging.open();
    }

    /**
     * Releases any held resources and makes the class ready for garbage collection.
     */
    public static void release()
    {
        m_paxLogging.close();
        m_paxLogging.dispose();
    }

    /**
     * Return an appropriate {@link org.slf4j.Logger} instance as specified by the
     * <code>name</code> parameter.
     *
     * <p>Null-valued name arguments are considered invalid.
     *
     * <p>Certain extremely simple logging systems, e.g. NOP, may always
     * return the same logger instance regardless of the requested name.
     *
     * @param name the name of the Logger to return
     */
    public Logger getLogger( String name )
    {
        PaxLogger paxLogger;
        if( m_paxLogging == null )
        {
            paxLogger = new DefaultServiceLog( null, name );
        }
        else
        {
            paxLogger = m_paxLogging.getLogger( name, Slf4jLogger.SLF4J_FQCN );
        }
        Slf4jLogger logger = new Slf4jLogger( name, paxLogger );
        m_loggers.put( logger, name );
        return logger;
    }
}
