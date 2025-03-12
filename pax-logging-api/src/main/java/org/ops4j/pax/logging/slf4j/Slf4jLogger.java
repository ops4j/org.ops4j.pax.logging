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
package org.ops4j.pax.logging.slf4j;

import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingManager;
import org.ops4j.pax.logging.PaxLoggingManagerAwareLogger;
import org.ops4j.pax.logging.PaxMarker;
import org.ops4j.pax.logging.spi.support.FallbackLogFactory;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;
import org.slf4j.event.LoggingEvent;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.spi.DefaultLoggingEventBuilder;
import org.slf4j.spi.LocationAwareLogger;
import org.slf4j.spi.LoggingEventAware;

/**
 * pax-logging specific {@link org.slf4j.Logger} that delegates to {@link PaxLogger} that is obtained from
 * framework specific {@link org.ops4j.pax.logging.PaxLoggingService} and eventually delegates to logging
 * implementation.
 */
public class Slf4jLogger implements LocationAwareLogger, PaxLoggingManagerAwareLogger, LoggingEventAware {

    public static final String SLF4J_MARKER_MDC_ATTRIBUTE = "slf4j.marker";
    public static final String SLF4J_FQCN = Slf4jLogger.class.getName().intern();
    public static final String SLF4J_BUILDER_FQCN = DefaultLoggingEventBuilder.class.getName().intern();

    private String m_name;
    private PaxLogger m_delegate;

    public static ThreadLocal<String> fcqn = new ThreadLocal<>();

    public Slf4jLogger(String name, PaxLogger delegate) {
        m_name = name;
        m_delegate = delegate;
    }

    @Override
    public void setPaxLoggingManager(PaxLoggingManager loggingManager) {
        if (loggingManager == null) {
            m_delegate = FallbackLogFactory.createFallbackLog(FrameworkUtil.getBundle(Logger.class), m_name);
        } else {
            m_delegate = loggingManager.getLogger(m_name, SLF4J_FQCN);
        }
    }

    // implementation of org.slf4j.spi.LoggingEventAware

    @Override
    public void log(LoggingEvent event) {
        // see org.slf4j.spi.DefaultLoggingEventBuilder.logViaPublicSLF4JLoggerAPI()
        Object[] argArray = event.getArgumentArray();
        int argLen = argArray == null ? 0 : argArray.length;

        Throwable t = event.getThrowable();
        int tLen = t == null ? 0 : 1;

        String msg = event.getMessage();

        Object[] combinedArguments = new Object[argLen + tLen];

        if (argArray != null) {
            System.arraycopy(argArray, 0, combinedArguments, 0, argLen);
        }
        if (t != null) {
            combinedArguments[argLen] = t;
        }

        msg = mergeMarkersAndKeyValuePairs(event, msg);

        try {
            fcqn.set(SLF4J_BUILDER_FQCN);
            switch (event.getLevel()) {
                case TRACE:
                    m_delegate.trace(msg, combinedArguments);
                    break;
                case DEBUG:
                    m_delegate.debug(msg, combinedArguments);
                    break;
                case INFO:
                    m_delegate.info(msg, combinedArguments);
                    break;
                case WARN:
                    m_delegate.warn(msg, combinedArguments);
                    break;
                case ERROR:
                    m_delegate.error(msg, combinedArguments);
                    break;
            }
        } finally {
            fcqn.remove();
        }
    }

    // copy from SLF4J
    private String mergeMarkersAndKeyValuePairs(LoggingEvent aLoggingEvent, String msg) {
        StringBuilder sb = null;

        if (aLoggingEvent.getMarkers() != null) {
            sb = new StringBuilder();
            for (Marker marker : aLoggingEvent.getMarkers()) {
                sb.append(marker);
                sb.append(' ');
            }
        }

        if (aLoggingEvent.getKeyValuePairs() != null) {
            if (sb == null) {
                sb = new StringBuilder();
            }
            for (KeyValuePair kvp : aLoggingEvent.getKeyValuePairs()) {
                sb.append(kvp.key);
                sb.append('=');
                sb.append(kvp.value);
                sb.append(' ');
            }
        }

        if (sb != null) {
            sb.append(msg);
            return sb.toString();
        } else {
            return msg;
        }
    }

    // implementation of org.slf4j.spi.LocationAwareLogger follows.
    // no need to call isXXXEnable, as the delegated logger (PaxLogger) does it anyway

    /**
     * Return the name of this <code>Logger</code> instance.
     */
    @Override
    public String getName() {
        return m_name;
    }

    /**
     * Is the logger instance enabled for the TRACE level?
     *
     * @return True if this Logger is enabled for the DEBUG level,
     *         false otherwise.
     */
    @Override
    public boolean isTraceEnabled() {
        return m_delegate.isTraceEnabled();
    }

    /**
     * Log a message at the DEBUG level.
     *
     * @param msg the message string to be logged
     */
    @Override
    public void trace(String msg) {
        m_delegate.trace(msg);
    }

