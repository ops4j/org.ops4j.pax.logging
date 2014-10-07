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
package org.ops4j.pax.logging.log4j2.internal;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import org.ops4j.pax.logging.PaxLoggingService;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class JdkHandler extends Handler
{
    private static final SecurityManagerEx securityManager;
    
    private PaxLoggingService m_logService;
    
    static
    {
        securityManager = new SecurityManagerEx();
    }

    public JdkHandler( PaxLoggingService logService )
    {
        m_logService = logService;
        setFormatter( new SimpleFormatter() );
    }

    /**
     * Close the <tt>Handler</tt> and free all associated resources.
     * <p>
     * The close method will perform a <tt>flush</tt> and then close the
     * <tt>Handler</tt>.   After close has been called this <tt>Handler</tt>
     * should no longer be used.  Method calls may either be silently
     * ignored or may throw runtime exceptions.
     *
     * @throws SecurityException if a security manager exists and if
     *                           the caller does not have <tt>LoggingPermission("control")</tt>.
     */
    public void close()
        throws SecurityException
    {
    }

    /**
     * Flush any buffered output.
     */
    public void flush()
    {
    }

    /**
     * Publish a <tt>LogRecord</tt>.
     * <p>
     * The logging request was made initially to a <tt>Logger</tt> object,
     * which initialized the <tt>LogRecord</tt> and forwarded it here.
     * <p>
     * The <tt>Handler</tt>  is responsible for formatting the message, when and
     * if necessary.  The formatting should include localization.
     *
     * @param record description of the log event
     */
    public void publish( LogRecord record )
    {
        Level level = record.getLevel();
        String loggerName = record.getLoggerName();
        Bundle callerBundle = getCallerBundle();
        String fqcn = java.util.logging.Logger.class.getName();
        PaxLoggerImpl logger = (PaxLoggerImpl) m_logService.getLogger( callerBundle, loggerName, fqcn );
        String message;
        try
        {
            message = getFormatter().formatMessage( record );
        }
        catch (Exception ex)
        {
            message = record.getMessage();
        }
        Throwable throwable = record.getThrown();
        int levelInt = level.intValue();
        if (levelInt <= Level.FINEST.intValue())
            logger.trace(message, throwable);
        else if (levelInt <= Level.FINE.intValue())
            logger.debug(message, throwable);
        else if (levelInt <= Level.INFO.intValue())
            logger.inform(message, throwable);
        else if (levelInt <= Level.WARNING.intValue())
            logger.warn(message, throwable);
        else
            logger.error(message, throwable);
    }
    
    private Bundle getCallerBundle() {
        Bundle ret = null;
        Class[] classCtx = securityManager.getClassContext();
        for (int i = 0; i < classCtx.length; i++) {
            if (!classCtx[i].getName().startsWith("org.ops4j.pax.logging")
                && !classCtx[i].getName().startsWith("java.util.logging")) {
                ret = FrameworkUtil.getBundle(classCtx[i]);
                break;
            }
        }
        return ret;
    }

    static class SecurityManagerEx extends SecurityManager
    {
        public Class[] getClassContext()
        {
            return super.getClassContext();
        }
    }

}
