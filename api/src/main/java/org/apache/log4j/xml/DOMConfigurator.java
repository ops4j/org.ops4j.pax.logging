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
package org.apache.log4j.xml;

import org.w3c.dom.Element;
import javax.xml.parsers.FactoryConfigurationError;
import java.net.URL;

/** Dummy class to allow more applications to be compatible with Pax Logging out of the box.
 *
 * Configuration requests will be ignored and the centralized configuration of Pax Logging Service
 * will always take effect.
 */

public class DOMConfigurator
{
    public DOMConfigurator()
    {
    }

    public static void configure( Element element )
    {
        // do nothing, ignore
    }

    public static void configureAndWatch( String filename )
    {
        // do nothing, ignore
    }

    public static void configureAndWatch( String filename, long period )
    {
        // do nothing, ignore
    }

    public static void configure( String filename )
        throws FactoryConfigurationError
    {
        // do nothing, ignore
    }

    public static void configure( URL url )
        throws FactoryConfigurationError
    {
        // do nothing, ignore
    }

}
