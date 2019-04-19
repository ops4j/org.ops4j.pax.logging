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
package org.ops4j.pax.logging;

/**
 * Version of {@link PaxLogger} that doesn't force implementor to provide methods accepting FQCN.
 */
public abstract class FqcnIgnoringPaxLogger implements PaxLogger {

    private static String appendToMessage(String message, String fqcn) {
        return message + " Ignored FQCN: " + fqcn;
    }

    public void trace(String message, Throwable t, String fqcn) {
        trace(appendToMessage(message, fqcn), t);
    }

    public void debug(String message, Throwable t, String fqcn) {
        debug(appendToMessage(message, fqcn), t);
    }

    public void inform(String message, Throwable t, String fqcn) {
        inform(appendToMessage(message, fqcn), t);
    }

    public void warn(String message, Throwable t, String fqcn) {
        warn(appendToMessage(message, fqcn), t);
    }

    public void error(String message, Throwable t, String fqcn) {
        error(appendToMessage(message, fqcn), t);
    }

    public void fatal(String message, Throwable t, String fqcn) {
        fatal(appendToMessage(message, fqcn), t);
    }

}
