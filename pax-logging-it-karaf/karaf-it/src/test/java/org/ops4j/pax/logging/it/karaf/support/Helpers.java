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
package org.ops4j.pax.logging.it.karaf.support;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import static org.junit.Assert.assertTrue;

public class Helpers {

    /**
     * <p>Helper method that does several things:<ul>
     *     <li>gets current {@link org.ops4j.pax.logging} config</li>
     *     <li>registers {@link org.osgi.service.event.EventHandler} for configuration topic</li>
     *     <li>updates the configuration using passed consumer</li>
     *     <li>awaits for successful configuration change</li>
     * </ul></p>
     * <p>Simply - it synchronously changes logging configuration and waits for it to be effective.</p>
     * @param context
     * @param cm
     */
    public static void updateLoggingConfig(BundleContext context, ConfigurationAdmin cm, Consumer<Dictionary<String, Object>> consumer) {
        try {
            Configuration c = cm.getConfiguration("org.ops4j.pax.logging", "?");

            final CountDownLatch latch = new CountDownLatch(1);
            EventHandler handler = event -> {
                latch.countDown();
            };
            Dictionary<String, Object> props = new Hashtable<>();
            props.put(EventConstants.EVENT_TOPIC, "org/ops4j/pax/logging/Configuration");
            context.registerService(EventHandler.class, handler, props);

            Dictionary<String, Object> configuration = new Hashtable<>();

            if (consumer != null) {
                consumer.accept(configuration);
            }

            c.update(configuration);

            assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
