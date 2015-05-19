//  Licensed to the Apache Software Foundation (ASF) under one or more
//  contributor license agreements.  See the NOTICE file distributed with
//  this work for additional information regarding copyright ownership.
//  The ASF licenses this file to You under the Apache License, Version 2.0
//  (the "License"); you may not use this file except in compliance with
//  the License.  You may obtain a copy of the License at
// 
//       http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
// This SQL script creates the required tables by
// org.apache.log4j.db.DBAppender and org.apache.log4j.db.DBReceiver.
//
// It is intended for HSQLDB. 
//

DROP TABLE logging_event_exception IF EXISTS;
DROP TABLE logging_event_property IF EXISTS;
DROP TABLE logging_event IF EXISTS;


CREATE TABLE logging_event 
  (
    sequence_number   BIGINT NOT NULL,
    timestamp         BIGINT NOT NULL,
    rendered_message  LONGVARCHAR NOT NULL,
    logger_name       VARCHAR NOT NULL,
    level_string      VARCHAR NOT NULL,
    ndc               LONGVARCHAR,
    thread_name       VARCHAR,
    reference_flag    SMALLINT,
    caller_filename   VARCHAR, 
    caller_class      VARCHAR, 
    caller_method     VARCHAR, 
    caller_line       CHAR(4), 
    event_id          INT NOT NULL IDENTITY
  );


CREATE TABLE logging_event_property
  (
    event_id	      INT NOT NULL,
    mapped_key        VARCHAR(254) NOT NULL,
    mapped_value      LONGVARCHAR,
    PRIMARY KEY(event_id, mapped_key),
    FOREIGN KEY (event_id) REFERENCES logging_event(event_id)
  );

CREATE TABLE logging_event_exception
  (
    event_id         INT NOT NULL,
    i                SMALLINT NOT NULL,
    trace_line       VARCHAR NOT NULL,
    PRIMARY KEY(event_id, i),
    FOREIGN KEY (event_id) REFERENCES logging_event(event_id)
  );
