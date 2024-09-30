/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * NOTE!!!! This is NOT the original Jakarta Commons Logging, but an adaption
 * of its interface so that this Log4J OSGi bundle can export the JCL interface
 * but redirect to dynamically configured logging library.
 * There is nothing here that is useful outside an OSGi environment.
 */
package org.apache.commons.logging;

import java.util.Hashtable;

import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingManager;
import org.ops4j.pax.logging.internal.Activator;
import org.ops4j.pax.logging.jcl.JclLogger;
import org.ops4j.pax.logging.spi.support.FallbackLogFactory;
import org.osgi.framework.FrameworkUtil;

/**
 * Factory for creating {@link Log} instances, with discovery and
 * configuration features similar to that employed by standard Java APIs
 * such as JAXP.
 *
 * <p>This is an adaptation of the Jakarta Commons Logging API for OSGi usage.
 *
 * <p>This is the only class from {@code org.apache.commons.logging} package that is adjusted. Other
 * commons-logging classes are simply repackaged from original jar.
 *
 * <p>There's no need for discovery code that's constituting most of original version's functionality.
 *
 * <p>Original {@code org.apache.commons.logging.LogFactory} is abstract. In pax-logging-api,
 * this class is concrete. All public methods and fields are preserved. Unnecessary private and protected methods
 * and fields are removed.
 *
 * <p>pax-logging-api used source from commons-logging:commons-logging:1.3.4
 *
 * @author Niclas Hedhman (responsible for the OSGi adaptation.)
 * @author Craig R. McClanahan
 * @author Costin Manolache
 * @author Richard A. Sitze
 * @author Grzegorz Grzybek (adjustments and code cleanup)
 */
public class LogFactory {
    // Implementation note re AccessController usage
    //
    // It is important to keep code invoked via an AccessController to small
    // auditable blocks. Such code must carefully evaluate all user input
    // (parameters, system properties, configuration file contents, etc). As an
    // example, a Log implementation should not write to its log file
    // with an AccessController anywhere in the call stack, otherwise an
    // insecure application could configure the log implementation to write
    // to a protected file using the privileges granted to JCL rather than
    // to the calling application.
    //
    // Under no circumstance should a non-private method return data that is
    // retrieved via an AccessController. That would allow an insecure application
    // to invoke that method and obtain data that it is not permitted to have.
    //
    // Invoking user-supplied code with an AccessController set is not a major
    // issue (for example, invoking the constructor of the class specified by
    // HASHTABLE_IMPLEMENTATION_PROPERTY). That class will be in a different
    // trust domain, and therefore must have permissions to do whatever it
    // is trying to do regardless of the permissions granted to JCL. There is
    // a slight issue in that untrusted code may point that environment variable
    // to another trusted library, in which case the code runs if both that
    // library and JCL have the necessary permissions even when the untrusted
    // caller does not. That's a pretty hard route to exploit though.

    private static PaxLoggingManager m_paxLogging;

    /**
     * Singleton instance without discovery
     */
    private static final LogFactory m_instance = new LogFactory();

    public static void setPaxLoggingManager(PaxLoggingManager manager) {
        m_paxLogging = manager;
    }

    /**
     * The name ({@code priority}) of the key in the configuration file used to
     * specify the priority of that particular configuration file. The associated value
     * is a floating-point number; higher values take priority over lower values.
     */
    public static final String PRIORITY_KEY = "priority";

    /**
     * The name ({@code use_tccl}) of the key in the configuration file used
     * to specify whether logging classes should be loaded via the thread
     * context class loader (TCCL), or not. By default, the TCCL is used.
     */
    public static final String TCCL_KEY = "use_tccl";

    /**
     * The name ({@code org.apache.commons.logging.LogFactory}) of the property
     * used to identify the LogFactory implementation
     * class name. This can be used as a system property, or as an entry in a
     * configuration properties file.
     */
    public static final String FACTORY_PROPERTY = "org.apache.commons.logging.LogFactory";

    /**
     * The fully qualified class name of the fallback {@code LogFactory}
     * implementation class to use, if no other can be found.
     */
    public static final String FACTORY_DEFAULT = "org.apache.commons.logging.LogFactory";

    /**
     * The name ({@code commons-logging.properties}) of the properties file to search for.
     */
    public static final String FACTORY_PROPERTIES = "commons-logging.properties";

