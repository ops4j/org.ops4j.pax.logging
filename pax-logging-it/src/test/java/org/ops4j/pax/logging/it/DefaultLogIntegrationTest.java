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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.logging.OSGIPaxLoggingManager;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingConstants;
import org.ops4j.pax.logging.spi.PaxDefaultLogStreamProvider;
import org.ops4j.pax.logging.spi.support.DefaultServiceLog;
import org.ops4j.pax.tinybundles.core.TinyBundles;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.ops4j.pax.exam.CoreOptions.frameworkProperty;
import static org.ops4j.pax.exam.OptionUtils.combine;
import static org.ops4j.pax.logging.it.support.Helpers.delegate;

/**
 * <p>Test that installs only pax-logging-api bundle and checks different aspects of fallback mechanisms</p>
 * <p>Note: when using pax-exam-container-native, both surefire runner ({@link PaxExam}) and
 * the framework itself runs in the same JVM, so it's a bit hard to separate logging configuration.</p>
 * <p>First ever logging statement is executed in {@code org.ops4j.pax.exam.spi.DefaultExamSystem#create(org.ops4j.pax.exam.Option[])}
 * which logs some information using SLF4J API. However, the classpath of this project includes both slf4j-api
 * and pax-logging-api. Inside pax-exam code, this snippet:<pre>
 * for (Enumeration e = LOG.getClass().getClassLoader().findResources("org/slf4j/Logger.class"); e.hasMoreElements(); ) {
 *     Object r = e.nextElement();
 *     System.out.println("r: " + r);
 * }
 * </pre> prints:<pre>
 * r: jar:file:~/.m2/repository/org/ops4j/pax/logging/pax-logging-api/1.11.0-SNAPSHOT/pax-logging-api-1.11.0-SNAPSHOT.jar!/org/slf4j/Logger.class
 * r: jar:file:~/.m2/repository/org/slf4j/slf4j-api/1.7.29/slf4j-api-1.7.29.jar!/org/slf4j/Logger.class
 * </pre>
 * That's why even pax-exam logs go through pax-logging-api's {@link DefaultServiceLog}.</p>
 */
@RunWith(PaxExam.class)
public class DefaultLogIntegrationTest extends AbstractStdoutInterceptingIntegrationTestBase {

    /**
     * Method called by surefire/failsafe, not to be used by @Test method inside OSGi container (even if native)
     * @throws IOException
     */
    public static void prepareBundles() throws IOException {
        InputStream bundle1 = TinyBundles.bundle()
                .set("Bundle-ManifestVersion", "2")
                .set("Bundle-SymbolicName", "MySpecialBundleThatShouldTriggerBundleEventWhenInstalling")
                .build();
        new File("target/bundles").mkdirs();
        IOUtils.copy(bundle1, new FileOutputStream("target/bundles/sample-bundle1.jar"));

        InputStream bundle2 = TinyBundles.bundle()
                .set("Bundle-ManifestVersion", "2")
                .set("Bundle-SymbolicName", "AnotherBundle")
                .build();
        new File("target/bundles").mkdirs();
        IOUtils.copy(bundle2, new FileOutputStream("target/bundles/sample-bundle2.jar"));
    }

    @Configuration
    public Option[] configure() throws IOException {
        prepareBundles();

        // this configuration sets up default/fallback logger that prints to System.out
        return combine(
                baseConfigure(),
                paxLoggingApi(),

                // every log with level higher or equal to DEBUG (i.e., not TRACE) will be logged
                frameworkProperty(PaxLoggingConstants.LOGGING_CFG_DEFAULT_LOG_LEVEL).value("DEBUG"),
                // threshold for R6 Compendium 101.6 logging statements
                frameworkProperty(PaxLoggingConstants.LOGGING_CFG_FRAMEWORK_EVENTS_LOG_LEVEL).value("DEBUG")
        );
    }

