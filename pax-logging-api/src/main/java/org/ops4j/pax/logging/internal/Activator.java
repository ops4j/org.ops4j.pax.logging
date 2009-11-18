/*  Copyright 2007 Niclas Hedhman.
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
package org.ops4j.pax.logging.internal;


import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.ops4j.pax.logging.slf4j.Slf4jLoggerFactory;
import org.ops4j.pax.logging.slf4j.Slf4jMDCAdapter;

public class Activator
    implements BundleActivator
{

    public void start( BundleContext bundleContext )
        throws Exception
    {
        org.ops4j.pax.logging.slf4j.Slf4jLoggerFactory.setBundleContext( bundleContext );
        String name = getClass().getName();
        org.slf4j.Logger slf4jLogger = org.slf4j.LoggerFactory.getLogger( name );
        Slf4jMDCAdapter.setBundleContext(bundleContext);
        slf4jLogger.info( "Enabling SLF4J API support." );
        org.apache.commons.logging.LogFactory.setBundleContext( bundleContext );
        org.apache.commons.logging.Log commonsLogger = org.apache.commons.logging.LogFactory.getLog( name );
        commonsLogger.info( "Enabling Jakarta Commons Logging API support." );
        org.apache.log4j.Logger.setBundleContext( bundleContext );
        org.apache.log4j.MDC.setBundleContext( bundleContext );
        org.apache.log4j.Logger log4jLogger = org.apache.log4j.Logger.getLogger( name );
        log4jLogger.info( "Enabling Log4J API support." );
        org.ops4j.pax.logging.avalon.AvalonLogFactory.setBundleContext( bundleContext );
        org.apache.avalon.framework.logger.Logger avalonLogger = org.ops4j.pax.logging.avalon.AvalonLogFactory.getLogger( name );
        avalonLogger.info( "Enabling Avalon Logger API support." );
        
        org.apache.juli.logging.LogFactory.setBundleContext( bundleContext );
        org.apache.juli.logging.Log juliLogger = org.apache.juli.logging.LogFactory.getLog( name );
        juliLogger.info( "Enabling JULI Logger API support." );
    }

    public void stop( BundleContext bundleContext )
        throws Exception
    {
        String name = getClass().getName();

        org.slf4j.Logger slf4jLogger = org.slf4j.LoggerFactory.getLogger( name );
        slf4jLogger.info( "Disabling SLF4J API support." );

        org.apache.commons.logging.Log commonsLogger = org.apache.commons.logging.LogFactory.getLog( name );
        commonsLogger.info( "Disabling Jakarta Commons Logging API support." );

        org.apache.log4j.Logger log4jLogger = org.apache.log4j.Logger.getLogger( name );
        log4jLogger.info( "Disabling Log4J API support." );

        org.apache.avalon.framework.logger.Logger avalonLogger = org.ops4j.pax.logging.avalon.AvalonLogFactory.getLogger( name );
        avalonLogger.info( "Disabling Avalon Logger API support." );

        org.apache.juli.logging.Log juliLogger = org.apache.juli.logging.LogFactory.getLog( name );
        juliLogger.info( "Disabling JULI Logger API support." );

        org.ops4j.pax.logging.slf4j.Slf4jLoggerFactory.dispose();
        Slf4jMDCAdapter.dispose();
        org.apache.commons.logging.LogFactory.dispose();
        org.apache.log4j.Logger.dispose();
        org.apache.log4j.MDC.dispose();
        org.ops4j.pax.logging.avalon.AvalonLogFactory.dispose();
        org.apache.juli.logging.LogFactory.dispose();
    }
}
