/*
 * Copyright 1999,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.log4j.filter;

import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.MDC;


/**
  The MDCMatchFilter matches a configured value against the
  value of a configured key in the MDC of a logging event.

  <p>The filter admits three options <b>KeyToMatch</b>,
  <b>ValueToMatch</b>, and <b>ExactMatch</b>.

  <p>The value of <b>KeyToMatch</b> property determines which
  key is used to match against in the MDC. The value of that
  key is used to test against the <b>ValueToMatch</b property.
  The <b>KeyToMatch</b> property must be set before this filter
  can function properly.

  <p>The value of <b>ValueToMatch</b> property determines the
  string value to match against. If <b>ExactMatch</b> is set
  to true, a match will occur only when <b>ValueToMatch</b> exactly
  matches the MDC value of the logging event.  Otherwise, if the
  <b>ExactMatch</b> property is set to <code>false</code>, a match
  will occur if <b>ValueToMatch</b> is contained anywhere within the
  MDC value. The <b>ExactMatch</b> property is set to
  <code>false</code> by default.

  <p>Note that by default the value to match is set to
  <code>null</code> and will only match if the key is not contained
  or the value is null in the MDC.

  <p>For more information about how the logging event will be
  passed to the appender for reporting, please see
  the {@link MatchFilterBase} class.

  @author Mark Womack

  @since 1.3
*/
public class MDCMatchFilter extends MatchFilterBase {
  /**
    The key to match in the MDC of the LoggingEvent. */
  String keyToMatch;

  /**
    The value to match in the MDC value of the LoggingEvent. */
  String valueToMatch;

  /**
    Do we look for an exact match or just a "contains" match? */
  boolean exactMatch = false;

  /**
    Sets the key to match in the MDC of the LoggingEvent.
    
    @param key The key that will be matched. */
  public void setKeyToMatch(String key) {
    keyToMatch = key;
  }

  /**
    Gets the key to match in the MDC of the LoggingEvent.
    
    @return String The key that will be matched. */
  public String getKeyToMatch() {
    return keyToMatch;
  }

  /**
    Sets the value to match in the NDC value of the LoggingEvent.
    
    @param value The value to match. */
  public void setValueToMatch(String value) {
    valueToMatch = value;
  }

  /**
    Gets the value to match in the NDC value of the LoggingEvent.
        
    @return String The value to match. */
  public String getValueToMatch() {
    return valueToMatch;
  }

  /**
    Set to true if configured value must exactly match the MDC
    value of the LoggingEvent. Set to false if the configured
    value must only be contained in the MDC value of the
    LoggingEvent. Default is false.
    
    @param exact True if an exact match should be checked for. */
  public void setExactMatch(boolean exact) {
    exactMatch = exact;
  }

  /**
    Returns the true if an exact match will be checked for.
    
    @return boolean True if an exact match will be checked for. */
  public boolean getExactMatch() {
    return exactMatch;
  }

  /**
    Returns true if a key to match has been configured.
    
    @return boolean True if a match can be performed. */
  protected boolean canMatch() {
    return (keyToMatch != null);
  }

  /**
    If <b>ExactMatch</b> is set to true, returns true only when
    <b>ValueToMatch</b> exactly matches the MDC value of the
    logging event. If the <b>ExactMatch</b> property
    is set to <code>false</code>, returns true when
    <b>ValueToMatch</b> is contained anywhere within the MDC
    value. Otherwise, false is returned.  
    
    @param event The logging event to match against.
    @return boolean True if matches criteria. */
  protected boolean match(LoggingEvent event) {
    // get the mdc value for the key from the event
    // use the toString() value of the value object
    //Object mdcObject = event.getMDC(keyToMatch); //removed in Log4j-1.3
    Object mdcObject = MDC.get(keyToMatch);
    String mdcValue;

    if (mdcObject != null) {
      mdcValue = mdcObject.toString();
    } else {
      mdcValue = null;
    }

    // check for a match
    if (mdcValue == null) {
      return (valueToMatch == null);
    } else {
      if (valueToMatch != null) {
        if (exactMatch) {
          return mdcValue.equals(valueToMatch);
        } else {
          return (mdcValue.indexOf(valueToMatch) != -1);
        }
      } else {
        return false;
      }
    }
  }
}
