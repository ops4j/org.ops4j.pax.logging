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

import org.apache.log4j.PropertyConfigurator;

import java.util.Properties;

/**
 * Implementation of the ConfigFactory interface
 */
public class ConfigFactoryImpl
    implements ConfigFactory
{
    /**
     * @see org.ops4j.pax.log4j.ConfigFactory#configure(Properties)
     */
    public void configure( Properties prop )
    {
        PropertyConfigurator.configure( prop );
    }
}
