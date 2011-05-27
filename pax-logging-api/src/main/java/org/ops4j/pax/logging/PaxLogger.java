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

public interface PaxLogger
{
    int LEVEL_TRACE = 0;
    int LEVEL_DEBUG = 1;
    int LEVEL_INFO = 2;
    int LEVEL_WARNING = 3;
    int LEVEL_ERROR = 4;

    boolean isTraceEnabled();
    boolean isDebugEnabled();
    boolean isWarnEnabled();
    boolean isInfoEnabled();
    boolean isErrorEnabled();
    boolean isFatalEnabled();

    void trace( String message, Throwable t );
    void debug( String message, Throwable t );
    void inform( String message, Throwable t );
    void warn( String message, Throwable t );
    void error( String message, Throwable t );
    void fatal( String message, Throwable t );

    void trace( String message, Throwable t, String fqcn);
    void debug( String message, Throwable t, String fqcn);
    void inform( String message, Throwable t, String fqcn);
    void warn( String message, Throwable t, String fqcn);
    void error( String message, Throwable t, String fqcn);
    void fatal( String message, Throwable t, String fqcn);

    int getLogLevel();

    String getName();
    
    PaxContext getPaxContext();
}

