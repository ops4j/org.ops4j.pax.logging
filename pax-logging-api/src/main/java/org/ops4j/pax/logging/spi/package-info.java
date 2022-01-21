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
 * This package abstracts common concepts related to configuration of popular logging
 * frameworks/libraries.
 *
 * The most important concept is <em>appender</em> which is a way to store logging <em>events</em>.
 * Usually file appenders are used, but any other processing may be involved.
 *
 * Users who want to extend the configuration of chosen backend (Log4J2, Logback) don't have to
 * deal with framework-specific classes/interfaces, but may use these pax-logging interfaces.
 * Implementations of these interfaces will work with all pax-logging supported backends.
 */
package org.ops4j.pax.logging.spi;
