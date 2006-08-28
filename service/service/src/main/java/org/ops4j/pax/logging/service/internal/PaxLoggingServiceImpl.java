/*
 * Copyright 2005 Niclas Hedhman.
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
package org.ops4j.pax.logging.service.internal;

import org.ops4j.pax.logging.PaxLoggingService;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxAppender;

public class PaxLoggingServiceImpl
    implements PaxLoggingService
{

    public PaxLogger getLogger( String category )
    {
        return new PaxLoggerImpl( org.apache.log4j.Logger.getLogger( category ) );
    }

    public void addAppender( PaxAppender appender )
    {
        throw new UnsupportedOperationException( "Not Implemented Yet.");
    }

    public void removeAppender( PaxAppender appender )
    {
        throw new UnsupportedOperationException( "Not Implemented Yet.");
    }
}
