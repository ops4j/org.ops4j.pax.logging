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

import org.ops4j.pax.logging.providers.DefaultLogProvider;
import org.ops4j.pax.logging.providers.LogProvider;
import org.osgi.framework.BundleContext;

/**
* This is an adaptation of the Jakarta Commons Logging API for
* OSGi usage.
* <p>
* The LogFactory, LogConfigurationException and Log classes are exported from
* the Commons-Logging bundle, and will be provided to the bundles that import
* the commons-logging classes. And this class will either bind to a plain OSGi
* LogService, or if the Log4J service is detected, it will use that one
* instead.
* </p>
* <p>
* The bundle that uses this library to get Jakarta Commons Logging to bind to
* any of the supported backend logging systems, must import these classes
* and also enable the proper usage in its bundle activator. Typical code looks
* like this;
* <code><pre>
* import org.apache.commons.logging.LogFactory;
* import org.apache.commons.logging.Log;
*
* import org.ops4j.pax.logging.providers.LogProvider;
* import org.ops4j.pax.logging.providers.Log4JProvider;
*
* public class Activator
*     implements BundleActivator
* {
*     public void start( BundleContext context )
*         throws Exception
*     {
*         LogFactory factory = LogFactory.getInstance();
*         factory.setBundleContext( context );
*         Log4JProvider provider = new Log4JProvider( context );
*         factory.setProvider( provider );
*     }
*
*     public void stop( BundleContext context )
*     {
*         LogFactory factory = LogFactory.getInstance();
*         factory.release();
*     }
* }
* </pre></code>
* </p>
* <p>Factory for creating {@link Log} instances, with discovery and
* configuration features similar to that employed by standard Java APIs
* such as JAXP.</p>
*
* @author Niclas Hedhman (responsible for the OSGi adaptation.)
* @author Craig R. McClanahan
* @author Costin Manolache
* @author Richard A. Sitze
* @version $Revision: 1.27 $ $Date: 2004/06/06 21:15:12 $
*/

public class LogFactory
{
    /**ps4j/pax/logging/providers
     * The name of the property used to identify the LogFactory implementation
     * class name.
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

    private static LogFactory m_Instance;

    private BundleContext  m_BundleContext;
    private LogProvider    m_LogProvider;

    static
    {
        m_Instance = new LogFactory();
    }

    public void setBundleContext( BundleContext context )
    {
        if( m_BundleContext != null )
        {
            return;
        }
        m_BundleContext = context;
    }

    public void setLogProvider( LogProvider provider )
    {
        m_LogProvider = provider;
    }

    /**
     * @exception LogConfigurationException if the implementation class is not
     *  available or cannot be instantiated.
     */
    public static LogFactory getFactory()
        throws LogConfigurationException
    {
        return m_Instance;
    }

    /**
     * Convenience method to return a named logger, without the application
     * having to care about factories.
     *
     * @param clazz Class from which a log name will be derived
     *
     * @exception LogConfigurationException if a suitable <code>Log</code>
     *  instance cannot be returned
     */
    public static Log getLog(Class clazz)
        throws LogConfigurationException
    {
        return getFactory().getInstance( clazz.getName() );
    }

    /**
     * Convenience method to return a named logger, without the application
     * having to care about factories.
     *
     * @param name Logical name of the <code>Log</code> instance to be
     *  returned (the meaning of this name is only known to the underlying
     *  logging implementation that is being wrapped)
     *
     * @exception LogConfigurationException if a suitable <code>Log</code>
     *  instance cannot be returned
     */
    public static Log getLog(String name)
        throws LogConfigurationException
    {
        return getFactory().getInstance( name );
    }

    /**
     * Release any internal references to previously created {@link LogFactory}
     * instances that have been associated with the specified class loader
     * (if any), after calling the instance method <code>release()</code> on
     * each of them.
     *
     * @param classLoader ClassLoader for which to release the LogFactory
     */
    public static void release( ClassLoader classLoader )
    {
    }

    /**
     * Release any internal references to previously created {@link LogFactory}
     * instances, after calling the instance method <code>release()</code> on
     * each of them.  This is useful in environments like servlet containers,
     * which implement application reloading by throwing away a ClassLoader.
     * Dangling references to objects in that class loader would prevent
     * garbage collection.
     */
    public static void releaseAll()
    {
    }

    private LogFactory()
    {
        m_LogProvider = new DefaultLogProvider();
    }

    /**
     * Return the configuration attribute with the specified name (if any),
     * or <code>null</code> if there is no such attribute.
     *
     * @param name Name of the attribute to return
     */
    public Object getAttribute(String name)
    {
        return null;
    }


    /**
     * Return an array containing the names of all currently defined
     * configuration attributes.  If there are no such attributes, a zero
     * length array is returned.
     */
    public String[] getAttributeNames()
    {
        return new String[ 0 ];
    }

    /**
     * Convenience method to derive a name from the specified class and
     * call <code>getInstance(String)</code> with it.
     *
     * @param clazz Class for which a suitable Log name will be derived
     *
     * @exception LogConfigurationException if a suitable <code>Log</code>
     *  instance cannot be returned
     */
    public Log getInstance( Class clazz )
        throws LogConfigurationException
    {
        return getInstance( clazz.getName() );
    }

    /**
     * <p>Construct (if necessary) and return a <code>Log</code> instance,
     * using the factory's current set of configuration attributes.</p>
     *
     * <p><strong>NOTE</strong> - Depending upon the implementation of
     * the <code>LogFactory</code> you are using, the <code>Log</code>
     * instance you are returned may or may not be local to the current
     * application, and may or may not be returned again on a subsequent
     * call with the same name argument.</p>
     *
     * @param name Logical name of the <code>Log</code> instance to be
     *  returned (the meaning of this name is only known to the underlying
     *  logging implementation that is being wrapped)
     *
     * @exception LogConfigurationException if a suitable <code>Log</code>
     *  instance cannot be returned
     */
    public Log getInstance( String name )
        throws LogConfigurationException
    {
        if( m_LogProvider == null )
        {
            throw new LogConfigurationException( "A LogProvider has not been set in the LogFactory." );
        }
        return m_LogProvider.getLogger( name );
    }

    /**
     * Release any internal references to previously created {@link Log}
     * instances returned by this factory.  This is useful in environments
     * like servlet containers, which implement application reloading by
     * throwing away a ClassLoader.  Dangling references to objects in that
     * class loader would prevent garbage collection.
     */
    public void release()
    {
        m_LogProvider.release();
    }

    /**
     * Remove any configuration attribute associated with the specified name.
     * If there is no such attribute, no action is taken.
     *
     * @param name Name of the attribute to remove
     */
    public void removeAttribute(String name)
    {
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
    public void setAttribute(String name, Object value)
    {
    }
}

