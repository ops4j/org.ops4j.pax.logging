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

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.ops4j.pax.logging.PaxContext;
import org.ops4j.pax.logging.PaxLoggingConstants;

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
	
	public void testNonInheritedMDC() throws Exception {
		System.setProperty(PaxLoggingConstants.LOGGING_CFG_INHERIT_THREAD_CONTEXT_MAP, "false");
		
		MDC.put("key", "value");
		
		final AtomicBoolean mdcIsSet = new AtomicBoolean(false);
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				mdcIsSet.set(MDC.get("key") != null);
			}
		});
		t.start();
		t.join();
		
		Assert.assertFalse( "The assigned key is not suppose to be null",  mdcIsSet.get());
	}
	
	public void testInheritedMDC() throws Exception {
		System.setProperty(PaxLoggingConstants.LOGGING_CFG_INHERIT_THREAD_CONTEXT_MAP, "true");
		
		MDC.put("key", "value");
		
		final AtomicBoolean mdcIsSet = new AtomicBoolean(false);
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				mdcIsSet.set(MDC.get("key") != null);
			}
		});
		t.start();
		t.join();
		
		Assert.assertTrue( "The assigned key is not suppose to be not null",  mdcIsSet.get());
	}
	
	@Before
	public void setup() throws Exception {
		clear();
	}
	
	@After
	public void tearDown() throws Exception {
		clear();
	}
	
	private void clear() throws Exception {
		System.getProperties().remove(PaxLoggingConstants.LOGGING_CFG_INHERIT_THREAD_CONTEXT_MAP);
		
		// Force the default context to be re-initialized
		Field field = MDC.class.getDeclaredField("m_defaultContext");
		field.setAccessible(true);
		field.set(null, new PaxContext());
		
		MDC.clear();
	}

}

