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

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;

import org.ops4j.pax.logging.PaxLogger;
import org.osgi.framework.Bundle;

/**
 * {@link DefaultServiceLog} that writes to configured file - useful for integration tests but also
 * for Karaf if you don't want to miss single log message.
 */
public class FileServiceLog extends DefaultServiceLog implements Closeable {

    private PrintStream out;

    /**
     * Constructs the logger - {@link PrintStream} passed should be a singleton.
     * @param logStream
     * @param bundle
     * @param categoryName
     */
    FileServiceLog(PrintStream logStream, Bundle bundle, String categoryName) {
        super(bundle, categoryName);
        out = logStream;
    }

    @Override
    protected void output(String levelName, String message, Throwable t) {
        // explicitly synchronize on a class loaded from pax-logging-api because FileServiceLog class
        // (and even FallbackLogFactory class) may be Private-Packaged in more bundles (i.e., more classloaders)
        synchronized (PaxLogger.class) {
            super.output(out, levelName, message, t);
            out.flush();
        }
    }

    @Override
    public void close() throws IOException {
        out.flush();
        out.close();
    }

}
