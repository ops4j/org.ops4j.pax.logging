/*
 * Copyright 2006 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.logging.internal;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingManager;

/**
 * <p>JUL {@link Handler} that bridges {@link LogRecord log records} to Pax Logging loggers.</p>
 * <p>Even if {@link SimpleFormatter} is used, we only call its
 * {@link java.util.logging.Formatter#formatMessage(LogRecord)} which only uses
 * {@link java.text.MessageFormat#format(String, Object...)} method on log record's message.
 * It doesn't do anything with remaining fields of {@link LogRecord}.</p>
 */
public class JdkHandler extends Handler {

    private static final String FQCN = java.util.logging.Logger.class.getName();

    private PaxLoggingManager m_loggingManager;

    public JdkHandler(PaxLoggingManager loggingManager) {
        m_loggingManager = loggingManager;
        setFormatter(new SimpleFormatter());
    }

    @Override
    public void close() throws SecurityException {
    }

    @Override
    public void flush() {
    }

    /**
     * Using information from {@link LogRecord} logging method on related {@link PaxLogger} is called.
     * @param record
     */
    @Override
    public void publish(LogRecord record) {
        Level level = record.getLevel();
        String loggerName = record.getLoggerName();
        PaxLogger logger = m_loggingManager.getLogger(loggerName, FQCN);
        String message;
        try {
            // LogRecord may have parameters associated, so let's format the message
            // using JUL formatter
            message = getFormatter().formatMessage(record);
        } catch (Exception ex) {
            message = record.getMessage();
        }

        Throwable throwable = record.getThrown();
        int levelInt = level.intValue();
        if (levelInt <= Level.FINER.intValue()) {
            logger.trace(message, throwable);
        } else if (levelInt <= Level.FINE.intValue()) {
            logger.debug(message, throwable);
        } else if (levelInt <= Level.INFO.intValue()) {
            logger.inform(message, throwable);
        } else if (levelInt <= Level.WARNING.intValue()) {
            logger.warn(message, throwable);
        } else {
            logger.error(message, throwable);
        }
    }

}
