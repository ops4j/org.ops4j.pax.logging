/*  Copyright 2012 Guillaume Nodet.
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
package org.ops4j.pax.logging.extender;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;

import org.ops4j.pax.logging.PaxLoggingService;
import org.ops4j.pax.logging.spi.PaxErrorHandler;
import org.ops4j.pax.logging.spi.PaxFilter;
import org.ops4j.pax.logging.spi.PaxLayout;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    public void start(BundleContext context) throws Exception {
        context.registerService(PaxErrorHandler.class,
                                new OsgiErrorHandler(),
                                props(PaxLoggingService.ERRORHANDLER_NAME_PROPERTY, "errorhandler"));
        context.registerService(PaxFilter.class,
                                new OsgiFilter(),
                                props(PaxLoggingService.FILTER_NAME_PROPERTY, "filter"));
        context.registerService(PaxLayout.class,
                                new OsgiLayout(),
                                props(PaxLoggingService.LAYOUT_NAME_PROPERTY, "layout"));
    }

    public void stop(BundleContext context) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private Dictionary props(String name, String value) {
        Hashtable props = new Properties();
        props.put(name, value);
        return props;
    }
}
