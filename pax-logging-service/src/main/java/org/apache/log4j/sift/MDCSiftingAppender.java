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
public class MDCSiftingAppender extends AppenderSkeleton
{

    private String key;
    private String defaultValue = "default";
    private OptionFactory appender;
    private Map appenders = new HashMap();

    private Node head = Node.constructHead();
    private long lastCheck = 0;

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getDefault()
    {
        return defaultValue;
    }

    public void setDefault(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    public OptionFactory getAppender()
    {
        return appender;
    }

    public void setAppender(OptionFactory appender)
    {
        this.appender = appender;
    }

    protected void append(LoggingEvent event)
    {
        Object value = event.getMDC(key);
        String valStr = value == null ? defaultValue : value.toString();
        Appender app = getAppender(valStr);
        app.doAppend(event);
    }

    public synchronized void close()
    {
        Node n = head.next;
        while (n.timestamp != 0) {
            n.appender.close();
            n = n.next;
        }
        head.prev = head.next = head;
        appenders.clear();
    }

    public boolean requiresLayout()
    {
        return false;
    }

    protected synchronized Appender getAppender(String valStr)
    {
        long timestamp = System.currentTimeMillis();
        Node node = (Node) appenders.get(valStr);
        if (node == null)
        {
            // create new appender and insert at the beginning of the queue
            node = new Node();
            node.key = valStr;
            Properties props = new Properties();
            props.put(key, valStr);
            node.appender = (Appender) appender.create(props);
            node.appender.setName(getName() + "[" + valStr + "]");
            node.timestamp = timestamp;

            node.next = head.next;
            head.next = node;
            node.prev = head;
            node.next.prev = node;

            appenders.put(valStr, node);
        } else {
            // move existing appender to the beginning of the queue
            node.timestamp = timestamp;
            node.prev.next = node.next;
            node.next.prev = node.prev;

            node.next = head.next;
            head.next = node;
            node.prev = head;
            node.next.prev = node;
        }

        // Do not check too often
        if (timestamp - lastCheck > 1000)
        {
            lastCheck = timestamp;
            Node n = head.prev;
            while (n.timestamp != 0 && timestamp - n.timestamp > 30 * 60 * 1000) {
                n.appender.close();
                appenders.remove(n.key);
                n = n.prev;
            }
            head.prev = n;
            n.next = head;
        }
        return node.appender;
    }

    protected static class Node
    {
        String key;
        Node next;
        Node prev;
        Appender appender;
        long timestamp;

        public static Node constructHead()
        {
            Node head = new Node();
            head.next = head;
            head.prev = head;
            head.timestamp = 0;
            head.key = null;
            head.appender = null;
            return head;
        }
    }

}
