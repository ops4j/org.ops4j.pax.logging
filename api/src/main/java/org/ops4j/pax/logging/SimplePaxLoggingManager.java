/*
 * Copyright 2006 Edward Yakop.
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
package org.ops4j.pax.logging;

import java.util.HashMap;
import java.util.Map;
import org.ops4j.pax.logging.internal.DefaultServiceLog;

public class SimplePaxLoggingManager
    implements PaxLoggingManager
{
    private final Map<String, PaxLogger> m_category_Logger;

    public SimplePaxLoggingManager()
    {
        m_category_Logger = new HashMap<String, PaxLogger>();
    }

    public PaxLogger getLogger( String category )
    {
        PaxLogger paxLogger = m_category_Logger.get( category );

        if( paxLogger == null )
        {
            paxLogger = new DefaultServiceLog( category );
            m_category_Logger.put( category, paxLogger );
        }

        return paxLogger;
    }

    public PaxLoggingService getPaxLoggingService()
    {
        return null;
    }

    public void open()
    {
        // Do nothing
    }

    public void close()
    {
        // Do Nothing
    }

    public void dispose()
    {
        // Do Nothing
    }
}
