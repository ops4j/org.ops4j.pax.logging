/*
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
package org.ops4j.pax.logging.log4j1.internal;

import java.lang.reflect.Constructor;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.ops4j.pax.logging.PaxLogger;
import org.osgi.service.log.LogLevel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PaxLoggerImplTest {

    @Test
    public void numericalLog4J1Values() {
        assertTrue(Level.ERROR.isGreaterOrEqual(Level.INFO));
        assertTrue(Level.ERROR.isGreaterOrEqual(Level.ERROR));
        assertFalse(Level.WARN.isGreaterOrEqual(Level.ERROR));
    }

    @Test
    public void numericalPaxLoggingValues() {
        Logger l1 = Logger.getLogger("l1");
        l1.setLevel(Level.ERROR);
        Logger l2 = Logger.getLogger("l2");
        l2.setLevel(Level.WARN);
        Logger l3 = Logger.getLogger("l3");
        l3.setLevel(Level.INFO);
        PaxLoggerImpl error = new PaxLoggerImpl(null, l1, "", null, false);
        PaxLoggerImpl warn = new PaxLoggerImpl(null, l2, "", null, false);
        PaxLoggerImpl info = new PaxLoggerImpl(null, l3, "", null, false);

        assertEquals(error.getLogLevel(), LogLevel.ERROR);
        assertEquals(warn.getLogLevel(), LogLevel.WARN);
        assertEquals(info.getLogLevel(), LogLevel.INFO);
    }

    @Test
    public void testGetEffectiveLevel() throws Exception {
        Constructor<Logger> c = Logger.class.getDeclaredConstructor(String.class);
        c.setAccessible(true);

        Logger logger = c.newInstance("test");
        PaxLoggerImpl loggerImpl = new PaxLoggerImpl(null, logger, null, null, false);

        logger.setLevel(null);
        assertEquals(LogLevel.AUDIT, loggerImpl.getLogLevel());

        logger.setLevel(Level.ALL);
        assertEquals(LogLevel.TRACE, loggerImpl.getLogLevel());

        logger.setLevel(Level.TRACE);
        assertEquals(LogLevel.TRACE, loggerImpl.getLogLevel());

        logger.setLevel(Level.DEBUG);
        assertEquals(LogLevel.DEBUG, loggerImpl.getLogLevel());

        logger.setLevel(Level.INFO);
        assertEquals(LogLevel.INFO, loggerImpl.getLogLevel());

        logger.setLevel(Level.WARN);
        assertEquals(LogLevel.WARN, loggerImpl.getLogLevel());

        logger.setLevel(Level.ERROR);
        assertEquals(LogLevel.ERROR, loggerImpl.getLogLevel());

        logger.setLevel(Level.FATAL);
        assertEquals(LogLevel.ERROR, loggerImpl.getLogLevel());

        logger.setLevel(Level.OFF);
        assertEquals(LogLevel.ERROR, loggerImpl.getLogLevel());
    }

}
