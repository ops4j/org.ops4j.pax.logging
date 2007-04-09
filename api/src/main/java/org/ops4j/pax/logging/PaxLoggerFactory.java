/*  Copyright 2007 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.logging;

import org.osgi.framework.BundleContext;

/** Native Pax Logging factory.
 *
 * Pax Logging is a lot about supporting legacy logging APIs in an OSGi environment. But that comes
 * with extra overhead, so for new code it is sometimes desirable to bypass as much overhead as
 * possible. This factory facilitates that need.
 *
 * Typical usage look like this;
 *
 * <pre><code>
 * public class MyClass
 * {
 *   private final PaxLogger logger;
 *
 *   public MyClass( BundleContext bc, ... )
 *   {
 *       logger = PaxLoggerFactory.getPaxLogger( bc, MyClass.class.getName() );
 *       :
 *   }
 *   :
 * }
 * </code></pre>
 *
 * Alternatively, accumulate the access to the Activator.
 *
 * <pre><code>
 * public class Activator
 *     implements BundleActivator
 * {
 *     private static BundleContext context;
 *
 *     public void start( BundleContext context )
 *     {
 *         this.context = context;
 *     }
 *  :
 *     public static PaxLogger getLogger( String category )
 *     {
 *         return PaxLoggerFactory.getPaxLogger( context, category );
 *     }
 * }
 *
 * public class MyClass
 * {
 *     private PaxLogger logger = Activator.getLogger( MyClass.class.getName() );
 *
 * }
 *
 * </code></pre>
 *
 */
public class PaxLoggerFactory
{
    public static PaxLogger getPaxLogger( BundleContext context, String category )
    {
        PaxLoggingManager paxLogging = new OSGIPaxLoggingManager( context );
        return paxLogging.getLogger( category );
    }
}
