package org.ops4j.pax.logging.logback.internal;

import edu.umd.cs.findbugs.annotations.Nullable;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

/**
 * @author Chris Dolan
 * @since 9/6/11 1:08 PM
 */
public interface PaxEventHandler {
    void handleEvents( Bundle bundle, @Nullable ServiceReference sr, int level, String message, Throwable exception );
}
