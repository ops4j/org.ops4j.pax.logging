/*
 * Copyright 2011 Avid Technology, Inc.
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
package org.ops4j.pax.logging.logback.internal;

import edu.umd.cs.findbugs.annotations.Nullable;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

/**
 * Simple delegate API for a logger to hand events back to the log service.
 * @author Chris Dolan
 * @since 9/6/11 1:08 PM
 */
public interface PaxEventHandler {
    void handleEvents( Bundle bundle, @Nullable ServiceReference sr, int level, String message, Throwable exception );
}
