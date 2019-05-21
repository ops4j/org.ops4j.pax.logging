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
package org.ops4j.pax.logging.logback.extra;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.AbstractMatcherFilter;
import ch.qos.logback.core.spi.FilterReply;

public class DebugFilter extends AbstractMatcherFilter<ILoggingEvent> {

    private DateFormat TS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private long beforeTS = 0L;
    private long afterTS = 0L;

    @Override
    public FilterReply decide(ILoggingEvent event) {
        String message = (String) getField(event, "message");
        if (message.equals("before")) {
            beforeTS = (long) getField(event, "timeStamp");
        }
        if (message.equals("after")) {
            // two months later
            afterTS = beforeTS + (2L * 31L * 24L * 60L * 60L * 1000L);
            setField(event, "timeStamp", afterTS);
        }
        if (message.equals("much after")) {
            // two months later
            long after2TS = afterTS + (2L * 31L * 24L * 60L * 60L * 1000L);
            setField(event, "timeStamp", after2TS);
        }
        return FilterReply.NEUTRAL;
    }

    /**
     * From {@code org.ops4j.pax.logging.it.support.Helpers#getField()}
     * @param object
     * @param fieldName
     * @return
     */
    public static Object getField(Object object, String fieldName) {
        String[] names = fieldName.split("\\.");
        for (String name : names) {
            Field f = null;
            try {
                f = object.getClass().getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                try {
                    f = object.getClass().getSuperclass().getDeclaredField(name);
                } catch (NoSuchFieldException ex) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
            f.setAccessible(true);
            try {
                object = f.get(object);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        return object;
    }

    /**
     * From {@code org.ops4j.pax.logging.it.support.Helpers#setField()}
     * @param object
     * @param fieldName
     * @param value
     */
    public static void setField(Object object, String fieldName, Object value) {
        Field f = null;
        try {
            f = object.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            try {
                f = object.getClass().getSuperclass().getDeclaredField(fieldName);
            } catch (NoSuchFieldException ex) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        f.setAccessible(true);
        try {
            f.set(object, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
