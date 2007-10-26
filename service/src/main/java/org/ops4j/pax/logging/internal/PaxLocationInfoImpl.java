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
package org.ops4j.pax.logging.internal;

import org.ops4j.pax.logging.spi.PaxLocationInfo;
import org.apache.log4j.spi.LocationInfo;

public class PaxLocationInfoImpl
    implements PaxLocationInfo
{
    private LocationInfo m_delegate;

    public PaxLocationInfoImpl( LocationInfo delegate )
    {
        m_delegate = delegate;
    }

    public String getFileName()
    {
        return m_delegate.getFileName();
    }

    public String getClassName()
    {
        return m_delegate.getClassName();
    }

    public String getLineNumber()
    {
        return m_delegate.getLineNumber();
    }

    public String getMethodName()
    {
        return m_delegate.getMethodName();
    }
}
