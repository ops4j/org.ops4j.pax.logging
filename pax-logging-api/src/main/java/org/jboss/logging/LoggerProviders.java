/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2011 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.logging;

/**
 * <p>Original {@code LoggerProviders} discovers a provider using {@link ClassLoader} and {@link java.util.ServiceLoader}
 * tricks. In pax-logging we simply use static provider.
 * PAXLOGGING-251 is about implementing actual delegating provider.</p>
 *
 * <p>Even if there's not much source to take from jboss-logging, pax-logging-api used source from
 * org.jboss.logging:jboss-logging:3.4.0.Final.</p>
 */
final class LoggerProviders {

    static final LoggerProvider PROVIDER = new Log4j2LoggerProvider();

    private LoggerProviders() {
    }

}
