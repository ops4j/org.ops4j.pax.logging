/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ops4j.pax.logging.spi.support;

import org.osgi.framework.ServiceReference;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

/**
 * Extension of Slf4J's class that helps with messages containing placeholders {@code {}}.
 *
 * OSGi R7 logging adds {@link org.osgi.service.log.Logger} interface with methods that accept formatting arguments.
 * These may be directly passed through Slf4J methods, or into Log4J2. This class helps with missing cases.
 *
 * It's a <em>triple</em> because it holds optional {@link Throwable}, {@link ServiceReference} and arguments array.
 */
public class FormattingTriple {

    private final FormattingTuple argsAndThrowable;
    private ServiceReference<?> serviceReference;

    private FormattingTriple(String message, Object[] argArray, Throwable throwable, ServiceReference<?> reference) {
        argsAndThrowable = new FormattingTuple(message, argArray, throwable);
        serviceReference = reference;
    }

    /**
     * According to OSGi R7 Logging specification, argument array may contain {@link Throwable} and/or
     * {@link ServiceReference} among last two arguments. This methods returns extracted information.
     * @param format
     * @param printfFormatting
     * @param argArray
     * @return
     */
    public static FormattingTriple discover(String format, boolean printfFormatting, Object... argArray) {
        return forArguments(format, printfFormatting, false, argArray);
    }

    /**
     * According to OSGi R7 Logging specification, argument array may contain {@link Throwable} and/or
     * {@link ServiceReference} among last two arguments. This methods returns extracted information, also, message
     * is immediately resolved using discovered arguments.
     * @param format
     * @param printfFormatting
     * @param argArray
     * @return
     */
    public static FormattingTriple resolve(String format, boolean printfFormatting, Object... argArray) {
        return forArguments(format, printfFormatting, true, argArray);
    }

    /**
     * According to OSGi R7 Logging specification, argument array may contain {@link Throwable} and/or
     * {@link ServiceReference} among last two arguments. This methods returns extracted information.
     * @param format
     * @param printfFormatting
     * @param resolve whether to replace format+arguments into formatted message
     * @param argArray
     * @return
     */
    private static FormattingTriple forArguments(String format, boolean printfFormatting, boolean resolve, Object... argArray) {
        if (argArray == null || argArray.length == 0) {
            // only message
            return new FormattingTriple(format, null, null, null);
        } else if (argArray.length == 1) {
            if (argArray[0] != null && Throwable.class.isAssignableFrom(argArray[0].getClass())) {
                return new FormattingTriple(format, null, (Throwable) argArray[0], null);
            }
            if (argArray[0] != null && ServiceReference.class.isAssignableFrom(argArray[0].getClass())) {
                return new FormattingTriple(format, null, null, (ServiceReference<?>) argArray[0]);
            }
            // format is one-argument format
            if (printfFormatting) {
                String m = resolve ? String.format(format, argArray) : format;
                return new FormattingTriple(m, argArray, null, null);
            } else {
                String m = resolve ? MessageFormatter.arrayFormat(format, argArray, null).getMessage() : format;
                return new FormattingTriple(m, argArray, null, null);
            }
        }

        // more than one argument
        int i1 = argArray.length - 1;
        int i2 = argArray.length - 2;

        Throwable t = null;
        ServiceReference<?> sr = null;

        if (argArray[i1] != null) {
            if (Throwable.class.isAssignableFrom(argArray[i1].getClass())) {
                t = (Throwable) argArray[i1];
            } else if (ServiceReference.class.isAssignableFrom(argArray[i1].getClass())) {
                sr = (ServiceReference<?>) argArray[i1];
            }
        }
        if (argArray[i2] != null && (t != null || sr != null)) {
            if (sr != null && Throwable.class.isAssignableFrom(argArray[i2].getClass())) {
                t = (Throwable) argArray[i2];
            } else if (t != null && ServiceReference.class.isAssignableFrom(argArray[i2].getClass())) {
                sr = (ServiceReference<?>) argArray[i2];
            }
        }

        int toCopy = argArray.length;
        if (t != null) {
            toCopy--;
        }
        if (sr != null) {
            toCopy--;
        }
        Object[] newArgs = new Object[toCopy];
        System.arraycopy(argArray, 0, newArgs, 0, toCopy);

        if (printfFormatting) {
            String m = resolve ? String.format(format, newArgs) : format;
            return new FormattingTriple(m, newArgs, t, sr);
        } else {
            String m = resolve ? MessageFormatter.arrayFormat(format, newArgs, t).getMessage() : format;
            return new FormattingTriple(m, newArgs, t, sr);
        }
    }

    public String getMessage() {
        return argsAndThrowable.getMessage();
    }

    public Object[] getArgArray() {
        return argsAndThrowable.getArgArray();
    }

    public Throwable getThrowable() {
        return argsAndThrowable.getThrowable();
    }

    public ServiceReference<?> getServiceReference() {
        return serviceReference;
    }

}
