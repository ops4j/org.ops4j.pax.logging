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
package org.ops4j.pax.logging.it.support;

import java.util.LinkedList;
import java.util.List;

import org.ops4j.pax.logging.PaxContext;
import org.ops4j.pax.logging.PaxLogger;

public class MockPaxLogger implements PaxLogger {

    private List<String> messages = new LinkedList<>();

    public List<String> getMessages() {
        return messages;
    }

    @Override
    public boolean isTraceEnabled() {
        return true;
    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public boolean isFatalEnabled() {
        return true;
    }

    @Override
    public void trace(String message, Throwable t) {

    }

    @Override
    public void debug(String message, Throwable t) {

    }

    @Override
    public void inform(String message, Throwable t) {
        messages.add(message);
    }

    @Override
    public void warn(String message, Throwable t) {

    }

    @Override
    public void error(String message, Throwable t) {

    }

    @Override
    public void fatal(String message, Throwable t) {

    }

    @Override
    public void trace(String message, Throwable t, String fqcn) {

    }

    @Override
    public void debug(String message, Throwable t, String fqcn) {

    }

    @Override
    public void inform(String message, Throwable t, String fqcn) {

    }

    @Override
    public void warn(String message, Throwable t, String fqcn) {

    }

    @Override
    public void error(String message, Throwable t, String fqcn) {

    }

    @Override
    public void fatal(String message, Throwable t, String fqcn) {

    }

    @Override
    public int getLogLevel() {
        return 0;
    }

    @Override
    public String getName() {
        return "mock";
    }

    @Override
    public PaxContext getPaxContext() {
        return new PaxContext();
    }

}
