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
 * <p>While pax-logging-api bundle exports {@code org.apache.log4j} package (and makes some methods no-op),
 * pax-logging-service bundle <strong>has to</strong> Private-Package some of the classes from the same package.</p>
 *
 * <p>Normally classes from this package will be loaded using OSGi import from pax-logging-api bundle,
 * but when needed, some classes from original log4j:log4j JAR have to be shaded here (and not exported).</p>
 *
 * <p>This is especially important for {@code org.apache.log4j.Logger} class itself - pax-logging-api exports
 * this class to be used as API of Log4J1, but pax-logging-service has to use own (and unchanged - except when
 * fixing some performance/synchronization issues) copy from log4j:log4j. But also other classes from this package
 * have to be shaded. General rule is - if pax-logging-api changes some class from Log4J1 (usually to no-op some
 * methods), pax-logging-service should use own copy.</p>
 */
package org.apache.log4j;
