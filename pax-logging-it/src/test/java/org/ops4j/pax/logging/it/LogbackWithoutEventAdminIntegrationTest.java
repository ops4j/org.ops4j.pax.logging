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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.ops4j.pax.exam.OptionUtils.combine;
import static org.ops4j.pax.logging.it.support.Helpers.getField;

@RunWith(PaxExam.class)
public class LogbackWithoutEventAdminIntegrationTest extends AbstractStdoutInterceptingIntegrationTestBase {

    @Configuration
    public Option[] configure() throws IOException {
        return combine(
                combine(baseConfigure(), defaultLoggingConfig()),

                paxLoggingApi(),
                paxLoggingLogback(),
                configAdmin()
        );
    }

    @Test
    public void noEventAdmin() throws IOException {
        Logger log = LoggerFactory.getLogger(LogbackWithoutEventAdminIntegrationTest.class);
        assertNotNull(log);
        assertThat(log.getClass().getName(), equalTo("org.ops4j.pax.logging.slf4j.Slf4jLogger"));
        log.info("simplestUsage - INFO");
        assertThat(getField(log, "m_delegate").getClass().getName(), equalTo("org.ops4j.pax.logging.internal.TrackingLogger"));
    }

}
