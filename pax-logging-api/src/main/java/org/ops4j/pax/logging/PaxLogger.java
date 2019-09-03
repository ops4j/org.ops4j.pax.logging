/*
 * Copyright 2005 Niclas Hedhman.
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
package org.ops4j.pax.logging;

import org.osgi.service.log.FormatterLogger;
import org.osgi.service.log.LogLevel;
import org.osgi.service.log.LoggerConsumer;

/**
 * Main pax-logging interface for loggers to interact with any logging system.
 */
public interface PaxLogger extends FormatterLogger {

    String FQCN = PaxLogger.class.getName();

    int LEVEL_TRACE = 0;
    int LEVEL_DEBUG = 1;
    int LEVEL_INFO = 2;
    int LEVEL_WARNING = 3;
    int LEVEL_ERROR = 4;
    int LEVEL_FATAL = 5;
    int LEVEL_AUDIT = 6;
    int LEVEL_NONE = 7;

    // since R7, isXXXEnabled are defined in org.osgi.service.log.Logger. Except for fatal and audit levels
    // org.osgi.service.log.LogLevel.AUDIT is always enabled

    boolean isFatalEnabled();
    default boolean isAuditEnabled() {
        return true;
    }

    // logging methods with marker support - not available in in R7 org.osgi.service.log.Logger

    boolean isTraceEnabled(PaxMarker marker);
    boolean isDebugEnabled(PaxMarker marker);
    boolean isInfoEnabled(PaxMarker marker);
    boolean isWarnEnabled(PaxMarker marker);
    boolean isErrorEnabled(PaxMarker marker);
    boolean isFatalEnabled(PaxMarker marker);
    // R7: org.osgi.service.log.LogLevel.AUDIT - always enabled
    default boolean isAuditEnabled(PaxMarker marker) {
        return true;
    }

    // R7 adds all the trace/debug/info/warn/error/audit methods where last (and next to last)
    // arguments may be java.lang.Throwable and/or org.osgi.framework.ServiceReference. Pax Logging 2.x
    // removes own versions of the methods and only duplicates all org.osgi.service.log.Logger methods
    // to provider marker-aware variants

    void trace(PaxMarker marker, String message);
    void trace(PaxMarker marker, String format, Object arg);
    void trace(PaxMarker marker, String format, Object arg1, Object arg2);
    void trace(PaxMarker marker, String format, Object... arguments);
    <E extends Exception> void trace(PaxMarker marker, LoggerConsumer<E> consumer) throws E;

    void debug(PaxMarker marker, String message);
    void debug(PaxMarker marker, String format, Object arg);
    void debug(PaxMarker marker, String format, Object arg1, Object arg2);
    void debug(PaxMarker marker, String format, Object... arguments);
    <E extends Exception> void debug(PaxMarker marker, LoggerConsumer<E> consumer) throws E;

    void info(PaxMarker marker, String message);
    void info(PaxMarker marker, String format, Object arg);
    void info(PaxMarker marker, String format, Object arg1, Object arg2);
    void info(PaxMarker marker, String format, Object... arguments);
    <E extends Exception> void info(PaxMarker marker, LoggerConsumer<E> consumer) throws E;

    void warn(PaxMarker marker, String message);
    void warn(PaxMarker marker, String format, Object arg);
    void warn(PaxMarker marker, String format, Object arg1, Object arg2);
    void warn(PaxMarker marker, String format, Object... arguments);
    <E extends Exception> void warn(PaxMarker marker, LoggerConsumer<E> consumer) throws E;

    void error(PaxMarker marker, String message);
    void error(PaxMarker marker, String format, Object arg);
    void error(PaxMarker marker, String format, Object arg1, Object arg2);
    void error(PaxMarker marker, String format, Object... arguments);
    <E extends Exception> void error(PaxMarker marker, LoggerConsumer<E> consumer) throws E;

