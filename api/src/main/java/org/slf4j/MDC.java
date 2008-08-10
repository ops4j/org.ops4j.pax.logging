/*
 * Copyright (c) 2004-2007 QOS.ch
 * All rights reserved.
 *
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 *
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 *
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.slf4j;

import org.ops4j.pax.logging.OSGIPaxLoggingManager;
import org.ops4j.pax.logging.PaxContext;
import org.ops4j.pax.logging.PaxLoggingManager;
import org.osgi.framework.BundleContext;

/**
 * This class hides and serves as a substitute for the underlying logging
 * system's MDC implementation.
 *
 * <p>
 * If the underlying logging system offers MDC functionality, then SLF4J's MDC,
 * i.e. this class, will delegate to the underlying system's MDC. Note that at
 * this time, only two logging systems, namely log4j and logback, offer MDC
 * functionality. If the undelying system does not support MDC, then SLF4J will
 * silently drop MDC information.
 *
 * <p>
 * Thus, as a SLF4J user, you can take advantage of MDC in the presence of log4j
 * or logback, but without forcing log4j or logback as dependencies upon your
 * users.
 *
 * <p>
 * For more information on MDC please see the <a
 * href="http://logback.qos.ch/manual/mdc.html">chapter on MDC</a> in the
 * logback manual.
 *
 * <p>
 * Please note that all methods in this class are static.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @since 1.4.1
 */
public class MDC {
   private static PaxContext m_context;
   private static PaxContext m_defaultContext = new PaxContext();
       
   private static PaxLoggingManager m_paxLogging;
   public static void setBundleContext( BundleContext ctx )
   {
       m_paxLogging = new OSGIPaxLoggingManager( ctx );
       // We need to instruct all loggers to ensure the SimplePaxLoggingManager is replaced.
       m_paxLogging.open();
   }

   /**
     * For all the methods that operate against the context, return true if the MDC should use the PaxContext object ffrom the PaxLoggingManager,
     * or if the logging manager is not set, or does not have its context available yet, use a default context local to this MDC.
     * @return true if the MDC should use the PaxContext object ffrom the PaxLoggingManager,
     * or if the logging manager is not set, or does not have its context available yet, use a default context local to this MDC.
     */
   private static boolean setContext(){
       if( m_context==null && m_paxLogging!=null ){
           m_context=(m_paxLogging.getPaxLoggingService()!=null)?m_paxLogging.getPaxLoggingService().getPaxContext():null;
       }
       return m_context!=null;
   } 
  /**
   * Put a context value (the <code>val</code> parameter) as identified with
   * the <code>key</code> parameter into the current thread's context map.
   * The <code>key</code> parameter cannot be null. The code>val</code> parameter
   * can be null only if the underlying implementation supports it.
   *
   * <p>
   * This method delegates all work to the MDC of the underlying logging system.
   *
   * @throws IllegalArgumentException in case the "key" parameter is null
   */
  public static void put(String key, String val) throws IllegalArgumentException {
    if (key == null) {
      throw new IllegalArgumentException("key parameter cannot be null");
    }
    if(setContext()){
        m_context.put(key, val);
    }else{
        m_defaultContext.put(key, val);
    }
  }

  /**
   * Get the context identified by the <code>key</code> parameter. The
   * <code>key</code> parameter cannot be null.
   *
   * <p>This method delegates all work to the MDC of the underlying logging system.
   *
   * @return the string value identified by the <code>key</code> parameter.
   * @throws IllegalArgumentException in case the "key" parameter is null
   */
  public static String get(String key) throws IllegalArgumentException {
    if (key == null) {
      throw new IllegalArgumentException("key parameter cannot be null");
    }
    if(setContext()){
        return m_context.get(key);
    }else{
        return m_defaultContext.get(key);
    }
  }

  /**
   * Remove the the context identified by the <code>key</code> parameter using
   * the underlying system's MDC implementation. The  <code>key</code> parameter
   * cannot be null. This method does nothing if there is no previous value
   * associated with <code>key</code>.
   *
   * @throws IllegalArgumentException in case the "key" parameter is null
   */
  public static void remove(String key) throws IllegalArgumentException {
    if (key == null) {
      throw new IllegalArgumentException("key parameter cannot be null");
    }
    if(setContext()){
        m_context.remove(key);
    }else{
        m_defaultContext.remove(key);
    }
  }

  /**
   * Clear all entries in the MDC of the underlying implementation.
   */
  public static void clear() {
     if(setContext()){
         m_context.clear();
     }else{
         m_defaultContext.clear();
     }
  }
}