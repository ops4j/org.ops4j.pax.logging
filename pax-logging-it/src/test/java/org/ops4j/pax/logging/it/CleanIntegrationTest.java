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

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.OptionUtils.combine;

/**
 * Clean test without any pax-logging bundles. Just simplest showcase of what's needed to run
 * manually controlled pax-exam test.
 */
@RunWith(PaxExam.class)
@Ignore("This test requires Slf4j 1.7 bundles installed for org.ops4j.pax.extender.service")
public class CleanIntegrationTest extends AbstractControlledIntegrationTestBase {

    public static Logger LOG = LoggerFactory.getLogger(CleanIntegrationTest.class);

    @Configuration
    public Option[] configure() {
        return combine(
                baseConfigure(),
                // pax-exam-extender-service bundle (org.ops4j.pax.exam.raw.extender.intern.Parser) requires
                // SLF4J API:
                // [org.ops4j.pax.exam.extender.service [9](R 9.0)] osgi.wiring.package; (&(osgi.wiring.package=org.slf4j)(version>=1.4.0)(!(version>=2.0.0)))
                mavenBundle("org.slf4j", "slf4j-api", System.getProperty("version.org.slf4j")),
                // while SLF4J API requires implementation (could be a fragment)
                mavenBundle("org.slf4j", "slf4j-nop", System.getProperty("version.org.slf4j")).start(false)
        );
    }

    @Test
    public void justRun() {
        Set<Bundle> bundles = new TreeSet<>((b1, b2) -> (int) (b1.getBundleId() - b2.getBundleId()));
        bundles.addAll(Arrays.asList(context.getBundles()));
        for (Bundle b : bundles) {
            String info = String.format("#%d: %s (%s)", b.getBundleId(), b.getSymbolicName(), b.getLocation());
            // System.out.println, thanks to failsafe's <redirectTestOutputToFile> will land in
            // pax-logging-it/target/failsafe-reports/org.ops4j.pax.logging.it.CleanIntegrationTest-output.txt
            System.out.println(info);
            // slf4j-nop logger won't do anything
            LOG.info(info);
        }
    }

}
