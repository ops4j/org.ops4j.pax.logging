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
package org.ops4j.pax.logging.it.support;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.ops4j.pax.logging.PaxLoggingConstants;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import static org.junit.Assert.assertTrue;

public class Helpers {

    private Helpers() {
    }

    public static Bundle paxLoggingApi(BundleContext context) {
        Optional<Bundle> paxLoggingApi = Arrays.stream(context.getBundles())
                .filter(b -> "org.ops4j.pax.logging.pax-logging-api".equals(b.getSymbolicName()))
                .findFirst();
        return paxLoggingApi.orElse(null);
    }

    public static void restartPaxLoggingApi(BundleContext context) throws BundleException {
        Bundle paxLoggingApi = paxLoggingApi(context);
        if (paxLoggingApi != null) {
            paxLoggingApi.stop(Bundle.STOP_TRANSIENT);
            paxLoggingApi.start(Bundle.STOP_TRANSIENT);
        }
    }

    public static void restartPaxLoggingService(BundleContext context, boolean await) {
        // restart pax-logging-service to pick up replaced stdout
        // awaits for signal indicating successfull (re)configuration
        Optional<Bundle> paxLoggingService = Arrays.stream(context.getBundles())
                .filter(b -> "org.ops4j.pax.logging.pax-logging-service".equals(b.getSymbolicName()))
                .findFirst();
        paxLoggingService.ifPresent(bundle -> {
            try {
                bundle.stop(Bundle.STOP_TRANSIENT);

                final CountDownLatch latch = new CountDownLatch(1);
                ServiceRegistration<EventHandler> sr = null;
                if (await) {
                    EventHandler handler = event -> {
                        latch.countDown();
                    };
                    Dictionary<String, Object> props = new Hashtable<>();
                    props.put(EventConstants.EVENT_TOPIC, PaxLoggingConstants.LOGGING_EVENT_ADMIN_CONFIGURATION_TOPIC);
                    sr = context.registerService(EventHandler.class, handler, props);
                }

                bundle.start();
                if (await) {
                    assertTrue(latch.await(5, TimeUnit.SECONDS));
                    sr.unregister();
                }
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });
    }

    public static void stopPaxLoggingService(BundleContext context) {
        Optional<Bundle> paxLoggingService = Arrays.stream(context.getBundles())
                .filter(b -> "org.ops4j.pax.logging.pax-logging-service".equals(b.getSymbolicName()))
                .findFirst();
        paxLoggingService.ifPresent(bundle -> {
            try {
                bundle.stop(Bundle.STOP_TRANSIENT);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });
    }

    /**
     * Uses reflection to get underlying field value.
     * @param object
     * @param fieldName
     * @return
     */
    public static Object getField(Object object, String fieldName) {
        Field f = null;
        try {
            f = object.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            try {
                f = object.getClass().getSuperclass().getDeclaredField(fieldName);
            } catch (NoSuchFieldException ex) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        f.setAccessible(true);
        try {
            return f.get(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Type-aware {@link #getField(Object, String)}
     * @param object
     * @param fieldName
     * @param clazz
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getField(Object object, String fieldName, Class<T> clazz) {
        return (T) getField(object, fieldName);
    }

    /**
     * Gets {@code m_delegate} used by loggers
     * @param object
     * @return
     */
    public static Object delegate(Object object) {
        return getField(object, "m_delegate");
    }

}
