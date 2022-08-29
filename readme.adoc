
= PAX-LOGGING

The goal of this project is to allow users and bundles to use well known Logging APIs like SLF4J or Commons-Logging.

== Versions


|===
| |1.9.x |1.10.x |1.11.x |1.12.x |2.0.x |2.1.x |2.2.x

|OSGi Log version
|R6 (1.3)
|R6 (1.3)
|R6 (1.3)
|R6 (1.3)
|R7 (1.4)
|R7 (1.4)
|R8 (1.5)

|Log4j1 backend
|yes
|yes
|yes
|no
|yes
|no
|no

|Log4j2 version
|2.12.4
|2.17.1
|2.17.1
|2.17.1
|2.18.0
|2.18.0
|2.18.0

|Logback version
|no
|1.2.9
|1.2.11
|1.2.11
|1.2.11
|1.2.11
|1.2.11
|===

NOTE: All versions support Log4j1 API, while only selected ones support Log4j1 backend.

.History of the versions

* Version 1.11.x was major refactor of 1.10.x wheren >100 integration tests were added and behavior of Log4j1, Log4j2 and Logback backends was unified
* New major version 2.0.x was created to provide implementation of new version of the `org.osgi.service.log` package from OSGi R7 specification
* Version 1.12.x and 2.1.x were created during https://www.lunasec.io/docs/blog/log4j-zero-day/[Log4j crisis] to provide versions that match 1.11.x and 2.0.x (respectively), but without Log4j1 backend
* Version 2.2.x was created to provide implementation of new version of the `org.osgi.service.log` package from OSGi R8 specification - from day 1 without Log4j1 backend (but still with Log4j1 API)

At some point, https://reload4j.qos.ch/[reload4j] fork of https://logging.apache.org/log4j/1.2/index.html[Apache log4j] was used.


== Basics

There should be a distinction between _Logging API_ which, by itself, doesn't do any log, but instead requires specific logging implementation (and related configuration) and the _Logging implementation_ itself.

Logging APIs (or _Facades_) matching the above distinction include:

* https://www.slf4j.org/[SLF4J]
* https://commons.apache.org/proper/commons-logging/[Commons Logging]
* https://osgi.org/specification/osgi.cmpn/7.0.0/service.log.html[OSGi Compendium Log Service]
* http://docs.jboss.org/hibernate/orm/4.3/topical/html/logging/Logging.html[JBoss Logging]

Logging implementations always provide their own _APIs_ and can be used without any of the above facades. These include:

* https://logging.apache.org/log4j/2.x/[Apache Log4J2]
* https://logback.qos.ch/[Logback]

However usage of Logback _without_ SLF4J is very rare and won't be implemented in pax-logging.

And the above two (with their specific configuration file syntax) are supported by (respectively):

* pax-logging-log4j2 (log4J2)
* pax-logging-logback (logoback)

`pax-logging-log4j1` was named `pax-logging-service` before version 2.0.0 and was removed completely in 2.1.0. However Log4j1 API was _not_ removed.

== Different Logging APIs

The fundamental interface is `org.osgi.service.log.LogService` from Chapter 101 of OSGi Compendium specification.
The history of `org.osgi.service.log` package versions is:

* `1.0`: OSGi R1
* `1.1`: OSGi R2
* `1.2`: OSGi R3
* `1.3`: OSGi R4, R4.1, R4.2, R4.3, R5, R6
* `1.4`: OSGi R7
* `1.5`: OSGi R8

pax-logging-api 1.x uses version `1.3` from OSGi Compendium R6.
pax-logging-api 2.x uses version `1.4` from OSGi Compendium R7.

Each class that's an entry point to specific Logging framework (like `org.slf4j.LoggerFactory`) interacts with pax-logging using `org.ops4j.pax.logging.PaxLoggingManager`. This _manager_ tracks instances of framework-specific `org.ops4j.pax.logging.PaxLoggingService` services that eventually delegate to particular framework (like Log4J2).

This indirection allows to plug-in OSGi specific mechanism (like replacing `org.ops4j.pax.logging.PaxLoggingService` implementation at runtime) without refreshing (in OSGi terms) pax-logging-api bundle.

There's one important thing. Logging facades and frameworks share familiar concepts of _category_ (or a logger _name_). This concept was introduced (afair) in Log4J1 with tree-like _hierarchy_ of loggers where more general loggers could provide shared configuration (like appenders) for more particular loggers.

OSGi Compendium `org.osgi.service.log.LogService` _doesn't_ have this notion of _category_, but (without a need to provide more technical details) pax-logging implements calls to `org.osgi.service.log.LogService.log()` method in such way that _category_ is (if possible) bundle's _symbolic name_. This _category_ is then used to get a logger from underlying logging backend.

In other words each `org.osgi.service.log.LogService.log()` call may be represended by this pseudo code:

[listing,options=nowrap]
----
// calculate category
category = try to take category from bundle's symbolic name

// obtain logger from underlying backend, like Log4J2
logger = LoggerFactory.getLogger(bundle, category)

// log a message according to specified level
logger.info(message, throwable) // or warn() or debug(), ...
----

Being aware of this, it's actually better to _not_ use `org.osgi.service.log.LogService.log()` in your code directly and just use given logging facade (like SLF4J or Commons Logging) - this removes a need to obtain a logger (which is cached anyway) on each log call.

=== OSGi R7 Log Service

With OSGi R7, standard Log Service gets a concept of _logger_. This makes the service easier to use - almost like de facto standard logging facades, where some _factory_ is used to obtain _loggers_ that are used to invoke logging methods.

OSGi R7 adds new interface `org.osgi.service.log.LoggerFactory`, which is actually a new super interface of old `org.osgi.service.log.LogService` service.

Factory methods that return instances of `org.osgi.service.log.Logger` by name or class are intuitive. There are however factory methods that take new argument: `Class<L> loggerType` where `<L extends org.osgi.service.log.Logger>`.
The only known/acceptable classes that can be passed to such factory methods are:

* `org.osgi.service.log.Logger`
* `org.osgi.service.log.FormatterLogger`

By passing `org.osgi.service.log.FormatterLogger.class` as an argument, user of factory method indicates that he/she wants to use printf-like formatting, for example:

[listing,options=nowrap]
----
ServiceReference<LoggerFactory> sr = context.getServiceReference(org.osgi.service.log.LoggerFactory.class);
LoggerFactory loggerFactory = context.getService(sr);
org.osgi.service.log.Logger log = loggerFactory.getLogger("com.example.service", FormatterLogger.class);
log.info("Hello %s", "world");
----

When passing `org.osgi.service.log.Logger.class` or when not passing anything, we assume Slf4J-like formatting:

[listing,options=nowrap]
----
ServiceReference<LoggerFactory> sr = context.getServiceReference(org.osgi.service.log.LoggerFactory.class);
LoggerFactory loggerFactory = context.getService(sr);
org.osgi.service.log.Logger log = loggerFactory.getLogger("com.example.service");
log.info("Hello {}", "world");
----

==== Fully Qualified Class Name

`FQCN` concept is very important. It's used to _mark_ a place inside stack trace where code that invokes logging operation _transitions_ into the logging mechanism itself. Last stack frame before logging mechanism marks a _location_ that can be used to obtain class name, method name, file name and line number (so called _location info_). Before OSGi R7, it was quite easy to implement correctly, because all the logging methods where managed by Pax Logging itself and proper `FQCN` could be set. With OSGi R7 logging service, _logger_ is also a standard interface and logging methods may be called without any facade (like Slf4J).

Pax Logging had to carefuly handle all the _entry points_ into logging mechanism, to correctly determine `FQCN`.

== Concepts

pax-logging is not trivial bridge to different (2 actually) logging frameworks (Log4J2, Logback), it also provides SPI layer that allows users to extend any logging framework. The most clear usage is when user wants to provide custom _appender_ that processes _logging events_.

=== Logging events

Each time a method like `log.info()` is called in any code fragment, new _logging event_ is created. This event is a representation of a fact that user wants to log some message/information. Each _logging event_ has some information associated like timestamp, code location (if available), message, exception (if needed) and severity (importance) of the event.

=== Appenders

_Appender_ is kind of processor that does something with the _logging event_ - usually appending new information (typically a line) to a file.

=== Level and threshold

This is a bit confusing part. At first glance it looks trivial - when user wants to invoke logging method, he/she may use one of typical methods like `info()`, `warn()`, `trace()` or other.

Simply from this set of alternatives we can derive the concept of _logging event level_ (or _severity_).

Let's start with something we can treat as _canonical_ - https://en.wikipedia.org/wiki/Syslog#Severity_level[Syslog]. Here are the level names and their numerical equivalents:

.Syslog levels
|===
|Numerical value |Severity/level name

|0
|Emergency

|1
|Alert

|2
|Critical

|3
|Error

|4
|Warning

|5
|Notice

|6
|Informational

|7
|Debug
|===

From the above, we can try to summarize:

====
Numerically _higher_ level is _less important_.
====

A concept related to _level_ is _threshold_. Without giving precise constraints, _threshold_ is a _limit_ for _logging events_. For the above, Syslog example, setting a _threshold_ value to _Warning_ means that we're interested in events with _Warning_ or (numerically) _lower_ events.

