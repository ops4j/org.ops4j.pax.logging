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
package org.ops4j.pax.logging.log4j2.internal.spi;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.spi.StandardLevel;
import org.ops4j.pax.logging.spi.PaxLevel;
import org.osgi.service.log.LogLevel;

/**
 * Numerical Log4J2 levels come from {@link StandardLevel}
 */
public class PaxLevelImpl implements PaxLevel {

    // this data comes from the Log4J1 org.apache.log4j.Level class
    public static final int SYSLOG_ALL = 7;
    public static final int SYSLOG_TRACE = 7;
    public static final int SYSLOG_DEBUG = 7;
    public static final int SYSLOG_INFO = 6;
    public static final int SYSLOG_WARN = 4;
    public static final int SYSLOG_ERROR = 3;
    public static final int SYSLOG_FATAL = 0;
    public static final int SYSLOG_OFF = 0;

    private final Level level;

    public PaxLevelImpl(Level level) {
        this.level = level;
    }

    @Override
    public boolean isGreaterOrEqual(PaxLevel r) {
        return getSyslogEquivalent() <= r.getSyslogEquivalent();
    }

    @Override
    public LogLevel toLevel() {
        int lvl = level.intLevel();
        if (lvl <= Level.ERROR.intLevel()) {
            return LogLevel.ERROR;
        }
        if (lvl <= Level.WARN.intLevel()) {
            return LogLevel.WARN;
        }
        if (lvl <= Level.INFO.intLevel()) {
            return LogLevel.INFO;
        }
        if (lvl <= Level.DEBUG.intLevel()) {
            return LogLevel.DEBUG;
        }
        return LogLevel.TRACE;
    }

    @Override
    public int getSyslogEquivalent() {
        int lvl = level.intLevel();
        if (lvl <= Level.OFF.intLevel()) {
            return SYSLOG_OFF;
        }
        if (lvl <= Level.ERROR.intLevel()) {
            return SYSLOG_ERROR;
        }
        if (lvl <= Level.WARN.intLevel()) {
            return SYSLOG_WARN;
        }
        if (lvl <= Level.INFO.intLevel()) {
            return SYSLOG_INFO;
        }
        if (lvl <= Level.DEBUG.intLevel()) {
            return SYSLOG_DEBUG;
        }
        if (lvl <= Level.ALL.intLevel()) {
            return SYSLOG_TRACE;
        }
        return SYSLOG_DEBUG; // fallback case...
    }

    @Override
    public String toString() {
        return level.toString();
    }
}
