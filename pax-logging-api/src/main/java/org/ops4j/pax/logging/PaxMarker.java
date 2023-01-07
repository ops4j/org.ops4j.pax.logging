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
package org.ops4j.pax.logging;

import java.util.Iterator;

import org.apache.logging.log4j.MarkerManager;
import org.ops4j.pax.logging.slf4j.PaxLoggingSLF4JServiceProvider;
import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMarkerFactory;

/**
 * Wrapper for either SLF4J {@code org.slf4j.Marker} or Log4J2 {@code org.apache.logging.log4j.Marker}. We can
 * do it, because both are exported by pax-logging-api bundle. pax-logging-log4j2 and pax-logging-logback will
 * know which method to call to get actual <em>marker</em>.
 *
 * Of course, with pax-logging it's possible to use Log4J2 API to log through Logback or use SLF4J to log through
 * Log4J2 (and many other combinations). If there's an attempt to get Log4J2 marker when only SLF4J is available,
 * proper marker has to be created.
 */
public class PaxMarker {

    private org.slf4j.Marker slf4jMarker;
    private org.apache.logging.log4j.Marker log4j2Marker;

    private final IMarkerFactory factory = PaxLoggingSLF4JServiceProvider.markerFactory;

    public PaxMarker(org.slf4j.Marker slf4jMarker) {
        this.slf4jMarker = slf4jMarker;
    }

    public PaxMarker(org.apache.logging.log4j.Marker log4j2Marker) {
        this.log4j2Marker = log4j2Marker;
    }

    public org.slf4j.Marker slf4jMarker() {
        if (slf4jMarker != null) {
            return slf4jMarker;
        }
        if (log4j2Marker != null) {
            slf4jMarker = createSlf4jMarker(log4j2Marker);
            return slf4jMarker;
        }
        return null;
    }

    public org.apache.logging.log4j.Marker log4j2Marker() {
        if (log4j2Marker != null) {
            return log4j2Marker;
        }
        if (slf4jMarker != null) {
            log4j2Marker = createLog4j2Marker(slf4jMarker);
            return log4j2Marker;
        }
        return null;
    }

    private org.slf4j.Marker createSlf4jMarker(org.apache.logging.log4j.Marker log4j2Marker) {
        if (!log4j2Marker.hasParents()) {
            return factory.getDetachedMarker(log4j2Marker.getName());
        }
        org.slf4j.Marker result = new BasicMarkerFactory().getDetachedMarker(log4j2Marker.getName());
        for (org.apache.logging.log4j.Marker parent : log4j2Marker.getParents()) {
            result.add(createSlf4jMarker(parent));
        }
        return result;
    }

    private org.apache.logging.log4j.Marker createLog4j2Marker(org.slf4j.Marker slf4jMarker) {
        if (!slf4jMarker.hasReferences()) {
            return MarkerManager.getMarker(slf4jMarker.getName());
        }
        org.apache.logging.log4j.Marker result = MarkerManager.getMarker(slf4jMarker.getName());
        for (Iterator<org.slf4j.Marker> it = slf4jMarker.iterator(); it.hasNext(); ) {
            result.addParents(createLog4j2Marker(it.next()));
        }
        return result;
    }

}