Thus:

====
The _higher_ (numerically) the threshold, the more logging events are processed. Less important events are processed.
====

.Adding more confusion

Logging frameworks (and APIs) used in pax-logging treat the _level_ concept differently... Log4J1 has direct relation to Syslog levels, but it's not a case with Log4J2 and java.util.logging.
Here's a table where Syslog and Log4J1 can be directly related. Placement of levels from other libraries is a bit arbitrary and related to logging level name equivalents.

* Log4J1: constants in `org.apache.log4j.Level` class
* Log4J2: values in `org.apache.logging.log4j.spi.StandardLevel` enum
* Logback: constants in `ch.qos.logback.classic.Level` class
* java.util.logging: values in `java.util.logging.Level` class
* Slf4J: constants in `org.slf4j.spi.LocationAwareLogger` interface
* OSGi R6: constants in `org.osgi.service.log.LogService` interface
* OSGi R7: values in `org.osgi.service.log.LogLevel` enum

[options=nowrap]
|===
|Syslog |Log4J1 |Log4J2 |Logback|java.util.logging |SLF4J |OSGi R6|OSGi R7

|0 - Emergency
|Integer.MAX_VALUE - OFF
|0 - OFF
|Integer.MAX_VALUE - OFF
|Integer.MAX_VALUE - OFF
|
|
|0 - AUDIT

|0 - Emergency
|50000 - FATAL
|100 - FATAL
|
|1000 - SEVERE
|
|
|

|1 - Alert
|
|
|
|
|
|
|

|2 - Critical
|
|
|
|
|
|
|

|3 - Error
|40000 - ERROR
|200 - ERROR
|40000 - ERROR
|1000 - SEVERE
|40 - ERROR
|1 - ERROR
|1 - ERROR

|4 - Warning
|30000 - WARN
|300 - WARN
|30000 - WARN
|900 - WARNING
|30 - WARN
|2 - WARNING
|2 - WARN

|5 - Notice
|
|
|
|
|
|
|

|6 - Informational
|20000 - INFO
|400 - INFO
|20000 - INFO
|800 - INFO, 700 - CONFIG
|20 - INFO
|3 - INFO
|3 - INFO

|7 - Debug
|10000 - DEBUG
|500 - DEBUG
|10000 - DEBUG
|500 - FINE
|10 - DEBUG
|4 - DEBUG
|4 - DEBUG

|7 - Debug
|5000 - TRACE
|600 - TRACE
|5000 - TRACE
|400 - FINER
|0 - TRACE
|
|5 - TRACE

|
|
|
|
|300 - FINEST
|
|
|

|7 - Debug
|Integer.MIN_VALUE - ALL
|Integer.MAX_VALUE - ALL
|Integer.MIN_VALUE - ALL
|Integer.MIN_VALUE - ALL
|
|
|
|===

Notes and confusing parts:

* Log4J1's `OFF` level matches numerical value of Syslog `Emergency` level
* java.util.logging: there are too many less important levels (FINE, FINER, FINEST) and too little more critical ones (only SEVERE)
* Syslog doesn't define _trace_ level, so its _debug_ entry is duplicated to cover constants from logging frameworks
* Syslog, Log4J2 and OSGi use increasing numerical level for decreasing event importance
* Log4J1, java.util.logging and SLF4J use higher numerical levels for more important events
* Mapping of java.util.logging levels to more popular level names is implemented in `org.ops4j.pax.logging.spi.support.BackendSupport.toJULLevel()`
* `OFF` and `ALL` special levels have to be treated carefully by pax-logging because the usage of numerical values is totally unintuitive.
* OSGi R7 introduces `AUDIT` log level for _information that must always be logged_ - thus suggesting that it's _more than error_ - I've associated it with Syslog's `Emergency` level. Though matching value from `org.apache.log4j.Priority` has comment _is intended to turn off logging_.

=== Markers

_Markers_ allow to pass/associate additional, dynamic information with logging operation itself. Just as logger name (category) and level are static aspects of the logger itself, _marker_ is associated with single logging invocation (thus effectively with _logging event_). Single logger may be used to log message with or without marker and it's up to specific implementation (Logback, Log4J2) to handle the marker accordingly.

For example, Log4J1 doesn't support markers, so slf4j-log4j12 bridges to Log4J1 using `org.slf4j.helpers.MarkerIgnoringBase` abstract base class which simply ignores markers. Logback and Log4J2 implement _full_ `org.slf4j.spi.LocationAwareLogger` with marker support.

Markers are used usually by implementation-specific filters and appenders:

