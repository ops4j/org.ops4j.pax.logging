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
package org.ops4j.pax.logging.spi.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.ops4j.pax.logging.PaxLogger;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;

/**
 * <p>Implementation of standard {@link LogReaderService} required by OSGi Compendium R6 "101.4 Log Reader Service".</p>
 * <p>This class was previously implemented in all backends (differently in each of them).
 * Now it's common in pax-logging-api bundle and may be reused by the backends (using {@link BackendSupport}).</p>
 *
 * @since 1.11.0
 */
public class LogReaderServiceImpl implements LogReaderService {

    private final List<LogListener> m_listeners = new CopyOnWriteArrayList<LogListener>();

    private final Deque<LogEntry> m_entries;
    private int m_maxEntries;
    // Internal logger for diagnostic purposes
    private final PaxLogger m_logger;

    public LogReaderServiceImpl(int m_maxEntries, PaxLogger logger) {
        this.m_maxEntries = m_maxEntries;
        this.m_entries = new LinkedList<>();
        this.m_logger = logger;
    }

    @Override
    public void addLogListener(LogListener listener) {
        m_listeners.add(listener);
    }

    @Override
    public void removeLogListener(LogListener listener) {
        m_listeners.remove(listener);
    }

    @Override
    public Enumeration<LogEntry> getLog() {
        // Need to do a copy to avoid a ConcurrentModificationException if
        // a new event is logged while the enumeration is iterated.
        synchronized (m_entries) {
            return Collections.enumeration(new ArrayList<LogEntry>(m_entries));
        }
    }

    public void fireEvent(LogEntry entry) {
        synchronized (m_entries) {
            m_entries.addFirst(entry);
            cleanUp();
        }
        final List<LogListener> listeners = m_listeners;
        for (LogListener listener : listeners) {
            fire(listener, entry);
        }
    }

    public void setMaxEntries(int maxSize) {
        synchronized (m_entries) {
            m_maxEntries = maxSize;
        }
    }

    /**
     * Trim the log to configured size.
     */
    private void cleanUp() {
        // caller must synchronize on m_entries
        while (m_entries.size() > m_maxEntries) {
            m_entries.removeLast();
        }
    }

    private void fire(LogListener listener, LogEntry entry) {
        try {
            listener.logged(entry);
        } catch (Throwable e) {
            if (m_logger != null) {
                m_logger.error("'" + listener + "' is removed as a LogListener since it threw an exception.", e);
                removeLogListener(listener);
            }
        }
    }

}
