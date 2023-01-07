/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.logging;

import java.util.HashMap;
import java.util.Map;

/**
 * The MDC class that provides <em>mapped diagnostic contexts</em>.
 *
 * A <em>Mapped Diagnostic Context</em>, or MDC in short, is an instrument for distinguishing
 * interleaved log output from different sources. Log output is typically interleaved
 * when a server handles multiple clients near-simultaneously.
 *
 * <b><em>The MDC is managed on a per thread basis</em></b>. A child thread automatically
 * inherits a <em>copy</em> of the mapped diagnostic context of its parent (with normal
 * {@link InheritableThreadLocal} child inherits the same references). That behavior can be switched 
 * off by setting the system property <em>org.ops4j.pax.logging.threadContextMapInheritable</em> to
 * <em>false</em> (default is <em>true</em>).
 *
 * The MDC class requires JDK 1.2 or above. Under JDK 1.1 the MDC will always return empty
 * values but otherwise will not affect or harm your application.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @since 1.2
 */
public class PaxContext {

    static final int HT_SIZE = 7;

    final ThreadLocal<Map<String, Object>> tlm;

    public PaxContext() {
        if (Boolean.parseBoolean(System.getProperty(PaxLoggingConstants.LOGGING_CFG_INHERIT_THREAD_CONTEXT_MAP, "true"))) {
            tlm = new ThreadLocalMap();
        } else {
            tlm = new ThreadLocal<>();
        }
    }

    /**
     * Sets all values from passed map in this thread-bound MDC context.
     * This methods preserves existing values.
     * @param context
     */
    public void putAll(Map<String, Object> context) {
        Map<String, Object> ht = tlm.get();
        if (ht == null) {
            ht = new HashMap<String, Object>(HT_SIZE);
            tlm.set(ht);
        }
        ht.putAll(context);
    }

    /**
     * Sets single value in this thread-bound MDC context.
     * @param key
     * @param o
     */
    public void put(String key, Object o) {
        Map<String, Object> ht = tlm.get();
        if (ht == null) {
            ht = new HashMap<String, Object>(HT_SIZE);
            tlm.set(ht);
        }
        ht.put(key, o);
    }

    /**
     * Gets a value from this thread-bound MDC context.
     * @param key
     * @return
     */
    public Object get(String key) {
        Map<String, Object> ht = tlm.get();
        if (ht != null && key != null) {
            return ht.get(key);
        } else {
            return null;
        }
    }

    /**
     * Removes a value from this thread-bound MDC context.
     * @param key
     */
    public void remove(String key) {
        Map<String, Object> ht = tlm.get();
        if (ht != null) {
            ht.remove(key);
        }
    }

    /**
     * Gets full map of values related to this thread-bound MDC context.
     * @return
     */
    public Map<String, Object> getContext() {
        return tlm.get();
    }

    /**
     * Gets a copy of full map of calues related to this thread-bound MDC context.
     * @return
     */
    public Map<String, Object> getCopyOfContextMap() {
        Map<String, Object> ht = tlm.get();
        if (ht != null) {
            return new HashMap<>(ht);
        } else {
            return null;
        }
    }

    /**
     * Clears entire thread-bound MDC context.
     */
    public void clear() {
        Map<String, Object> ht = tlm.get();
        if (ht != null) {
            ht.clear();
        }
    }

    /**
     * Sets all values from passed map in this thread-bound MDC context.
     * This methods always clears existing values first.
     * @param contextMap
     */
    public void setContextMap(Map<String, Object> contextMap) {
        Map<String, Object> ht = tlm.get();
        if (ht == null) {
            ht = new HashMap<String, Object>(HT_SIZE);
            tlm.set(ht);
        } else {
            ht.clear();
        }
        ht.putAll(contextMap);
    }

    /**
     * Version of {@link InheritableThreadLocal} that creates a copy of parent thread's map before
     * starting child thread. That's why we may use not-synchronized version of {@link Map}.
     */
    static class ThreadLocalMap extends InheritableThreadLocal<Map<String, Object>> {

        @Override
        protected Map<String, Object> childValue(Map<String, Object> parentValue) {
            if (parentValue != null) {
                return new HashMap<String, Object>(parentValue);
            } else {
                return null;
            }
        }
    }

}
