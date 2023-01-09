/*
 * Copyright 2023 OPS4J.
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
package org.ops4j.pax.logging.slf4j;

import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

/**
 * In SLF4J 2 API, the goal of {@link SLF4JServiceProvider} is to prepare and return on demand 3 implementations:<ul>
 *     <li>{@link ILoggerFactory}</li>
 *     <li>{@link IMarkerFactory}</li>
 *     <li>{@link MDCAdapter}</li>
 * </ul>
 */
public class PaxLoggingSLF4JServiceProvider implements SLF4JServiceProvider {

    private ILoggerFactory loggerFactory;
    public static IMarkerFactory markerFactory = new BasicMarkerFactory();
    private MDCAdapter mdcAdapter;

    @Override
    public ILoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    @Override
    public IMarkerFactory getMarkerFactory() {
        return markerFactory;
    }

    @Override
    public MDCAdapter getMDCAdapter() {
        return mdcAdapter;
    }

    @Override
    public String getRequestedApiVersion() {
        return "2.0.100";
    }

    @Override
    public void initialize() {
        this.loggerFactory = new Slf4jLoggerFactory();
        this.mdcAdapter = new Slf4jMDCAdapter();
    }

}
