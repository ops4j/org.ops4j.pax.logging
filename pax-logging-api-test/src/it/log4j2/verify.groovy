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

File surefireOutput = new File(basedir, "target/surefire-reports/org.ops4j.pax.logging.test.log4j2.Log4j2NativeApiTest-output.txt")
List<String> lines = surefireOutput.readLines()
int ok = 0
for (String l : lines) {
  if (l.contains("org.ops4j.pax.logging.test.log4j2.Log4j2NativeApiTest ({}) INFO ( | ): simplestUsage - INFO1")) {
    ok++
  }
  if (l.contains("  ({}) INFO ( | ): simplestUsage - INFO2")) {
    ok++
  }
  if (l.contains("org.ops4j.pax.logging.test.log4j2.Log4j2NativeApiTest ({}) INFO ( | ): simplestUsage - INFO3")) {
    ok++
  }
  if (l.contains("org.ops4j.pax.logging.test.log4j2.Log4j2NativeApiTest ({country=Equestria}) INFO ( | ): mdc - INFO")) {
    ok++
  }
  if (l.contains("org.ops4j.pax.logging.test.log4j2.Log4j2NativeApiTest ({}) INFO (m1[ p1, p2 ] | m1): markers - INFO")) {
    ok++
  }
  if (l.contains("FORMAT> -42- 0042")) {
    ok++
  }
  if (l.contains("MAP> <Map>")) {
    ok++
  }
  if (l.contains("MAP> {\"k1\":\"v2\",\"k2\":\"v2\"}")) {
    ok++
  }
  if (l.contains("SD> sd-test [1 k1=\"v1\" k2=\"v2\"] hello10")) {
    ok++
  }
  if (l.contains("SD> 1/sd-test v1 sd-test [1 k1=\"v1\" k2=\"v2\"] hello10")) {
    ok++
  }
}
assert ok == 10
