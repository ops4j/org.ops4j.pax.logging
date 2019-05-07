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

import org.osgi.framework.ServiceRegistration;

/**
 * Class keeping both the service and it's {@link org.osgi.framework.ServiceRegistration}
 * @param <S> Interface of the registered service
 * @param <T> Actual type of the service
 */
public class RegisteredService<S, T> {

    private T service;
    private ServiceRegistration<S> registration;

    public RegisteredService(T service, ServiceRegistration<S> registration) {
        this.service = service;
        this.registration = registration;
    }

    public T getService() {
        return service;
    }

    public ServiceRegistration<S> getRegistration() {
        return registration;
    }

    public void close() throws Exception {
        if (registration != null) {
            registration.unregister();
        }
        if (service instanceof AutoCloseable) {
            ((AutoCloseable) service).close();
        }
    }

}
