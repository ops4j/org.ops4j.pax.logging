/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */

package org.apache.logging.log4j.util;

/**
 * <em>Consider this class private.</em>
 * Classes implementing this interface know how to supply a value.
 *
 * <p>This is a <a href="https://docs.oracle.com/javase/8/docs/api/java/util/function/package-summary.html">functional
 * interface</a> intended to support lambda expressions in log4j 2.
 *
 * <p>Implementors are free to cache values or return a new or distinct value each time the supplier is invoked.
 *
 * @param <T> the type of values returned by this supplier
 *
 * @since 2.4
 */
public interface Supplier<T> {

    /**
     * Gets a value.
     *
     * @return a value
     */
    T get();
}
