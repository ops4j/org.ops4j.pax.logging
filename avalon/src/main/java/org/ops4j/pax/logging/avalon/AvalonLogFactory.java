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
import org.osgi.framework.BundleContext;

public class AvalonLogFactory
{

    private static PaxLoggingManager m_paxLogging;

    public static void setBundleContext( BundleContext context )
    {
        m_paxLogging = new PaxLoggingManager( context );
        m_paxLogging.open();
    }

    /** Lifecycle method to release any resources held.
     *
     */
    public static void release()
    {
        m_paxLogging.close();
        m_paxLogging.dispose();
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
        PaxLogger logger = m_paxLogging.getLogger( newName );
        return new AvalonLogger( "", logger );
    }
}
