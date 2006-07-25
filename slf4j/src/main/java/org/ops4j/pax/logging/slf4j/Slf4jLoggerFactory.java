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
import org.osgi.framework.BundleContext;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

public class Slf4jLoggerFactory
    implements ILoggerFactory
{
    private static PaxLoggingManager m_paxLogging;

    public static void setBundleContext( BundleContext context )
    {
        m_paxLogging = new PaxLoggingManager( context );
        m_paxLogging.open();
    }

    /** Releases any held resources and makes the class ready for garbage collection.
     *
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
        PaxLogger logger = m_paxLogging.getLogger( name );
        return new Slf4jLogger( name, logger );
    }
}
