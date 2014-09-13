/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package org.ops4j.pax.logging.log4j;

import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.ops4j.pax.logging.PaxContext;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingService;
import org.osgi.framework.Bundle;

/**
 *
 */
public class Log4jPaxLogger implements PaxLogger {

    private final Logger delegate;
    private final Bundle bundle;
    private final PaxLoggingService service;

    public Log4jPaxLogger(Logger delegate, Bundle bundle, PaxLoggingService service) {
        this.delegate = delegate;
        this.bundle = bundle;
        this.service = service;
    }

    @Override
    public boolean isTraceEnabled() {
        return delegate.isTraceEnabled();
    }

    @Override
    public boolean isDebugEnabled() {
        return delegate.isDebugEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return delegate.isWarnEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return delegate.isInfoEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return delegate.isErrorEnabled();
    }

    @Override
    public boolean isFatalEnabled() {
        return delegate.isFatalEnabled();
    }

    private void setThreadContext() {
        ThreadContext.put("bundle.id", String.valueOf(bundle.getBundleId()));
        ThreadContext.put("bundle.name", bundle.getSymbolicName());
        ThreadContext.put("bundle.version", bundle.getVersion().toString());
        final Map<?, ?> paxContext = service.getPaxContext().getContext();
        if (paxContext != null) {
            for (final Map.Entry<?, ?> entry : paxContext.entrySet()) {
                final String key = String.valueOf(entry.getKey());
                final String value = String.valueOf(entry.getValue());
                ThreadContext.put(key, value);
            }
        }
    }

    private void clearThreadContext() {
        ThreadContext.clearAll();
    }

    @Override
    public void trace(String message, Throwable t) {
        setThreadContext();
        delegate.trace(message, t);
        clearThreadContext();
    }

    @Override
    public void debug(String message, Throwable t) {
        setThreadContext();
        delegate.debug(message, t);
        clearThreadContext();
    }

    @Override
    public void inform(String message, Throwable t) {
        setThreadContext();
        delegate.info(message, t);
        clearThreadContext();
    }

    @Override
    public void warn(String message, Throwable t) {
        setThreadContext();
        delegate.warn(message, t);
        clearThreadContext();
    }

    @Override
    public void error(String message, Throwable t) {
        setThreadContext();
        delegate.error(message, t);
        clearThreadContext();
    }

    @Override
    public void fatal(String message, Throwable t) {
        setThreadContext();
        delegate.fatal(message, t);
        clearThreadContext();
    }

    @Override
    public void trace(String message, Throwable t, String fqcn) {
        trace(message, t);
    }

    @Override
    public void debug(String message, Throwable t, String fqcn) {
        debug(message, t);
    }

    @Override
    public void inform(String message, Throwable t, String fqcn) {
        inform(message, t);
    }

    @Override
    public void warn(String message, Throwable t, String fqcn) {
        warn(message, t);
    }

    @Override
    public void error(String message, Throwable t, String fqcn) {
        error(message, t);
    }

    @Override
    public void fatal(String message, Throwable t, String fqcn) {
        fatal(message, t);
    }

    @Override
    public int getLogLevel() {
        return LevelUtil.convertToPaxLoggingLevel(delegate.getLevel());
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public PaxContext getPaxContext() {
        return service.getPaxContext();
    }
}
