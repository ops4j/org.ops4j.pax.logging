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
package org.ops4j.pax.logging.internal;

import java.util.Hashtable;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.ops4j.pax.logging.PaxLoggingService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;

/**
 * Starts the Log4j log services.
 */
public class Activator
    implements BundleActivator
{
    /**
     * The Managed Service PID for the log4j configuration
     */
    public static final String CONFIGURATION_PID = "org.ops4j.pax.logging";

    /**
     * Reference to the registered service
     */
    private ServiceRegistration m_RegistrationPaxLogging;
    private JdkHandler m_JdkHandler;
    private ServiceRegistration m_registrationLogReaderService;
    private FrameworkHandler m_frameworkHandler;

    /**
     * Default constructor
     */
    public Activator()
    {
    }

    /**
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start( BundleContext bundleContext )
        throws Exception
    {
        // register the LogReaderService
        LogReaderServiceImpl logReader = new LogReaderServiceImpl();
        m_registrationLogReaderService =
            bundleContext.registerService( LogReaderService.class.getName(), logReader, null );

        // register the Pax Logging service
        Log4jConfiguratorImpl log4jConfigurator = new Log4jConfiguratorImpl();
        PaxLoggingServiceImpl paxLogging = new PaxLoggingServiceImpl( logReader );
        final LoggingServiceConfiguration loggingServiceConfig = new LoggingServiceConfiguration( log4jConfigurator );
        final LoggingServiceFactory loggingServiceFactory =
            new LoggingServiceFactory( loggingServiceConfig, paxLogging );

        String[] services =
            {
                LogService.class.getName(),
                org.knopflerfish.service.log.LogService.class.getName(),
                PaxLoggingService.class.getName()
            };

        Hashtable srProperties = new Hashtable();
        m_RegistrationPaxLogging = bundleContext.registerService( services, loggingServiceFactory, srProperties );
        // Register the logging service configuration
        Hashtable configProperties = new Hashtable();
        configProperties.put( Constants.SERVICE_ID, "org.ops4j.pax.logging.configuration" );
        configProperties.put( Constants.SERVICE_PID, CONFIGURATION_PID );
        bundleContext.registerService( LoggingServiceConfiguration.class.getName(), loggingServiceConfig, configProperties );
        // Add a global handler for all JDK Logging (java.util.logging).
        m_JdkHandler = new JdkHandler( paxLogging );
        Logger rootLogger = LogManager.getLogManager().getLogger( "" );
        rootLogger.addHandler( m_JdkHandler );

        m_frameworkHandler = new FrameworkHandler( paxLogging );
        bundleContext.addBundleListener( m_frameworkHandler );
        bundleContext.addFrameworkListener( m_frameworkHandler );
        bundleContext.addServiceListener( m_frameworkHandler );
    }

    /**
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop( BundleContext bundleContext )
        throws Exception
    {
        // Clean up the listeners.
        bundleContext.removeBundleListener( m_frameworkHandler );
        bundleContext.removeFrameworkListener( m_frameworkHandler );
        bundleContext.removeServiceListener( m_frameworkHandler );

        // Remove the global handler for all JDK Logging (java.util.logging).
        Logger rootLogger = LogManager.getLogManager().getLogger( "" );
        rootLogger.removeHandler( m_JdkHandler );
        m_JdkHandler.flush();
        m_JdkHandler.close();
        m_JdkHandler = null;
        m_RegistrationPaxLogging.unregister();
        m_registrationLogReaderService.unregister();
    }
}
