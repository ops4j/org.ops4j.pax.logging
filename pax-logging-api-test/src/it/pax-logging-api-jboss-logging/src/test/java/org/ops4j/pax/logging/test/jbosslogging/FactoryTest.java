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
import org.jboss.logging.NDC;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class FactoryTest {

    @Test
    public void paxLoggingSpecificJBossLoggingFactory() {
        Logger log = Logger.getLogger(this.getClass());
        MDC.put("mdc", "value1");
        NDC.push("value2");

        log.info("Log: " + log);

        // pax-logging version here
        assertTrue(log.getClass().getName().startsWith("org.ops4j.pax.logging.jbosslogging"));
    }

}
