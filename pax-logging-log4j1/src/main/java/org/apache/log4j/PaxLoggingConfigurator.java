/*
 * Copyright 2006 Niclas Hedhman.
 * Copyright 2012 Guillaume Nodet.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.log4j;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.config.PaxPropertySetter;
import org.apache.log4j.config.PropertySetter;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.spi.AppenderAttachable;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.OptionHandler;
import org.ops4j.pax.logging.log4j1.internal.bridges.AppenderBridgeImpl;
import org.ops4j.pax.logging.log4j1.internal.bridges.ErrorHandlerBridgeImpl;
import org.ops4j.pax.logging.log4j1.internal.bridges.FilterBridgeImpl;
import org.ops4j.pax.logging.log4j1.internal.bridges.LayoutBridgeImpl;
import org.ops4j.pax.logging.spi.support.PaxAppenderProxy;
import org.osgi.framework.BundleContext;

/**
 * <p>OSGi-specific {@link PropertyConfigurator} that can parse {@code log4j.properties} file and
 * dynamically reference some log4j <em>services</em> (appenders, layouts, error handlers, filters) from OSGi
 * registry.</p>
 *
 * @since 2007-10-26
 */
public class PaxLoggingConfigurator extends PropertyConfigurator {

    public static final String OSGI_PREFIX = "osgi:";

    // these are private in super class, so have to be repeat here
    private static final String LOGGER_REF = "logger-ref";
    private static final String ROOT_REF = "root-ref";
    private static final String APPENDER_REF_TAG = "appender-ref";

    private LoggerRepository repository;
    private BundleContext m_bundleContext;
    private List<PaxAppenderProxy> proxies = new ArrayList<>();

    public PaxLoggingConfigurator(BundleContext bundleContext) {
        m_bundleContext = bundleContext;
    }

    public List<PaxAppenderProxy> getProxies() {
        return proxies;
    }

    @Override
    public void doConfigure(Properties properties, LoggerRepository hierarchy) {
        repository = hierarchy;

        // super.doConfigure will do this:
        // - handle global threshold
        // - configure root logger (log4j.rootLogger)
        // - configure loggers (with appenders) and renderers (log4j.logger.* , log4j.renderer.*)
        // pax-logging overrides some methods to alter the parsing procedure
        super.doConfigure(properties, hierarchy);
    }

