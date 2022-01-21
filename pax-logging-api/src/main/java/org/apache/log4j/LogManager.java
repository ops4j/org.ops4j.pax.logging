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

package org.apache.log4j;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;

import org.apache.log4j.spi.LoggerFactory;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.RepositorySelector;

/**
 * <p>In pax-logging-api, this class is part of Log4j1 API, but it doesn't
 * perform any discovery. It can be used to access some internal information
 * from Log4j1.
 *
 * <p>Use the <code>LogManager</code> class to retreive {@link Logger}
 * instances or to operate on the current {@link
 * LoggerRepository}. When the <code>LogManager</code> class is loaded
 * into memory the default initalzation procedure is inititated. The
 * default initialization procedure is described in the <a
 * href="../../../../manual.html#defaultInit">short log4j manual</a>.
 *
 * @author Ceki G&uuml;lc&uuml; */
public class LogManager {

  /**
   * @deprecated This variable is for internal use only. It will
   * become package protected in future versions.
   * */
  static public final String DEFAULT_CONFIGURATION_FILE = "log4j.properties";
  
  static final String DEFAULT_XML_CONFIGURATION_FILE = "log4j.xml";  
   
  /**
   * @deprecated This variable is for internal use only. It will
   * become private in future versions.
   * */
  static final public String DEFAULT_CONFIGURATION_KEY="log4j.configuration";

  /**
   * @deprecated This variable is for internal use only. It will
   * become private in future versions.
   * */
  static final public String CONFIGURATOR_CLASS_KEY="log4j.configuratorClass";

  /**
  * @deprecated This variable is for internal use only. It will
  * become private in future versions.
  */
  public static final String DEFAULT_INIT_OVERRIDE_KEY = 
                                                 "log4j.defaultInitOverride";

  /**
     Sets <code>LoggerFactory</code> but only if the correct
     <em>guard</em> is passed as parameter.
     
     <p>Initally the guard is null.  If the guard is
     <code>null</code>, then invoking this method sets the logger
     factory and the guard. Following invocations will throw a {@link
     IllegalArgumentException}, unless the previously set
     <code>guard</code> is passed as the second parameter.

     <p>This allows a high-level component to set the {@link
  RepositorySelector} used by the <code>LogManager</code>.
     
     <p>For example, when tomcat starts it will be able to install its
     own repository selector. However, if and when Tomcat is embedded
     within JBoss, then JBoss will install its own repository selector
     and Tomcat will use the repository selector set by its container,
     JBoss.  */
  static
  public
  void setRepositorySelector(RepositorySelector selector, Object guard)
                                                 throws IllegalArgumentException {
    throw new UnsupportedOperationException("Operation not supported in pax-logging");
  }


    /**
     * This method tests if called from a method that
     * is known to result in class members being abnormally
     * set to null but is assumed to be harmless since the
     * all classes are in the process of being unloaded.
     *
     * @param ex exception used to determine calling stack.
     * @return true if calling stack is recognized as likely safe.
     */
  private static boolean isLikelySafeScenario(final Exception ex) {
      StringWriter stringWriter = new StringWriter();
      ex.printStackTrace(new PrintWriter(stringWriter));
      String msg = stringWriter.toString();
      return msg.indexOf("org.apache.catalina.loader.WebappClassLoader.stop") != -1;
  }

  static
  public LoggerRepository getLoggerRepository() {
    throw new UnsupportedOperationException("Operation not supported in pax-logging");
  }

  /**
     Retrieve the appropriate root logger.
   */
  public
  static Logger getRootLogger() {
     // Delegate the actual manufacturing of the logger to the logger factory managed by pax-web.
    return Logger.getRootLogger();
  }

  /**
     Retrieve the appropriate {@link Logger} instance.
  */
  public
  static Logger getLogger(final String name) {
     // Delegate the actual manufacturing of the logger to the logger factory managed by pax-web.
    return Logger.getLogger(name);
  }

 /**
     Retrieve the appropriate {@link Logger} instance.
  */
  public
  static Logger getLogger(final Class clazz) {
     // Delegate the actual manufacturing of the logger to the logger factory managed by pax-web.
    return Logger.getLogger(clazz);
  }


  /**
     Retrieve the appropriate {@link Logger} instance.
  */
  public
  static Logger getLogger(final String name, final LoggerFactory factory) {
     // Delegate the actual manufacturing of the logger to the logger factory managed by pax-web.
    return Logger.getLogger(name);
  }

  public
  static Logger exists(final String name) {
    throw new UnsupportedOperationException("Operation not supported in pax-logging");
  }

  public
  static
  Enumeration getCurrentLoggers() {
    throw new UnsupportedOperationException("Operation not supported in pax-logging");
  }

  public
  static
  void shutdown() {
  }

  public
  static
  void resetConfiguration() {
  }
}

