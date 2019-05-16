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
package org.ops4j.pax.logging.logback.internal;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEventVO;
import ch.qos.logback.core.Context;
import org.apache.felix.framework.FilterImpl;
import org.junit.Test;
import org.mockito.stubbing.Answer;
import org.ops4j.pax.logging.logback.internal.bridges.PaxAppenderDelegate;
import org.ops4j.pax.logging.logback.internal.spi.PaxLoggingEventForLogback;
import org.ops4j.pax.logging.spi.PaxAppender;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Chris Dolan
 * @since 6/14/11 10:59 AM
 */
public class PaxAppenderDelegateTest {

    @Test
    public void test() throws InvalidSyntaxException {
        String filterStr = "(&(objectClass=org.ops4j.pax.logging.spi.PaxAppender)(org.ops4j.pax.logging.appender.name=foo-pax-name))";

        ILoggingEvent evt = new LoggingEventVO();

        PaxAppender appender = mock(PaxAppender.class);

        @SuppressWarnings("unchecked")
        ServiceReference<PaxAppender> sr = mock(ServiceReference.class);

        Filter filter = mock(Filter.class);

        BundleContext bundlecontext = mock(BundleContext.class);
        when(bundlecontext.createFilter(filterStr)).thenAnswer((Answer<Filter>) invocation ->
                new FilterImpl(invocation.getArgument(0)));
        when(bundlecontext.getProperty(Constants.FRAMEWORK_VERSION)).thenReturn("0"); // it seems that different OSGi versions call this differently
        when(bundlecontext.getServiceReferences((String) null, filterStr)).thenReturn(new ServiceReference[] { sr });

        when(bundlecontext.getService(same(sr))).thenReturn(appender);

        when(bundlecontext.ungetService(same(sr))).thenReturn(true);

        Context context = mock(Context.class);
        when(context.getObject(PaxLoggingServiceImpl.LOGGER_CONTEXT_BUNDLECONTEXT_KEY)).thenReturn(bundlecontext);

        PaxAppenderDelegate delegate = new PaxAppenderDelegate();
        delegate.setContext(context);
        delegate.setPaxname("foo-pax-name");
        delegate.start();
        delegate.start(); // second start is a no-op
        try {
            delegate.doAppend(evt);
        } finally {
            delegate.stop();
            delegate.stop(); // second stop is a no-op
        }

        verify(appender).doAppend(new PaxLoggingEventForLogback(evt));
        verify(bundlecontext).addServiceListener(any(), eq(filterStr));
        verify(bundlecontext).removeServiceListener(any());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPropertyPaxnameNull() {
        new PaxAppenderDelegate().setPaxname(null);
    }

}
