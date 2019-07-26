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
import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpHandler;
import org.mortbay.http.HttpListener;
import org.mortbay.http.HttpServer;
import org.mortbay.http.SocketListener;
import org.mortbay.util.InetAddrPort;
import org.ops4j.pax.logging.avalon.AvalonLogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.LoggerFactory;

/**
 * This Activator starts up the Jetty server and enables port 8080, which serves a Hello,World message.
 *
 * Jetty 5.1 uses Jakarta Commons Logging, and we are showing that those logging statements will be passed to
 * the Pax Logging service, and ultimately output to the Log4J backend.
 */
public class Activator
    implements BundleActivator
{
    private HttpServer m_server;

    private Log m_jclLogger;
    private org.apache.juli.logging.Log m_juliLogger;
    private org.apache.avalon.framework.logger.Logger m_avalonLogger;
    private org.slf4j.Logger m_slf4jLogger;
    private java.util.logging.Logger m_jdkLogger;

    public void start( BundleContext bundleContext )
        throws Exception
    {
        m_jclLogger = LogFactory.getLog( Activator.class );
        m_juliLogger = org.apache.juli.logging.LogFactory.getLog( Activator.class );
        m_avalonLogger = AvalonLogFactory.getLogger( Activator.class.getName() );
        m_slf4jLogger = LoggerFactory.getLogger( Activator.class );
        m_jdkLogger = java.util.logging.Logger.getLogger( Activator.class.getName() );
        m_jclLogger.info(    "Starting Example...    (jcl)" );
        m_avalonLogger.info( "Starting Example...    (avalon)" );
        m_slf4jLogger.info(  "Starting Example...    (slf4j)" );
        m_jdkLogger.info(    "Starting Example...    (jdk)" );
        m_juliLogger.info(   "Starting Example...    (juli)" );

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
        m_jclLogger.info(    "Stopping Example...    (jcl)" );
        m_avalonLogger.info( "Stopping Example...    (avalon)" );
        m_slf4jLogger.info(  "Stopping Example...    (slf4j)");
        m_jdkLogger.info(    "Stopping Example...    (jdk)");
        m_juliLogger.info(   "Stopping Example...    (juli)");
        m_server.stop();
    }
}