* filters may be configured to restrict logging statements to ones using (or not using) particular marker
* some appenders may simply do not do anything if specific marker is (or is not) present (for example that's the case with `ch.qos.logback.classic.boolex.OnMarkerEvaluator` that may be attached to `ch.qos.logback.classic.net.SMTPAppender`)

Finally, a marker may have parent (or child) marker(s) associated - making them something slightly more complex than single _name_.

In Pax Logging, `org.ops4j.pax.logging.PaxLogger` interface didn't contain methods accepting markers. https://ops4j1.jira.com/browse/PAXLOGGING-160[PAXLOGGING-160] passed _marker_ as String attribute through thread-bound `org.ops4j.pax.logging.PaxContext`. https://ops4j1.jira.com/browse/PAXLOGGING-259[PAXLOGGING-259] adds such methods to this interface.

Remember - in Pax Logging, it's possible to use for example Log4J2 _API_ to log information that's effectively handled by Logback, so despite the API being aware of markers, they may not be used correctly by actual logging implementation. As consequence, `isXXXEnabled(..., marker, ...)` methods may not be handled early in the process of logging.

== SLF4J

`slf4j-api-1.7.33-sources.jar` contains more sources than `slf4j-api-1.7.33.jar` has classes - in particular, `org.slf4j.impl` package is removed from the jar and the responsibility to provide:

* `org.ops4j.impl.StaticLoggerBinder`
* `org.ops4j.impl.StaticMDCBinder`
* `org.ops4j.impl.StaticMarkerBinder`

classes lies on the side of _binding library_ for SLF4J API. Such classes are provided by (among others):

* `logback-classic-1.2.10.jar`
* `log4j-slf4j-impl-2.17.1.jar`
* `slf4j-nop-1.7.33.jar`
* `slf4j-log4j12-1.7.33.jar`
* `slf4j-simple-1.7.33.jar`

pax-logging-api provides own implementation of these three classes. All other classes are directly repackaged (using bndlib) from `slf4j-api-1.7.33.jar` - classes that don't have to be changed are no longer shipped in pax-logging-api source directory.

== Commons Logging

While SLF4J takes simple and elegant approach for finding the actual implementation (`StaticLoggerBinder`), Commons Logging uses old school discovery through various ClassLoader and ServiceLoader tricks.

In pax-logging, all this discovery is not needed, so the only reimplemented class is `org.apache.commons.logging.LogFactory` with all the discovery code removed.

== Apache JULI

Apache JULI is specialized (and repackaged) version of Commons Logging with original discovery mechanism already removed for Tomcat's internal logging mechanism purposes.

In pax-logging, there was less work to do - discovery mechanism was already removed, only `org.apache.juli.logging.LogFactory.getInstance(java.lang.String)` method was changed to delegate to `PaxLoggingManager`.

== Avalon Logging

Ancient Avalon framework predates most (if not all) Java server frameworks aiming to provide code and component organization patterns and programming model. Without dealing much with archeology, pax-logging-api provides support for `org.apache.avalon.framework.logger` package where the ultimate _source of truth_ is https://svn.apache.org/repos/asf/excalibur/tags/avalon-framework-api-4.3-Release/framework/api/src/java/org/apache/avalon/framework/logger/[this SVN tag and directory].

There are no _factory methods_ to access Avalong loggers as we know from SLF4J or even from Commons Logging. There's simply new instance creation, where the reference may be assigned to `org.apache.avalon.framework.logger.Logger` interface. Thus pax-logging-api doesn't include any source from Avalon Framework. Simply implementation of `org.apache.avalon.framework.logger.Logger` is provided.

Excalibur (actual library/framework using Avalon) simply provides concrete implementations of `org.apache.avalon.framework.logger.Logger`, like:

* `org.apache.avalon.excalibur.logger.Log4JLogger`
* `org.apache.avalon.framework.logger.NullLogger`
* `org.apache.avalon.framework.logger.CommonsLogger`
* `org.apache.avalon.excalibur.logger.ServletLogger`
* `org.apache.avalon.framework.logger.Jdk14Logger`
* `org.apache.avalon.framework.logger.ConsoleLogger`

To achieve _factory method_ approach, pax-logging-api exports `org.ops4j.pax.logging.avalon` package with special (not implied from Avalon Framework design) factory class for Avalon loggers. For other facades, package with factory classes is not `org.ops4j.pax.logging.*`.

== JBoss Logging

JBoss started to use dedicated logging _bridge_ (facade) with http://docs.jboss.org/hibernate/orm/4.3/topical/html/logging/Logging.html[Hibernate 4.0]. Similarly to e.g., Commons Logging, actual logging framework is discovered at runtime.

JBoss Logging can delegate to either concrete logging implementation (like Log4J2) or another logging facade (like SLF4J or Commons Logging). It uses discovery (ClassLoader + ServiceLoader) mechanism to find the framework to delegate to.

Originally, `org.jboss.logging.provider` property may be set to one of these values:

* jboss
* jdk
* log4j2
* log4j
* slf4j

Then discovery checks ServiceLoader for `org.jboss.logging.Provider` provider (`/META-INF/services/org.jboss.logging.Provider`).

pax-logging API doesn't yet delegate JBoss Logging API to pax-logging OSGi manager.
https://ops4j1.jira.com/browse/PAXLOGGING-251[PAXLOGGING-251] tracks this issue.

== Log4j

Ah, the grandfather of all configurable Logging frameworks. Created when there was no logging bridges/facades around. Actually first facades (Commons Logging) was created to bridge common logging API to one of different logging frameworks (back then, it was only Log4J1 and Java Util Logging (JUL) from JDK1.4).

Because its origins are in pre-logging bridge times, Log4J1's API was used directly by very large amount of code. That's why pax-logging fully supports its native API. However in Pax Logging 1.12.x and 2.1.x I've removed the implementation (in particular the appenders) based on Log4J1.

Also, this was the first logging framework embraced by pax-logging project itself.

Here, the problem is with splitting original log4j:log4j JAR into API (for pax-logging-api) and implementation (for pax-logging-log4j1).

The original `Export-Package` header of log4j:log4j (yes - it is correct OSGi bundle) is (after formatting):

[listing,options=nowrap]
----
org.apache.log4j;         version="1.2.17"; uses:="org.apache.log4j.spi,org.apache.log4j.helpers,org.apache.log4j.pattern,org.apache.log4j.or,org.apache.log4j.config",
org.apache.log4j.config;  version="1.2.17"; uses:="org.apache.log4j.helpers,org.apache.log4j,org.apache.log4j.spi",
org.apache.log4j.helpers; version="1.2.17"; uses:="org.apache.log4j,org.apache.log4j.spi,org.apache.log4j.pattern",
org.apache.log4j.jdbc;    version="1.2.17"; uses:="org.apache.log4j,org.apache.log4j.spi",
org.apache.log4j.jmx;     version="1.2.17"; uses:="org.apache.log4j,javax.management,org.apache.log4j.helpers,org.apache.log4j.spi",
org.apache.log4j.net;     version="1.2.17"; uses:="org.apache.log4j,org.apache.log4j.spi,javax.naming,org.apache.log4j.helpers,javax.jms,org.apache.log4j.xml,javax.mail,javax.mail.internet,org.w3c.dom,javax.jmdns",
org.apache.log4j.nt;      version="1.2.17"; uses:="org.apache.log4j.helpers,org.apache.log4j,org.apache.log4j.spi",
org.apache.log4j.or;      version="1.2.17"; uses:="org.apache.log4j.helpers,org.apache.log4j.spi,org.apache.log4j",
org.apache.log4j.or.jms;  version="1.2.17"; uses:="org.apache.log4j.helpers,javax.jms,org.apache.log4j.or",
org.apache.log4j.or.sax;  version="1.2.17"; uses:="org.apache.log4j.or,org.xml.sax",
org.apache.log4j.pattern; version="1.2.17"; uses:="org.apache.log4j.helpers,org.apache.log4j.spi,org.apache.log4j,org.apache.log4j.or",
org.apache.log4j.rewrite; version="1.2.17"; uses:="org.apache.log4j,org.apache.log4j.spi,org.apache.log4j.helpers,org.apache.log4j.xml,org.w3c.dom",
org.apache.log4j.spi;     version="1.2.17"; uses:="org.apache.log4j,org.apache.log4j.helpers,org.apache.log4j.or",
org.apache.log4j.varia;   version="1.2.17"; uses:="org.apache.log4j.spi,org.apache.log4j,org.apache.log4j.helpers"
org.apache.log4j.xml;     version="1.2.17"; uses:="javax.xml.parsers,org.w3c.dom,org.xml.sax,org.apache.log4j.config,org.apache.log4j.helpers,org.apache.log4j,org.apache.log4j.spi,org.apache.log4j.or",
----

Additionally, the jar contains:

* org.apache.log4j.chainsaw
* org.apache.log4j.lf5.*

pax-logging-api exports these (from log4j1):

[listing,options=nowrap]
----
org.apache.log4j;     version=1.2.15; uses:="org.apache.log4j.spi org.ops4j.pax.logging org.osgi.framework"
org.apache.log4j.spi; version=1.2.15; uses:="org.apache.log4j"
org.apache.log4j.xml; version=1.2.15; uses:="javax.xml.parsers org.w3c.dom"
----

I checked original `log4j:log4j` and started with single reexport of `org.apache.log4j` package. The closure of exports turned out to be:
[listing,options=nowrap]
----
Export-Package:
 org.apache.log4j;         version="1.2.17"; uses:="org.apache.log4j.helpers,org.apache.log4j.or,org.apache.log4j.spi",
 org.apache.log4j.config;  version="1.2.17"; uses:="org.apache.log4j",
 org.apache.log4j.helpers; version="1.2.17"; uses:="org.apache.log4j,org.apache.log4j.spi",
 org.apache.log4j.or;      version="1.2.17"; uses:="org.apache.log4j.spi",
 org.apache.log4j.pattern; version="1.2.17"; uses:="org.apache.log4j,org.apache.log4j.helpers,org.apache.log4j.spi",
 org.apache.log4j.spi;     version="1.2.17"; uses:="org.apache.log4j,org.apache.log4j.or",
 org.apache.log4j.xml;     version="1.2.17"; uses:="org.apache.log4j,org.apache.log4j.config,org.apache.log4j.spi"
Import-Package:
 com.ibm.uvm.tools;resolution:=optional
----

`com.ibm.uvm.tools` was additional import generated by analyzing (bndlib) `org.apache.log4j.spi.LocationInfo` class.

So the remaining exports from original `log4j:log4j` that are not part of the above closure are:
[listing,options=nowrap]
----
org.apache.log4j.jdbc;    version="1.2.17"; uses:="org.apache.log4j,org.apache.log4j.spi",
org.apache.log4j.jmx;     version="1.2.17"; uses:="org.apache.log4j,javax.management,org.apache.log4j.helpers,org.apache.log4j.spi",
org.apache.log4j.net;     version="1.2.17"; uses:="org.apache.log4j,org.apache.log4j.spi,javax.naming,org.apache.log4j.helpers,javax.jms,org.apache.log4j.xml,javax.mail,javax.mail.internet,org.w3c.dom,javax.jmdns",
org.apache.log4j.nt;      version="1.2.17"; uses:="org.apache.log4j.helpers,org.apache.log4j,org.apache.log4j.spi",
org.apache.log4j.or.jms;  version="1.2.17"; uses:="org.apache.log4j.helpers,javax.jms,org.apache.log4j.or",
org.apache.log4j.or.sax;  version="1.2.17"; uses:="org.apache.log4j.or,org.xml.sax",
org.apache.log4j.rewrite; version="1.2.17"; uses:="org.apache.log4j,org.apache.log4j.spi,org.apache.log4j.helpers,org.apache.log4j.xml,org.w3c.dom",
org.apache.log4j.varia;   version="1.2.17"; uses:="org.apache.log4j.spi,org.apache.log4j,org.apache.log4j.helpers"
----

Not exported packages:

* org.apache.log4j.chainsaw
* org.apache.log4j.lf5

`pax-logging-log4j1` (before it was removed) did not export anything.

Additionally, apache-log4j-extras-1.2.17.jar has some new packages:

OSGi Exported:

* org.apache.log4j.extras
* org.apache.log4j.filter
* org.apache.log4j.rolling
* org.apache.log4j.rule

Not OSGi exported:

* org.apache.log4j.component
* org.apache.log4j.receivers

apache-log4j-extras-1.2.17.jar duplicates some packages from log4j-1.2.17.jar, but with additional classes (most of the classes are the same):

* org.apache.log4j (has additional `DBAppender.class`, `LoggerRepositoryExImpl.class` (with 2 inner classes))
* org.apache.log4j.pattern (has additional `ExtrasFormattingInfo.class`, `ExtrasPatternParser.class` and `ExtrasPatternParser$ReadOnlyMap.class`)
* org.apache.log4j.spi (has additional `LoggingEventFieldResolver.class`)
* org.apache.log4j.varia (has additional `SoundAppender.class`)
* org.apache.log4j.xml (has additional `XSLTLayout.class`)

With PAXLOGGING-252, I'd like to make it easier to maintain pax-logging itself. The goals (and kind of work log) are:

* if some classes are needed from original Log4J1 (and later with Log4J2 too) they should be Export-Packaged
* if some classes have to be adjusted for pax-logging (OSGi in general), they should be copied _and committed_ without changing. Changes should be done in separate commit to distinguish original version from changes.
* log4j classes should only be exported by pax-logging-api *or* Private-Packaged by pax-logging-log4j1 - never both (so far it was the case with `org.apache.log4j.Category`)
* I'm going to export `org.apache.log4j` package with the closure of _uses_, which is:
** org.apache.log4j
** org.apache.log4j.config
** org.apache.log4j.helpers
** org.apache.log4j.or
** org.apache.log4j.pattern
** org.apache.log4j.spi
** org.apache.log4j.xml
* possibly the above list will change, if some pax-logging adjustments will remove some _uses_ from the closure.
* I've removed _all_ log4j1 sources from pax-logging, I'm going to copy `org.apache.log4j.Logger`, `org.apache.log4j.MDC` and `org.apache.log4j.NDC` classes and the classes they require, reapply _all_ the changes done so far in pax-logging-api with better tracking (_diffability_, _cherrypickability_) and finally remove the sources that don't have changes (those classes will then be simply Export-Packaged from log4j:log4j dependency).
* After adjusting some classes to pax-logging (like making configuration methods dummy), it turned out that these packages don't have to be exported:
** org.apache.log4j.config
** org.apache.log4j.xml
* But because `org.apache.log4j.xml` was exported in previous versions of pax-logging-api, I'll leave it as is. Also because pax-logging-log4j1 requires some classes from `org.apache.log4j.config` and I don't want this bundle to duplicate any pax-logging-api classes (whether exported or private), I'll add export for `org.apache.log4j.config` package in pax-logging-api.

.Update

My plan was to export the above set of packages from pax-logging-api and import them in pax-logging-log4j1 with few exceptions. Mainly, `org.apache.log4j.Logger` class _has to_ be exported by pax-logging-api (with changes related to delegation to pax-logging services), but it also _has to_ be private packaged in pax-logging-log4j1, because it actually has to call log4j:log4j functionality (like keeping hierarchy of loggers).

OSGi R6 Core specification says:

====
*3.9.4 Overall Search Order*

Frameworks must adhere to the following rules for class or resource loading. When a bundle's class
loader is requested to load a class or find a resource, the search must be performed in the following
order:

…

*3*. If the class or resource is in a package that is imported using Import-Package or was imported dynamically in a previous load, then the request is delegated to the exporting bundle's class loader [...]

...

*5*. Search the bundle's embedded classpath.
====

So it was not possible:

* to have changed `org.apache.log4j.Logger` class exported from in pax-logging-api and
* to have unchanged `org.apache.log4j.Logger` class private-packaged in pax-logging-log4j1, while other classes from `org.apache.log4j` package kept being imported from pax-logging-api

The only solution is to *not* import `org.apache.log4j` package from pax-logging-api to pax-logging-log4j1 bundle.
Some Maven tricks (`maven-dependency-plugin:unpack`) have to be involved.

This is set of rules I found:

* first, pax-logging-api has to export consistent set of packages, even if some classes are adjusted for OSGi purposes. This is easy by Export-Packaging and copying to `src/main/java` if needed
* if pax-logging-log4j1 can use *all* the classes from one of the above exported packages from pax-logging-api, it should import them
* if there's at least one class from the above exported packages, that has to be different in pax-logging-log4j1 (like `org.apache.log4j.Category` or `org.apache.log4j.helpers.AppenderAttachableImpl`), then pax-logging-log4j1 has to Private-Package such package
* but because Private-Package handling (by maven-bundle-plugin and bndlib) involves discovery using classpath, we have to be careful. We can only assume that `org.apache.felix.bundleplugin.BundlePlugin.getClasspath()` method uses `currentProject.getBuild().getOutputDirectory()` as *first* directory/location when checking the package.
* because `org.apache.log4j` package is available both from pax-logging-api and log4j:log4j (and log4j:apache-log4j-extras) dependencies of pax-logging-log4j1, we have to ensure that classes from log4j:log4j are taken. Instead of relying on `<dependency>` order in pax-logging-log4j1 POM, we rather use `maven-dependency-plugin:unpack` with this configuration:

=== Summary of package splitting for Log4J1 (deprecated information in 1.12.x and 2.1.x)

I think users deserve this summary, because there are 4 bundles/jars:

* pax-logging-api
* pax-logging-log4j1 (the Log4J1 _backend_)
* log4j:log4j - the implementation
* log4j:apache-log4j-extras which is log4j:log4j + some additional classes

And there's this design flaw that single JAR is treated as both API and Implementation (what's worse - some packages mix API and Implementation classes).

