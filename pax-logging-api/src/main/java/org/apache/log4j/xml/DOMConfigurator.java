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

package org.apache.log4j.xml;

import org.apache.log4j.config.PropertySetter;
import org.apache.log4j.helpers.FileWatchdog;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.spi.Configurator;
import org.apache.log4j.spi.LoggerRepository;
import org.w3c.dom.Element;

import javax.xml.parsers.FactoryConfigurationError;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Properties;

// Contributors:   Mark Womack
//                 Arun Katkere 

/**
 * This class isn't used by pax-logging, but is part of public API. Methods are simply no-op.
 */
public class DOMConfigurator implements Configurator {

  static final String CONFIGURATION_TAG = "log4j:configuration";
  static final String OLD_CONFIGURATION_TAG = "configuration";
  static final String RENDERER_TAG      = "renderer";
  static final String APPENDER_TAG 	= "appender";
  static final String APPENDER_REF_TAG 	= "appender-ref";  
  static final String PARAM_TAG    	= "param";
  static final String LAYOUT_TAG	= "layout";
  static final String CATEGORY		= "category";
  static final String LOGGER		= "logger";
  static final String LOGGER_REF	= "logger-ref";
  static final String CATEGORY_FACTORY_TAG  = "categoryFactory";
  static final String LOGGER_FACTORY_TAG  = "loggerFactory";
  static final String NAME_ATTR		= "name";
  static final String CLASS_ATTR        = "class";
  static final String VALUE_ATTR	= "value";
  static final String ROOT_TAG		= "root";
  static final String ROOT_REF		= "root-ref";
  static final String LEVEL_TAG	        = "level";
  static final String PRIORITY_TAG      = "priority";
  static final String FILTER_TAG	= "filter";
  static final String ERROR_HANDLER_TAG	= "errorHandler";
  static final String REF_ATTR		= "ref";
  static final String ADDITIVITY_ATTR    = "additivity";  
  static final String THRESHOLD_ATTR       = "threshold";
  static final String CONFIG_DEBUG_ATTR  = "configDebug";
  static final String INTERNAL_DEBUG_ATTR  = "debug";
  static final String RENDERING_CLASS_ATTR = "renderingClass";
  static final String RENDERED_CLASS_ATTR = "renderedClass";

  static final String EMPTY_STR = "";
  static final Class[] ONE_STRING_PARAM = new Class[] {String.class};

  final static String dbfKey = "javax.xml.parsers.DocumentBuilderFactory";

  /**
     No argument constructor.
  */
  public
  DOMConfigurator () { 
  }


  /**
     Configure log4j using a <code>configuration</code> element as
     defined in the log4j.dtd. 

  */
  static
  public
  void configure (Element element) {
  }

 /**
     Like {@link #configureAndWatch(String, long)} except that the
     default delay as defined by {@link FileWatchdog#DEFAULT_DELAY} is
     used. 

     @param configFilename A log4j configuration file in XML format.

  */
  static
  public
  void configureAndWatch(String configFilename) {
  }

  /**
     Read the configuration file <code>configFilename</code> if it
     exists. Moreover, a thread will be created that will periodically
     check if <code>configFilename</code> has been created or
     modified. The period is determined by the <code>delay</code>
     argument. If a change or file creation is detected, then
     <code>configFilename</code> is read to configure log4j.  

      @param configFilename A log4j configuration file in XML format.
      @param delay The delay in milliseconds to wait between each check.
  */
  static
  public
  void configureAndWatch(String configFilename, long delay) {
  }
  
  public
  void doConfigure(final String filename, LoggerRepository repository) {
  }
  

  public
  void doConfigure(final URL url, LoggerRepository repository) {
  }

  /**
     Configure log4j by reading in a log4j.dtd compliant XML
     configuration file.

  */
  public
  void doConfigure(final InputStream inputStream, LoggerRepository repository) 
                                          throws FactoryConfigurationError {
  }

  /**
     Configure log4j by reading in a log4j.dtd compliant XML
     configuration file.

  */
  public
  void doConfigure(final Reader reader, LoggerRepository repository) 
                                          throws FactoryConfigurationError {
  }

  /**
     Configure by taking in an DOM element. 
  */
  public void doConfigure(Element element, LoggerRepository repository) {
  }

  
  /**
     A static version of {@link #doConfigure(String, LoggerRepository)}.  */
  static
  public
  void configure(String filename) throws FactoryConfigurationError {
  }

  /**
     A static version of {@link #doConfigure(URL, LoggerRepository)}.
   */
  static
  public
  void configure(URL url) throws FactoryConfigurationError {
  }

    /**
     * Substitutes property value for any references in expression.
     *
     * @param value value from configuration file, may contain
     *              literal text, property references or both
     * @param props properties.
     * @return evaluated expression, may still contain expressions
     *         if unable to expand.
     * @since 1.2.15
     */
    public static String subst(final String value, final Properties props) {
        try {
            return OptionConverter.substVars(value, props);
        } catch (IllegalArgumentException e) {
            LogLog.warn("Could not perform variable substitution.", e);
            return value;
        }
    }


    /**
     * Sets a parameter based from configuration file content.
     *
     * @param elem       param element, may not be null.
     * @param propSetter property setter, may not be null.
     * @param props      properties
     * @since 1.2.15
     */
    public static void setParameter(final Element elem,
                                    final PropertySetter propSetter,
                                    final Properties props) {
        throw new UnsupportedOperationException("Operation not supported in pax-logging");
    }

    /**
     * Creates an object and processes any nested param elements
     * but does not call activateOptions.  If the class also supports
     * UnrecognizedElementParser, the parseUnrecognizedElement method
     * will be call for any child elements other than param.
     *
     * @param element       element, may not be null.
     * @param props         properties
     * @param expectedClass interface or class expected to be implemented
     *                      by created class
     * @return created class or null.
     * @throws Exception thrown if the contain object should be abandoned.
     * @since 1.2.15
     */
    public static Object parseElement(final Element element,
                                             final Properties props,
                                             final Class expectedClass) throws Exception {
        throw new UnsupportedOperationException("Operation not supported in pax-logging");
    }

}
