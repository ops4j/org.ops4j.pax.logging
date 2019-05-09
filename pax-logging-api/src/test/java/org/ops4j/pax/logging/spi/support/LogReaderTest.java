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
package org.ops4j.pax.logging.spi.support;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;

import static org.junit.Assert.assertEquals;

public class LogReaderTest {

    @Test
    public void testAddRemoveListener() throws Exception {
        LogReaderServiceImpl underTest = new LogReaderServiceImpl(10, null);
        MyTestListener listener = new MyTestListener();
        underTest.addLogListener(listener);
        LogEntry entry = new LogEntryImpl(null, null, 2, "", null);
        underTest.fireEvent(entry);
        assertEquals(1, listener.entries.size());
        underTest.addLogListener(listener);
        underTest.fireEvent(entry);
        assertEquals(3, listener.entries.size());
        underTest.removeLogListener(listener);
        underTest.fireEvent(entry);
        assertEquals(4, listener.entries.size());
        underTest.removeLogListener(listener);
        underTest.fireEvent(entry);
        assertEquals(4, listener.entries.size());
    }

    private class MyTestListener implements LogListener {

        private List<LogEntry> entries;

        public MyTestListener() {
            entries = new LinkedList<>();
        }

        public void logged(LogEntry logEntry) {
            entries.add(logEntry);
        }
    }
}
