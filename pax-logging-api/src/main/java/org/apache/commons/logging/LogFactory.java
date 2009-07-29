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

/* NOTE!!!!  This is NOT the original Jakarta Commons Logging, but an adaption
 of its interface so that this Log4J OSGi bundle can export the JCL interface
 but redirect to the Log4J implementation. There is nothing here that is useful
 outside an OSGi environment.
 */

package org.apache.commons.logging;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import org.apache.commons.logging.internal.JclLogger;
import org.osgi.framework.BundleContext;
import org.ops4j.pax.logging.DefaultServiceLog;
import org.ops4j.pax.logging.OSGIPaxLoggingManager;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingManager;

/**
 * This is an adaptation of the Jakarta Commons Logging API for OSGi usage.
 * <p>
 * The client code that wishes to use this adaptation of Jakarta Commons Logging
 * and have the log output to be directed to the Pax Logging Service backend,
 * which is driven by Log4J, it is necessary to;
 * <ul>
 * <li>ensure that commons-logging.jar is <b>NOT</b> included in your bundle
 * jar.</li>
 * <li>include the pax-logging-client.jar into the client bundle.</li>
 * <li>update your Manifest.MF to import the org.ops4j.pax.logging package.</li>
 * <li>Initiate this class by setting the Bundle Context.</li>
 * </ul>
 * Typical code looks like this; <code><pre>
 * import org.apache.commons.logging.LogFactory;
 * import org.apache.commons.logging.Log;
 *
 * public class Activator
 *         implements BundleActivator
 * {
 *     public void start( BundleContext context ) throws Exception
 *     {
 *         LogFactory.getFactory().setBundleContext( context );
 *     }
 * }
 * </pre></code>
 * </p>
 *
 * @author Niclas Hedhman (responsible for the OSGi adaptation.)
 * @author Craig R. McClanahan
 * @author Costin Manolache
 * @author Richard A. Sitze
 */

public class LogFactory
{

    /**
     * ps4j/pax/logging/providers The name of the property used to identify the
     * LogFactory implementation class name.
     */
    public static final String FACTORY_PROPERTY = "org.apache.commons.logging.LogFactory";

    /**
     * The fully qualified class name of the fallback <code>LogFactory</code>
     * implementation class to use, if no other can be found.
     */
    public static final String FACTORY_DEFAULT = "org.apache.commons.logging.impl.LogFactoryImpl";

    /**
     * The name of the properties file to search for.
     */
    public static final String FACTORY_PROPERTIES = "commons-logging.properties";

    private static LogFactory m_instance;

    private static PaxLoggingManager m_paxLogging;

    private static Map m_loggers;

    static
    {
        m_instance = new LogFactory();
        m_loggers = Collections.synchronizedMap( new WeakHashMap() );
    }

    public static void setBundleContext( BundleContext ctx )
    {
        m_paxLogging = new OSGIPaxLoggingManager( ctx );

        Set entrySet = m_loggers.entrySet();
        Iterator iterator = entrySet.iterator();
        while( iterator.hasNext() )
        {
            Map.Entry entry = (Entry) iterator.next();
            JclLogger logger = (JclLogger) entry.getKey();
            String name = (String) entry.getValue();
            logger.setPaxLoggingManager( m_paxLogging, name );
        }
        m_paxLogging.open();
    }

    /**
     * @return the LogFactory instance to use.
     *
     * @throws LogConfigurationException if the implementation class is not available or cannot be
     *                                   instantiated.
     */
    public static LogFactory getFactory()
        throws LogConfigurationException
    {
        return m_instance;
    }

    /**
     * Convenience method to return a named logger, without the application
     * having to care about factories.
     *
     * @param clazz Class from which a log name will be derived
     *
     * @return the Log instance to use for the class
     *
     * @throws LogConfigurationException if a suitable <code>Log</code> instance cannot be returned
     */
    public static Log getLog( Class clazz )
        throws LogConfigurationException
    {
        return getLog( clazz.getName() );
    }

