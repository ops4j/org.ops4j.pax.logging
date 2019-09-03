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
import org.osgi.service.log.LogLevel;

/**
 * @author cdolan
 * @since 5/2/12 1:03 PM
 */
public class PaxLevelImplTest {

    @Test
    public void test() {
        PaxLevelImpl all = new PaxLevelImpl(Level.ALL);
        PaxLevelImpl trace = new PaxLevelImpl(Level.TRACE);
        PaxLevelImpl debug = new PaxLevelImpl(Level.DEBUG);
        PaxLevelImpl info = new PaxLevelImpl(Level.INFO);
        PaxLevelImpl warn = new PaxLevelImpl(Level.WARN);
        PaxLevelImpl error = new PaxLevelImpl(Level.ERROR);
        PaxLevelImpl off = new PaxLevelImpl(Level.OFF);

        Assert.assertEquals(LogLevel.TRACE, all.toLevel());
        Assert.assertEquals(7, all.getSyslogEquivalent());
        Assert.assertFalse(all.isGreaterOrEqual(trace));
        Assert.assertTrue(all.isGreaterOrEqual(all));

        Assert.assertEquals(LogLevel.TRACE, trace.toLevel());
        Assert.assertEquals(7, trace.getSyslogEquivalent());
        Assert.assertTrue(trace.isGreaterOrEqual(all));
        Assert.assertTrue(trace.isGreaterOrEqual(trace));
        Assert.assertFalse(trace.isGreaterOrEqual(debug));

        Assert.assertEquals(LogLevel.DEBUG, debug.toLevel());
        Assert.assertEquals(7, debug.getSyslogEquivalent());
        Assert.assertTrue(debug.isGreaterOrEqual(trace));
        Assert.assertTrue(debug.isGreaterOrEqual(debug));
        Assert.assertFalse(debug.isGreaterOrEqual(info));

        Assert.assertEquals(LogLevel.INFO, info.toLevel());
        Assert.assertEquals(6, info.getSyslogEquivalent());
        Assert.assertTrue(info.isGreaterOrEqual(debug));
        Assert.assertTrue(info.isGreaterOrEqual(info));
        Assert.assertFalse(info.isGreaterOrEqual(warn));

        Assert.assertEquals(LogLevel.WARN, warn.toLevel());
        Assert.assertEquals(4, warn.getSyslogEquivalent());
        Assert.assertTrue(warn.isGreaterOrEqual(info));
        Assert.assertTrue(warn.isGreaterOrEqual(warn));
        Assert.assertFalse(warn.isGreaterOrEqual(error));

        Assert.assertEquals(LogLevel.ERROR, error.toLevel());
        Assert.assertEquals(3, error.getSyslogEquivalent());
        Assert.assertTrue(error.isGreaterOrEqual(warn));
        Assert.assertTrue(error.isGreaterOrEqual(error));
        Assert.assertFalse(error.isGreaterOrEqual(off));

        Assert.assertEquals(LogLevel.ERROR, off.toLevel());
        Assert.assertEquals(0, off.getSyslogEquivalent());
        Assert.assertTrue(off.isGreaterOrEqual(error));
        Assert.assertTrue(off.isGreaterOrEqual(off));
    }

}
