/*
 * Copyright 2005-2020 Red Hat, Inc.
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
package org.ops4j.pax.logging.service.internal;

import java.util.Dictionary;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

/**
 * Separate {@link ManagedService} to monitor updates to {@code org.ops4j.pax.logging} PID if Configuration Admin
 * is available.
 */
public class LoggingManagedService implements ManagedService {

    private final PaxLoggingServiceImpl service;

    public LoggingManagedService(PaxLoggingServiceImpl service) {
        this.service = service;
    }

    @Override
    public void updated(Dictionary<String, ?> configuration) throws ConfigurationException {
        service.updated(configuration);
    }

}