    /**
     * <p>pax-logging adds proxies (that resolve to OSGi-registry services) for components
     * (like appenders) if the names of the components start with {@link PaxLoggingConfigurator#OSGI_PREFIX osgi:}</p>
     * <p>Superclass version is not easily extensible (see: template method pattern), so we had to copy it
     * and adjust.</p>
     *
     * @param props
     * @param appenderName
     * @return
     */
    @Override
    Appender parseAppender(Properties props, String appenderName) {
        Appender appender = registryGet(appenderName);
        if (appender != null) {
            LogLog.debug("Appender \"" + appenderName + "\" was already parsed.");
            return appender;
        }
        if (appenderName.startsWith(OSGI_PREFIX)) {
            // pax-logging-log4j1: appender is not a class name - it's part of OSGi service filter.
            String osgiAppenderName = appenderName.substring(OSGI_PREFIX.length());
            PaxAppenderProxy paxAppender = new PaxAppenderProxy(m_bundleContext, osgiAppenderName);
            proxies.add(paxAppender);
            appender = new AppenderBridgeImpl(paxAppender);
            appender.setName(appenderName);
        } else {
            // Appender was not previously initialized.
            String prefix = APPENDER_PREFIX + appenderName;
            String layoutPrefix = prefix + ".layout";

            appender = (Appender) OptionConverter.instantiateByKey(props, prefix,
                    Appender.class,
                    null);
            if (appender == null) {
                LogLog.error("Could not instantiate appender named \"" + appenderName + "\".");
                return null;
            }
            appender.setName(appenderName);

            if (appender instanceof OptionHandler) {
                if (appender.requiresLayout()) {
                    String layoutClass = OptionConverter.findAndSubst(layoutPrefix, props);
                    if (layoutClass != null && layoutClass.startsWith(OSGI_PREFIX)) {
                        // pax-logging-log4j1: layout is not a class name - it's part of OSGi service filter.
                        String osgiLayoutName = layoutClass.substring(OSGI_PREFIX.length());
                        Layout fallback = null;
                        // pax-logging-log4j1: log4j.appender.appenderName.layout.fallback may specify fallback class for layout
                        // and log4j.appender.appenderName.layout.fallback.option[1-N] may specify its properties
                        String fallbackClass = OptionConverter.findAndSubst(layoutPrefix + ".fallback", props);
                        if (fallbackClass != null) {
                            fallback = (Layout) OptionConverter.instantiateByKey(
                                    props, layoutPrefix + ".fallback", Layout.class, null);
                            if (fallback != null) {
                                PaxPropertySetter.setProperties(fallback, props, layoutPrefix + ".fallback.");
                            }
                        }
                        Layout layout = new LayoutBridgeImpl(m_bundleContext, osgiLayoutName, fallback);
                        appender.setLayout(layout);
                    } else {
                        Layout layout = (Layout) OptionConverter.instantiateByKey(props,
                                layoutPrefix,
                                Layout.class,
                                null);
                        if (layout != null) {
                            appender.setLayout(layout);
                            LogLog.debug("Parsing layout options for \"" + appenderName + "\".");
                            //configureOptionHandler(layout, layoutPrefix + ".", props);
                            PaxPropertySetter.setProperties(layout, props, layoutPrefix + ".");
                            LogLog.debug("End of parsing for \"" + appenderName + "\".");
                        }
                    }
                }
                final String errorHandlerPrefix = prefix + ".errorhandler";
                String errorHandlerClass = OptionConverter.findAndSubst(errorHandlerPrefix, props);
                if (errorHandlerClass != null && errorHandlerClass.startsWith(OSGI_PREFIX)) {
                    // pax-logging-log4j1: error handler is not a class name - it's part of OSGi service filter.
                    String errorHandlerName = errorHandlerClass.substring(OSGI_PREFIX.length());
                    ErrorHandler fallback = null;
                    // pax-logging-log4j1: log4j.appender.appenderName.errorhandler.fallback may specify fallback class for error handler
                    // and log4j.appender.appenderName.errorhandler.fallback.option[1-N] may specify its properties
                    String fallbackClass = OptionConverter.findAndSubst(errorHandlerPrefix + ".fallback", props);
                    if (fallbackClass != null) {
                        fallback = (ErrorHandler) OptionConverter.instantiateByKey(
                                props, errorHandlerPrefix + ".fallback", ErrorHandler.class, null);
                        if (fallback != null) {
                            parseErrorHandler(fallback, errorHandlerPrefix + ".fallback", props, repository);
                        }
                    }
                    ErrorHandler eh = new ErrorHandlerBridgeImpl(m_bundleContext, errorHandlerName, fallback);
                    appender.setErrorHandler(eh);
                } else if (errorHandlerClass != null) {
                    ErrorHandler eh = (ErrorHandler) OptionConverter.instantiateByKey(props,
                            errorHandlerPrefix,
                            ErrorHandler.class,
                            null);
                    if (eh != null) {
                        appender.setErrorHandler(eh);
                        LogLog.debug("Parsing errorhandler options for \"" + appenderName + "\".");
                        parseErrorHandler(eh, errorHandlerPrefix, props, repository);
                        LogLog.debug("End of errorhandler parsing for \"" + appenderName + "\".");
                    }

                }
                if (appender instanceof AppenderAttachable) {
                    // this fragment is not part of original log4j:log4j and I can't track the reason why
                    // it was added...
                    final String appenderPrefix = prefix + ".appenders";
                    String appenderNames = OptionConverter.findAndSubst(appenderPrefix, props);
                    StringTokenizer st = new StringTokenizer(appenderNames, ", ");
                    Appender childAppender;
                    String childAppenderName;
                    while (st.hasMoreTokens()) {
                        childAppenderName = st.nextToken().trim();
                        LogLog.debug("Parsing appender named \"" + childAppenderName + "\".");
                        childAppender = parseAppender(props, childAppenderName);
                        if (childAppender != null) {
                            ((AppenderAttachable) appender).addAppender(childAppender);
                        }
                    }
                }
                //configureOptionHandler((OptionHandler) appender, prefix + ".", props);
                PaxPropertySetter.setProperties(appender, props, prefix + ".");
                LogLog.debug("Parsed \"" + appenderName + "\" options.");
            }
        }
        parseAppenderFilters(props, appenderName, appender);
        registryPut(appender);
        return appender;
    }

