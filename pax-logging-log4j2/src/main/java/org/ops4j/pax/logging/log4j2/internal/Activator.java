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
package org.ops4j.pax.logging.log4j2.internal;

import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.ops4j.pax.logging.EventAdminPoster;
import org.ops4j.pax.logging.PaxLoggingService;
import org.ops4j.pax.logging.internal.eventadmin.EventAdminTracker;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.log.LogEntry;
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

    private static final String[] LOGSERVICE_NAMES = {
        LogService.class.getName(),
        org.knopflerfish.service.log.LogService.class.getName(),
        PaxLoggingService.class.getName(),
        ManagedService.class.getName()
    };

    /**
     * Reference to the registered service
     */
    private PaxLoggingServiceImpl m_PaxLogging;
    private ServiceRegistration m_RegistrationPaxLogging;
    private JdkHandler m_JdkHandler;
    private ServiceRegistration m_registrationLogReaderService;
    private FrameworkHandler m_frameworkHandler;
    private EventAdminPoster m_eventAdmin;

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
        int ranking = 1;
        String rankingProperty = bundleContext.getProperty("org.ops4j.pax.logging.ranking");
        if (rankingProperty != null) {
            ranking = Integer.parseInt(rankingProperty);
        }

        // register the LogReaderService
        LogReaderServiceImpl logReader = new LogReaderServiceImpl( 100 );
        String readerServiceName = LogReaderService.class.getName();
        Hashtable serviceProperties = new Hashtable();
        serviceProperties.put( Constants.SERVICE_RANKING, ranking );
        m_registrationLogReaderService = bundleContext.registerService( readerServiceName, logReader, serviceProperties );

        // Tracking for the EventAdmin
        try
        {
            m_eventAdmin = new EventAdminTracker( bundleContext );
        }
        catch( NoClassDefFoundError e )
        {
            // If we hit a NCDFE, this means the event admin package is not available,
            // so use a dummy poster
            m_eventAdmin = new EventAdminPoster()
            {
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
        m_PaxLogging = new PaxLoggingServiceImpl( bundleContext, logReader, m_eventAdmin );
        serviceProperties = new Hashtable();
        serviceProperties.put( Constants.SERVICE_ID, "org.ops4j.pax.logging.configuration" );
        serviceProperties.put( Constants.SERVICE_PID, CONFIGURATION_PID );
        serviceProperties.put( Constants.SERVICE_RANKING, ranking );
        m_RegistrationPaxLogging = bundleContext.registerService( LOGSERVICE_NAMES, m_PaxLogging, serviceProperties );

        // Add a global handler for all JDK Logging (java.util.logging).
        String skipJULProperty=bundleContext.getProperty("org.ops4j.pax.logging.skipJUL");
        if( !Boolean.parseBoolean(skipJULProperty))
        {
            LogManager manager = LogManager.getLogManager();
            manager.reset();
            
            // clear out old handlers
            Logger rootLogger = manager.getLogger( "" );
            Handler[] handlers = rootLogger.getHandlers();
            for (int i = 0; i < handlers.length;i++) {
            	rootLogger.removeHandler(handlers[i]);
            }

            rootLogger.setFilter(null);

            m_JdkHandler = new JdkHandler( m_PaxLogging );
            rootLogger.addHandler( m_JdkHandler );
        }
        m_frameworkHandler = new FrameworkHandler( m_PaxLogging );
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

        // Shutdown Pax Logging to ensure appender file locks get released
        if (m_PaxLogging != null) {
            m_PaxLogging.shutdown();
            m_PaxLogging = null;
        }

        m_registrationLogReaderService.unregister();
        m_registrationLogReaderService = null;
    }
}
