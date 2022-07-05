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
package org.ops4j.pax.logging.log4j2.extra;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

@Plugin(name = "List", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
public class ListAppender extends AbstractAppender {

    private static final BlockingQueue<String> events = new LinkedBlockingQueue<>();

    public ListAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions, Property[] properties) {
        super(name, filter, layout, ignoreExceptions, properties);
    }

    @Override
    public void append(LogEvent event) {
        events.offer(event.getMessage().getFormattedMessage());
    }

    public BlockingQueue<String> getEvents() {
        return events;
    }

    @PluginFactory
    public static ListAppender factory(
            @PluginAttribute(value = "name", defaultString = "null") final String name) {

        Bundle bundle = FrameworkUtil.getBundle(ListAppender.class);

        // put to OSGi to reference in test
        Hashtable<String, Object> props = new Hashtable<>();
        props.put("name", name);
        bundle.getBundleContext().registerService(BlockingQueue.class, events, props);

        return new ListAppender(name, null, null, true, Property.EMPTY_ARRAY);
    }

}
