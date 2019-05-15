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

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.ops4j.pax.logging.PaxLoggingConstants;
import org.ops4j.pax.logging.it.AbstractControlledIntegrationTestBase;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
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

    public static Bundle paxLoggingService(BundleContext context) {
        Optional<Bundle> paxLoggingService = Arrays.stream(context.getBundles())
                .filter(b -> "org.ops4j.pax.logging.pax-logging-service".equals(b.getSymbolicName()))
                .findFirst();
        return paxLoggingService.orElse(null);
    }

    public static void restartPaxLoggingApi(BundleContext context) throws BundleException {
        Bundle paxLoggingApi = paxLoggingApi(context);
        if (paxLoggingApi != null) {
            paxLoggingApi.stop(Bundle.STOP_TRANSIENT);
            paxLoggingApi.start(Bundle.START_TRANSIENT);
        }
    }

    public static void restartPaxLoggingService(BundleContext context, boolean await) {
        // restart pax-logging-service to pick up replaced stdout
        // awaits for signal indicating successfull (re)configuration
        Bundle paxLoggingService = paxLoggingService(context);
        if (paxLoggingService != null) {
            try {
                paxLoggingService.stop(Bundle.STOP_TRANSIENT);

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

                paxLoggingService.start();
                if (await) {
                    assertTrue(latch.await(5, TimeUnit.SECONDS));
                    sr.unregister();
                }
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    /**
     * Uses reflection to set internal field value.
     * @param object
     * @param fieldName
     * @param value
     */
    public static void setField(Object object, String fieldName, Object value) {
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
            f.set(object, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Uses reflection to get underlying field value.
     * @param object
     * @param fieldName
     * @return
     */
    public static Object getField(Object object, String fieldName) {
        String[] names = fieldName.split("\\.");
        for (String name : names) {
            Field f = null;
            try {
                f = object.getClass().getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                try {
                    f = object.getClass().getSuperclass().getDeclaredField(name);
                } catch (NoSuchFieldException ex) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
            f.setAccessible(true);
            try {
                object = f.get(object);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        return object;
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


    /**
     * <p>{@link #updateLoggingConfig(BundleContext, ConfigurationAdmin, Helpers.LoggingLibrary, String, Consumer)} without
     * any properties processing callback.</p>
     * @param context
     * @param cm
     * @param library
     * @param prefix
     */
    public static void updateLoggingConfig(BundleContext context, ConfigurationAdmin cm, LoggingLibrary library, String prefix) {
        updateLoggingConfig(context, cm, library, prefix, null);
    }

    /**
     * <p>Helper method that does several things:<ul>
     *     <li>gets current {@link org.ops4j.pax.logging.PaxLoggingConstants#LOGGING_CONFIGURATION_PID} config</li>
     *     <li>reads single properties configuration file and extracts properties prefixed with {@code prefix}</li>
     *     <li>registers {@link org.osgi.service.event.EventHandler} for configuration topic</li>
     *     <li>updates the configuration</li>
     *     <li>awaits for successful configuration change</li>
     * </ul></p>
     * <p>Simply - it synchronously changes logging configuration and waits for it to be effective.</p>
     * @param context
     * @param cm
     * @param library
     * @param prefix
     */
    public static void updateLoggingConfig(BundleContext context, ConfigurationAdmin cm, LoggingLibrary library, String prefix, Consumer<Dictionary<String, Object>> consumer) {
        try {
            Configuration c = cm.getConfiguration(PaxLoggingConstants.LOGGING_CONFIGURATION_PID, null);

            final CountDownLatch latch = new CountDownLatch(1);
            EventHandler handler = event -> {
                latch.countDown();
            };
            Dictionary<String, Object> props = new Hashtable<>();
            props.put(EventConstants.EVENT_TOPIC, PaxLoggingConstants.LOGGING_EVENT_ADMIN_CONFIGURATION_TOPIC);
            context.registerService(EventHandler.class, handler, props);

            Dictionary<String, Object> configuration = readPrefixedProperties(library, prefix);

            if (consumer != null) {
                consumer.accept(configuration);
            }

            c.update(configuration);

            assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * It's better (IMO) to keep all the properties in single file and just prefix them for each test
     * @param library
     * @param prefix
     * @return
     */
    public static Dictionary<String, Object> readPrefixedProperties(LoggingLibrary library, String prefix) throws IOException {
        Properties props = new Properties();
        props.load(AbstractControlledIntegrationTestBase.class.getResourceAsStream(library.config()));

        Dictionary<String, Object> newProperties = new Hashtable<>();

        for (String key : props.stringPropertyNames()) {
            if (key.startsWith(prefix + ".")) {
                newProperties.put(key.substring(prefix.length() + 1), props.getProperty(key));
            }
        }

        return newProperties;
    }

    public enum LoggingLibrary {
        LOG4J1("log4j-all.properties"),
        LOG4J2(""),
        LOGBACK("");

        private final String propertiesFile;

        LoggingLibrary(String propertiesFile) {
            this.propertiesFile = propertiesFile;
        }

        public String config() {
            return this.propertiesFile;
        }
    }

}
