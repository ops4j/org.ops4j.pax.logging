package org.ops4j.pax.logging.logback.internal;

import org.ops4j.pax.logging.spi.PaxLocationInfo;

/**
 * This is a a simple facade to translate Logback location data to its Pax equivalent.
 * @author Chris Dolan
 * @since 6/14/11 10:13 AM
 */
public class PaxLocationInfoForLogback implements PaxLocationInfo {
    private final StackTraceElement caller;

    public PaxLocationInfoForLogback(StackTraceElement[] callerData) {
        caller = callerData == null || callerData.length == 0 ? null : callerData[0];
    }

    public String getFileName() {
        if (caller != null) {
            String fileName = caller.getFileName();
            if (fileName != null)
                return fileName;
        }
        return "?";
    }

    public String getClassName() {
        if (caller != null) {
            String className = caller.getClassName();
            if (className != null)
                return className;
        }
        return "?";
    }

    public String getMethodName() {
        if (caller != null) {
            String methodName = caller.getMethodName();
            if (methodName != null)
                return methodName;
        }
        return "?";
    }

    public String getLineNumber() {
        if (caller != null) {
            int line = caller.getLineNumber();
            if (line > 0)
                return Integer.toString(line);
        }
        return "?";
    }
}
