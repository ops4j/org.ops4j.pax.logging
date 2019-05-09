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
import java.lang.reflect.Field;
import javax.inject.Inject;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.logging.spi.support.DefaultServiceLog;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertSame;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.OptionUtils.combine;

/**
 * <p>Test that proves that all facades use the same underlying logging service.</p>
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class AllLoggingFacadesIntegrationTest extends AbstractControlledIntegrationTestBase {

    public static Logger LOG = LoggerFactory.getLogger(AllLoggingFacadesIntegrationTest.class);

    @Inject
    private BundleContext context;

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
    public void logUsingEverything() {
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
        // 6. Knopflerfish - no special facade
        // 7. Log4J1
        org.apache.log4j.Logger.getLogger(name).info("INFO using Log4Jv1");
        // 8. Logback - only behind SLF4J
        // 9. Log4J2
        // Log4J2 Logging involves log() methods that pass FQCN
        org.apache.logging.log4j.LogManager.getLogger(name).info("INFO using Log4Jv2");
        // 10. JUL - extra handling without a pax-logging specific facade and shadowing. Only handler redirection
        java.util.logging.Logger.getLogger(name).info("INFO using java.util.logging");
    }

    /**
     * Each of the delegate loggers under facade-specific logger should be the same when not using
     * any pax-logging backend.
     */
    @Test
    @Ignore
    public void sharedDelegate() throws NoSuchFieldException, IllegalAccessException {
        String name = "org.ops4j.pax.logging.it.test";

        // 1. SLF4j
        Object d1 = delegateLogger(org.slf4j.LoggerFactory.getLogger(name));
        // 2. Commons Logging
        Object d2 = delegateLogger(org.apache.commons.logging.LogFactory.getLog(name));
        // 3. JULI Logging
        Object d3 = delegateLogger(org.apache.juli.logging.LogFactory.getLog(name));
        // 4. Avalon Logging
        Object d4 = delegateLogger(org.ops4j.pax.logging.avalon.AvalonLogFactory.getLogger(name));
        // 5. JBoss Logging
//        org.jboss.logging.Logger.getLogger(name);
        // 6. Knopflerfish - no special facade
        // 7. Log4J1
        Object d7 = delegateLogger(org.apache.log4j.Logger.getLogger(name));
        // 8. Logback - only behind SLF4J
        // 9. Log4J2
        // Log4J2 Logging involves log() methods that pass FQCN
        Object d9 = delegateLogger(org.apache.logging.log4j.LogManager.getLogger(name));
        // 10. JUL - extra handling without a pax-logging specific facade and shadowing. Only handler redirection
//        java.util.logging.Logger.getLogger(name);

        assertSame(d1, d2);
        assertSame(d2, d3);
        assertSame(d3, d4);
        assertSame(d4, d7);
        assertSame(d7, d9);
    }

    /**
     * Uses reflection to get underlying delegate logger.
     * @param logger
     * @return
     */
    private Object delegateLogger(Object logger) throws NoSuchFieldException, IllegalAccessException {
        Field f = null;
        try {
            f = logger.getClass().getDeclaredField("m_delegate");
        } catch (NoSuchFieldException e) {
            f = logger.getClass().getSuperclass().getDeclaredField("m_delegate");
        }
        f.setAccessible(true);
        return f.get(logger);
    }

}
