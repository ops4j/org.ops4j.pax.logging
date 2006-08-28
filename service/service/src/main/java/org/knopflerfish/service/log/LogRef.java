/*
 * Copyright (c) 2003, KNOPFLERFISH project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials
 *   provided with the distribution.
 *
 * - Neither the name of the KNOPFLERFISH project nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.knopflerfish.service.log;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

/**
 * * LogRef is an utility class that simplifies the use of the LogService. * *
 * <P> * * LogRef let you use the log without worrying about getting new *
 * service objects when the log service is restarted. It also * supplies methods
 * with short names that does logging with all the * different LogService
 * severity types. * *
 * </P>
 * <P> * * To use the LogRef you need to import *
 * <code>org.knopflerfish.service.log.LogRef</code> and instantiate * LogRef
 * with your bundle context as parameter. The bundle context * is used for
 * getting the LogService and adding a service listener. * *
 * </P> * *
 * <H2>Example usage</H2> * * The <code>if</code> statement that protects
 * each call to the * <code>LogRef</code> instance below is there to save the
 * effort * required for creating the message string object in cases where the *
 * log will throw away the log entry due to its unimportance. The * user must
 * have this <code>if</code>-test in his code since that is * the only way to
 * avoid constructing the string object. Placing it * in the wrapper (LogRef)
 * will not help due to the design of the Java * programming language. * * *
 * 
 * <PRE> * package org.knopflerfish.example.hello; * * import
 * org.osgi.framework.*; * import org.knopflerfish.service.log.LogRef; * * *
 * public class Hello implements BundleActivator { * LogRef log; * * public void
 * start(BundleContext bundleContext) { * log = new LogRef(bundleContext); * if
 * (log.doInfo()) log.info("Hello started."); * } * * public void
 * stop(BundleContext bundleContext) { * if (log.doDebug()) log.debug("Hello
 * stopped."); * } * } *
 * 
 * </PRE> * * *
 * 
 * @author Gatespace AB *
 * @see org.osgi.service.log.LogService *
 * @see org.knopflerfish.service.log.LogService
 */

public class LogRef implements ServiceListener, LogService {
    // Class name of the OSGI log service
    private final static String LOG_CLASS_OSGI = org.osgi.service.log.LogService.class
            .getName();

    // Class name of Knopflerfish extended log service
    private final static String LOG_CLASS_KF = org.knopflerfish.service.log.LogService.class
            .getName();

    private final static String logServiceFilter = "(|" + "(objectClass="
            + LOG_CLASS_KF + ")(objectClass=" + LOG_CLASS_OSGI + "))";

    // Date formater used then sending entries to System.out
    private static SimpleDateFormat simpleDateFormat = null;

    // Handle to the framework
    private BundleContext bc;

    // Service reference for the current log service
    private ServiceReference logSR;

    // The current log service
    private org.osgi.service.log.LogService log;

    // If true and no log service, print on System.out
    private boolean useOut;

    // The id of the calling bundle
    private long bundleId;

    // If true warn about using closed LogRef object
    private boolean doWarnIfClosed;

    /**
     * * Create new LogRef object for a given bundle. *
     * 
     * @param bc
     *            the bundle context of the bundle that this log ref * instance
     *            belongs too. *
     * @param out
     *            If true print messages on <code>System.out</code> when *
     *            there is no log service.
     */
    public LogRef(BundleContext bc, boolean out) {
        init(bc, out);
    }

    /**
     * * Create new LogRef object for a given bundle. * *
     * <p> * If the system property <tt>org.knopflerfish.log.out</tt> equals *
     * "true", system.out will be used as fallback if no log service * is found. *
     * </p> *
     * 
     * @param bc
     *            the bundle context of the bundle that this log ref * instance
     *            belongs too.
     */
    public LogRef(BundleContext bc) {
        boolean b = false;

        try {
            b = "true".equals(System.getProperty("org.knopflerfish.log.out"));
        } catch (Throwable t) {
            System.err.println("get system property failed: " + t);
            t.printStackTrace();
        }

        init(bc, b);
    }