    void audit(PaxMarker marker, String message);
    void audit(PaxMarker marker, String format, Object arg);
    void audit(PaxMarker marker, String format, Object arg1, Object arg2);
    void audit(PaxMarker marker, String format, Object... arguments);
    <E extends Exception> void audit(LoggerConsumer<E> consumer) throws E;
    <E extends Exception> void audit(PaxMarker marker, LoggerConsumer<E> consumer) throws E;

    // there are no fatal() methods in org.osgi.service.log.Logger

    void fatal(String message);
    void fatal(String format, Object arg);
    void fatal(String format, Object arg1, Object arg2);
    void fatal(String format, Object... arguments);
    <E extends Exception> void fatal(LoggerConsumer<E> consumer) throws E;
    void fatal(PaxMarker marker, String message);
    void fatal(PaxMarker marker, String format, Object arg);
    void fatal(PaxMarker marker, String format, Object arg1, Object arg2);
    void fatal(PaxMarker marker, String format, Object... arguments);
    <E extends Exception> void fatal(PaxMarker marker, LoggerConsumer<E> consumer) throws E;

    // With OSGi R7, the methods from org.osgi.service.log.Logger interface caused a conflict with previous
    // PaxLogger methods that allowed to pass FQCN. That's why we had to create new methods with "fq" prefix to
    // remove conflicts with varargs
    // these mathods usually take already processed message, no more need to format it using parameters.

    void fqtrace(String fqcn, String message);
    void fqdebug(String fqcn, String message);
    void fqinfo(String fqcn, String message);
    void fqwarn(String fqcn, String message);
    void fqerror(String fqcn, String message);
    void fqfatal(String fqcn, String message);

    void fqtrace(String fqcn, PaxMarker marker, String message);
    void fqdebug(String fqcn, PaxMarker marker, String message);
    void fqinfo(String fqcn, PaxMarker marker, String message);
    void fqwarn(String fqcn, PaxMarker marker, String message);
    void fqerror(String fqcn, PaxMarker marker, String message);
    void fqfatal(String fqcn, PaxMarker marker, String message);

    void fqtrace(String fqcn, String message, Throwable t);
    void fqdebug(String fqcn, String message, Throwable t);
    void fqinfo(String fqcn, String message, Throwable t);
    void fqwarn(String fqcn, String message, Throwable t);
    void fqerror(String fqcn, String message, Throwable t);
    void fqfatal(String fqcn, String message, Throwable t);

    void fqtrace(String fqcn, PaxMarker marker, String message, Throwable t);
    void fqdebug(String fqcn, PaxMarker marker, String message, Throwable t);
    void fqinfo(String fqcn, PaxMarker marker, String message, Throwable t);
    void fqwarn(String fqcn, PaxMarker marker, String message, Throwable t);
    void fqerror(String fqcn, PaxMarker marker, String message, Throwable t);
    void fqfatal(String fqcn, PaxMarker marker, String message, Throwable t);

    /**
     * <p>Returns numerical log level associated with this logger. Higher values mean more <em>important</em>
     * levels (as in {@link org.ops4j.pax.logging.spi.PaxLevel}). Only these constants should be returned
     * (in increasing importance/severity):<ul>
     *     <li>{@link PaxLogger#LEVEL_NONE}</li>
     *     <li>{@link PaxLogger#LEVEL_TRACE}</li>
     *     <li>{@link PaxLogger#LEVEL_DEBUG}</li>
     *     <li>{@link PaxLogger#LEVEL_INFO}</li>
     *     <li>{@link PaxLogger#LEVEL_WARNING}</li>
     *     <li>{@link PaxLogger#LEVEL_ERROR}</li>
     *     <li>{@link PaxLogger#LEVEL_AUDIT}</li>
     * </ul></p>
     * @return
     */
    int getPaxLogLevel();

    /**
     * <p>Returns R7 {@link LogLevel} for this logger.</p>
     * @return
     * @since 2.0.0
     */
    LogLevel getLogLevel();

    /**
     * {@link PaxContext} of this logger that gives access to thread-bound MDC context.
     * @return
     */
    PaxContext getPaxContext();

}
