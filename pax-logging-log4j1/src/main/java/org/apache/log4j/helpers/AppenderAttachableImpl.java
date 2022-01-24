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

import org.apache.log4j.spi.AppenderAttachable;
import org.apache.log4j.spi.LoggingEvent;

import org.apache.log4j.Appender;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A straightforward implementation of the {@link AppenderAttachable} interface.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @since version 0.9.1
 */
public class AppenderAttachableImpl implements AppenderAttachable {

    /** Array of appenders. */
    protected final CopyOnWriteArrayList<Appender> appenderList = new CopyOnWriteArrayList<Appender>();

    /**
     * Attach an appender. If the appender is already in the list in won't be added
     * again.
     */
    public void addAppender(Appender newAppender) {
	// Null values for newAppender parameter are strictly forbidden.
	if (newAppender == null)
	    return;

        appenderList.addIfAbsent(newAppender);
    }

    /**
     * Call the <code>doAppend</code> method on all attached appenders.
     */
    public int appendLoopOnAppenders(LoggingEvent event) {
        int nb = 0;
        for (Appender appender : appenderList) {
            appender.doAppend(event);
            nb++;
        }
        return nb;
    }

    public void closeAppenders() {
        for (Appender appender : appenderList) {
            appender.close();
        }
    }

    /**
     * Get all attached appenders as an Enumeration. If there are no attached
     * appenders <code>null</code> is returned.
     * 
     * @return Enumeration An enumeration of attached appenders.
     */
    public Enumeration getAllAppenders() {
        return new Enumeration() {
            final Iterator it = appenderList.iterator();

            @Override
            public boolean hasMoreElements() {
                return it.hasNext();
            }

            @Override
            public Object nextElement() {
                return it.next();
            }
        };
    }

    public Iterable<Appender> getAppenders() {
        return appenderList;
    }

    /**
     * Look for an attached appender named as <code>name</code>.
     * 
     * <p>
     * Return the appender with that name if in the list. Return null otherwise.
     * 
     */
    public Appender getAppender(String name) {
        if (name == null)
            return null;

        for (Appender appender : appenderList) {
            if (name.equals(appender.getName()))
                return appender;
        }
        return null;
    }

    /**
     * Returns <code>true</code> if the specified appender is in the list of
     * attached appenders, <code>false</code> otherwise.
     * 
     * @since 1.2
     */
    public boolean isAttached(Appender appender) {
        if (appender == null)
            return false;

        for (Appender a : appenderList) {
            if (a == appender)
                return true;
        }
        return false;
    }

    /**
     * Remove and close all previously attached appenders.
     */
    public void removeAllAppenders() {
        for (Appender a : appenderList) {
            a.close();
        }
        appenderList.clear();
    }

    /**
     * Remove the appender passed as parameter form the list of attached appenders.
     */
    public void removeAppender(Appender appender) {
        if (appender == null)
            return;
        appenderList.remove(appender);
    }

    /**
     * Remove the appender with the name passed as parameter form the list of
     * appenders.
     */
    public void removeAppender(String name) {
        if (name == null) return;
        for (Iterator<Appender> it = appenderList.iterator(); it.hasNext(); ) {
            if (name.equals(it.next().getName())) {
                it.remove();
                break;
            }
        }
    }

}
