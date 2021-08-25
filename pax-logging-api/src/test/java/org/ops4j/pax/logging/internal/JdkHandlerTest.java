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
import org.ops4j.pax.logging.PaxLoggingConstants;
import org.ops4j.pax.logging.PaxLoggingManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.BundleRevision;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

/**
 * @author Chris Dolan
 * @since 6/10/11 10:38 AM
 */
public class JdkHandlerTest {

    private static final String NO_LOGGING_MODE = "no_logging";
    private static final String DEBUG_LOGGING_MODE = "debug_logging";
    private static final String NO_HEX_DUMPS_LOGGING_MODE = "no_hex_dumps_logging";

    @Test
    public void test() {
        final Bundle bundle = makeBundle();

        PaxLogger logger = mock(PaxLogger.class);

        PaxLoggingManager logManager = mock(PaxLoggingManager.class);
        when(logManager.getLogger(null, "java.util.logging.Logger")).thenReturn(logger);
        when(logManager.getLogger("javax.net.ssl", "java.util.logging.Logger")).thenReturn(logger);
        String parameter = "test";
        String hexDump = "  0000: 16 03 03 00 F1 01 00 00   ED 03 03 60 CB 54 00 5E  ...........`.T.^";

        // TLS log record parameters
        Object[] parameters = new Object[]{parameter, null, hexDump};
        String lineSeparator = System.lineSeparator();

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

            System.setProperty(PaxLoggingConstants.LOGGING_CFG_TLS_LOGGING_MODE, NO_LOGGING_MODE);
            handler.publish(mkTLSLogRecord(Level.FINE, "Skip TLS Debug log", null, null));

            System.setProperty(PaxLoggingConstants.LOGGING_CFG_TLS_LOGGING_MODE, "");
            handler.publish(mkTLSLogRecord(Level.FINE, "Skip TLS Debug log 1", null, null));

            System.clearProperty(PaxLoggingConstants.LOGGING_CFG_TLS_LOGGING_MODE);
            handler.publish(mkTLSLogRecord(Level.FINE, "Skip TLS Debug log 2", null, null));

            System.setProperty(PaxLoggingConstants.LOGGING_CFG_TLS_LOGGING_MODE, NO_HEX_DUMPS_LOGGING_MODE);
            handler.publish(mkTLSLogRecord(Level.FINE, "TLS Debug", null, parameters));

            System.setProperty(PaxLoggingConstants.LOGGING_CFG_TLS_LOGGING_MODE, DEBUG_LOGGING_MODE);
            handler.publish(mkTLSLogRecord(Level.FINE, "TLS Debug", null, parameters));
            handler.flush(); // no-op
        } finally {
            handler.close();
        }

        verify(logger).trace("all");
        verify(logger).trace("fff");
        verify(logger).trace("ff");
        verify(logger).debug("f");
        verify(logger).info("c");
        verify(logger).info("i");
        verify(logger).warn("w");
        verify(logger).error("s");
        verify(logger).error("off");
        verify(logger).error(eq("s"), isA(Throwable.class));
        verify(logger, never()).debug("Skip TLS Debug log");
        verify(logger, never()).debug("Skip TLS Debug log 1");
        verify(logger, never()).debug("Skip TLS Debug log 2");
        verify(logger).debug("TLS Debug" + lineSeparator + parameter);
        verify(logger).debug("TLS Debug" + lineSeparator + parameter + lineSeparator + hexDump);
    }

    private LogRecord mkRecord(Level lvl, String msg, Throwable t) {
        LogRecord record = new LogRecord(lvl, msg);
        if (t != null)
            record.setThrown(t);
        return record;
    }

    private LogRecord mkTLSLogRecord(Level lvl, String msg, Throwable t, Object[] params){
        LogRecord record = new LogRecord(lvl, msg);
        record.setLoggerName("javax.net.ssl");
        if (t != null)
            record.setThrown(t);

        if (params != null)
            record.setParameters(params);

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
