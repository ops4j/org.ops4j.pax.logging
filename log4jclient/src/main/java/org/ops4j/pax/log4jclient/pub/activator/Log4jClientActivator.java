package org.ops4j.pax.log4jclient.pub.activator;

import java.util.Properties;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.InvalidSyntaxException;

import org.apache.log4j.Logger;

public class Log4jClientActivator implements BundleActivator{

    public void start(BundleContext context)
    {
	System.out.println("Bundle log4jClient started");

	//	try{
	    //Properties log4jprops = new Properties();
	    //log4jprops.put("logger.name", "testLogger");
	    //String filter = "(logger.name=demoLogger)";
	    //String filterShort = "loggerName";	    
	    //ServiceReference[] srs = context.getServiceReferences(Logger.class.getName(), filter);
	    //if(srs.length>0){
	    String demoLog = "demoLogger";
	    Logger logger = Logger.getLogger(demoLog);
	    logger.warn("this is a warning");
	    logger.error("this is an error");

	    String develLog = "develLogger";
	    Logger logger2 = Logger.getLogger(develLog);
	    logger2.warn("this is a warning");
	    logger2.error("this is an error");

	    String anonymousLog = "anonymousLogger";
	    Logger logger3 = Logger.getLogger(anonymousLog);
	    logger3.warn("this is a warning");
	    logger3.error("this is an error");
	    //logger.log(Level.WARN, "this is another warning");
	    //}
	    //else{
	    //System.out.println("no service found");
	    //}
	    /*}
	catch(InvalidSyntaxException e){
	    e.printStackTrace();
	}
	    */
    }

    public void stop(BundleContext context)
    {
	System.out.println("Bundle log4jClient stopped");
    }
    
}
