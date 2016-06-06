/*
 * Copyright 2009 Niclas Hedhman.
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

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.osgi.framework.Bundle;
import org.ops4j.pax.logging.PaxLogger;

/** This factory creates the fallback strategy when Pax Logging Service is not available.
 *
 */
public class FallbackLogFactory
{
    
    private FallbackLogFactory() 
    {
    }
    
    public static PaxLogger createFallbackLog( Bundle bundle, String categoryName )
    {
        if( isBuffering() )
        {
            return new BufferingLog( bundle, categoryName );
        }
        else
        {
            return new DefaultServiceLog( bundle, categoryName );
        }
    }

    private static boolean isBuffering()
    {
        if (System.getSecurityManager() != null)
        {
            return AccessController.doPrivileged(
                    new PrivilegedAction<Boolean>()
                    {
                        public Boolean run()
                        {
                            return Boolean.getBoolean( "org.ops4j.pax.logging.useBufferingLogFallback" );
                        }
                    }
            );
        }
        else
        {
            return Boolean.getBoolean( "org.ops4j.pax.logging.useBufferingLogFallback" );
        }
    }
}
