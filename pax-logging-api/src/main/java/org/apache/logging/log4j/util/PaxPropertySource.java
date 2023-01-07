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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * High priority {@link PropertySource} to ensure that user won't turn on (or off) anything that could break
 * pax-logging-log4j2.
 * See <a href="https://logging.apache.org/log4j/2.x/manual/configuration.html#SystemProperties">SystemProperties</a>
 */
public class PaxPropertySource implements PropertySource {

    private static final String PREFIX = "log4j2.";

    public static boolean debug;
    public static String defaultLevel;

    // PAXLOGGING-308: external file may be passed here (can be comma-separated). It'll be picked
    // up by org.apache.logging.log4j.core.config.ConfigurationFactory.Factory.getConfiguration()
    // using org.apache.logging.log4j.core.config.ConfigurationFactory.CONFIGURATION_FILE_PROPERTY
    public static String fileConfiguration = null;

    private static final Map<String, String> properties = new HashMap<>();

    public PaxPropertySource() {
        properties.put(Constants.LOG4J2_DEBUG, Boolean.toString(debug));
        properties.put("log4j2.enable.threadlocals", Boolean.FALSE.toString());
        properties.put("log4j2.is.webapp", Boolean.FALSE.toString());
        properties.put("log4j2.shutdownHookEnabled", Boolean.FALSE.toString());
        properties.put("log4j2.level", defaultLevel);
        properties.put("log4j2.disableJmx", Boolean.TRUE.toString());
        properties.put("log4j2.skipJansi", Boolean.TRUE.toString());
        if (fileConfiguration != null) {
            properties.put("log4j.configurationFile", fileConfiguration);
        }
        // log4j2.isThreadContextMapInheritable - https://github.com/ops4j/org.ops4j.pax.logging/pull/38
    }

    public static void updateFileConfiguration(String fileName) {
        fileConfiguration = fileName;
        properties.put("log4j.configurationFile", fileName);
    }

    @Override
    public int getPriority() {
        // higher than org.apache.logging.log4j.util.SystemPropertiesPropertySource.DEFAULT_PRIORITY
        // https://github.com/ops4j/org.ops4j.pax.logging/issues/484 when it's the same as in
        // org.apache.logging.log4j.util.PropertyFilePropertySource, only one is used (same key
        // in org.apache.logging.log4j.util.PropertiesUtil.Environment.sources map!)
        return 199;
    }

    @Override
    public Collection<String> getPropertyNames() {
        return properties.keySet();
    }

    @Override
    public String getProperty(String key) {
        return properties.get(key);
    }

    @Override
    public void forEach(BiConsumer<String, String> action) {
        properties.forEach(action::accept);
    }

    @Override
    public boolean containsProperty(String key) {
        return properties.containsKey(key);
    }

    @Override
    public CharSequence getNormalForm(Iterable<? extends CharSequence> tokens) {
        return PREFIX + Util.joinAsCamelCase(tokens);
    }

}