log4j:apache-log4j-extras source JAR (and github repository) duplicates these packages from log4j:log4j:

* org.apache.log4j
* org.apache.log4j.pattern
* org.apache.log4j.spi
* org.apache.log4j.varia
* org.apache.log4j.xml

But fortunately doesn't duplicate any of actual source files.

log4j:apache-log4j-extras JAR duplicates the above packages where the classes are simply merged from own project and from log4j:log4j JAR. However, pax-logging-api re-exports `org.apache.log4j`, `org.apache.log4j.pattern`, `org.apache.log4j.spi` and `org.apache.log4j.xml` from the log4j:log4j JAR, not from log4j:apache-log4j-extras, because some additional classes (like `org.apache.log4j.DBAppender`) introduce too many additional packages that have to be re-exported (because of `uses` clause).

Here's full list of packages and notes about how it's used in pax-logging.

org.apache.log4j::
This is the main package mixing all kinds of classes (API, Implementation, internal functionality, ...)

* pax-logging-api re-exports all the classes from log4j:log4j, but `BasicConfigurator`, `Category`, `Hierarchy`, `Logger`, `LogManager`, `MDC`, `NDC`, `Priority` and `PropertyConfigurator` are changed to adjust them for OSGi/pax-logging requirements. The changes turn some methods into noop variants. While factory methods (the most important _get logger_ for example) delegate to pax logging services to obtain loggers.
* pax-logging-log4j1 doesn't import this package from pax-logging-api, instead it Private-Packages all the classes from log4j:apache-log4j-extras without exporting, but there are some additional and changed classes:
** `AsyncAppender` has fixes related to https://ops4j1.jira.com/browse/PAXLOGGING-101[PAXLOGGING-101] and https://ops4j1.jira.com/browse/PAXLOGGING-182[PAXLOGGING-182]
** `Category` has fixes related to https://ops4j1.jira.com/browse/PAXLOGGING-99[PAXLOGGING-99] and https://ops4j1.jira.com/browse/PAXLOGGING-182[PAXLOGGING-182]
** `ConsoleAppender` has fixes related to https://ops4j1.jira.com/browse/PAXLOGGING-90[PAXLOGGING-90]
** There's new `DailyZipRollingFileAppender` class related to https://ops4j1.jira.com/browse/PAXLOGGING-226[PAXLOGGING-226] - it's not available in original Log4J1
** There's new `OsgiThrowableRenderer` introduced with https://ops4j1.jira.com/browse/PAXLOGGING-80[PAXLOGGING-80]
** There's new `PaxLoggingConfigurator` that handles special, OSGi configuration parsing (with references to OSGi services implementing interfaces from `org.ops4j.pax.logging.spi` package)
** There's new `SanitizingPatternLayout` introduced with https://ops4j1.jira.com/browse/PAXLOGGING-201[PAXLOGGING-201]

org.apache.log4j.chainsaw::
This package comes from log4j:log4j and is Private-Packaged in pax-logging-log4j1 without changes.

org.apache.log4j.component.*::
This package (and subpackages) comes from log4j:apache-log4j-extras and is Private-Packaged in pax-logging-log4j1 without changes.

org.apache.log4j.config::
This package comes from log4j:log4j.