    private void parseErrorHandler(
            final ErrorHandler eh,
            final String errorHandlerPrefix,
            final Properties props,
            final LoggerRepository hierarchy) {
        boolean rootRef = OptionConverter.toBoolean(
                OptionConverter.findAndSubst(errorHandlerPrefix + ROOT_REF, props), false);
        if (rootRef) {
            eh.setLogger(hierarchy.getRootLogger());
        }
        String loggerName = OptionConverter.findAndSubst(errorHandlerPrefix + LOGGER_REF, props);
        if (loggerName != null) {
            Logger logger = (loggerFactory == null) ? hierarchy.getLogger(loggerName)
                    : hierarchy.getLogger(loggerName, loggerFactory);
            eh.setLogger(logger);
        }
        String appenderName = OptionConverter.findAndSubst(errorHandlerPrefix + APPENDER_REF_TAG, props);
        if (appenderName != null) {
            Appender backup = parseAppender(props, appenderName);
            if (backup != null) {
                eh.setBackupAppender(backup);
            }
        }
        final Properties edited = new Properties();
        final String[] keys = new String[] {
                errorHandlerPrefix + "." + ROOT_REF,
                errorHandlerPrefix + "." + LOGGER_REF,
                errorHandlerPrefix + "." + APPENDER_REF_TAG
        };
        for (Iterator iter = props.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry entry = (Map.Entry) iter.next();
            int i = 0;
            for (; i < keys.length; i++) {
                if (keys[i].equals(entry.getKey())) break;
            }
            if (i == keys.length) {
                edited.put(entry.getKey(), entry.getValue());
            }
        }
        PaxPropertySetter.setProperties(eh, edited, errorHandlerPrefix + ".");
    }

    @Override
    void parseAppenderFilters(Properties props, String appenderName, Appender appender) {
        // extract filters and filter options from props into a hashtable mapping
        // the property name defining the filter class to a list of pre-parsed
        // name-value pairs associated to that filter
        final String filterPrefix = APPENDER_PREFIX + appenderName + ".filter.";
        int fIdx = filterPrefix.length();
        Hashtable<String, Vector<NameValue>> filters = new Hashtable<>();
        Enumeration e = props.keys();
        String name = "";
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            if (key.startsWith(filterPrefix)) {
                int dotIdx = key.indexOf('.', fIdx);
                String filterKey = key;
                if (dotIdx != -1) {
                    filterKey = key.substring(0, dotIdx);
                    name = key.substring(dotIdx + 1);
                }
                Vector<NameValue> filterOpts = filters.get(filterKey);
                if (filterOpts == null) {
                    filterOpts = new Vector<>();
                    filters.put(filterKey, filterOpts);
                }
                if (dotIdx != -1) {
                    String value = OptionConverter.findAndSubst(key, props);
                    filterOpts.add(new NameValue(name, value));
                }
            }
        }

        // sort filters by IDs, insantiate filters, set filter options,
        // add filters to the appender
        Enumeration g = new SortedKeyEnumeration(filters);
        while (g.hasMoreElements()) {
            String key = (String) g.nextElement();
            String clazz = props.getProperty(key);
            if (clazz != null && clazz.startsWith(OSGI_PREFIX)) {
                // pax-logging-log4j1: filter is not a class name - it's part of OSGi service filter.
                String filterName = clazz.substring(OSGI_PREFIX.length());
                Filter fallback = null;
                // pax-logging-log4j1: log4j.appender.appenderName.filter.fallback may specify fallback class for filter
                // and log4j.appender.appenderName.filter.fallback.option[1-N] may specify its properties
                String fallbackClass = OptionConverter.findAndSubst(key + ".fallback", props);
                if (fallbackClass != null) {
                    fallback = (Filter) OptionConverter.instantiateByKey(
                            props, key + ".fallback", Filter.class, null);
                    if (fallback != null) {
                        PaxPropertySetter.setProperties(fallback, props, key + ".fallback.");
                    }
                }
                Filter filter = new FilterBridgeImpl(m_bundleContext, filterName, fallback);
                appender.addFilter(filter);
            } else if (clazz != null) {
                LogLog.debug("Filter key: [" + key + "] class: [" + props.getProperty(key) + "] props: " + filters.get(key));
                Filter filter = (Filter) OptionConverter.instantiateByClassName(clazz, Filter.class, null);
                if (filter != null) {
                    PropertySetter propSetter = new PropertySetter(filter);
                    Vector<NameValue> v = filters.get(key);
                    Enumeration<NameValue> filterProps = v.elements();
                    while (filterProps.hasMoreElements()) {
                        NameValue kv = filterProps.nextElement();
                        propSetter.setProperty(kv.key, kv.value);
                    }
                    propSetter.activate();
                    LogLog.debug("Adding filter of type [" + filter.getClass()
                            + "] to appender named [" + appender.getName() + "].");
                    appender.addFilter(filter);
                }
            } else {
                LogLog.warn("Missing class definition for filter: [" + key + "]");
            }
        }
    }

}
