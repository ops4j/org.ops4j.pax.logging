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
package org.ops4j.pax.logging.log4jv2;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.spi.ThreadContextMap;
import org.ops4j.pax.logging.OSGIPaxLoggingManager;
import org.ops4j.pax.logging.PaxContext;
import org.ops4j.pax.logging.PaxLoggingManager;
import org.osgi.framework.BundleContext;

/**
 * The actual ThreadContext Map. A new ThreadContext Map is created each time it is updated and the Map stored is always
 * immutable. This means the Map can be passed to other threads without concern that it will be updated. Since it is
 * expected that the Map will be passed to many more log events than the number of keys it contains the performance
 * should be much better than if the Map was copied for each event.
 */
public class Log4jv2ThreadContextMap implements ThreadContextMap {

    private static PaxContext m_context;
    private static PaxContext m_defaultContext = new PaxContext();

    private static PaxLoggingManager m_paxLogging;

    public static void setBundleContext( BundleContext ctx )
    {
        m_paxLogging = new OSGIPaxLoggingManager( ctx );
        // We need to instruct all loggers to ensure the SimplePaxLoggingManager is replaced.
        m_paxLogging.open();
    }

    public static void dispose()
    {
    }

    /**
     * For all the methods that operate against the context, return true if the MDC should use the PaxContext object from the PaxLoggingManager,
     * or if the logging manager is not set, or does not have its context available yet, use a default context local to this MDC.
     * @return m_context if the MDC should use the PaxContext object from the PaxLoggingManager,
     * or m_defaultContext if the logging manager is not set, or does not have its context available yet.
     */
    private static PaxContext getContext() {
        if( m_context==null && m_paxLogging!=null ){
            m_context=(m_paxLogging.getPaxLoggingService()!=null)?m_paxLogging.getPaxLoggingService().getPaxContext():null;
        }
        return m_context!=null?m_context:m_defaultContext;
    }

    @Override
    public void put(String key, String value) {
        getContext().put(key, value);
    }

    @Override
    public String get(String key) {
        Object obj = getContext().get(key);
        return obj != null ? obj.toString() : null;
    }

    @Override
    public void remove(String key) {
        getContext().remove(key);
    }

    @Override
    public void clear() {
        getContext().clear();
    }

    @Override
    public boolean containsKey(String key) {
        return getContext().get(key) != null;
    }

    @Override
    public Map<String, String> getCopy() {
        final Map<String, Object> copy = getContext().getCopyOfContextMap();
        if (copy == null) {
            return Collections.emptyMap();
        }
        return new AbstractMap<String, String>() {
            @Override
            public Set<Entry<String, String>> entrySet() {
                return new AbstractSet<Entry<String, String>>() {
                    final Set<Entry<String, Object>> set = copy.entrySet();
                    @Override
                    public Iterator<Entry<String, String>> iterator() {
                        return new Iterator<Entry<String, String>>() {
                            Iterator<Entry<String, Object>> it = set.iterator();
                            @Override
                            public boolean hasNext() {
                                return it.hasNext();
                            }
                            @Override
                            public Entry<String, String> next() {
                                Entry<String, Object> entry = it.next();
                                return new SimpleEntry<String, String>(
                                        entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : null);
                            }
                            @Override
                            public void remove() {
                                // nothing to do
                            }
                        };
                    }
                    @Override
                    public int size() {
                        return set.size();
                    }
                };
            }
        };
    }

    @Override
    public Map<String, String> getImmutableMapOrNull() {
        return getCopy();
    }

    @Override
    public boolean isEmpty() {
        Map<String, Object> ctx = getContext().getContext();
        return ctx == null || ctx.isEmpty();
    }
}
