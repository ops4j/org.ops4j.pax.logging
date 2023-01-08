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

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.config.PropertySetter;
import org.apache.log4j.helpers.FileWatchdog;
import org.apache.log4j.helpers.Loader;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.or.RendererMap;
import org.apache.log4j.spi.AppenderAttachable;
import org.apache.log4j.spi.Configurator;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.RendererSupport;
import org.apache.log4j.spi.ThrowableRenderer;
import org.apache.log4j.spi.ThrowableRendererSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.Reader;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;
import java.util.Properties;

// Contributors:   Mark Womack
//                 Arun Katkere 

/**
 * Use this class to initialize the log4j environment using a DOM tree.
 *
 * <p>
 * The DTD is specified in <a href="doc-files/log4j.dtd"><b>log4j.dtd</b></a>.
 *
 * <p>
 * Sometimes it is useful to see how log4j is reading configuration files. You can enable log4j internal logging by
 * defining the <b>log4j.debug</b> variable on the java command line. Alternatively, set the <code>debug</code>
 * attribute in the <code>log4j:configuration</code> element. As in
 *
 * <pre>
 * &lt;log4j:configuration <b>debug="true"</b> xmlns:log4j="http://jakarta.apache.org/log4j/"&gt;
 * ...
 * &lt;/log4j:configuration&gt;
 * </pre>
 *
 * <p>
 * There are sample XML files included in the package.
 *
 * @author Christopher Taylor
 * @author Ceki G&uuml;lc&uuml;
 * @author Anders Kristensen
 * @since 0.8.3
 */
public class DOMConfigurator implements Configurator {

    static final String CONFIGURATION_TAG = "log4j:configuration";
    static final String OLD_CONFIGURATION_TAG = "configuration";
    static final String RENDERER_TAG = "renderer";
    static final String APPENDER_TAG = "appender";
    static final String APPENDER_REF_TAG = "appender-ref";
    static final String PARAM_TAG = "param";
    static final String LAYOUT_TAG = "layout";
    static final String CATEGORY = "category";
    static final String LOGGER = "logger";
    static final String LOGGER_REF = "logger-ref";
    static final String CATEGORY_FACTORY_TAG = "categoryFactory";
    static final String LOGGER_FACTORY_TAG = "loggerFactory";
    static final String NAME_ATTR = "name";
    static final String CLASS_ATTR = "class";
    static final String VALUE_ATTR = "value";
    static final String ROOT_TAG = "root";
    static final String ROOT_REF = "root-ref";
    static final String LEVEL_TAG = "level";
    static final String PRIORITY_TAG = "priority";
    static final String FILTER_TAG = "filter";
    static final String ERROR_HANDLER_TAG = "errorHandler";
    static final String REF_ATTR = "ref";
    static final String ADDITIVITY_ATTR = "additivity";
    static final String THRESHOLD_ATTR = "threshold";
    static final String CONFIG_DEBUG_ATTR = "configDebug";
    static final String INTERNAL_DEBUG_ATTR = "debug";
    static final String RENDERING_CLASS_ATTR = "renderingClass";
    static final String RENDERED_CLASS_ATTR = "renderedClass";

    static final String EMPTY_STR = "";
    static final Class[] ONE_STRING_PARAM = new Class[] { String.class };

    final static String dbfKey = "javax.xml.parsers.DocumentBuilderFactory";

    // key: appenderName, value: appender
    Hashtable appenderBag;

    Properties props;
    LoggerRepository repository;

    protected LoggerFactory catFactory = null;

    /**
     * No argument constructor.
     */
    public DOMConfigurator() {
        appenderBag = new Hashtable();
    }

    /**
     * Used internally to parse appenders by IDREF name.
     */
    protected Appender findAppenderByName(Document doc, String appenderName) {
        Appender appender = (Appender) appenderBag.get(appenderName);

        if (appender != null) {
            return appender;
        } else {
            // Doesn't work on DOM Level 1 :
            // Element element = doc.getElementById(appenderName);

            // Endre's hack:
            Element element = null;
            NodeList list = doc.getElementsByTagName("appender");
            for (int t = 0; t < list.getLength(); t++) {
                Node node = list.item(t);
                NamedNodeMap map = node.getAttributes();
                Node attrNode = map.getNamedItem("name");
                if (appenderName.equals(attrNode.getNodeValue())) {
                    element = (Element) node;
                    break;
                }
            }
            // Hack finished.

            if (element == null) {
                LogLog.error("No appender named [" + appenderName + "] could be found.");
                return null;
            } else {
                appender = parseAppender(element);
                if (appender != null) {
                    appenderBag.put(appenderName, appender);
                }
                return appender;
            }
        }
    }

    /**
     * Used internally to parse appenders by IDREF element.
     */
    protected Appender findAppenderByReference(Element appenderRef) {
        String appenderName = subst(appenderRef.getAttribute(REF_ATTR));
        Document doc = appenderRef.getOwnerDocument();
        return findAppenderByName(doc, appenderName);
    }

    /**
     * Used internally to parse an appender element.
     */
    protected Appender parseAppender(Element appenderElement) {
        return null;
    }

