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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpException;
import org.mortbay.http.HttpHandler;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.apache.log4j.Logger;

/** This is a pure test class, which is handles the requests on port 8080 in this example.
 *
 * Jetty 4.2 used Jakarta Commons Logging, and we are showing that in combination with Log4J
 * usage in this particular class.
 */
public class TestHandler
    implements HttpHandler
{
    private Logger m_Logger = Logger.getLogger( TestHandler.class );
    private java.util.logging.Logger m_JdkLogger = java.util.logging.Logger.getLogger( TestHandler.class.getName() );

    private HttpContext m_Context;
    private String m_Name;
    private boolean m_started;

    public TestHandler( String name )
    {
        m_Logger.info( "Creating TestHandler." );
        m_Name = name;
    }

    public String getName()
    {
        return m_Name;
    }

    public HttpContext getHttpContext()
    {
        return m_Context;
    }

    public void initialize( HttpContext httpContext )
    {
        m_Logger.info( "Initializing the TestHandler with a HttpContext: " + httpContext  + "  (log4j)");
        m_JdkLogger.info( "Initializing the TestHandler with a HttpContext: " + httpContext  + "  (jdk)");
        m_Context = httpContext;
    }

    public void handle( String string, String string1, HttpRequest httpRequest, HttpResponse httpResponse )
        throws HttpException, IOException
    {
        m_Logger.info( "Processing Request.  (log4j)" );
        m_JdkLogger.info( "Processing Request.  (jdk)" );
        httpResponse.setContentType( "text/html" );
        OutputStream outputStream = httpResponse.getOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter( outputStream, "UTF-8" );
        PrintWriter out = new PrintWriter( osw );
        out.print( "<html><body><h1>Hello, World!</h1></body></html>" );
        out.flush();
        httpRequest.setHandled( true );
    }

    public void start()
        throws Exception
    {
        m_Logger.info( "Starting.  (log4j)" );
        m_JdkLogger.info( "Starting.  (jdk)" );
        m_started = true;
    }

    public void stop()
        throws InterruptedException
    {
        m_Logger.info( "Stopping.  (log4j)" );
        m_JdkLogger.info( "Stopping.  (jdk)" );
        m_started = false;
    }

    public boolean isStarted()
    {
        return m_started;
    }
}
