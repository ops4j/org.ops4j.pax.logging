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
package org.ops4j.pax.logging.log4j;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import org.osgi.framework.Bundle;

/**
 * Custom ClassLoader that delegates functionality to an underlying Bundle instance.
 */
public class BundleDelegatingClassLoader extends ClassLoader {
    private final Bundle bundle;

    public BundleDelegatingClassLoader(final Bundle bundle) {
        if (bundle == null) {
            throw new NullPointerException("Bundle cannot be null");
        }
        this.bundle = bundle;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return bundle.loadClass(name);
    }

    @Override
    protected URL findResource(String name) {
        return bundle.getResource(name);
    }

    @Override
    protected Enumeration<URL> findResources(String name) throws IOException {
        return bundle.getResources(name);
    }
}
