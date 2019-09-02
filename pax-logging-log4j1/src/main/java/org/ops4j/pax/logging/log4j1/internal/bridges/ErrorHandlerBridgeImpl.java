/*  Copyright 2012 Guillaume Nodet.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.logging.log4j1.internal.bridges;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.OnlyOnceErrorHandler;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.LoggingEvent;
import org.ops4j.pax.logging.PaxLoggingConstants;
import org.ops4j.pax.logging.spi.PaxErrorHandler;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Bridge from Log4J to pax-logging. Event is filtered using {@link PaxErrorHandler} OSGi service.
 * Internal tracker is closed when pax-logging-log4j1 bundle stops.
 */
public class ErrorHandlerBridgeImpl implements ErrorHandler {

    private ServiceTracker<PaxErrorHandler, PaxErrorHandler> m_tracker;
    private ErrorHandler m_fallback;

    public ErrorHandlerBridgeImpl(BundleContext bundleContext, String name, ErrorHandler fallback) {
        m_tracker = new ServiceTracker<>(bundleContext, createFilter(bundleContext, name), null);
        m_tracker.open();
        m_fallback = fallback != null ? fallback : new OnlyOnceErrorHandler();
    }

    /**
     * Filter in the form of {@code (&(objectClass=org.ops4j.pax.logging.spi.PaxErrorHandler)(org.ops4j.pax.logging.errorhandler.name=NAME))},
     * where {@code NAME} comes from {@link org.apache.log4j.PaxLoggingConfigurator#OSGI_PREFIX osgi:} prefixed
     * references from logging configuration.
     * @param bundleContext
     * @param name
     * @return
     */
    public static org.osgi.framework.Filter createFilter(BundleContext bundleContext, String name) {
        try {
            return bundleContext.createFilter(
                    "(&(" + Constants.OBJECTCLASS + "=" + PaxErrorHandler.class.getName() + ")" +
                            "(" + PaxLoggingConstants.SERVICE_PROPERTY_ERRORHANDLER_NAME_PROPERTY + "=" + name + "))");
        } catch (InvalidSyntaxException e) {
            throw new IllegalStateException("unable to create error handler tracker", e);
        }
    }

    @Override
    public void error(String message) {
        PaxErrorHandler handler = m_tracker.getService();
        if (handler != null) {
            handler.error(message, null);
        } else {
            m_fallback.error(message);
        }
    }

    @Override
    public void error(String message, Exception e, int errorCode) {
        PaxErrorHandler handler = m_tracker.getService();
        if (handler != null) {
            handler.error(message, e);
        } else {
            m_fallback.error(message, e, errorCode);
        }
    }

    @Override
    public void error(String message, Exception e, int errorCode, LoggingEvent event) {
        PaxErrorHandler handler = m_tracker.getService();
        if (handler != null) {
            handler.error(message, e);
        } else {
            m_fallback.error(message, e, errorCode, event);
        }
    }

    @Override
    public void activateOptions() {
    }

    @Override
    public void setLogger(Logger logger) {
    }

    @Override
    public void setAppender(Appender appender) {
    }

    @Override
    public void setBackupAppender(Appender appender) {
    }

}
