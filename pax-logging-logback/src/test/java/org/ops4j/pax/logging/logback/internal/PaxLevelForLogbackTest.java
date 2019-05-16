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
package org.ops4j.pax.logging.logback.internal;

import ch.qos.logback.classic.Level;
import org.junit.Assert;
import org.junit.Test;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.logback.internal.spi.PaxLevelForLogback;

/**
 * @author cdolan
 * @since 5/2/12 1:03 PM
 */
public class PaxLevelForLogbackTest {
    @Test
    public void test() {
        PaxLevelForLogback all   = new PaxLevelForLogback(Level.ALL);
        PaxLevelForLogback trace = new PaxLevelForLogback(Level.TRACE);
        PaxLevelForLogback debug = new PaxLevelForLogback(Level.DEBUG);
        PaxLevelForLogback info  = new PaxLevelForLogback(Level.INFO);
        PaxLevelForLogback warn  = new PaxLevelForLogback(Level.WARN);
        PaxLevelForLogback error = new PaxLevelForLogback(Level.ERROR);
        PaxLevelForLogback off   = new PaxLevelForLogback(Level.OFF);

        Assert.assertEquals(PaxLogger.LEVEL_TRACE, all.toInt());
        Assert.assertEquals(org.apache.log4j.Level.ALL.getSyslogEquivalent(), all.getSyslogEquivalent());
        Assert.assertFalse(all.isGreaterOrEqual(trace));
        Assert.assertTrue(all.isGreaterOrEqual(all));

        Assert.assertEquals(PaxLogger.LEVEL_TRACE, trace.toInt());
        Assert.assertEquals(org.apache.log4j.Level.TRACE.getSyslogEquivalent(), trace.getSyslogEquivalent());
        Assert.assertTrue(trace.isGreaterOrEqual(all));
        Assert.assertTrue(trace.isGreaterOrEqual(trace));
        Assert.assertFalse(trace.isGreaterOrEqual(debug));

        Assert.assertEquals(PaxLogger.LEVEL_DEBUG, debug.toInt());
        Assert.assertEquals(org.apache.log4j.Level.DEBUG.getSyslogEquivalent(), debug.getSyslogEquivalent());
        Assert.assertTrue(debug.isGreaterOrEqual(trace));
        Assert.assertTrue(debug.isGreaterOrEqual(debug));
        Assert.assertFalse(debug.isGreaterOrEqual(info));

        Assert.assertEquals(PaxLogger.LEVEL_INFO, info.toInt());
        Assert.assertEquals(org.apache.log4j.Level.INFO.getSyslogEquivalent(), info.getSyslogEquivalent());
        Assert.assertTrue(info.isGreaterOrEqual(debug));
        Assert.assertTrue(info.isGreaterOrEqual(info));
        Assert.assertFalse(info.isGreaterOrEqual(warn));

        Assert.assertEquals(PaxLogger.LEVEL_WARNING, warn.toInt());
        Assert.assertEquals(org.apache.log4j.Level.WARN.getSyslogEquivalent(), warn.getSyslogEquivalent());
        Assert.assertTrue(warn.isGreaterOrEqual(info));
        Assert.assertTrue(warn.isGreaterOrEqual(warn));
        Assert.assertFalse(warn.isGreaterOrEqual(error));

        Assert.assertEquals(PaxLogger.LEVEL_ERROR, error.toInt());
        Assert.assertEquals(org.apache.log4j.Level.ERROR.getSyslogEquivalent(), error.getSyslogEquivalent());
        Assert.assertTrue(error.isGreaterOrEqual(warn));
        Assert.assertTrue(error.isGreaterOrEqual(error));
        Assert.assertFalse(error.isGreaterOrEqual(off));

        Assert.assertEquals(PaxLogger.LEVEL_ERROR, off.toInt());
        Assert.assertEquals(org.apache.log4j.Level.OFF.getSyslogEquivalent(), off.getSyslogEquivalent());
        Assert.assertTrue(off.isGreaterOrEqual(error));
        Assert.assertTrue(off.isGreaterOrEqual(off));
    }
}