* It's exported from pax-logging-api without changes
* It's Private-Packaged in pax-logging-log4j1 from log4j:log4j without importing from pax-logging-api. There's one additional class:
** `PaxPropertySetter' which is a copy of `PropertySetter` with fixes related to https://ops4j1.jira.com/browse/PAXLOGGING-83[PAXLOGGING-83]

org.apache.log4j.extras::
This package comes from log4j:apache-log4j-extras and is Private-Packaged in pax-logging-log4j1 without changes.

org.apache.log4j.filter::
This package comes from log4j:apache-log4j-extras and is Private-Packaged in pax-logging-log4j1. pax-logging-log4j1 contains additional classes:

* `MatchFilterBase` and `MDCMatchFilter` come from abandoned Log4J 1.3 release moved at some point to log4j-sandbox

org.apache.log4j.helpers::
This package is tricky. It's in `uses` closure of packages exported from pax-logging-api, but pax-logging-log4j1 can't import it. pax-logging-log4j1 fixes performance problems with `AppenderAttachableImpl`, but it can't import this package from pax-logging-api, because it can't import `org.apache.log4j` package and this _root_ package contains `org.apache.log4j.Appender` class which is used as argument to some of `AppenderAttachableImpl` methods.

* pax-logging-api re-exports this package from log4j:log4j and:
** changes `Loader` class to load classes using OSGi methods
** changes `LogLog` class to delegate to fallback logger from pax-logging-api itself
** adds `MessageFormatter` class from sandbox/abandoned Log4J1 1.3

* pax-logging-log4j1 Private-Packages this package from ... pax-logging-api (to include the fixes for `Loader` and `LogLog` classes) and:
** `AppenderAttachableImpl` has fixes related to https://ops4j1.jira.com/browse/PAXLOGGING-182[PAXLOGGING-182]

org.apache.log4j.jdbc::
This package comes from log4j:log4j and is Private-Packaged in pax-logging-log4j1 without changes.

org.apache.log4j.jmx::
This package comes from log4j:log4j and is Private-Packaged in pax-logging-log4j1 without changes.

org.apache.log4j.lf5.*::
This package (and subpackages) comes from log4j:log4j and is Private-Packaged in pax-logging-log4j1 without changes.

org.apache.log4j.net::
This package comes from log4j:log4j and is Private-Packaged in pax-logging-log4j1 without changes.

org.apache.log4j.nt::
This package comes from log4j:log4j and is Private-Packaged in pax-logging-log4j1 without changes.

org.apache.log4j.or (Object Renderer)::

* pax-logging-api re-exports this package from log4j:log4j without changes, because it's in the `uses` closure of the exported Log4J1 API.
* pax-logging-log4j1 imports this package from pax-logging-api, because it doesn't add any own changes

org.apache.log4j.or.jms::
This package comes from log4j:log4j and is Private-Packaged in pax-logging-log4j1 without changes.

org.apache.log4j.or.sax::
This package comes from log4j:log4j and is Private-Packaged in pax-logging-log4j1 without changes.

org.apache.log4j.pattern::
This package comes from log4j:log4j, but log4j:apache-log4j-extras adds `ExtrasFormattingInfo` and `ExtrasPatternParser`.

* pax-logging-api exports this package from log4j:log4j (because pax-logging-api can't have Maven dependency on log4j:apache-log4j-extras) and keeps a copy if these two additional classes taken directly from log4j:apache-log4j-extras
* pax-logging-log4j1 imports this package from pax-logging-api

org.apache.log4j.receivers.*::
This package (and subpackages) comes from log4j:apache-log4j-extras and is Private-Packaged in pax-logging-log4j1 without changes.

org.apache.log4j.rewrite::
This package comes from log4j:log4j and is Private-Packaged in pax-logging-log4j1 without changes.

org.apache.log4j.rolling.*::
This package (and subpackages) comes from log4j:apache-log4j-extras and is Private-Packaged in pax-logging-log4j1.

* `RollingFileAppender` has fixes related to https://ops4j1.jira.com/browse/PAXLOGGING-189[PAXLOGGING-189]

org.apache.log4j.rule::
This package comes from log4j:apache-log4j-extras and is Private-Packaged in pax-logging-log4j1 without changes.

org.apache.log4j.sift::
That's entirely pax-logging-log4j1 private package with `MDCSiftingLoggingAppender` class created for https://ops4j1.jira.com/browse/PAXLOGGING-83[PAXLOGGING-83]

org.apache.log4j.spi::

* pax-logging-api re-exports this package from log4j:log4j without changes
* pax-logging-log4j1 doesn't import this package from pax-logging-api, instead, it Private-Packages it from both log4j:log4j and log4j:apache-log4j-extras.
** log4j:apache-log4j-extras has `LoggingEventFieldResolver` - it couldn't be exported from pax-logging-api because it requires classes from `org.apache.log4j.rule` package, which we don't want to export from pax-logging-api
** pax-logging-log4j1 adds `OptionFactory` - new class created for https://ops4j1.jira.com/browse/PAXLOGGING-83[PAXLOGGING-83]

org.apache.log4j.varia::
This package comes from both log4j:log4j and log4j:apache-log4j-extras and is Private-Packaged in pax-logging-log4j1 without changes.

org.apache.log4j.xml::
This package comes from both log4j:log4j and log4j:apache-log4j-extras (which adds `XSLTLayout` class).

* pax-logging-api re-exports this package from log4j:log4j, and:
** adds `XSLTLayout` copied directly from log4j:apache-log4j-extras to own `src/main/java`
** changes `DOMConfigurator`, so methods are effectively no-op
* pax-logging-log4j1 imports this package from pax-logging-api

org.apache.log4j.zip::
That's entirely pax-logging-log4j1 private package with `ZipRollingFileAppender` class created for https://ops4j1.jira.com/browse/PAXLOGGING-116[PAXLOGGING-116]

=== Location Info

When Log4J1 is used with pattern layout that deals with class/method names and/or file names and line numbers, there's a need to analyze stack trace to get this info.

When log4J1 is called normally, without ANY facade (and outside of pax-logging), the relevant stack trace fragment is:

[listing,options=nowrap]
----
"main@1" prio=5 tid=0x1 nid=NA runnable
  java.lang.Thread.State: RUNNABLE
	  at org.apache.log4j.spi.LocationInfo.<init>(LocationInfo.java:144)
	  at org.apache.log4j.spi.LoggingEvent.getLocationInformation(LoggingEvent.java:253)
	  at org.apache.log4j.helpers.PatternParser$LocationPatternConverter.convert(PatternParser.java:500)
	  at org.apache.log4j.helpers.PatternConverter.format(PatternConverter.java:65)
	  at org.apache.log4j.PatternLayout.format(PatternLayout.java:506)
	  at org.apache.log4j.WriterAppender.subAppend(WriterAppender.java:310)
	  at org.apache.log4j.WriterAppender.append(WriterAppender.java:162)
	  at org.apache.log4j.AppenderSkeleton.doAppend(AppenderSkeleton.java:251)
	  at org.apache.log4j.helpers.AppenderAttachableImpl.appendLoopOnAppenders(AppenderAttachableImpl.java:66)
	  at org.apache.log4j.Category.callAppenders(Category.java:206)
	  at org.apache.log4j.Category.forcedLog(Category.java:391)
	  at org.apache.log4j.Category.info(Category.java:666)
	  at org.ops4j.pax.logging.test.log4j1.Log4j1NativeApiTest.loggerAPI(Log4j1NativeApiTest.java:80)
...
----

The discovered class name shuold be `org.ops4j.pax.logging.test.log4j1.Log4j1NativeApiTest`.
What log4j ensures to make it work is passing `org.apache.log4j.Category.FQCN` (or `org.apache.log4j.Logger.FQCN`) value down through `org.apache.log4j.Category.forcedLog` method. Then the last stack trace element before `FQCN` is used to collection location info.

When Log4J1 is used through SLF4J, `org.slf4j.impl.Log4jLoggerAdapter.FQCN` is used to pass through `org.apache.log4j.Category.log()` and `org.apache.log4j.Category.callAppenders()`.

With pax-logging, the stack trace is a bit more complex:
[listing,options=nowrap]
----
"Karaf Shell Console Thread@9179" daemon prio=5 tid=0x31 nid=NA runnable
  java.lang.Thread.State: RUNNABLE
	  at org.apache.log4j.spi.LocationInfo.<init>(LocationInfo.java:136)
	  at org.apache.log4j.spi.LoggingEvent.getLocationInformation(LoggingEvent.java:253)
	  at org.apache.log4j.helpers.PatternParser$ClassNamePatternConverter.getFullyQualifiedName(PatternParser.java:555)
	  at org.apache.log4j.helpers.PatternParser$NamedPatternConverter.convert(PatternParser.java:528)
	  at org.apache.log4j.helpers.PatternConverter.format(PatternConverter.java:65)
	  at org.apache.log4j.PatternLayout.format(PatternLayout.java:506)
	  at org.apache.log4j.WriterAppender.subAppend(WriterAppender.java:310)
	  at org.apache.log4j.RollingFileAppender.subAppend(RollingFileAppender.java:276)
	  at org.apache.log4j.WriterAppender.append(WriterAppender.java:162)
	  at org.apache.log4j.AppenderSkeleton.doAppend(AppenderSkeleton.java:251)
	  - locked <0x2402> (a org.apache.log4j.RollingFileAppender)
	  at org.apache.log4j.helpers.AppenderAttachableImpl.appendLoopOnAppenders(AppenderAttachableImpl.java:59)
	  at org.apache.log4j.Category.callAppenders(Category.java:179)
	  at org.apache.log4j.Category.forcedLog(Category.java:333)
	  at org.apache.log4j.Category.log(Category.java:724)
	  at org.ops4j.pax.logging.log4j1.internal.PaxLoggerImpl.doLog0(PaxLoggerImpl.java:152)
	  at org.ops4j.pax.logging.log4j1.internal.PaxLoggerImpl.doLog(PaxLoggerImpl.java:145)
	  at org.ops4j.pax.logging.log4j1.internal.PaxLoggerImpl.inform(PaxLoggerImpl.java:179)
	  at org.ops4j.pax.logging.internal.TrackingLogger.inform(TrackingLogger.java:86)
	  at org.ops4j.pax.logging.slf4j.Slf4jLogger.info(Slf4jLogger.java:476)
	  at org.ops4j.pax.logging.test.log4j1.Log4j1PaxLoggingApiTest.loggerAPI(...)
...
----

And the FQCN that's equal to `org.ops4j.pax.logging.slf4j.Slf4jLogger` is ensured by pax-logging-api and shaded classes from given facade (here - SLF4J).

When pax-logging is used with Log4J1 and without SLF4J, stack trace is like:
[listing,options=nowrap]
----
"Karaf Shell Console Thread@9190" daemon prio=5 tid=0x31 nid=NA runnable
  java.lang.Thread.State: RUNNABLE
	  at org.apache.log4j.spi.LocationInfo.<init>(LocationInfo.java:136)
	  at org.apache.log4j.spi.LoggingEvent.getLocationInformation(LoggingEvent.java:253)
	  at org.apache.log4j.helpers.PatternParser$ClassNamePatternConverter.getFullyQualifiedName(PatternParser.java:555)
	  at org.apache.log4j.helpers.PatternParser$NamedPatternConverter.convert(PatternParser.java:528)
	  at org.apache.log4j.helpers.PatternConverter.format(PatternConverter.java:65)
	  at org.apache.log4j.PatternLayout.format(PatternLayout.java:506)
	  at org.apache.log4j.WriterAppender.subAppend(WriterAppender.java:310)
	  at org.apache.log4j.RollingFileAppender.subAppend(RollingFileAppender.java:276)
	  at org.apache.log4j.WriterAppender.append(WriterAppender.java:162)
	  at org.apache.log4j.AppenderSkeleton.doAppend(AppenderSkeleton.java:251)
	  - locked <0x240e> (a org.apache.log4j.RollingFileAppender)
	  at org.apache.log4j.helpers.AppenderAttachableImpl.appendLoopOnAppenders(AppenderAttachableImpl.java:59)
	  at org.apache.log4j.Category.callAppenders(Category.java:179)
	  at org.apache.log4j.Category.forcedLog(Category.java:333)
	  at org.apache.log4j.Category.log(Category.java:724)
	  at org.ops4j.pax.logging.log4j1.internal.PaxLoggerImpl.doLog0(PaxLoggerImpl.java:152)
	  at org.ops4j.pax.logging.log4j1.internal.PaxLoggerImpl.doLog(PaxLoggerImpl.java:145)
	  at org.ops4j.pax.logging.log4j1.internal.PaxLoggerImpl.inform(PaxLoggerImpl.java:179)
	  at org.ops4j.pax.logging.internal.TrackingLogger.inform(TrackingLogger.java:86)
	  at org.apache.log4j.Category.info(Category.java:623)
	  at org.apache.log4j.Logger.info(Logger.java:585)
	  at org.ops4j.pax.logging.test.log4j1.Log4j1PaxLoggingApiTest.loggerAPI(...)
...
----

So the FQCN should be `org.apache.log4j.Logger`. Even if the logger is obtained via `org.apache.log4j.Category` static methods, the logger is of `org.apache.log4j.Logger` class and stack trace analysis works without problems.
Also, trace/debug/info/warn/error/fatal methods are defined in `Category` class, but overriden in `Logger`, to properly detect the calling class/method.

But not all logging methods are overriden...
[listing,options=nowrap]
----
"Karaf Shell Console Thread@9205" daemon prio=5 tid=0x31 nid=NA runnable
  java.lang.Thread.State: RUNNABLE
	  at org.apache.log4j.spi.LocationInfo.<init>(LocationInfo.java:136)
	  at org.apache.log4j.spi.LoggingEvent.getLocationInformation(LoggingEvent.java:253)
	  at org.apache.log4j.helpers.PatternParser$ClassNamePatternConverter.getFullyQualifiedName(PatternParser.java:555)
	  at org.apache.log4j.helpers.PatternParser$NamedPatternConverter.convert(PatternParser.java:528)
	  at org.apache.log4j.helpers.PatternConverter.format(PatternConverter.java:65)
	  at org.apache.log4j.PatternLayout.format(PatternLayout.java:506)
	  at org.apache.log4j.WriterAppender.subAppend(WriterAppender.java:310)
	  at org.apache.log4j.RollingFileAppender.subAppend(RollingFileAppender.java:276)
	  at org.apache.log4j.WriterAppender.append(WriterAppender.java:162)
	  at org.apache.log4j.AppenderSkeleton.doAppend(AppenderSkeleton.java:251)
	  - locked <0x240c> (a org.apache.log4j.RollingFileAppender)
	  at org.apache.log4j.helpers.AppenderAttachableImpl.appendLoopOnAppenders(AppenderAttachableImpl.java:59)
	  at org.apache.log4j.Category.callAppenders(Category.java:179)
	  at org.apache.log4j.Category.forcedLog(Category.java:333)
	  at org.apache.log4j.Category.log(Category.java:724)
	  at org.ops4j.pax.logging.log4j1.internal.PaxLoggerImpl.doLog0(PaxLoggerImpl.java:152)
	  at org.ops4j.pax.logging.log4j1.internal.PaxLoggerImpl.doLog(PaxLoggerImpl.java:145)
	  at org.ops4j.pax.logging.log4j1.internal.PaxLoggerImpl.inform(PaxLoggerImpl.java:179)
	  at org.ops4j.pax.logging.internal.TrackingLogger.inform(TrackingLogger.java:86)
	  at org.apache.log4j.Category.info(Category.java:644)
	  at org.apache.log4j.Logger.info(Logger.java:589)
	  at org.apache.log4j.Category.log(Category.java:858)
	  at org.apache.log4j.Category.log(Category.java:829)
	  at org.ops4j.pax.logging.test.log4j1.Log4j1PaxLoggingApiTest.loggerAPI(...)
...
----

When calling `org.apache.log4j.Category.log(org.apache.log4j.Priority, java.lang.Object)` directly, the method is defined in `Category` class, so when analyzing stack trace, `org.apache.log4j.Category.log(Category.java:858)` will be detected as logging event location. This will be fixed with PAXLOGGING-252.

The location info should be `org.ops4j.pax.logging.test.OsgiLogServiceApiTest.logServiceAPI()`.
FQCN is ... `""` location can't be found and in logs we can see (for pattern `%d{ISO8601} | %-5.5p | {%t} [%c]/[%C] (%F:%L) | %m%n` and symbolic name = `my-bundle`):
[listing,options=nowrap]
----
2019-04-26 08:11:53,126 | INFO  | {Karaf Shell Console Thread} [my-bundle]/[?] (?:?) | Hello!
----

=== API / Implementation separation

The biggest problem with Log4J1 is not only OSGi-specific problem of having API and implementation classes in single log4j:log4j library. Even methods are mixed within _single class_.

`org.apache.log4j.Logger` (together with its superclass `org.apache.log4j.Category`) class contains ~80 methods.
These methods can be groupped into:

* factory methods used to obtain a _logger_ (which is of the same instance `org.apache.log4j.Logger`): `getLogger`, `getInstance`, `getRootLogger`, ...
* logging methods used to log messages: `info`, `debug`, `warn`, ... (with different parameter list)
* logging threshold methods: `isInfoEnabled`, `isDebugEnabled`, ...
* methods related to appenders: `addAppender`, `isAttached`, ... - these methods allow (in original usage) to attach appenders to loggers dynamically. In OSGi it doesn't make sense, because Log4J1 *API* may be used to log messages which are eventually handled by Logback or Log4J2 backend (or even `DefaultServiceLog` if pax-logging backend is not (yet) installed)
* methods related to logger configuration: `getAddittivity`, `getParent`, `setLevel`, ...
* meta methods related to _logging repository_: `getCurrentCategories`, `getHierarchy`, `getLoggerRepository`, `shutdown`, ... - these methods are generally throwing `UnsupportedOperationException` in pax-logging-api.

The above groupping is much better implemented in other logging frameworks which have separate logger and factory classes and also do the configuration and all the _meta_ in different way (than through single _logger_ class).

== Logback

As mentioned on https://logback.qos.ch/[project's web page], Logback _picks up where log4j leaves off_.

Logback was created after the logging-bridge (r)evolution and even if it may be used without any logging facade/bridge, it is very uncommon to do so. That's why there are no special API classes in pax-logging-api related to Logback. Logback is handled by pax-logging _only_ through implementation of `org.ops4j.pax.logging.PaxLoggingService`.

Logback is mostly used behind SLF4J facade and both logger factory and MDC/NDC API comes from SLF4J itself when dealing with Logback.

Logback is initialized using `org.slf4j.impl.StaticLoggerBinder` Slf4J mechanism - but only if such class is
explicitly requested/loaded (e.g., through `org.slf4j.LoggerFactory.getLogger()` and `org.slf4j.LoggerFactory.bind()`).
With pax-logging-logback, Logback's version of `org.slf4j.impl.StaticLoggerBinder` is neither exported nor used.

pax-logging-logback implementation of `org.ops4j.pax.logging.PaxLoggingService` explicitly configures `ch.qos.logback.classic.LoggerContext` instance (which, by the way, implements `org.slf4j.ILoggerFactory`).

=== Logback contrib

See https://github.com/qos-ch/logback-contrib

There are several additional JARs we Private-Package in pax-logging-logback:

* logback-jackson
* logback-json-core
* logback-json-classic

After private-packaging the above, I've adjusted the generated `Import-Package` header providing explicit version ranges for Groovy and Jackson and making some imports optional.

== Log4J2

After huge (in my humble, subjective opinion) success of Logback, Log4J2 was created as modernized version of original Log4j project with full awareness of logging bridges/facades and weird properties file syntax.

pax-logging provides dedicated implementation of `org.ops4j.pax.logging.PaxLoggingService` that delegates to Log4J2.

Again, Log4J2 itself may be used without bridge/facade and (differently than with Logback) pax-logging fully supports its native API.

Here's a list of all `org.apache.logging.log4j` artifacts I found in version 2.11.2:

* org.apache.logging.log4j:log4j-api
* org.apache.logging.log4j:log4j-1.2-api
* org.apache.logging.log4j:log4j-appserver
* org.apache.logging.log4j:log4j-cassandra
* org.apache.logging.log4j:log4j-core
* org.apache.logging.log4j:log4j-couchdb
* org.apache.logging.log4j:log4j-flume-ng
* org.apache.logging.log4j:log4j-iostreams
* org.apache.logging.log4j:log4j-jcl
* org.apache.logging.log4j:log4j-jdbc-dbcp2
* org.apache.logging.log4j:log4j-jmx-gui
* org.apache.logging.log4j:log4j-jpa
* org.apache.logging.log4j:log4j-jul
* org.apache.logging.log4j:log4j-liquibase
* org.apache.logging.log4j:log4j-mongodb2
* org.apache.logging.log4j:log4j-mongodb3
* org.apache.logging.log4j:log4j-osgi
* org.apache.logging.log4j:log4j-slf4j-impl
* org.apache.logging.log4j:log4j-slf4j18-impl
* org.apache.logging.log4j:log4j-taglib
* org.apache.logging.log4j:log4j-to-slf4j
* org.apache.logging.log4j:log4j-web

Currently, pax-logging uses 3:

* org.apache.logging.log4j:log4j-api
* org.apache.logging.log4j:log4j-core
* org.apache.logging.log4j:log4j-slf4j-impl

I'm going to include some more just like with `log4j:apache-log4j-extras` and `ch.qos.logback.contrib`.

These won't be supported/embedded/referenced:

* org.apache.logging.log4j:log4j-1.2-api - it's Log4J1 "API" (with all the restrictions I mentioned when talking about API/Impl separation problems of Log4J1) and actually it's very similar to how pax-logging-api itself changes original Log4J1 classes
* org.apache.logging.log4j:log4j-jcl - it's Apache Commons Logging _service_ defined in `/META-INF/services/org.apache.commons.logging.LogFactory`, effectively bridging Apache Commons Logging directly into Log4J2. pax-logging-api does it a bit differently.
* org.apache.logging.log4j:log4j-jul - it provides `java.util.logging.LogManager` implementation to be used with `-Djava.util.logging.manager` system property. pax-logging-api however registers global `java.util.logging.Handler` which bridges Java Util Logging into pax-logging.
* org.apache.logging.log4j:log4j-slf4j-impl - provides org.apache.logging.slf4j.Log4jLoggerFactory which is implementation of Slf4J's `org.slf4j.ILoggerFactory`. pax-logging-api provides own `org.slf4j.impl.StaticLoggerBinder` with own `org.slf4j.ILoggerFactory` implementation
* org.apache.logging.log4j:log4j-slf4j18-impl - just like the above, but for Slf4J 1.8.x (still beta at the time of writing)
* org.apache.logging.log4j:log4j-to-slf4j - is a library that enforces kind of _reversed_ usage. Log4J2 API calls are directed to Slf4J which (by design) has to be bridged to target logging framework. See https://logging.apache.org/log4j/2.x/log4j-to-slf4j/index.html. This definitely isn't something pax-logging should support.
* org.apache.logging.log4j:log4j-osgi - strange "bundle" including only some non-pax-exam tests that install other Log4J2 bundles.
* org.apache.logging.log4j:log4j-appserver - `org.apache.logging.log4j.appserver.jetty.Log4j2Logger` (Jetty) and `org.apache.logging.log4j.appserver.tomcat.TomcatLogger` (Tomcat JULI) implementations
* org.apache.logging.log4j:log4j-web - `/META-INF/services/javax.servlet.ServletContainerInitializer` service that installs `org.apache.logging.log4j.web.Log4jServletFilter` filter and `org.apache.logging.log4j.web.Log4jServletContextListener` listener
* org.apache.logging.log4j:log4j-taglib - `http://logging.apache.org/log4j/tld/log` tag library to be used in JSP pages
* org.apache.logging.log4j:log4j-jmx-gui - `/META-INF/services/com.sun.tools.jconsole.JConsolePlugin` service for JConsole.
* org.apache.logging.log4j:log4j-liquibase - bridges `liquibase.logging.core.AbstractLogger` into Log4J2

