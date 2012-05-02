package org.ops4j.pax.logging.logback.internal;

import org.junit.Assert;
import org.junit.Test;

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
