/*
 * Copyright 2006 Niclas Hedhman.
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
package org.ops4j.pax.logging.slf4j;

import java.util.Map;
import org.slf4j.spi.MDCAdapter;
import org.osgi.framework.BundleContext;
import org.ops4j.pax.logging.PaxContext;
import org.ops4j.pax.logging.PaxLoggingManager;
import org.ops4j.pax.logging.OSGIPaxLoggingManager;

public class Slf4jMDCAdapter
    implements MDCAdapter
{

    private static PaxContext m_context;
    private static PaxContext m_defaultContext = new PaxContext();

    private static PaxLoggingManager m_paxLogging;
    public static void setBundleContext( BundleContext ctx )
    {
        m_paxLogging = new OSGIPaxLoggingManager( ctx );
        // We need to instruct all loggers to ensure the SimplePaxLoggingManager is replaced.
        m_paxLogging.open();
    }

    /**
      * For all the methods that operate against the context, return true if the MDC should use the PaxContext object from the PaxLoggingManager,
      * or if the logging manager is not set, or does not have its context available yet, use a default context local to this MDC.
      * @return m_context if the MDC should use the PaxContext object from the PaxLoggingManager,
      * or m_defaultContext if the logging manager is not set, or does not have its context available yet.
      */
    private static PaxContext getContext(){
        if( m_paxLogging!=null ){
            m_context=(m_paxLogging.getPaxLoggingService()!=null)?m_paxLogging.getPaxLoggingService().getPaxContext():null;
        }
        return m_context!=null?m_context:m_defaultContext;
    }

    public void put( String key, String val )
    {
        getContext().put(key, val);
    }

    public String get( String key )
    {
		Object value = null;
        return ( (value = getContext().get(key) ) != null ) ? value.toString() : null;
    }

    public void remove( String key )
    {
        getContext().remove(key);
    }

    public void clear()
    {
        getContext().clear();
    }

    public Map getCopyOfContextMap()
    {
        return getContext().getCopyOfContextMap();
    }

    public void setContextMap( Map contextMap )
    {
        getContext().setContextMap(contextMap);
    }

    /** Pax Logging internal method. Should never be used directly. */
    public static void dispose()
    {
        m_paxLogging.close();
        m_paxLogging.dispose();
        m_paxLogging = null;
    }
}
