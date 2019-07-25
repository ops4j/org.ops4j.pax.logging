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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;

import org.h2.tools.Server;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class H2SetupIntegrationTest {

    public static Logger LOG = LoggerFactory.getLogger(H2SetupIntegrationTest.class);

    @Test
    public void setupAndConnectToH2Database() throws Exception {
        final ServerSocket ss = new ServerSocket(0);
        final int port = ss.getLocalPort();
        String sport = Integer.toString(port);
        ss.close();

        LOG.info("Starting H2 server on port " + port);

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

        Server server = Server.createTcpServer(
                "-tcp",
                "-tcpPort", sport,
                "-baseDir", basedir.getCanonicalPath()
        ).start();

        // connect using embedded JDBC URL to create logsdb database

        try (Connection c = DriverManager.getConnection("jdbc:h2:file:" + basedir.getCanonicalPath() + "/logsdb")) {
            LOG.info(String.format("Connection %s %s", c.getMetaData().getDatabaseProductName(), c.getMetaData().getDatabaseProductVersion()));
            try (Statement st = c.createStatement()) {
                LOG.info(" -- CATALOGS");
                try (ResultSet rs = st.executeQuery("select * from INFORMATION_SCHEMA.CATALOGS")) {
                    while (rs.next()) {
                        LOG.info(rs.getString(1));
                    }
                }
            }
            try (Statement st = c.createStatement()) {
                LOG.info(" -- SCHEMATA");
                try (ResultSet rs = st.executeQuery("select ID, CATALOG_NAME, SCHEMA_NAME, SCHEMA_OWNER from INFORMATION_SCHEMA.SCHEMATA")) {
                    while (rs.next()) {
                        LOG.info(rs.getString(1) + " | " + rs.getString(2) + " | " + rs.getString(3) + " | " + rs.getString(4));
                    }
                }
            }
            try (Statement st = c.createStatement()) {
                LOG.info(" -- ROLES");
                try (ResultSet rs = st.executeQuery("select NAME, REMARKS, ID from INFORMATION_SCHEMA.ROLES")) {
                    while (rs.next()) {
                        LOG.info(rs.getString(1) + " | " + rs.getString(2) + " | " + rs.getString(3));
                    }
                }
            }
            try (Statement st = c.createStatement()) {
                try (ResultSet rs = st.executeQuery("select NAME, ADMIN, REMARKS, ID from INFORMATION_SCHEMA.USERS")) {
                    LOG.info(" -- USERS");
                    while (rs.next()) {
                        LOG.info(rs.getString(1) + " | " + rs.getString(2) + " | " + rs.getString(3) + " | " + rs.getString(4));
                    }
                }
            }
            try (Statement st = c.createStatement()) {
                st.execute("create user logadmin password 'logpassword'");
            }
            try (Statement st = c.createStatement()) {
                try (ResultSet rs = st.executeQuery("select NAME, ADMIN, REMARKS, ID from INFORMATION_SCHEMA.USERS")) {
                    LOG.info(" -- USERS");
                    while (rs.next()) {
                        LOG.info(rs.getString(1) + " | " + rs.getString(2) + " | " + rs.getString(3) + " | " + rs.getString(4));
                    }
                }
            }
        }

//        try (Connection c = DriverManager.getConnection("jdbc:h2:mem:management_db_" + sport)) {
//            LOG.info(String.format("Connection %s %s", c.getMetaData().getDatabaseProductName(), c.getMetaData().getDatabaseProductVersion()));
//            try (Statement st = c.createStatement()) {
//                LOG.info(" -- CATALOGS");
//                try (ResultSet rs = st.executeQuery("select * from INFORMATION_SCHEMA.CATALOGS")) {
//                    while (rs.next()) {
//                        LOG.info(rs.getString(1));
//                    }
//                }
//            }
//        }

        // connect using TCP JDBC URL as admin to create tables

        try (Connection c = DriverManager.getConnection("jdbc:h2:tcp://localhost:" + sport + "/logsdb", "", "")) {
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

        // connect using TCP JDBC URL as logadmin to write and read tables

        String category = "";
        try (Connection c = DriverManager.getConnection("jdbc:h2:tcp://localhost:" + sport + "/logsdb", "", "")) {
            try (PreparedStatement pst = c.prepareStatement("insert into logs.log (date, level, source, message) values (?, ?, ?, ?)")) {
                pst.setTimestamp(1, new Timestamp(new Date().getTime()));
                pst.setString(2, "INFO");
                pst.setString(3, "org.ops4j.pax.logging");
                pst.setString(4, "Hello!");
                assertThat(pst.executeUpdate(), equalTo(1));
            }
            try (Statement st = c.createStatement()) {
                try (ResultSet rs = st.executeQuery("select id, date, level, source, message from logs.log")) {
                    LOG.info(" -- LOG");
                    while (rs.next()) {
                        LOG.info(rs.getInt(1) + " | " + rs.getTimestamp(2) + " | " + rs.getString(3) + " | " + rs.getString(4) + " | " + rs.getString(5));
                        category = rs.getString(4);
                    }
                }
            }
        }
        assertThat(category, equalTo("org.ops4j.pax.logging"));

        server.stop();
    }

}
