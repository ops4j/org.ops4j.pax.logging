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

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.message.DefaultFlowMessageFactory;
import org.apache.logging.log4j.message.EntryMessage;
import org.apache.logging.log4j.message.FormattedMessage;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MessageFormatMessage;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.message.StringFormattedMessage;
import org.apache.logging.log4j.message.StringMapMessage;
import org.apache.logging.log4j.message.StructuredDataMessage;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * <p>This unit test shows different log4j2 API usages. It's much easier than with Log4J1 because API separation
 * in Log4J2 is better.</p>
 */
public class Log4j2NativeApiTest {

    private static LoggerContext context;

    @BeforeClass
    public static void config() {
        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
        builder.setConfigurationName("programmatic");
        builder.setStatusLevel(Level.ERROR);

        AppenderComponentBuilder appenderBuilder = builder.newAppender("Stdout", "CONSOLE")
                .addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT);
        appenderBuilder.add(builder.newLayout("PatternLayout")
                .addAttribute("pattern", "%d {%t} %c (%X) %level (%marker | %markerSimpleName): %msg%n%throwable"));
        builder.add(appenderBuilder);

        builder.add(builder.newRootLogger(Level.DEBUG)
                .add(builder.newAppenderRef("Stdout")));

        context = Configurator.initialize(builder.build(true));
    }

    @Test
    public void simplestUsage() {
        // Loggers are created by calling LogManager.getLogger. The Logger itself performs no direct actions.
        // It simply has a name and is associated with a LoggerConfig

        Logger log = LogManager.getLogger(Log4j2NativeApiTest.class);

        log.info("simplestUsage - INFO1");
        log.trace("simplestUsage - TRACE");

        Logger root = LogManager.getRootLogger();
        root.info("simplestUsage - INFO2");

        // Since naming Loggers after their owning class is such a common idiom, the convenience method
        // LogManager.getLogger() is provided to automatically use the calling class's fully qualified class name
        // as the Logger name.
        Logger log2 = LogManager.getLogger();
        log2.info("simplestUsage - INFO3");
    }

    @Test
    public void mdc() {
        Logger log = LogManager.getLogger(Log4j2NativeApiTest.class);

        ThreadContext.put("country", "Equestria");
        log.info("mdc - INFO");
        ThreadContext.clearAll();
    }

    @Test
    public void markers() {
        // Loggers are created by calling LogManager.getLogger. The Logger itself performs no direct actions.
        // It simply has a name and is associated with a LoggerConfig

        Logger log = LogManager.getLogger(Log4j2NativeApiTest.class);

        Marker m1 = MarkerManager.getMarker("m1");
        m1.addParents(MarkerManager.getMarker("p1"), MarkerManager.getMarker("p2"));
        log.info(m1, "markers - INFO");
    }

    @Test
    public void messages() {
        // backup appenders
        org.apache.logging.log4j.core.Logger root = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();
        Map<String, Appender> appenders = root.getAppenders();
        appenders.values().forEach(root::removeAppender);

        Logger log = LogManager.getLogger(Log4j2NativeApiTest.class);

        log.info((Message) new SimpleMessage("Hello1"));

        EntryMessage hello2 = new DefaultFlowMessageFactory().newEntryMessage(new SimpleMessage("Hello2"));
        log.info(hello2);

        // org.apache.logging.log4j.message.FormattedMessage dynamically choses between:
        //  - java.text.MessageFormat {N,format} style
        //  - java.util.Formatter % style
        //  - slf4j-like {} style
        assertThat(MessageFormat.format("-{0,number}- {0,number,0000}", 3), equalTo("-3- 0003"));

        layout("FORMAT> %msg%n");
        Message hello3 = new FormattedMessage("{0,number,00}", 1);
        log.info(hello3);
        Message hello4 = new FormattedMessage("%03d", 1);
        log.info(hello4);
        Message hello5 = new FormattedMessage("{}, {}", 1, "hello5");
        log.info(hello5);

        Message hello6 = new MessageFormatMessage("-{0,number}- {0,number,0000}", 42);
        log.info(hello6);

        Message hello7 = new StringFormattedMessage("%04d", 42);
        log.info(hello7);

        Message hello8 = new ParameterizedMessage("{}, {}", 42, 43);
        log.info(hello8);

        HashMap<String, String> map9 = new HashMap<>();
        map9.put("k1", "v2");
        map9.put("k2", "v2");
        Message hello9 = new StringMapMessage(map9);

        // org.apache.logging.log4j.message.MapMessage.MapFormat.lookupIgnoreCase() for %msg parameters
        layout("MAP> %msg{XML}%n");
        log.info(hello9);
        layout("MAP> %msg{JSON}%n");
        log.info(hello9);
        ThreadContext.put("x1", "y1");
        layout("MAP> ${map:k1} ${ctx:x1} ${sys:java.home} %msg%n");
        log.info(hello9);

        // http://logging.apache.org/log4j/2.x/manual/configuration.html#PropertySubstitution
        // http://logging.apache.org/log4j/2.x/manual/lookups.html#StructuredDataLookup
        StructuredDataMessage hello10 = new StructuredDataMessage("1", "hello10", "sd-test");
        hello10.put("k1", "v1");
        hello10.put("k2", "v2");
        layout("SD> %msg%n");
        log.info(hello10);
        layout("SD> ${sd:id}/${sd:type} ${sd:k1} %msg%n");
        log.info(hello10);

        // restore appenders
        root.getAppenders().values().forEach(root::removeAppender);
        appenders.values().forEach(root::addAppender);
        ThreadContext.clearAll();
    }

    private void layout(String layout) {
        org.apache.logging.log4j.core.Logger root = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();
        Map<String, Appender> appenders = root.getAppenders();
        appenders.values().forEach(root::removeAppender);

        PatternLayout pl = PatternLayout.newBuilder()
                .withPattern(layout)
                .withConfiguration(context.getConfiguration())
                .build();
        ConsoleAppender appender = ConsoleAppender.newBuilder()
                .setName("Stdout")
                .setConfiguration(context.getConfiguration())
                .setTarget(ConsoleAppender.Target.SYSTEM_OUT)
                .setLayout(pl)
                .build();
        appender.start();

        root.addAppender(appender);
    }

    @Test
    public void levels() {
        // LoggerConfigs will be assigned a Log Level. The set of built-in levels includes TRACE, DEBUG, INFO, WARN,
        // ERROR, and FATAL. Log4j 2 also supports custom log levels. Another mechanism for getting more granularity
        // is to use Markers instead.
    }

    @Test
    public void filters() {
        // In addition to the automatic log Level filtering that takes place as described in the previous section,
        // Log4j provides Filters that can be applied before control is passed to any LoggerConfig, after control
        // is passed to a LoggerConfig but before calling any Appenders, after control is passed to a LoggerConfig
        // but before calling a specific Appender, and on each Appender.
    }

    @Test
    public void appenders() {
        // The ability to selectively enable or disable logging requests based on their logger is only part of the
        // picture. Log4j allows logging requests to print to multiple destinations. In log4j speak, an output
        // destination is called an Appender. Currently, appenders exist for the console, files, remote socket servers,
        // Apache Flume, JMS, remote UNIX Syslog daemons, and various database APIs.
    }

}
