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
package org.apache.log4j;

import java.util.Properties;
import org.ops4j.pax.logging.internal.AppenderTracker;
import org.ops4j.pax.logging.internal.AppenderBridgeImpl;
import org.ops4j.pax.logging.spi.PaxAppender;

public class PaxLoggingConfigurator extends PropertyConfigurator
{
    public static final String OSGI_APPENDER_PREFIX = "osgi:";

    private AppenderTracker m_appenderTracker;

    public PaxLoggingConfigurator( AppenderTracker appenderTracker )
    {
        m_appenderTracker = appenderTracker;
    }

    Appender parseAppender( Properties props, String appenderName )
    {
        if (appenderName.startsWith( OSGI_APPENDER_PREFIX )) {
            appenderName = appenderName.substring(  OSGI_APPENDER_PREFIX.length() );
            PaxAppender appender = m_appenderTracker.getAppender( appenderName );
            return new AppenderBridgeImpl( appender );
        }
        else
        {
            return super.parseAppender( props, appenderName );
        }
    }
}
