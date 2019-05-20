/*
 * Copyright 2010 Guillaume Nodet.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.log4j.sift;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.OptionFactory;

/**
 * A log4j appender which splits the output based on an MDC key
 */
public class MDCSiftingAppender extends AppenderSkeleton {

    /** Key in MDC for value that's used to select target {@link org.apache.log4j.Appender} */
    private String key;
    /** Value used if there's nothing under specified {@link #getKey() key} */
    private String defaultValue = "default";

    /** Lazy information about the dynamic appender selected for given value */
    private OptionFactory appender;

    /** Cache of already created appenders */
    private Map<String, Node> appenders = new HashMap<>();

    private Node head = null;
    private Node tail = null;
    private long lastCheck;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDefault() {
        return defaultValue;
    }

    public void setDefault(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public OptionFactory getAppender() {
        return appender;
    }

    public void setAppender(OptionFactory appender) {
        this.appender = appender;
    }

    @Override
    protected void append(LoggingEvent event) {
        Object value = event.getMDC(key);
        Map<?, ?> mdc = event.getProperties();
        String valStr = value == null ? defaultValue : value.toString();
        Appender app = getAppender(valStr, mdc);
        app.doAppend(event);
    }

    @Override
    public synchronized void close() {
        for (Iterator it = appenders.values().iterator(); it.hasNext(); ) {
            Node node = (Node) it.next();
            node.appender.close();
        }
        appenders.clear();
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

    protected synchronized Appender getAppender(String valStr, Map<?, ?> mdc) {
        long timestamp = System.currentTimeMillis();
        Node node = appenders.get(valStr);
        if (node == null) {
            node = new Node();
            Properties props = new Properties();
            props.put(key, valStr);
            props.putAll(mdc);
            node.next = head;
            node.prev = null;
            node.appender = (Appender) appender.create(props);
            node.appender.setName(getName() + "[" + valStr + "]");
            node.timestamp = timestamp;
            head = node;
            if (tail == null) {
                tail = node;
            }
            appenders.put(valStr, node);
        } else {
            Node p = node.prev;
            Node n = node.next;
            node.next = head;
            node.prev = null;
            head = node;
            if (p != null) {
                p.next = n;
            }
            if (n != null) {
                n.prev = p;
            } else {
                tail = p;
            }
            node.timestamp = timestamp;
        }
        // Do not check too often
        if (timestamp - lastCheck > 1000) {
            Node n = tail;
            while (n != null && timestamp - n.timestamp > 30 * 60 * 1000) {
                n.appender.close();
                n = n.prev;
            }
            if (n == null) {
                tail = head = null;
            } else {
                n.next = null;
                tail = n;
            }
        }
        return node.appender;
    }

    /**
     * Pointer to real target {@link Appender} with timestamped acces.
     */
    protected static class Node {
        Node next;
        Node prev;
        Appender appender;
        long timestamp;
    }

}
