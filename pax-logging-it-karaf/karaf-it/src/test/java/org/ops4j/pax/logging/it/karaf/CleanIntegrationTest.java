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

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ops4j.pax.exam.OptionUtils.combine;

/**
 * Clean test without any pax-logging bundles. Just simplest showcase of what's needed to run
 * manually controlled pax-exam test with Karaf.
 */
@RunWith(PaxExam.class)
public class CleanIntegrationTest extends AbstractControlledIntegrationTestBase {

    public static Logger LOG = LoggerFactory.getLogger(CleanIntegrationTest.class);

    @Configuration
    public Option[] configure() {
        return combine(
                baseConfigure()
        );
    }

    @Test
    public void justRun() {
        Set<Bundle> bundles = new TreeSet<>((b1, b2) -> (int) (b1.getBundleId() - b2.getBundleId()));
        bundles.addAll(Arrays.asList(context.getBundles()));
        for (Bundle b : bundles) {
            String info = String.format("#%d: %s (%s)", b.getBundleId(), b.getSymbolicName(), b.getLocation());
            LOG.info(info);
        }
    }

}
