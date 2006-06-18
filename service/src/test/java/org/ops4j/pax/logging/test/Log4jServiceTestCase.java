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

import org.apache.log4j.BasicConfigurator;
import org.jmock.MockObjectTestCase;
import org.ops4j.pax.logging.internal.PaxLoggingServiceImpl;
import org.osgi.service.log.LogService;

public class Log4jServiceTestCase extends MockObjectTestCase
{
    public Log4jServiceTestCase( String name )
    {
        super( name );
    }

    public void testLogService() throws Exception
    {
        BasicConfigurator.configure();
        PaxLoggingServiceImpl ls = new PaxLoggingServiceImpl();
        ls.log( LogService.LOG_DEBUG, "*******TESTING*********" );
        ls.log( LogService.LOG_ERROR, "*******TESTING*********", new Exception() );
        ls.log( LogService.LOG_INFO, "*******TESTING*********" );
        ls.log( LogService.LOG_WARNING, "*******TESTING*********" );
        ls.log( null, LogService.LOG_INFO, "*******TESTING*********" );
    }

}
