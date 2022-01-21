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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
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
import org.ops4j.pax.logging.PaxLoggingConstants;
import org.ops4j.pax.logging.spi.PaxDefaultLogStreamProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
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
     * Reasonable defaults for default logging level (actually a threshold), framework logger level and usage
     * of file-based default/fallback logger.
     * @return
     */
    protected Option[] defaultLoggingConfig() {
        String fileName = null;
        try {
            fileName = new File(LOG_DIR, getClass().getSimpleName() + ".log").getCanonicalPath();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }

        return new Option[] {
                // every log with level higher or equal to DEBUG (i.e., not TRACE) will be logged
                frameworkProperty(PaxLoggingConstants.LOGGING_CFG_DEFAULT_LOG_LEVEL).value("DEBUG"),
                // threshold for R6 Compendium 101.6 logging statements
                // (from framework/bundle/service events)
                frameworkProperty(PaxLoggingConstants.LOGGING_CFG_FRAMEWORK_EVENTS_LOG_LEVEL).value("DISABLED"),
                // default log will be written to file which we can safely read without failsafe-maven-plugin
                // synchronization problems
                frameworkProperty(PaxLoggingConstants.LOGGING_CFG_USE_FILE_FALLBACK_LOGGER).value(fileName),
                // treat configuration errors in log4j as exceptions to catch them through EventAdmin
                frameworkProperty(PaxLoggingConstants.LOGGING_CFG_LOG4J2_ERRORS_AS_EXCEPTION).value("true")
        };
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

    /**
     * Reads log lines written to test class-related log file
     * (see {@link PaxLoggingConstants#LOGGING_CFG_USE_FILE_FALLBACK_LOGGER}).
     * @return
     */
    protected List<String> readLines() {
        try {
            // flush the underlying, cleverly exposed PrintStream
            ServiceReference<PaxDefaultLogStreamProvider> ref = context.getServiceReference(PaxDefaultLogStreamProvider.class);
            if (ref != null) {
                PaxDefaultLogStreamProvider provider = context.getService(ref);
                if (provider != null && provider.stream() != null) {
                    provider.stream().flush();
                }
            }

            return readLines(new FileInputStream(new File(LOG_DIR, getClass().getSimpleName() + ".log").getCanonicalPath()), 0);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Return log lines from named file
     * @param file
     * @return
     */
    protected List<String> readLines(String file) {
        try {
            return readLines(new FileInputStream(file), 0);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Returns log lines from any {@link InputStream}
     * @param input
     * @param trim how many chars from the beginning of each line to trim?
     * @return
     */
    protected List<String> readLines(InputStream input, int trim) {
        try {
            InputStreamReader isReader = new InputStreamReader(input, StandardCharsets.UTF_8);
            List<String> lines;
            try (BufferedReader reader = new BufferedReader(isReader)) {
                lines = new ArrayList<>();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            }
            lines = lines.stream()
                    .map(l -> trim > 0 && l.length() > trim ? l.substring(trim) : l)
                    .collect(Collectors.toList());
            return lines;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
