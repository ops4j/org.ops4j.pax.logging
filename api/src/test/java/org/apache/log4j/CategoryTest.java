/*
 * Copyright 2008 Michael Pilquist.
 *
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

import org.jmock.Mock;
import org.jmock.core.matcher.InvokeOnceMatcher;
import org.jmock.core.stub.ReturnStub;
import org.ops4j.pax.logging.PaxLogger;

import junit.framework.TestCase;

public class CategoryTest extends TestCase 
{
	public void testIsEnabledForFatal()
	{
		testIsEnabledFor( Level.FATAL, "isFatalEnabled" );
	}
	
	public void testIsEnabledForError()
	{
		testIsEnabledFor( Level.ERROR, "isErrorEnabled" );
	}
	
	public void testIsEnabledForWarn()
	{
		testIsEnabledFor( Level.WARN, "isWarnEnabled" );
	}
	
	public void testIsEnabledForInfo()
	{
		testIsEnabledFor( Level.INFO, "isInfoEnabled" );
	}
	
	public void testIsEnabledForDebug()
	{
		testIsEnabledFor( Level.DEBUG, "isDebugEnabled" );
	}
	
	public void testIsEnabledForTrace()
	{
		testIsEnabledFor( Level.TRACE, "isTraceEnabled" );
	}
	
	public void testIsEnabledForAll()
	{
		testIsEnabledFor( Level.ALL, "isTraceEnabled" );
	}
	
	public void testIsEnabledForOff()
	{
		final Mock mockDelegate = new Mock( PaxLogger.class );
		final Category category = new SimpleCategory( (PaxLogger) mockDelegate.proxy() );
		assertFalse( category.isEnabledFor( Level.OFF ) );
	}

	// Helpers
	
	private void testIsEnabledFor( Level level, String method )
	{
		final Mock mockDelegate = new Mock( PaxLogger.class );
		mockDelegate.expects( new InvokeOnceMatcher() ).method( method ).will( new ReturnStub( Boolean.TRUE ) );
		final Category category = new SimpleCategory( (PaxLogger) mockDelegate.proxy() );
		category.isEnabledFor( level );
	}
	
	/** Category used in tests. */
	private static class SimpleCategory extends Category
	{
		public SimpleCategory( PaxLogger delegate ) 
		{
			super(delegate);
		}

		protected void trace( Object message, Throwable t ) 
		{
			// Nothing to do
		}
	}
}
