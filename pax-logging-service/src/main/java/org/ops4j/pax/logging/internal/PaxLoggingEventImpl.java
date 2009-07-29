/*
* Copyright 2006 Niclas Hedhman.
*
* Licensed  under the  Apache License,  Version 2.0  (the "License");
* you may not use  this file  except in  compliance with the License.
* You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed  under the  License is distributed on an "AS IS" BASIS,
* WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
* implied.
*
* See the License for the specific language governing permissions and
* limitations under the License.
*/

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ops4j.pax.logging.internal;

import org.apache.log4j.Level;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.ops4j.pax.logging.spi.PaxLevel;
import org.ops4j.pax.logging.spi.PaxLocationInfo;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;

// Contributors:   Nelson Minar <nelson@monkey.org>
//                 Wolf Siberski
//                 Anders Kristensen <akristensen@dynamicsoft.com>

/**
 * The internal representation of logging events. When an affirmative
 * decision is made to log then a <code>LoggingEvent</code> instance
 * is created. This instance is passed around to the different log4j
 * components.
 *
 * <p>This class is of concern to those wishing to extend log4j.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author James P. Cakalic
 * @author Niclas Hedhman
 * @since 0.8.2
 */
public class PaxLoggingEventImpl
    implements java.io.Serializable, PaxLoggingEvent
{

    private final LoggingEvent m_delegate;

    public PaxLoggingEventImpl( LoggingEvent event )
    {
        m_delegate = event;
    }

    /**
     * Set the location information for this logging event. The collected
     * information is cached for future use.
     */
    public PaxLocationInfo getLocationInformation()
    {
        LocationInfo info = m_delegate.getLocationInformation();
        return new PaxLocationInfoImpl( info );
    }

    /**
     * Return the level of this event. Use this form instead of directly
     * accessing the <code>level</code> field.
     */
    public PaxLevel getLevel()
    {
        Level level = m_delegate.getLevel();
        return new PaxLevelImpl( level );
    }

    /**
     * Return the name of the logger. Use this form instead of directly
     * accessing the <code>categoryName</code> field.
     */
    public String getLoggerName()
    {
        return m_delegate.getLoggerName();
    }

    /**
     * Return the message for this logging event.
     */
    public String getMessage()
    {
        return (String) m_delegate.getMessage();
    }

    public String getRenderedMessage()
    {
        return m_delegate.getRenderedMessage();
    }

    public String getThreadName()
    {
        return m_delegate.getThreadName();
    }

    /**
     * Return this event's throwable's string[] representaion.
     */
    public String[] getThrowableStrRep()
    {
        return m_delegate.getThrowableStrRep();
    }

    /**
     * Check for the existence of location information without creating it
     * (a byproduct of calling getLocationInformation).
     *
     * @return true if location information has been extracted.
     *
     * @since 1.2.15
     */
    public final boolean locationInformationExists()
    {
        return m_delegate.locationInformationExists();
    }

    /**
     * Getter for the event's time stamp. The time stamp is calculated starting
     * from 1970-01-01 GMT.
     *
     * @return timestamp
     *
     * @since 1.2.15
     */
    public final long getTimeStamp()
    {
        return m_delegate.getTimeStamp();
    }

    /**
     * Get the fully qualified name of the calling logger sub-class/wrapper.
     * Provided for compatibility with log4j 1.3
     *
     * @return fully qualified class name, may be null.
     *
     * @since 1.2.15
     */
    public String getFQNOfLoggerClass()
    {
        return m_delegate.getFQNOfLoggerClass();
    }
}
