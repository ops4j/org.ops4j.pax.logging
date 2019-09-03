/*  Copyright 2007 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.logging.spi.support;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicLong;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogLevel;

public class LogEntryImpl implements LogEntry {

    private static AtomicLong seq = new AtomicLong(0);

    private String m_name;
    private long m_time;
    private WeakReference<Bundle> m_bundle;
    private WeakReference<ServiceReference> m_service;
    private int m_level;
    private LogLevel m_logLevel;
    private String m_message;
    private Throwable m_exception;
    private String m_thread;
    private Long m_seq;

    public LogEntryImpl(String name, Bundle bundle, ServiceReference service, LogLevel level, String message, Throwable exception) {
        if (bundle != null) {
            m_bundle = new WeakReference<>(bundle);
        }
        if (service != null) {
            m_service = new WeakReference<>(service);
        }
        m_logLevel = level;
        // Always take the ordinal, as org.osgi.service.log.LogService.log(int level, ...) methods
        // obtain loggers anyway under covers
        m_level = level.ordinal();
        m_message = message;
        m_exception = exception;
        m_time = System.currentTimeMillis();
        m_name = name;
        m_thread = Thread.currentThread().getName();
        m_seq = seq.incrementAndGet();
    }

    @Override
    public Bundle getBundle() {
        return m_bundle == null ? null : m_bundle.get();
    }

    @Override
    public ServiceReference getServiceReference() {
        return m_service == null ? null : m_service.get();
    }

    @Override
    public int getLevel() {
        return m_level;
    }

    @Override
    public String getMessage() {
        return m_message;
    }

    @Override
    public Throwable getException() {
        return m_exception;
    }

    @Override
    public long getTime() {
        return m_time;
    }

    @Override
    public LogLevel getLogLevel() {
        return m_logLevel;
    }

    @Override
    public String getLoggerName() {
        return m_name;
    }

    @Override
    public long getSequence() {
        return m_seq;
    }

    @Override
    public String getThreadInfo() {
        return m_thread;
    }

    @Override
    public StackTraceElement getLocation() {
        return null;
    }

}