    private void init(BundleContext bc, boolean out) {
        this.bc = bc;
        useOut = out;
        bundleId = bc.getBundle().getBundleId();
        try {
            bc.addServiceListener(this, logServiceFilter);
        } catch (InvalidSyntaxException e) {
            error("Failed to register log service listener (filter="
                    + logServiceFilter + ")", e);
        }
    }

    /**
     * * Service listener entry point. Releases the log service object if * one
     * has been fetched. * *
     * 
     * @param evt
     *            Service event
     */
    public void serviceChanged(ServiceEvent evt) {
        if (evt.getServiceReference() == logSR
                && evt.getType() == ServiceEvent.UNREGISTERING) {
            ungetLogService();
        }
    }

    /**
     * * Unget the log service. Note that this method is synchronized on * the
     * same object as the internal method that calls the actual log * service.
     * This ensures that the log service is not removed by * this method while a
     * log message is generated.
     */
    private synchronized void ungetLogService() {
        doWarnIfClosed = doDebug();
        if (log != null) {
            bc.ungetService(logSR);
            logSR = null;
            log = null;
        }
    }

    /**
     * * Close this LogRef object. Ungets the log service if active.
     */
    public void close() {
        ungetLogService();
        bc.removeServiceListener(this);
        bc = null;
    }

    /**
     * * Sends a message to the log if possible. * *
     * 
     * @param msg
     *            Human readable string describing the condition. *
     * @param level
     *            The severity of the message (Should be one of the * four
     *            predefined severities). *
     * @param sr
     *            The <code>ServiceReference</code> of the service * that this
     *            message is associated with. *
     * @param e
     *            The exception that reflects the condition.
     */
    protected synchronized void doLog(String msg, int level,
            ServiceReference sr, Throwable e) {
        if (bc != null && log == null) {
            logSR = bc.getServiceReference(LOG_CLASS_KF);
            if (logSR == null) {
                // No service implementing the Knopflerfish extended log, try to
                // look for a standard OSGi log service.
                logSR = bc.getServiceReference(LOG_CLASS_OSGI);
            }
            if (logSR != null) {
                log = (org.osgi.service.log.LogService) bc.getService(logSR);
            }
            if (log == null) {
                // Failed to get log service clear the service reference.
                logSR = null;
            }
        }
        if (log != null) {
            log.log(sr, level, msg, e);
        } else if (useOut || doWarnIfClosed) {
            if (bc == null) {
                System.err.println("WARNING! Bundle #" + bundleId
                        + " called closed LogRef object");
            }
            // No log service and request for messages on System.out
            System.out.print(LogUtil.fromLevel(level, 8));
            System.out.print(" ");
            if (simpleDateFormat == null) {
                simpleDateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
            }
            System.out.print(simpleDateFormat.format(new Date()));
            System.out.print(" ");
            System.out.print(getBundleName());
            System.out.print(" - ");
            if (sr != null) {
                System.out.print("[");
                System.out.print(sr);
                System.out.print("] ");
            }
            System.out.print(msg);
            if (e != null) {
                System.out.print(" (");
                System.out.print(e);
                System.out.print(")");

                System.out.println();
                e.printStackTrace();
            }
            System.out.println();
        }
    }

    /**
     * * Returns the current log level. There is no use to generate log *
     * entries with a severity level less than this value since such * entries
     * will be thrown away by the log. * *
     * 
     * @return the current severity log level for this bundle.
     */
    public int getLogLevel() {
        if (log != null && (log instanceof LogService)) {
            return ((LogService) log).getLogLevel();
        }
        return LOG_DEBUG;
    }

    /**
     * * Returns true if messages with severity debug or higher * are saved by
     * the log. *
     * 
     * @return <code>true</code> if messages with severity LOG_DEBUG * or
     *         higher are included in the log, otherwise <code>false</code>.
     */
    public boolean doDebug() {
        return getLogLevel() >= LOG_DEBUG;
    }

    /**
     * * Returns true if messages with severity warning or higher * are saved by
     * the log. *
     * 
     * @return <code>true</code> if messages with severity LOG_WARNING * or
     *         higher are included in the log, otherwise <code>false</code>.
     */
    public boolean doWarn() {
        return getLogLevel() >= LOG_WARNING;
    }

