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
package org.ops4j.pax.logging;

import java.util.HashMap;
import org.ops4j.pax.logging.internal.TrackingLogger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class PaxLoggingManager extends ServiceTracker
{

    private PaxLoggingService m_service;
    private BundleContext m_context;
    private HashMap<String, TrackingLogger> m_loggers;
    private ServiceReference m_logServiceRef;

    public PaxLoggingManager( BundleContext context )
    {
        super( context, PaxLoggingService.class.getName(), null );
        m_loggers = new HashMap<String, TrackingLogger>();
        m_context = context;
    }

    public Object addingService( ServiceReference reference )
    {
        m_logServiceRef = reference;
        m_service = (PaxLoggingService) m_context.getService( reference );
        for( TrackingLogger logger : m_loggers.values() )
        {
            logger.added( m_service );
        }
        return m_service;
    }

    public void removedService( ServiceReference reference, Object service )
    {
        m_service = null;
        m_context.ungetService( m_logServiceRef );
        m_logServiceRef = null;
        for( TrackingLogger logger : m_loggers.values() )
        {
            logger.removed();
        }
    }

    public PaxLogger getLogger( String category )
    {
        TrackingLogger logger = new TrackingLogger( m_service, category );
        m_loggers.put( category, logger );
        return logger;
    }

    public PaxLoggingService getPaxLoggingService()
    {
        return m_service;
    }

    public void dispose()
    {
        if( m_logServiceRef != null )
        {
            m_context.ungetService( m_logServiceRef );
        }
    }
}
