/*
 * Copyright 2005 Niclas Hedhman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.logging.providers;

import org.apache.commons.logging.Log;

import org.ops4j.pax.logging.service.PaxLoggingService;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public class PaxLoggingProvider
    implements LogProvider
{
    private ServiceTracker m_PaxLoggingServiceTracker;

    public PaxLoggingProvider( BundleContext context )
    {
        m_PaxLoggingServiceTracker = new ServiceTracker( context, PaxLoggingService.class.getName(), null );
        m_PaxLoggingServiceTracker.open();
    }

    public Log getLogger( String categoryName )
    {
        return new PaxLoggingServiceLog( m_PaxLoggingServiceTracker, categoryName );
    }

    public void release()
    {
        m_PaxLoggingServiceTracker.close();
    }
}
