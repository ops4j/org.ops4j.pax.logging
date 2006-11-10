/*
 * Copyright 2005 Makas Tzavellas.
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

import java.io.IOException;
import java.util.Dictionary;

import org.apache.log4j.Logger;
import org.ops4j.pax.logging.PaxLoggingService;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

/**
 * ServiceFactory implementation to return LogService implementation instances.
 */
public class LoggingServiceFactory
    implements ServiceFactory
{

    /**
     * System PaxLogger
     */
    private static final Logger m_SystemLogger;

    private Object m_PaxLogging;
    private LoggingServiceConfiguration m_ConfigFactory;

    static
    {
        m_SystemLogger = Logger.getLogger( "pax.logging.system" );
    }

    /**
     * Constructor
     *
     * @param config the Configuration Factory to use
     */
    public LoggingServiceFactory( LoggingServiceConfiguration config, PaxLoggingService paxLogging  )
    {
        m_PaxLogging = paxLogging;
        m_ConfigFactory = config;
    }

    /**
     * @see org.osgi.framework.ServiceFactory#getService(org.osgi.framework.Bundle,
     *      org.osgi.framework.ServiceRegistration)
     */
    public Object getService( Bundle bundle, ServiceRegistration registration )
    {
        Dictionary dictionary = bundle.getHeaders();
        String loggerName = (String) dictionary.get( LoggingServiceConfiguration.LOG4J_LOGGER_NAME );
        if( loggerName != null )
        {
            String configFileName = (String) dictionary.get( LoggingServiceConfiguration.LOG4J_CONFIG_FILE );
            try
            {
                m_ConfigFactory.mergeProperties( bundle, loggerName, configFileName );
            } catch( IOException e )
            {
                m_SystemLogger.error(
                    "Can not read Log4J configuration " + configFileName + " from bundle for '" + loggerName + "'.", e
                );
            }
        }
        return m_PaxLogging;
    }

    /**
     * Disposes the LogService instance.
     *
     * @see org.osgi.framework.ServiceFactory#ungetService(org.osgi.framework.Bundle,
     *      org.osgi.framework.ServiceRegistration, java.lang.Object)
     */
    public void ungetService( Bundle bundle, ServiceRegistration registration, Object service )
    {
    }

}
