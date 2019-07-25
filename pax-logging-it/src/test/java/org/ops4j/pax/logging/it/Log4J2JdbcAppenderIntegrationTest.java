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
import javax.inject.Inject;

import org.h2.tools.Server;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.logging.it.support.Helpers;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.Constants.START_LEVEL_SYSTEM_BUNDLES;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.OptionUtils.combine;

@RunWith(PaxExam.class)
public class Log4J2JdbcAppenderIntegrationTest extends AbstractStdoutInterceptingIntegrationTestBase {

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

                systemProperty("h2.port").value(sport),
                systemProperty("h2.basedir").value(basedir.getCanonicalPath()),

                paxLoggingApi(),
                paxLoggingLog4J2(),
                configAdmin(),
                eventAdmin(),

                // this fragment is needed for pax-logging-log4j2 to load org.h2.Driver class
                // it simply adds "Import-Package: org.h2"
                mavenBundle("org.ops4j.pax.logging", "pax-logging-sample-fragment-log4j2-h2").versionAsInProject().noStart()
        );
    }

    @Test
    public void jdbcAppender() throws SQLException {
        // setup H2 databased from within the test method. H2 is available at system classpath (no bundle)
        String basedir = context.getProperty("h2.basedir");
        String port = context.getProperty("h2.port");
        setupH2Database(basedir, port);

        Helpers.updateLoggingConfig(context, cm, Helpers.LoggingLibrary.LOG4J2_PROPERTIES, "jdbc.dm", props -> {
            props.put("log4j2.appender.jdbc.connectionSource.connectionString", "jdbc:h2:tcp://localhost:" + port + "/logsdb");
        });

        Logger log = LoggerFactory.getLogger("my.logger");
        log.info("Hello into JdbcAppender");

        String message = "";
        try (Connection c = DriverManager.getConnection("jdbc:h2:tcp://localhost:" + port + "/logsdb", "logadmin", "logpassword")) {
            try (Statement st = c.createStatement()) {
                try (ResultSet rs = st.executeQuery("select message from logs.log")) {
                    assertTrue(rs.next());
                    message = rs.getString(1);
                }
            }
        }
        assertThat(message, equalTo("Hello into JdbcAppender"));

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
