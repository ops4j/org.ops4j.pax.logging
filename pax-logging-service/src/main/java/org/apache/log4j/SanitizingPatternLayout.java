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

import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.helpers.PatternParser;
import org.apache.log4j.spi.LoggingEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A PatternLayout that allows you to trim and sanitize a logging event
 * message to avoid security issues like: https://cwe.mitre.org/data/definitions/117.html
 */
public class SanitizingPatternLayout extends PatternLayout {

    private String replaceRegex;
    private String replacement;
    private boolean trim;
    private Pattern compiledRegex;

    @Override
    public void activateOptions() {
        if( replaceRegex !=null ) {
            compiledRegex = Pattern.compile(replaceRegex);
        }
        super.activateOptions();
    }

    protected PatternParser createPatternParser(String pattern) {
        return new PatternParser(pattern) {
            @Override
            protected void finalizeConverter(char c) {
                PatternConverter pc;
                switch (c) {
                    case 'm':
                        pc = new PatternConverter(formattingInfo) {
                            @Override
                            protected String convert(LoggingEvent event) {
                                return sanitize(event.getRenderedMessage());
                            }
                        };
                        currentLiteral.setLength(0);
                        addConverter(pc);
                        break;
                    default:
                        super.finalizeConverter(c);
                        break;
                }
            }
        };
    }

    private String sanitize(String message) {
        if (message == null) {
            return message;
        }
        if ( trim ) {
            message = message.trim();
        }
        if (compiledRegex == null) {
            return message;
        }
        Matcher matcher = compiledRegex.matcher(message);
        if( !matcher.matches() ) {
            message = matcher.replaceAll(replacement);
        }
        return message;
    }

    public String getReplaceRegex() {
        return replaceRegex;
    }

    public void setReplaceRegex(String replaceRegex) {
        this.replaceRegex = replaceRegex;
    }

    public String getReplacement() {
        return replacement;
    }

    public void setReplacement(String replacement) {
        this.replacement = replacement;
    }

    public boolean isTrim() {
        return trim;
    }

    public void setTrim(boolean trim) {
        this.trim = trim;
    }
}