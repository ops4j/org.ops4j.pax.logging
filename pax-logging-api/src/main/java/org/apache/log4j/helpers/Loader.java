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

package org.apache.log4j.helpers;

import java.net.URL;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.io.InterruptedIOException;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * Load resources (or images) from various sources.
 *
 * @author Ceki G&uuml;lc&uuml;
 */

public class Loader {

    static final String TSTR = "Caught Exception while in Loader.getResource. This may be innocuous.";

    static private boolean ignoreTCL = false;

    /**
     * Get a resource by delegating to getResource(String).
     *
     * @param resource resource name
     * @param clazz    class, ignored.
     * @return URL to resource or null.
     * @deprecated as of 1.2.
     */
    public static URL getResource(String resource, Class clazz) {
        return getResource(resource);
    }

    /**
     * This method will search for <code>resource</code> in different places. The search order is as follows:
     * <ol>
     * <li><p>Search for <code>resource</code> using the thread context class loader,
     * unless "log4j.ignoreTCL" system property was set to true.</p>
     * </li>
     * <li><p>Search for <code>resource</code> using the class
     * loader that loaded this class (<code>Loader</code>).<p>
     * </li>
     * <li>In Pax Logging, {@link org.osgi.framework.Bundle#getResource(String)} is first checked</li>
     * <li>Try one last time with
     * <code>ClassLoader.getSystemResource(resource)</code>, that is is using the
     * system class loader.
     * </ol>
     *
     *  <p>Nota bene: In versions of reload4j 1.2.23 and earlier, the jaadoc documentation stated that
     *  the thread context class loader was used but when running under JDK 9 and later this
     *  was <b>not</b> actually the case. As of version 1.2.25, the javadoc corresponds to the original
     *  intention as documented.
     *  </p>
     *
     *
     * @param resource the resource to load
     */
    static public URL getResource(String resource) {
        Bundle bundle = FrameworkUtil.getBundle(Loader.class);
        if (bundle != null) {
            LogLog.debug("Trying to find [" + resource + "] using " + bundle.getSymbolicName() + "/" + bundle.getVersion() + " bundle.");
            URL url = bundle.getResource(resource);
            if (url != null) {
                return url;
            }
        }

        try {
            // unless intsructed to ignore the TCL, try getting the resource using TCL, return if found.
            if(!ignoreTCL) {
                URL url0 = innerGetResource(resource, getTCL());
                if(url0 != null) { return url0; }
            }

            // if we were instructed to ignore TCL or if no url was dound, try using the
            // class loader that loaded this class.
            URL url = innerGetResource(resource,  Loader.class.getClassLoader());
            if(url != null) { return url; }

        } catch (SecurityException t) {
            // can't be InterruptedException or InterruptedIOException
            // since not declared, must be error or RuntimeError.
            LogLog.warn(TSTR, t);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        // Last ditch attempt: get the resource from the class path. It
        // may be the case that clazz was loaded by the Extentsion class
        // loader which the parent of the system class loader. Hence the
        // code below.
        LogLog.debug("Trying to find [" + resource + "] using ClassLoader.getSystemResource().");
        return ClassLoader.getSystemResource(resource);
    }

    private static URL innerGetResource(String resource, ClassLoader classLoader) {
        if (classLoader != null) {
           LogLog.debug("Trying to find [" + resource + "] using " + classLoader + " class loader.");
           return classLoader.getResource(resource);
        } else {
           return null;
        }
    }

    /**
     * Are we running under JDK 1.x?
     * @deprecated with no replacement
     */
    @Deprecated
    public static boolean isJava1() {
        return false;
    }

    /**
     * Get the Thread Context Loader which is a JDK 1.2 feature. If we are running under JDK 1.1 or anything else goes
     * wrong the method returns
     * <code>null<code>.
     */
    private static ClassLoader getTCL() throws IllegalAccessException, InvocationTargetException {

        // Are we running on a JDK 1.2 or later system?
        Method method = null;
        try {
            method = Thread.class.getMethod("getContextClassLoader", null);
        } catch (NoSuchMethodException e) {
            // We are running on JDK 1.1
            return null;
        }

        return (ClassLoader) method.invoke(Thread.currentThread(), null);
    }

    /**
     * Load the specified class using the {@linl Class#forName} method.
     *
     * <p>Nota bene: In versions of reload4j 1.2.23 and earlier, the documentation stated that
     * the thread context class loader was used to load the specified class but
     * when running under JDK 9 and later this was <b>not</b> actually the case. As of version 1.2.24,
     * the javadoc above matches the code as executed.
     * </p>
     *
     * In pax-logging, TCCL is always checked first, then {@link Bundle#loadClass(String)}
     * eventually {@link Class#forName(String)} is called.
     *
     * @param clazz the name of class to load
     */
    static public Class loadClass(String clazz) throws ClassNotFoundException {
        if (Thread.currentThread().getContextClassLoader() != null) {
            try {
                return Thread.currentThread().getContextClassLoader().loadClass(clazz);
            } catch (Exception ignored) {
            }
        }

        Bundle bundle = FrameworkUtil.getBundle(Loader.class);
        if (bundle != null) {
            try {
                return bundle.loadClass(clazz);
            } catch (Exception ignored) {
            }
        }
        return Class.forName(clazz);
    }
}
