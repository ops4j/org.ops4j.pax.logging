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
package org.ops4j.pax.logging.it;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.logging.OSGIPaxLoggingManager;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.spi.PaxDefaultLogStreamProvider;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.OptionUtils.combine;
import static org.ops4j.pax.logging.it.support.Helpers.delegate;
import static org.ops4j.pax.logging.it.support.Helpers.getField;

@RunWith(PaxExam.class)
public class DefaultLogToFileSingletonIntegrationTest extends AbstractControlledIntegrationTestBase {

    @Configuration
    public Option[] configure() throws IOException {
        // this configuration sets up default/fallback logger that writes to a file
        return combine(
                combine(baseConfigure(), defaultLoggingConfig()),
                mavenBundle("org.ops4j.pax.logging", "pax-logging-api").versionAsInProject()
        );
    }

    @Test
    public void defaultFileLogger() throws InvalidSyntaxException, ClassNotFoundException {
        Collection<ServiceReference<PaxDefaultLogStreamProvider>> refs
                = context.getServiceReferences(PaxDefaultLogStreamProvider.class, null);
        assertEquals("There should be singleton stream provider registered", 1, refs.size());

        // pax-logging-api creates own manager, but there are no restrictions to create another one
        OSGIPaxLoggingManager manager = new OSGIPaxLoggingManager(context);
        PaxLogger category1 = manager.getLogger(context.getBundle(), "category1", null);
        PaxLogger category1a = manager.getLogger(context.getBundle(), "category1", null);
        // different bundle, same category means different logger
        PaxLogger category1b = manager.getLogger(context.getBundle("link:classpath:META-INF/links/org.ops4j.pax.exam.invoker.junit.link"), "category1", null);
        PaxLogger category2 = manager.getLogger(context.getBundle(), "category2", null);

        assertSame(category1, category1a);
        assertNotSame(category1, category1b);
        assertNotSame(category1, category2);
        assertThat("delegate should be tracking logger",
                category1.getClass().getName(), equalTo("org.ops4j.pax.logging.internal.TrackingLogger"));

        Object d1 = delegate(category1);
        Object d1a = delegate(category1a);
        Object d1b = delegate(category1b);
        Object d2 = delegate(category2);

        assertSame(d1, d1a);
        assertNotSame(d1, d1b);
        assertNotSame(d1, d2);
        assertThat("delegate should be file based logger",
                d1.getClass().getName(), equalTo("org.ops4j.pax.logging.spi.support.FileServiceLog"));

        PrintStream ps1 = getField(d1, "out", PrintStream.class);
        PrintStream ps1a = getField(d1a, "out", PrintStream.class);
        PrintStream ps1b = getField(d1b, "out", PrintStream.class);
        PrintStream ps2 = getField(d2, "out", PrintStream.class);

        assertSame(ps1, ps1a);
        assertSame(ps1, ps1b);
        assertSame(ps1, ps2);
    }

    @Test
    public void singleStreamManyLoggers() throws NoSuchFieldException, IllegalAccessException, InvalidSyntaxException {
        Logger log1 = LoggerFactory.getLogger("l1");
        // we have no backend installed, so there should be default/fallback log underneath
        log1.info("We should log to fallback logger configured as file");

        Logger log2 = LoggerFactory.getLogger("l2");
        log2.info("We should log to fallback logger configured as file");

        List<String> lines = readLines();

        // verification of LogLog messages
        assertTrue(lines.contains("PaxExam-Probe [l1] INFO : We should log to fallback logger configured as file"));
        assertTrue(lines.contains("PaxExam-Probe [l2] INFO : We should log to fallback logger configured as file"));

        Logger log2a = LoggerFactory.getLogger("l2");

        assertNotSame("There should be two different SLF4JLoggers", log1, log2);
        assertNotSame("There should be two different SLF4JLoggers for the same category", log2, log2a);

        Object l1 = delegate(log1);
        Object l2 = delegate(log2);
        Object l2a = delegate(log2a);
        assertNotSame("There should be two different TrackingLoggers", l1, l2);
        assertSame("There should be one TrackingLogger for the same category", l2, l2a);

        Object d1 = delegate(l1);
        Object d2 = delegate(l2);
        Object d2a = delegate(l2a);
        assertNotSame("There should be two different FileServiceLog(ger)s", d1, d2);
        assertSame("There should be one FileServiceLog(ger) for the same category", d2, d2a);
    }

}
