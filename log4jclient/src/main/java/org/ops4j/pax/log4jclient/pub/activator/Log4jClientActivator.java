/*
 *   Copyright 2006 Pierre Parrand
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package org.ops4j.pax.log4jclient.pub.activator;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Log4jClientActivator
    implements BundleActivator
{

    public void start( BundleContext context )
    {
        System.out.println( "Bundle log4jClient started" );

        String demoLog = "demoLogger";
        Logger logger = Logger.getLogger( demoLog );
        logger.warn( "this is a warning" );
        logger.error( "this is an error" );

        String develLog = "develLogger";
        Logger logger2 = Logger.getLogger( develLog );
        logger2.warn( "this is a warning" );
        logger2.error( "this is an error" );

        String anonymousLog = "anonymousLogger";
        Logger logger3 = Logger.getLogger( anonymousLog );
        logger3.warn( "this is a warning" );
        logger3.error( "this is an error" );
    }

    public void stop( BundleContext context )
    {
        System.out.println( "Bundle log4jClient stopped" );
    }

}
