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
import org.apache.log4j.helpers.ThreadLocalMap;

/**
   The MDC class is similar to the {@link NDC} class except that it is
   based on a map instead of a stack. It provides <em>mapped
   diagnostic contexts</em>. A <em>Mapped Diagnostic Context</em>, or
   MDC in short, is an instrument for distinguishing interleaved log
   output from different sources. Log output is typically interleaved
   when a server handles multiple clients near-simultaneously.

   <p><b><em>The MDC is managed on a per thread basis</em></b>. A
   child thread automatically inherits a <em>copy</em> of the mapped
   diagnostic context of its parent.
  
   <p>The MDC class requires JDK 1.2 or above. Under JDK 1.1 the MDC
   will always return empty values but otherwise will not affect or
   harm your application.
   
   @since 1.2

   @author Ceki G&uuml;lc&uuml; */
public class PaxContext {
  
  static final int HT_SIZE = 7;

  Object tlm;
  
  public PaxContext() {
      tlm = new ThreadLocalMap();
  }

 
  public void putAll(Map context){
      Hashtable ht = (Hashtable) ((ThreadLocalMap)tlm).get();
      if(ht == null) {
        ht = new Hashtable(HT_SIZE);
        ((ThreadLocalMap)tlm).set(ht);
      }    
      ht.putAll(context);   
  }
  
  public void put(String key, Object o) {
    Hashtable ht = (Hashtable) ((ThreadLocalMap)tlm).get();
    if(ht == null) {
        ht = new Hashtable(HT_SIZE);
        ((ThreadLocalMap)tlm).set(ht);
    }    
    ht.put(key, o); 
  }
  
  public String get(String key) {
    Hashtable ht = (Hashtable) ((ThreadLocalMap)tlm).get();
    if(ht != null && key != null) {
        return (String) ht.get(key);
    } else {
        return null;
    }    
  }

  public void remove(String key) {
    Hashtable ht = (Hashtable) ((ThreadLocalMap)tlm).get();
    if(ht != null) {
        ht.remove(key);
    }    
  }


  public Map getContext() {
    return (Map) ((ThreadLocalMap)tlm).get();
  }
  
  public void clear() {
      Map context=(Map)((ThreadLocalMap)tlm).get();
      if(context!=null){
          context.clear();
      }
  }
}
