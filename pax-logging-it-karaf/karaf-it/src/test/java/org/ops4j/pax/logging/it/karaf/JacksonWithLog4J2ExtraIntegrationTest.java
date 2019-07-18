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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.logging.it.karaf.support.Helpers;
import org.osgi.framework.BundleException;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ops4j.pax.exam.OptionUtils.combine;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFilePut;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.replaceConfigurationFile;

@RunWith(PaxExam.class)
public class JacksonWithLog4J2ExtraIntegrationTest extends AbstractControlledIntegrationTestBase {

    public static Logger LOG = LoggerFactory.getLogger(JacksonWithLog4J2ExtraIntegrationTest.class);

    @Inject
    protected ConfigurationAdmin cm;

    @Configuration
    public Option[] configure() {
        return combine(
                baseConfigure(),

                replaceConfigurationFile("etc/log4j2-json.xml", new File("src/test/resources/etc/log4j2-json.xml")),
//                mavenBundle("com.fasterxml.jackson.core", "jackson-annotations").versionAsInProject(),
//                mavenBundle("com.fasterxml.jackson.core", "jackson-core").versionAsInProject(),
//                mavenBundle("com.fasterxml.jackson.core", "jackson-databind").versionAsInProject(),

                editConfigurationFilePut("etc/startup.properties", "mvn:org.ops4j.pax.logging/pax-logging-log4j2-extra/" + System.getProperty("version.pax-logging"), "11"),
                editConfigurationFilePut("etc/startup.properties", "mvn:com.fasterxml.jackson.core/jackson-annotations/" + System.getProperty("version.jackson"), "11"),
                editConfigurationFilePut("etc/startup.properties", "mvn:com.fasterxml.jackson.core/jackson-core/" + System.getProperty("version.jackson"), "11"),
                editConfigurationFilePut("etc/startup.properties", "mvn:com.fasterxml.jackson.core/jackson-databind/" + System.getProperty("version.jackson"), "11")
        );
    }

    @Test
    public void jsonLayoutButNoLog4j2ExtraFragment() throws BundleException, IOException {
        Helpers.updateLoggingConfig(context, cm, c -> {
            c.put("org.ops4j.pax.logging.log4j2.config.file", new File(System.getProperty("karaf.etc"), "log4j2-json.xml").getAbsolutePath());
        });
        LOG.info("Hello!");
    }

}
