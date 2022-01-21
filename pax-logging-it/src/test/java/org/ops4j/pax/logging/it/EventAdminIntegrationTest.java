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
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.logging.it.support.Helpers;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.ops4j.pax.exam.OptionUtils.combine;

@RunWith(PaxExam.class)
public class EventAdminIntegrationTest extends AbstractControlledIntegrationTestBase {

    @Configuration
    public Option[] configure() throws IOException {
        return combine(
                combine(baseConfigure(), defaultLoggingConfig()),

                paxLoggingApi(),
                paxLoggingLogback().noStart(),
                paxLoggingLog4J2().noStart(),
                configAdmin(),
                eventAdmin()
        );
    }

    @Test
    public void eventAdminNotificationFromAll2Backends() throws Exception {
        Logger logger = LoggerFactory.getLogger(EventAdminIntegrationTest.class);

        Bundle paxLoggingLogback = Helpers.paxLoggingLogback(context);
        Bundle paxLoggingLog4J2 = Helpers.paxLoggingLog4j2(context);

        Bundle probe = FrameworkUtil.getBundle(this.getClass());

        final List<Event> events = new CopyOnWriteArrayList<>();
        Dictionary<String, Object> props = new Hashtable<>();
        props.put(EventConstants.EVENT_TOPIC, new String[] {
                "org/osgi/service/log/LogEntry/LOG_AUDIT",
                "org/osgi/service/log/LogEntry/LOG_ERROR",
                "org/osgi/service/log/LogEntry/LOG_WARNING",
                "org/osgi/service/log/LogEntry/LOG_INFO",
                "org/osgi/service/log/LogEntry/LOG_DEBUG",
                "org/osgi/service/log/LogEntry/LOG_OTHER"
        });
        context.registerService(EventHandler.class, events::add, props);

        logger.info("when no backend available");

        paxLoggingLogback.start();

        logger.info("when logback available (info)");
        logger.error("when logback available (error)", new Exception("exception 2"));

        paxLoggingLogback.stop();
        paxLoggingLog4J2.start();

        logger.info("when log4j2 available (info)");
        logger.error("when log4j2 available (error)", new Exception("exception 3"));

        Event e22 = ev(events, "when logback available (info)");
        Event e23 = ev(events, "when logback available (error)");
        Event e32 = ev(events, "when log4j2 available (info)");
        Event e33 = ev(events, "when log4j2 available (error)");

        assertThat(e22.getProperty("bundle"), equalTo(probe));
        assertThat(e23.getProperty("bundle"), equalTo(probe));
        assertThat(e32.getProperty("bundle"), equalTo(probe));
        assertThat(e33.getProperty("bundle"), equalTo(probe));

        assertThat(e22.getTopic(), equalTo("org/osgi/service/log/LogEntry/LOG_INFO"));
        assertThat(e23.getTopic(), equalTo("org/osgi/service/log/LogEntry/LOG_ERROR"));
        assertThat(e32.getTopic(), equalTo("org/osgi/service/log/LogEntry/LOG_INFO"));
        assertThat(e33.getTopic(), equalTo("org/osgi/service/log/LogEntry/LOG_ERROR"));

        assertThat(e22.getProperty("log.level"), equalTo(3));
        assertThat(e23.getProperty("log.level"), equalTo(1));
        assertThat(e32.getProperty("log.level"), equalTo(3));
        assertThat(e33.getProperty("log.level"), equalTo(1));

        assertThat(((Exception)e23.getProperty("exception")).getMessage(), equalTo("exception 2"));
        assertThat(((Exception)e33.getProperty("exception")).getMessage(), equalTo("exception 3"));
    }

    private Event ev(List<Event> events, String msg) {
        return events.stream().filter(e -> e.getProperty("message").equals(msg)).findFirst().orElse(null);
    }

}
