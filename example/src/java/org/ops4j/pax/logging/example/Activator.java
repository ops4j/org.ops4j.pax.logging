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
import org.ops4j.pax.logging.providers.PaxLoggingProvider;
import org.ops4j.pax.logging.providers.LogProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

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
    private HttpServer m_server;

    public void start( BundleContext bundleContext )
        throws Exception
    {
        LogProvider provider = new PaxLoggingProvider( bundleContext );
        LogFactory.getFactory().setLogProvider( provider );
        m_logger = LogFactory.getLog( Activator.class );
        m_logger.info( "Starting Example..." );

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
        m_logger.info( "Stopping Example..." );
        m_server.stop();
    }
}
