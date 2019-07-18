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
import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.karaf.options.DoNotModifyLogOption;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
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
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.systemTimeout;
import static org.ops4j.pax.exam.CoreOptions.url;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.debugConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFilePut;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;

/**
 * Base class for all Karaf integration tests - manually sets up pax-exam configuration (without implicit configuration).
 */
@ExamReactorStrategy(PerClass.class)
public class AbstractControlledIntegrationTestBase {

    public static final Logger LOG = LoggerFactory.getLogger(AbstractControlledIntegrationTestBase.class);
    public static final String PROBE_SYMBOLIC_NAME = "PaxExam-Probe";

    // location of where pax-logging-api will have output file written according to
    // "org.ops4j.pax.logging.useFileLogFallback" system/context property
    // filename will match test class name with ".log" extension
    static final File LOG_DIR = new File("target/logs-default");

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
        LOG_DIR.mkdirs();
        String fileName = null;
        try {
            fileName = new File(LOG_DIR, getClass().getSimpleName() + ".log").getCanonicalPath();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }

        MavenArtifactUrlReference karaf = maven()
                .groupId("org.ops4j.pax.logging.karaf")
                .artifactId("karaf-distribution")
                .type("zip")
                .versionAsInProject();

        Option[] options = new Option[] {
                // basic options
                bootDelegationPackage("sun.*"),
                bootDelegationPackage("com.sun.*"),

                frameworkStartLevel(START_LEVEL_TEST_BUNDLE),

                cleanCaches(false),
                keepRuntimeFolder(),
                systemTimeout(60 * 60 * 1000),

                karafDistributionConfiguration()
                        .frameworkUrl(karaf)
                        .unpackDirectory(new File("target/paxexam"))
                        .useDeployFolder(false),
                new DoNotModifyLogOption(),

                debugConfiguration("9999", false),

                // set to "4" to see Felix wiring information
                frameworkProperty("felix.log.level").value("1"),

                editConfigurationFilePut("etc/system.properties", "org.ops4j.pax.logging.DefaultServiceLog.level", "INFO"),
                editConfigurationFilePut("etc/custom.properties", "org.ops4j.pax.logging.service.frameworkEventsLogLevel", "DISABLED"),
                editConfigurationFilePut("etc/custom.properties", "org.ops4j.pax.logging.useFileLogFallback", fileName),

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

}
