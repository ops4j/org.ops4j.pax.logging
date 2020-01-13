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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

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
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

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

    public static Bundle paxLoggingLog4j1(BundleContext context) {
        Optional<Bundle> paxLoggingLog4j1 = Arrays.stream(context.getBundles())
                .filter(b -> "org.ops4j.pax.logging.pax-logging-log4j1".equals(b.getSymbolicName()))
                .findFirst();
        return paxLoggingLog4j1.orElse(null);
    }

    public static Bundle paxLoggingLogback(BundleContext context) {
        Optional<Bundle> paxLoggingLogback = Arrays.stream(context.getBundles())
                .filter(b -> "org.ops4j.pax.logging.pax-logging-logback".equals(b.getSymbolicName()))
                .findFirst();
        return paxLoggingLogback.orElse(null);
    }

    public static Bundle paxLoggingLog4j2(BundleContext context) {
        Optional<Bundle> paxLoggingLog4j2 = Arrays.stream(context.getBundles())
                .filter(b -> "org.ops4j.pax.logging.pax-logging-log4j2".equals(b.getSymbolicName()))
                .findFirst();
        return paxLoggingLog4j2.orElse(null);
    }

    public static void restartPaxLoggingApi(BundleContext context) throws BundleException {
        Bundle paxLoggingApi = paxLoggingApi(context);
        if (paxLoggingApi != null) {
            paxLoggingApi.stop(Bundle.STOP_TRANSIENT);
            paxLoggingApi.start(Bundle.START_TRANSIENT);
        }
    }

    public static void restartPaxLoggingLog4j1(BundleContext context, boolean await) {
        // restart pax-logging-log4j1 to pick up replaced stdout
        // awaits for signal indicating successfull (re)configuration
        Bundle paxLoggingLog4j1 = paxLoggingLog4j1(context);
        if (paxLoggingLog4j1 != null) {
            try {
                paxLoggingLog4j1.stop(Bundle.STOP_TRANSIENT);

                final CountDownLatch latch = new CountDownLatch(1);
                ServiceRegistration<EventHandler> sr = null;
                if (await) {
                    EventHandler handler = event -> {
                        latch.countDown();
                    };
                    Dictionary<String, Object> props = new Hashtable<>();
                    props.put(EventConstants.EVENT_TOPIC, PaxLoggingConstants.EVENT_ADMIN_CONFIGURATION_TOPIC);
                    sr = context.registerService(EventHandler.class, handler, props);
                }

                paxLoggingLog4j1.start();
                if (await) {
                    assertTrue(latch.await(5, TimeUnit.SECONDS));
                    sr.unregister();
                }
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    public static void restartPaxLoggingLogback(BundleContext context, boolean await) {
        // restart pax-logging-logback to pick up replaced stdout
        // awaits for signal indicating successfull (re)configuration
        Bundle paxLoggingLogback = paxLoggingLogback(context);
        if (paxLoggingLogback != null) {
            try {
                paxLoggingLogback.stop(Bundle.STOP_TRANSIENT);

                final CountDownLatch latch = new CountDownLatch(1);
                ServiceRegistration<EventHandler> sr = null;
                if (await) {
                    EventHandler handler = event -> {
                        latch.countDown();
                    };
                    Dictionary<String, Object> props = new Hashtable<>();
                    props.put(EventConstants.EVENT_TOPIC, PaxLoggingConstants.EVENT_ADMIN_CONFIGURATION_TOPIC);
                    sr = context.registerService(EventHandler.class, handler, props);
                }

                paxLoggingLogback.start();
                if (await) {
                    assertTrue(latch.await(5, TimeUnit.SECONDS));
                    sr.unregister();
                }
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    public static void restartPaxLoggingLog4j2(BundleContext context, boolean await) {
        // restart pax-logging-log4j2 to pick up replaced stdout
        // awaits for signal indicating successfull (re)configuration
        Bundle paxLoggingLog4j2 = paxLoggingLog4j2(context);
        if (paxLoggingLog4j2 != null) {
            try {
                paxLoggingLog4j2.stop(Bundle.STOP_TRANSIENT);

                final CountDownLatch latch = new CountDownLatch(1);
                ServiceRegistration<EventHandler> sr = null;
                if (await) {
                    EventHandler handler = event -> {
                        latch.countDown();
                    };
                    Dictionary<String, Object> props = new Hashtable<>();
                    props.put(EventConstants.EVENT_TOPIC, PaxLoggingConstants.EVENT_ADMIN_CONFIGURATION_TOPIC);
                    sr = context.registerService(EventHandler.class, handler, props);
                }

                paxLoggingLog4j2.start();
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
     * {@link #updateLoggingConfig(BundleContext, ConfigurationAdmin, Helpers.LoggingLibrary, String, Consumer)} without
     * any properties processing callback.
     *
     * @param context
     * @param cm
     * @param library
     * @param prefix
     */
    public static void updateLoggingConfig(BundleContext context, ConfigurationAdmin cm, LoggingLibrary library, String prefix) {
        updateLoggingConfig(context, cm, library, prefix, null);
    }

    /**
     * Helper method that does several things:
     * <ul>
     *     <li>gets current {@link org.ops4j.pax.logging.PaxLoggingConstants#LOGGING_CONFIGURATION_PID} config</li>
     *     <li>reads single properties configuration file and extracts properties prefixed with {@code prefix}</li>
     *     <li>registers {@link org.osgi.service.event.EventHandler} for configuration topic</li>
     *     <li>updates the configuration</li>
     *     <li>awaits for successful configuration change</li>
     * </ul>
     *
     * Simply - it synchronously changes logging configuration and waits for it to be effective.
     *
     * @param context
     * @param cm
     * @param library
     * @param prefix
     */
    public static void updateLoggingConfig(BundleContext context, ConfigurationAdmin cm, LoggingLibrary library, String prefix, Consumer<Dictionary<String, Object>> consumer) {
        final Throwable[] pt = new Throwable[1];
        ServiceRegistration<EventHandler> reg = null;
        try {
            Configuration c = cm.getConfiguration(PaxLoggingConstants.LOGGING_CONFIGURATION_PID, "?");

            final CountDownLatch latch = new CountDownLatch(1);
            EventHandler handler = event -> {
                if (event.containsProperty("exception")) {
                    pt[0] = (Throwable) event.getProperty("exception");
                }
                latch.countDown();
            };
            Dictionary<String, Object> props = new Hashtable<>();
            props.put(EventConstants.EVENT_TOPIC, PaxLoggingConstants.EVENT_ADMIN_CONFIGURATION_TOPIC);
            reg = context.registerService(EventHandler.class, handler, props);

            Dictionary<String, Object> configuration = readPrefixedProperties(library, prefix);

            if (consumer != null) {
                consumer.accept(configuration);
            }

            c.update(configuration);

            assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (reg != null) {
                reg.unregister();
            }
        }

        if (pt[0] != null) {
            throw new RuntimeException("Configuration problem", pt[0]);
        }
    }

    /**
     * Synchronized method that simply deletes any {@code org.ops4j.pax.logging} configuration
     * @param context
     * @param cm
     */
    public static void deleteLoggingConfig(BundleContext context, ConfigurationAdmin cm) {
        final Throwable[] pt = new Throwable[1];
        ServiceRegistration<EventHandler> reg = null;
        try {
            Configuration c = cm.getConfiguration(PaxLoggingConstants.LOGGING_CONFIGURATION_PID, "?");

            final CountDownLatch latch = new CountDownLatch(1);
            EventHandler handler = event -> {
                if (event.containsProperty("exception")) {
                    pt[0] = (Throwable) event.getProperty("exception");
                }
                latch.countDown();
            };
            Dictionary<String, Object> props = new Hashtable<>();
            props.put(EventConstants.EVENT_TOPIC, PaxLoggingConstants.EVENT_ADMIN_CONFIGURATION_TOPIC);
            reg = context.registerService(EventHandler.class, handler, props);

            c.delete();

            assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (reg != null) {
                reg.unregister();
            }
        }

        if (pt[0] != null) {
            throw new RuntimeException("Configuration problem", pt[0]);
        }
    }

    /**
     * It's better (IMO) to keep all the properties in single file and just prefix them for each test
     * @param library
     * @param prefix
     * @return
     */
    public static Dictionary<String, Object> readPrefixedProperties(LoggingLibrary library, String prefix) throws IOException {
        if (library.supportsProperties()) {
            // properties
            Properties props = new Properties();
            props.load(AbstractControlledIntegrationTestBase.class.getResourceAsStream(library.config()));

            Dictionary<String, Object> newProperties = new Hashtable<>();

            for (String key : props.stringPropertyNames()) {
                if (key.startsWith(prefix + ".")) {
                    newProperties.put(key.substring(prefix.length() + 1), props.getProperty(key));
                }
            }

            return newProperties;
        } else if (library == LoggingLibrary.LOG4J2_XML) {
            // XML - we have to store it into some file
            InputSource is = new InputSource(AbstractControlledIntegrationTestBase.class.getResourceAsStream(library.config()));
            XPath xp = XPathFactory.newInstance().newXPath();
            try {
                NodeList ns = (NodeList) xp.evaluate("/all-configurations/Configuration[@id='" + prefix + "']", is, XPathConstants.NODESET);
                new File("target/xml").mkdirs();
                String name = "target/xml/" + UUID.randomUUID().toString() + ".xml";
                TransformerFactory.newInstance().newTransformer()
                        .transform(new DOMSource(ns.item(0)), new StreamResult(new FileOutputStream(name)));
                Dictionary<String, Object> newProperties = new Hashtable<>();
                newProperties.put(PaxLoggingConstants.PID_CFG_LOG4J2_CONFIG_FILE, name);

                return newProperties;
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        } else if (library == LoggingLibrary.LOGBACK) {
            // XML - we have to store it into some file
            InputSource is = new InputSource(AbstractControlledIntegrationTestBase.class.getResourceAsStream(library.config()));
            XPath xp = XPathFactory.newInstance().newXPath();
            try {
                NodeList ns = (NodeList) xp.evaluate("/all-configurations/configuration[@id='" + prefix + "']", is, XPathConstants.NODESET);
                new File("target/xml").mkdirs();
                String name = "target/xml/" + UUID.randomUUID().toString() + ".xml";
                TransformerFactory.newInstance().newTransformer()
                        .transform(new DOMSource(ns.item(0)), new StreamResult(new FileOutputStream(name)));
                Dictionary<String, Object> newProperties = new Hashtable<>();
                newProperties.put(PaxLoggingConstants.PID_CFG_LOGBACK_CONFIG_FILE, name);

                return newProperties;
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * Do some action that should lead to "configuration done" EventAdmin event
     * @param waits
     * @param context
     * @param action
     * @throws InterruptedException
     */
    public static void awaitingConfigurationDone(int waits, BundleContext context, Runnable action) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(waits);
        EventHandler handler = event -> {
            latch.countDown();
        };
        Dictionary<String, Object> props = new Hashtable<>();
        props.put(EventConstants.EVENT_TOPIC, PaxLoggingConstants.EVENT_ADMIN_CONFIGURATION_TOPIC);
        ServiceRegistration<EventHandler> reg = context.registerService(EventHandler.class, handler, props);

        action.run();

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        reg.unregister();
    }

    public enum LoggingLibrary {
        LOG4J1("log4j-all.properties", true),
        LOG4J2_PROPERTIES("log4j2-all.properties", true),
        LOG4J2_XML("log4j2-all.xml", false),
        LOG4J2_JSON("log4j2-all.json", false),
        LOG4J2_YAML("log4j2-all.yaml", false),
        LOGBACK("logback-all.xml", false);

        private final String propertiesFile;
        private final boolean properties;

        LoggingLibrary(String propertiesFile, boolean supportsProperties) {
            this.propertiesFile = propertiesFile;
            this.properties = supportsProperties;
        }

        public String config() {
            return this.propertiesFile;
        }

        public boolean supportsProperties() {
            return properties;
        }
    }

}
