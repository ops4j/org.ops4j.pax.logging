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
package org.ops4j.pax.logging.internal;

import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.Test;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.BundleRevision;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Chris Dolan
 * @since 6/10/11 10:38 AM
 */
public class JdkHandlerTest {

    @Test
    public void test() {
        final Bundle bundle = makeBundle();

        PaxLogger logger = mock(PaxLogger.class);

        PaxLoggingManager logManager = mock(PaxLoggingManager.class);
        when(logManager.getLogger(null, "java.util.logging.Logger")).thenReturn(logger);

        JdkHandler handler = new JdkHandler(logManager);
        try {
            handler.publish(mkRecord(Level.ALL, "all", null));
            handler.publish(mkRecord(Level.FINEST, "fff", null));
            handler.publish(mkRecord(Level.FINER, "ff", null));
            handler.publish(mkRecord(Level.FINE, "f", null));
            handler.publish(mkRecord(Level.CONFIG, "c", null));
            handler.publish(mkRecord(Level.INFO, "i", null));
            handler.publish(mkRecord(Level.WARNING, "w", null));
            handler.publish(mkRecord(Level.SEVERE, "s", null));
            handler.publish(mkRecord(Level.OFF, "off", null));
            handler.publish(mkRecord(Level.SEVERE, "s", new Throwable()));

            handler.flush(); // no-op
        } finally {
            handler.close();
        }

        verify(logger).trace("all", null);
        verify(logger).trace("fff", null);
        verify(logger).debug("ff", null);
        verify(logger).debug("f", null);
        verify(logger).inform("c", null);
        verify(logger).inform("i", null);
        verify(logger).warn("w", null);
        verify(logger).error("s", null);
        verify(logger).error("off", null);
        verify(logger).error(eq("s"), isA(Throwable.class));
    }

    private LogRecord mkRecord(Level lvl, String msg, Throwable t) {
        LogRecord record = new LogRecord(lvl, msg);
        if (t != null)
            record.setThrown(t);
        return record;
    }

    private Bundle makeBundle() {
        BundleRevision revision = mock(BundleRevision.class);
        Bundle bundle = mock(Bundle.class);

        when(bundle.getSymbolicName()).thenReturn("bundle1");
        when(bundle.getVersion()).thenReturn(Version.emptyVersion);
        when(bundle.getBundleId()).thenReturn(1L);
        when(bundle.getHeaders()).thenReturn(new Hashtable<>());
        when(bundle.adapt(BundleRevision.class)).thenReturn(revision);

        return bundle;
    }

}