    /**
     * JDK 1.3+ <a href="https://java.sun.com/j2se/1.3/docs/guide/jar/jar.html#Service%20Provider">
     * 'Service Provider' specification</a>.
     */
    protected static final String SERVICE_ID = "META-INF/services/org.apache.commons.logging.LogFactory";

    /**
     * The name ({@code org.apache.commons.logging.diagnostics.dest})
     * of the property used to enable internal commons-logging
     * diagnostic output, in order to get information on what logging
     * implementations are being discovered, what class loaders they
     * are loaded through, etc.
     * <p>
     * If a system property of this name is set then the value is
     * assumed to be the name of a file. The special strings
     * STDOUT or STDERR (case-sensitive) indicate output to
     * System.out and System.err respectively.
     * <p>
     * Diagnostic logging should be used only to debug problematic
     * configurations and should not be set in normal production use.
     */
    public static final String DIAGNOSTICS_DEST_PROPERTY = "org.apache.commons.logging.diagnostics.dest";

    /**
     * Setting this system property
     * ({@code org.apache.commons.logging.LogFactory.HashtableImpl})
     * value allows the {@code Hashtable} used to store
     * class loaders to be substituted by an alternative implementation.
     * <p>
     * <strong>Note:</strong> {@code LogFactory} will print:
     * </p>
     * <pre>
     * [ERROR] LogFactory: Load of custom hash table failed
     * </pre>
     * <p>
     * to system error and then continue using a standard Hashtable.
     * </p>
     * <p>
     * <strong>Usage:</strong> Set this property when Java is invoked
     * and {@code LogFactory} will attempt to load a new instance
     * of the given implementation class.
     * For example, running the following ant scriplet:
     * </p>
     * <pre>
     *  &lt;java classname="${test.runner}" fork="yes" failonerror="${test.failonerror}"&gt;
     *     ...
     *     &lt;sysproperty
     *        key="org.apache.commons.logging.LogFactory.HashtableImpl"
     *        value="org.apache.commons.logging.AltHashtable"/&gt;
     *  &lt;/java&gt;
     * </pre>
     * <p>
     * will mean that {@code LogFactory} will load an instance of
     * {@code org.apache.commons.logging.AltHashtable}.
     * </p>
     * <p>
     * A typical use case is to allow a custom
     * Hashtable implementation using weak references to be substituted.
     * This will allow class loaders to be garbage collected without
     * the need to release them (on 1.3+ JVMs only, of course ;).
     * </p>
     */
    public static final String HASHTABLE_IMPLEMENTATION_PROPERTY = "org.apache.commons.logging.LogFactory.HashtableImpl";

    /**
     * The previously constructed {@code LogFactory} instances, keyed by
     * the {@code ClassLoader} with which it was created.
     */
    protected static Hashtable<ClassLoader, LogFactory> factories;

    /**
     * Previously constructed {@code LogFactory} instance as in the
     * {@code factories} map, but for the case where
     * {@code getClassLoader} returns {@code null}.
     * This can happen when:
     * <ul>
     * <li>using JDK1.1 and the calling code is loaded via the system
     *  class loader (very common)</li>
     * <li>using JDK1.2+ and the calling code is loaded via the boot
     *  class loader (only likely for embedded systems work).</li>
     * </ul>
     * Note that {@code factories} is a <em>Hashtable</em> (not a HashMap),
     * and hash tables don't allow null as a key.
     * @deprecated since 1.1.2
     */
    @Deprecated
    protected static volatile LogFactory nullClassLoaderFactory;

    static {
        // empty in Pax Logging
    }

