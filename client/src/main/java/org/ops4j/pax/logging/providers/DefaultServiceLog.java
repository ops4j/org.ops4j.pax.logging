/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * Copyright 2005 Niclas Hedhman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.logging.providers;

import org.apache.commons.logging.Log;

public class DefaultServiceLog
    implements Log
{
    private String m_CategoryName;

    public DefaultServiceLog( String categoryName )
    {
        m_CategoryName = categoryName;
    }

    /**
     * <p> Is debug logging currently enabled? </p>
     *
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatenation)
     * when the log level is more than debug. </p>
     */
    public boolean isDebugEnabled()
    {
        return true;
    }

    /**
     * <p> Is error logging currently enabled? </p>
     *
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatenation)
     * when the log level is more than error. </p>
     */
    public boolean isErrorEnabled()
    {
        return true;
    }

    /**
     * <p> Is fatal logging currently enabled? </p>
     *
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatenation)
     * when the log level is more than fatal. </p>
     */
    public boolean isFatalEnabled()
    {
        return true;
    }
    /**
     * <p> Is info logging currently enabled? </p>
     *
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatenation)
     * when the log level is more than info. </p>
     */
    public boolean isInfoEnabled()
    {
        return true;
    }
    /**
     * <p> Is trace logging currently enabled? </p>
     *
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatenation)
     * when the log level is more than trace. </p>
     */
    public boolean isTraceEnabled()
    {
        return true;
    }
    /**
     * <p> Is warn logging currently enabled? </p>
     *
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatenation)
     * when the log level is more than warn. </p>
     */
    public boolean isWarnEnabled()
    {
        return true;
    }
    /**
     * <p> Log a message with trace log level. </p>
     *
     * @param message log this message
     */
    public void trace( Object message )
    {
        System.out.println( "TRACE - " + m_CategoryName + " : " + message );
    }

    /**
     * <p> Log an error with trace log level. </p>
     *
     * @param message log this message
     * @param t log this cause
     */
    public void trace( Object message, Throwable t )
    {
        System.out.println( "TRACE - " + m_CategoryName + " : " + message );
        t.printStackTrace();
    }

    /**
     * <p> Log a message with debug log level. </p>
     *
     * @param message log this message
     */
    public void debug( Object message )
    {
        System.out.println( "DEBUG - " + m_CategoryName + " : " + message );
    }

    /**
     * <p> Log an error with debug log level. </p>
     *
     * @param message log this message
     * @param t log this cause
     */
    public void debug(Object message, Throwable t)
    {
        System.out.println( "DEBUG - " + m_CategoryName + " : " + message );
        t.printStackTrace();
    }

    /**
     * <p> Log a message with info log level. </p>
     *
     * @param message log this message
     */
    public void info(Object message)
    {
        System.out.println( "INFO - " + m_CategoryName + " : " + message );

    }

    /**
     * <p> Log an error with info log level. </p>
     *
     * @param message log this message
     * @param t log this cause
     */
    public void info(Object message, Throwable t)
    {
        System.out.println( "INFO - " + m_CategoryName + " : " + message );
        t.printStackTrace();
    }

    /**
     * <p> Log a message with warn log level. </p>
     *
     * @param message log this message
     */
    public void warn(Object message)
    {
        System.out.println( "WARN - " + m_CategoryName + " : " + message );
    }

    /**
     * <p> Log an error with warn log level. </p>
     *
     * @param message log this message
     * @param t log this cause
     */
    public void warn(Object message, Throwable t)
    {
        System.out.println( "WARN - " + m_CategoryName + " : " + message );
        t.printStackTrace();
    }

    /**
     * <p> Log a message with error log level. </p>
     *
     * @param message log this message
     */
    public void error(Object message)
    {
        System.err.println( "ERROR - " + m_CategoryName + " : " + message );
    }

    /**
     * <p> Log an error with error log level. </p>
     *
     * @param message log this message
     * @param t log this cause
     */
    public void error(Object message, Throwable t)
    {
        System.err.println( "ERROR - " + m_CategoryName + " : " + message );
        t.printStackTrace();
    }

    /**
     * <p> Log a message with fatal log level. </p>
     *
     * @param message log this message
     */
    public void fatal(Object message)
    {
        System.err.println( "FATAL - " + m_CategoryName + " : " + message );
    }

    /**
     * <p> Log an error with fatal log level. </p>
     *
     * @param message log this message
     * @param t log this cause
     */
    public void fatal(Object message, Throwable t)
    {
        System.err.println( "FATAL - " + m_CategoryName + " : " + message );
        t.printStackTrace();
    }

    public int getLogLevel()
    {
        // Always DEBUG mode.
        return 10000;
    }
}