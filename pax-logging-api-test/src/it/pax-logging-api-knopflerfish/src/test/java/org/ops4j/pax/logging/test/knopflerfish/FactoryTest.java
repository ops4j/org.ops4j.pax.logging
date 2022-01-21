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
package org.ops4j.pax.logging.test.knopflerfish;

import org.junit.Test;
import org.knopflerfish.service.log.LogRef;
import org.mockito.ArgumentCaptor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class FactoryTest {

    @Test
    @SuppressWarnings("unchecked")
    public void paxLoggingSpecificCommonsLoggingFactory() throws InvalidSyntaxException {
        BundleContext context = mock(BundleContext.class);
        Bundle bundle = mock(Bundle.class);
        ServiceReference<LogService> sr = mock(ServiceReference.class);
        LogService logService = mock(LogService.class);

        when(context.getBundle()).thenReturn(bundle);
        when(bundle.getBundleId()).thenReturn(42L);
        when(context.getServiceReference(LogService.class)).thenReturn(sr, (ServiceReference<LogService>)null);
        when(context.getService(sr)).thenReturn(logService);

        ArgumentCaptor<ServiceListener> argument = ArgumentCaptor.forClass(ServiceListener.class);

        ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);

        LogRef lr = new LogRef(context);
        verify(context).addServiceListener(argument.capture(), anyString());

        lr.info("INFO");
        verify(logService).log(any(), anyInt(), message.capture(), any());
        assertThat(message.getValue(), equalTo("INFO"));

        argument.getValue().serviceChanged(new ServiceEvent(ServiceEvent.UNREGISTERING, sr));

        clearInvocations(logService);
        lr.info("INFO");
        verifyNoMoreInteractions(logService);
    }

}
