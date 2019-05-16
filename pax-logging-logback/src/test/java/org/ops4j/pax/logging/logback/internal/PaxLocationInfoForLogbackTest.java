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
package org.ops4j.pax.logging.logback.internal;

import org.junit.Assert;
import org.junit.Test;
import org.ops4j.pax.logging.logback.internal.spi.PaxLocationInfoForLogback;

/**
 * @author cdolan
 * @since 5/2/12 1:48 PM
 */
public class PaxLocationInfoForLogbackTest {
    @Test
    public void test() {
        PaxLocationInfoForLogback info = new PaxLocationInfoForLogback(new Exception().getStackTrace());
        Assert.assertEquals(getClass().getName(), info.getClassName());
        Assert.assertEquals("test", info.getMethodName());
        final int lineNum = Integer.parseInt(info.getLineNumber());
        Assert.assertTrue("linenum = " + lineNum, 10 < lineNum);
        Assert.assertTrue("linenum = " + lineNum, 200 > lineNum);
        Assert.assertEquals(getClass().getSimpleName() + ".java", info.getFileName());

        info = new PaxLocationInfoForLogback(null);
        Assert.assertEquals("?", info.getClassName());
        Assert.assertEquals("?", info.getMethodName());
        Assert.assertEquals("?", info.getLineNumber());
        Assert.assertEquals("?", info.getFileName());
    }
}
