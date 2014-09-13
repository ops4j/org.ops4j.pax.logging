/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package org.ops4j.pax.logging.log4j;

import org.apache.logging.log4j.Level;
import org.ops4j.pax.logging.PaxLogger;
import org.osgi.service.log.LogService;

/**
 * Utility class to convert between Log4j 2 Levels and Log Service/Pax Logging level integer values.
 */
public final class LevelUtil {
    private LevelUtil() {}

    public static int convertToPaxLoggingLevel(final Level level) {
        if (level.isLessSpecificThan(Level.TRACE)) {
            return PaxLogger.LEVEL_TRACE;
        } else if (level.isLessSpecificThan(Level.DEBUG)) {
            return PaxLogger.LEVEL_DEBUG;
        } else if (level.isLessSpecificThan(Level.INFO)) {
            return PaxLogger.LEVEL_INFO;
        } else if (level.isLessSpecificThan(Level.WARN)) {
            return PaxLogger.LEVEL_WARNING;
        } else {
            return PaxLogger.LEVEL_ERROR;
        }
    }

    public static Level convertFromLogServiceLevel(final int level) {
        switch (level) {
            case LogService.LOG_DEBUG:
                return Level.DEBUG;
            case LogService.LOG_INFO:
                return Level.INFO;
            case LogService.LOG_WARNING:
                return Level.WARN;
            case LogService.LOG_ERROR:
                return Level.ERROR;
            default:
                return Level.ALL;
        }
    }
}
