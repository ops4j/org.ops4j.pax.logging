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
package org.ops4j.pax.logging.test.jbosslogging;

import org.jboss.logging.Logger;
import org.jboss.logging.MDC;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class JBossLoggingNativeApiTest {

    @Test
    public void simplestUsage() {
        Logger log = Logger.getLogger(JBossLoggingNativeApiTest.class);
        MDC.put("user", "me");
        MDC.put("country", "Equestria");

        log.info("INFO");
        log.trace("TRACE");

        assertThat(log.getClass().getName(), equalTo("org.jboss.logging.JDKLogger"));

        Logger.getLogger("special").trace("TRACE");
    }

}
