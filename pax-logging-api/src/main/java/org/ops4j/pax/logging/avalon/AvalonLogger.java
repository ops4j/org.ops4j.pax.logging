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
package org.ops4j.pax.logging.avalon;

import org.apache.avalon.framework.logger.Logger;
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
public class AvalonLogger implements Logger, PaxLoggingManagerAwareLogger {

    public static final String AVALON_FQCN = AvalonLogger.class.getName();

    private String m_name;
    private PaxLogger m_delegate;

    public AvalonLogger(String name, PaxLogger delegate) {
        m_name = name;
        m_delegate = delegate;
    }

    @Override
    public void setPaxLoggingManager(PaxLoggingManager paxLoggingManager) {
        if (paxLoggingManager == null) {
            m_delegate = FallbackLogFactory.createFallbackLog(FrameworkUtil.getBundle(Logger.class), m_name);
        } else {
            m_delegate = paxLoggingManager.getLogger(m_name, AVALON_FQCN);
        }
    }

    // implementation of org.apache.avalon.framework.logger.Logger follows.
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
    public boolean isFatalErrorEnabled() {
        return m_delegate.isFatalEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return m_delegate.isInfoEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return m_delegate.isWarnEnabled();
    }

    @Override
    public void debug(String string) {
        m_delegate.debug(string);
    }

    @Override
    public void debug(String string, Throwable throwable) {
        m_delegate.debug(string, throwable);
    }

    @Override
    public void info(String string) {
        m_delegate.info(string);
    }

    @Override
    public void info(String string, Throwable throwable) {
        m_delegate.info(string, throwable);
    }

    @Override
    public void warn(String string) {
        m_delegate.warn(string);
    }

    @Override
    public void warn(String string, Throwable throwable) {
        m_delegate.warn(string, throwable);
    }

    @Override
    public void error(String string) {
        m_delegate.error(string);
    }

    @Override
    public void error(String string, Throwable throwable) {
        m_delegate.error(string, throwable);
    }

    @Override
    public void fatalError(String string) {
        m_delegate.fatal(string);
    }

    @Override
    public void fatalError(String string, Throwable throwable) {
        m_delegate.fatal(string, throwable);
    }

    @Override
    public Logger getChildLogger(String name) {
        return AvalonLogFactory.getLogger(this, name);
    }

    public String getName() {
        return m_delegate.getName();
    }

}
