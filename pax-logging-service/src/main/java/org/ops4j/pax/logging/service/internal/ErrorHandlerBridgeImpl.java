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
package org.ops4j.pax.logging.service.internal;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.OnlyOnceErrorHandler;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.LoggingEvent;
import org.ops4j.pax.logging.PaxLoggingService;
import org.ops4j.pax.logging.spi.PaxErrorHandler;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;

public class ErrorHandlerBridgeImpl implements ErrorHandler
{

    private ServiceTracker m_tracker;
    private ErrorHandler m_fallback;

    public ErrorHandlerBridgeImpl( BundleContext bundleContext, String name, ErrorHandler fallback )
    {
        m_tracker = new ServiceTracker( bundleContext, createFilter(bundleContext, name), null );
        m_tracker.open();
        m_fallback = fallback != null ? fallback : new OnlyOnceErrorHandler();
    }

    public static org.osgi.framework.Filter createFilter( BundleContext bundleContext, String name )
    {
        try
        {
            return bundleContext.createFilter(
                    "(&(" + Constants.OBJECTCLASS + "=" + PaxErrorHandler.class.getName() + ")" +
                            "(" + PaxLoggingService.ERRORHANDLER_NAME_PROPERTY + "=" + name + "))");
        }
        catch (InvalidSyntaxException e)
        {
            throw new IllegalStateException("unable to create layout tracker", e);
        }
    }

    public void error(String message)
    {
        PaxErrorHandler handler = (PaxErrorHandler) m_tracker.getService();
        if (handler != null)
        {
            handler.error(message, null);
        }
        else
        {
            m_fallback.error(message);
        }
    }

    public void error(String message, Exception e, int errorCode)
    {
        PaxErrorHandler handler = (PaxErrorHandler) m_tracker.getService();
        if (handler != null)
        {
            handler.error(message, e);
        }
        else
        {
            m_fallback.error(message, e, errorCode);
        }
    }

    public void error(String message, Exception e, int errorCode, LoggingEvent event)
    {
        PaxErrorHandler handler = (PaxErrorHandler) m_tracker.getService();
        if (handler != null)
        {
            handler.error(message, e);
        }
        else
        {
            m_fallback.error(message, e, errorCode, event);
        }
    }

    public void activateOptions()
    {
    }

    public void setLogger(Logger logger)
    {
    }

    public void setAppender(Appender appender)
    {
    }

    public void setBackupAppender(Appender appender)
    {
    }

}
