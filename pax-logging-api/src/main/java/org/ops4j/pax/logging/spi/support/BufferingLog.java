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
package org.ops4j.pax.logging.spi.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ops4j.pax.logging.PaxContext;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxMarker;
import org.osgi.framework.Bundle;

/**
 * Experimental fallback strategy for non-availability.
 */
public class BufferingLog implements PaxLogger {

    private final List<LogPackage> m_queue;
    private final String m_fqcn;
    private PaxContext m_context = new PaxContext();
    public BufferingLog(Bundle bundle, String categoryName) {
        m_fqcn = getClass().getName();
        m_queue = new ArrayList<LogPackage>();
    }

    void flush(PaxLogger destination) {
        for (LogPackage pack : m_queue) {
            String fqcn = pack.getFqcn();
            Throwable throwable = pack.getException();
            String message = pack.getMessage();
            getPaxContext().putAll(pack.getContext());
            LogType logType = pack.getType();
            int logTypeAsInt = logType.getType();
            switch (logTypeAsInt) {
                case LogType.DEBUG_INT:
                    destination.debug(message, throwable, fqcn);
                    break;
                case LogType.TRACE_INT:
                    destination.trace(message, throwable, fqcn);
                    break;
                case LogType.INFO_INT:
                    destination.inform(message, throwable, fqcn);
                    break;
                case LogType.WARN_INT:
                    destination.warn(message, throwable, fqcn);
                    break;
                case LogType.ERROR_INT:
                    destination.error(message, throwable, fqcn);
                    break;
                case LogType.FATAL_INT:
                    destination.fatal(message, throwable, fqcn);
                    break;
            }
            getPaxContext().clear();
        }
    }

    @Override
    public boolean isTraceEnabled() {
        return true;
    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public boolean isFatalEnabled() {
        return true;
    }

    @Override
    public boolean isTraceEnabled(PaxMarker marker) {
        return true;
    }

    @Override
    public boolean isDebugEnabled(PaxMarker marker) {
        return true;
    }

    @Override
    public boolean isInfoEnabled(PaxMarker marker) {
        return true;
    }

    @Override
    public boolean isWarnEnabled(PaxMarker marker) {
        return true;
    }

    @Override
    public boolean isErrorEnabled(PaxMarker marker) {
        return true;
    }

    @Override
    public boolean isFatalEnabled(PaxMarker marker) {
        return true;
    }

    @Override
    public void trace(String message, Throwable t) {
        LogPackage p = new LogPackage(m_fqcn, LogType.trace, message, t, getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void debug(String message, Throwable t) {
        LogPackage p = new LogPackage(m_fqcn, LogType.debug, message, t, getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void inform(String message, Throwable t) {
        LogPackage p = new LogPackage(m_fqcn, LogType.info, message, t, getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void warn(String message, Throwable t) {
        LogPackage p = new LogPackage(m_fqcn, LogType.warn, message, t, getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void error(String message, Throwable t) {
        LogPackage p = new LogPackage(m_fqcn, LogType.error, message, t, getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void fatal(String message, Throwable t) {
        LogPackage p = new LogPackage(m_fqcn, LogType.fatal, message, t, getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void trace(String message, Throwable t, String fqcn) {
        LogPackage p = new LogPackage(fqcn, LogType.trace, message, t, getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void debug(String message, Throwable t, String fqcn) {
        LogPackage p = new LogPackage(fqcn, LogType.debug, message, t, getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void inform(String message, Throwable t, String fqcn) {
        LogPackage p = new LogPackage(fqcn, LogType.info, message, t, getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void warn(String message, Throwable t, String fqcn) {
        LogPackage p = new LogPackage(fqcn, LogType.warn, message, t, getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void error(String message, Throwable t, String fqcn) {
        LogPackage p = new LogPackage(fqcn, LogType.error, message, t, getPaxContext().getContext());
        m_queue.add(p);
    }

    @Override
    public void fatal(String message, Throwable t, String fqcn) {
        LogPackage p = new LogPackage(fqcn, LogType.fatal, message, t, getPaxContext().getContext());
        m_queue.add(p);
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
    public void trace(PaxMarker marker, String message, Throwable t, String fqcn) {
        trace(message, t, fqcn);
    }

    @Override
    public void debug(PaxMarker marker, String message, Throwable t, String fqcn) {
        debug(message, t, fqcn);
    }

    @Override
    public void inform(PaxMarker marker, String message, Throwable t, String fqcn) {
        inform(message, t, fqcn);
    }

    @Override
    public void warn(PaxMarker marker, String message, Throwable t, String fqcn) {
        warn(message, t, fqcn);
    }

    @Override
    public void error(PaxMarker marker, String message, Throwable t, String fqcn) {
        error(message, t, fqcn);
    }

    @Override
    public void fatal(PaxMarker marker, String message, Throwable t, String fqcn) {
        fatal(message, t, fqcn);
    }

    @Override
    public int getLogLevel() {
        return PaxLogger.LEVEL_TRACE;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public PaxContext getPaxContext() {
        return m_context;
    }

    private static class LogType {

        private static final int TRACE_INT = 0;
        private static final int DEBUG_INT = 1;
        private static final int INFO_INT = 2;
        private static final int WARN_INT = 3;
        private static final int ERROR_INT = 4;
        private static final int FATAL_INT = 5;

        private static LogType trace = new LogType(TRACE_INT);
        private static LogType debug = new LogType(DEBUG_INT);
        private static LogType info = new LogType(INFO_INT);
        private static LogType warn = new LogType(WARN_INT);
        private static LogType error = new LogType(ERROR_INT);
        private static LogType fatal = new LogType(FATAL_INT);

        private final int m_type;

        private LogType(int type) {
            m_type = type;
        }

        private int getType() {
            return m_type;
        }
    }

    private static class LogPackage {

        private final String m_fqcn;
        private final LogType m_type;
        private final String m_message;
        private final Throwable m_exception;
        private final Map<String, Object> m_context;

        public LogPackage(String fqcn, LogType type, String message, Throwable exception, Map<String, Object> context) {
            m_fqcn = fqcn;
            m_type = type;
            m_message = message;
            m_exception = exception;
            m_context = context;
            if (m_exception != null)
                m_exception.fillInStackTrace();
        }

        public String getFqcn() {
            return m_fqcn;
        }

        public String getMessage() {
            return m_message;
        }

        public Throwable getException() {
            return m_exception;
        }

        public LogType getType() {
            return m_type;
        }

        public Map<String, Object> getContext() {
            return m_context;
        }
    }

}
