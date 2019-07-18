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
package org.ops4j.pax.logging.it.karaf;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.logging.it.karaf.support.Helpers;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.wiring.FrameworkWiring;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.OptionUtils.combine;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFilePut;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.replaceConfigurationFile;

/**
 * Clean test without any pax-logging bundles. Just simplest showcase of what's needed to run
 * manually controlled pax-exam test with Karaf.
 */
@RunWith(PaxExam.class)
public class JacksonWithLog4J2ExtraAndRefreshIntegrationTest extends AbstractControlledIntegrationTestBase {

    public static Logger LOG = LoggerFactory.getLogger(JacksonWithLog4J2ExtraAndRefreshIntegrationTest.class);

    @Inject
    protected ConfigurationAdmin cm;

    @Configuration
    public Option[] configure() {
        return combine(
                baseConfigure(),

                replaceConfigurationFile("etc/log4j2-json.xml", new File("src/test/resources/etc/log4j2-json.xml")),
                mavenBundle("com.fasterxml.jackson.core", "jackson-annotations").versionAsInProject(),
                mavenBundle("com.fasterxml.jackson.core", "jackson-core").versionAsInProject(),
                mavenBundle("com.fasterxml.jackson.core", "jackson-databind").versionAsInProject(),

                editConfigurationFilePut("etc/startup.properties", "mvn:org.ops4j.pax.logging/pax-logging-log4j2-extra/" + System.getProperty("version.pax-logging"), "13")
        );
    }

    @Test
    public void jsonLayoutButNoLog4j2ExtraFragment() throws BundleException, IOException, InterruptedException {
        refreshPaxLoggingLog4j2();
        Helpers.updateLoggingConfig(context, cm, c -> {
            c.put("org.ops4j.pax.logging.log4j2.config.file", new File(System.getProperty("karaf.etc"), "log4j2-json.xml").getAbsolutePath());
        });
        LOG.info("Hello!");
    }


    private void refreshPaxLoggingLog4j2() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        Optional<Bundle> ob = Arrays.stream(context.getBundles())
                .filter(b -> "org.ops4j.pax.logging.pax-logging-log4j2".equals(b.getSymbolicName()))
                .findFirst();
        Bundle paxLoggingLog4j2 = ob.orElse(null);
        if (paxLoggingLog4j2 != null) {
            FrameworkWiring wiring = paxLoggingLog4j2.getBundleContext().getBundle(0L).adapt(FrameworkWiring.class);
            wiring.refreshBundles(Collections.singletonList(paxLoggingLog4j2), event -> {
                if (event.getType() == FrameworkEvent.PACKAGES_REFRESHED) {
                    latch.countDown();
                }
            });
        }
        assertTrue(latch.await(10, TimeUnit.SECONDS));
    }

}
