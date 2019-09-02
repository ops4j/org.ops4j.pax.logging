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
/**
 * <p>pax-logging-log4j1 (Log4J1) may be extended in two ways:<ol>
 *     <li>using fragment bundle, so we have access to entire Log4J1</li>
 *     <li>using <em>whiteboard</em> approach, where we register services implementing interfaces
 *     from {@code org.ops4j.pax.logging.spi} package.</li>
 * </ol></p>
 *
 * <p>When using <em>whiteboard</em> approach, we can provide these 4 extensions:<ul>
 *     <li>appenders - by registering {@link org.ops4j.pax.logging.spi.PaxAppender} service</li>
 *     <li>filters - by registering {@link org.ops4j.pax.logging.spi.PaxFilter} service</li>
 *     <li>layouts - by registering {@link org.ops4j.pax.logging.spi.PaxLayout} service</li>
 *     <li>error handlers - by registering {@link org.ops4j.pax.logging.spi.PaxErrorHandler} service</li>
 * </ul></p>
 *
 * <p>In configuration (for PID {@code org.ops4j.pax.logging}) when encountering appender/filter/layout/errorHandler
 * prefixed with {@link org.apache.log4j.PaxLoggingConfigurator#OSGI_PREFIX osgi:}, a <em>bridge</em> is created
 * and passed to Log4J1. The responsibility of this bridge is to:<ul>
 *     <li>be called by Log4j1</li>
 *     <li>in case of filter/layout/errorHandler, pass the call to single tracked service <strong>or</strong>
 *     fallback filter/layout/errorHandler</li> when the service is not found. filter/layout/errorHandler bridges
 *     themselves use {@link org.osgi.util.tracker.ServiceTracker service trackers}
 *     <li>in case of appender, pass the call to all tracked services without fallback appender(s) when OSGi
 *     appenders are not available. Appender bridge doesn't use a
 *     {@link org.osgi.util.tracker.ServiceTracker service tracker}. Instead, appender bridge delegates to
 *     {@link org.ops4j.pax.logging.spi.support.PaxAppenderProxy} which is
 *     {@link org.osgi.util.tracker.ServiceTracker service tracker}.</li>
 * </ul></p>
 */
package org.ops4j.pax.logging.log4j1.internal.bridges;