The remaining Log4J2 artifacts can be split into 3 categories:

* API - log4j-api - to be included in (handled by) pax-logging-api (I hope)
* Implementation - log4j-core - to be included in pax-logging-log4j2
* Additional appenders, specialized `org.apache.logging.log4j.core.appender.db.jdbc.AbstractConnectionSource` or similar extensions:
** log4j-iostreams - `java.io` bridges to Log4J2. See https://logging.apache.org/log4j/2.x/log4j-iostreams/index.html
** log4j-jdbc-dbcp2
** log4j-jpa
** log4j-cassandra
** log4j-couchdb
** log4j-mongodb2
** log4j-mongodb3
** log4j-flume-ng - see https://logging.apache.org/log4j/2.x/log4j-flume-ng/index.html

The original exports of `org.apache.logging.log4j:log4j-api` are:

[listing,options=nowrap]
----
org.apache.logging.log4j;         version="2.11.2"; uses:="org.apache.logging.log4j.message, org.apache.logging.log4j.spi, org.apache.logging.log4j.util"
org.apache.logging.log4j.message; version="2.11.2"; uses:="org.apache.logging.log4j.util"
org.apache.logging.log4j.simple;  version="2.11.2"; uses:="org.apache.logging.log4j,org.apache.logging.log4j.message,org.apache.logging.log4j.spi,org.apache.logging.log4j.util"
org.apache.logging.log4j.spi;     version="2.11.2"; uses:="org.apache.logging.log4j,org.apache.logging.log4j.message,org.apache.logging.log4j.util"
org.apache.logging.log4j.status;  version="2.11.2"; uses:="org.apache.logging.log4j,org.apache.logging.log4j.message,org.apache.logging.log4j.spi"
org.apache.logging.log4j.util;    version="2.11.2"; uses:="org.apache.logging.log4j.message,org.apache.logging.log4j.spi,org.osgi.framework"
----