    /**
     * * Returns true if messages with severity info or higher * are saved by
     * the log. *
     * 
     * @return <code>true</code> if messages with severity LOG_INFO * or
     *         higher are included in the log, otherwise <code>false</code>.
     */
    public boolean doInfo() {
        return getLogLevel() >= LOG_INFO;
    }

    /**
     * * Returns true if messages with severity error or higher * are saved by
     * the log. *
     * 
     * @return <code>true</code> if messages with severity LOG_ERROR * or
     *         higher are included in the log, otherwise <code>false</code>.
     */
    public boolean doError() {
        return getLogLevel() >= LOG_ERROR;
    }

    /**
     * * Log a debug level message * *
     * 
     * @param msg
     *            Log message.
     */
    public void debug(String msg) {
        doLog(msg, LOG_DEBUG, (ServiceReference) null, (Throwable) null);
    }

    /**
     * * Log a debug level message. * *
     * 
     * @param msg
     *            Log message *
     * @param sr
     *            The <code>ServiceReference</code> of the service * that this
     *            message is associated with.
     */
    public void debug(String msg, ServiceReference sr) {
        doLog(msg, LOG_DEBUG, sr, (Throwable) null);
    }

    /**
     * * Log a debug level message. * *
     * 
     * @param msg
     *            Log message *
     * @param e
     *            The exception that reflects the condition.
     */
    public void debug(String msg, Throwable e) {
        doLog(msg, LOG_DEBUG, (ServiceReference) null, e);
    }

    /**
     * * Log a debug level message. * *
     * 
     * @param msg
     *            Log message *
     * @param sr
     *            The <code>ServiceReference</code> of the service * that this
     *            message is associated with. *
     * @param e
     *            The exception that reflects the condition.
     */
    public void debug(String msg, ServiceReference sr, Throwable e) {
        doLog(msg, LOG_DEBUG, sr, e);
    }

    /**
     * * Log an info level message. * *
     * 
     * @param msg
     *            Log message
     */
    public void info(String msg) {
        doLog(msg, LOG_INFO, (ServiceReference) null, (Throwable) null);
    }

    /**
     * * Log an info level message. * *
     * 
     * @param msg
     *            Log message *
     * @param sr
     *            The <code>ServiceReference</code> of the service * that this
     *            message is associated with.
     */
    public void info(String msg, ServiceReference sr) {
        doLog(msg, LOG_INFO, sr, (Throwable) null);
    }

    /**
     * * Log an info level message. * *
     * 
     * @param msg
     *            Log message *
     * @param e
     *            The exception that reflects the condition.
     */
    public void info(String msg, Throwable e) {
        doLog(msg, LOG_INFO, (ServiceReference) null, e);
    }

    /**
     * * Log an info level message. * *
     * 
     * @param msg
     *            Log message *
     * @param sr
     *            The <code>ServiceReference</code> of the service * that this
     *            message is associated with. *
     * @param e
     *            The exception that reflects the condition.
     */
    public void info(String msg, ServiceReference sr, Throwable e) {
        doLog(msg, LOG_INFO, sr, e);
    }

    /**
     * * Log a warning level message. * *
     * 
     * @param msg
     *            Log message
     */
    public void warn(String msg) {
        doLog(msg, LOG_WARNING, (ServiceReference) null, (Throwable) null);
    }

    /**
     * * Log a warning level message. * *
     * 
     * @param msg
     *            Log message *
     * @param sr
     *            The <code>ServiceReference</code> of the service * that this
     *            message is associated with.
     */
    public void warn(String msg, ServiceReference sr) {
        doLog(msg, LOG_WARNING, sr, (Throwable) null);
    }

    /**
     * * Log a warning level message. * *
     * 
     * @param msg
     *            Log message *
     * @param e
     *            The exception that reflects the condition.
     */
    public void warn(String msg, Throwable e) {
        doLog(msg, LOG_WARNING, (ServiceReference) null, e);
    }

