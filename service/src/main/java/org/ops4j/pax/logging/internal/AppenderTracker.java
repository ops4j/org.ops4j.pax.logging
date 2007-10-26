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
package org.ops4j.pax.logging.internal;

import java.util.HashMap;
import org.apache.log4j.Appender;
import org.ops4j.pax.logging.PaxLoggingService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class AppenderTracker extends ServiceTracker
{
    private HashMap m_appenders;

    public AppenderTracker( BundleContext bundleContext )
    {
        super( bundleContext
            , "&("
              + "(" + Constants.OBJECTCLASS + "=" + Appender.class.getName() + ")"
              + "(" + PaxLoggingService.APPENDER_NAME_PROPERTY + "=*)"
              + ")"
            , null
        );
        m_appenders = new HashMap();
    }

    public Object addingService( ServiceReference serviceReference )
    {
        Object name = serviceReference.getProperty( PaxLoggingService.APPENDER_NAME_PROPERTY );
        Object appender = super.addingService( serviceReference );
        m_appenders.put( name, appender );
        return appender;
    }

    public void modifiedService( ServiceReference serviceReference, Object object )
    {
        removeAppender( (Appender) object );
        Object name = serviceReference.getProperty( PaxLoggingService.APPENDER_NAME_PROPERTY );
        m_appenders.put( name, object );
        super.modifiedService( serviceReference, object );
    }

    public void removedService( ServiceReference serviceReference, Object object )
    {
        Object name = serviceReference.getProperty( PaxLoggingService.APPENDER_NAME_PROPERTY );
        m_appenders.remove( name );
        super.removedService( serviceReference, object );
    }

    public Appender getAppender( String name )
    {
        return (Appender) m_appenders.get( name );
    }

    private void removeAppender( Appender appender )
    {
        String name = appender.getName();
        m_appenders.remove( name );
    }
}