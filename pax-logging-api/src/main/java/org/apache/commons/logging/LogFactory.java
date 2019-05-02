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

import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingManager;
import org.ops4j.pax.logging.internal.Activator;
import org.ops4j.pax.logging.internal.FallbackLogFactory;
import org.ops4j.pax.logging.jcl.JclLogger;

/**
 * <p>This is an adaptation of the Jakarta Commons Logging API for OSGi usage.</p>
 * <p>This is the only class from {@code org.apache.commons.logging} package that is adjusted. Other
 * commons-logging classes are simply repackaged from original jar.</p>
 * <p>There's no need for discovery code that's constituting most of original version's functionalty</p>
 * <p>Original {@code org.apache.commons.logging.LogFactory} is abstract. In pax-logging-api,
 * this class is concrete. All public methods and fields are preserved. Unnecessary private and protected methods
 * and fields are removed.</p>
 *
 * <p>pax-logging-api used source from commons-logging:commons-logging:1.2</p>
 *
 * @author Niclas Hedhman (responsible for the OSGi adaptation.)
 * @author Craig R. McClanahan
 * @author Costin Manolache
 * @author Richard A. Sitze
 * @author Grzegorz Grzybek (adjustments and code cleanup)
 */
public class LogFactory {

    private static PaxLoggingManager m_paxLogging;

    public static void setPaxLoggingManager(PaxLoggingManager manager) {
        m_paxLogging = manager;
    }

    // copy of original, public constants, to preserve the API

    // ----------------------------------------------------- Manifest Constants

    /**
     * The name (<code>priority</code>) of the key in the config file used to
     * specify the priority of that particular config file. The associated value
     * is a floating-point number; higher values take priority over lower values.
     */
    public static final String PRIORITY_KEY = "priority";

    /**
     * The name (<code>use_tccl</code>) of the key in the config file used
     * to specify whether logging classes should be loaded via the thread
     * context class loader (TCCL), or not. By default, the TCCL is used.
     */
    public static final String TCCL_KEY = "use_tccl";

    /**
     * The name (<code>org.apache.commons.logging.LogFactory</code>) of the property
     * used to identify the LogFactory implementation
     * class name. This can be used as a system property, or as an entry in a
     * configuration properties file.
     */
    public static final String FACTORY_PROPERTY = "org.apache.commons.logging.LogFactory";

    /**
     * The fully qualified class name of the fallback <code>LogFactory</code>
     * implementation class to use, if no other can be found.
     */
    public static final String FACTORY_DEFAULT = "org.apache.commons.logging.LogFactory";

    /**
     * The name (<code>commons-logging.properties</code>) of the properties file to search for.
     */
    public static final String FACTORY_PROPERTIES = "commons-logging.properties";

    /**
     * The name (<code>org.apache.commons.logging.diagnostics.dest</code>)
     * of the property used to enable internal commons-logging
     * diagnostic output, in order to get information on what logging
     * implementations are being discovered, what classloaders they
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
    public static final String DIAGNOSTICS_DEST_PROPERTY =
        "org.apache.commons.logging.diagnostics.dest";

    /**
     * Setting this system property
     * (<code>org.apache.commons.logging.LogFactory.HashtableImpl</code>)
     * value allows the <code>Hashtable</code> used to store
     * classloaders to be substituted by an alternative implementation.
     * <p>
     * <strong>Note:</strong> <code>LogFactory</code> will print:
     * <pre>
     * [ERROR] LogFactory: Load of custom hashtable failed
     * </pre>
     * to system error and then continue using a standard Hashtable.
     * <p>
     * <strong>Usage:</strong> Set this property when Java is invoked
     * and <code>LogFactory</code> will attempt to load a new instance
     * of the given implementation class.
     * For example, running the following ant scriplet:
     * <pre>
     *  &lt;java classname="${test.runner}" fork="yes" failonerror="${test.failonerror}"&gt;
     *     ...
     *     &lt;sysproperty
     *        key="org.apache.commons.logging.LogFactory.HashtableImpl"
     *        value="org.apache.commons.logging.AltHashtable"/&gt;
     *  &lt;/java&gt;
     * </pre>
     * will mean that <code>LogFactory</code> will load an instance of
     * <code>org.apache.commons.logging.AltHashtable</code>.
     * <p>
     * A typical use case is to allow a custom
     * Hashtable implementation using weak references to be substituted.
     * This will allow classloaders to be garbage collected without
     * the need to release them (on 1.3+ JVMs only, of course ;).
     */
    public static final String HASHTABLE_IMPLEMENTATION_PROPERTY =
        "org.apache.commons.logging.LogFactory.HashtableImpl";

    /**
     * Singleton instance without discovery
     */
    private static LogFactory m_instance = new LogFactory();

    // ----------------------------------------------------------- Constructors