This perfectly matches what pax-logging-api (re)exported. These are actually all the packages included in `org.apache.logging.log4j:log4j-api`.

=== Plugins

Log4J2 is extended using plugin system. Quoting http://lo[the manual]:

====
In Log4j 2 a plugin is declared by adding a `@Plugin` annotation to the class declaration. During initialization the `Configuration` will invoke the `PluginManager` to load the built-in Log4j plugins as well as any custom plugins. The `PluginManager` locates plugins by looking in five places:

1. Serialized plugin listing files on the classpath. These files are generated automatically during the build (more details below).
2. (OSGi only) Serialized plugin listing files in each active OSGi bundle. A BundleListener is added on activation to continue checking new bundles after log4j-core has started.
3. A comma-separated list of packages specified by the log4j.plugin.packages system property.
4. Packages passed to the static PluginManager.addPackages method (before Log4j configuration occurs).
5. The packages declared in your log4j2 configuration file.
====

Currently, pax-logging doesn't do the same discovery as bundle activator of original `org.apache.logging.log4j:log4j-core`.
Though similar mechanism may be added in the future.

`org.apache.logging.log4j.core.config.plugins.util.PluginManager.collectPlugins()` collects the plugins from different sources. The cache file is declared as `org.apache.logging.log4j.core.config.plugins.processor.PluginProcessor.PLUGIN_CACHE_FILE` and refers to `META-INF/org/apache/logging/log4j/core/config/plugins/Log4j2Plugins.dat`. It's a binary file conforming to `java.io.DataInputStream` which may occur multiple times on the classpath.

pax-logging-log4j2 bundle directly uses the original plugin cache file from `org.apache.logging.log4j:log4j-core` and additional plugins are added using `org.apache.logging.log4j.core.config.plugins.util.PluginManager.addPackage()` during pax-logging-log4j2 initialization.

Default cache file contains exactly these categories and numbers of plugins (206 total):
[listing,options=nowrap]
----
cache = {org.apache.logging.log4j.core.config.plugins.processor.PluginCache@1030}
 categories: java.util.Map  = {java.util.LinkedHashMap@1059}  size = 6
  "core" -> {java.util.LinkedHashMap@1069}  size = 117
  "converter" -> {java.util.LinkedHashMap@1071}  size = 44
  "lookup" -> {java.util.LinkedHashMap@1073}  size = 13
  "configurationfactory" -> {java.util.LinkedHashMap@1075}  size = 4
  "fileconverter" -> {java.util.LinkedHashMap@1077}  size = 2
  "typeconverter" -> {java.util.LinkedHashMap@1079}  size = 26
----

=== Configuration

Log4J2 has complex configuration mechanisms and can process configuration from different sources. Configuration may be stored in XML, JSON, YAML and properties files. Among these, properties file (very common in Log4J1 times) is the most confusing...

`org.apache.logging.log4j.core.config.builder.impl.DefaultConfigurationBuilder` is the most important class here that allows to understand how configuration is organized. This builder includes `org.apache.logging.log4j.core.config.builder.api.Component` _components_ for these _concepts_:

