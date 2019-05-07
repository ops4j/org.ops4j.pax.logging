/*  Copyright 2007 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.logging.internal;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Handler;
import java.util.logging.LogManager;

import org.apache.log4j.helpers.LogLog;
import org.ops4j.pax.logging.OSGIPaxLoggingManager;
import org.ops4j.pax.logging.PaxLoggingConstants;
import org.ops4j.pax.logging.PaxLoggingManager;
import org.ops4j.pax.logging.PaxLoggingManagerAwareLogger;
import org.ops4j.pax.logging.spi.support.BackendSupport;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    /**
     * This map will keep facade-specific loggers before {@link PaxLoggingManager} becomes available.
     */
    public static final Map<String, PaxLoggingManagerAwareLogger> m_loggers = new WeakHashMap<String, PaxLoggingManagerAwareLogger>();

    private PaxLoggingManager manager;

    // optional JUL handler to bridge events to pax-logging
    private JdkHandler m_JdkHandler;

    // bundle/service/framework listener that logs events into log service
    // as required by "101.6 Mapping of Events"
    private FrameworkHandler m_frameworkHandler;

    public void start(BundleContext bundleContext) throws Exception {
        String name = getClass().getName();

        // This class is effectively a tracker of PaxLoggingService services - there should be only one
        manager = new OSGIPaxLoggingManager(bundleContext);

        // Falback PaxLogger configuration
        String levelName = BackendSupport.defaultLogLevel(bundleContext);
        DefaultServiceLog.setLogLevel(levelName);
        if (DefaultServiceLog.getStaticLogLevel() <= DefaultServiceLog.DEBUG) {
            // Log4j1 debug
            LogLog.setInternalDebugging(true);
        }

        // for JUL we may install bridging java.util.logging.Handler just like org.slf4j:jul-to-slf4j
        if (!Boolean.parseBoolean(bundleContext.getProperty(PaxLoggingConstants.LOGGING_CFG_SKIP_JUL))
                && !skipJulRegistration()) {
            LogManager logManager = LogManager.getLogManager();

            if (!Boolean.valueOf(bundleContext.getProperty(PaxLoggingConstants.LOGGING_CFG_SKIP_JUL_RESET))
                    && !skipJulReset()) {
                logManager.reset();
            }

            // clear out old handlers
            java.util.logging.Logger rootLogger = logManager.getLogger("");
            Handler[] handlers = rootLogger.getHandlers();
            for (int i = 0; i < handlers.length; i++) {
                rootLogger.removeHandler(handlers[i]);
            }
            rootLogger.setFilter(null);

            m_JdkHandler = new JdkHandler(manager);
            rootLogger.addHandler(m_JdkHandler);

            java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger(name);
            julLogger.info("Enabling Java Util Logging API support.");
        }

        // for each logging framework/facade supported, we'll:
        // 1. configure the facade/bridge/factory with single instance of PaxLoggingManager
        // 2. obtain the framework specific log(ger)
        // 3. use the log(ger)

        // SLF4J
        org.ops4j.pax.logging.slf4j.Slf4jLoggerFactory.setPaxLoggingManager(manager);
        org.slf4j.Logger slf4jLogger = org.slf4j.LoggerFactory.getLogger(name);
        slf4jLogger.info("Enabling SLF4J API support.");

        // Apache Commons Logging
        org.apache.commons.logging.LogFactory.setPaxLoggingManager(manager);
        org.apache.commons.logging.Log commonsLogger = org.apache.commons.logging.LogFactory.getLog(name);
        commonsLogger.info("Enabling Apache Commons Logging API support.");

        // Apache Juli Logging
        org.apache.juli.logging.LogFactory.setPaxLoggingManager(manager);
        org.apache.juli.logging.Log juliLogger = org.apache.juli.logging.LogFactory.getLog(name);
        juliLogger.info("Enabling JULI Logger API support.");

        // Avalon Logging
        org.ops4j.pax.logging.avalon.AvalonLogFactory.setPaxLoggingManager(manager);
        org.apache.avalon.framework.logger.Logger avalonLogger = org.ops4j.pax.logging.avalon.AvalonLogFactory.getLogger(name);
        avalonLogger.info("Enabling Avalon Logger API support.");

        // JBoss Logging
        // PAXLOGGING-251

        // Log4j1
        org.apache.log4j.Logger.configurePaxLoggingManager(manager);
        org.apache.log4j.Logger log4j1Logger = org.apache.log4j.Logger.getLogger(name);
        log4j1Logger.info("Enabling Log4J v1 API support.");

        // Log4j2
        org.ops4j.pax.logging.log4jv2.Log4jv2LoggerContext.setBundleContext(bundleContext);
        org.apache.logging.log4j.Logger log4j2Logger = org.apache.logging.log4j.LogManager.getLogger(name);
        log4j2Logger.info("Enabling Log4J v2 API support.");

        // after all the above facades are configured to get loggers from PaxLoggingManager (and further - from
        // specific PaxLoggingService), we just have to reconfigure already created loggers
        synchronized (m_loggers) {
            // We need to instruct all loggers to ensure they delegate to proper PaxLogger that delegates to
            // actual PaxLoggingService
            for (PaxLoggingManagerAwareLogger logger : m_loggers.values()) {
                logger.setPaxLoggingManager(manager);
            }
        }

        // handler that logs framework/bundle/service events, according to OSGi Compendium R6 101.6
        m_frameworkHandler = new FrameworkHandler(bundleContext, manager);
        bundleContext.addBundleListener(m_frameworkHandler);
        bundleContext.addFrameworkListener(m_frameworkHandler);
        bundleContext.addServiceListener(m_frameworkHandler);
    }

    public void stop(BundleContext bundleContext) throws Exception {
        // Clean up the listeners.
        if (m_frameworkHandler != null) {
            bundleContext.removeBundleListener(m_frameworkHandler);
            bundleContext.removeFrameworkListener(m_frameworkHandler);
            bundleContext.removeServiceListener(m_frameworkHandler);
        }

        String name = getClass().getName();

        org.slf4j.Logger slf4jLogger = org.slf4j.LoggerFactory.getLogger(name);
        slf4jLogger.info("Disabling SLF4J API support.");

        org.apache.commons.logging.Log commonsLogger = org.apache.commons.logging.LogFactory.getLog(name);
        commonsLogger.info("Disabling Apache Commons Logging API support.");

        org.apache.juli.logging.Log juliLogger = org.apache.juli.logging.LogFactory.getLog(name);
        juliLogger.info("Disabling JULI Logger API support.");

        org.apache.avalon.framework.logger.Logger avalonLogger = org.ops4j.pax.logging.avalon.AvalonLogFactory.getLogger(name);
        avalonLogger.info("Disabling Avalon Logger API support.");

        // JBoss Logging - PAXLOGGING-251

        org.apache.log4j.Logger log4j1Logger = org.apache.log4j.Logger.getLogger(name);
        log4j1Logger.info("Disabling Log4J v1 API support.");

        org.apache.logging.log4j.Logger log4j2Logger = org.apache.logging.log4j.LogManager.getLogger(getClass());
        log4j2Logger.info("Disabling Log4J v2 API support.");

        org.ops4j.pax.logging.log4jv2.Log4jv2LoggerContext.dispose();
        org.ops4j.pax.logging.log4jv2.Log4jv2ThreadContextMap.dispose();

        // Remove the global handler for all JDK Logging (java.util.logging).
        if (m_JdkHandler != null) {
            java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger(name);
            julLogger.info("Disabling Java Util Logging API support.");

            java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");
            rootLogger.removeHandler(m_JdkHandler);
            m_JdkHandler.flush();
            m_JdkHandler.close();
            m_JdkHandler = null;
        }

        if (manager != null) {
            manager.dispose();
            manager.close();
        }
    }

    private boolean skipJulRegistration() {
        if (System.getSecurityManager() != null) {
            return AccessController.doPrivileged((PrivilegedAction<Boolean>) ()
                    -> Boolean.getBoolean(PaxLoggingConstants.LOGGING_CFG_SKIP_JUL));
        } else {
            return Boolean.getBoolean(PaxLoggingConstants.LOGGING_CFG_SKIP_JUL);
        }
    }

    private boolean skipJulReset() {
        if (System.getSecurityManager() != null) {
            return AccessController.doPrivileged((PrivilegedAction<Boolean>) ()
                    -> Boolean.getBoolean(PaxLoggingConstants.LOGGING_CFG_SKIP_JUL_RESET));
        } else {
            return Boolean.getBoolean(PaxLoggingConstants.LOGGING_CFG_SKIP_JUL_RESET);
        }
    }

}
