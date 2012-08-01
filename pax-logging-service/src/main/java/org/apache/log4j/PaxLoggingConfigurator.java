/*
 * Copyright 2006 Niclas Hedhman.
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.log4j.config.PaxPropertySetter;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.spi.AppenderAttachable;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.OptionHandler;
import org.ops4j.pax.logging.service.internal.AppenderBridgeImpl;
import org.ops4j.pax.logging.service.internal.PaxAppenderProxy;
import org.osgi.framework.BundleContext;

public class PaxLoggingConfigurator extends PropertyConfigurator
{

    public static final String OSGI_APPENDER_PREFIX = "osgi:";

    private static final String LOGGER_REF	= "logger-ref";
    private static final String ROOT_REF		= "root-ref";
    private static final String APPENDER_REF_TAG 	= "appender-ref";

    private LoggerRepository repository;
    private BundleContext m_bundleContext;
    private List proxies = new ArrayList();

    public PaxLoggingConfigurator( BundleContext bundleContext )
    {
        m_bundleContext = bundleContext;
    }

    public List getProxies() {
        return proxies;
    }

    public void doConfigure(Properties properties, LoggerRepository hierarchy)
    {
        repository = hierarchy;
        super.doConfigure(properties, hierarchy);
    }

    Appender parseAppender( Properties props, String appenderName )
    {
        Appender appender = registryGet( appenderName );
        if( appender != null )
        {
            LogLog.debug("Appender \"" + appenderName + "\" was already parsed.");
            return appender;
        }
        if( appenderName.startsWith( OSGI_APPENDER_PREFIX ) )
        {
            String osgiAppenderName = appenderName.substring( OSGI_APPENDER_PREFIX.length() );
            PaxAppenderProxy paxAppender = new PaxAppenderProxy( m_bundleContext, osgiAppenderName );
            proxies.add(paxAppender);
            appender = new AppenderBridgeImpl( paxAppender );
            appender.setName(appenderName);
        }
        else
        {
            // Appender was not previously initialized.
            String prefix = APPENDER_PREFIX + appenderName;
            String layoutPrefix = prefix + ".layout";

            appender = (Appender) OptionConverter.instantiateByKey(props, prefix,
                    org.apache.log4j.Appender.class,
                    null);
            if (appender == null)
            {
                LogLog.error("Could not instantiate appender named \"" + appenderName + "\".");
                return null;
            }
            appender.setName(appenderName);

            if (appender instanceof OptionHandler)
            {
                if (appender.requiresLayout())
                {
                    Layout layout = (Layout) OptionConverter.instantiateByKey(props,
                            layoutPrefix,
                            Layout.class,
                            null);
                    if (layout != null)
                    {
                        appender.setLayout(layout);
                        LogLog.debug("Parsing layout options for \"" + appenderName + "\".");
                        //configureOptionHandler(layout, layoutPrefix + ".", props);
                        PaxPropertySetter.setProperties(layout, props, layoutPrefix + ".");
                        LogLog.debug("End of parsing for \"" + appenderName + "\".");
                    }
                }
                final String errorHandlerPrefix = prefix + ".errorhandler";
                String errorHandlerClass = OptionConverter.findAndSubst(errorHandlerPrefix, props);
                if (errorHandlerClass != null)
                {
                      ErrorHandler eh = (ErrorHandler) OptionConverter.instantiateByKey(props,
                                errorHandlerPrefix,
                                ErrorHandler.class,
                                null);
                      if (eh != null)
                      {
                            appender.setErrorHandler(eh);
                            LogLog.debug("Parsing errorhandler options for \"" + appenderName +"\".");
                            parseErrorHandler(eh, errorHandlerPrefix, props, repository);
                            final Properties edited = new Properties();
                            final String[] keys = new String[] {
                                    errorHandlerPrefix + "." + ROOT_REF,
                                    errorHandlerPrefix + "." + LOGGER_REF,
                                    errorHandlerPrefix + "." + APPENDER_REF_TAG
                            };
                            for(Iterator iter = props.entrySet().iterator();iter.hasNext();)
                            {
                                Map.Entry entry = (Map.Entry) iter.next();
                                int i = 0;
                                for(; i < keys.length; i++)
                                {
                                    if(keys[i].equals(entry.getKey())) break;
                                }
                                if (i == keys.length)
                                {
                                    edited.put(entry.getKey(), entry.getValue());
                                }
                            }
                            PaxPropertySetter.setProperties(eh, edited, errorHandlerPrefix + ".");
                            LogLog.debug("End of errorhandler parsing for \"" + appenderName +"\".");
                      }

                }
                if (appender instanceof AppenderAttachable)
                {
                    final String appenderPrefix = prefix + ".appenders";
                    String appenderNames = OptionConverter.findAndSubst(appenderPrefix, props);
                    StringTokenizer st = new StringTokenizer(appenderNames, ",");
                    Appender childAppender;
                    String childAppenderName;
                    while(st.hasMoreTokens())
                    {
                        childAppenderName = st.nextToken().trim();
                        if( childAppenderName == null || childAppenderName.equals(",") )
                        {
                            continue;
                        }
                        LogLog.debug("Parsing appender named \"" + childAppenderName +"\".");
                        childAppender = parseAppender(props, childAppenderName);
                        if( childAppender != null )
                        {
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
            final LoggerRepository hierarchy)
    {
        boolean rootRef = OptionConverter.toBoolean(
                OptionConverter.findAndSubst(errorHandlerPrefix + ROOT_REF, props), false);
        if (rootRef)
        {
            eh.setLogger(hierarchy.getRootLogger());
        }
        String loggerName = OptionConverter.findAndSubst(errorHandlerPrefix + LOGGER_REF, props);
        if (loggerName != null)
        {
            Logger logger = (loggerFactory == null) ? hierarchy.getLogger(loggerName)
                    : hierarchy.getLogger(loggerName, loggerFactory);
            eh.setLogger(logger);
        }
        String appenderName = OptionConverter.findAndSubst(errorHandlerPrefix + APPENDER_REF_TAG, props);
        if (appenderName != null)
        {
            Appender backup = parseAppender(props, appenderName);
            if (backup != null)
            {
                eh.setBackupAppender(backup);
            }
        }
    }

}
