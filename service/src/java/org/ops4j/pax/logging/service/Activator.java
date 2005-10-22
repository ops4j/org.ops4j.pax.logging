/*
 * Copyright 2005 Niclas Hedhman.
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
package org.ops4j.pax.logging.service;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;

import org.ops4j.pax.logging.service.internal.ConfigFactoryImpl;
import org.ops4j.pax.logging.service.internal.Log4jServiceFactory;

/**
 * Starts the Log4j log services.
 * 
 */
public class Activator
    implements BundleActivator
{
    /**
     * Reference to the registered service
     */
    private ServiceRegistration m_Registration;

    /**
     * Default constructor
     */
    public Activator()
    {
    }

    /**
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start( BundleContext bundleContext ) throws Exception
    {
        ConfigFactoryImpl configFactory = new ConfigFactoryImpl();
        Log4jServiceFactory log4jServiceFactory = new Log4jServiceFactory( configFactory );
        String name = LogService.class.getName();
        Hashtable properties = new Hashtable();
        properties.put( Log4jServiceFactory.LOG4J_CONFIG_FILE, "" );
        m_Registration = bundleContext.registerService( name, log4jServiceFactory, properties );
    }

    /**
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop( BundleContext bundleContext ) throws Exception
    {
        m_Registration.unregister();
    }
}
