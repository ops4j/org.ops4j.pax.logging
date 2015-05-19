/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.log4j.component.spi;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.component.ULogger;
import org.apache.log4j.component.helpers.MessageFormatter;

/**
 * An implementation of ULogger on org.apache.log4j.Logger.
 */
public final class Log4JULogger implements ULogger {

    /**
     * Wrapped log4j logger.
     */
    private final Logger logger;

    /**
     * Create a new instance.
     *
     * @param l logger, may not be null.
     */
    public Log4JULogger(final Logger l) {
        super();
        if (l == null) {
            throw new NullPointerException("l");
        }
        logger = l;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    /**
     * {@inheritDoc}
     */
    public void debug(final Object msg) {
        logger.debug(msg);
    }

    /**
     * {@inheritDoc}
     */
    public void debug(final Object parameterizedMsg,
                      final Object param1) {
        if (logger.isDebugEnabled()) {
            logger.debug(MessageFormatter.format(
                    parameterizedMsg.toString(), param1));
        }
    }

    /**
     * {@inheritDoc}
     */
    public void debug(final String parameterizedMsg,
                      final Object param1,
                      final Object param2) {
        if (logger.isDebugEnabled()) {
            logger.debug(MessageFormatter.format(
                    parameterizedMsg.toString(), param1, param2));
        }
    }

    /**
     * {@inheritDoc}
     */
    public void debug(final Object msg,
                      final Throwable t) {
        logger.debug(msg, t);
    }


    /**
     * {@inheritDoc}
     */
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    /**
     * {@inheritDoc}
     */
    public void info(final Object msg) {
        logger.info(msg);
    }


    /**
     * {@inheritDoc}
     */
    public void info(final Object parameterizedMsg,
                     final Object param1) {
        if (logger.isInfoEnabled()) {
            logger.info(MessageFormatter.format(
                    parameterizedMsg.toString(), param1));
        }
    }

    /**
     * {@inheritDoc}
     */
    public void info(final String parameterizedMsg,
                     final Object param1,
                     final Object param2) {
        if (logger.isInfoEnabled()) {
            logger.info(MessageFormatter.format(
                    parameterizedMsg.toString(),
                    param1,
                    param2));
        }
    }

    /**
     * {@inheritDoc}
     */
    public void info(final Object msg, final Throwable t) {
        logger.info(msg, t);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isWarnEnabled() {
        return logger.isEnabledFor(Level.WARN);
    }

    /**
     * {@inheritDoc}
     */
    public void warn(final Object msg) {
        logger.warn(msg);
    }

    /**
     * {@inheritDoc}
     */
    public void warn(final Object parameterizedMsg,
                     final Object param1) {
        if (logger.isEnabledFor(Level.WARN)) {
            logger.warn(MessageFormatter.format(
                    parameterizedMsg.toString(), param1));
        }
    }

    /**
     * {@inheritDoc}
     */
    public void warn(final String parameterizedMsg,
                     final Object param1,
                     final Object param2) {
        if (logger.isEnabledFor(Level.WARN)) {
            logger.warn(MessageFormatter.format(
                    parameterizedMsg.toString(), param1, param2));
        }
    }

    /**
     * {@inheritDoc}
     */
    public void warn(final Object msg, final Throwable t) {
        logger.warn(msg, t);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isErrorEnabled() {
        return logger.isEnabledFor(Level.ERROR);
    }

    /**
     * {@inheritDoc}
     */
    public void error(final Object msg) {
        logger.error(msg);
    }


    /**
     * {@inheritDoc}
     */
    public void error(final Object parameterizedMsg, final Object param1) {
        if (logger.isEnabledFor(Level.ERROR)) {
            logger.error(MessageFormatter.format(
                    parameterizedMsg.toString(), param1));
        }
    }

    /**
     * {@inheritDoc}
     */
    public void error(final String parameterizedMsg,
                      final Object param1,
                      final Object param2) {
        if (logger.isEnabledFor(Level.ERROR)) {
            logger.error(MessageFormatter.format(
                    parameterizedMsg.toString(), param1, param2));
        }
    }

    /**
     * {@inheritDoc}
     */
    public void error(final Object msg, final Throwable t) {
        logger.error(msg, t);
    }

}
