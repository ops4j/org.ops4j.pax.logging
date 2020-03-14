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

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.options.MavenArtifactProvisionOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ops4j.pax.exam.Constants.START_LEVEL_SYSTEM_BUNDLES;
import static org.ops4j.pax.exam.Constants.START_LEVEL_TEST_BUNDLE;
import static org.ops4j.pax.exam.CoreOptions.bootDelegationPackage;
import static org.ops4j.pax.exam.CoreOptions.cleanCaches;
import static org.ops4j.pax.exam.CoreOptions.frameworkProperty;
import static org.ops4j.pax.exam.CoreOptions.frameworkStartLevel;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.linkBundle;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.systemTimeout;
import static org.ops4j.pax.exam.CoreOptions.url;
import static org.ops4j.pax.exam.CoreOptions.workingDirectory;

/**
 * Base class for all integration tests - manually sets up pax-exam configuration (without implicit configuration).
 * {@link PerClass} strategy is needed. maven-failsafe-plugin's {@code reuseForks=false} and {@code forkCount=1} is
 * not enough to properly clean up JVM between methods and we may miss some URL handlers, etc. In other words - don't
 * use {@link org.ops4j.pax.exam.spi.reactors.PerMethod}.
 */
@ExamReactorStrategy(PerClass.class)
public class AbstractControlledIntegrationTestBase {

    public static final Logger LOG = LoggerFactory.getLogger(AbstractControlledIntegrationTestBase.class);
    public static final String PROBE_SYMBOLIC_NAME = "PaxExam-Probe";

    @Rule
    public TestName testName = new TestName();

    @Inject
    protected BundleContext context;

    @Before
    public void beforeEach() {
        LOG.info("========== Running {}.{}() ==========", getClass().getName(), testName.getMethodName());
    }

    @After
    public void afterEach() {
        LOG.info("========== Finished {}.{}() ==========", getClass().getName(), testName.getMethodName());
    }

    protected Option[] baseConfigure() {
        Option[] options = new Option[] {
                // basic options
                bootDelegationPackage("com.sun.*"),

                frameworkStartLevel(START_LEVEL_TEST_BUNDLE),

                workingDirectory("target/paxexam"),
                cleanCaches(true),
                systemTimeout(60 * 60 * 1000),

                // set to "4" to see Felix wiring information
                frameworkProperty("felix.log.level").value("1"),

                // added implicitly by pax-exam, if pax.exam.system=test
                // these resources are provided inside org.ops4j.pax.exam:pax-exam-link-mvn jar
                // for example, "link:classpath:META-INF/links/org.ops4j.base.link" = "mvn:org.ops4j.base/ops4j-base/1.5.0"
                url("link:classpath:META-INF/links/org.ops4j.base.link").startLevel(START_LEVEL_SYSTEM_BUNDLES),
                url("link:classpath:META-INF/links/org.ops4j.pax.swissbox.core.link").startLevel(START_LEVEL_SYSTEM_BUNDLES),
                url("link:classpath:META-INF/links/org.ops4j.pax.swissbox.extender.link").startLevel(START_LEVEL_SYSTEM_BUNDLES),
                url("link:classpath:META-INF/links/org.ops4j.pax.swissbox.framework.link").startLevel(START_LEVEL_SYSTEM_BUNDLES),
                url("link:classpath:META-INF/links/org.ops4j.pax.swissbox.lifecycle.link").startLevel(START_LEVEL_SYSTEM_BUNDLES),
                url("link:classpath:META-INF/links/org.ops4j.pax.swissbox.tracker.link").startLevel(START_LEVEL_SYSTEM_BUNDLES),
                url("link:classpath:META-INF/links/org.ops4j.pax.exam.link").startLevel(START_LEVEL_SYSTEM_BUNDLES),
                url("link:classpath:META-INF/links/org.ops4j.pax.exam.inject.link").startLevel(START_LEVEL_SYSTEM_BUNDLES),
                url("link:classpath:META-INF/links/org.ops4j.pax.extender.service.link").startLevel(START_LEVEL_SYSTEM_BUNDLES),

                linkBundle("org.apache.servicemix.bundles.javax-inject").startLevel(START_LEVEL_SYSTEM_BUNDLES),

                junitBundles(),
        };
        return options;
    }

    /**
     * Configuring symbolic name in test probe we can easily locate related log entries in the output.
     * @param builder
     * @return
     */
    @ProbeBuilder
    public TestProbeBuilder probeBuilder(TestProbeBuilder builder) {
        builder.setHeader(Constants.BUNDLE_SYMBOLICNAME, PROBE_SYMBOLIC_NAME);
        return builder;
    }

    protected MavenArtifactProvisionOption paxLoggingApi() {
        return mavenBundle("org.ops4j.pax.logging", "pax-logging-api")
                .version(System.getProperty("version.pax-logging")).startLevel(START_LEVEL_TEST_BUNDLE - 1).start();
    }

    protected MavenArtifactProvisionOption paxLoggingLog4J1() {
        return mavenBundle("org.ops4j.pax.logging", "pax-logging-service")
                .version(System.getProperty("version.pax-logging")).startLevel(START_LEVEL_TEST_BUNDLE - 1).start();
    }

    protected MavenArtifactProvisionOption paxLoggingLog4J2() {
        return mavenBundle("org.ops4j.pax.logging", "pax-logging-log4j2")
                .version(System.getProperty("version.pax-logging")).startLevel(START_LEVEL_TEST_BUNDLE - 1).start();
    }

    protected MavenArtifactProvisionOption paxLoggingLog4J2Extra() {
        return mavenBundle("org.ops4j.pax.logging", "pax-logging-log4j2-extra")
                .version(System.getProperty("version.pax-logging")).startLevel(START_LEVEL_TEST_BUNDLE - 1).noStart();
    }

    protected MavenArtifactProvisionOption paxLoggingLogback() {
        return mavenBundle("org.ops4j.pax.logging", "pax-logging-logback")
                .version(System.getProperty("version.pax-logging")).startLevel(START_LEVEL_TEST_BUNDLE - 1).start();
    }

    protected MavenArtifactProvisionOption configAdmin() {
        return mavenBundle("org.apache.felix", "org.apache.felix.configadmin")
                .versionAsInProject().startLevel(START_LEVEL_SYSTEM_BUNDLES).start();
    }

    protected MavenArtifactProvisionOption eventAdmin() {
        return mavenBundle("org.apache.felix", "org.apache.felix.eventadmin")
                .versionAsInProject().startLevel(START_LEVEL_SYSTEM_BUNDLES).start();
    }

}
