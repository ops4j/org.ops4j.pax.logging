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

package org.apache.log4j;

import java.util.Hashtable;
import java.util.Map;

import org.ops4j.pax.logging.PaxContext;
import org.ops4j.pax.logging.PaxLoggingManager;
import org.ops4j.pax.logging.PaxLoggingService;

/**
 * The MDC class is similar to the {@link NDC} class except that it is based on a map instead of a stack. It provides
 * <em>mapped diagnostic contexts</em>. A
 * <em>Mapped Diagnostic Context</em>, or MDC in short, is an instrument for
 * distinguishing interleaved log output from different sources. Log output is typically interleaved when a server
 * handles multiple clients near-simultaneously.
 *
 * <p>
 * <b><em>The MDC is managed on a per thread basis</em></b>. A child thread
 * automatically inherits a <em>copy</em> of the mapped diagnostic context of its parent.
 *
 * <p>
 * The MDC class requires JDK 1.2 or above. Under JDK 1.1 the MDC will always return empty values but otherwise will not
 * affect or harm your application.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @since 1.2
 */
public class MDC {

    /** {@link PaxContext} used when {@link org.ops4j.pax.logging.PaxLoggingService} is not available */
    private static PaxContext m_defaultContext = new PaxContext();
    /** {@link PaxContext} obtained from {@link org.ops4j.pax.logging.PaxLoggingService} */
    private static PaxContext m_context;

    private MDC() {
    }

    /**
     * <p>For all the methods that use the context, default, static, {@link PaxContext} may be used (tied to pax-logging-api
     * bundle) if there's no available {@link PaxLoggingManager} or {@link PaxLoggingService}. If the service is
     * available, it is <strong>always</strong> used to get service specific {@link PaxContext}.</p>
     * <p>Refering <strong>always</strong> to {@link PaxLoggingService#getPaxContext()} is cheap operation, as it's
     * only reference to fields.</p>
     *
     * <p>See: https://ops4j1.jira.com/browse/PAXLOGGING-247</p>
     *
     * @return m_context if the MDC should use the PaxContext object from the PaxLoggingManager,
     *      or m_defaultContext if the logging manager is not set, or does not have its context available yet.
     */
    private static PaxContext getPaxContext() {
        PaxLoggingManager manager = Logger.m_paxLogging;
        if (manager != null) {
            synchronized (MDC.class) {
                PaxLoggingService service = manager.getPaxLoggingService();
                m_context = service != null ? service.getPaxContext() : null;
            }
        }
        return m_context != null ? m_context : m_defaultContext;
    }

    /**
     * Put a context value (the <code>o</code> parameter) as identified with the
     * <code>key</code> parameter into the current thread's context map.
     *
     * <p>
     * If the current thread does not have a context map it is created as a side effect.
     */
    static public void put(String key, Object o) {
        getPaxContext().put(key, o);
    }

    /**
     * Get the context identified by the <code>key</code> parameter.
     *
     * <p>
     * This method has no side effects.
     */
    static public Object get(String key) {
        return getPaxContext().get(key);
    }

    /**
     * Remove the the context identified by the <code>key</code> parameter.
     */
    static public void remove(String key) {
        getPaxContext().remove(key);
    }

    /**
     * Get the current thread's MDC as a hashtable. This method is intended to be used internally.
     *
     * pax-logging note: this method has different return type than original {@code org.apache.log4j.MDC}.
     * Originally this method returns {@link Hashtable}, but pax-logging's version
     * returned {@link Map} for very long time.
     */
    public static Map<?, ?> getContext() {
        return getPaxContext().getContext();
    }

    /**
     * Remove all values from the MDC.
     *
     * @since 1.2.16
     */
    public static void clear() {
        getPaxContext().clear();
    }

}