    /**
     * Safely get access to the class loader for the specified class.
     * <p>
     * Theoretically, calling getClassLoader can throw a security exception,
     * and so should be done under an AccessController in order to provide
     * maximum flexibility. However in practice people don't appear to use
     * security policies that forbid getClassLoader calls. So for the moment
     * all code is written to call this method rather than Class.getClassLoader,
     * so that we could put AccessController stuff in this method without any
     * disruption later if we need to.
     * </p>
     * <p>
     * Even when using an AccessController, however, this method can still
     * throw SecurityException. Commons Logging basically relies on the
     * ability to access class loaders. A policy that forbids all
     * class loader access will also prevent commons-logging from working:
     * currently this method will throw an exception preventing the entire app
     * from starting up. Maybe it would be good to detect this situation and
     * just disable all commons-logging? Not high priority though - as stated
     * above, security policies that prevent class loader access aren't common.
     * </p>
     * <p>
     * Note that returning an object fetched via an AccessController would
     * technically be a security flaw anyway; untrusted code that has access
     * to a trusted JCL library could use it to fetch the class loader for
     * a class even when forbidden to do so directly.
     * </p>
     *
     * @param clazz Class.
     * @return a ClassLoader.
     *
     * @since 1.1
     */
    protected static ClassLoader getClassLoader(final Class<?> clazz) {
        return clazz.getClassLoader();
    }

    /**
     * Opposite to original {@code LogFactory.getFactory}, simply preinstantiated factory is returned.
     * No discovery is performed at all.
     *
     * Constructs (if necessary) and return a {@code LogFactory} instance, using the following ordered lookup procedure to determine the name of the
     * implementation class to be loaded.
     * <ul>
     * <li>The {@code org.apache.commons.logging.LogFactory} system property.</li>
     * <li>The JDK 1.3 Service Discovery mechanism</li>
     * <li>Use the properties file {@code commons-logging.properties} file, if found in the class path of this class. The configuration file is in standard
     * {@link java.util.Properties} format and contains the fully qualified name of the implementation class with the key being the system property defined
     * above.</li>
     * <li>Fall back to a default implementation class ({@code org.apache.commons.logging.impl.LogFactoryImpl}).</li>
     * </ul>
     * <p>
     * <em>NOTE</em> - If the properties file method of identifying the {@code LogFactory} implementation class is utilized, all of the properties defined in
     * this file will be set as configuration attributes on the corresponding {@code LogFactory} instance.
     * </p>
     * <p>
     * <em>NOTE</em> - In a multi-threaded environment it is possible that two different instances will be returned for the same class loader environment.
     * </p>
     *
     * @return a {@code LogFactory}.
     * @throws LogConfigurationException if the implementation class is not available or cannot be instantiated.
     */
    public static LogFactory getFactory() throws LogConfigurationException {
        return m_instance;
    }

    /**
     * Gets a named logger, without the application having to care about factories.
     *
     * @param clazz Class from which a log name will be derived
     * @return a named logger.
     * @throws LogConfigurationException if a suitable {@code Log} instance cannot be returned
     */
    public static Log getLog(final Class<?> clazz) throws LogConfigurationException {
        return getLog(clazz.getName());
    }

    /**
     * Gets a named logger, without the application having to care about factories.
     *
     * @param name Logical name of the {@code Log} instance to be returned (the meaning of this name is only known to the underlying logging implementation that
     *             is being wrapped)
     * @return a named logger.
     * @throws LogConfigurationException if a suitable {@code Log} instance cannot be returned
     */
    public static Log getLog(final String name) throws LogConfigurationException {
        return getFactory().getInstance(name);
    }

    /**
     * Checks whether the supplied Throwable is one that needs to be
     * re-thrown and ignores all others.
     *
     * The following errors are re-thrown:
     * <ul>
     *   <li>ThreadDeath</li>
     *   <li>VirtualMachineError</li>
     * </ul>
     *
     * @param t the Throwable to check
     */
    protected static void handleThrowable(final Throwable t) {
        if (t instanceof ThreadDeath) {
            throw (ThreadDeath) t;
        }
        if (t instanceof VirtualMachineError) {
            throw (VirtualMachineError) t;
        }
        // All other instances of Throwable will be silently ignored
    }

    /**
     * Returns a string that uniquely identifies the specified object, including
     * its class.
     * <p>
     * The returned string is of form {@code "className@hashCode"}, that is, is the same as
     * the return value of the {@link Object#toString()} method, but works even when
     * the specified object's class has overridden the toString method.
     * </p>
     *
     * @param obj may be null.
     * @return a string of form {@code className@hashCode}, or "null" if obj is null.
     * @since 1.1
     */
    public static String objectId(final Object obj) {
        if (obj == null) {
            return "null";
        }
        return obj.getClass().getName() + "@" + System.identityHashCode(obj);
    }

