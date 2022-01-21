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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.logging.spi.support.DefaultServiceLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.OptionUtils.combine;

/**
 * Test that proves that all facades use the same underlying logging service.
 */
@RunWith(PaxExam.class)
public class AllLoggingFacadesIntegrationTest extends AbstractControlledIntegrationTestBase {

    public static Logger LOG = LoggerFactory.getLogger(AllLoggingFacadesIntegrationTest.class);

    @Configuration
    public Option[] configure() throws IOException {
        return combine(
                combine(baseConfigure(), defaultLoggingConfig()),
                mavenBundle("org.ops4j.pax.logging", "pax-logging-api").versionAsInProject()
        );
    }

    /**
     * Because only pax-logging-api bundle is installed, all the 10 facades/bridges/apis will log through
     * {@link DefaultServiceLog} and down to {@code stdout}.
     */
    @Test
    public void logUsingAllFacades() {
        String name = "org.ops4j.pax.logging.it.test";

        // 1. SLF4j
        org.slf4j.LoggerFactory.getLogger(name).info("INFO using SLF4J");
        // 2. Commons Logging
        org.apache.commons.logging.LogFactory.getLog(name).info("INFO using Commons Logging");
        // 3. JULI Logging
        org.apache.juli.logging.LogFactory.getLog(name).info("INFO using Juli Logging");
        // 4. Avalon Logging
        org.ops4j.pax.logging.avalon.AvalonLogFactory.getLogger(name).info("INFO using Avalon Logging");
        // 5. JBoss Logging
        org.jboss.logging.Logger.getLogger(name).info("INFO using JBoss Logging");
        // 6. Log4J1 API
        org.apache.log4j.Logger.getLogger(name).info("INFO using Log4Jv1");
        // 7. Logback - only behind SLF4J
        // 8. Log4J2
        // Log4J2 Logging involves log() methods that pass FQCN
        org.apache.logging.log4j.LogManager.getLogger(name).info("INFO using Log4Jv2");
        // 9. JUL - extra handling without a pax-logging specific facade and shadowing. Only handler redirection
        java.util.logging.Logger.getLogger(name).info("INFO using java.util.logging");
        java.util.logging.Logger.getLogger(name).fine("FINE using java.util.logging");
        java.util.logging.Logger.getLogger(name).finer("FINER using java.util.logging");
        java.util.logging.Logger.getLogger(name).finest("FINEST using java.util.logging");

        List<String> lines = readLines();

        assertTrue(lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.test] INFO : INFO using SLF4J"));
        assertTrue(lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.test] INFO : INFO using Commons Logging"));
        assertTrue(lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.test] INFO : INFO using Juli Logging"));
        assertTrue(lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.test] INFO : INFO using Avalon Logging"));
        // here, FQCN is passed, which is explicitly added to message by DefaultServiceLog
        assertTrue(lines.stream().anyMatch(l -> l.startsWith("PaxExam-Probe [org.ops4j.pax.logging.it.test] INFO : INFO using JBoss Logging")));
        assertTrue(lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.test] INFO : INFO using Log4Jv1"));
        // here, FQCN is passed, which is explicitly added to message by DefaultServiceLog
        assertTrue(lines.stream().anyMatch(l -> l.startsWith("PaxExam-Probe [org.ops4j.pax.logging.it.test] INFO : INFO using Log4Jv2")));
        assertTrue(lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.test] INFO : INFO using java.util.logging"));
        assertTrue(lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.test] DEBUG : FINE using java.util.logging"));
        assertFalse(lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.test] TRACE : FINER using java.util.logging"));
        assertFalse(lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.test] TRACE : FINEST using java.util.logging"));
    }

}
