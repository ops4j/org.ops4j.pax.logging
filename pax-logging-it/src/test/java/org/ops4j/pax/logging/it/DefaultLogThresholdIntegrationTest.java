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
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.logging.PaxLoggingConstants;
import org.ops4j.pax.tinybundles.core.TinyBundles;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.frameworkProperty;
import static org.ops4j.pax.exam.OptionUtils.combine;

@RunWith(PaxExam.class)
public class DefaultLogThresholdIntegrationTest extends AbstractStdoutInterceptingIntegrationTestBase {

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

                // every log with level higher or equal to INFO (i.e., not TRACE and not DEBUG) will be logged
                frameworkProperty(PaxLoggingConstants.LOGGING_CFG_DEFAULT_LOG_LEVEL).value("INFO"),
                // level at which OSGi R6 Compendium 101.6 logging statements will be printed
                frameworkProperty(PaxLoggingConstants.LOGGING_CFG_FRAMEWORK_EVENTS_LOG_LEVEL).value("DEBUG")
        );
    }

    @Test
    public void logAtDifferentLevels() {
        Logger log = LoggerFactory.getLogger(DefaultLogThresholdIntegrationTest.class);
        log.error("DefaultLogThresholdIntegrationTest/ERROR");
        log.warn("DefaultLogThresholdIntegrationTest/WARN");
        log.info("DefaultLogThresholdIntegrationTest/INFO");
        // these should not be logged
        log.debug("DefaultLogThresholdIntegrationTest/DEBUG");
        log.trace("DefaultLogThresholdIntegrationTest/TRACE");

        List<String> lines = readLines();

        // verification of log levels with fallback logger (stdout, but redirected to file)
        assertTrue(lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.DefaultLogThresholdIntegrationTest] ERROR : DefaultLogThresholdIntegrationTest/ERROR"));
        assertTrue(lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.DefaultLogThresholdIntegrationTest] WARN : DefaultLogThresholdIntegrationTest/WARN"));
        assertTrue(lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.DefaultLogThresholdIntegrationTest] INFO : DefaultLogThresholdIntegrationTest/INFO"));
        assertFalse(lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.DefaultLogThresholdIntegrationTest] DEBUG : DefaultLogThresholdIntegrationTest/DEBUG"));
        assertFalse(lines.contains("PaxExam-Probe [org.ops4j.pax.logging.it.DefaultLogThresholdIntegrationTest] TRACE : DefaultLogThresholdIntegrationTest/TRACE"));
    }

    @Test
    public void bundleInstallation() throws BundleException, FileNotFoundException, MalformedURLException {
        // we should get a bundle even about installing new bundle, but it shouldn't be logged due to threshold
        File f1 = new File("target/bundles/sample-bundle1.jar");
        File f2 = new File("target/bundles/sample-bundle2.jar");
        Bundle b1 = context.installBundle(f1.toURI().toURL().toString(), new FileInputStream(f1));
        Bundle b2 = context.installBundle(f2.toURI().toURL().toString(), new FileInputStream(f2));
        b2.start();

        List<String> lines = readLines();

        // verification of logging BundleEvents
        assertFalse(lines.contains("MySpecialBundleThatShouldTriggerBundleEventWhenInstalling [org.osgi.framework.BundleEvent] DEBUG : BundleEvent INSTALLED"));
        assertFalse(lines.contains("MySpecialBundleThatShouldTriggerBundleEventWhenInstalling [org.osgi.framework.BundleEvent] DEBUG : BundleEvent STARTED"));
        assertFalse(lines.contains("AnotherBundle [org.osgi.framework.BundleEvent] DEBUG : BundleEvent INSTALLED"));
        assertFalse(lines.contains("AnotherBundle [org.osgi.framework.BundleEvent] DEBUG : BundleEvent STARTED"));
    }

}
