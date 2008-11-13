/*  Copyright 2008 Edward Yakop.
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
package org.ops4j.pax.logging.it.appender;

import java.util.LinkedList;
import java.util.List;
import org.ops4j.pax.logging.spi.PaxAppender;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;

/**
 * @author edward.yakop@gmail.com
 */
final class CustomAppender
    implements PaxAppender
{

    private final List events;

    public CustomAppender()
    {
        events = new LinkedList();
    }

    public final List getEvents()
    {
        return events;
    }

    public void doAppend( PaxLoggingEvent event )
    {
        events.add( event );
    }

    public String toString()
    {
        return "Custom appender";
    }
}