    /**
     * Log a message at the TRACE level according to the specified format
     * and argument.
     *
     * This form avoids superfluous object creation when the logger
     * is disabled for the TRACE level.
     *
     * @param format the format string
     * @param arg    the argument
     */
    @Override
    public void trace(String format, Object arg) {
        if (m_delegate.isTraceEnabled()) {
            FormattingTuple tuple = MessageFormatter.format(format, arg);
            Throwable t = tuple.getThrowable();
            if (t == null) {
                m_delegate.trace(tuple.getMessage());
            } else {
                m_delegate.trace(tuple.getMessage(), t);
            }
        }
    }

    /**
     * Log a message at the TRACE level according to the specified format
     * and arguments.
     *
     * This form avoids superfluous object creation when the logger
     * is disabled for the TRACE level.
     *
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     */
    @Override
    public void trace(String format, Object arg1, Object arg2) {
        if (m_delegate.isTraceEnabled()) {
            FormattingTuple tuple = MessageFormatter.format(format, arg1, arg2);
            Throwable t = tuple.getThrowable();
            if (t == null) {
                m_delegate.trace(tuple.getMessage());
            } else {
                m_delegate.trace(tuple.getMessage(), t);
            }
        }
    }

    /**
     * Log a message at the TRACE level according to the specified format
     * and arguments.
     *
     * This form avoids superfluous object creation when the logger
     * is disabled for the TRACE level.
     *
     * @param format   the format string
     * @param argArray an array of arguments
     */
    @Override
    public void trace(String format, Object[] argArray) {
        if (m_delegate.isTraceEnabled()) {
            FormattingTuple tuple = MessageFormatter.arrayFormat(format, argArray);
            Throwable t = tuple.getThrowable();
            if (t == null) {
                m_delegate.trace(tuple.getMessage());
            } else {
                m_delegate.trace(tuple.getMessage(), t);
            }
        }
    }

    /**
     * Log an exception (throwable) at the TRACE level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     */
    @Override
    public void trace(String msg, Throwable t) {
        m_delegate.trace(msg, t);
    }

    /**
     * Similar to {@link #isTraceEnabled()} method except that the
     * marker data is also taken into account.
     *
     * @param marker The marker data to take into consideration
     */
    @Override
    public boolean isTraceEnabled(Marker marker) {
        return m_delegate.isTraceEnabled(new PaxMarker(marker));
    }

    /**
     * Log a message with the specific Marker at the TRACE level.
     *
     * @param marker the marker data specific to this log statement
     * @param msg    the message string to be logged
     */
    @Override
    public void trace(Marker marker, String msg) {
        if (marker != null) {
            PaxMarker m = new PaxMarker(marker);
            if (m_delegate.isTraceEnabled(m)) {
                m_delegate.trace(m, msg);
            }
        } else {
            if (m_delegate.isTraceEnabled()) {
                m_delegate.trace(msg);
            }
        }
    }

    /**
     * This method is similar to {@link #trace(String, Object)} method except that the
     * marker data is also taken into consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg    the argument
     */
    @Override
    public void trace(Marker marker, String format, Object arg) {
        if (marker != null) {
            PaxMarker m = new PaxMarker(marker);
            if (m_delegate.isTraceEnabled(m)) {
                FormattingTuple tuple = MessageFormatter.format(format, arg);
                Throwable t = tuple.getThrowable();
                if (t == null) {
                    m_delegate.trace(m, tuple.getMessage());
                } else {
                    m_delegate.trace(m, tuple.getMessage(), t);
                }
            }
        } else {
            if (m_delegate.isTraceEnabled()) {
                FormattingTuple tuple = MessageFormatter.format(format, arg);
                Throwable t = tuple.getThrowable();
                if (t == null) {
                    m_delegate.trace(tuple.getMessage());
                } else {
                    m_delegate.trace(tuple.getMessage(), t);
                }
            }
        }
    }

