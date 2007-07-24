/*  Copyright 2007 Niclas Hedhman.
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
package org.ops4j.pax.logging.internal;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogEntry;

public class LogEntryImpl
    implements LogEntry
{

    private long m_time;
    private Bundle m_bundle;
    private ServiceReference m_service;
    private int m_level;
    private String m_message;
    private Throwable m_exception;

    public LogEntryImpl( Bundle bundle, ServiceReference service, int level, String message, Throwable exception )
    {
        m_bundle = bundle;
        m_service = service;
        m_level = level;
        m_message = message;
        m_exception = exception;
        m_time = System.currentTimeMillis();
    }

    public Bundle getBundle()
    {
        return m_bundle;
    }

    public ServiceReference getServiceReference()
    {
        return m_service;
    }

    public int getLevel()
    {
        return m_level;
    }

    public String getMessage()
    {
        return m_message;
    }

    public Throwable getException()
    {
        return m_exception;
    }

    public long getTime()
    {
        return m_time;
    }
}
