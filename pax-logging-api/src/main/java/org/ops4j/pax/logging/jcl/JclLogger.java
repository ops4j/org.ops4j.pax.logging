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
package org.ops4j.pax.logging.jcl;

import org.apache.commons.logging.Log;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingManager;
import org.ops4j.pax.logging.PaxLoggingManagerAwareLogger;

/**
 * <p>pax-logging specific {@link Log} that delegates to {@link PaxLogger} that is obtained from
 * framework specific {@link org.ops4j.pax.logging.PaxLoggingService} and eventually delegates to logging
 * implementation.</p>
 */
public class JclLogger implements Log, PaxLoggingManagerAwareLogger {

    public static final String JCL_FQCN = JclLogger.class.getName();

    private String m_name;
    private PaxLogger m_delegate;

    public JclLogger(String name, PaxLogger delegate) {
        m_name = name;
        m_delegate = delegate;
    }

    @Override
    public void setPaxLoggingManager(PaxLoggingManager paxLoggingManager) {
        m_delegate = paxLoggingManager.getLogger(m_name, JCL_FQCN);
    }

    // implementation of org.apache.commons.logging.Log follows.
    // no need to call isXXXEnable, as the delegated logger (PaxLogger) does it anyway

    @Override
    public boolean isDebugEnabled() {
        return m_delegate.isDebugEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return m_delegate.isErrorEnabled();
    }

    @Override
    public boolean isFatalEnabled() {
        return m_delegate.isFatalEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return m_delegate.isInfoEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return m_delegate.isTraceEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return m_delegate.isWarnEnabled();
    }

    @Override
    public void trace(Object message) {
        m_delegate.trace(message == null ? null : message.toString(), null);
    }

    @Override
    public void trace(Object message, Throwable t) {
        m_delegate.trace(message == null ? null : message.toString(), t);
    }

    @Override
    public void debug(Object message) {
        m_delegate.debug(message == null ? null : message.toString(), null);
    }

    @Override
    public void debug(Object message, Throwable t) {
        m_delegate.debug(message == null ? null : message.toString(), t);
    }

    @Override
    public void info(Object message) {
        m_delegate.inform(message == null ? null : message.toString(), null);
    }

    @Override
    public void info(Object message, Throwable t) {
        m_delegate.inform(message == null ? null : message.toString(), t);
    }

    @Override
    public void warn(Object message) {
        m_delegate.warn(message == null ? null : message.toString(), null);
    }

    @Override
    public void warn(Object message, Throwable t) {
        m_delegate.warn(message == null ? null : message.toString(), t);
    }

    @Override
    public void error(Object message) {
        m_delegate.error(message == null ? null : message.toString(), null);
    }

    @Override
    public void error(Object message, Throwable t) {
        m_delegate.error(message == null ? null : message.toString(), t);
    }

    @Override
    public void fatal(Object message) {
        m_delegate.fatal(message == null ? null : message.toString(), null);
    }

    @Override
    public void fatal(Object message, Throwable t) {
        m_delegate.fatal(message == null ? null : message.toString(), t);
    }

}
