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
package org.ops4j.pax.logging.log4j.internal;

import org.apache.log4j.Level;
import org.ops4j.pax.logging.spi.PaxLevel;

public class PaxLevelImpl
    implements PaxLevel
{

    private Level m_delegate;

    public PaxLevelImpl( Level delegate )
    {
        m_delegate = delegate;
    }

    public boolean isGreaterOrEqual( PaxLevel r )
    {
        PaxLevelImpl impl = (PaxLevelImpl) r;
        return m_delegate.isGreaterOrEqual( impl.m_delegate );
    }

    public int toInt()
    {
        return m_delegate.toInt();
    }

    public int getSyslogEquivalent()
    {
        return m_delegate.getSyslogEquivalent();
    }

    public String toString()
    {
        return m_delegate.toString();
    }

}
