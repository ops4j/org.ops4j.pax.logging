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

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.logging.PaxLoggingConstants;
import org.ops4j.pax.logging.it.support.Helpers;
import org.ops4j.pax.logging.spi.PaxLayout;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.OptionUtils.combine;

/**
 * <p>Testing layouts that are registered as OSGi services of {@link PaxLayout}
 * interface.</p>
 */
@RunWith(PaxExam.class)
public class Log4J1OsgiLayoutsIntegrationTest extends AbstractStdoutInterceptingIntegrationTestBase {

    @Inject
    private ConfigurationAdmin cm;

    @Configuration
    public Option[] configure() throws IOException {
        return combine(
                combine(baseConfigure(), defaultLoggingConfig()),

                paxLoggingApi(),
                paxLoggingLog4J1(),
                configAdmin(),
                eventAdmin(),

                // fragment for pax-logging-log4j1, so we can directly use classes from this bundle as
                // appenders
                mavenBundle("org.ops4j.pax.logging", "pax-logging-sample-fragment").versionAsInProject().noStart()
        );
    }

    @Test
    public void customLayout() {
        Hashtable<String, Object> properties = new Hashtable<>();
        properties.put(PaxLoggingConstants.SERVICE_PROPERTY_LAYOUT_NAME_PROPERTY, "custom");
        MyPaxLayout layout = new MyPaxLayout();
        context.registerService(PaxLayout.class, layout, properties);

        Helpers.updateLoggingConfig(context, cm, Helpers.LoggingLibrary.LOG4J1, "custom.layout");

        Logger log = LoggerFactory.getLogger("my.logger");
        log.info("should go to custom layout");

        List<String> lines = readLines();
        assertTrue(lines.contains("[my.logger] : [should go to custom layout]"));
    }

    @Test
    public void fallbackLayout() {
        // not registering any layout - fallback one should be used

        Helpers.updateLoggingConfig(context, cm, Helpers.LoggingLibrary.LOG4J1, "custom.fallback.layout");

        Logger log = LoggerFactory.getLogger("my.logger");
        log.info("should go to fallback layout");

        List<String> lines = readLines();
        assertTrue(lines.contains(">>> should go to fallback layout (Plain and simple parameter)"));
    }

    private static class MyPaxLayout implements PaxLayout {

        @Override
        public String doLayout(PaxLoggingEvent event) {
            return "[" + event.getLoggerName() + "] : [" + event.getRenderedMessage() + "]\n";
        }

        @Override
        public String getContentType() {
            return null;
        }

        @Override
        public String getHeader() {
            return null;
        }

        @Override
        public String getFooter() {
            return null;
        }
    }

}
