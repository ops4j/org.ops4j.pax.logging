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

import org.ops4j.pax.logging.spi.PaxLocationInfo;

public class PaxLocationInfoImpl implements PaxLocationInfo {

    private final StackTraceElement source;

    public PaxLocationInfoImpl(StackTraceElement source) {
        this.source = source;
    }

    @Override
    public String getFileName() {
        String s = source != null ? source.getFileName() : null;
        return s != null ? s : "?";
    }

    @Override
    public String getClassName() {
        String s = source != null ? source.getClassName() : null;
        return s != null ? s : "?";
    }

    @Override
    public String getLineNumber() {
        return source != null ? Integer.toString(source.getLineNumber()) : "?";
    }

    @Override
    public String getMethodName() {
        String s = source != null ? source.getMethodName() : null;
        return s != null ? s : "?";
    }
}
