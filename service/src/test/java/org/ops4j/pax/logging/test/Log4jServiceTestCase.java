/*
 * Copyright 2005 Makas Tzavellas.
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
package org.ops4j.pax.logging.test;

import org.apache.log4j.BasicConfigurator;
import org.jmock.MockObjectTestCase;
import org.ops4j.pax.logging.internal.PaxLoggingServiceImpl;
import org.ops4j.pax.logging.internal.LogReaderServiceImpl;
import org.osgi.service.log.LogService;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogEntry;
import java.util.Enumeration;

public class Log4jServiceTestCase extends MockObjectTestCase implements LogListener
{

    private boolean m_logged;

    public Log4jServiceTestCase( String name )
    {
        super( name );
    }

    public void testLogService() throws Exception
    {
        BasicConfigurator.configure();
        LogReaderServiceImpl logReader = new LogReaderServiceImpl();
        logReader.addLogListener( this );
        m_logged = false;
        PaxLoggingServiceImpl ls = new PaxLoggingServiceImpl( logReader, null );
        ls.log( LogService.LOG_DEBUG, "*******TESTING*********" );
        Exception exception = new Exception();
        ls.log( LogService.LOG_ERROR, "*******TESTING*********", exception );
        ls.log( LogService.LOG_INFO, "*******TESTING*********" );
        ls.log( LogService.LOG_WARNING, "*******TESTING*********" );
        ls.log( null, LogService.LOG_INFO, "*******TESTING*********" );
        assertTrue( m_logged );
        Enumeration history = logReader.getLog();
        assertTrue( history.hasMoreElements() );
        LogEntry entry = (LogEntry) history.nextElement();
        assertNull( entry.getBundle() );
        assertNull( entry.getException() );
        assertEquals( LogService.LOG_DEBUG, entry.getLevel() );
        assertEquals( "*******TESTING*********", entry.getMessage() );
        assertTrue( entry.getTime() <= System.currentTimeMillis() );
        assertTrue( entry.getTime() >= System.currentTimeMillis()-50 );
        assertTrue( history.hasMoreElements() );
        entry = (LogEntry) history.nextElement();
        assertNull( entry.getBundle() );
        assertEquals( exception, entry.getException() );
        assertEquals( LogService.LOG_ERROR, entry.getLevel() );
        assertEquals( "*******TESTING*********", entry.getMessage() );
        assertTrue( entry.getTime() <= System.currentTimeMillis() );
        assertTrue( entry.getTime() >= System.currentTimeMillis()-50 );
        assertTrue( history.hasMoreElements() );
        entry = (LogEntry) history.nextElement();
        assertNull( entry.getBundle() );
        assertNull( entry.getException() );
        assertEquals( LogService.LOG_INFO, entry.getLevel() );
        assertEquals( "*******TESTING*********", entry.getMessage() );
        assertTrue( entry.getTime() <= System.currentTimeMillis() );
        assertTrue( entry.getTime() >= System.currentTimeMillis()-50 );
        assertTrue( history.hasMoreElements() );
        entry = (LogEntry) history.nextElement();
        assertNull( entry.getBundle() );
        assertNull( entry.getException() );
        assertEquals( LogService.LOG_WARNING, entry.getLevel() );
        assertEquals( "*******TESTING*********", entry.getMessage() );
        assertTrue( entry.getTime() <= System.currentTimeMillis() );
        assertTrue( entry.getTime() >= System.currentTimeMillis()-50 );
        assertTrue( history.hasMoreElements() );
        entry = (LogEntry) history.nextElement();
        assertNull( entry.getBundle() );
        assertNull( entry.getException() );
        assertEquals( LogService.LOG_INFO, entry.getLevel() );
        assertEquals( "*******TESTING*********", entry.getMessage() );
        assertTrue( entry.getTime() <= System.currentTimeMillis() );
        assertTrue( entry.getTime() >= System.currentTimeMillis()-50 );
        assertFalse( history.hasMoreElements() );
    }

    public void logged( LogEntry logEntry )
    {
        m_logged = true;
    }
}
