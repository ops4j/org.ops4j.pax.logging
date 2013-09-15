/*
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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.ops4j.pax.logging.slf4j.Slf4jLogger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class MarkerTest {

    @Test
    public void test() {

        Marker AUDIT = MarkerFactory.getMarker("AUDIT");
        Marker SECURITY = MarkerFactory.getMarker("SECURITY");
        Marker AUTHENTICATION = MarkerFactory.getMarker("AUTHENTICATION");
        Marker AUTHORIZATION = MarkerFactory.getMarker("AUTHORIZATION");

        AUTHENTICATION.add(SECURITY);
        AUTHORIZATION.add(SECURITY);
        SECURITY.add(AUDIT);

        assertEquals("AUDIT", Slf4jLogger.getMarkerName(AUDIT));
        assertEquals("SECURITY.AUDIT", Slf4jLogger.getMarkerName(SECURITY));
        assertEquals("AUTHENTICATION.SECURITY.AUDIT", Slf4jLogger.getMarkerName(AUTHENTICATION));
        assertEquals("AUTHORIZATION.SECURITY.AUDIT", Slf4jLogger.getMarkerName(AUTHORIZATION));

    }

}
