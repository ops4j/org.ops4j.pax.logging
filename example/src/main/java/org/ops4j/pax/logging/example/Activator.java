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
package org.ops4j.pax.logging.example;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpHandler;
import org.mortbay.http.HttpListener;
import org.mortbay.http.HttpServer;
import org.mortbay.http.SocketListener;
import org.mortbay.util.InetAddrPort;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.ops4j.pax.logging.avalon.AvalonLogFactory;

/**
 * This Activator starts up the Jetty server and enables port 8080, which serves a Hello,World message.
 *
 * Jetty 5.1 uses Jakarta Commons Logging, and we are showing that those logging statements will be passed to
 * the Pax Logging service, and ultimately output to the Log4J backend.
 *
 */
public class Activator
    implements BundleActivator
{
    private Log m_logger;
    private org.apache.avalon.framework.logger.Logger m_AvalonLogger;

    private HttpServer m_server;

    public void start( BundleContext bundleContext )
        throws Exception
    {
        Logger.setBundleContext( bundleContext );
        LogFactory.setBundleContext( bundleContext );
        AvalonLogFactory.setBundleContext( bundleContext );
        m_logger = LogFactory.getLog( Activator.class );
        m_AvalonLogger = AvalonLogFactory.getLogger( Activator.class.getName() );

        m_logger.info( "Starting Example...    (jcl)" );
        m_AvalonLogger.info( "Starting Example...    (avalon)" );

        HttpHandler handler = new TestHandler( "test" );
        InetAddrPort port = new InetAddrPort( 8080 );
        HttpListener listener = new SocketListener( port );
        m_server = new HttpServer();
        HttpContext context = new HttpContext();
        context.setContextPath( "/" );
        context.addHandler( handler );
        m_server.addContext( context );
        m_server.addListener( listener );
        m_server.start();
    }

    public void stop( BundleContext bundleContext )
        throws Exception
    {
        m_logger.info( "Stopping Example...    (jcl)" );
        m_AvalonLogger.info( "Stopping Example...    (avalon)" );
        m_server.stop();
        Logger.release();
        LogFactory.release();
    }
}
