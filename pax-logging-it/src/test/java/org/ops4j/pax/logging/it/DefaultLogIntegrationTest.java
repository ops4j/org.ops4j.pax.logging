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
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
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
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ops4j.pax.exam.CoreOptions.frameworkProperty;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.OptionUtils.combine;

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
 * r: jar:file:~/.m2/repository/org/slf4j/slf4j-api/1.7.26/slf4j-api-1.7.26.jar!/org/slf4j/Logger.class
 * </pre>
 * That's why even pax-exam logs go through pax-logging-api's {@link org.ops4j.pax.logging.internal.DefaultServiceLog}.</p>
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class DefaultLogIntegrationTest extends AbstractControlledIntegrationTestBase {

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

                // every log with level higher or equal to DEBUG (i.e., not TRACE) will be logged
                frameworkProperty(PaxLoggingConstants.LOGGING_CFG_DEFAULT_LOG_LEVEL).value("DEBUG"),
                // level at which OSGi R6 Compendium 101.6 logging statements will be printed
                // (from framework/bundle/service events)
                frameworkProperty(PaxLoggingConstants.LOGGING_CFG_FRAMEWORK_EVENTS_LOG_LEVEL).value("DEBUG")
        );
    }

    @Test
    public void logBundleInformation() {
        Set<Bundle> bundles = new TreeSet<>((b1, b2) -> (int) (b1.getBundleId() - b2.getBundleId()));
        bundles.addAll(Arrays.asList(context.getBundles()));
        for (Bundle b : bundles) {
            String info = String.format("#%d: %s (%s)", b.getBundleId(), b.getSymbolicName(), b.getLocation());
            LOG.debug(info);
        }
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
    }

    @Test
    public void bundleInstallation() throws BundleException, FileNotFoundException {
        // we should get a bundle even about installing new bundle
        Bundle b1 = context.installBundle("test:1", new FileInputStream("target/bundles/sample-bundle1.jar"));
        Bundle b2 = context.installBundle("test:2", new FileInputStream("target/bundles/sample-bundle2.jar"));
        b2.start();
    }

    @Test
    public void serviceRegistration() {
        // we should get a service registration event
        ServiceRegistration<?> sr = context.registerService(new String[] {
                FilenameFilter.class.getName(), Object.class.getName()
        }, (FilenameFilter)(dir, name) -> true, null);
        sr.unregister();
    }

}