    /**
     * This method is similar to {@link #trace(String, Object, Object)}
     * method except that the marker data is also taken into
     * consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     */
    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        if (marker != null) {
            PaxMarker m = new PaxMarker(marker);
            if (m_delegate.isTraceEnabled(m)) {
                FormattingTuple tuple = MessageFormatter.format(format, arg1, arg2);
                Throwable t = tuple.getThrowable();
                if (t == null) {
                    m_delegate.trace(m, tuple.getMessage());
                } else {
                    m_delegate.trace(m, tuple.getMessage(), t);
                }
            }
        } else {
            if (m_delegate.isTraceEnabled()) {
                FormattingTuple tuple = MessageFormatter.format(format, arg1, arg2);
                Throwable t = tuple.getThrowable();
                if (t == null) {
                    m_delegate.trace(tuple.getMessage());
                } else {
                    m_delegate.trace(tuple.getMessage(), t);
                }
            }
        }
    }

    /**
     * This method is similar to {@link #trace(String, Object[])}
     * method except that the marker data is also taken into
     * consideration.
     *
     * @param marker   the marker data specific to this log statement
     * @param format   the format string
     * @param argArray an array of arguments
     */
    @Override
    public void trace(Marker marker, String format, Object[] argArray) {
        if (marker != null) {
            PaxMarker m = new PaxMarker(marker);
            if (m_delegate.isTraceEnabled(m)) {
                FormattingTuple tuple = MessageFormatter.arrayFormat(format, argArray);
                Throwable t = tuple.getThrowable();
                if (t == null) {
                    m_delegate.trace(m, tuple.getMessage());
                } else {
                    m_delegate.trace(m, tuple.getMessage(), t);
                }
            }
        } else {
            if (m_delegate.isTraceEnabled()) {
                FormattingTuple tuple = MessageFormatter.arrayFormat(format, argArray);
                Throwable t = tuple.getThrowable();
                if (t == null) {
                    m_delegate.trace(tuple.getMessage());
                } else {
                    m_delegate.trace(tuple.getMessage(), t);
                }
            }
        }
    }

    /**
     * This method is similar to {@link #trace(String, Throwable)} method except that the
     * marker data is also taken into consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param msg    the message accompanying the exception
     * @param t      the exception (throwable) to log
     */
    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        if (marker != null) {
            PaxMarker m = new PaxMarker(marker);
            if (m_delegate.isTraceEnabled(m)) {
                m_delegate.trace(m, msg, t);
            }
        } else {
            if (m_delegate.isTraceEnabled()) {
                m_delegate.trace(msg, t);
            }
        }
    }

    /**
     * Is the logger instance enabled for the DEBUG level?
     *
     * @return True if this Logger is enabled for the DEBUG level,
     *         false otherwise.
     */
    @Override
    public boolean isDebugEnabled() {
        return m_delegate.isDebugEnabled();
    }

    /**
     * Log a message at the DEBUG level.
     *
     * @param msg the message string to be logged
     */
    @Override
    public void debug(String msg) {
        m_delegate.debug(msg);
    }

    /**
     * Log a message at the DEBUG level according to the specified format
     * and argument.
     *
     * This form avoids superfluous object creation when the logger
     * is disabled for the DEBUG level.
     *
     * @param format the format string
     * @param arg    the argument
     */
    @Override
    public void debug(String format, Object arg) {
        if (m_delegate.isDebugEnabled()) {
            FormattingTuple tuple = MessageFormatter.format(format, arg);
            Throwable t = tuple.getThrowable();
            if (t == null) {
                m_delegate.debug(tuple.getMessage());
            } else {
                m_delegate.debug(tuple.getMessage(), t);
            }
        }
    }

    /**
     * Log a message at the DEBUG level according to the specified format
     * and arguments.
     *
     * This form avoids superfluous object creation when the logger
     * is disabled for the DEBUG level.
     *
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     */
    @Override
    public void debug(String format, Object arg1, Object arg2) {
        if (m_delegate.isDebugEnabled()) {
            FormattingTuple tuple = MessageFormatter.format(format, arg1, arg2);
            Throwable t = tuple.getThrowable();
            if (t == null) {
                m_delegate.debug(tuple.getMessage());
            } else {
                m_delegate.debug(tuple.getMessage(), t);
            }
        }
    }

    /**
     * Log a message at the DEBUG level according to the specified format
     * and arguments.
     *
     * This form avoids superfluous object creation when the logger
     * is disabled for the DEBUG level.
     *
     * @param format   the format string
     * @param argArray an array of arguments
     */
    @Override
    public void debug(String format, Object[] argArray) {
        if (m_delegate.isDebugEnabled()) {
            FormattingTuple tuple = MessageFormatter.arrayFormat(format, argArray);
            Throwable t = tuple.getThrowable();
            if (t == null) {
                m_delegate.debug(tuple.getMessage());
            } else {
                m_delegate.debug(tuple.getMessage(), t);
            }
        }
    }

    /**
     * Log an exception (throwable) at the DEBUG level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     */
    @Override
    public void debug(String msg, Throwable t) {
        m_delegate.debug(msg, t);
    }

    /**
     * Similar to {@link #isDebugEnabled()} method except that the
     * marker data is also taken into account.
     *
     * @param marker The marker data to take into consideration
     */
    @Override
    public boolean isDebugEnabled(Marker marker) {
        return m_delegate.isDebugEnabled(new PaxMarker(marker));
    }

    /**
     * Log a message with the specific Marker at the DEBUG level.
     *
     * @param marker the marker data specific to this log statement
     * @param msg    the message string to be logged
     */
    @Override
    public void debug(Marker marker, String msg) {
        if (marker != null) {
            PaxMarker m = new PaxMarker(marker);
            if (m_delegate.isDebugEnabled(m)) {
                m_delegate.debug(m, msg);
            }
        } else {
            if (m_delegate.isDebugEnabled()) {
                m_delegate.debug(msg);
            }
        }
    }

    /**
     * This method is similar to {@link #debug(String, Object)} method except that the
     * marker data is also taken into consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg    the argument
     */
    @Override
    public void debug(Marker marker, String format, Object arg) {
        if (marker != null) {
            PaxMarker m = new PaxMarker(marker);
            if (m_delegate.isDebugEnabled(m)) {
                FormattingTuple tuple = MessageFormatter.format(format, arg);
                Throwable t = tuple.getThrowable();
                if (t == null) {
                    m_delegate.debug(m, tuple.getMessage());
                } else {
                    m_delegate.debug(m, tuple.getMessage(), t);
                }
            }
        } else {
            if (m_delegate.isDebugEnabled()) {
                FormattingTuple tuple = MessageFormatter.format(format, arg);
                Throwable t = tuple.getThrowable();
                if (t == null) {
                    m_delegate.debug(tuple.getMessage());
                } else {
                    m_delegate.debug(tuple.getMessage(), t);
                }
            }
        }
    }

    /**
     * This method is similar to {@link #debug(String, Object, Object)}
     * method except that the marker data is also taken into
     * consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     */
    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        if (marker != null) {
            PaxMarker m = new PaxMarker(marker);
            if (m_delegate.isDebugEnabled(m)) {
                FormattingTuple tuple = MessageFormatter.format(format, arg1, arg2);
                Throwable t = tuple.getThrowable();
                if (t == null) {
                    m_delegate.debug(m, tuple.getMessage());
                } else {
                    m_delegate.debug(m, tuple.getMessage(), t);
                }
            }
        } else {
            if (m_delegate.isDebugEnabled()) {
                FormattingTuple tuple = MessageFormatter.format(format, arg1, arg2);
                Throwable t = tuple.getThrowable();
                if (t == null) {
                    m_delegate.debug(tuple.getMessage());
                } else {
                    m_delegate.debug(tuple.getMessage(), t);
                }
            }
        }
    }

    /**
     * This method is similar to {@link #debug(String, Object[])}
     * method except that the marker data is also taken into
     * consideration.
     *
     * @param marker   the marker data specific to this log statement
     * @param format   the format string
     * @param argArray an array of arguments
     */
    @Override
    public void debug(Marker marker, String format, Object[] argArray) {
        if (marker != null) {
            PaxMarker m = new PaxMarker(marker);
            if (m_delegate.isDebugEnabled(m)) {
                FormattingTuple tuple = MessageFormatter.arrayFormat(format, argArray);
                Throwable t = tuple.getThrowable();
                if (t == null) {
                    m_delegate.debug(m, tuple.getMessage());
                } else {
                    m_delegate.debug(m, tuple.getMessage(), t);
                }
            }
        } else {
            if (m_delegate.isDebugEnabled()) {
                FormattingTuple tuple = MessageFormatter.arrayFormat(format, argArray);
                Throwable t = tuple.getThrowable();
                if (t == null) {
                    m_delegate.debug(tuple.getMessage());
                } else {
                    m_delegate.debug(tuple.getMessage(), t);
                }
            }
        }
    }

    /**
     * This method is similar to {@link #debug(String, Throwable)} method except that the
     * marker data is also taken into consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param msg    the message accompanying the exception
     * @param t      the exception (throwable) to log
     */
    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        if (marker != null) {
            PaxMarker m = new PaxMarker(marker);
            if (m_delegate.isDebugEnabled(m)) {
                m_delegate.debug(m, msg, t);
            }
        } else {
            if (m_delegate.isDebugEnabled()) {
                m_delegate.debug(msg, t);
            }
        }
    }

    /**
     * Is the logger instance enabled for the INFO level?
     *
     * @return True if this Logger is enabled for the INFO level,
     *         false otherwise.
     */
    @Override
    public boolean isInfoEnabled() {
        return m_delegate.isInfoEnabled();
    }

    /**
     * Log a message at the INFO level.
     *
     * @param msg the message string to be logged
     */
    @Override
    public void info(String msg) {
        m_delegate.info(msg);
    }

    /**
     * Log a message at the INFO level according to the specified format
     * and argument.
     *
     * This form avoids superfluous object creation when the logger
     * is disabled for the INFO level.
     *
     * @param format the format string
     * @param arg    the argument
     */
    @Override
    public void info(String format, Object arg) {
        if (m_delegate.isInfoEnabled()) {
            FormattingTuple tuple = MessageFormatter.format(format, arg);
            Throwable t = tuple.getThrowable();
            if (t == null) {
                m_delegate.info(tuple.getMessage());
            } else {
                m_delegate.info(tuple.getMessage(), t);
            }
        }
    }

    /**
     * Log a message at the INFO level according to the specified format
     * and arguments.
     *
     * This form avoids superfluous object creation when the logger
     * is disabled for the INFO level.
     *
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     */
    @Override
    public void info(String format, Object arg1, Object arg2) {
        if (m_delegate.isInfoEnabled()) {
            FormattingTuple tuple = MessageFormatter.format(format, arg1, arg2);
            Throwable t = tuple.getThrowable();
            if (t == null) {
                m_delegate.info(tuple.getMessage());
            } else {
                m_delegate.info(tuple.getMessage(), t);
            }
        }
    }

    /**
     * Log a message at the INFO level according to the specified format
     * and arguments.
     *
     * This form avoids superfluous object creation when the logger
     * is disabled for the INFO level.
     *
     * @param format   the format string
     * @param argArray an array of arguments
     */
    @Override
    public void info(String format, Object[] argArray) {
        if (m_delegate.isInfoEnabled()) {
            FormattingTuple tuple = MessageFormatter.arrayFormat(format, argArray);
            Throwable t = tuple.getThrowable();
            if (t == null) {
                m_delegate.info(tuple.getMessage());
            } else {
                m_delegate.info(tuple.getMessage(), t);
            }
        }
    }

    /**
     * Log an exception (throwable) at the INFO level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     */
    @Override
    public void info(String msg, Throwable t) {
        m_delegate.info(msg, t);
    }

    /**
     * Similar to {@link #isInfoEnabled()} method except that the marker
     * data is also taken into consideration.
     *
     * @param marker The marker data to take into consideration
     */
    @Override
    public boolean isInfoEnabled(Marker marker) {
        return m_delegate.isInfoEnabled(new PaxMarker(marker));
    }

    /**
     * Log a message with the specific Marker at the INFO level.
     *
     * @param marker The marker specific to this log statement
     * @param msg    the message string to be logged
     */
    @Override
    public void info(Marker marker, String msg) {
        if (marker != null) {
            PaxMarker m = new PaxMarker(marker);
            if (m_delegate.isInfoEnabled(m)) {
                m_delegate.info(m, msg);
            }
        } else {
            if (m_delegate.isInfoEnabled()) {
                m_delegate.info(msg);
            }
        }
    }

    /**
     * This method is similar to {@link #info(String, Object)} method except that the
     * marker data is also taken into consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg    the argument
     */
    @Override
    public void info(Marker marker, String format, Object arg) {
        if (marker != null) {
            PaxMarker m = new PaxMarker(marker);
            if (m_delegate.isInfoEnabled(m)) {
                FormattingTuple tuple = MessageFormatter.format(format, arg);
                Throwable t = tuple.getThrowable();
                if (t == null) {
                    m_delegate.info(m, tuple.getMessage());
                } else {
                    m_delegate.info(m, tuple.getMessage(), t);
                }
            }
        } else {
            if (m_delegate.isInfoEnabled()) {
                FormattingTuple tuple = MessageFormatter.format(format, arg);
                Throwable t = tuple.getThrowable();
                if (t == null) {
                    m_delegate.info(tuple.getMessage());
                } else {
                    m_delegate.info(tuple.getMessage(), t);
                }
            }
        }
    }

    /**
     * This method is similar to {@link #info(String, Object, Object)}
     * method except that the marker data is also taken into
     * consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     */
    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        if (marker != null) {
            PaxMarker m = new PaxMarker(marker);
            if (m_delegate.isInfoEnabled(m)) {
                FormattingTuple tuple = MessageFormatter.format(format, arg1, arg2);
                Throwable t = tuple.getThrowable();
                if (t == null) {
                    m_delegate.info(m, tuple.getMessage());
                } else {
                    m_delegate.info(m, tuple.getMessage(), t);
                }
            }
        } else {
            if (m_delegate.isInfoEnabled()) {
                FormattingTuple tuple = MessageFormatter.format(format, arg1, arg2);
                Throwable t = tuple.getThrowable();
                if (t == null) {
                    m_delegate.info(tuple.getMessage());
                } else {
                    m_delegate.info(tuple.getMessage(), t);
                }
            }
        }
    }

    /**
     * This method is similar to {@link #info(String, Object[])}
     * method except that the marker data is also taken into
     * consideration.
     *
     * @param marker   the marker data specific to this log statement
     * @param format   the format string
     * @param argArray an array of arguments
     */
    @Override
    public void info(Marker marker, String format, Object[] argArray) {
        if (marker != null) {
            PaxMarker m = new PaxMarker(marker);
            if (m_delegate.isInfoEnabled(m)) {
                FormattingTuple tuple = MessageFormatter.arrayFormat(format, argArray);
                Throwable t = tuple.getThrowable();
                if (t == null) {
                    m_delegate.info(m, tuple.getMessage());
                } else {
                    m_delegate.info(m, tuple.getMessage(), t);
                }
            }
        } else {
            if (m_delegate.isInfoEnabled()) {
                FormattingTuple tuple = MessageFormatter.arrayFormat(format, argArray);
                Throwable t = tuple.getThrowable();
                if (t == null) {
                    m_delegate.info(tuple.getMessage());
                } else {
                    m_delegate.info(tuple.getMessage(), t);
                }
            }
        }
    }

    /**
     * This method is similar to {@link #info(String, Throwable)} method
     * except that the marker data is also taken into consideration.
     *
     * @param marker the marker data for this log statement
     * @param msg    the message accompanying the exception
     * @param t      the exception (throwable) to log
     */
    @Override
    public void info(Marker marker, String msg, Throwable t) {
        if (marker != null) {
            PaxMarker m = new PaxMarker(marker);
            if (m_delegate.isInfoEnabled(m)) {
                m_delegate.info(m, msg, t);
            }
        } else {
            if (m_delegate.isInfoEnabled()) {
                m_delegate.info(msg, t);
            }
        }
    }

    /**
     * Is the logger instance enabled for the WARN level?
     *
     * @return True if this Logger is enabled for the WARN level,
     *         false otherwise.
     */
    @Override
    public boolean isWarnEnabled() {
        return m_delegate.isWarnEnabled();
    }

    /**
     * Log a message at the WARN level.
     *
     * @param msg the message string to be logged
     */
    @Override
    public void warn(String msg) {
        m_delegate.warn(msg);
    }

    /**
     * Log a message at the WARN level according to the specified format
     * and argument.
     *
     * This form avoids superfluous object creation when the logger
     * is disabled for the WARN level.
     *
     * @param format the format string
     * @param arg    the argument
     */
    @Override
    public void warn(String format, Object arg) {
        if (m_delegate.isWarnEnabled()) {
            FormattingTuple tuple = MessageFormatter.format(format, arg);
            Throwable t = tuple.getThrowable();
            if (t == null) {
                m_delegate.warn(tuple.getMessage());
            } else {
                m_delegate.warn(tuple.getMessage(), t);
            }
        }
    }

    /**
     * Log a message at the WARN level according to the specified format
     * and arguments.
     *
     * This form avoids superfluous object creation when the logger
     * is disabled for the WARN level.
     *
     * @param format   the format string
     * @param argArray an array of arguments
     */
    @Override
    public void warn(String format, Object[] argArray) {
        if (m_delegate.isWarnEnabled()) {
            FormattingTuple tuple = MessageFormatter.arrayFormat(format, argArray);
            Throwable t = tuple.getThrowable();
            if (t == null) {
                m_delegate.warn(tuple.getMessage());
            } else {
                m_delegate.warn(tuple.getMessage(), t);
            }
        }
    }

    /**
     * Log a message at the WARN level according to the specified format
     * and arguments.
     *
     * This form avoids superfluous object creation when the logger
     * is disabled for the WARN level.
     *
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     */
    @Override
    public void warn(String format, Object arg1, Object arg2) {
        if (m_delegate.isWarnEnabled()) {
            FormattingTuple tuple = MessageFormatter.format(format, arg1, arg2);
            Throwable t = tuple.getThrowable();
            if (t == null) {
                m_delegate.warn(tuple.getMessage());
            } else {
                m_delegate.warn(tuple.getMessage(), t);
            }
        }
    }

    /**
     * Log an exception (throwable) at the WARN level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     */
    @Override
    public void warn(String msg, Throwable t) {
        m_delegate.warn(msg, t);
    }

    /**
     * Similar to {@link #isWarnEnabled()} method except that the marker
     * data is also taken into consideration.
     *
     * @param marker The marker data to take into consideration
     */
    @Override
    public boolean isWarnEnabled(Marker marker) {
        return m_delegate.isWarnEnabled(new PaxMarker(marker));
    }

    /**
     * Log a message with the specific Marker at the WARN level.
     *
     * @param marker The marker specific to this log statement
     * @param msg    the message string to be logged
     */
    @Override
    public void warn(Marker marker, String msg) {
        if (marker != null) {
            PaxMarker m = new PaxMarker(marker);
            if (m_delegate.isWarnEnabled(m)) {
                m_delegate.warn(m, msg);
            }
        } else {
            if (m_delegate.isWarnEnabled()) {
                m_delegate.warn(msg);
            }
        }
    }

    /**
     * This method is similar to {@link #warn(String, Object)} method except that the
     * marker data is also taken into consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg    the argument
     */
    @Override
    public void warn(Marker marker, String format, Object arg) {
        if (marker != null) {
            PaxMarker m = new PaxMarker(marker);
            if (m_delegate.isWarnEnabled(m)) {
                FormattingTuple tuple = MessageFormatter.format(format, arg);
                Throwable t = tuple.getThrowable();
                if (t == null) {
                    m_delegate.warn(m, tuple.getMessage());
                } else {
                    m_delegate.warn(m, tuple.getMessage(), t);
                }
            }
        } else {
            if (m_delegate.isWarnEnabled()) {
                FormattingTuple tuple = MessageFormatter.format(format, arg);
                Throwable t = tuple.getThrowable();
                if (t == null) {
                    m_delegate.warn(tuple.getMessage());
                } else {
                    m_delegate.warn(tuple.getMessage(), t);
                }
            }
        }
    }

    /**
     * This method is similar to {@link #warn(String, Object, Object)}
     * method except that the marker data is also taken into
     * consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     */
    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        if (marker != null) {
            PaxMarker m = new PaxMarker(marker);
            if (m_delegate.isWarnEnabled(m)) {
                FormattingTuple tuple = MessageFormatter.format(format, arg1, arg2);
                Throwable t = tuple.getThrowable();
                if (t == null) {
                    m_delegate.warn(m, tuple.getMessage());
                } else {
                    m_delegate.warn(m, tuple.getMessage(), t);
                }
            }
        } else {
            if (m_delegate.isWarnEnabled()) {
                FormattingTuple tuple = MessageFormatter.format(format, arg1, arg2);
                Throwable t = tuple.getThrowable();
                if (t == null) {
                    m_delegate.warn(tuple.getMessage());
                } else {
                    m_delegate.warn(tuple.getMessage(), t);
                }
            }
        }
    }

    /**
     * This method is similar to {@link #warn(String, Object[])}
     * method except that the marker data is also taken into
     * consideration.
     *
     * @param marker   the marker data specific to this log statement
     * @param format   the format string
     * @param argArray an array of arguments
     */
    @Override
    public void warn(Marker marker, String format, Object[] argArray) {
        if (marker != null) {
            PaxMarker m = new PaxMarker(marker);
            if (m_delegate.isWarnEnabled(m)) {
                FormattingTuple tuple = MessageFormatter.arrayFormat(format, argArray);
                Throwable t = tuple.getThrowable();
                if (t == null) {
                    m_delegate.warn(m, tuple.getMessage());
                } else {
                    m_delegate.warn(m, tuple.getMessage(), t);
                }
            }
        } else {
            if (m_delegate.isWarnEnabled()) {
                FormattingTuple tuple = MessageFormatter.arrayFormat(format, argArray);
                Throwable t = tuple.getThrowable();
                if (t == null) {
                    m_delegate.warn(tuple.getMessage());
                } else {
                    m_delegate.warn(tuple.getMessage(), t);
                }
            }
        }
    }

    /**
     * This method is similar to {@link #warn(String, Throwable)} method
     * except that the marker data is also taken into consideration.
     *
     * @param marker the marker data for this log statement
     * @param msg    the message accompanying the exception
     * @param t      the exception (throwable) to log
     */
    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        if (marker != null) {
            PaxMarker m = new PaxMarker(marker);
            if (m_delegate.isWarnEnabled(m)) {
                m_delegate.warn(m, msg, t);
            }
        } else {
            if (m_delegate.isWarnEnabled()) {
                m_delegate.warn(msg, t);
            }
        }
    }

    /**
     * Is the logger instance enabled for the ERROR level?
     *
     * @return True if this Logger is enabled for the ERROR level,
     *         false otherwise.
     */
    @Override
    public boolean isErrorEnabled() {
        return m_delegate.isErrorEnabled();
    }

    /**
     * Log a message at the ERROR level.
     *
     * @param msg the message string to be logged
     */
    @Override
    public void error(String msg) {
        m_delegate.error(msg);
    }

    /**
     * Log a message at the ERROR level according to the specified format
     * and argument.
     *
     * This form avoids superfluous object creation when the logger
     * is disabled for the ERROR level.
     *
     * @param format the format string
     * @param arg    the argument
     */
    @Override
    public void error(String format, Object arg) {
        if (m_delegate.isErrorEnabled()) {
            FormattingTuple tuple = MessageFormatter.format(format, arg);
            Throwable t = tuple.getThrowable();
            if (t == null) {
                m_delegate.error(tuple.getMessage());
            } else {
                m_delegate.error(tuple.getMessage(), t);
            }
        }
    }

    /**
     * Log a message at the ERROR level according to the specified format
     * and arguments.
     *
     * This form avoids superfluous object creation when the logger
     * is disabled for the ERROR level.
     *
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     */
    @Override
    public void error(String format, Object arg1, Object arg2) {
        if (m_delegate.isErrorEnabled()) {
            FormattingTuple tuple = MessageFormatter.format(format, arg1, arg2);
            Throwable t = tuple.getThrowable();
            if (t == null) {
                m_delegate.error(tuple.getMessage());
            } else {
                m_delegate.error(tuple.getMessage(), t);
            }
        }
    }

    /**
     * Log a message at the ERROR level according to the specified format
     * and arguments.
     *
     * This form avoids superfluous object creation when the logger
     * is disabled for the ERROR level.
     *
     * @param format   the format string
     * @param argArray an array of arguments
     */
    @Override
    public void error(String format, Object[] argArray) {
        if (m_delegate.isErrorEnabled()) {
            FormattingTuple tuple = MessageFormatter.arrayFormat(format, argArray);
            Throwable t = tuple.getThrowable();
            if (t == null) {
                m_delegate.error(tuple.getMessage());
            } else {
                m_delegate.error(tuple.getMessage(), t);
            }
        }
    }

    /**
     * Log an exception (throwable) at the ERROR level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     */
    @Override
    public void error(String msg, Throwable t) {
        m_delegate.error(msg, t);
    }

    /**
     * Similar to {@link #isErrorEnabled()} method except that the
     * marker data is also taken into consideration.
     *
     * @param marker The marker data to take into consideration
     */
    @Override
    public boolean isErrorEnabled(Marker marker) {
        return m_delegate.isErrorEnabled(new PaxMarker(marker));
    }

    /**
     * Log a message with the specific Marker at the ERROR level.
     *
     * @param marker The marker specific to this log statement
     * @param msg    the message string to be logged
     */
    @Override
    public void error(Marker marker, String msg) {
        if (marker != null) {
            PaxMarker m = new PaxMarker(marker);
            if (m_delegate.isErrorEnabled(m)) {
                m_delegate.error(m, msg);
            }
        } else {
            if (m_delegate.isErrorEnabled()) {
                m_delegate.error(msg);
            }
        }
    }

    /**
     * This method is similar to {@link #error(String, Object)} method except that the
     * marker data is also taken into consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg    the argument
     */
    @Override
    public void error(Marker marker, String format, Object arg) {
        if (marker != null) {
            PaxMarker m = new PaxMarker(marker);
            if (m_delegate.isErrorEnabled(m)) {
                FormattingTuple tuple = MessageFormatter.format(format, arg);
                Throwable t = tuple.getThrowable();
                if (t == null) {
                    m_delegate.error(m, tuple.getMessage());
                } else {
                    m_delegate.error(m, tuple.getMessage(), t);
                }
            }
        } else {
            if (m_delegate.isErrorEnabled()) {
                FormattingTuple tuple = MessageFormatter.format(format, arg);
                Throwable t = tuple.getThrowable();
                if (t == null) {
                    m_delegate.error(tuple.getMessage());
                } else {
                    m_delegate.error(tuple.getMessage(), t);
                }
            }
        }
    }

    /**
     * This method is similar to {@link #error(String, Object, Object)}
     * method except that the marker data is also taken into
     * consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     */
    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        if (marker != null) {
            PaxMarker m = new PaxMarker(marker);
            if (m_delegate.isErrorEnabled(m)) {
                FormattingTuple tuple = MessageFormatter.format(format, arg1, arg2);
                Throwable t = tuple.getThrowable();
                if (t == null) {
                    m_delegate.error(m, tuple.getMessage());
                } else {
                    m_delegate.error(m, tuple.getMessage(), t);
                }
            }
        } else {
            if (m_delegate.isErrorEnabled()) {
                FormattingTuple tuple = MessageFormatter.format(format, arg1, arg2);
                Throwable t = tuple.getThrowable();
                if (t == null) {
                    m_delegate.error(tuple.getMessage());
                } else {
                    m_delegate.error(tuple.getMessage(), t);
                }
            }
        }
    }

    /**
     * This method is similar to {@link #error(String, Object[])}
     * method except that the marker data is also taken into
     * consideration.
     *
     * @param marker   the marker data specific to this log statement
     * @param format   the format string
     * @param argArray an array of arguments
     */
    @Override
    public void error(Marker marker, String format, Object[] argArray) {
        if (marker != null) {
            PaxMarker m = new PaxMarker(marker);
            if (m_delegate.isErrorEnabled(m)) {
                FormattingTuple tuple = MessageFormatter.arrayFormat(format, argArray);
                Throwable t = tuple.getThrowable();
                if (t == null) {
                    m_delegate.error(m, tuple.getMessage());
                } else {
                    m_delegate.error(m, tuple.getMessage(), t);
                }
            }
        } else {
            if (m_delegate.isErrorEnabled()) {
                FormattingTuple tuple = MessageFormatter.arrayFormat(format, argArray);
                Throwable t = tuple.getThrowable();
                if (t == null) {
                    m_delegate.error(tuple.getMessage());
                } else {
                    m_delegate.error(tuple.getMessage(), t);
                }
            }
        }
    }

    /**
     * This method is similar to {@link #error(String, Throwable)}
     * method except that the marker data is also taken into
     * consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param msg    the message accompanying the exception
     * @param t      the exception (throwable) to log
     */
    @Override
    public void error(Marker marker, String msg, Throwable t) {
        if (marker != null) {
            PaxMarker m = new PaxMarker(marker);
            if (m_delegate.isErrorEnabled(m)) {
                m_delegate.error(m, msg, t);
            }
        } else {
            if (m_delegate.isErrorEnabled()) {
                m_delegate.error(msg, t);
            }
        }
    }

    /**
     * This method implements LocationAwareLogger.log
     *
     * The caller passes in it's own Fully Qualified Class Name (fqcn).
     *
     * @param marker
     * @param fqcn the fully qualified class name (FQCN) of the <b>caller</b>
     * @param level Integer representation of the log level as defined in LocationAwareLogger
     * @param message the message as a format string
     * @param argArray an array of arguments to use in the message format string
     * @param t the throwable to log
     */
    @Override
    public void log(Marker marker, String fqcn, int level, String message, Object[] argArray, Throwable t) {
        PaxMarker paxMarker = null;
        if (marker != null) {
            paxMarker = new PaxMarker(marker);
        }
        switch (level) {
            case (TRACE_INT):
                if (m_delegate.isTraceEnabled()) {
                    FormattingTuple tuple = MessageFormatter.arrayFormat(message, argArray);
                    if (paxMarker != null) {
                        m_delegate.trace(paxMarker, tuple.getMessage(), t);
                    } else {
                        m_delegate.trace(tuple.getMessage(), t);
                    }
                }
                break;
            case (DEBUG_INT):
                if (m_delegate.isDebugEnabled()) {
                    FormattingTuple tuple = MessageFormatter.arrayFormat(message, argArray);
                    if (paxMarker != null) {
                        m_delegate.debug(paxMarker, tuple.getMessage(), t);
                    } else {
                        m_delegate.debug(tuple.getMessage(), t);
                    }
                }
                break;
            case (INFO_INT):
                if (m_delegate.isInfoEnabled()) {
                    FormattingTuple tuple = MessageFormatter.arrayFormat(message, argArray);
                    if (paxMarker != null) {
                        m_delegate.info(paxMarker, tuple.getMessage(), t);
                    } else {
                        m_delegate.info(tuple.getMessage(), t);
                    }
                }
                break;
            case (WARN_INT):
                if (m_delegate.isWarnEnabled()) {
                    FormattingTuple tuple = MessageFormatter.arrayFormat(message, argArray);
                    if (paxMarker != null) {
                        m_delegate.warn(paxMarker, tuple.getMessage(), t);
                    } else {
                        m_delegate.warn(tuple.getMessage(), t);
                    }
                }
                break;
            case (ERROR_INT):
                if (m_delegate.isErrorEnabled()) {
                    FormattingTuple tuple = MessageFormatter.arrayFormat(message, argArray);
                    if (paxMarker != null) {
                        m_delegate.error(paxMarker, tuple.getMessage(), t);
                    } else {
                        m_delegate.error(tuple.getMessage(), t);
                    }
                }
                break;
            default:
                break;
        }
    }

}
