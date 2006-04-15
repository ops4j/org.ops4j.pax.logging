
This Example bundle demonstrates the use of the Jakarta Commons Logging API
(JCL) in Pax Logging subsystem, together with Log4J API usage and JDK Logging and
Avalon Logging as well.

We are using Jetty 5.1 as an example of a third-party library that uses JCL
which will hook into the Pax Logging system, which in turn uses Log4J for its
backend.

If you are running on Oscar, you will need to deploy the following bundles from
the OBR;
    OSGi Service
    OSGi Util
    Servlet

and from the Pax Logging project;
    Pax Logging Service
    Pax Logging Example


-------------------------------------------------
2006-01-07, www.ops4j.org