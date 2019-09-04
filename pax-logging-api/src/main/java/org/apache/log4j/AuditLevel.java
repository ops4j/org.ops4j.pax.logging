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
package org.apache.log4j;

import java.io.Serializable;

import org.osgi.service.log.LogLevel;

/**
 * An implementation of Log4J1's {@link Priority} to satisfy R7 {@link LogLevel#AUDIT} level. The confusing part is
 * that Log4J1 has {@link Priority#OFF_INT} of higher value than {@link Priority#FATAL_INT}.
 */
public class AuditLevel extends Priority implements Serializable {

    /**
     * <p>The {@code AUDIT} has the highest rank/priority and is intended to mean <em>log always</em>.</p>
     * <p>{@link Priority#isGreaterOrEqual(Priority)} returns {@code true} even when comparing with {@link Level#OFF}</p>
     */
    final static public Level AUDIT = new Level(Integer.MAX_VALUE, "AUDIT", 0);

}