    /**
     * Convenience method to return a named logger, without the application
     * having to care about factories.
     *
     * @param name Logical name of the <code>Log</code> instance to be returned
     *             (the meaning of this name is only known to the underlying
     *             logging implementation that is being wrapped)
     *
     * @return the Log instance to use for the class of the given name
     *
     * @throws LogConfigurationException if a suitable <code>Log</code> instance cannot be returned
     */
    public static Log getLog( String name )
        throws LogConfigurationException
    {
        return getFactory().getInstance( name );
    }

    /**
     * Release any internal references to previously created {@link LogFactory}
     * instances that have been associated with the specified class loader (if
     * any), after calling the instance method <code>release()</code> on each
     * of them.
     *
     * @param classLoader ClassLoader for which to release the LogFactory
     */
    public static void release( ClassLoader classLoader )
    {
    }

    /**
     * Release any internal references to previously created {@link LogFactory}
     * instances, after calling the instance method <code>release()</code> on
     * each of them. This is useful in environments like servlet containers,
     * which implement application reloading by throwing away a ClassLoader.
     * Dangling references to objects in that class loader would prevent garbage
     * collection.
     */
    public static void releaseAll()
    {
        release();
    }

    private LogFactory()
    {
    }

    /**
     * Return the configuration attribute with the specified name (if any), or
     * <code>null</code> if there is no such attribute.
     *
     * @param name Name of the attribute to return
     *
     * @return always return null. This method is not supported in Pax Logging.
     */
    public Object getAttribute( String name )
    {
        return null;
    }

    /**
     * Return an array containing the names of all currently defined
     * configuration attributes. If there are no such attributes, a zero length
     * array is returned.
     *
     * @return always returns an emtpy String array. This method is not supported in Pax Logging.
     */
    public String[] getAttributeNames()
    {
        return new String[0];
    }

    /**
     * Convenience method to derive a name from the specified class and call
     * <code>getInstance(String)</code> with it.
     *
     * @param clazz Class for which a suitable Log name will be derived
     *
     * @return the Log instance to use for the given class.
     *
     * @throws LogConfigurationException if a suitable <code>Log</code> instance cannot be returned
     */
    public Log getInstance( Class clazz )
        throws LogConfigurationException
    {
        return getInstance( clazz.getName() );
    }

    /**
     * <p>
     * Construct (if necessary) and return a <code>Log</code> instance, using
     * the factory's current set of configuration attributes.
     * </p>
     *
     * <p>
     * <strong>NOTE</strong> - Depending upon the implementation of the
     * <code>LogFactory</code> you are using, the <code>Log</code> instance
     * you are returned may or may not be local to the current application, and
     * may or may not be returned again on a subsequent call with the same name
     * argument.
     * </p>
     *
     * @param name Logical name of the <code>Log</code> instance to be returned
     *             (the meaning of this name is only known to the underlying
     *             logging implementation that is being wrapped)
     *
     * @return the Log instance of the class with the given name.
     *
     * @throws LogConfigurationException if a suitable <code>Log</code> instance cannot be returned
     */
    public Log getInstance( String name )
        throws LogConfigurationException
    {
        PaxLogger logger;
        if( m_paxLogging == null )
        {
            logger = new DefaultServiceLog( null, name );
        }
        else
        {
            logger = m_paxLogging.getLogger( name, JclLogger.JCL_FQCN );
        }
        JclLogger jclLogger = new JclLogger( logger );
        m_loggers.put( jclLogger, name );
        return jclLogger;
    }

    /**
     * Release any internal references to previously created {@link Log}
     * instances returned by this factory. This is useful in environments like
     * servlet containers, which implement application reloading by throwing
     * away a ClassLoader. Dangling references to objects in that class loader
     * would prevent garbage collection.
     */
    static public void release()
    {
        m_paxLogging.close();
        m_paxLogging.dispose();
        m_paxLogging = null;
    }

    /**
     * Remove any configuration attribute associated with the specified name. If
     * there is no such attribute, no action is taken.
     *
     * @param name Name of the attribute to remove
     */
    public void removeAttribute( String name )
    {
    }

    /**
     * Set the configuration attribute with the specified name. Calling this
     * with a <code>null</code> value is equivalent to calling
     * <code>removeAttribute(name)</code>.
     *
     * @param name  Name of the attribute to set
     * @param value Value of the attribute to set, or <code>null</code> to
     *              remove any setting for this attribute
     */
    public void setAttribute( String name, Object value )
    {
    }
}