    /**
     * Releases any internal references to previously created {@link LogFactory}
     * instances that have been associated with the specified class loader
     * (if any), after calling the instance method {@code release()} on
     * each of them.
     *
     * @param classLoader ClassLoader for which to release the LogFactory
     */
    public static void release(final ClassLoader classLoader) {
    }

    /**
     * Release any internal references to previously created {@link LogFactory}
     * instances, after calling the instance method {@code release()} on
     * each of them.  This is useful in environments like servlet containers,
     * which implement application reloading by throwing away a ClassLoader.
     * Dangling references to objects in that class loader would prevent
     * garbage collection.
     */
    public static void releaseAll() {
    }

    /**
     * Constructs a new instance.
     */
    protected LogFactory() {
    }

    /**
     * Gets the configuration attribute with the specified name (if any),
     * or {@code null} if there is no such attribute.
     *
     * @param name Name of the attribute to return
     * @return the configuration attribute with the specified name.
     */
    public Object getAttribute(String name) {
        return null;
    }

    /**
     * Gets an array containing the names of all currently defined configuration attributes. If there are no such attributes, a zero length array is returned.
     *
     * @return an array containing the names of all currently defined configuration attributes
     */
    public String[] getAttributeNames() {
        return new String[0];
    }

    /**
     * Gets a Log for the given class.
     *
     * @param clazz Class for which a suitable Log name will be derived
     * @return a name from the specified class.
     * @throws LogConfigurationException if a suitable {@code Log} instance cannot be returned
     */
    public Log getInstance(Class<?> clazz)
            throws LogConfigurationException {
        return getInstance(clazz.getName());
    }

    /**
     * Gets a (possibly new) {@code Log} instance, using the factory's current set of configuration attributes.
     * <p>
     * <strong>NOTE</strong> - Depending upon the implementation of the {@code LogFactory} you are using, the {@code Log} instance you are returned may or may
     * not be local to the current application, and may or may not be returned again on a subsequent call with the same name argument.
     * </p>
     *
     * <p>In pax-logging, loggers are obtained from current or fallback
     * {@link PaxLoggingManager}</p>
     *
     * @param name Logical name of the {@code Log} instance to be returned (the meaning of this name is only known to the underlying logging implementation that
     *             is being wrapped)
     * @return a {@code Log} instance.
     * @throws LogConfigurationException if a suitable {@code Log} instance cannot be returned
     */
    public Log getInstance(String name)
        throws LogConfigurationException {
        PaxLogger logger;
        if (m_paxLogging == null) {
            logger = FallbackLogFactory.createFallbackLog(FrameworkUtil.getBundle(Log.class), name);
        } else {
            logger = m_paxLogging.getLogger(name, JclLogger.JCL_FQCN);
        }
        JclLogger jclLogger = new JclLogger(name, logger);
        if (m_paxLogging == null) {
            synchronized (Activator.m_loggers) {
                Activator.m_loggers.add(jclLogger);
            }
        }
        return jclLogger;
    }

    /**
     * Releases any internal references to previously created {@link Log}
     * instances returned by this factory.  This is useful in environments
     * like servlet containers, which implement application reloading by
     * throwing away a ClassLoader.  Dangling references to objects in that
     * class loader would prevent garbage collection.
     */
    public void release() {
    }

    /**
     * Removes any configuration attribute associated with the specified name.
     * If there is no such attribute, no action is taken.
     *
     * @param name Name of the attribute to remove
     */
    public void removeAttribute(String name) {
    }

    //
    // We can't do this in the class constructor, as there are many
    // static methods on this class that can be called before any
    // LogFactory instances are created, and they depend upon this
    // stuff having been set up.
    //
    // Note that this block must come after any variable declarations used
    // by any methods called from this block, as we want any static initializer
    // associated with the variable to run first. If static initializers for
    // variables run after this code, then (a) their value might be needed
    // by methods called from here, and (b) they might *override* any value
    // computed here!
    //
    // So the wisest thing to do is just to place this code at the very end
    // of the class file.

    /**
     * Sets the configuration attribute with the specified name.  Calling
     * this with a {@code null} value is equivalent to calling
     * {@code removeAttribute(name)}.
     *
     * @param name Name of the attribute to set
     * @param value Value of the attribute to set, or {@code null}
     *  to remove any setting for this attribute
     */
    public void setAttribute(String name, Object value) {
    }

}
