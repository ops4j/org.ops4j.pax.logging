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
package org.ops4j.pax.logging.service.internal;

import org.ops4j.pax.logging.PaxLoggingService;
import org.ops4j.pax.logging.spi.PaxAppender;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;

public class PaxAppenderProxy extends ServiceTracker implements PaxAppender
{

    private volatile int count = -1;
    private Object[] appenders = null;

    public PaxAppenderProxy(BundleContext bundleContext, String name)
    {
        super( bundleContext, createFilter( bundleContext, name ), null);
    }

    public static Filter createFilter( BundleContext bundleContext, String name )
    {
        try
        {
            return bundleContext.createFilter(
                        "(&(" + Constants.OBJECTCLASS + "=" + PaxAppender.class.getName() + ")" +
                            "(" + PaxLoggingService.APPENDER_NAME_PROPERTY + "=" + name + "))");
        }
        catch (InvalidSyntaxException e)
        {
            throw new IllegalStateException("unable to create appender tracker", e);
        }
    }


    public void doAppend( PaxLoggingEvent event )
    {
        if (count != getTrackingCount()) {
            count = getTrackingCount();
            appenders = getServices();
        }
        if (appenders != null) {
            for (int i = 0; i < appenders.length; i++) {
                ((PaxAppender) appenders[i]).doAppend( event );
            }
        }
    }
}
