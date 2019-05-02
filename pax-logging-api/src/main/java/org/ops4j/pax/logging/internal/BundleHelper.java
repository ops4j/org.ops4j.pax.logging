/*
 * Copyright 2010 Guillaume Nodet.
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
package org.ops4j.pax.logging.internal;

import java.lang.reflect.Method;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public final class BundleHelper {

    private BundleHelper() {
    }

    private static final SecurityManagerEx securityManager;
    private static final Method getBundleMethod;

    static {
        securityManager = new SecurityManagerEx();
        Method mth = null;
        try {
            mth = FrameworkUtil.class.getMethod("getBundle", Class.class);
        } catch (NoSuchMethodException ignored) {
        }
        getBundleMethod = mth;
    }

    /**
     * <p>Gets a {@link Bundle} invoking logging method which is the first bundle different than
     * pax-logging-api. If no bundle can't be found, {@code defaultBundle} is returned.</p>
     * <p>This method analyzes class context top down (skipping two topmost classes, as they're known to
     * come from pax-logging-api bundle). Also, {@code java.util.logging} packages are skipped.</p>
     *
     * @param defaultBundle
     * @return
     */
    public static Bundle getCallerBundle(Bundle defaultBundle) {
        if (getBundleMethod == null) {
            return defaultBundle;
        }

        try {
            Class[] classCtx = securityManager.getClassContext();
            /*
             * Skip first 2 classes on call stack since:
             *  0 = "class org.ops4j.pax.logging.internal.BundleHelper$SecurityManagerEx.getClassContext()"
             *  1 = "class org.ops4j.pax.logging.internal.BundleHelper.getCallerBundle()"
             */
            Bundle curBundle = null;
            for (int i = 2; i < classCtx.length; i++) {
                Bundle bundle = FrameworkUtil.getBundle(classCtx[i]);
                if (bundle == null && (classCtx[i].getPackage() == null
                        || !classCtx[i].getPackage().getName().equals("java.util.logging"))) {
                    return defaultBundle;
                } else if (curBundle == null) {
                    curBundle = bundle;
                } else if (bundle != null && bundle != curBundle) {
                    return bundle;
                }
            }
        } catch (Exception ignored) {
        }

        return defaultBundle;
    }

    /**
     * <p>Gets a {@link Bundle} invoking logging method which is the first bundle before the class matching
     * {@code fqcn} argument. Checking is done bottom-up the stack trace. If no bundle can't be found,
     * {@code defaultBundle} is returned.</p>
     *
     * @param defaultBundle
     * @return
     */
    public static Bundle getCallerBundle(Bundle defaultBundle, String fqcn) {
        if (getBundleMethod == null) {
            return defaultBundle;
        }

        try {
            Class[] classCtx = securityManager.getClassContext();
            int previousClass = -1;
            for (int i = classCtx.length - 1; i >= 0; i--) {
                if (previousClass != -1 && classCtx[i].getName().equals(fqcn)) {
                    Bundle bundle = FrameworkUtil.getBundle(classCtx[previousClass]);
                    if (bundle != null) {
                        return bundle;
                    }
                } else {
                    previousClass = i;
                }
            }
        } catch (Exception ignored) {
        }

        return defaultBundle;
    }

    /**
     * {@link SecurityManager} that exposes {@code getClassContext} as public method.
     */
    static class SecurityManagerEx extends SecurityManager {

        public Class[] getClassContext() {
            return super.getClassContext();
        }
    }

}
