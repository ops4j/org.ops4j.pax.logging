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
package org.ops4j.pax.logging.test.logback;

import ch.qos.logback.classic.BasicConfigurator;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.filter.AbstractMatcherFilter;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.util.StatusPrinter;
import org.junit.Test;

/**
 * This unit test shows different Logback API usages. Logback should generally be used via SLF4J, but we
 * need some canonical examples.
 */
public class LogbackNativeApiTest {

    @Test
    public void status() {
        // only logback-core - nothing to see here
        ContextBase context = new ContextBase();
        context.putObject("object1", "Hello!");
        context.start();
        context.getStatusManager().add(new InfoStatus("Just checking", "context object"));
        StatusPrinter.print(context);
    }

    @Test
    public void statusWithLogbackClassic() {
        LoggerContext context = new LoggerContext();
        context.start();

        Logger logger = context.getLogger("my.logger");
        logger.info("Hello");
        logger.info("Hello 2");

        Logger logger2 = context.getLogger("my.logger2");
        logger2.info("Hello");
        logger2.info("Hello 2");

        StatusPrinter.print(context);
    }

    @Test
    public void filters() {
        LoggerContext context = new LoggerContext();
        BasicConfigurator bc = new BasicConfigurator();
        bc.setContext(context);
        bc.configure(context);
        context.start();

        Logger logger = context.getLogger("my.logger");
        logger.info("Hello");
        logger.info("Hello 2");

        Logger logger2 = context.getLogger("my.logger2");
        logger2.info("Hello");
        logger2.info("Hello 2");

        Logger l = context.getLogger(Logger.ROOT_LOGGER_NAME);
        Appender<ILoggingEvent> console = l.getAppender("console");
        console.addFilter(new AbstractMatcherFilter<ILoggingEvent>() {
            @Override
            public FilterReply decide(ILoggingEvent event) {
                System.out.println("{" + event.getMessage() + "}");
                return FilterReply.NEUTRAL;
            }
        });
        l.info("Hello with filter");

        StatusPrinter.print(context);
    }

}
