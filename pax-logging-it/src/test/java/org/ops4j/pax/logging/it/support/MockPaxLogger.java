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
import org.ops4j.pax.logging.PaxMarker;
import org.osgi.service.log.LogLevel;
import org.osgi.service.log.LoggerConsumer;

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
    public boolean isTraceEnabled(PaxMarker marker) {
        return true;
    }

    @Override
    public boolean isDebugEnabled(PaxMarker marker) {
        return true;
    }

    @Override
    public boolean isInfoEnabled(PaxMarker marker) {
        return true;
    }

    @Override
    public boolean isWarnEnabled(PaxMarker marker) {
        return true;
    }

    @Override
    public boolean isErrorEnabled(PaxMarker marker) {
        return true;
    }

    @Override
    public boolean isFatalEnabled(PaxMarker marker) {
        return true;
    }

    @Override
    public void trace(String message) {

    }

    @Override
    public void trace(String format, Object arg) {

    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {

    }

    @Override
    public void trace(String format, Object... arguments) {

    }

    @Override
    public <E extends Exception> void trace(LoggerConsumer<E> consumer) throws E {

    }

    @Override
    public void debug(String message) {

    }

    @Override
    public void debug(String format, Object arg) {

    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {

    }

    @Override
    public void debug(String format, Object... arguments) {

    }

    @Override
    public <E extends Exception> void debug(LoggerConsumer<E> consumer) throws E {

    }

    @Override
    public void info(String message) {
        messages.add(message);
    }

    @Override
    public void info(String format, Object arg) {

    }

    @Override
    public void info(String format, Object arg1, Object arg2) {

    }

    @Override
    public void info(String format, Object... arguments) {

    }

    @Override
    public <E extends Exception> void info(LoggerConsumer<E> consumer) throws E {

    }

    @Override
    public void warn(String message) {

    }

    @Override
    public void warn(String format, Object arg) {

    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {

    }

    @Override
    public void warn(String format, Object... arguments) {

    }

    @Override
    public <E extends Exception> void warn(LoggerConsumer<E> consumer) throws E {

    }

    @Override
    public void error(String message) {

    }

    @Override
    public void error(String format, Object arg) {

    }

    @Override
    public void error(String format, Object arg1, Object arg2) {

    }

    @Override
    public void error(String format, Object... arguments) {

    }

    @Override
    public <E extends Exception> void error(LoggerConsumer<E> consumer) throws E {

    }

    @Override
    public void audit(String message) {

    }

    @Override
    public void audit(String format, Object arg) {

    }

    @Override
    public void audit(String format, Object arg1, Object arg2) {

    }

    @Override
    public void audit(String format, Object... arguments) {

    }

    @Override
    public void trace(PaxMarker marker, String message) {

    }

    @Override
    public void trace(PaxMarker marker, String format, Object arg) {

    }

    @Override
    public void trace(PaxMarker marker, String format, Object arg1, Object arg2) {

    }

    @Override
    public void trace(PaxMarker marker, String format, Object... arguments) {

    }

    @Override
    public <E extends Exception> void trace(PaxMarker marker, LoggerConsumer<E> consumer) throws E {

    }

    @Override
    public void debug(PaxMarker marker, String message) {

    }

    @Override
    public void debug(PaxMarker marker, String format, Object arg) {

    }

    @Override
    public void debug(PaxMarker marker, String format, Object arg1, Object arg2) {

    }

    @Override
    public void debug(PaxMarker marker, String format, Object... arguments) {

    }

    @Override
    public <E extends Exception> void debug(PaxMarker marker, LoggerConsumer<E> consumer) throws E {

    }

    @Override
    public void info(PaxMarker marker, String message) {

    }

    @Override
    public void info(PaxMarker marker, String format, Object arg) {

    }

    @Override
    public void info(PaxMarker marker, String format, Object arg1, Object arg2) {

    }

    @Override
    public void info(PaxMarker marker, String format, Object... arguments) {

    }

    @Override
    public <E extends Exception> void info(PaxMarker marker, LoggerConsumer<E> consumer) throws E {

    }

    @Override
    public void warn(PaxMarker marker, String message) {

    }

    @Override
    public void warn(PaxMarker marker, String format, Object arg) {

    }

    @Override
    public void warn(PaxMarker marker, String format, Object arg1, Object arg2) {

    }

    @Override
    public void warn(PaxMarker marker, String format, Object... arguments) {

    }

    @Override
    public <E extends Exception> void warn(PaxMarker marker, LoggerConsumer<E> consumer) throws E {

    }

    @Override
    public void error(PaxMarker marker, String message) {

    }

    @Override
    public void error(PaxMarker marker, String format, Object arg) {

    }

    @Override
    public void error(PaxMarker marker, String format, Object arg1, Object arg2) {

    }

    @Override
    public void error(PaxMarker marker, String format, Object... arguments) {

    }

    @Override
    public <E extends Exception> void error(PaxMarker marker, LoggerConsumer<E> consumer) throws E {

    }

    @Override
    public void audit(PaxMarker marker, String message) {

    }

    @Override
    public void audit(PaxMarker marker, String format, Object arg) {

    }

    @Override
    public void audit(PaxMarker marker, String format, Object arg1, Object arg2) {

    }

    @Override
    public void audit(PaxMarker marker, String format, Object... arguments) {

    }

    @Override
    public <E extends Exception> void audit(LoggerConsumer<E> consumer) throws E {

    }

    @Override
    public <E extends Exception> void audit(PaxMarker marker, LoggerConsumer<E> consumer) throws E {

    }

    @Override
    public void fatal(String message) {

    }

    @Override
    public void fatal(String format, Object arg) {

    }

    @Override
    public void fatal(String format, Object arg1, Object arg2) {

    }

    @Override
    public void fatal(String format, Object... arguments) {

    }

    @Override
    public <E extends Exception> void fatal(LoggerConsumer<E> consumer) throws E {

    }

    @Override
    public void fatal(PaxMarker marker, String message) {

    }

    @Override
    public void fatal(PaxMarker marker, String format, Object arg) {

    }

    @Override
    public void fatal(PaxMarker marker, String format, Object arg1, Object arg2) {

    }

    @Override
    public void fatal(PaxMarker marker, String format, Object... arguments) {

    }

    @Override
    public <E extends Exception> void fatal(PaxMarker marker, LoggerConsumer<E> consumer) throws E {

    }

    @Override
    public void fqtrace(String fqcn, String message) {

    }

    @Override
    public void fqdebug(String fqcn, String message) {

    }

    @Override
    public void fqinfo(String fqcn, String message) {

    }

    @Override
    public void fqwarn(String fqcn, String message) {

    }

    @Override
    public void fqerror(String fqcn, String message) {

    }

    @Override
    public void fqfatal(String fqcn, String message) {

    }

    @Override
    public void fqtrace(String fqcn, PaxMarker marker, String message) {

    }

    @Override
    public void fqdebug(String fqcn, PaxMarker marker, String message) {

    }

    @Override
    public void fqinfo(String fqcn, PaxMarker marker, String message) {

    }

    @Override
    public void fqwarn(String fqcn, PaxMarker marker, String message) {

    }

    @Override
    public void fqerror(String fqcn, PaxMarker marker, String message) {

    }

    @Override
    public void fqfatal(String fqcn, PaxMarker marker, String message) {

    }

    @Override
    public void fqtrace(String fqcn, String message, Throwable t) {

    }

    @Override
    public void fqdebug(String fqcn, String message, Throwable t) {

    }

    @Override
    public void fqinfo(String fqcn, String message, Throwable t) {

    }

    @Override
    public void fqwarn(String fqcn, String message, Throwable t) {

    }

    @Override
    public void fqerror(String fqcn, String message, Throwable t) {

    }

    @Override
    public void fqfatal(String fqcn, String message, Throwable t) {

    }

    @Override
    public void fqtrace(String fqcn, PaxMarker marker, String message, Throwable t) {

    }

    @Override
    public void fqdebug(String fqcn, PaxMarker marker, String message, Throwable t) {

    }

    @Override
    public void fqinfo(String fqcn, PaxMarker marker, String message, Throwable t) {

    }

    @Override
    public void fqwarn(String fqcn, PaxMarker marker, String message, Throwable t) {

    }

    @Override
    public void fqerror(String fqcn, PaxMarker marker, String message, Throwable t) {

    }

    @Override
    public void fqfatal(String fqcn, PaxMarker marker, String message, Throwable t) {

    }

    @Override
    public int getPaxLogLevel() {
        return 0;
    }

    @Override
    public LogLevel getLogLevel() {
        return LogLevel.ERROR;
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
