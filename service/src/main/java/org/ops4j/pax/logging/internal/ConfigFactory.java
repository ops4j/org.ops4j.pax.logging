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
package org.ops4j.pax.logging.internal;

import java.util.Properties;

/**
 * Separating out the configuration class for more detailed testing.
 *
 * TODO Currently this class has two methods, which to some extent expose the underlying implementations.
 * TODO We probably need to encapsulate the Properties/XML config/Filenames/etc. into a LogConfig interface,
 * TODO with different implementations depending on the logging framework and config type
 */
public interface ConfigFactory
{

    /**
     * Expects a "merged" log4j configuration properties.
     *
     * @param prop log4j configuration properties.
     */
    void configure( Properties prop );

    /**
     * Expects log4j configuration filename.
     * @param filename log4j Xml configuration filename.
     */
    void configureXml( String filename );
}
