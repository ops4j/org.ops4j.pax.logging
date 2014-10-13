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
/**
 * Derived from org.apache.log4j.MDC
 */
package org.ops4j.pax.logging;

import java.util.Hashtable;
import java.util.Map;

/**
 * The MDC class is similar to the {@link org.apache.log4j.NDC} class except that it is
 * based on a map instead of a stack. It provides <em>mapped
 * diagnostic contexts</em>. A <em>Mapped Diagnostic Context</em>, or
 * MDC in short, is an instrument for distinguishing interleaved log
 * output from different sources. Log output is typically interleaved
 * when a server handles multiple clients near-simultaneously.
 * <p/>
 * <p><b><em>The MDC is managed on a per thread basis</em></b>. A
 * child thread automatically inherits a <em>copy</em> of the mapped
 * diagnostic context of its parent.
 * <p/>
 * <p>The MDC class requires JDK 1.2 or above. Under JDK 1.1 the MDC
 * will always return empty values but otherwise will not affect or
 * harm your application.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @since 1.2
 */
public class PaxContext
{

    static final int HT_SIZE = 7;

    final ThreadLocalMap tlm = new ThreadLocalMap();

    public PaxContext()
    {
    }


    public void putAll(Map<String, Object> context)
    {
        Hashtable<String, Object> ht = tlm.get();
        if (ht == null)
        {
            ht = new Hashtable<String, Object>(HT_SIZE);
            tlm.set(ht);
        }
        ht.putAll(context);
    }

    public void put(String key, Object o)
    {
        Hashtable<String, Object> ht = tlm.get();
        if (ht == null)
        {
            ht = new Hashtable<String, Object>(HT_SIZE);
            tlm.set(ht);
        }
        ht.put(key, o);
    }

    public Object get(String key)
    {
        Hashtable ht = tlm.get();
        if (ht != null && key != null)
        {
            return ht.get(key);
        }
        else
        {
            return null;
        }
    }

    public void remove(String key)
    {
        Hashtable ht = tlm.get();
        if (ht != null)
        {
            ht.remove(key);
        }
    }


    public Map<String, Object> getContext()
    {
        return tlm.get();
    }

    public void clear()
    {
        Hashtable<String, Object> ht = tlm.get();
        if (ht != null)
        {
            ht.clear();
        }
    }

    public Map<String, Object> getCopyOfContextMap()
    {
        Hashtable<String, Object> ht = tlm.get();
        if (ht != null)
        {
            return new Hashtable<String, Object>(ht);
        }
        else
        {
            return null;
        }
    }

    public void setContextMap(Map<String, Object> contextMap)
    {
        Hashtable<String, Object> ht = tlm.get();
        if (ht != null)
        {
            ht.clear();
        }
        else
        {
            ht = new Hashtable<String, Object>(HT_SIZE);
            tlm.set(ht);
        }
        ht.putAll(contextMap);
    }

    static class ThreadLocalMap extends InheritableThreadLocal<Hashtable<String, Object>>
    {
        @Override
        protected Hashtable<String, Object> childValue(Hashtable<String, Object> parentValue)
        {
            if (parentValue != null)
            {
                return new Hashtable<String, Object>(parentValue);
            }
            else
            {
                return null;
            }
        }
    }
}
