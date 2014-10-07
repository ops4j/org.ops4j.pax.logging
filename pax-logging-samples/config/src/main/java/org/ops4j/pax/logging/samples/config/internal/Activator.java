/*
 * Copyright 2008 Alin Dreghiciu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.logging.samples.config.internal;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * Bundle Activator.<br/>
 * Looks up the Configuration Admin service and on activation will configure Pax Logging.
 * On deactivation will unconfigure Pax Logging.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.2.2, November 26, 2008
 */
public final class Activator
    implements BundleActivator
{

    /**
     * {@inheritDoc}
     * Configures Pax Logging via Configuration Admin.
     */
    public void start( final BundleContext bundleContext )
        throws Exception
    {
        updateConfiguration( bundleContext, "%5p [%t] - %m%n" );
    }

    /**
     * {@inheritDoc}
     * UnConfigures Pax Logging via Configuration Admin.
     */
    public void stop( final BundleContext bundleContext )
        throws Exception
    {
        updateConfiguration( bundleContext, "%d [%t] %-5p %c - %m%n" );
    }

    /**
     * Updates Pax Logging configuration to a specifid conversion pattern.
     *
     * @param bundleContext bundle context
     * @param pattern       layout conversion pattern
     *
     * @throws IOException - Re-thrown
     */
    private void updateConfiguration( BundleContext bundleContext,
                                      final String pattern )
        throws IOException
    {
        final ConfigurationAdmin configAdmin = getConfigurationAdmin( bundleContext );
        final Configuration configuration = configAdmin.getConfiguration( "org.ops4j.pax.logging", null );

        final Hashtable<String, Object> log4jProps = new Hashtable<String, Object>();
        log4jProps.put( "log4j.rootLogger", "DEBUG, CONSOLE" );
        log4jProps.put( "log4j.appender.CONSOLE", "org.apache.log4j.ConsoleAppender" );
        log4jProps.put( "log4j.appender.CONSOLE.layout", "org.apache.log4j.PatternLayout" );
        log4jProps.put( "log4j.appender.CONSOLE.layout.ConversionPattern", pattern );

        configuration.update( log4jProps );
    }

    /**
     * Gets Configuration Admin service from service registry.
     *
     * @param bundleContext bundle context
     *
     * @return configuration admin service
     *
     * @throws IllegalStateException - If no Configuration Admin service is available
     */
    private ConfigurationAdmin getConfigurationAdmin( final BundleContext bundleContext )
    {
        final ServiceReference ref = bundleContext.getServiceReference( ConfigurationAdmin.class.getName() );
        if( ref == null )
        {
            throw new IllegalStateException( "Cannot find a configuration admin service" );
        }
        return (ConfigurationAdmin) bundleContext.getService( ref );
    }

}

