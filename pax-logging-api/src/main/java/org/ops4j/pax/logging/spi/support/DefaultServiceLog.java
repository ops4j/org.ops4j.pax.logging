/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * Copyright 2005 Niclas Hedhman
 * Copyright 2007 Hiram Chirino
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.logging.spi.support;

import java.io.PrintStream;

import org.ops4j.pax.logging.FqcnIgnoringPaxLogger;
import org.ops4j.pax.logging.PaxContext;
import org.ops4j.pax.logging.PaxMarker;
import org.osgi.framework.Bundle;

/**
 * <p>This Logger will be used when the Pax Logging Service is not (yet) available.</p>
 *
 * <p>Default threshold is DEBUG but can be changed if the {@link org.ops4j.pax.logging.PaxLoggingConstants#LOGGING_CFG_DEFAULT_LOG_LEVEL}
 * system or context property is set to on of the following: TRACE, DEBUG, INFO, WARN, ERROR, FATAL, or NONE,
 * by calling the static method {@link #setLogLevel(String)}, where <b>threshold</b> is one of the same strings.</p>
 */
public class DefaultServiceLog extends FqcnIgnoringPaxLogger {

    public static final String[] levels = {
            "TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL"
    };

    public static final int TRACE = 0;
    public static final int DEBUG = 1;
    public static final int INFO = 2;
    public static final int WARN = 3;
    public static final int ERROR = 4;
    public static final int FATAL = 5;
    public static final int NONE = 6;

    /** A threshold level for default log service */
    private static int threshold;

    private Bundle m_bundle;
    private String m_categoryName;
    private PaxContext m_context = new PaxContext();

    static {
        // PAXLOGGING-274: needed when loggers are called before pax-logging-api bundle starts
        setLogLevel(BackendSupport.defaultLogLevel(null));
    }

    DefaultServiceLog(Bundle bundle, String categoryName) {
        m_bundle = bundle;
        m_categoryName = categoryName;
    }

    @Override
    public boolean isTraceEnabled() {
        return threshold <= TRACE;
    }

    @Override
    public boolean isDebugEnabled() {
        return threshold <= DEBUG;
    }

    @Override
    public boolean isInfoEnabled() {
        return threshold <= INFO;
    }

    @Override
    public boolean isWarnEnabled() {
        return threshold <= WARN;
    }

    @Override
    public boolean isErrorEnabled() {
        return threshold <= ERROR;
    }

    @Override
    public boolean isFatalEnabled() {
        return threshold <= FATAL;
    }

    @Override
    public boolean isTraceEnabled(PaxMarker marker) {
        return threshold <= TRACE;
    }

    @Override
    public boolean isDebugEnabled(PaxMarker marker) {
        return threshold <= DEBUG;
    }

    @Override
    public boolean isInfoEnabled(PaxMarker marker) {
        return threshold <= INFO;
    }

    @Override
    public boolean isWarnEnabled(PaxMarker marker) {
        return threshold <= WARN;
    }

    @Override
    public boolean isErrorEnabled(PaxMarker marker) {
        return threshold <= ERROR;
    }

    @Override
    public boolean isFatalEnabled(PaxMarker marker) {
        return threshold <= FATAL;
    }

    @Override
    public void trace(String message, Throwable t) {
        if (isTraceEnabled()) {
            output(levels[TRACE], message, t);
        }
    }

    @Override
    public void debug(String message, Throwable t) {
        if (isDebugEnabled()) {
            output(levels[DEBUG], message, t);
        }
    }

    @Override
    public void inform(String message, Throwable t) {
        if (isInfoEnabled()) {
            output(levels[INFO], message, t);
        }
    }

    @Override
    public void warn(String message, Throwable t) {
        if (isWarnEnabled()) {
            output(levels[WARN], message, t);
        }
    }

    @Override
    public void error(String message, Throwable t) {
        if (isErrorEnabled()) {
            output(levels[ERROR], message, t);
        }
    }

    @Override
    public void fatal(String message, Throwable t) {
        if (isFatalEnabled()) {
            output(levels[FATAL], message, t);
        }
    }

    @Override
    public void trace(PaxMarker marker, String message, Throwable t) {
        trace(message, t);
    }

    @Override
    public void debug(PaxMarker marker, String message, Throwable t) {
        debug(message, t);
    }

    @Override
    public void inform(PaxMarker marker, String message, Throwable t) {
        inform(message, t);
    }

    @Override
    public void warn(PaxMarker marker, String message, Throwable t) {
        warn(message, t);
    }

    @Override
    public void error(PaxMarker marker, String message, Throwable t) {
        error(message, t);
    }

    @Override
    public void fatal(PaxMarker marker, String message, Throwable t) {
        fatal(message, t);
    }

    @Override
    public int getLogLevel() {
        return threshold;
    }

    public static int getStaticLogLevel() {
        return threshold;
    }

    /**
     * <p>Sets the threshold for this default/fallback logger. Events with level lower than given threshold
     * won't be logged.</p>
     * <p>Karaf sets this threshold to {@code ERROR} (in {@code etc/system.properties}).</p>
     * @param level
     */
    public static void setLogLevel(String level) {
        DefaultServiceLog.threshold = convertLevel(level);
    }

    public String getName() {
        return m_categoryName;
    }

    /**
     * Outputs logging <em>event</em> with preconfigured layout.
     * @param levelName
     * @param message
     * @param t
     */
    protected void output(String levelName, String message, Throwable t) {
        synchronized (System.out) {
            output(System.out, levelName, message, t);
        }
    }

    protected final void output(PrintStream out, String levelName, String message, Throwable t) {
        // Might be [null] if used by standard test cases.
        if (m_bundle != null) {
            out.print(m_bundle.getSymbolicName());
            out.print(" ");
        }

        out.print("[");
        out.print(m_categoryName);
        out.print("] ");
        out.print(levelName);
        out.print(" ");
        out.print(": ");
        out.println(message);

        if (t != null) {
            t.printStackTrace(out);
        }
    }

    private static int convertLevel(String levelName) {
        if ("TRACE".equalsIgnoreCase(levelName) || "FINER".equalsIgnoreCase(levelName)) {
            return TRACE;
        } else if ("DEBUG".equalsIgnoreCase(levelName) || "FINE".equalsIgnoreCase(levelName)) {
            return DEBUG;
        } else if ("INFO".equalsIgnoreCase(levelName) || "CONFIG".equalsIgnoreCase(levelName) || "CONF".equalsIgnoreCase(levelName)) {
            return INFO;
        } else if ("WARN".equalsIgnoreCase(levelName) || "WARNING".equalsIgnoreCase(levelName)) {
            return WARN;
        } else if ("ERROR".equalsIgnoreCase(levelName) || "SEVERE".equalsIgnoreCase(levelName)) {
            return ERROR;
        } else if ("FATAL".equalsIgnoreCase(levelName)) {
            return FATAL;
        } else if ("NONE".equalsIgnoreCase(levelName) || "OFF".equalsIgnoreCase(levelName) || "DISABLED".equalsIgnoreCase(levelName)) {
            return NONE;
        } else {
            return DEBUG;
        }
    }

    @Override
    public PaxContext getPaxContext() {
        return m_context;
    }

}