    /**
     * Protected constructor that is not available for public use.
     */
    protected LogFactory() {
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Return the configuration attribute with the specified name (if any),
     * or <code>null</code> if there is no such attribute.
     *
     * @param name Name of the attribute to return
     */
    public Object getAttribute(String name) {
        return null;
    }

    /**
     * Return an array containing the names of all currently defined
     * configuration attributes.  If there are no such attributes, a zero
     * length array is returned.
     */
    public String[] getAttributeNames() {
        return new String[0];
    }

    /**
     * Convenience method to derive a name from the specified class and
     * call <code>getInstance(String)</code> with it.
     *
     * @param clazz Class for which a suitable Log name will be derived
     * @throws LogConfigurationException if a suitable <code>Log</code>
     *  instance cannot be returned
     */
    public Log getInstance(Class clazz)
            throws LogConfigurationException {
        return getInstance(clazz.getName());
    }

    /**
     * Construct (if necessary) and return a <code>Log</code> instance,
     * using the factory's current set of configuration attributes.
     * <p>
     * <strong>NOTE</strong> - Depending upon the implementation of
     * the <code>LogFactory</code> you are using, the <code>Log</code>
     * instance you are returned may or may not be local to the current
     * application, and may or may not be returned again on a subsequent
     * call with the same name argument.
     *
     * <p>In pax-logging, loggers are obtained from current or fallback
     * {@link PaxLoggingManager}</p>
     *
     * @param name Logical name of the <code>Log</code> instance to be
     *  returned (the meaning of this name is only known to the underlying
     *  logging implementation that is being wrapped)
     * @throws LogConfigurationException if a suitable <code>Log</code>
     *  instance cannot be returned
     */
    public Log getInstance(String name)
            throws LogConfigurationException {
        PaxLogger logger;
        if (m_paxLogging == null) {
            logger = FallbackLogFactory.createFallbackLog(null, name);
        } else {
            logger = m_paxLogging.getLogger(name, JclLogger.JCL_FQCN);
        }
        JclLogger jclLogger = new JclLogger(name, logger);
        if (m_paxLogging == null) {
            synchronized (Activator.m_loggers) {
                Activator.m_loggers.put(name, jclLogger);
            }
        }
        return jclLogger;
    }

    /**
     * Release any internal references to previously created {@link Log}
     * instances returned by this factory.  This is useful in environments
     * like servlet containers, which implement application reloading by
     * throwing away a ClassLoader.  Dangling references to objects in that
     * class loader would prevent garbage collection.
     */
    public void release() {
    }

    /**
     * Remove any configuration attribute associated with the specified name.
     * If there is no such attribute, no action is taken.
     *
     * @param name Name of the attribute to remove
     */
    public void removeAttribute(String name) {
    }

    /**
     * Set the configuration attribute with the specified name.  Calling
     * this with a <code>null</code> value is equivalent to calling
     * <code>removeAttribute(name)</code>.
     *
     * @param name Name of the attribute to set
     * @param value Value of the attribute to set, or <code>null</code>
     *  to remove any setting for this attribute
     */
    public void setAttribute(String name, Object value) {
    }

    // --------------------------------------------------------- Static Methods

    /**
     * Opposite to original {@code LogFactory.getFactory}, simply preinstantiated factory is returned.
     * No discovery is performed at all.
     *
     * @throws LogConfigurationException if the implementation class is not
     *  available or cannot be instantiated.
     */
    public static LogFactory getFactory() throws LogConfigurationException {
        return m_instance;
    }

    /**
     * Convenience method to return a named logger, without the application
     * having to care about factories.
     *
     * @param clazz Class from which a log name will be derived
     * @throws LogConfigurationException if a suitable <code>Log</code>
     *  instance cannot be returned
     */
    public static Log getLog(Class clazz) throws LogConfigurationException {
        return getLog(clazz.getName());
    }

    /**
     * Convenience method to return a named logger, without the application
     * having to care about factories.
     *
     * @param name Logical name of the <code>Log</code> instance to be
     *  returned (the meaning of this name is only known to the underlying
     *  logging implementation that is being wrapped)
     * @throws LogConfigurationException if a suitable <code>Log</code>
     *  instance cannot be returned
     */
    public static Log getLog(String name) throws LogConfigurationException {
        return getFactory().getInstance(name);
    }

    /**
     * Release any internal references to previously created {@link LogFactory}
     * instances that have been associated with the specified class loader
     * (if any), after calling the instance method <code>release()</code> on
     * each of them.
     *
     * @param classLoader ClassLoader for which to release the LogFactory
     */
    public static void release(ClassLoader classLoader) {
    }

    /**
     * Release any internal references to previously created {@link LogFactory}
     * instances, after calling the instance method <code>release()</code> on
     * each of them.  This is useful in environments like servlet containers,
     * which implement application reloading by throwing away a ClassLoader.
     * Dangling references to objects in that class loader would prevent
     * garbage collection.
     */
    public static void releaseAll() {
    }

    /**
     * Returns a string that uniquely identifies the specified object, including
     * its class.
     * <p>
     * The returned string is of form "classname@hashcode", ie is the same as
     * the return value of the Object.toString() method, but works even when
     * the specified object's class has overidden the toString method.
     *
     * @param o may be null.
     * @return a string of form classname@hashcode, or "null" if param o is null.
     * @since 1.1
     */
    public static String objectId(Object o) {
        if (o == null) {
            return "null";
        } else {
            return o.getClass().getName() + "@" + System.identityHashCode(o);
        }
    }

}
