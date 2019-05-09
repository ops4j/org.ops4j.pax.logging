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
package org.ops4j.pax.logging.service.internal;

import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.Loader;
import org.apache.log4j.xml.XMLLayout;
import org.ops4j.pax.logging.EventAdminPoster;
import org.ops4j.pax.logging.PaxLoggingConstants;
import org.ops4j.pax.logging.PaxLoggingService;
import org.ops4j.pax.logging.spi.support.BackendSupport;
import org.ops4j.pax.logging.spi.support.LogReaderServiceImpl;
import org.ops4j.pax.logging.spi.support.RegisteredService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;

/**
 * <p>Activator for one of different <em>backends</em> supporting pax-logging-api multi-facade.</p>
 * <p>The most important tasks are:<ul>
 *     <li>register {@link LogService}/{@link org.knopflerfish.service.log.LogService}/{@link PaxLoggingService}
 *     service specific to Log4J1</li>
 *     <li>register {@link ManagedService} to track {@code org.ops4j.pax.logging} PID</li>
 * </ul></p>
 */
public class Activator implements BundleActivator {

    // PaxLoggingService implementation backed by Log4J1 and its registration
    private ServiceRegistration<?> m_RegistrationPaxLogging;
    private PaxLoggingServiceImpl m_PaxLogging;

    private RegisteredService<LogReaderService, LogReaderServiceImpl> logReaderInfo;
    private RegisteredService<EventAdminPoster, EventAdminPoster> eventAdminInfo;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        sanityCheck();

        // OSGi Compendium 101.4: Log Reader Service
        logReaderInfo = BackendSupport.createAndRegisterLogReaderService(bundleContext);

        // OSGi Compendium 101.6.4: Log Events
        eventAdminInfo = BackendSupport.eventAdminSupport(bundleContext);

        // OSGi Compendium 101.2: The Log Service Interface - register Log4J1 specific Pax Logging service
        m_PaxLogging = new PaxLoggingServiceImpl(bundleContext, logReaderInfo.getService(), eventAdminInfo.getService());

        // registration of log service and CM ManagedService for org.ops4j.pax.logging PID
        Dictionary<String, Object> serviceProperties = new Hashtable<>();
        serviceProperties.put(Constants.SERVICE_PID, PaxLoggingConstants.LOGGING_CONFIGURATION_PID);
        serviceProperties.put(Constants.SERVICE_RANKING, BackendSupport.paxLoggingServiceRanking(bundleContext));
        m_RegistrationPaxLogging = bundleContext.registerService(PaxLoggingConstants.LOGGING_LOGSERVICE_NAMES,
                m_PaxLogging, serviceProperties);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        if (eventAdminInfo != null) {
            eventAdminInfo.close();
        }
        if (logReaderInfo != null) {
            logReaderInfo.close();
        }

        m_RegistrationPaxLogging.unregister();
        m_RegistrationPaxLogging = null;

        // Shutdown Pax Logging to ensure appender file locks get released
        if (m_PaxLogging != null) {
            m_PaxLogging.shutdown();
            m_PaxLogging = null;
        }
    }

    /**
     * Ensure that some specific classes are loaded by pax-logging-service classloader instead of
     * from pax-logging-api bundle.
     */
    private void sanityCheck() {
        Bundle paxLoggingApi = FrameworkUtil.getBundle(PaxLoggingService.class);
        Bundle paxLoggingService = FrameworkUtil.getBundle(this.getClass());

        // pax-logging-api has own versions of these classes because they're part of the API
        // and exported packages
        Bundle b1 = FrameworkUtil.getBundle(Logger.class);
        if (paxLoggingService != b1) {
            String b1Bundle = b1 == null ? "system classloader" : b1.toString();
            throw new IllegalStateException("org.apache.log4j.Logger class was loaded from " + b1Bundle +
                    ". It should be loaded from " + paxLoggingService + ".");
        }
        Bundle b2 = FrameworkUtil.getBundle(LogManager.class);
        if (paxLoggingService != b2) {
            String b2Bundle = b2 == null ? "system classloader" : b2.toString();
            throw new IllegalStateException("org.apache.log4j.LogManager class was loaded from " + b2Bundle +
                    ". It should be loaded from " + paxLoggingService + ".");
        }

        // org.apache.log4j.helpers.Loader should be taken from pax-logging-service even if pax-logging-api
        // has own version.
        // pax-logging-api also needs this class, because it's part of exported package.
        Bundle b3 = FrameworkUtil.getBundle(Loader.class);
        if (paxLoggingService != b3) {
            String b3Bundle = b3 == null ? "system classloader" : b3.toString();
            throw new IllegalStateException("org.apache.log4j.helpers.Loader class was loaded from " + b3Bundle +
                    ". It should be loaded from " + paxLoggingService + ".");
        }

        Bundle b4 = FrameworkUtil.getBundle(XMLLayout.class);
        if (paxLoggingApi != b4) {
            String b4Bundle = b4 == null ? "system classloader" : b4.toString();
            throw new IllegalStateException("org.apache.log4j.xml.XMLLayout class was loaded from " + b4Bundle +
                    ". It should be loaded from " + paxLoggingApi + ".");
        }
    }
}
