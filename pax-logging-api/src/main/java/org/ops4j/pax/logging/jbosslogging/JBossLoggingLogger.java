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
package org.ops4j.pax.logging.jbosslogging;

import java.text.MessageFormat;

import org.jboss.logging.Logger;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingManager;
import org.ops4j.pax.logging.PaxLoggingManagerAwareLogger;
import org.ops4j.pax.logging.spi.support.FallbackLogFactory;
import org.osgi.framework.FrameworkUtil;

/**
 * <p>pax-logging specific {@link Logger} that delegates to {@link PaxLogger} that is obtained from
 * framework specific {@link org.ops4j.pax.logging.PaxLoggingService} and eventually delegates to logging
 * implementation.</p>
 */
public class JBossLoggingLogger extends Logger implements PaxLoggingManagerAwareLogger {

    static final String JBOSS_LOGGING_FQCN = JBossLoggingLogger.class.getName();

    private String m_name;
    private volatile PaxLogger m_delegate;

    public JBossLoggingLogger(String name, PaxLogger delegate) {
        super(name);
        m_name = name;
        m_delegate = delegate;
    }

    @Override
    public void setPaxLoggingManager(PaxLoggingManager manager) {
        if (manager == null) {
            m_delegate = FallbackLogFactory.createFallbackLog(FrameworkUtil.getBundle(JBossLoggingLogger.class), m_name);
        } else {
            m_delegate = manager.getLogger(m_name, JBOSS_LOGGING_FQCN);
        }
    }

    @Override
    protected void doLog(Level level, String loggerClassName, Object message, Object[] parameters, Throwable thrown) {
        final String text = parameters == null || parameters.length == 0 ? String.valueOf(message) : MessageFormat.format(String.valueOf(message), parameters);
        switch (level) {
            case FATAL:
                m_delegate.fatal(text, thrown, loggerClassName);
                break;
            case ERROR:
                m_delegate.error(text, thrown, loggerClassName);
                break;
            case WARN:
                m_delegate.warn(text, thrown, loggerClassName);
                break;
            case INFO:
                m_delegate.inform(text, thrown, loggerClassName);
                break;
            case DEBUG:
                m_delegate.debug(text, thrown, loggerClassName);
                break;
            case TRACE:
                m_delegate.trace(text, thrown, loggerClassName);
                break;
        }
    }

    @Override
    protected void doLogf(Level level, String loggerClassName, String format, Object[] parameters, Throwable thrown) {
        doLog(level, loggerClassName, format, parameters, thrown);
    }

    @Override
    public boolean isEnabled(Level level) {
        switch (level) {
            case FATAL:
                return m_delegate.isFatalEnabled();
            case ERROR:
                return m_delegate.isErrorEnabled();
            case WARN:
                return m_delegate.isWarnEnabled();
            case INFO:
                return m_delegate.isInfoEnabled();
            case DEBUG:
                return m_delegate.isDebugEnabled();
            case TRACE:
                return m_delegate.isTraceEnabled();
        }
        return false;
    }

}
