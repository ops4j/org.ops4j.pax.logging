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
import java.util.LinkedList;
import java.util.List;
import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.logging.PaxLoggingConstants;
import org.ops4j.pax.logging.it.support.Helpers;
import org.ops4j.pax.logging.spi.PaxErrorHandler;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.OptionUtils.combine;

/**
 * Testing error handlers that are registered as OSGi services of {@link PaxErrorHandler}
 * interface.
 */
@RunWith(PaxExam.class)
public class Log4J1OsgiErrorHandlersIntegrationTest extends AbstractStdoutInterceptingIntegrationTestBase {

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

                // fragment for pax-logging-log4j1
                mavenBundle("org.ops4j.pax.logging", "pax-logging-sample-fragment").versionAsInProject().noStart()
        );
    }

    @Test
    public void customErrorHandler() {
        Hashtable<String, Object> properties = new Hashtable<>();
        properties.put(PaxLoggingConstants.SERVICE_PROPERTY_ERRORHANDLER_NAME_PROPERTY, "custom");
        MyPaxErrorHandler errorHandler = new MyPaxErrorHandler();
        context.registerService(PaxErrorHandler.class, errorHandler, properties);

        Helpers.updateLoggingConfig(context, cm, Helpers.LoggingLibrary.LOG4J1, "custom.eh");

        Logger log = LoggerFactory.getLogger("my.logger");
        log.info("should go to failing appender");

        assertThat(errorHandler.getMessages().size(), equalTo(1));
        assertThat(errorHandler.getMessages().get(0), equalTo("Don't log through me!"));
        assertThat(errorHandler.getExceptions().size(), equalTo(1));
    }

    private static class MyPaxErrorHandler implements PaxErrorHandler {

        private List<String> messages = new LinkedList<>();
        private List<Exception> exceptions = new LinkedList<>();

        @Override
        public void error(String message, Exception e) {
            messages.add(message);
            exceptions.add(e);
        }

        public List<String> getMessages() {
            return messages;
        }

        public List<Exception> getExceptions() {
            return exceptions;
        }
    }

}
