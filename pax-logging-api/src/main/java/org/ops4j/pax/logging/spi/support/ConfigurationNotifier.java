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
package org.ops4j.pax.logging.spi.support;

/**
 * Interface for backends that want to inform about configuration changes
 * (usually after Configuration Admin update which happens over multiple,
 * hard to synchronize threads).
 */
public interface ConfigurationNotifier extends AutoCloseable {

    /**
     * Signals successful completion of configuration procedure
     */
    void configurationDone();

    /**
     * Signals problems related to configuration procedure
     * @param t
     */
    void configurationError(Throwable t);

}
