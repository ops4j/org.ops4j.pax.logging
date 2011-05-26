/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * Copyright 2005 Niclas Hedhman
 * Copyright 2007 Hiram Chirino
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
package org.ops4j.pax.logging.internal;

import org.ops4j.pax.logging.FqcnIgnoringPaxLogger;
import org.osgi.framework.Bundle;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxContext;

/**
 * This Logger will be used when the Pax Logging Service is not available.
 * <p>
 * Defaults to DEBUG but can be changed if if the "org.ops4j.pax.logging.DefaultServiceLog.level" system property
 * is set to on of the following: TRACE, DEBUG, INFO, WARN, ERROR, FATAL, or NONE, prior to this class is loaded,
 * OR by calling the static method <code>DefaultServiceLog.setLogLevel( String <b>level</b> )</code>, where
 * <b>level</b> is one of the same strings.
 */
public class DefaultServiceLog extends FqcnIgnoringPaxLogger
{

    private static final int TRACE = 0;
    private static final int DEBUG = 1;
    private static final int INFO = 2;
    private static final int WARN = 3;
    private static final int ERROR = 4;
    private static final int FATAL = 5;
    private static final int NONE = 6;

    public static int level;

    private Bundle m_bundle;
    private String m_categoryName;
    private PaxContext m_context=new PaxContext();

    static
    {
        String levelName = System.getProperty( "org.ops4j.pax.logging.DefaultServiceLog.level", "DEBUG" ).trim();
        convertLevel( levelName );
    }

    DefaultServiceLog( Bundle bundle, String categoryName )
    {
        m_bundle = bundle;
        m_categoryName = categoryName;
    }

    public boolean isTraceEnabled()
    {
        return level <= TRACE;
    }

    public boolean isDebugEnabled()
    {
        return level <= DEBUG;
    }

    public boolean isWarnEnabled()
    {
        return level <= WARN;
    }

    public boolean isInfoEnabled()
    {
        return level <= INFO;
    }

    public boolean isErrorEnabled()
    {
        return level <= ERROR;
    }

    public boolean isFatalEnabled()
    {
        return level <= FATAL;
    }

    public void trace( String message, Throwable t )
    {
        if( isTraceEnabled() )
        {
            output( message, t );
        }
    }

    public void debug( String message, Throwable t )
    {
        if( isDebugEnabled() )
        {
            output( message, t );
        }
    }

    public void inform( String message, Throwable t )
    {
        if( isInfoEnabled() )
        {
            output( message, t );
        }
    }

    public void warn( String message, Throwable t )
    {
        if( isWarnEnabled() )
        {
            output( message, t );
        }
    }

    public void error( String message, Throwable t )
    {
        if( isErrorEnabled() )
        {
            output( message, t );
        }
    }

    public void fatal( String message, Throwable t )
    {
        if( isFatalEnabled() )
        {
            output( message, t );
        }
    }

    public int getLogLevel()
    {
        return level;
    }

    public static void setLogLevel( String level )
    {
        convertLevel( level );
    }

    public String getName()
    {
        return m_categoryName;
    }

    private void output( String message, Throwable t )
    {
        // Might be [null] if used by standard test cases.
        if( m_bundle != null )
        {
            System.out.print( m_bundle.getSymbolicName() );
        }

        System.out.print( "[" );
        System.out.print( m_categoryName );
        System.out.print( "] : " );
        System.out.println( message );

        if( t != null )
        {
            t.printStackTrace( System.out );
        }
    }

    private static void convertLevel( String levelName )
    {
        if( "TRACE".equals( levelName ) )
        {
            level = TRACE;
        }
        else if( "DEBUG".equals( levelName ) )
        {
            level = DEBUG;
        }
        else if( "INFO".equals( levelName ) )
        {
            level = INFO;
        }
        else if( "WARN".equals( levelName ) )
        {
            level = WARN;
        }
        else if( "ERROR".equals( levelName ) )
        {
            level = ERROR;
        }
        else if( "FATAL".equals( levelName ) )
        {
            level = FATAL;
        }
        else if( "NONE".equals( levelName ) )
        {
            level = NONE;
        }
        else
        {
            level = DEBUG;
        }
    }
    
    public PaxContext getPaxContext() {
        return m_context;
    }
}