* root
* loggers
* appenders
* filters
* properties
* custom levels
* scripts

`org.apache.logging.log4j.core.config.builder.api.Component` is generally a container for:

* attributes (a map)
* child `org.apache.logging.log4j.core.config.builder.api.Component` instances
* plugin type
* name

Plugin types of components inside "root" component may be one of:

* "Scripts"
* "Loggers"
* "Appenders"
* "Filters"
* "Properties"
* "CustomLevels"

But generally, plugin _type_ is a key for org.apache.logging.log4j.core.config.plugins.util.PluginManager.plugins map which maps names to `org.apache.logging.log4j.core.config.plugins.util.PluginType`

== Testing in Karaf

That's tricky problem. If we want to use Pax Exam and test Pax Logging under Karaf with Maven we have to consider:

* Maven runs maven-surefire|failsafe-plugin test in separate JVM (by default)
* When using `pax-exam-container-karaf`, 3rd JVM process is launched
* We can't use normal Karaf (even minimal one) because it uses fixed pax-logging version for bundles started from `etc/startup.properties`
* So we have to prepare custom Karaf distribution, where initial (startup) bundles are the ones from current Pax Logging version (the one being tested)

Then, taking into account the logging process itself:

* We want logging statement issued by pax-exam itself to be handled properly (before launching Karaf) - ideally using `src/test/resources/log4j2-test.properties`
* Thus `test` classpath has to contain `org.slf4j:slf4j-api` and `org.apache.logging.log4j:log4j-slf4j-impl`
* Karaf has to start without any `org.ops4j.pax.logging` PID configured, but we want logging statements to be handled properly even if invoked from pax-logging-api bundle activator
* Thus proper _default service log_ has to be configured in `etc/config.properties`
* Remember that maven-surefire|failsafe-plugin can be configured with `<redirectTestOutputToFile>`

Summarizing (for `pax-logging-it-karaf/karaf-it`):

* there's `src/test/resources/log4j2-test.properties` with `Console` and `RollingFile` appenders. Both will start and end logging with these:
[listing,options=nowrap]
----
EXAM> 12:22:55.205 [main] INFO  (DefaultExamSystem.java:127) org.ops4j.pax.exam.spi.DefaultExamSystem - Pax Exam System (Version: 4.13.1) created.
...
EXAM> 12:22:59.423 [main] INFO  (ReactorManager.java:444) org.ops4j.pax.exam.spi.reactors.ReactorManager - suite finished
----
* for `RollingFile` appender, the output will go to `pax-logging-it-karaf/karaf-it/target/logs/pax-exam-test.log` (as configured in `src/test/resources/log4j2-test.properties`)
* for `Console` appender, the output will go to:
** stdout, if maven-failsafe-plugin is configured with `<redirectTestOutputToFile>false</redirectTestOutputToFile>`
** `pax-logging-it-karaf/karaf-it/target/failsafe-reports/org.ops4j.pax.logging.it.karaf.CleanIntegrationTest-output.txt` if maven-failsafe-plugin is configured with `<redirectTestOutputToFile>true</redirectTestOutputToFile>`
* When Karaf starts, before `pax-logging-api` is **resolved** (it doesn't have to be started/active to provide the exported classes!) each _early_ bundle (like fileinstall or configadmin) has to dynamically deal with logging. Such bundles usually don't use e.g., SLF4J API. For example, configadmin uses `org.apache.felix.cm.impl.Log` and fileinstall uses `org.apache.felix.fileinstall.internal.Util.Logger` (and subclasses). If a bundle uses e.g., SLF4J API, pax-logging-api **has to** be resolved.
* When a bundle (I created special `org.ops4j.pax.logging.karaf:karaf-base-logger`) uses e.g., SLF4J API (imports `org.slf4j` package), but pax-logging-api bundle is not yet started, console based `org.ops4j.pax.logging.spi.support.DefaultServiceLog` is used internally. Even if `org.ops4j.pax.logging.spi.support.FileServiceLog` could be used as indicated by `etc/config.properties`, it's not used when pax-logging-api is stopped (because I implemented special synchronization of file-backend for such fallback logger).
** then such bundle (e.g., in `org.ops4j.pax.logging.karaf.base.Activator.start()`) simply writes to stdout using `DefaultServiceLog`. This is printed to stdout (if maven-failsafe-plugin is told so) (with layout hardcoded in `DefaultServiceLog`):
[listing,options=nowrap]
----
org.ops4j.pax.logging.karaf.base-logger [org.ops4j.pax.logging.karaf.base.Activator] INFO : Starting before pax-logging-api
----
* Then pax-logging-api starts and in its activator again calls logging methods through various logging APIs (managed by itself). Now, because this bundle is already starting, `FileServiceLog` may be used (as indicated by `etc/config.properties` - configured using `editConfigurationFilePut("etc/custom.properties", "org.ops4j.pax.logging.useFileLogFallback", fileName)` in Pax Exam configuration). This is printed to `pax-logging-it-karaf/karaf-it/target/logs-default/CleanIntegrationTest.log`:
[listing,options=nowrap]
----
org.ops4j.pax.logging.pax-logging-api [org.ops4j.pax.logging.internal.Activator] INFO : Enabling Java Util Logging API support.
org.ops4j.pax.logging.pax-logging-api [org.ops4j.pax.logging.internal.Activator] INFO : Enabling SLF4J API support.
...
org.ops4j.pax.logging.pax-logging-api [org.ops4j.pax.logging.internal.Activator] INFO : Disabling Log4J v2 API support.
org.ops4j.pax.logging.pax-logging-api [org.ops4j.pax.logging.internal.Activator] INFO : Disabling Java Util Logging API support.
----
* Then pax-logging-log4j2 starts, finds there's no `org.ops4j.pax.logging` PID so defaults are used (in this particular case from `org.apache.logging.log4j.core.config.AbstractConfiguration.setToDefault()`). The pattern is `org.apache.logging.log4j.core.config.DefaultConfiguration.DEFAULT_PATTERN` and no file appender is configured, so this is printed to stdout (if maven-failsafe-plugin is told so):
[listing,options=nowrap]
----
13:23:05.344 [FelixStartLevel] DEBUG org.apache.felix.configadmin - Registering service [org.osgi.service.log.LogService, xxx, org.ops4j.pax.logging.PaxLoggingService, org.osgi.service.cm.ManagedService, id=15, bundle=7/mvn:org.ops4j.pax.logging/pax-logging-log4j2/1.11.0-SNAPSHOT]
13:23:05.353 [FelixStartLevel] DEBUG org.apache.felix.configadmin - Scheduling task ManagedService Update: pid=[org.ops4j.pax.logging]
13:23:05.358 [FelixStartLevel] DEBUG org.apache.felix.configadmin - [ManagedService Update: pid=[org.ops4j.pax.logging]] scheduled
13:23:05.544 [CM Configuration Updater (Update: pid=org.apache.karaf.features)] DEBUG org.apache.felix.configadmin - Running task Update: pid=org.apache.karaf.features
...
13:23:06.664 [BundleWatcher: 1] DEBUG org.ops4j.pax.exam.raw.extender.intern.Probe - Registering Service: org.ops4j.pax.exam.ProbeInvoker with Probe-Signature="PaxExam-3be14d5b-583b-4688-ae78-9f8c1c2ef280" and expression="org.ops4j.pax.logging.it.karaf.CleanIntegrationTest;justRun"
13:23:06.786 [RMI TCP Connection(1)-127.0.0.1] INFO  org.ops4j.pax.exam.invoker.junit.internal.ContainerTestRunner - running justRun in reactor
13:23:06.789 [RMI TCP Connection(1)-127.0.0.1] INFO  org.ops4j.pax.logging.it.karaf.AbstractControlledIntegrationTestBase - ========== Running org.ops4j.pax.logging.it.karaf.CleanIntegrationTest.justRun() ==========
13:23:06.790 [RMI TCP Connection(1)-127.0.0.1] INFO  org.ops4j.pax.logging.it.karaf.CleanIntegrationTest - #0: org.apache.felix.framework (System Bundle)
13:23:06.790 [RMI TCP Connection(1)-127.0.0.1] INFO  org.ops4j.pax.logging.it.karaf.CleanIntegrationTest - #1: org.ops4j.pax.logging.karaf.base-logger (mvn:org.ops4j.pax.logging.karaf/karaf-base-logger/1.11.0-SNAPSHOT)
...
13:23:06.815 [FelixStartLevel] DEBUG org.ops4j.pax.swissbox.extender.BundleWatcher - Releasing bundle [org.apache.geronimo.specs.geronimo-atinject_1.0_spec]
13:23:06.825 [FelixStartLevel] DEBUG org.apache.felix.configadmin - Unregistering service [org.osgi.service.cm.ManagedService, id=28, bundle=8/mvn:org.apache.karaf.features/org.apache.karaf.features.core/4.2.6]
org.ops4j.pax.logging.pax-logging-api [org.ops4j.pax.logging.karaf.base.Activator] INFO : Stopping after pax-logging-api
----
* The above listing contains entries from logging invocations made inside `@Test` methods using SLF4J API.
