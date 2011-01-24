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
package org.ops4j.pax.logging.service.internal;

import java.util.Hashtable;

import junit.framework.TestCase;
import org.jmock.Mock;
import org.jmock.core.stub.ReturnStub;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogService;

public class FrameworkHandlerTest extends TestCase
{
    private LogEntry lastLogEntry;

    public void testDefaultLogLevelIsDebug() throws Exception
    {
        System.clearProperty( FrameworkHandler.FRAMEWORK_EVENTS_LOG_LEVEL_PROP_NAME );
        logAnEvent();
        assertEquals( LogService.LOG_DEBUG, lastLogEntry.getLevel() );
    }

    public void testLogLevelIsConfigurableViaSystemProperty() throws Exception
    {
        System.setProperty( FrameworkHandler.FRAMEWORK_EVENTS_LOG_LEVEL_PROP_NAME, "INFO" );
        try
        {
            logAnEvent();
            assertEquals( LogService.LOG_INFO, lastLogEntry.getLevel() );
        }
        finally
        {
            System.clearProperty( FrameworkHandler.FRAMEWORK_EVENTS_LOG_LEVEL_PROP_NAME );
        }
    }

    public void testInvalidConfiguredLogLevelCausesDebugLevelToBeUsed() throws Exception
    {
        System.setProperty( FrameworkHandler.FRAMEWORK_EVENTS_LOG_LEVEL_PROP_NAME, "GARBAGE" );
        try
        {
            logAnEvent();
            assertEquals( LogService.LOG_DEBUG, lastLogEntry.getLevel() );
        }
        finally
        {
            System.clearProperty( FrameworkHandler.FRAMEWORK_EVENTS_LOG_LEVEL_PROP_NAME );
        }
    }

    // Helper methods

    private void logAnEvent()
    {
        createHandler().bundleChanged( anEvent() );
    }

    private FrameworkHandler createHandler()
    {
        final LogReaderServiceImpl reader = new LogReaderServiceImpl(10);
        reader.addLogListener( new LogListener() {
            public void logged( final LogEntry entry )
            {
                lastLogEntry = entry;
            }
        });
        return new FrameworkHandler( new PaxLoggingServiceImpl( null, reader, null ) );
    }

    private BundleEvent anEvent()
    {
        return new BundleEvent( BundleEvent.INSTALLED, aBundle() );
    }

    private Bundle aBundle()
    {
        final Mock bundleMock = new Mock( Bundle.class );
        bundleMock.stubs().method( "getSymbolicName" ).will( new ReturnStub( "test-bundle" ) );
        bundleMock.stubs().method( "getBundleId" ).will( new ReturnStub( Long.valueOf(42) ) );
        bundleMock.stubs().method( "getHeaders" ).will( new ReturnStub( new Hashtable() ) );
        return (Bundle) bundleMock.proxy();
    }
}
