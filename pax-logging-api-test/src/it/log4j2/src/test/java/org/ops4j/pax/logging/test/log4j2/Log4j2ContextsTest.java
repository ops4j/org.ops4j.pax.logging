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
package org.ops4j.pax.logging.test.log4j2;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * <p>This unit test shows different log4j2 API usages. It's much easier than with Log4J1 because API separation
 * in Log4J2 is better.</p>
 */
public class Log4j2ContextsTest {

    @BeforeClass
    public static void config() {
        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
        builder.setConfigurationName("programmatic");
        builder.setStatusLevel(Level.ERROR);

        AppenderComponentBuilder appenderBuilder = builder.newAppender("Stdout", "CONSOLE")
                .addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT);
        appenderBuilder.add(builder.newLayout("PatternLayout")
                .addAttribute("pattern", "%d {%t} %c (%X) %-5level: %msg%n%throwable"));
        builder.add(appenderBuilder);

        builder.add(builder.newRootLogger(Level.DEBUG)
                .add(builder.newAppenderRef("Stdout")));

        Configurator.initialize(builder.build(true));
    }

    @Test
    public void logManager() {
        LogManager.getLogger("l1").info("org.apache.logging.log4j.Logger.info(java.lang.String)");
    }

    @Test
    public void contexts() {
        // The LoggerContext acts as the anchor point for the Logging system.
        // However, it is possible to have multiple active LoggerContexts in an application depending on the circumstances.
        //
        // the fundamental interface here is org.apache.logging.log4j.core.selector.ContextSelector

        org.apache.logging.log4j.spi.LoggerContext ctx1 = new LoggerContext("custom1");
        ctx1.getLogger("l1").info("org.apache.logging.log4j.Logger.info(java.lang.String)");
    }

    @Test
    public void configuration() {
        // Every LoggerContext has an active Configuration. The Configuration contains all the
        // Appenders, context-wide Filters, LoggerConfigs and contains the reference to the StrSubstitutor.

        // LoggerConfig objects are created when Loggers are declared in the logging configuration. The LoggerConfig
        // contains a set of Filters that must allow the LogEvent to pass before it will be passed to any Appenders.
        // It contains references to the set of Appenders that should be used to process the event.

        LoggerContext context = new LoggerContext("default");
        context.start(new DefaultConfiguration());
        context.getLogger("some-logger").info("Hello!");
    }

}
