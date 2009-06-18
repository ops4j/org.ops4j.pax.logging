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

/**
 * This LogService provides an extra method for querying the service about the
 * current log level. Log entries that are less severe than the current log
 * level will be discarded by the log.
 * 
 * The log levels have the following hierarchy:
 * <ol>
 * <li><code>LOG_ERROR</code></li>
 * <li><code>LOG_WARNING</code></li>
 * <li><code>LOG_INFO</code></li>
 * <li><code>LOG_DEBUG</code></li>
 * </ol>
 * 
 * @author Gatespace AB
 */
public interface LogService extends org.osgi.service.log.LogService {
    /**
     * * Get the current log level. The log will discard log entires * with a
     * level that is less severe than the current level. * * E.g. If the current
     * log level is LOG_WARNING then the log will * discard all log entries with
     * level LOG_INFO and LOG_DEBUG. I.e. * there is no need for a bundle to try
     * to send such log entries * to the log. The bundle may actually save a
     * number of CPU-cycles * by getting the log level and do nothing if the
     * intended log * entry is less severe than the current log level. * *
     * 
     * @return the lowest severity level that is accepted into the * log.
     */
    int getLogLevel();
}
