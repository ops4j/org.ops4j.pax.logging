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
package org.apache.logging.log4j.util;

/**
 * High priority {@link PropertySource} to ensure that user won't turn on (or off) anything that could break
 * pax-logging-log4j2.
 * See http://logging.apache.org/log4j/2.x/manual/configuration.html#SystemProperties
 */
public class PaxPropertySource implements PropertySource {

    private static final String PREFIX = "log4j2.";

    public static boolean debug;
    public static String defaultLevel;

    @Override
    public int getPriority() {
        // higher than org.apache.logging.log4j.util.SystemPropertiesPropertySource.DEFAULT_PRIORITY
        return 200;
    }

    @Override
    public void forEach(BiConsumer<String, String> action) {
        action.accept(Constants.LOG4J2_DEBUG, Boolean.toString(debug));
        action.accept("log4j2.enable.threadlocals", Boolean.FALSE.toString());
        action.accept("log4j2.is.webapp", Boolean.FALSE.toString());
        action.accept("log4j2.shutdownHookEnabled", Boolean.FALSE.toString());
        action.accept("log4j2.level", defaultLevel);
        action.accept("log4j2.disableJmx", Boolean.TRUE.toString());
        action.accept("log4j2.skipJansi", Boolean.TRUE.toString());
        // log4j2.isThreadContextMapInheritable - https://github.com/ops4j/org.ops4j.pax.logging/pull/38
    }

    @Override
    public CharSequence getNormalForm(Iterable<? extends CharSequence> tokens) {
        return PREFIX + Util.joinAsCamelCase(tokens);
    }

}
