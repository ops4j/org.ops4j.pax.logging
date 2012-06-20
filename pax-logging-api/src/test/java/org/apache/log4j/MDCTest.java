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

import junit.framework.TestCase;
import org.junit.Assert;

public class MDCTest extends TestCase {

	public void testMDCGet() {
		String key = "key";
		Exception value = new Exception( key );

		MDC.put( key, value );

		Object mdcValue = MDC.get( key );

		Assert.assertNotNull( "The assigned key is not suppose to be null", mdcValue );

		Assert.assertEquals( "The return type is not an instance of Exception", Exception.class, mdcValue.getClass() );

		Assert.assertSame( "The returned instance of Exception are not the same", value, mdcValue );
	}

}

