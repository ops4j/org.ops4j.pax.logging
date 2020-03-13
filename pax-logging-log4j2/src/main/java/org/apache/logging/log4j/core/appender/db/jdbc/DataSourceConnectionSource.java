/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package org.apache.logging.log4j.core.appender.db.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.Strings;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jndi.JNDIConstants;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * <p>A {@link JdbcAppender} connection source that uses a {@link DataSource} to connect to the database.</p>
 *
 * <p>PaxLogging changes: more dynamic approach to JNDI.</p>
 * <p>The idea is to be able to configure JNDI datasource for Log4J2 JDBC appender in situations where
 * both JNDI (aries-jndi) and {@link DataSource} service itself may dynamically come and go to/from OSGi registry.</p>
 */
@Plugin(name = "DataSource", category = Core.CATEGORY_NAME, elementType = "connectionSource", printObject = true)
public final class DataSourceConnectionSource extends AbstractConnectionSource {
    private static final Logger LOGGER = StatusLogger.getLogger();

    private DataSource dataSource;
    private final String description;

    private final String jndiName;
    private final boolean lazy;

    private ServiceTracker<DataSource, DataSource> tracker;

    private DataSourceConnectionSource(final String jndiName,
                                       final BundleContext context, final Filter filter,
                                       final DataSource dataSource, final boolean lazy) {
        this.dataSource = dataSource;
        this.lazy = lazy;
        Filter filter1;
        if (jndiName != null) {
            this.jndiName = jndiName;
            filter1 = null;
            this.description = "dataSource{ jndiName=" + jndiName + ", value=" + dataSource + ", lazy=" + lazy + " }";
        } else {
            this.jndiName = null;
            filter1 = filter;
            this.description = "dataSource{ filter=" + filter.toString() + ", value=" + dataSource + ", lazy=" + lazy + " }";

            // setup tracker
            tracker = new ServiceTracker<>(context, filter1, new ServiceTrackerCustomizer<DataSource, DataSource>() {
                @Override
                public DataSource addingService(ServiceReference<DataSource> reference) {
                    DataSource ds = context.getService(reference);
                    setDataSource(ds);
                    return ds;
                }

                @Override
                public void modifiedService(ServiceReference<DataSource> reference, DataSource service) {
                    setDataSource(service);
                }

                @Override
                public void removedService(ServiceReference<DataSource> reference, DataSource service) {
                    setDataSource(null);
                }
            });
            tracker.open();
        }
    }

    private synchronized void setDataSource(DataSource ds) {
        this.dataSource = ds;
    }

    @Override
    public void stop() {
        super.stop();
        if (tracker != null) {
            tracker.close();
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (this.lazy) {
            DataSource ds = this.dataSource;
            if (ds != null) {
                return ds.getConnection();
            } else if (jndiName != null) {
                // reacquire data source from JNDI
                ds = acquireDataSourceFromJNDI(jndiName, true);
                return ds == null ? null : ds.getConnection();
            }
            // fallback for situation where data source is configured from OSGi registry - we rely on the
            // tracker here which didn't provide us with any data source
            return null;
        } else {
            // assume dataSource was found at setup time
            return this.dataSource.getConnection();
        }
    }

    @Override
    public String toString() {
        return this.description;
    }

    /**
     * Factory method for creating a connection source within the plugin manager.
     *
     * @param jndiName The full JNDI path where the data source is bound. Should start with java:/comp/env or
     *                 environment-equivalent.
     * @param serviceFilter {@link Filter} definition to get {@link DataSource} directly
     *                      from OSGi registry without JNDI
     * @param lazy whether we should create <em>lazy</em> {@link ConnectionSource} that's more tolerant to missing
     *             JNDI and/or actual bound {@link DataSource}.
     * @return the created connection source.
     */
    @PluginFactory
    public static DataSourceConnectionSource createConnectionSource(@PluginAttribute("jndiName") final String jndiName,
                                                                    @PluginAttribute("service") final String serviceFilter,
                                                                    @PluginAttribute("lazy") final boolean lazy) {
        if (Strings.isEmpty(jndiName) && Strings.isEmpty(serviceFilter)) {
            LOGGER.error("No JNDI name or OSGi service filter provided.");
            return null;
        }

        String lazyJndiName = null;
        String lazyServiceFilter = null;
        DataSource dataSource = null;
        if (serviceFilter != null && !"".trim().equals(serviceFilter)) {
            lazyServiceFilter = serviceFilter.trim();
            // always access via ServiceTracker
            dataSource = null;
        } else if (jndiName != null && !"".trim().equals(jndiName)) {
            if (jndiName.startsWith("osgi:service/")) {
                // org.apache.karaf.jndi.internal.JndiServiceImpl#OSGI_JNDI_CONTEXT_PREFIX special case
                lazyServiceFilter = String.format("(&(objectClass=javax.sql.DataSource)(%s=%s))",
                        JNDIConstants.JNDI_SERVICENAME, jndiName.substring("osgi:service/".length()).trim());
                // always access via ServiceTracker
                dataSource = null;
            } else {
                lazyJndiName = jndiName.trim();
                dataSource = acquireDataSourceFromJNDI(lazyJndiName, lazy);
            }
        }

        BundleContext context = null;
        Filter filter = null;
        if (lazyServiceFilter != null) {
            // we need OSGi
            Bundle b = FrameworkUtil.getBundle(DataSourceConnectionSource.class);
            if (b == null || (context = b.getBundleContext()) == null) {
                LOGGER.error("Can't access Bundle and Bundle context required to access {}", lazyServiceFilter);
                return null;
            }
            try {
                filter = context.createFilter(lazyServiceFilter);
            } catch (InvalidSyntaxException e) {
                LOGGER.error("Can't parse {}", lazyServiceFilter, e);
                return null;
            }
        }

        // even if dataSource is null, we return new connection source which will handle reacquirement of
        // actual javax.sql.DataSource from JNDI or service registry
        return new DataSourceConnectionSource(lazyJndiName, context, filter, dataSource, lazy);
    }

    private static DataSource acquireDataSourceFromJNDI(String jndiName, boolean lazy) {
        try {
            final InitialContext context = new InitialContext();
            final DataSource dataSource = (DataSource) context.lookup(jndiName);
            if (dataSource == null) {
                if (lazy) {
                    LOGGER.warn("No data source found with JNDI name [" + jndiName + "].");
                } else {
                    LOGGER.error("No data source found with JNDI name [" + jndiName + "].");
                }
                return null;
            }

            return dataSource;
        } catch (final NamingException e) {
            // more serious problem - but still, JNDI may be there, but there's no (yet) DataSource
            // registered there
            if (lazy) {
                LOGGER.debug(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage(), e);
            }
            return null;
        }
    }
}
