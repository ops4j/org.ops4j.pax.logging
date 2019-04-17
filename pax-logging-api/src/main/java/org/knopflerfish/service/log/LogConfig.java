/*
 * Copyright (c) 2003-2013, KNOPFLERFISH project
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

import java.io.File;
import java.util.HashMap;

public interface LogConfig {

  /**
   * If CM is available, upload the current configuration to CM.
   */
  public void commit();

  /**
   * Returns <code>false</code> when the configuration is obtained
   * from CM.
   */
  public boolean isDefaultConfig();

  /**
   * Set number of log entries that are kept in memory.
   *
   * @param size
   *            the new maximum number of log entries in memory.
   */
  public void setMemorySize(int size);

  /**
   * Get the number of log entries that are kept in memory.
   *
   * @return number of log entries that are kept in memory.
   */
  public int getMemorySize();

  /**
   * Set the default filter level.
   *
   * @param filter
   *            the new default filter level.
   */
  public void setFilter(int filter);

  public int getFilter();

  /**
   * Set the filter level for bundles that matches the pattern.
   * The pattern may be one of:
   * <ul>
   *   <li>The location of the bundle.
   *   <li>The symbolic name of the bundle.
   *   <li>The name of the bundle.
   * </ul>
   *
   * @param bundleLocation A pattern to be matched against the bundle.
   * @param filter
   *            the new default filter level.
   */
  public void setFilter(String bundleLocation, int filter);

  public HashMap<String,Integer> getFilters();

  /**
   * Property controlling if log entries are written to
   * <code>System.out</code> or not.
   *
   * @param b
   *            if <code>true</code> log entries will be written to
   *            <code>System.out</code>.
   */
  public void setOut(boolean b);

  public boolean getOut();

  public void setFile(boolean f);

  public boolean getFile();

  /**
   * @return the directory that the file log is written to.
   */
  public File getDir();

  public void setFileSize(int fS);

  public int getFileSize();

  public void setMaxGen(int maxGen);

  public int getMaxGen();

  public void setFlush(boolean f);

  public boolean getFlush();

  /**
   * Define the format of the time-stamp used when presenting log
   * entries. I.e., in the file log and to <code>System.out</code>.
   *
   * @param pattern Date time pattern as defined in
   *                <code>java.text.SimpleDateFormat(java.lang.String)</code>.
   *                If the given pattern is invalid, the old pattern
   *                is keept.
   */
  public void setTimestampPattern(String pattern);

  /**
   * @return the current timestamp pattern.
   */
  public String getTimestampPattern();

}
