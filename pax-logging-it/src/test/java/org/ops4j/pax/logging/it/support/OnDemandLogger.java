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
package org.ops4j.pax.logging.it.support;

import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class uses logger with known {@code org.ops4j.test} name and we'll try to call it through different bundles.
 */
public class OnDemandLogger implements Consumer<String> {

    public static Logger SLF4J_LOG = LoggerFactory.getLogger("org.ops4j.test");
    public static org.apache.logging.log4j.Logger LOG4J2_LOG = LogManager.getLogger("org.ops4j.test");

    @Override
    public void accept(String message) {
        String sn = FrameworkUtil.getBundle(this.getClass()).getSymbolicName();
        SLF4J_LOG.info("slf4j: sn=" + sn + ", message=" + message);
        LOG4J2_LOG.info("log4j2: sn=" + sn + ", message=" + message);
    }

}
