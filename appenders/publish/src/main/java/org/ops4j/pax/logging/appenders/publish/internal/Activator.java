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
package org.ops4j.pax.logging.appenders.publish.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.ops4j.pax.logging.appenders.publish.PublishAppender;
import org.apache.log4j.Appender;

/** This Activator creates the PublishAppender and registers it to the OSGi framework.
 * The Pax Logging Service will then see this (whiteboard pattern) and make the appender
 * available to be used in the configuration.
 *
 * <b>NOTE:</b> This is work in progress and not working yet.
 */
public class Activator
    implements BundleActivator
{
    private PublishAppender m_appender;
    private ServiceRegistration m_registration;

    public void start( BundleContext bundleContext )
        throws Exception
    {
        m_appender = new PublishAppender( bundleContext );
        m_registration = bundleContext.registerService( Appender.class.getName(), m_appender, null );
    }

    public void stop( BundleContext bundleContext )
        throws Exception
    {
        m_appender.close();
        m_registration.unregister();
    }
}
