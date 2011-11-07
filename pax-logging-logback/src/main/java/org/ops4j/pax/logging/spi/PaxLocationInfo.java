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

public interface PaxLocationInfo
{

    /**
     * Return the file name of the caller.
     * <p>This information is not always available.
     *
     * @return the file name of the caller.
     */
    String getFileName();

    /**
     * Return the fully qualified class name of the caller making the
     * logging request.
     *
     * @return the fully qualified class name of the caller making the logging request.
     */
    String getClassName();

    /**
     * Returns the line number of the caller.
     * <p>This information is not always available.
     *
     * @return the line number of the caller.
     */
    String getLineNumber();

    /**
     * Returns the method name of the caller.
     *
     * @return the method name of the caller.
     */
    String getMethodName();
}
