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
package org.ops4j.pax.logging.spi;

public interface PaxLevel
{

    /**
     * Returns <code>true</code> if this level has a higher or equal
     * level than the level passed as argument, <code>false</code>
     * otherwise.
     *
     * <p>You should think twice before overriding the default
     * implementation of <code>isGreaterOrEqual</code> method.
     *
     * @param r the PaxLevel to compare with.
     *
     * @return true if this level has a higher or equal level than the level passed as argument, <code>false</code> otherwise.
     */
    boolean isGreaterOrEqual( PaxLevel r );

    /**
     * Returns the integer representation of this level.
     *
     * @return the integer representation of this level.
     */
    int toInt();

    /**
     * Return the syslog equivalent of this priority as an integer.
     *
     * @return the syslog equivalent of this priority as an integer.
     */
    int getSyslogEquivalent();

}
