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

File surefireOutput = new File(basedir, "target/surefire-reports/org.ops4j.pax.logging.test.jbosslogging.JBossLoggingNativeApiTest-output.txt")
List<String> lines = surefireOutput.readLines()
int ok = 0
for (String l : lines) {
  if (l.endsWith("org.ops4j.pax.logging.test.jbosslogging.JBossLoggingNativeApiTest simplestUsage")) {
    ok++
  }
  if (l == "INFO: INFO") {
    ok++
  }
}
assert ok == 2
