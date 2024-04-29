/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
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
package org.apache.logging.log4j.status;

import static java.util.Objects.requireNonNull;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.message.Message;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.spi.support.DefaultServiceLog;
import org.ops4j.pax.logging.spi.support.FallbackLogFactory;
import org.osgi.framework.FrameworkUtil;

/**
 * A {@link StatusListener} that writes to the console.
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class StatusConsoleListener implements StatusListener {

    private final Lock lock = new ReentrantLock();

    private final Level initialLevel;

    private final PrintStream initialStream;

    // `volatile` is necessary to correctly read the `level` without holding the lock
    private volatile Level level;

    // `volatile` is necessary to correctly read the `stream` without holding the lock
    private volatile PrintStream stream;

    private static PaxLogger logger = FallbackLogFactory.createFallbackLog(FrameworkUtil.getBundle(StatusLogger.class), "log4j2");

    /**
     * Constructs a {@link StatusConsoleListener} instance writing to {@link System#out} using the supplied level.
     *
     * @param level the level of status messages that should appear on the console
     * @throws NullPointerException on null {@code level}
     */
    public StatusConsoleListener(final Level level) {
        this(level, System.out);
    }

    /**
     * Constructs a {@link StatusConsoleListener} instance using the supplied level and stream.
     * <p>
     * Make sure not to use a logger stream of some sort to avoid creating an infinite loop of indirection!
     * </p>
     *
     * @param level the level of status messages that should appear on the console
     * @param stream the stream to write to
     * @throws NullPointerException on null {@code level} or {@code stream}
     */
    public StatusConsoleListener(final Level level, final PrintStream stream) {
        this.initialLevel = this.level = requireNonNull(level, "level");
        this.initialStream = this.stream = requireNonNull(stream, "stream");
    }

    /**
     * Sets the level to a new value.
     *
     * @param level the new level
     * @throws NullPointerException on null {@code level}
     */
    public void setLevel(final Level level) {
        // Pax Logging: Configured directly through org.ops4j.pax.logging.spi.support.DefaultServiceLog.setLogLevel()
    }

    /**
     * Sets the output stream to a new value.
     *
     * @param stream the new output stream
     * @throws NullPointerException on null {@code stream}
     * @since 2.23.0
     */
    public void setStream(final PrintStream stream) {
        requireNonNull(stream, "stream");
        // Check if a mutation (and locking!) is necessary at all
        if (this.stream != stream) {
            @Nullable OutputStream oldStream = null;
            lock.lock();
            try {
                if (this.stream != stream) {
                    oldStream = this.stream;
                    this.stream = stream;
                }
            } finally {
                lock.unlock();
            }
            if (oldStream != null) {
                closeNonSystemStream(oldStream);
            }
        }
    }

    /**
     * Returns the level for which the listener should receive events.
     *
     * @return the log level
     */
    @Override
    public Level getStatusLevel() {
        switch (logger.getLogLevel()) {
            case DefaultServiceLog.TRACE:
                return Level.TRACE;
            case DefaultServiceLog.INFO:
                return Level.INFO;
            case DefaultServiceLog.WARN:
                return Level.WARN;
            case DefaultServiceLog.ERROR:
                return Level.ERROR;
            case DefaultServiceLog.FATAL:
                return Level.FATAL;
            case DefaultServiceLog.NONE:
                return Level.OFF;
            case DefaultServiceLog.DEBUG:
            default:
                return Level.DEBUG;
        }
    }

    /**
     * Writes status messages to the console.
     *
     * @param data a status data
     * @throws NullPointerException on null {@code data}
     */
    @Override
    public void log(final StatusData data) {
        requireNonNull(data, "data");
        StackTraceElement ste = data.getStackTraceElement();
        logMessage0(ste == null ? null : ste.getClassName(), data.getLevel(), data.getMarker(),
                data.getMessage(), data.getThrowable());
        final String formattedStatus = data.getFormattedStatus();
        stream.println(formattedStatus);
    }

    private void logMessage0(String fqcn, Level level, Marker marker, Message msg, Throwable t) {
        if (level == Level.ALL) {
            // level treated as threshold...
            logger.trace(msg.getFormattedMessage(), t, fqcn);
        } else if (level == Level.FATAL) {
            logger.fatal(msg.getFormattedMessage(), t, fqcn);
        } else if (level == Level.ERROR) {
            logger.error(msg.getFormattedMessage(), t, fqcn);
        } else if (level == Level.WARN) {
            logger.warn(msg.getFormattedMessage(), t, fqcn);
        } else if (level == Level.INFO) {
            logger.inform(msg.getFormattedMessage(), t, fqcn);
        } else if (level == Level.DEBUG) {
            logger.debug(msg.getFormattedMessage(), t, fqcn);
        } else if (level == Level.TRACE) {
            logger.trace(msg.getFormattedMessage(), t, fqcn);
        }
    }

    /**
     * Adds package name filters to exclude.
     *
     * @param filters An array of package names to exclude.
     * @deprecated This method is ineffective and only kept for binary backward compatibility.
     */
    @Deprecated
    public void setFilters(final String... filters) {}

    /**
     * Resets the level and output stream to its initial values, and closes the output stream, if it is a non-system one.
     */
    @Override
    public void close() {
        final OutputStream oldStream;
        lock.lock();
        try {
            oldStream = stream;
            stream = initialStream;
            level = initialLevel;
        } finally {
            lock.unlock();
        }
        closeNonSystemStream(oldStream);
    }

    private static void closeNonSystemStream(final OutputStream stream) {
        // Close only non-system streams
        if (stream != System.out && stream != System.err) {
            try {
                stream.close();
            } catch (IOException error) {
                // We are at the lowest level of the system.
                // Hence, there is nothing better we can do but dumping the failure.
                error.printStackTrace(System.err);
            }
        }
    }
}
