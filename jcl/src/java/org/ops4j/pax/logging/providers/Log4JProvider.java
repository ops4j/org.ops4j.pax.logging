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

public class Log4JProvider
    implements LogProvider
{
    private ServiceTracker m_Log4jServiceTracker;

    public Log4JProvider( BundleContext context )
    {
        m_Log4jServiceTracker = new ServiceTracker( context, PaxLoggingService.class.getName(), null );
        m_Log4jServiceTracker.open();
    }

    public Log getLogger( String categoryName )
    {
        return new Log4JServiceLog( m_Log4jServiceTracker, categoryName );
    }

    public void release()
    {
        m_Log4jServiceTracker.close();
    }
}
