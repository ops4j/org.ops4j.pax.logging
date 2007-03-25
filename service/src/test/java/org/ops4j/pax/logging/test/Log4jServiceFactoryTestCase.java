/*
 * Copyright 2005 Makas Tzavellas.
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
package org.ops4j.pax.logging.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.Invocation;
import org.jmock.core.Stub;
import org.ops4j.pax.logging.PaxLoggingService;
import org.ops4j.pax.logging.internal.ConfigFactory;
import org.ops4j.pax.logging.internal.LoggingServiceConfiguration;
import org.ops4j.pax.logging.internal.LoggingServiceFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class Log4jServiceFactoryTestCase extends MockObjectTestCase
{

    public Log4jServiceFactoryTestCase( String name )
    {
        super( name );
    }

    public void testMergingProperties()
        throws Exception
    {
        Mock configFactory = new Mock( ConfigFactory.class );
        BasicConfigureStub stub = new BasicConfigureStub();
        configFactory.expects( atLeastOnce() ).method( "configure" ).with( NOT_NULL ).will( stub );
        
        LoggingServiceConfiguration loggingConfig = new LoggingServiceConfiguration( (ConfigFactory) configFactory.proxy() );
        ResourceStub resourceStub = new ResourceStub();

        Mock bundle1 = new Mock( Bundle.class );
        Hashtable dictionary = new Hashtable();
        dictionary.put( LoggingServiceConfiguration.LOG4J_LOGGER_NAME, "bundle1" );
        dictionary.put( LoggingServiceConfiguration.LOG4J_CONFIG_FILE, "./bundle1_log4j.properties" );
        bundle1.expects( atLeastOnce() ).method( "getHeaders" ).will( returnValue( dictionary ) );
        bundle1.expects( once() ).method( "getResource" ).will( resourceStub );
        Bundle bundle = (Bundle) bundle1.proxy();
        ServiceRegistration serviceRegistration = new TestServiceRegistration( bundle );
        PaxLoggingService paxLogging = (PaxLoggingService) new Mock( PaxLoggingService.class  ).proxy();
        LoggingServiceFactory factory = new LoggingServiceFactory( loggingConfig, paxLogging );
        factory.getService( (Bundle) bundle1.proxy(), serviceRegistration );

        Mock bundle2 = new Mock( Bundle.class );
        Hashtable dictionary2 = new Hashtable();
        dictionary2.put( LoggingServiceConfiguration.LOG4J_LOGGER_NAME, "bundle2" );
        dictionary2.put( LoggingServiceConfiguration.LOG4J_CONFIG_FILE, "./bundle2_log4j.properties" );
        bundle2.expects( atLeastOnce() ).method( "getHeaders" ).will( returnValue( dictionary2 ) );
        bundle2.expects( once() ).method( "getResource" ).will( resourceStub );
        factory.getService( (Bundle) bundle2.proxy(), serviceRegistration );

        configFactory.verify();
        bundle1.verify();
        bundle2.verify();
    }

    public void testManagedServiceConfiguration()
        throws Exception
    {
        Mock configFactory = new Mock( ConfigFactory.class );
        GlobalConfigureStub stub = new GlobalConfigureStub();
        configFactory.expects( atLeastOnce() ).method( "configure" ).with( NOT_NULL ).will( stub );
        LoggingServiceConfiguration loggingConfig = new LoggingServiceConfiguration( (ConfigFactory) configFactory.proxy() );

        ResourceStub resourceStub = new ResourceStub();

        // ML - Aug 15, 2005: Test using the basic configuration
        Mock bundle1 = new Mock( Bundle.class );
        Hashtable dictionary = new Hashtable();
        dictionary.put( LoggingServiceConfiguration.LOG4J_LOGGER_NAME, "bundle1" );
        dictionary.put( LoggingServiceConfiguration.LOG4J_CONFIG_FILE, "./bundle1_log4j.properties" );
        bundle1.expects( atLeastOnce() ).method( "getHeaders" ).will( returnValue( dictionary ) );
        bundle1.expects( once() ).method( "getResource" ).will( resourceStub );
        Bundle bundle = (Bundle) bundle1.proxy();
        ServiceRegistration serviceRegistration = new TestServiceRegistration( bundle );
        PaxLoggingService paxLogging = (PaxLoggingService) new Mock( PaxLoggingService.class  ).proxy();
        LoggingServiceFactory factory = new LoggingServiceFactory( loggingConfig, paxLogging );
        factory.getService( bundle, serviceRegistration );
        loggingConfig.updated( null );

        // ML - Aug 15, 2005: Test using the global configuration
        String fileName = getClass().getClassLoader().getResource( "./global_log4j.properties" ).toString();
        Hashtable configuration = new Hashtable();
        configuration.put( LoggingServiceConfiguration.LOG4J_CONFIG_FILE, fileName );
        stub.setState( 10 );
        loggingConfig.updated( configuration );

        // ML - Aug 15, 2005: Test reseting the global configuration
        stub.setState( 20 );
        loggingConfig.updated( null );

        configFactory.verify();
        bundle1.verify();
    }

    private class GlobalConfigureStub
        implements Stub
    {

        private Properties m_DefaultProps;
        private Properties m_GlobalProps;
        private int m_State;
        private Properties m_Merged;

        public GlobalConfigureStub()
        {
            try
            {
                m_DefaultProps = load( "./default_log4j.properties" );
                m_GlobalProps = load( "./global_log4j.properties" );
                Properties bundle1Props = load( "./bundle1_log4j_expected.properties" );
                m_Merged = new Properties();
                m_Merged.putAll( m_DefaultProps );
                m_Merged.putAll( bundle1Props );
            } catch( IOException e )
            {
                e.printStackTrace();
            }
        }

        private Properties load( String name )
            throws IOException
        {
            Properties prop = new Properties();
            prop.load( getClass().getClassLoader().getResourceAsStream( name ) );
            return prop;
        }

        public Object invoke( Invocation invocation )
            throws Throwable
        {
            Properties prop = (Properties) invocation.parameterValues.get( 0 );
            assertNotNull( prop );
            switch( m_State  )
            {
                case 0:  // Initial default properties...
                    assertEquals( 4, prop.size() );
                    assertEquals( "State=0 --> Incorrect bundle available.", m_DefaultProps, prop );
                    m_State = 5;
                    break;
                case 5:
                    assertEquals( 9, prop.size() );
                    assertEquals( "State=5 --> Incorrect bundle available.", m_Merged, prop );
                    break;
                case 10:
                    assertEquals( 5, prop.size() );
                    assertEquals( "State=10 --> Incorrect bundle available.", m_GlobalProps, prop );
                    break;
                case 20:
                    assertEquals( 9, prop.size() );
                    assertEquals( "State=20 --> Incorrect bundle available.", m_Merged, prop );
                    break;
                default:
                    fail( "Unexpected m_State: " + m_State );
            }
            return null;
        }

        public StringBuffer describeTo( StringBuffer buffer )
        {
            buffer.append( "Verifying if log4j properties are switched accordingly in State: " );
            buffer.append( m_State );
            return buffer;
        }

        public void setState( int state )
        {
            m_State = state;
        }
    }

    private class BasicConfigureStub
        implements Stub
    {

        private Properties m_BundleOrig;
        private Properties m_Merged1;
        private Properties m_Merged2;
        private int m_State;

        public BasicConfigureStub()
        {
            try
            {
                m_BundleOrig = load( "./default_log4j.properties" );
                Properties bundle1 = load( "./bundle1_log4j_expected.properties" );
                Properties bundle2 = load( "./bundle2_log4j_expected.properties" );
                m_Merged1 = new Properties();
                m_Merged1.putAll( m_BundleOrig );
                m_Merged1.putAll( bundle1 );
                m_Merged2 = new Properties();

                m_Merged2.putAll( m_BundleOrig );
                m_Merged2.putAll( bundle1 );
                m_Merged2.putAll( bundle2 );
                m_State = 0;
            } catch( IOException e )
            {
                e.printStackTrace();
            }
        }

        private Properties load( String name )
            throws IOException
        {
            Properties prop = new Properties();
            ClassLoader classLoader = getClass().getClassLoader();
            InputStream resourceAsStream = classLoader.getResourceAsStream( name );
            prop.load( resourceAsStream );
            return prop;
        }

        public Object invoke( Invocation invocation )
            throws Throwable
        {
            Properties prop = (Properties) invocation.parameterValues.get( 0 );
            assertNotNull( prop );
            switch( m_State )
            {
                case 0:
                    assertEquals( 4, prop.size() );
                    assertEquals( m_BundleOrig, prop );
                    m_State = 1;
                    break;
                case 1:
                    assertEquals( 9, prop.size() );
                    assertEquals( m_Merged1, prop );
                    m_State = 2;
                    break;
                case 2:
                    assertEquals( 14, prop.size() );
                    assertEquals( m_Merged2, prop );
                    m_State = 2;
                    break;
                default:
                    fail( "Too many calls to ConfigFactory." );
            }
            return null;
        }

        public StringBuffer describeTo( StringBuffer buffer )
        {
            return buffer.append( "Verifying if log4j properties are properly merged" );
        }
    }

    private class ResourceStub
        implements Stub
    {

        public Object invoke( Invocation invocation )
            throws Throwable
        {
            String name = (String) invocation.parameterValues.get( 0 );
            return getClass().getClassLoader().getResource( name );
        }

        public StringBuffer describeTo( StringBuffer buffer )
        {
            return buffer.append( "Returns the default classloader URL" );
        }

    }

    private static class TestServiceRegistration
        implements ServiceRegistration
    {

        private ServiceReference m_reference;

        public TestServiceRegistration( Bundle bundle )
        {
            m_reference = new TestReference( bundle );
        }

        public ServiceReference getReference()
        {
            return m_reference;
        }

        public void setProperties( Dictionary dictionary )
        {
            ( (TestReference) m_reference ).setProperties( dictionary );
        }

        public void unregister()
        {
        }
    }

    private static class TestReference
        implements ServiceReference
    {

        private Bundle m_bundle;
        private Dictionary m_Properties;

        public TestReference( Bundle bundle )
        {
            m_bundle = bundle;
        }

        void setProperties( Dictionary props )
        {
            m_Properties = props;
        }

        public Object getProperty( String string )
        {
            return m_Properties.get( string );
        }

        public String[] getPropertyKeys()
        {
            Enumeration list = m_Properties.keys();
            String[] result = new String[ m_Properties.size() ];
            int i = 0;
            while( list.hasMoreElements() )
            {
                result[ i++ ] = (String) list.nextElement();
            }
            return result;
        }

        public Bundle getBundle()
        {
            return m_bundle;
        }

        public Bundle[] getUsingBundles()
        {
            return new Bundle[0];
        }

        public boolean isAssignableTo( Bundle bundle, String string )
        {
            System.out.println( "isAssignableTo( " + bundle + ", " + string + ");" );
            return false;
        }
    }
}
