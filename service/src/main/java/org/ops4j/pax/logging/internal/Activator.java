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
import java.util.Map;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;
import org.osgi.service.log.LogEntry;
import org.ops4j.pax.logging.PaxLoggingService;
import org.ops4j.pax.logging.EventAdminPoster;

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

    private static final String[] LOGSERVICE_NAMES = {
        LogService.class.getName(),
        org.knopflerfish.service.log.LogService.class.getName(),
        PaxLoggingService.class.getName(),
        ManagedService.class.getName()
    };

    /**
     * Reference to the registered service
     */
    private ServiceRegistration m_RegistrationPaxLogging;
    private JdkHandler m_JdkHandler;
    private ServiceRegistration m_registrationLogReaderService;
    private FrameworkHandler m_frameworkHandler;
    private EventAdminPoster m_eventAdmin;
    private AppenderTracker m_appenderTracker;

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
        LogReaderServiceImpl logReader = new LogReaderServiceImpl( 100 );
        m_registrationLogReaderService =
            bundleContext.registerService( LogReaderService.class.getName(), logReader, null );

        // Tracking for the EventAdmin
        try {
            m_eventAdmin = new EventAdminTracker( bundleContext );
        } catch (NoClassDefFoundError e) {
            // If we hit a NCDFE, this means the event admin package is not available,
            // so use a dummy poster
            m_eventAdmin = new EventAdminPoster() {
                public void postEvent( Bundle bundle, int level, LogEntry entry, String message, Throwable exception,
                                       ServiceReference sr, Map context )
                {
                }
                public void destroy()
                {
                }
            };
        }

        // register the Pax Logging service
        m_appenderTracker = new AppenderTracker( bundleContext );
        m_appenderTracker.open( true );
        PaxLoggingServiceImpl paxLogging = new PaxLoggingServiceImpl( logReader, m_eventAdmin, m_appenderTracker );
        Hashtable serviceProperties = new Hashtable();
        serviceProperties.put( Constants.SERVICE_ID, "org.ops4j.pax.logging.configuration" );
        serviceProperties.put( Constants.SERVICE_PID, CONFIGURATION_PID );
        m_RegistrationPaxLogging = bundleContext.registerService( LOGSERVICE_NAMES, paxLogging, serviceProperties );

        // Add a global handler for all JDK Logging (java.util.logging).
        if( !Boolean.getBoolean( "org.ops4j.pax.logging.skipJUL" ) )
        {
            m_JdkHandler = new JdkHandler( paxLogging );
            Logger rootLogger = LogManager.getLogManager().getLogger( "" );
            rootLogger.addHandler( m_JdkHandler );
        }
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
        // shut down the trackers.
        m_appenderTracker.close();
        m_eventAdmin.destroy();

        // Clean up the listeners.
        bundleContext.removeBundleListener( m_frameworkHandler );
        bundleContext.removeFrameworkListener( m_frameworkHandler );
        bundleContext.removeServiceListener( m_frameworkHandler );

        // Remove the global handler for all JDK Logging (java.util.logging).
        if( m_JdkHandler != null )
        {
            Logger rootLogger = LogManager.getLogManager().getLogger( "" );
            rootLogger.removeHandler( m_JdkHandler );
            m_JdkHandler.flush();
            m_JdkHandler.close();
            m_JdkHandler = null;
        }
        
        m_RegistrationPaxLogging.unregister();
        m_RegistrationPaxLogging = null;

        m_registrationLogReaderService.unregister();
        m_registrationLogReaderService = null;
    }
}
