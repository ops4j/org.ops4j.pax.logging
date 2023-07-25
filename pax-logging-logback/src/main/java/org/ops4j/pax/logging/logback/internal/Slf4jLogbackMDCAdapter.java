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
package org.ops4j.pax.logging.logback.internal;

import java.util.Deque;
import java.util.Map;
import java.util.Set;

import ch.qos.logback.classic.util.LogbackMDCAdapter;
import org.slf4j.MDC;
import org.slf4j.spi.MDCAdapter;

// With https://jira.qos.ch/browse/LOGBACK-1742, Logback stopped using
// MDC.getMDCAdapter()
public class Slf4jLogbackMDCAdapter extends LogbackMDCAdapter {

    private final MDCAdapter adapter;

    public Slf4jLogbackMDCAdapter() {
        adapter = MDC.getMDCAdapter();
    }

    @Override
    public void put(String key, String val) throws IllegalArgumentException {
        adapter.put(key, val);
    }

    @Override
    public String get(String key) {
        return adapter.get(key);
    }

    @Override
    public void remove(String key) {
        adapter.remove(key);
    }

    @Override
    public void clear() {
        adapter.clear();
    }

    @Override
    public Map<String, String> getPropertyMap() {
        return adapter.getCopyOfContextMap();
    }

    @Override
    public Map getCopyOfContextMap() {
        return adapter.getCopyOfContextMap();
    }

    @Override
    public Set<String> getKeys() {
        return adapter.getCopyOfContextMap().keySet();
    }

    @Override
    public void setContextMap(Map contextMap) {
        adapter.setContextMap(contextMap);
    }

    @Override
    public void pushByKey(String key, String value) {
        adapter.pushByKey(key, value);
    }

    @Override
    public String popByKey(String key) {
        return adapter.popByKey(key);
    }

    @Override
    public Deque<String> getCopyOfDequeByKey(String key) {
        return adapter.getCopyOfDequeByKey(key);
    }

    @Override
    public void clearDequeByKey(String key) {
        adapter.clearDequeByKey(key);
    }

}
