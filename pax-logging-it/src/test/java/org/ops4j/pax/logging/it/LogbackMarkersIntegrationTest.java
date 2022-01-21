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
package org.ops4j.pax.logging.it;

import java.io.IOException;
import java.util.List;
import javax.inject.Inject;

import org.apache.logging.log4j.MarkerManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingService;
import org.ops4j.pax.logging.PaxMarker;
import org.ops4j.pax.logging.it.support.Helpers;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMarkerFactory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.OptionUtils.combine;

@RunWith(PaxExam.class)
public class LogbackMarkersIntegrationTest extends AbstractStdoutInterceptingIntegrationTestBase {

    @Inject
    private ConfigurationAdmin cm;

    @Configuration
    public Option[] configure() throws IOException {
        return combine(
                combine(baseConfigure(), defaultLoggingConfig()),

                paxLoggingApi(),
                paxLoggingLogback(),
                configAdmin(),
                eventAdmin()
        );
    }

    @Test
    public void loggingWithMarkers() {
        Helpers.updateLoggingConfig(context, cm, Helpers.LoggingLibrary.LOGBACK, "markers");

        IMarkerFactory mf = new BasicMarkerFactory();
        org.slf4j.Marker m1a = mf.getDetachedMarker("slf4j-marker-1");
        org.slf4j.Marker m1b = mf.getDetachedMarker("slf4j-marker-2");
        org.slf4j.Marker pass1 = mf.getDetachedMarker("pass");
        org.slf4j.Marker reject1 = mf.getDetachedMarker("anything without pass");
        m1a.add(m1b);
        m1a.add(pass1);

        org.apache.logging.log4j.Marker m2a = MarkerManager.getMarker("log4j2-marker-1");
        org.apache.logging.log4j.Marker m2b = MarkerManager.getMarker("log4j2-marker-2");
        org.apache.logging.log4j.Marker pass2 = MarkerManager.getMarker("pass");
        org.apache.logging.log4j.Marker reject2 = MarkerManager.getMarker("anything without pass");
        m2a.addParents(m2b);
        m2a.addParents(pass2);

        String name = "my.logger";

        // 1. SLF4j
        org.slf4j.LoggerFactory.getLogger(name).info(m1a, "INFO using SLF4J with marker");
        org.slf4j.LoggerFactory.getLogger(name).info(reject1, "INFO using SLF4J with marker2");
        assertTrue(org.slf4j.LoggerFactory.getLogger(name).isInfoEnabled(m1a));
        assertFalse(org.slf4j.LoggerFactory.getLogger(name).isInfoEnabled(reject1));
        // 9. Log4J2
        org.apache.logging.log4j.LogManager.getLogger(name).info(m2a, "INFO using Log4Jv2 with marker");
        org.apache.logging.log4j.LogManager.getLogger(name).info(reject2, "INFO using Log4Jv2 with marker2");
        assertTrue(org.apache.logging.log4j.LogManager.getLogger(name).isInfoEnabled(m2a));
        assertFalse(org.apache.logging.log4j.LogManager.getLogger(name).isInfoEnabled(reject2));
        // 12. PaxLoggingService itself
        ServiceReference<PaxLoggingService> sr = context.getServiceReference(PaxLoggingService.class);
        PaxLoggingService paxLoggingService = context.getService(sr);

        PaxLogger paxLogger = paxLoggingService.getLogger(context.getBundle(), "my.logger", paxLoggingService.getClass().getName());
        String fqcn = paxLogger.getClass().getName();
        // again - with better FQCN
        paxLogger = paxLoggingService.getLogger(context.getBundle(), "my.logger", fqcn);
        paxLogger.inform(new PaxMarker(m1a), "INFO using PaxLogger with SLF4J marker", null);
        paxLogger.inform(new PaxMarker(reject1), "INFO using PaxLogger with SLF4J marker2", null);
        paxLogger.inform(new PaxMarker(m2a), "INFO using PaxLogger with Log4J2 marker", null);
        paxLogger.inform(new PaxMarker(reject2), "INFO using PaxLogger with Log4J2 marker2", null);
        assertTrue(paxLogger.isInfoEnabled(new PaxMarker(m1a)));
        assertTrue(paxLogger.isInfoEnabled(new PaxMarker(m2a)));
        assertFalse(paxLogger.isInfoEnabled(new PaxMarker(reject1)));
        assertFalse(paxLogger.isInfoEnabled(new PaxMarker(reject2)));

        List<String> lines = readLines();
        assertTrue(lines.contains("my.logger/org.ops4j.pax.logging.it.LogbackMarkersIntegrationTest [INFO] slf4j-marker-1 [ slf4j-marker-2, pass ] INFO using SLF4J with marker"));
        assertTrue(lines.contains("my.logger/org.ops4j.pax.logging.it.LogbackMarkersIntegrationTest [INFO] log4j2-marker-1 [ log4j2-marker-2, pass ] INFO using Log4Jv2 with marker"));
        assertTrue(lines.contains("my.logger/org.ops4j.pax.logging.it.LogbackMarkersIntegrationTest [INFO] slf4j-marker-1 [ slf4j-marker-2, pass ] INFO using PaxLogger with SLF4J marker"));
        assertTrue(lines.contains("my.logger/org.ops4j.pax.logging.it.LogbackMarkersIntegrationTest [INFO] log4j2-marker-1 [ log4j2-marker-2, pass ] INFO using PaxLogger with Log4J2 marker"));
        // this should be rejected by MarkerFilter
        assertFalse(lines.stream().anyMatch(l -> l.contains("with marker2")));
    }

}