    @Test
    public void defaultStdoutLogger() throws InvalidSyntaxException, ClassNotFoundException {
        // I know this test is full of internal-awareness, but it's here to not break this fragile balance

        Collection<ServiceReference<PaxDefaultLogStreamProvider>> refs
                = context.getServiceReferences(PaxDefaultLogStreamProvider.class, null);
        assertEquals("There should be no singleton stream providers registered", 0, refs.size());

        try {
            context.getBundle().loadClass("org.ops4j.pax.logging.spi.support.FallbackLogFactory");
            fail("Should not be able to load FallbackLogFactory class");
        } catch (ClassNotFoundException ignored) {
        }

        assertSame(PaxDefaultLogStreamProvider.class,
                context.getBundle().loadClass("org.ops4j.pax.logging.spi.PaxDefaultLogStreamProvider"));

        // pax-logging-api creates own manager, but there are no restrictions to create another one
        OSGIPaxLoggingManager manager = new OSGIPaxLoggingManager(context);
        PaxLogger category1 = manager.getLogger(context.getBundle(), "category1", null);
        PaxLogger category1a = manager.getLogger(context.getBundle(), "category1", null);
        // different bundle, same category means different logger
        PaxLogger category1b = manager.getLogger(context.getBundle("link:classpath:META-INF/links/org.ops4j.pax.exam.invoker.junit.link"), "category1", null);
        PaxLogger category2 = manager.getLogger(context.getBundle(), "category2", null);

        assertSame(category1, category1a);
        assertNotSame(category1, category1b);
        assertNotSame(category1, category2);
        assertThat("delegate should be tracking logger",
                category1.getClass().getName(), equalTo("org.ops4j.pax.logging.internal.TrackingLogger"));

        Object d1 = delegate(category1);
        Object d1a = delegate(category1a);
        Object d1b = delegate(category1b);
        Object d2 = delegate(category2);

        assertSame(d1, d1a);
        assertNotSame(d1, d1b);
        assertNotSame(d1, d2);
        assertThat("delegate should be System.out based logger",
                d1.getClass().getName(), equalTo("org.ops4j.pax.logging.spi.support.DefaultServiceLog"));
    }

    @Test
    public void logAtDifferentLevels() {
        Logger log = LoggerFactory.getLogger(DefaultLogIntegrationTest.class);
        log.error("DefaultLogIntegrationTest/ERROR");
        log.warn("DefaultLogIntegrationTest/WARN");
        log.info("DefaultLogIntegrationTest/INFO");
        log.debug("DefaultLogIntegrationTest/DEBUG");
        // this should not be logged
        log.trace("DefaultLogIntegrationTest/TRACE");

        List<String> lines = readLines();

        // each line is in format: "symbolic-name [category] level : message"
        assertTrue(lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.DefaultLogIntegrationTest] ERROR : DefaultLogIntegrationTest/ERROR"));
        assertTrue(lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.DefaultLogIntegrationTest] WARN : DefaultLogIntegrationTest/WARN"));
        assertTrue(lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.DefaultLogIntegrationTest] INFO : DefaultLogIntegrationTest/INFO"));
        assertTrue(lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.DefaultLogIntegrationTest] DEBUG : DefaultLogIntegrationTest/DEBUG"));
        assertFalse(lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.DefaultLogIntegrationTest] TRACE : DefaultLogIntegrationTest/TRACE"));
    }

    @Test
    public void bundleInstallation() throws BundleException, FileNotFoundException, MalformedURLException {
        // we should get a bundle even about installing new bundle
        File f1 = new File("target/bundles/sample-bundle1.jar");
        File f2 = new File("target/bundles/sample-bundle2.jar");
        Bundle b1 = context.installBundle(f1.toURI().toURL().toString(), new FileInputStream(f1));
        Bundle b2 = context.installBundle(f2.toURI().toURL().toString(), new FileInputStream(f2));
        b2.start();

        List<String> lines = readLines();

        // verification of logging BundleEvents
        assertTrue(lines.contains("MySpecialBundleThatShouldTriggerBundleEventWhenInstalling [org.osgi.framework.BundleEvent] INFO : BundleEvent INSTALLED"));
        assertTrue(lines.contains("AnotherBundle [org.osgi.framework.BundleEvent] INFO : BundleEvent INSTALLED"));
        assertTrue(lines.contains("AnotherBundle [org.osgi.framework.BundleEvent] INFO : BundleEvent RESOLVED"));
        assertTrue(lines.contains("AnotherBundle [org.osgi.framework.BundleEvent] INFO : BundleEvent STARTED"));
    }

    @Test
    public void serviceRegistration() {
        // we should get a service registration event
        ServiceRegistration<?> sr = context.registerService(new String[] {
                FilenameFilter.class.getName(), Object.class.getName()
        }, (FilenameFilter)(dir, name) -> true, null);
        sr.unregister();

        List<String> lines = readLines();

        // verification of logging ServiceEvents
        assertTrue(lines.contains("PaxExam-Probe [org.osgi.framework.ServiceEvent] INFO : ServiceEvent REGISTERED - [java.io.FilenameFilter, java.lang.Object]"));
        assertTrue(lines.contains("PaxExam-Probe [org.osgi.framework.ServiceEvent] INFO : ServiceEvent UNREGISTERING - [java.io.FilenameFilter, java.lang.Object]"));
    }

}
