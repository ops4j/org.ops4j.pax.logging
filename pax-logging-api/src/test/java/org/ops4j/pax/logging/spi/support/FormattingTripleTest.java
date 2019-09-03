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

import java.util.MissingFormatArgumentException;

import org.junit.Test;
import org.osgi.framework.ServiceReference;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class FormattingTripleTest {

    @Test
    public void noArguments() {
        FormattingTriple ft = FormattingTriple.resolve("Hello!", false);
        assertThat(ft.getMessage(), equalTo("Hello!"));
        ft = FormattingTriple.resolve("Hello!", true);
        assertThat(ft.getMessage(), equalTo("Hello!"));
        ft = FormattingTriple.resolve("Hello!", true, new Throwable());
        assertThat(ft.getMessage(), equalTo("Hello!"));
        assertNotNull(ft.getThrowable());
        ft = FormattingTriple.resolve("Hello!", false, mock(ServiceReference.class));
        assertThat(ft.getMessage(), equalTo("Hello!"));
        assertNotNull(ft.getServiceReference());
        ft = FormattingTriple.resolve("Hello!", false, new Throwable(), mock(ServiceReference.class));
        assertThat(ft.getMessage(), equalTo("Hello!"));
        assertNotNull(ft.getThrowable());
        assertNotNull(ft.getServiceReference());
        ft = FormattingTriple.resolve("Hello!", false, mock(ServiceReference.class), new Throwable());
        assertThat(ft.getMessage(), equalTo("Hello!"));
        assertNotNull(ft.getThrowable());
        assertNotNull(ft.getServiceReference());
    }

    @Test
    public void oneFormattingArgument() {
        FormattingTriple ft = FormattingTriple.resolve("Hello {}", false, "world");
        assertThat(ft.getMessage(), equalTo("Hello world"));
        ft = FormattingTriple.resolve("Hello {}", true, "world");
        assertThat(ft.getMessage(), equalTo("Hello {}"));
        ft = FormattingTriple.resolve("Hello %s", true, "world");
        assertThat(ft.getMessage(), equalTo("Hello world"));
        ft = FormattingTriple.resolve("Hello %s", false, "world");
        assertThat(ft.getMessage(), equalTo("Hello %s"));
    }

    @Test
    public void oneNonFormattingArgument() {
        FormattingTriple ft = FormattingTriple.resolve("Hello {}", false, new Throwable());
        assertThat(ft.getMessage(), equalTo("Hello {}"));
        assertNotNull(ft.getThrowable());
        ft = FormattingTriple.resolve("Hello {}", true, new Throwable());
        assertThat(ft.getMessage(), equalTo("Hello {}"));
        assertNotNull(ft.getThrowable());
        ft = FormattingTriple.resolve("Hello %s", true, new Throwable());
        assertThat(ft.getMessage(), equalTo("Hello %s"));
        assertNotNull(ft.getThrowable());
        ft = FormattingTriple.resolve("Hello %s", false, new Throwable());
        assertThat(ft.getMessage(), equalTo("Hello %s"));
        assertNotNull(ft.getThrowable());
    }

    @Test
    public void oneDifferentNonFormattingArgument() {
        FormattingTriple ft = FormattingTriple.resolve("Hello {}", false, mock(ServiceReference.class));
        assertThat(ft.getMessage(), equalTo("Hello {}"));
        assertNotNull(ft.getServiceReference());
        ft = FormattingTriple.resolve("Hello {}", true, mock(ServiceReference.class));
        assertThat(ft.getMessage(), equalTo("Hello {}"));
        assertNotNull(ft.getServiceReference());
        ft = FormattingTriple.resolve("Hello %s", true, mock(ServiceReference.class));
        assertThat(ft.getMessage(), equalTo("Hello %s"));
        assertNotNull(ft.getServiceReference());
        ft = FormattingTriple.resolve("Hello %s", false, mock(ServiceReference.class));
        assertThat(ft.getMessage(), equalTo("Hello %s"));
        assertNotNull(ft.getServiceReference());
    }

    @Test
    public void twoNonFormattingArguments() {
        FormattingTriple ft = FormattingTriple.resolve("Hello {}", false, new Throwable(), mock(ServiceReference.class));
        assertThat(ft.getMessage(), equalTo("Hello {}"));
        assertNotNull(ft.getThrowable());
        assertNotNull(ft.getServiceReference());
        ft = FormattingTriple.resolve("Hello {}", true, new Throwable(), mock(ServiceReference.class));
        assertThat(ft.getMessage(), equalTo("Hello {}"));
        assertNotNull(ft.getThrowable());
        assertNotNull(ft.getServiceReference());
        try {
            FormattingTriple.resolve("Hello %s", true, new Throwable(), mock(ServiceReference.class));
            fail("Should throw MissingFormatArgumentException");
        } catch (MissingFormatArgumentException expected) {
        }
        ft = FormattingTriple.resolve("Hello %s", false, new Throwable(), mock(ServiceReference.class));
        assertThat(ft.getMessage(), equalTo("Hello %s"));
        assertNotNull(ft.getThrowable());
        assertNotNull(ft.getServiceReference());
    }

    @Test
    public void twoDifferentNonFormattingArguments() {
        FormattingTriple ft = FormattingTriple.resolve("Hello {}", false, mock(ServiceReference.class), new Throwable());
        assertThat(ft.getMessage(), equalTo("Hello {}"));
        assertNotNull(ft.getThrowable());
        assertNotNull(ft.getServiceReference());
        ft = FormattingTriple.resolve("Hello {}", true, mock(ServiceReference.class), new Throwable());
        assertThat(ft.getMessage(), equalTo("Hello {}"));
        assertNotNull(ft.getThrowable());
        assertNotNull(ft.getServiceReference());
        try {
            FormattingTriple.resolve("Hello %s", true, mock(ServiceReference.class), new Throwable());
            fail("Should throw MissingFormatArgumentException");
        } catch (MissingFormatArgumentException expected) {
        }
        ft = FormattingTriple.resolve("Hello %s", false, mock(ServiceReference.class), new Throwable());
        assertThat(ft.getMessage(), equalTo("Hello %s"));
        assertNotNull(ft.getThrowable());
        assertNotNull(ft.getServiceReference());
    }

    @Test
    public void twoSameNonFormattingArguments() {
        FormattingTriple ft = FormattingTriple.resolve("Hello {}", false, new MyThrowable(), new Throwable());
        assertThat(ft.getMessage(), equalTo("Hello my exception"));
        assertNotNull(ft.getThrowable());
        ft = FormattingTriple.resolve("Hello {}", true, new MyThrowable(), new Throwable());
        assertThat(ft.getMessage(), equalTo("Hello {}"));
        assertNotNull(ft.getThrowable());
        ft = FormattingTriple.resolve("Hello %s", true, new MyThrowable(), new Throwable());
        assertThat(ft.getMessage(), equalTo("Hello my exception"));
        assertNotNull(ft.getThrowable());
        ft = FormattingTriple.resolve("Hello %s", false, new MyThrowable(), new Throwable());
        assertThat(ft.getMessage(), equalTo("Hello %s"));
        assertNotNull(ft.getThrowable());
    }

    @Test
    public void twoFormattingArgumentsWithThrowableAsNonLastArgument() {
        FormattingTriple ft = FormattingTriple.resolve("Hello {} {}", false, new MyThrowable(), "arg");
        assertThat(ft.getMessage(), equalTo("Hello my exception arg"));
        assertNull(ft.getThrowable());
        ft = FormattingTriple.resolve("Hello {} {}", true, new MyThrowable(), "arg");
        assertThat(ft.getMessage(), equalTo("Hello {} {}"));
        assertNull(ft.getThrowable());
        ft = FormattingTriple.resolve("Hello %s %s", true, new MyThrowable(), "arg");
        assertThat(ft.getMessage(), equalTo("Hello my exception arg"));
        assertNull(ft.getThrowable());
        ft = FormattingTriple.resolve("Hello %s %s", false, new MyThrowable(), "arg");
        assertThat(ft.getMessage(), equalTo("Hello %s %s"));
        assertNull(ft.getThrowable());
    }

    @Test
    public void threeFormattingArguments() {
        FormattingTriple ft = FormattingTriple.resolve("Hello {} {}", false, "1", "2", "3");
        assertThat(ft.getMessage(), equalTo("Hello 1 2"));
        ft = FormattingTriple.resolve("Hello {} {} {}", false, "1", "2", "3");
        assertThat(ft.getMessage(), equalTo("Hello 1 2 3"));
        ft = FormattingTriple.resolve("Hello {} {} {} {}", false, "1", "2", "3");
        assertThat(ft.getMessage(), equalTo("Hello 1 2 3 {}"));

        ft = FormattingTriple.resolve("Hello {} {}", true, "1", "2", "3");
        assertThat(ft.getMessage(), equalTo("Hello {} {}"));
        ft = FormattingTriple.resolve("Hello {} {} {}", true, "1", "2", "3");
        assertThat(ft.getMessage(), equalTo("Hello {} {} {}"));
        ft = FormattingTriple.resolve("Hello {} {} {} {}", true, "1", "2", "3");
        assertThat(ft.getMessage(), equalTo("Hello {} {} {} {}"));

        ft = FormattingTriple.resolve("Hello %s %s", true, "1", "2", "3");
        assertThat(ft.getMessage(), equalTo("Hello 1 2"));
        ft = FormattingTriple.resolve("Hello %s %s %s", true, "1", "2", "3");
        assertThat(ft.getMessage(), equalTo("Hello 1 2 3"));
        try {
            ft = FormattingTriple.resolve("Hello %s %s %s %s", true, "1", "2", "3");
            fail("Should throw MissingFormatArgumentException");
        } catch (MissingFormatArgumentException expected) {
        }

        ft = FormattingTriple.resolve("Hello %s %s", false, "1", "2", "3");
        assertThat(ft.getMessage(), equalTo("Hello %s %s"));
        ft = FormattingTriple.resolve("Hello %s %s %s", false, "1", "2", "3");
        assertThat(ft.getMessage(), equalTo("Hello %s %s %s"));
        ft = FormattingTriple.resolve("Hello %s %s %s %s", false, "1", "2", "3");
        assertThat(ft.getMessage(), equalTo("Hello %s %s %s %s"));
    }

    private static class MyThrowable extends Exception {
        @Override
        public String toString() {
            return "my exception";
        }
    }

}