    /**
     * * Log a warning level message. * *
     * 
     * @param msg
     *            Log message *
     * @param sr
     *            The <code>ServiceReference</code> of the service * that this
     *            message is associated with. *
     * @param e
     *            The exception that reflects the condition.
     */
    public void warn(String msg, ServiceReference sr, Throwable e) {
        doLog(msg, LOG_WARNING, sr, e);
    }

    /**
     * * Log an error level message. * *
     * 
     * @param msg
     *            Log message
     */
    public void error(String msg) {
        doLog(msg, LOG_ERROR, (ServiceReference) null, (Throwable) null);
    }

    /**
     * * Log an error level message. * *
     * 
     * @param msg
     *            Log message *
     * @param sr
     *            The <code>ServiceReference</code> of the service * that this
     *            message is associated with.
     */
    public void error(String msg, ServiceReference sr) {
        doLog(msg, LOG_ERROR, sr, (Throwable) null);
    }

    /**
     * * Log an error level message. * *
     * 
     * @param msg
     *            Log message *
     * @param e
     *            The exception that reflects the condition.
     */
    public void error(String msg, Throwable e) {
        doLog(msg, LOG_ERROR, (ServiceReference) null, e);
    }

    /**
     * * Log an error level message. * *
     * 
     * @param msg
     *            Log message *
     * @param sr
     *            The <code>ServiceReference</code> of the service * that this
     *            message is associated with. *
     * @param e
     *            The exception that reflects the condition.
     */
    public void error(String msg, ServiceReference sr, Throwable e) {
        doLog(msg, LOG_ERROR, sr, e);
    }

    /**
     * * Log a message. * The ServiceDescription field and the Throwable * field
     * of the LogEntry will be set to null. *
     * 
     * @param level
     *            The severity of the message. (Should be one of the * four
     *            predefined severities.) *
     * @param message
     *            Human readable string describing the condition.
     */
    public void log(int level, String message) {
        doLog(message, level, (ServiceReference) null, (Throwable) null);
    }

    /**
     * * Log a message with an exception. * The ServiceDescription field of the
     * LogEntry will be set to null. *
     * 
     * @param level
     *            The severity of the message. (Should be one of the * four
     *            predefined severities.) *
     * @param message
     *            Human readable string describing the condition. *
     * @param exception
     *            The exception that reflects the condition.
     */
    public void log(int level, String message, Throwable exception) {
        doLog(message, level, (ServiceReference) null, exception);
    }

    /**
     * * Log a message associated with a specific Service. * The Throwable field
     * of the LogEntry will be set to null. *
     * 
     * @param sr
     *            The <code>ServiceReference</code> of the service that * this
     *            message is associated with. *
     * @param level
     *            The severity of the message. (Should be one of the * four
     *            predefined severities.) *
     * @param message
     *            Human readable string describing the condition.
     */
    public void log(ServiceReference sr, int level, String message) {
        doLog(message, level, sr, (Throwable) null);
    }

    /**
     * * Log a message with an exception associated with a specific Service. *
     * 
     * @param sr
     *            The <code>ServiceReference</code> of the service that * this
     *            message is associated with. *
     * @param level
     *            The severity of the message. (Should be one of the * four
     *            predefined severities.) *
     * @param message
     *            Human readable string describing the condition. *
     * @param exception
     *            The exception that reflects the condition.
     */
    public void log(ServiceReference sr, int level, String message,
            Throwable exception) {
        doLog(message, level, sr, exception);
    }

    /**
     * * Returns a human readable name for the bundle that * <code>bc</code>
     * represents. *
     * 
     * @return Name of the bundle that uses this wrapper * (at least 12
     *         characters).
     */
    private String getBundleName() {
        StringBuffer bundleName = new StringBuffer(24);
        // We can't get bundle-name since it requires AdminPermission.
        // bundleName.append((String)bc.getBundle().getHeaders().get("Bundle-Name"));
        // If name was not found use the Bid as name.
        if (bundleName.length() <= 0) {
            bundleName.append("bid#");
            bundleName.append(String.valueOf(bundleId));
        }
        if (bundleName.length() < 12) {
            bundleName.append("            ");
            bundleName.setLength(12);
        }
        return bundleName.toString();
    }

}
