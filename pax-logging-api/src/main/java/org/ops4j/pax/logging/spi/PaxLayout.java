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

/**
 * Framework library agnostic representation of a <em>layout</em> that's used
 * to turn a <em>logging event</em> into String representation.
 */
public interface PaxLayout {

    /**
     * Creates the string representation of a logging event.
     *
     * @param event the logging event
     * @return the string reprensentation
     */
    String doLayout(PaxLoggingEvent event);

    /**
     * Return the type of content
     */
    String getContentType();

    /**
     * Returns the header for the layout format. The base class returns
     * <code>null</code>.
     */
    String getHeader();

    /**
     * Returns the footer for the layout format. The base class returns
     * <code>null</code>.
     */
    String getFooter();

}
