/*
 * Copyright 2011 Guillaume Nodet.
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
package org.ops4j.pax.logging.util;

import java.lang.reflect.InvocationTargetException;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleReference;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;

public class OsgiUtil {

    
    private static final int OSGI_1_0 = 10;
    private static final int OSGI_1_1 = 11;
    private static final int OSGI_1_2 = 12;
    private static final int OSGI_1_3 = 13;
    private static final int OSGI_1_4 = 14;
    private static final int OSGI_1_5 = 15;
    private static final int OSGI_1_6 = 16;
    
    private static final int osgiVersion;
    
    static {
        //
        // The org.osgi.framework package has had no code change from 1.1 to 1.2
        // so this version is not detected
        //
        int version;
        try {
            Bundle.class.getMethod("getDataFile", new Class[] { String.class });
            version = OSGI_1_6;
        } catch (Throwable t1) {
            try {
                Bundle.class.getMethod("getVersion", new Class[] { });
                version = OSGI_1_5;
            } catch (Throwable t2) {
                try {
                    Bundle.class.getMethod("getBundleContext", new Class[] { });
                    version = OSGI_1_4;
                } catch (Throwable t3) {
                    try {
                        Bundle.class.getMethod("getLastModified", new Class[] { });
                        version = OSGI_1_3;
                    } catch (Throwable t4) {
                        try {
                            Constants.class.getField("DYNAMICIMPORT_PACKAGE");
                            version = OSGI_1_2;
                        } catch (Throwable t5) {
                            try {
                                Bundle.class.getMethod("getResource", new Class[] { String.class });
                                version = OSGI_1_1;
                            } catch (Throwable t6) {
                                version = OSGI_1_0;
                            }
                        }
                    }
                }
            }
        }
        osgiVersion = version;
    }

    public static String getBundleSymbolicName(Bundle bundle) {
        if (osgiVersion >= OSGI_1_3) {
            return bundle.getSymbolicName();
        } else {
            return (String) bundle.getHeaders().get(Constants.BUNDLE_SYMBOLICNAME);
        }
    }
    
    public static String getVersion(Bundle bundle) {
        if (osgiVersion >= OSGI_1_5) {
            return bundle.getVersion().toString();
        }
          return (String) bundle.getHeaders().get(Constants.BUNDLE_VERSION);
    }

    public static Class loadClass(ClassLoader loader, String className) throws ClassNotFoundException {
        if (osgiVersion >= OSGI_1_5 && !checkValidLoader(loader)) {
            throw new ClassNotFoundException(className);
        }
        return loader.loadClass(className);
    }

    private static boolean checkValidLoader(ClassLoader loader) throws ClassNotFoundException {
        if (loader instanceof BundleReference) {
            Bundle b = ((BundleReference) loader).getBundle();
            if (b == null || b.getState() == Bundle.INSTALLED || b.getState() == Bundle.UNINSTALLED) {
                return false;
            }
            // If the bundle has dynamic imports, do not try to load from it
            // as it could cause a resolution and lead to deadlocks
            if (b.getHeaders().get(Constants.DYNAMICIMPORT_PACKAGE) != null) {
                return false;
            }
        }
        return true;
    }

    public static Bundle getBundleOrNull(Class cls) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (osgiVersion >= OSGI_1_5) {
            return FrameworkUtil.getBundle(cls);
        }
        return null;
    }

}
