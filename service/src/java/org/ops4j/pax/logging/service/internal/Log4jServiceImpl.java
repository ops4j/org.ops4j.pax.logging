/*
 * Copyright 2005 Makas Tzavellas.
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
package org.ops4j.pax.logging.service.internal;

import org.apache.log4j.Logger;
import org.ops4j.pax.logging.service.Log4JService;

/**
 * Default logger service using log4j underneath implementing the LogService
 * interface.
 */
public class Log4jServiceImpl
    implements Log4JService
{
    /**
     * Log4J logger
     */
    private Logger m_Logger;

    /**
     * Instantiates a Log4jService for a particular bundle requesting this
     * service instance.
     *
     * @param loggerName
     *            Bundle requesting this service instance.
     */
    public Log4jServiceImpl( String loggerName )
    {
        m_Logger = Logger.getLogger( loggerName );
    }

    /**
     * Removes all the resources held by this service instance.
     *
     */
    public void dispose()
    {
        m_Logger = null;
    }

    public org.ops4j.pax.logging.service.Logger getLogger(String category)
    {
        return new Logger4JImpl( m_Logger );
    }
}
