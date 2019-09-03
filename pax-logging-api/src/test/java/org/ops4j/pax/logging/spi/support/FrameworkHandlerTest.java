/*
 * Copyright 2008 Michael Pilquist.
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

import java.util.Hashtable;

import org.junit.Test;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingConstants;
import org.ops4j.pax.logging.PaxLoggingManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.service.log.LogEntry;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * This test shows mocked interaction between {@link org.ops4j.pax.logging.PaxLoggingService} and
 * {@link FrameworkHandler}. It's up to concrete {@link org.ops4j.pax.logging.PaxLoggingService} implementation
 * to actually pass the events to framework handler and this will be tested in each backend separately.
 */
public class FrameworkHandlerTest {

    private LogEntry lastLogEntry;
    private PaxLogger currentLogger;

    @Test
    public void testDefaultLogLevelIsDebug() throws Exception {
        System.clearProperty(PaxLoggingConstants.LOGGING_CFG_FRAMEWORK_EVENTS_LOG_LEVEL);
        logAnEvent();
        verify(currentLogger).debug(anyString());
    }

    @Test
    public void testLogLevelIsConfigurableViaSystemProperty() throws Exception {
        System.setProperty(PaxLoggingConstants.LOGGING_CFG_FRAMEWORK_EVENTS_LOG_LEVEL, "INFO");
        try {
            logAnEvent();
            verify(currentLogger).info(anyString());
        } finally {
            System.clearProperty(PaxLoggingConstants.LOGGING_CFG_FRAMEWORK_EVENTS_LOG_LEVEL);
        }
    }

    @Test
    public void testInvalidConfiguredLogLevelCausesDebugLevelToBeUsed() throws Exception {
        System.setProperty(PaxLoggingConstants.LOGGING_CFG_FRAMEWORK_EVENTS_LOG_LEVEL, "GARBAGE");
        try {
            logAnEvent();
            verify(currentLogger).debug(anyString());
        } finally {
            System.clearProperty(PaxLoggingConstants.LOGGING_CFG_FRAMEWORK_EVENTS_LOG_LEVEL);
        }
    }

    // Helper methods

    private void logAnEvent() {
        createHandler().bundleChanged(anEvent());
    }

    private FrameworkHandler createHandler() {
        final LogReaderServiceImpl reader = new LogReaderServiceImpl(10, null);
        reader.addLogListener(entry -> lastLogEntry = entry);

        PaxLoggingManager manager = mock(PaxLoggingManager.class);
        BundleContext context = mock(BundleContext.class);
        currentLogger = mock(PaxLogger.class);

        when(manager.getLogger(any(), any(), any())).thenReturn(currentLogger);
        return new FrameworkHandler(context, manager);
    }

    private BundleEvent anEvent() {
        return new BundleEvent(BundleEvent.INSTALLED, aBundle());
    }

    private Bundle aBundle() {
        BundleRevision revision = mock(BundleRevision.class);
        Bundle bundle = mock(Bundle.class);

        when(bundle.getSymbolicName()).thenReturn("test-bundle");
        when(bundle.getVersion()).thenReturn(Version.emptyVersion);
        when(bundle.getBundleId()).thenReturn(42L);
        when(bundle.getHeaders()).thenReturn(new Hashtable<>());
        when(bundle.adapt(BundleRevision.class)).thenReturn(revision);

        return bundle;
    }
}
