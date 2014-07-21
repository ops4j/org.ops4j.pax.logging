/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package org.ops4j.pax.logging.log4j;

import java.util.Dictionary;
import java.util.Hashtable;

import org.ops4j.pax.logging.PaxLoggingService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.log.LogService;

/**
 *
 */
public class Activator implements BundleActivator {

    public static final String CONFIGURATION_PID = "org.ops4j.pax.logging";

    private static final String[] LOG_SERVICE_INTERFACE_NAMES = {
        LogService.class.getName(),
        org.knopflerfish.service.log.LogService.class.getName(),
        PaxLoggingService.class.getName(),
        ManagedService.class.getName()
    };

    private Log4jPaxLoggingService service;
    private ServiceRegistration<?> registration;

    @Override
    public void start(BundleContext context) throws Exception {
        try {
            startInternal(context);
        } catch (final Exception e) {
            e.printStackTrace();
            throw e;
        } catch (final Error e) {
            e.printStackTrace();
            throw e;
        } catch (final Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void startInternal(BundleContext context) {
        service = new Log4jPaxLoggingService(context);
        final Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(Constants.SERVICE_PID, CONFIGURATION_PID);
        registration = context.registerService(LOG_SERVICE_INTERFACE_NAMES, service, props);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        registration.unregister();
        registration = null;
        service = null;
    }
}
