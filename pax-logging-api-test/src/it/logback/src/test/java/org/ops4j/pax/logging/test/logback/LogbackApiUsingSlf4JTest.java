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

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LogbackServiceProvider;
import ch.qos.logback.core.util.StatusPrinter;
import org.junit.Test;
import org.slf4j.LoggerFactory;

/**
 * This unit test shows different Logback API usages. Logback should generally be used via SLF4J, but we
 * need some canonical examples.
 */
public class LogbackApiUsingSlf4JTest {

    @Test
    public void statusWithLogbackClassicAndLowLevelSlf4JUsage() {
        // low level method
        LogbackServiceProvider provider = new LogbackServiceProvider();
        provider.initialize();
        ch.qos.logback.classic.LoggerContext context = (LoggerContext) provider.getLoggerFactory();

        Logger logger = context.getLogger("my.logger");
        logger.info("Hello");
        logger.info("Hello 2");

        Logger logger2 = context.getLogger("my.logger2");
        logger2.info("Hello");
        logger2.info("Hello 2");

        StatusPrinter.print(context);
    }

    @Test
    public void statusWithLogbackClassic() {
        // high level method
        ch.qos.logback.classic.LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
//        context.reset();

        Logger logger = context.getLogger("my.logger");
        logger.info("Hello");
        logger.info("Hello 2");

        Logger logger2 = context.getLogger("my.logger2");
        logger2.info("Hello");
        logger2.info("Hello 2");

        StatusPrinter.print(context);

        context.stop();
    }

}
