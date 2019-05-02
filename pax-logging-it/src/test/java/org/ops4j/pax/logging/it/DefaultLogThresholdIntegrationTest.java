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
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.logging.PaxLoggingConstants;
import org.ops4j.pax.tinybundles.core.TinyBundles;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ops4j.pax.exam.CoreOptions.frameworkProperty;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.OptionUtils.combine;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class DefaultLogThresholdIntegrationTest extends AbstractControlledIntegrationTestBase {

    @Inject
    private BundleContext context;

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

        return combine(
                baseConfigure(),
                mavenBundle("org.ops4j.pax.logging", "pax-logging-api").versionAsInProject(),

                // every log with level higher or equal to INFO (i.e., not TRACE and not DEBUG) will be logged
                frameworkProperty(PaxLoggingConstants.LOGGING_CFG_DEFAULT_LOG_LEVEL).value("INFO"),
                // level at which OSGi R6 Compendium 101.6 logging statements will be printed
                // (from framework/bundle/service events) - with the above threshold, there won't be such logs
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
    }

    @Test
    public void bundleInstallation() throws BundleException, FileNotFoundException {
        // we should get a bundle even about installing new bundle, but it shouldn't be logged due to threshold
        Bundle b1 = context.installBundle("test:1", new FileInputStream("target/bundles/sample-bundle1.jar"));
        Bundle b2 = context.installBundle("test:2", new FileInputStream("target/bundles/sample-bundle2.jar"));
        b2.start();
    }

}
