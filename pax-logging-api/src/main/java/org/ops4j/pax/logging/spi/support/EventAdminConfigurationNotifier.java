/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ops4j.pax.logging.spi.support;

import java.util.HashMap;
import java.util.Map;

import org.ops4j.pax.logging.PaxLoggingConstants;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventProperties;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventAdminConfigurationNotifier implements ConfigurationNotifier {

    public static Logger LOG = LoggerFactory.getLogger(EventAdminConfigurationNotifier.class);

    private ServiceTracker<EventAdmin, EventAdmin> tracker;

    public EventAdminConfigurationNotifier(BundleContext context) {
        tracker = new ServiceTracker<EventAdmin, EventAdmin>(context, EventAdmin.class, null);
        tracker.open();
    }

    @Override
    public void configurationDone() {
        EventAdmin ea = tracker.getService();
        if (ea != null) {
            LOG.info("Sending Event Admin nofification (configuration successful) to " + PaxLoggingConstants.EVENT_ADMIN_CONFIGURATION_TOPIC);
            ea.postEvent(new Event(PaxLoggingConstants.EVENT_ADMIN_CONFIGURATION_TOPIC, (Map<String, ?>)null));
        } else {
            LOG.info("Logging configuration changed. (Event Admin service unavailable - no notification sent).");
        }
    }

    @Override
    public void configurationError(Throwable t) {
        EventAdmin ea = tracker.getService();
        if (ea != null) {
            LOG.warn("Sending Event Admin nofification (configuration error) to " + PaxLoggingConstants.EVENT_ADMIN_CONFIGURATION_TOPIC);
            Map<String, Object> properties = new HashMap<>();
            properties.put("exception", t);
            ea.postEvent(new Event(PaxLoggingConstants.EVENT_ADMIN_CONFIGURATION_TOPIC, new EventProperties(properties)));
        } else {
            LOG.warn("Logging configuration problem. (Event Admin service unavailable - no notification sent).", t);
        }
    }

    @Override
    public void close() throws Exception {
        if (tracker != null) {
            tracker.close();
        }
    }

}
