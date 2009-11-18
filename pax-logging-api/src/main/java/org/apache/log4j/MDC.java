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

import java.util.Map;
import org.ops4j.pax.logging.OSGIPaxLoggingManager;
import org.ops4j.pax.logging.PaxContext;
import org.ops4j.pax.logging.PaxLoggingManager;
import org.osgi.framework.BundleContext;

/**
 *  Wrap the PaxContext with MDC api.  PaxContext is derived from original MDC.
 */
public class MDC {

    private static PaxContext m_context;
    private static PaxContext m_defaultContext = new PaxContext();
    private static PaxLoggingManager m_paxLogging;

    public static void setBundleContext(BundleContext ctx) {
        m_paxLogging = new OSGIPaxLoggingManager(ctx);
        // We need to instruct all loggers to ensure the SimplePaxLoggingManager is replaced.
        m_paxLogging.open();
    }

    /**
     * For all the methods that operate against the context, return true if the MDC should use the PaxContext object ffrom the PaxLoggingManager,
     * or if the logging manager is not set, or does not have its context available yet, use a default context local to this MDC.
     * @return true if the MDC should use the PaxContext object ffrom the PaxLoggingManager,
     * or if the logging manager is not set, or does not have its context available yet, use a default context local to this MDC.
     */
    private static boolean setContext() {
        if (m_context == null && m_paxLogging != null) {
            m_context = (m_paxLogging.getPaxLoggingService() != null) ? m_paxLogging.getPaxLoggingService().getPaxContext() : null;
        }
        return m_context != null;
    }

    /**
    Put a context value (the <code>o</code> parameter) as identified
    with the <code>key</code> parameter into the current thread's
    context map.
    
    <p>If the current thread does not have a context map it is
    created as a side effect.
    
     */
    static public void put(String key, Object o) {
        if (setContext()) {
            m_context.put(key, o);
        } else {
            m_defaultContext.put(key, o);
        }
    }

    /**
    Get the context identified by the <code>key</code> parameter.
    
    <p>This method has no side effects.
     */
    static public Object get(String key) {
        if (setContext()) {
            return m_context.get(key);
        } else {
            return m_defaultContext.get(key);
        }
    }

    /**
    Remove the the context identified by the <code>key</code>
    parameter.
    
     */
    public static void remove(String key) {
        if (setContext()) {
            m_context.remove(key);
        } else {
            m_defaultContext.remove(key);
        }
    }

    /**
     * Get the current thread's MDC as a map. 
     * */
    public static Map getContext() {
        if (setContext()) {
            return m_context.getContext();
        } else {
            return m_defaultContext.getContext();
        }
    }

    /** Pax Logging internal method. Should never be used directly. */
    public static void dispose()
    {
        m_paxLogging.close();
        m_paxLogging.dispose();
        m_paxLogging = null;
    }
}
