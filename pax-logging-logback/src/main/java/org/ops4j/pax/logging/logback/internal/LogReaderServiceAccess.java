package org.ops4j.pax.logging.logback.internal;

import org.osgi.service.log.LogEntry;

/**
 * Back-door API for logging service to interact with the log reader service
 * @author Chris Dolan
 */
interface LogReaderServiceAccess {
    /**
     * Sets the max number of entries that should be allowed in the LogReader buffer.
     *
     * @param maxSize the maximum number of entries in the LogReader buffer.
     */
   void setMaxEntries( int maxSize );
   void fireEvent( LogEntry entry );
}
