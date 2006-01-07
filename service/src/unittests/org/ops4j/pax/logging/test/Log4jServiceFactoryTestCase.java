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
import java.util.Hashtable;
import java.util.Properties;
import java.util.Dictionary;
import java.util.Enumeration;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.Invocation;
import org.jmock.core.Stub;
import org.ops4j.pax.logging.service.internal.ConfigFactory;
import org.ops4j.pax.logging.service.internal.LoggingServiceFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.ServiceReference;

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

        ResourceStub resourceStub = new ResourceStub();

        Mock bundle1 = new Mock( Bundle.class );
        Hashtable dictionary = new Hashtable();
        dictionary.put( LoggingServiceFactory.LOG4J_LOGGER_NAME, "bundle1" );
        dictionary.put( LoggingServiceFactory.LOG4J_CONFIG_FILE, "./bundle1_log4j.properties" );
        bundle1.expects( atLeastOnce() ).method( "getHeaders" ).will( returnValue( dictionary ) );
        bundle1.expects( once() ).method( "getResource" ).will( resourceStub );
        Bundle bundle = (Bundle) bundle1.proxy();
        ServiceRegistration serviceRegistration = new TestServiceRegistration( bundle );
        Dictionary props = new Properties();
        props.put( "type", "pax-log" );
        serviceRegistration.setProperties( props );
        LoggingServiceFactory factory = new LoggingServiceFactory( (ConfigFactory)configFactory.proxy() );
        factory.getService( (Bundle) bundle1.proxy(), serviceRegistration );

        Mock bundle2 = new Mock( Bundle.class );
        Hashtable dictionary2 = new Hashtable();
        dictionary2.put( LoggingServiceFactory.LOG4J_LOGGER_NAME, "bundle2" );
        dictionary2.put( LoggingServiceFactory.LOG4J_CONFIG_FILE, "./bundle2_log4j.properties" );
        bundle2.expects( atLeastOnce() ).method( "getHeaders" ).will( returnValue( dictionary2 ) );
        bundle2.expects( once() ).method( "getResource" ).will( resourceStub );
        factory.getService( (Bundle) bundle2.proxy(), serviceRegistration );

        configFactory.verify();
        bundle1.verify();
        bundle2.verify();
    }

    public void testManagedServiceConfiguration() throws Exception
    {
        Mock configFactory = new Mock( ConfigFactory.class );
        GlobalConfigureStub stub = new GlobalConfigureStub();
        stub.setGlobal( false );
        configFactory.expects( atLeastOnce() ).method( "configure" ).with( NOT_NULL ).will( stub );

        ResourceStub resourceStub = new ResourceStub();

        // ML - Aug 15, 2005: Test using the basic configuration
        Mock bundle1 = new Mock( Bundle.class );
        Hashtable dictionary = new Hashtable();
        dictionary.put( LoggingServiceFactory.LOG4J_LOGGER_NAME, "bundle1" );
        dictionary.put( LoggingServiceFactory.LOG4J_CONFIG_FILE, "./bundle1_log4j.properties" );
        bundle1.expects( atLeastOnce() ).method( "getHeaders" ).will( returnValue( dictionary ) );
        bundle1.expects( once() ).method( "getResource" ).will( resourceStub );
        Bundle bundle = (Bundle) bundle1.proxy();
        ServiceRegistration serviceRegistration = new TestServiceRegistration( bundle );
        Dictionary props = new Properties();
        props.put( "type", "pax-log" );
        serviceRegistration.setProperties( props );
        LoggingServiceFactory factory = new LoggingServiceFactory( (ConfigFactory)configFactory.proxy() );
        factory.getService( bundle, serviceRegistration );
        factory.updated( null );

        // ML - Aug 15, 2005: Test using the global configuration
        String fileName = getClass().getClassLoader().getResource( "./global_log4j.properties" ).toString();
        Hashtable configuration = new Hashtable();
        configuration.put( LoggingServiceFactory.LOG4J_CONFIG_FILE, fileName );
        stub.setGlobal( true );
        factory.updated( configuration );

        // ML - Aug 15, 2005: Test reseting the global configuration
        stub.setGlobal( false );
        factory.updated( null );

        configFactory.verify();
        bundle1.verify();
    }

    private class GlobalConfigureStub implements Stub
    {
        private Properties m_Bundle1;
        private Properties m_Global;
        private boolean m_IsGlobal;

        public GlobalConfigureStub()
        {
            try
            {
                m_Bundle1 = load( "./bundle1_log4j_expected.properties" );
                m_Global = load( "./global_log4j.properties" );
            } catch( IOException e )
            {
                e.printStackTrace();
            }
        }

        public void setGlobal( boolean global )
        {
            m_IsGlobal = global;
        }

        private Properties load( String name ) throws IOException
        {
            Properties prop = new Properties();
            prop.load( getClass().getClassLoader().getResourceAsStream( name ) );
            return prop;
        }

        public Object invoke( Invocation invocation ) throws Throwable
        {
            Properties prop = (Properties)invocation.parameterValues.get( 0 );
            assertNotNull( prop );
            assertEquals( 5, prop.size() );
            if( m_IsGlobal )
            {
                assertEquals( m_Global, prop );
            }
            else
            {
                assertEquals( m_Bundle1, prop );
            }
            return null;
        }

        public StringBuffer describeTo( StringBuffer buffer )
        {
            return buffer.append( "Verifying if log4j properties are switched accordingly" );
        }
    }

    private class BasicConfigureStub implements Stub
    {
        private Properties m_Properties;
        private Properties m_Bundle1;
        private Properties m_Bundle2;
        private Properties m_MergedBundle;

        public BasicConfigureStub()
        {
            try
            {
                m_Bundle1 = load( "./bundle1_log4j_expected.properties" );
                m_Bundle2 = load( "./bundle2_log4j_expected.properties" );
                m_MergedBundle = new Properties();
                m_MergedBundle.putAll( m_Bundle1 );
                m_MergedBundle.putAll( m_Bundle2 );
            } catch( IOException e )
            {
                e.printStackTrace();
            }
        }

        private Properties load( String name ) throws IOException
        {
            Properties prop = new Properties();
            prop.load( getClass().getClassLoader().getResourceAsStream( name ) );
            return prop;
        }

        public Object invoke( Invocation invocation ) throws Throwable
        {
            Properties prop = (Properties)invocation.parameterValues.get( 0 );
            assertNotNull( prop );
            if( m_Properties == null )
            {
                assertEquals( 5, prop.size() );
                assertEquals( m_Bundle1, prop );
                m_Properties = prop;
            }
            else
            {
                assertEquals( 10, prop.size() );
                assertEquals( m_MergedBundle, prop );
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

        public Object invoke( Invocation invocation ) throws Throwable
        {
            String name = (String)invocation.parameterValues.get( 0 );
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
            ((TestReference) m_reference).setProperties( dictionary );
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
    }
}
