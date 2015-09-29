/*
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

import java.util.Properties;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SanitizingPatternLayoutTest {

    static final Logger FOO = Logger.getLogger("foo.bar");

    @Test
    public void testSanitation() {

        Properties props = new Properties();
        props.setProperty("log4j.rootLogger", "DEBUG, out");
        props.setProperty("log4j.appender.out", "org.apache.log4j.MockAppender");
        props.setProperty("log4j.appender.out.layout", "org.apache.log4j.SanitizingPatternLayout");
        props.setProperty("log4j.appender.out.layout.ConversionPattern","%p | [%m]");
        props.setProperty("log4j.appender.out.layout.replaceRegex","\\n");
        props.setProperty("log4j.appender.out.layout.replacement","--NL--");

        props.setProperty("log4j.appender.out.layout.trim", "true");
        PropertyConfigurator.configure(props);
        FOO.info("Hello\nWorld\n");
        assertEquals("INFO | [Hello--NL--World]", MockAppender.writer.toString());

        // let try a different config.
        MockAppender.writer.getBuffer().setLength(0);
        props.setProperty("log4j.appender.out.layout.trim", "false");
        PropertyConfigurator.configure(props);

        FOO.info("Hello\nWorld\n");
        assertEquals("INFO | [Hello--NL--World--NL--]", MockAppender.writer.toString());

        assertTrue(true);
    }

}