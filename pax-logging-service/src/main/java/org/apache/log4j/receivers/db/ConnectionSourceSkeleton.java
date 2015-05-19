/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.log4j.receivers.db;


import org.apache.log4j.component.spi.ComponentBase;
import org.apache.log4j.receivers.db.dialect.Util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;


/**
 * @author Ceki G&uuml;lc&uuml;
 */
public abstract class ConnectionSourceSkeleton extends ComponentBase implements ConnectionSource {
  
  private Boolean overriddenSupportsGetGeneratedKeys = null;
  
  private String user = null;
  private String password = null;

  // initially we have an unkonw dialect
  private int dialectCode = UNKNOWN_DIALECT;
  private boolean supportsGetGeneratedKeys = false;
  private boolean supportsBatchUpdates = false;


  /**
   * Learn relevant information about this connection source.
   *
   */
  public void discoverConnnectionProperties() {
    Connection connection = null;
    try {
      connection = getConnection();
      if (connection == null) {
        getLogger().warn("Could not get a conneciton");
        return;
      }
      DatabaseMetaData meta = connection.getMetaData();
      Util util = new Util();
      util.setLoggerRepository(repository);
      if (overriddenSupportsGetGeneratedKeys != null) {
        supportsGetGeneratedKeys = overriddenSupportsGetGeneratedKeys
            .booleanValue();
      } else {
        supportsGetGeneratedKeys = util.supportsGetGeneratedKeys(meta);
      }
      supportsBatchUpdates = util.supportsBatchUpdates(meta);
      dialectCode = Util.discoverSQLDialect(meta);
    } catch (SQLException se) {
      getLogger().warn("Could not discover the dialect to use.", se);
    } finally {
      DBHelper.closeConnection(connection);
    }
  }

  /**
   * Does this connection support the JDBC Connection.getGeneratedKeys method?
   */
  public final boolean supportsGetGeneratedKeys() {
    return supportsGetGeneratedKeys;
  }

  public final int getSQLDialectCode() {
    return dialectCode;
  }

  /**
   * Get the password for this connection source.
   */
  public final String getPassword() {
    return password;
  }

  /**
   * Sets the password.
   * @param password The password to set
   */
  public final void setPassword(final String password) {
    this.password = password;
  }

  /**
   * Get the user for this connection source.
   */
  public final String getUser() {
    return user;
  }

  /**
   * Sets the username.
   * @param username The username to set
   */
  public final void setUser(final String username) {
    this.user = username;
  }

  /**
   * Returns the "overridden" value of "supportsGetGeneratedKeys" property of
   * the JDBC driver. In certain cases, getting (e.g. Oracle 10g) generated keys
   * does not work because it returns the ROWID, not the value of the sequence.
   * 
   * @return A non null string, with "true" or "false" value, if overridden,
   *         <code>null</code> if not overridden.
   */
  public String getOverriddenSupportsGetGeneratedKeys() {
    return overriddenSupportsGetGeneratedKeys != null ? overriddenSupportsGetGeneratedKeys
        .toString()
        : null;
  }

  /**
   * Sets the "overridden" value of "supportsGetGeneratedKeys" property of the
   * JDBC driver. In certain cases, getting (e.g. Oracle 10g) generated keys
   * does not work because it returns the ROWID, not the value of the sequence.
   * 
   * @param overriddenSupportsGetGeneratedKeys
   *          A non null string, with "true" or "false" value, if overridden,
   *          <code>null</code> if not overridden.
   */
  public void setOverriddenSupportsGetGeneratedKeys(
      String overriddenSupportsGetGeneratedKeys) {
    this.overriddenSupportsGetGeneratedKeys = Boolean
        .valueOf(overriddenSupportsGetGeneratedKeys);
  }
  
  /**
   * Does this connection support batch updates?
   */
  public final boolean supportsBatchUpdates() {
    return supportsBatchUpdates;
  }
}