    /**
     * Used internally to parse an {@link ErrorHandler} element.
     */
    protected void parseErrorHandler(Element element, Appender appender) {
    }

    /**
     * Used internally to parse a filter element.
     */
    protected void parseFilters(Element element, Appender appender) {
    }

    /**
     * Used internally to parse an category element.
     */
    protected void parseCategory(Element loggerElement) {
    }

    /**
     * Used internally to parse the category factory element.
     */
    protected void parseCategoryFactory(Element factoryElement) {
    }

    /**
     * Used internally to parse the roor category element.
     */
    protected void parseRoot(Element rootElement) {
    }

    /**
     * Used internally to parse the children of a category element.
     */
    protected void parseChildrenOfLoggerElement(Element catElement, Logger cat, boolean isRoot) {
    }

    /**
     * Used internally to parse a layout element.
     */
    protected Layout parseLayout(Element layout_element) {
        return null;
    }

    protected void parseRenderer(Element element) {
    }

    /**
     * Parses throwable renderer.
     *
     * @param element throwableRenderer element.
     * @return configured throwable renderer.
     * @since 1.2.16.
     */
    protected ThrowableRenderer parseThrowableRenderer(final Element element) {
        return null;
    }

    /**
     * Used internally to parse a level element.
     */
    protected void parseLevel(Element element, Logger logger, boolean isRoot) {
    }

    protected void setParameter(Element elem, PropertySetter propSetter) {
    }

    /**
     * Configure log4j using a <code>configuration</code> element as defined in the log4j.dtd.
     */
    static public void configure(Element element) {
    }

    /**
     * Like {@link #configureAndWatch(String, long)} except that the default delay as defined by
     * {@link FileWatchdog#DEFAULT_DELAY} is used.
     *
     * @param configFilename A log4j configuration file in XML format.
     */
    static public void configureAndWatch(String configFilename) {
    }

    /**
     * Read the configuration file <code>configFilename</code> if it exists. Moreover, a thread will be created that
     * will periodically check if
     * <code>configFilename</code> has been created or modified. The period is
     * determined by the <code>delay</code> argument. If a change or file creation is detected, then
     * <code>configFilename</code> is read to configure log4j.
     *
     * @param configFilename A log4j configuration file in XML format.
     * @param delay          The delay in milliseconds to wait between each check.
     */
    static public void configureAndWatch(String configFilename, long delay) {
    }

    public void doConfigure(final String filename, LoggerRepository repository) {
    }

    public void doConfigure(final URL url, LoggerRepository repository) {
    }

    /**
     * Configure log4j by reading in a log4j.dtd compliant XML configuration file.
     */
    public void doConfigure(final InputStream inputStream, LoggerRepository repository)
            throws FactoryConfigurationError {
    }

    /**
     * Configure log4j by reading in a log4j.dtd compliant XML configuration file.
     */
    public void doConfigure(final Reader reader, LoggerRepository repository) throws FactoryConfigurationError {
    }

    /**
     * Configure log4j by reading in a log4j.dtd compliant XML configuration file.
     */
    protected void doConfigure(final InputSource inputSource, LoggerRepository repository)
            throws FactoryConfigurationError {
    }

    /**
     * Configure by taking in an DOM element.
     */
    public void doConfigure(Element element, LoggerRepository repository) {
    }

    /**
     * A static version of {@link #doConfigure(String, LoggerRepository)}.
     */
    static public void configure(String filename) throws FactoryConfigurationError {
    }

    /**
     * A static version of {@link #doConfigure(URL, LoggerRepository)}.
     */
    static public void configure(URL url) throws FactoryConfigurationError {
    }

    /**
     * Used internally to configure the log4j framework by parsing a DOM tree of XML elements based on <a
     * href="doc-files/log4j.dtd">log4j.dtd</a>.
     */
    protected void parse(Element element) {
    }

    protected String subst(final String value) {
        return value;
    }

    /**
     * Substitutes property value for any references in expression.
     *
     * @param value value from configuration file, may contain literal text, property references or both
     * @param props properties.
     * @return evaluated expression, may still contain expressions if unable to expand.
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
    public static void setParameter(final Element elem, final PropertySetter propSetter, final Properties props) {
        String name = subst(elem.getAttribute("name"), props);
        String value = (elem.getAttribute("value"));
        value = subst(OptionConverter.convertSpecialChars(value), props);
        propSetter.setProperty(name, value);
    }

    /**
     * Creates an object and processes any nested param elements but does not call activateOptions. If the class also
     * supports UnrecognizedElementParser, the parseUnrecognizedElement method will be call for any child elements other
     * than param.
     *
     * @param element       element, may not be null.
     * @param props         properties
     * @param expectedClass interface or class expected to be implemented by created class
     * @return created class or null.
     * @throws Exception thrown if the contain object should be abandoned.
     * @since 1.2.15
     */
    public static Object parseElement(final Element element, final Properties props, final Class expectedClass)
            throws Exception {
        return null;
    }

}

class XMLWatchdog extends FileWatchdog {

    XMLWatchdog(String filename) {
        super(filename);
    }

    /**
     * Call {@link DOMConfigurator#configure(String)} with the <code>filename</code> to reconfigure log4j.
     */
    public void doOnChange() {
    }
}
