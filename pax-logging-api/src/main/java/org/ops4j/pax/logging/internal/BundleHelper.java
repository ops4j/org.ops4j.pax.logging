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

public final class BundleHelper
{
    private BundleHelper()
    {
    }

    private static final SecurityManagerEx securityManager;
    private static final Method getBundleMethod;

    static
    {
        securityManager = new SecurityManagerEx();
        Method mth = null;
        try
        {
            mth = FrameworkUtil.class.getMethod("getBundle", new Class[] { Class.class });
        }
        catch (NoSuchMethodException e)
        {
        }
        getBundleMethod = mth;
    }

    public static Bundle getCallerBundle(Bundle defaultBundle)
    {
        if (getBundleMethod == null) {
            return defaultBundle;
        }

        try
        {
            Class[] classCtx = securityManager.getClassContext();
            /* Skip first 2 classes on call stack since:
             *  classCtx[0] is always SecurityManagerEx.getClassContext()
             *  classCtx[1] is always BundleHelper.getCallerBundle()
             */
            Bundle curBundle = null;
            for (int i = 2; i < classCtx.length; i++)
            {
                Bundle bundle = FrameworkUtil.getBundle(classCtx[i]);
                if (bundle == null)
                {
                    return defaultBundle;
                }
                else if (curBundle == null)
                {
                    curBundle = bundle;
                }
                else if (bundle != curBundle)
                {
                    return bundle;
                }
            }
        }
        catch (Exception e)
        {
        }
        
        return defaultBundle;
    }

    static class SecurityManagerEx extends SecurityManager
    {
        public Class[] getClassContext()
        {
            return super.getClassContext();
        }
    }

}
