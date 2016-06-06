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

package org.apache.log4j.receivers.net;

import org.apache.log4j.Logger;
import org.apache.log4j.component.helpers.Constants;
import org.apache.log4j.component.plugins.Pauseable;
import org.apache.log4j.component.plugins.Receiver;
import org.apache.log4j.component.spi.ComponentBase;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.LoggingEvent;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


// Contributors:  Moses Hohman <mmhohman@rainbow.uchicago.edu>

/**
   Read {@link LoggingEvent} objects sent from a remote client using
   Sockets (TCP). These logging events are logged according to local
   policy, as if they were generated locally.

   <p>For example, the socket node might decide to log events to a
   local file and also resent them to a second socket node.

    Implementation lifted from org.apache.log4j.net.SocketNode
    in log4j 1.3 and renamed to prevent collision with
    log4j 1.2 implementation.

    @author  Ceki G&uuml;lc&uuml;
    @author  Paul Smith (psmith@apache.org)


*/
public class SocketNode13 extends ComponentBase implements Runnable, Pauseable {

    /**
     * Paused state.
     */
  private boolean paused;
    /**
     * Closed state.
     */
  private boolean closed;
    /**
     * Socket.
     */
  private Socket socket;
    /**
     * Receiver.
     */
  private Receiver receiver;
    /**
     * List of listeners.
     */
  private List listenerList = Collections.synchronizedList(new ArrayList());



  /**
    Constructor for socket and logger repository.
   @param s socket
   @param hierarchy logger repository
   */
  public SocketNode13(final Socket s,
                    final LoggerRepository hierarchy) {
    super();
    this.socket = s;
    this.repository = hierarchy;
  }

  /**
    Constructor for socket and receiver.
   @param s socket
   @param r receiver
   */
  public SocketNode13(final Socket s, final Receiver r) {
    super();
    this.socket = s;
    this.receiver = r;
  }

  /**
   * Set the event listener on this node.
   *
   * @deprecated Now supports mutliple listeners, this method
   * simply invokes the removeSocketNodeEventListener() to remove
   * the listener, and then readds it.
   * @param l listener
   */
  public void setListener(final SocketNodeEventListener l) {
    removeSocketNodeEventListener(l);
    addSocketNodeEventListener(l);
  }

  /**
   * Adds the listener to the list of listeners to be notified of the
   * respective event.
   * @param listener the listener to add to the list
   */
  public void addSocketNodeEventListener(
          final SocketNodeEventListener listener) {
    listenerList.add(listener);
  }

  /**
   * Removes the registered Listener from this instances list of
   * listeners.  If the listener has not been registered, then invoking
   * this method has no effect.
   *
   * @param listener the SocketNodeEventListener to remove
   */
  public void removeSocketNodeEventListener(
          final SocketNodeEventListener listener) {
    listenerList.remove(listener);
  }


    /**
     * Deserialize events from socket until interrupted.
     */
  public void run() {
    LoggingEvent event;
    Logger remoteLogger;
    Exception listenerException = null;
    ObjectInputStream ois = null;

    try {
      ois =
        new ObjectInputStream(
          new BufferedInputStream(socket.getInputStream()));
    } catch (Exception e) {
      ois = null;
      listenerException = e;
      getLogger().error("Exception opening ObjectInputStream to " + socket, e);
    }

    if (ois != null) {

      String hostName = socket.getInetAddress().getHostName();
      String remoteInfo = hostName + ":" + socket.getPort();

      /**
       * notify the listener that the socket has been
       * opened and this SocketNode is ready and waiting
       */
      fireSocketOpened(remoteInfo);

      try {
        while (!isClosed()) {
          // read an event from the wire
          event = (LoggingEvent) ois.readObject();
          event.setProperty(Constants.HOSTNAME_KEY, hostName);
          // store the known remote info in an event property
          event.setProperty("log4j.remoteSourceInfo", remoteInfo);

          // if configured with a receiver, tell it to post the event
          if (!isPaused() && !isClosed()) {
            if ((receiver != null)) {
              receiver.doPost(event);

              // else post it via the hierarchy
            } else {
              // get a logger from the hierarchy. The name of the logger
              // is taken to be the name contained in the event.
              remoteLogger = repository.getLogger(event.getLoggerName());

              //event.logger = remoteLogger;
              // apply the logger-level filter
              if (event
                .getLevel()
                .isGreaterOrEqual(remoteLogger.getEffectiveLevel())) {
                // finally log the event as if was generated locally
                remoteLogger.callAppenders(event);
              }
            }
          } else {
            //we simply discard this event.
          }
        }
      } catch (java.io.EOFException e) {
        getLogger().info("Caught java.io.EOFException closing connection.");
        listenerException = e;
      } catch (java.net.SocketException e) {
        getLogger().info("Caught java.net.SocketException closing connection.");
        listenerException = e;
      } catch (IOException e) {
        getLogger().info("Caught java.io.IOException: " + e);
        getLogger().info("Closing connection.");
        listenerException = e;
      } catch (Exception e) {
        getLogger().error("Unexpected exception. Closing connection.", e);
        listenerException = e;
      }
    }

    // close the socket
    try {
      if (ois != null) {
        ois.close();
      }
    } catch (Exception e) {
      //getLogger().info("Could not close connection.", e);
    }

    // send event to listener, if configured
    if (!listenerList.isEmpty() && !isClosed()) {
      fireSocketClosedEvent(listenerException);
    }
  }

  /**
   * Notifies all registered listeners regarding the closing of the Socket.
   * @param listenerException listener exception
   */
  private void fireSocketClosedEvent(final Exception listenerException) {
    synchronized (listenerList) {
        for (Iterator iter = listenerList.iterator(); iter.hasNext();) {
            SocketNodeEventListener snel =
                    (SocketNodeEventListener) iter.next();
            if (snel != null) {
                snel.socketClosedEvent(listenerException);
            }
        }
    }
  }

  /**
   * Notifies all registered listeners regarding the opening of a Socket.
   * @param remoteInfo remote info
   */
  private void fireSocketOpened(final String remoteInfo) {
    synchronized (listenerList) {
        for (Iterator iter = listenerList.iterator(); iter.hasNext();) {
            SocketNodeEventListener snel =
                    (SocketNodeEventListener) iter.next();
            if (snel != null) {
                snel.socketOpened(remoteInfo);
            }
        }
    }
  }

    /**
     * Sets if node is paused.
     * @param b new value
     */
  public void setPaused(final boolean b) {
    this.paused = b;
  }

    /**
     * Get if node is paused.
     * @return true if pause.
     */
  public boolean isPaused() {
    return this.paused;
  }

    /**
     * Close the node and underlying socket
     */
  public void close() throws IOException {
    getLogger().debug("closing socket");
    this.closed = true;
    socket.close();
    fireSocketClosedEvent(null);
  }
  
    /**
     * Get if node is closed.
     * @return true if closed.
     */
  public boolean isClosed() {
    return this.closed;
  }
}
