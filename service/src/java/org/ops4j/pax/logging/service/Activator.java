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

import org.ops4j.pax.logging.service.internal.ConfigFactoryImpl;
import org.ops4j.pax.logging.service.internal.Log4jServiceFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;

/**
 * Starts the Log4j log services.
 *
 */
public class Activator
    implements BundleActivator, ServiceFactory
{
    /**
     * Reference to the registered service
     */
    private ServiceRegistration m_RegistrationLog4J;
    private ServiceRegistration m_RegistrationManagedService;
    private Log4jServiceFactory m_Log4jServiceFactory;

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
        m_Log4jServiceFactory = new Log4jServiceFactory( configFactory );
        Hashtable properties = new Hashtable();
        properties.put( Log4jServiceFactory.LOG4J_CONFIG_FILE, "" );
        properties.put( Constants.SERVICE_PID, Log4jServiceFactory.class.getName() );

        String log4jServiceName = Log4JService.class.getName();
        m_RegistrationLog4J = bundleContext.registerService( log4jServiceName, m_Log4jServiceFactory, properties );
        String managedServiceName = ManagedService.class.getName();
        m_RegistrationManagedService = bundleContext.registerService( managedServiceName, this, properties );
    }

    /**
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop( BundleContext bundleContext ) throws Exception
    {
        m_RegistrationLog4J.unregister();
        m_RegistrationManagedService.unregister();
        m_Log4jServiceFactory = null;
    }

    public Object getService( Bundle bundle, ServiceRegistration registration )
    {
        return m_Log4jServiceFactory;
    }

    public void ungetService( Bundle bundle, ServiceRegistration registration, Object service )
    {
        // Dont think we need to dispose of it since it will be the same instance returned
        // the next time getService is called.
    }
}
