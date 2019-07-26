/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ops4j.pax.logging.it;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import org.apache.xbean.naming.context.WritableContext;
import org.apache.xbean.naming.global.GlobalContextManager;
import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.Server;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.logging.it.support.Helpers;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.Constants.START_LEVEL_SYSTEM_BUNDLES;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.OptionUtils.combine;

@RunWith(PaxExam.class)
public class Log4J2JdbcLazyJNDIAppenderIntegrationTest extends AbstractStdoutInterceptingIntegrationTestBase {

    @Inject
    private ConfigurationAdmin cm;

    private Server server;

    @Configuration
    public Option[] configure() throws IOException {
        final ServerSocket ss = new ServerSocket(0);
        final int port = ss.getLocalPort();
        String sport = Integer.toString(port);
        ss.close();

        File basedir = new File("target/h2");
        if (basedir.isDirectory()) {
            Files.walkFileTree(basedir.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        basedir.mkdirs();

        return combine(
                combine(baseConfigure(), defaultLoggingConfig()),

                mavenBundle("com.h2database", "h2").versionAsInProject().startLevel(START_LEVEL_SYSTEM_BUNDLES).start(),

                mavenBundle("org.apache.aries", "org.apache.aries.util").versionAsInProject().startLevel(START_LEVEL_SYSTEM_BUNDLES).start(),
                mavenBundle("org.apache.aries.jndi", "org.apache.aries.jndi.api").versionAsInProject().startLevel(START_LEVEL_SYSTEM_BUNDLES).start(),
                mavenBundle("org.apache.aries.jndi", "org.apache.aries.jndi.core").versionAsInProject().startLevel(START_LEVEL_SYSTEM_BUNDLES).start(),
                mavenBundle("org.apache.xbean", "xbean-naming").versionAsInProject().startLevel(START_LEVEL_SYSTEM_BUNDLES).start(),

                systemProperty("h2.port").value(sport),
                systemProperty("h2.basedir").value(basedir.getCanonicalPath()),

                paxLoggingApi(),
                paxLoggingLog4J2(),
                configAdmin(),
                eventAdmin()
        );
    }

    @Test
    @Ignore("Plain JNDI requires some work. What's bind here is javax.naming.Reference. I'm not sure what to do with it.")
    public void jdbcAppender() throws SQLException, NamingException {
        // setup H2 databased from within the test method. H2 is available at system classpath (no bundle)
        String basedir = context.getProperty("h2.basedir");
        String port = context.getProperty("h2.port");

        Helpers.updateLoggingConfig(context, cm, Helpers.LoggingLibrary.LOG4J2_PROPERTIES, "jdbc.jndi");

        // setup database ONLY after Log4J2 configuration
        setupH2Database(basedir, port);

        Logger log = LoggerFactory.getLogger("my.logger");
        log.info("Hello1 into LazyJdbcAppender");

        GlobalContextManager.setGlobalContext(new WritableContext());
        context.registerService(InitialContextFactory.class, new GlobalContextManager(), null);

        // let's bind data source to JNDI without osgi.service/ context root
        JdbcDataSource ds = new JdbcDataSource();
        ds.setUrl("jdbc:h2:tcp://localhost:" + port + "/logsdb;SCHEMA=logs");
        ds.setUser("logadmin");
        ds.setPassword("logpassword");

        InitialContext ic = new InitialContext();
        ic.bind("/ds1", ds);

        log.info("Hello2 into LazyJdbcAppender");

        ic.unbind("/ds1");

        log.info("Hello3 into LazyJdbcAppender");

        Set<String> messages = new HashSet<>();
        try (Connection c = DriverManager.getConnection("jdbc:h2:tcp://localhost:" + port + "/logsdb", "logadmin", "logpassword")) {
            try (Statement st = c.createStatement()) {
                try (ResultSet rs = st.executeQuery("select message from logs.log order by id asc")) {
                    while (rs.next()) {
                        messages.add(rs.getString(1));
                    }
                }
            }
        }
        assertFalse(messages.contains("Hello1 into LazyJdbcAppender"));
        assertTrue(messages.contains("Hello2 into LazyJdbcAppender"));
        assertFalse(messages.contains("Hello3 into LazyJdbcAppender"));

        stopH2Database();
    }

    private void setupH2Database(String basedir, String port) throws SQLException {
        server = Server.createTcpServer(
                "-tcp",
                "-tcpPort", port,
                "-baseDir", basedir
        ).start();

        // connect using embedded JDBC URL to create logsdb database

        try (Connection c = DriverManager.getConnection("jdbc:h2:file:" + basedir + "/logsdb")) {
            try (Statement st = c.createStatement()) {
                st.execute("create user logadmin password 'logpassword'");
            }
        }

        // connect using TCP JDBC URL as admin to create tables

        try (Connection c = DriverManager.getConnection("jdbc:h2:tcp://localhost:" + port + "/logsdb", "", "")) {
            try (Statement st = c.createStatement()) {
                st.execute("create schema LOGS");
            }
            try (Statement st = c.createStatement()) {
                st.execute("set schema LOGS");
            }
            try (Statement st = c.createStatement()) {
                st.execute("create table LOG (" +
                        "ID int generated always as identity (start with 1 no cycle), " +
                        "DATE timestamp, " +
                        "LEVEL varchar(5), " +
                        "SOURCE varchar(128), " +
                        "MESSAGE varchar(1024), " +
                        "primary key (ID)" +
                        ")");
            }
            try (Statement st = c.createStatement()) {
                st.execute("grant all on schema LOGS to logadmin");
            }
        }
    }

    private void stopH2Database() {
        server.stop();
    }

}
