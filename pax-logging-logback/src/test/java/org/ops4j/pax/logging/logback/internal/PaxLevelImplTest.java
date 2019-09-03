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
import org.ops4j.pax.logging.logback.internal.spi.PaxLevelImpl;

/**
 * @author cdolan
 * @since 5/2/12 1:03 PM
 */
public class PaxLevelImplTest {
    @Test
    public void test() {
        PaxLevelImpl all   = new PaxLevelImpl(Level.ALL);
        PaxLevelImpl trace = new PaxLevelImpl(Level.TRACE);
        PaxLevelImpl debug = new PaxLevelImpl(Level.DEBUG);
        PaxLevelImpl info  = new PaxLevelImpl(Level.INFO);
        PaxLevelImpl warn  = new PaxLevelImpl(Level.WARN);
        PaxLevelImpl error = new PaxLevelImpl(Level.ERROR);
        PaxLevelImpl off   = new PaxLevelImpl(Level.OFF);

        Assert.assertEquals(PaxLogger.LEVEL_TRACE, all.toPaxLoggingLevel());
        Assert.assertEquals(org.apache.log4j.Level.ALL.getSyslogEquivalent(), all.getSyslogEquivalent());
        Assert.assertFalse(all.isGreaterOrEqual(trace));
        Assert.assertTrue(all.isGreaterOrEqual(all));

        Assert.assertEquals(PaxLogger.LEVEL_TRACE, trace.toPaxLoggingLevel());
        Assert.assertEquals(org.apache.log4j.Level.TRACE.getSyslogEquivalent(), trace.getSyslogEquivalent());
        Assert.assertTrue(trace.isGreaterOrEqual(all));
        Assert.assertTrue(trace.isGreaterOrEqual(trace));
        Assert.assertFalse(trace.isGreaterOrEqual(debug));

        Assert.assertEquals(PaxLogger.LEVEL_DEBUG, debug.toPaxLoggingLevel());
        Assert.assertEquals(org.apache.log4j.Level.DEBUG.getSyslogEquivalent(), debug.getSyslogEquivalent());
        Assert.assertTrue(debug.isGreaterOrEqual(trace));
        Assert.assertTrue(debug.isGreaterOrEqual(debug));
        Assert.assertFalse(debug.isGreaterOrEqual(info));

        Assert.assertEquals(PaxLogger.LEVEL_INFO, info.toPaxLoggingLevel());
        Assert.assertEquals(org.apache.log4j.Level.INFO.getSyslogEquivalent(), info.getSyslogEquivalent());
        Assert.assertTrue(info.isGreaterOrEqual(debug));
        Assert.assertTrue(info.isGreaterOrEqual(info));
        Assert.assertFalse(info.isGreaterOrEqual(warn));

        Assert.assertEquals(PaxLogger.LEVEL_WARNING, warn.toPaxLoggingLevel());
        Assert.assertEquals(org.apache.log4j.Level.WARN.getSyslogEquivalent(), warn.getSyslogEquivalent());
        Assert.assertTrue(warn.isGreaterOrEqual(info));
        Assert.assertTrue(warn.isGreaterOrEqual(warn));
        Assert.assertFalse(warn.isGreaterOrEqual(error));

        Assert.assertEquals(PaxLogger.LEVEL_ERROR, error.toPaxLoggingLevel());
        Assert.assertEquals(org.apache.log4j.Level.ERROR.getSyslogEquivalent(), error.getSyslogEquivalent());
        Assert.assertTrue(error.isGreaterOrEqual(warn));
        Assert.assertTrue(error.isGreaterOrEqual(error));
        Assert.assertFalse(error.isGreaterOrEqual(off));

        Assert.assertEquals(PaxLogger.LEVEL_ERROR, off.toPaxLoggingLevel());
        Assert.assertEquals(org.apache.log4j.Level.OFF.getSyslogEquivalent(), off.getSyslogEquivalent());
        Assert.assertTrue(off.isGreaterOrEqual(error));
        Assert.assertTrue(off.isGreaterOrEqual(off));
    }
}
