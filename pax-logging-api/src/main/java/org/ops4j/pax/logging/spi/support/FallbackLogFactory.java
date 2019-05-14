/*
 * Copyright 2009 Niclas Hedhman.
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

package org.ops4j.pax.logging.spi.support;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingConstants;
import org.ops4j.pax.logging.spi.PaxDefaultLogStreamProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * <p>This factory creates the fallback strategy when Pax Logging Service is not (yet) available.</p>
 * <p>This class is not part of pax-logging-api, it's in package that's supposed to be Private-Packaged by
 * pax-logging backends. This special care has to be taken when dealing with singletons that can be produced
 * by this factory. The singleton is implemented for {@link FileServiceLog} which is a default/fallback {@link PaxLogger}
 * that writes to single {@link FileOutputStream}.</p>
 */
public class FallbackLogFactory {

    // even if FallbackLogFactory class may be Private-Packaged in several bundles, only one of them
    // should have active registration (JVM/OSGi-wide singleton)
    private static ServiceRegistration<PaxDefaultLogStreamProvider> singletonStreamRegistration;

    // all bundles (and classloaders) will have however the reference available to use
    // for returned file-based default/fallback loggers (if configured)
    private static ServiceReference<PaxDefaultLogStreamProvider> singletonStreamReference;

    // and the provider itself
    private static PaxDefaultLogStreamProvider singletonStream;

    // cached context for pax-logging-api bundle
    private static BundleContext context;
    // cached bundle for pax-logging-api - it's never cleaned, because we want to detect bundle state
    private static Bundle bundle;

    /**
     * Create {@link PaxLogger} that doesn't delegate to logger from specific
     * {@link org.ops4j.pax.logging.PaxLoggingService}.
     * @param requestingBundle a bundle requesting fallback logger that'll be used when we can't detect better one
     * @param categoryName
     * @return
     */
    public static PaxLogger createFallbackLog(Bundle requestingBundle, String categoryName) {
        // BundleContext is to lookup and register services (should be context from pax-logging-api bundle)
        // Bundle is the bundle that tries to obtain a logger

        requestingBundle = BundleHelper.getCallerBundle(requestingBundle, 3);

        if (bundle == null) {
            synchronized (FallbackLogFactory.class) {
                if (bundle == null) {
                    Bundle b = FrameworkUtil.getBundle(PaxLogger.class);
                    bundle = b;
                }
            }
        }

        if (context == null) {
            synchronized (FallbackLogFactory.class) {
                if (context == null) {
                    Bundle b = bundle == null ? FrameworkUtil.getBundle(PaxLogger.class) : bundle;
                    if (b != null) {
                        context = b.getBundleContext();
                    }
                }
            }
        }

        if (bundle != null && (bundle.getState() == Bundle.STARTING || bundle.getState() == Bundle.ACTIVE)) {
            // only deal with context (check properties and lookup/register services) in selected
            // bundle states
            File logFile = logFile(context);
            if (logFile != null) {
                // file-backed default/fallback log should be singleton with the underlying stream
                // kept in OSGi registry. static fields can't be used, because there may be many instances of this
                // class loaded by different bundles (pax-logging-api and backends)
                if (context != null) {
                    PaxDefaultLogStreamProvider defaultStream = getRegisteredStream(context);
                    if (defaultStream != null) {
                        return createFallbackLog0(requestingBundle, defaultStream, context, categoryName);
                    }

                    // no file stream registered, double check in synchronized block
                    synchronized (PaxLogger.class) {
                        defaultStream = getRegisteredStream(context);
                        if (defaultStream == null) {
                            try {
                                PrintStream stream = new PrintStream(new BufferedOutputStream(new FileOutputStream(logFile)));
                                singletonStream = new PaxDefaultLogStreamProvider() {
                                    @Override
                                    public PrintStream stream() {
                                        return stream;
                                    }

                                    @Override
                                    public void close() throws IOException {
                                        stream.close();
                                    }
                                };
                                singletonStreamRegistration = context.registerService(PaxDefaultLogStreamProvider.class, singletonStream, null);
                                defaultStream = singletonStream;
                            } catch (FileNotFoundException ignored) {
                            }
                        }
                        return createFallbackLog0(requestingBundle, defaultStream, context, categoryName);
                    }
                }
            }
        }

        return createFallbackLog0(requestingBundle, null, context, categoryName);
    }

    /**
     * Because {@link FallbackLogFactory} may be Private-Packaged in all the backends (and in pax-logging-api too),
     * each bundle has to call {@link #cleanup()} in it's {@link org.osgi.framework.BundleActivator#stop(BundleContext)}.
     */
    public static void cleanup() {
        if (singletonStreamRegistration != null) {
            singletonStreamRegistration.unregister();
            try {
                singletonStream.close();
            } catch (IOException ignored) {
            }
        }
        if (context != null && singletonStreamReference != null) {
            try {
                context.ungetService(singletonStreamReference);
            } catch (IllegalStateException ignored) {
                // pax-logging-api context is invalid
            }
            singletonStreamReference = null;
            singletonStream = null;
        }
    }

    private static PaxDefaultLogStreamProvider getRegisteredStream(BundleContext ctx) {
        // we have to be careful here, because we can be in pax-logging backend bundle and
        // the service could be registered in pax-logging-api (which may have been restarted_
        if (singletonStreamRegistration != null) {
            // we've registered it
            return singletonStream;
        }

        if (singletonStreamReference == null) {
            singletonStreamReference = ctx.getServiceReference(PaxDefaultLogStreamProvider.class);
        }

        if (singletonStreamReference != null) {
            // we've looked up the reference. was the reference unregistered?
            if (singletonStreamReference.getBundle() == null) {
                ctx.ungetService(singletonStreamReference);
                singletonStream = null;
            }
            // we have reference, we can have a service itself
            if (singletonStream == null) {
                PaxDefaultLogStreamProvider service = ctx.getService(singletonStreamReference);
                singletonStream = service;
            }
        }

        return singletonStream;
    }

    private static PaxLogger createFallbackLog0(Bundle bundle, PaxDefaultLogStreamProvider streamProvider,
                                                BundleContext ctx, String categoryName) {
        if (isBuffering(ctx)) {
            return new BufferingLog(bundle, categoryName);
        } else {
            if (streamProvider != null && streamProvider.stream() != null) {
                // FileServiceLog is per category, but underlying stream is per file
                return new FileServiceLog(streamProvider.stream(), bundle, categoryName);
            }
            return new DefaultServiceLog(bundle, categoryName);
        }
    }

    private static boolean isBuffering(BundleContext context) {
        String buffering = OsgiUtil.systemOrContextProperty(context,
                PaxLoggingConstants.LOGGING_CFG_USE_BUFFERING_FALLBACK_LOGGER);
        return Boolean.parseBoolean(buffering);
    }

    private static File logFile(BundleContext context) {
        String fileName = OsgiUtil.systemOrContextProperty(context,
                PaxLoggingConstants.LOGGING_CFG_USE_FILE_FALLBACK_LOGGER);
        if (fileName != null) {
            File f = new File(fileName);
            if (f.getParentFile().isDirectory() && (!f.exists() || f.isFile())) {
                return f;
            }
        }
        return null;
    }

}
