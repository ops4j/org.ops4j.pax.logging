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
import org.ops4j.pax.logging.spi.PaxAppender;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.ops4j.pax.exam.OptionUtils.combine;

/**
 * Testing appenders that are registered as OSGi services of {@link PaxAppender}
 * interface.
 */
@RunWith(PaxExam.class)
public class LogbackOsgiAppendersIntegrationTest extends AbstractStdoutInterceptingIntegrationTestBase {

    @Inject
    private ConfigurationAdmin cm;

    @Configuration
    public Option[] configure() throws IOException {
        return combine(
                combine(baseConfigure(), defaultLoggingConfig()),

                paxLoggingApi(),
                paxLoggingLogback(),
                configAdmin(),
                eventAdmin()
        );
    }

    @Test
    public void customAppender() {
        Hashtable<String, Object> properties = new Hashtable<>();
        properties.put(PaxLoggingConstants.SERVICE_PROPERTY_APPENDER_NAME_PROPERTY, "custom");
        MyPaxAppender appender = new MyPaxAppender();
        context.registerService(PaxAppender.class, appender, properties);

        Helpers.updateLoggingConfig(context, cm, Helpers.LoggingLibrary.LOGBACK, "custom");

        Logger log = LoggerFactory.getLogger("my.logger");
        log.info("should go to custom appender");

        assertThat(appender.getEvents().size(), equalTo(1));
        assertThat(appender.getEvents().get(0).getMessage(), equalTo("should go to custom appender"));
    }

    private static class MyPaxAppender implements PaxAppender {

        private final List<PaxLoggingEvent> events = new LinkedList<>();

        @Override
        public void doAppend(PaxLoggingEvent event) {
            event.getProperties(); // ensure MDC properties are copied
            events.add(event);
        }

        public List<PaxLoggingEvent> getEvents() {
            return events;
        }
    }

}
