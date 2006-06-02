/*
 * Copyright (c) 2003, KNOPFLERFISH project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials
 *   provided with the distribution.
 *
 * - Neither the name of the KNOPFLERFISH project nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.knopflerfish.service.log;

import org.osgi.service.log.LogService;

/**
 * * Utility class for the LogService interface. * It exports methods that
 * translates between the numeric values of * the severity level constants and
 * human readable strings.
 */

public class LogUtil {
    /**
     * * Converts from a numeric log severity level to a string. * *
     * 
     * @param level
     *            is the log severity level.
     */
    static public String fromLevel(int level) {
        return fromLevel(level, 0);
    }

    /**
     * * Converts from a numeric log severity level to a left justified * string
     * of at least the given length. * *
     * 
     * @param level
     *            is the log severity level. *
     * @param length
     *            the minimum length of the resulting string.
     */
    static public String fromLevel(int level, int length) {
        StringBuffer sb = new StringBuffer(length > 7 ? length : 7);
        switch (level) {
        case LogService.LOG_INFO:
            sb.append("info");
            break;
        case LogService.LOG_DEBUG:
            sb.append("debug");
            break;
        case LogService.LOG_WARNING:
            sb.append("Warning");
            break;
        case LogService.LOG_ERROR:
            sb.append("ERROR");
            break;
        case 0:
            sb.append("DEFAULT");
            break;
        default:
            sb.append("[");
            sb.append(level);
            sb.append("]");
        }
        for (int i = sb.length(); i < length; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }

    /**
     * * Converts a string representing a log severity level to an int. * *
     * 
     * @param level
     *            The string to convert. *
     * @param def
     *            Default value to use if the string is not * recognized as a
     *            log level. *
     * @return the log level, or the default value if the string can * not be
     *         recognized.
     */
    static public int toLevel(String level, int def) {
        if (level.equalsIgnoreCase("INFO")) {
            return LogService.LOG_INFO;
        } else if (level.equalsIgnoreCase("DEBUG")) {
            return LogService.LOG_DEBUG;
        } else if (level.equalsIgnoreCase("WARNING")) {
            return LogService.LOG_WARNING;
        } else if (level.equalsIgnoreCase("ERROR")) {
            return LogService.LOG_ERROR;
        } else if (level.equalsIgnoreCase("DEFAULT")) {
            return 0;
        }
        return def;
    }

}
