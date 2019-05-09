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

import org.junit.Ignore;

@Ignore
public class CategoryTest
{
//	public void testIsEnabledForFatal()
//	{
//		testIsEnabledFor( Level.FATAL, "isFatalEnabled" );
//	}
//
//	public void testIsEnabledForError()
//	{
//		testIsEnabledFor( Level.ERROR, "isErrorEnabled" );
//	}
//
//	public void testIsEnabledForWarn()
//	{
//		testIsEnabledFor( Level.WARN, "isWarnEnabled" );
//	}
//
//	public void testIsEnabledForInfo()
//	{
//		testIsEnabledFor( Level.INFO, "isInfoEnabled" );
//	}
//
//	public void testIsEnabledForDebug()
//	{
//		testIsEnabledFor( Level.DEBUG, "isDebugEnabled" );
//	}
//
//	public void testIsEnabledForTrace()
//	{
//		testIsEnabledFor( Level.TRACE, "isTraceEnabled" );
//	}
//
//	public void testIsEnabledForAll()
//	{
//		testIsEnabledFor( Level.ALL, "isTraceEnabled" );
//	}
//
//	public void testIsEnabledForOff()
//    {
//        final Mock mockDelegate = new Mock( PaxLogger.class );
//        final Category category = new SimpleCategory( (PaxLogger) mockDelegate.proxy() );
//        assertFalse( category.isEnabledFor( Level.OFF ) );
//        mockDelegate.verify();
//    }
//
//	public void testIsEnabledForFatalPriority()
//    {
//        testIsEnabledFor( Priority.FATAL, "isFatalEnabled" );
//    }
//
//    public void testIsEnabledForErrorPriority()
//    {
//        testIsEnabledFor( Priority.ERROR, "isErrorEnabled" );
//    }
//
//    public void testIsEnabledForWarnPriority()
//    {
//        testIsEnabledFor( Priority.WARN, "isWarnEnabled" );
//    }
//
//    public void testIsEnabledForInfoPriority()
//    {
//        testIsEnabledFor( Priority.INFO, "isInfoEnabled" );
//    }
//
//    public void testIsEnabledForDebugPriority()
//    {
//        testIsEnabledFor( Priority.DEBUG, "isDebugEnabled" );
//    }
//
//    public void testLogFatal()
//    {
//        testLog( Level.FATAL, "fatal" );
//    }
//
//    public void testLogError()
//    {
//        testLog( Level.ERROR, "error" );
//    }
//
//    public void testLogWarn()
//    {
//        testLog( Level.WARN, "warn" );
//    }
//
//    public void testLogInfo()
//    {
//        testLog( Level.INFO, "inform" );
//    }
//
//    public void testLogDebug()
//    {
//        testLog( Level.DEBUG, "debug" );
//    }
//
//    public void testLogTrace()
//    {
//        testLog( Level.TRACE, "trace" );
//    }
//
//    public void testLogAll()
//    {
//        testLog( Level.ALL, "trace" );
//    }
//
//    public void testLogFatalPriority()
//    {
//        testLog( Priority.FATAL, "fatal" );
//    }
//
//    public void testLogErrorPriority()
//    {
//        testLog( Priority.ERROR, "error" );
//    }
//
//    public void testLogWarnPriority()
//    {
//        testLog( Priority.WARN, "warn" );
//    }
//
//    public void testLogInfoPriority()
//    {
//        testLog( Priority.INFO, "inform" );
//    }
//
//    public void testLogDebugPriority()
//    {
//        testLog( Priority.DEBUG, "debug" );
//    }
//
//    public void testGetEffectiveLevel()
//    {
//        final Mock mockDelegate = new Mock( PaxLogger.class );
//        mockDelegate.expects( new InvokeOnceMatcher() ).method( "getLogLevel" ).withAnyArguments().will( new ReturnStub( new Integer( -1 ) ) );
//        mockDelegate.expects( new InvokeOnceMatcher() ).method( "getLogLevel" ).withAnyArguments().will( new ReturnStub( new Integer( PaxLogger.LEVEL_ERROR ) ) );
//        mockDelegate.expects( new InvokeOnceMatcher() ).method( "getLogLevel" ).withAnyArguments().will( new ReturnStub( new Integer( PaxLogger.LEVEL_WARNING ) ) );
//        mockDelegate.expects( new InvokeOnceMatcher() ).method( "getLogLevel" ).withAnyArguments().will( new ReturnStub( new Integer( PaxLogger.LEVEL_INFO ) ) );
//        mockDelegate.expects( new InvokeOnceMatcher() ).method( "getLogLevel" ).withAnyArguments().will( new ReturnStub( new Integer( PaxLogger.LEVEL_DEBUG ) ) );
//        mockDelegate.expects( new InvokeOnceMatcher() ).method( "getLogLevel" ).withAnyArguments().will( new ReturnStub( new Integer( PaxLogger.LEVEL_TRACE ) ) );
//
//        final Category category = new SimpleCategory( (PaxLogger) mockDelegate.proxy() );
//        assertEquals( Level.TRACE , category.getEffectiveLevel() );
//        assertEquals( Level.DEBUG , category.getEffectiveLevel() );
//        assertEquals( Level.INFO , category.getEffectiveLevel() );
//        assertEquals( Level.WARN , category.getEffectiveLevel() );
//        assertEquals( Level.ERROR , category.getEffectiveLevel() );
//        assertEquals( null , category.getEffectiveLevel() );
//
//        mockDelegate.verify();
//    }
//
//	// Helpers
//
//	private void testIsEnabledFor( final Priority priority, final String method )
//	{
//		final Mock mockDelegate = new Mock( PaxLogger.class );
//		mockDelegate.expects( new InvokeOnceMatcher() ).method( method ).will( new ReturnStub( Boolean.TRUE ) );
//		final Category category = new SimpleCategory( (PaxLogger) mockDelegate.proxy() );
//		category.isEnabledFor( priority );
//		mockDelegate.verify();
//	}
//
//	private void testLog( final Priority priority, final String method )
//	{
//	    final Mock mockDelegate = new Mock( PaxLogger.class );
//        mockDelegate.expects( new InvokeOnceMatcher() ).method( method ).withAnyArguments();
//        mockDelegate.stubs().method( "isFatalEnabled" ).will( new ReturnStub( Boolean.TRUE ) );
//        mockDelegate.stubs().method( "isErrorEnabled" ).will( new ReturnStub( Boolean.TRUE ) );
//        mockDelegate.stubs().method( "isWarnEnabled" ).will( new ReturnStub( Boolean.TRUE ) );
//        mockDelegate.stubs().method( "isInfoEnabled" ).will( new ReturnStub( Boolean.TRUE ) );
//        mockDelegate.stubs().method( "isDebugEnabled" ).will( new ReturnStub( Boolean.TRUE ) );
//        mockDelegate.stubs().method( "isTraceEnabled" ).will( new ReturnStub( Boolean.TRUE ) );
//        final Category category = new SimpleCategory( (PaxLogger) mockDelegate.proxy() );
//        category.log( priority, "message" );
//        mockDelegate.verify();
//	}
//
//	/** Category used in tests. */
//	private static class SimpleCategory extends Category
//	{
//		public SimpleCategory( final PaxLogger delegate )
//		{
//			super(delegate);
//		}
//
//		protected void trace( final Object message, final Throwable t )
//		{
//		    m_delegate.trace( message.toString(), t );
//		}
//	}
}
