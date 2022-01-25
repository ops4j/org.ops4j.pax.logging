/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Contibutors: Alex Blewitt <Alex.Blewitt@ioshq.com>
//              Markus Oestreicher <oes@zurich.ibm.com>
//              Frank Hoering <fhr@zurich.ibm.com>
//              Nelson Minar <nelson@media.mit.edu>
//              Jim Cakalic <jim_cakalic@na.biomerieux.com>
//              Avy Sharell <asharell@club-internet.fr>
//              Ciaran Treanor <ciaran@xelector.com>
//              Jeff Turner <jeff@socialchange.net.au>
//              Michael Horwitz <MHorwitz@siemens.co.za>
//              Calvin Chan <calvin.chan@hic.gov.au>
//              Aaron Greenhouse <aarong@cs.cmu.edu>
//              Beat Meier <bmeier@infovia.com.ar>
//              Colin Sampaleanu <colinml1@exis.com>

package org.apache.log4j;

import org.apache.log4j.helpers.MessageFormatter;
import org.apache.log4j.spi.AppenderAttachable;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.helpers.NullEnumeration;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingManager;
import org.ops4j.pax.logging.PaxLoggingManagerAwareLogger;
import org.ops4j.pax.logging.spi.support.FallbackLogFactory;
import org.osgi.framework.FrameworkUtil;

import java.util.Enumeration;
import java.util.ResourceBundle;

/**
 * <font color="#AA2222"><b>This class has been deprecated and replaced by the
 * {@link Logger} <em>subclass</em></b></font>. It will be kept around to
 * preserve backward compatibility until mid 2003.
 * 
 * <p>
 * <code>Logger</code> is a subclass of Category, i.e. it extends Category. In
 * other words, a logger <em>is</em> a category. Thus, all operations that can
 * be performed on a category can be performed on a logger. Internally, whenever
 * log4j is asked to produce a Category object, it will instead produce a Logger
 * object. Log4j 1.2 will <em>never</em> produce Category objects but only
 * <code>Logger</code> instances. In order to preserve backward compatibility,
 * methods that previously accepted category objects still continue to accept
 * category objects.
 * 
 * <p>
 * For example, the following are all legal and will work as expected.
 * 
 * <pre>
  &nbsp;&nbsp;&nbsp;// Deprecated form:
  &nbsp;&nbsp;&nbsp;Category cat = Category.getInstance("foo.bar")
 
  &nbsp;&nbsp;&nbsp;// Preferred form for retrieving loggers:
  &nbsp;&nbsp;&nbsp;Logger logger = Logger.getLogger("foo.bar")
 * </pre>
 * 
 * <p>
 * The first form is deprecated and should be avoided.
 * 
 * <p>
 * <b>There is absolutely no need for new client code to use or refer to the
 * <code>Category</code> class.</b> Whenever possible, please avoid referring to
 * it or using it.
 * 
 * <p>
 * See the <a href="../../../../manual.html">short manual</a> for an
 * introduction on this class.
 * <p>
 * See the document entitled
 * <a href="http://www.qos.ch/logging/preparingFor13.html">preparing for log4j
 * 1.3</a> for a more detailed discussion.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author Anders Kristensen
 */
public abstract class Category implements AppenderAttachable, PaxLoggingManagerAwareLogger {

    protected static final String LOG4J_FQCN = Logger.class.getName();

    protected static PaxLoggingManager m_paxLogging;

    protected PaxLogger m_delegate;

    /**
     * The hierarchy where categories are attached to by default.
     */
    // static
    // public
    // final Hierarchy defaultHierarchy = new Hierarchy(new
    // RootCategory(Level.DEBUG));

    /**
     * The name of this category.
     */
    protected String name;

    /**
     * The assigned level of this category. The <code>level</code> variable need not
     * be assigned a value in which case it is inherited form the hierarchy.
     */
    volatile protected Level level;

    /**
     * The parent of this category. All categories have at least one ancestor which
     * is the root category.
     */
    volatile protected Category parent;

    /**
     * The fully qualified name of the Category class. See also the getFQCN method.
     */
    private static final String FQCN = Category.class.getName();

    // Categories need to know what Hierarchy they are in
    protected LoggerRepository repository;

    /**
     * Additivity is set to true by default, that is children inherit the appenders
     * of their ancestors by default. If this variable is set to <code>false</code>
     * then the appenders found in the ancestors of this category are not used.
     * However, the children of this category will inherit its appenders, unless the
     * children have their additivity flag set to <code>false</code> too. See the
     * user manual for more details.
     */
    protected boolean additive = true;

    /**
     * This constructor created a new <code>Category</code> instance and sets its
     * name.
     * 
     * <p>
     * It is intended to be used by sub-classes only. You should not create
     * categories directly.
     * 
     * @param name The name of the category.
     */
    protected Category(String name) {
	this.name = name;
    }

    protected Category(String name, PaxLogger delegate) {
        this.name = name;
        this.m_delegate = delegate;
    }

    /**
     * Static method is different than usual, because here logger is also a factory.
     * @param manager
     */
    public static void configurePaxLoggingManager(PaxLoggingManager manager) {
        m_paxLogging = manager;
    }

    @Override
    public void setPaxLoggingManager(PaxLoggingManager loggingManager) {
        if (loggingManager == null) {
            m_delegate = FallbackLogFactory.createFallbackLog(FrameworkUtil.getBundle(Logger.class), name);
        } else {
            m_delegate = loggingManager.getLogger(name, LOG4J_FQCN);
        }
    }

    // public API of original org.apache.log4j.Category follows.
    // no need to call isXXXEnabled, as the delegated logger (PaxLogger) does it anyway
    // non-public API is removed or changed to no-op if that's reasonable in
    // pax-logging case.

    /**
     * Add <code>newAppender</code> to the list of appenders of this Category
     * instance.
     * 
     * <p>
     * If <code>newAppender</code> is already in the list of appenders, then it
     * won't be added again.
     */
    synchronized public void addAppender(Appender newAppender) {
	throw new UnsupportedOperationException("Operation not supported in pax-logging");
    }

    /**
     * If <code>assertion</code> parameter is <code>false</code>, then logs
     * <code>msg</code> as an {@link #error(Object) error} statement.
     * 
     * <p>
     * The <code>assert</code> method has been renamed to <code>assertLog</code>
     * because <code>assert</code> is a language reserved word in JDK 1.4.
     * 
     * @param assertion
     * @param msg       The message to print if <code>assertion</code> is false.
     * 
     * @since 1.2
     */
    public void assertLog(boolean assertion, String msg) {
	if (!assertion)
	    this.error(msg);
    }

    /**
     * Call the appenders in the hierrachy starting at <code>this</code>. If no
     * appenders could be found, emit a warning.
     * 
     * <p>
     * This method calls all the appenders inherited from the hierarchy
     * circumventing any evaluation of whether to log or not to log the particular
     * log request.
     * 
     * @param event the event to log.
     */
    public void callAppenders(LoggingEvent event) {
        // we have to unwrap the event coming from LogMF/LogSF
        Throwable t = event.getThrowableInformation() == null ? null
                : event.getThrowableInformation().getThrowable();
        log(event.getFQNOfLoggerClass(), event.getLevel(), event.getMessage(), t);
    }

    /**
     * Close all attached appenders implementing the AppenderAttachable interface.
     * 
     * @since 1.0
     */
    synchronized void closeNestedAppenders() {
    }

    /**
     * Log a message object with the {@link Level#DEBUG DEBUG} level.
     * 
     * <p>
     * This method first checks if this category is <code>DEBUG</code> enabled by
     * comparing the level of this category with the {@link Level#DEBUG DEBUG}
     * level. If this category is <code>DEBUG</code> enabled, then it converts the
     * message object (passed as parameter) to a string by invoking the appropriate
     * {@link org.apache.log4j.or.ObjectRenderer}. It then proceeds to call all the
     * registered appenders in this category and also higher in the hierarchy
     * depending on the value of the additivity flag.
     * 
     * <p>
     * <b>WARNING</b> Note that passing a {@link Throwable} to this method will
     * print the name of the <code>Throwable</code> but no stack trace. To print a
     * stack trace use the {@link #debug(Object, Throwable)} form instead.
     * 
     * @param message the message object to log.
     */
    public void debug(Object message) {
	m_delegate.debug(message == null ? null : message.toString(), null);
    }

    /**
     * Log a message object with the <code>DEBUG</code> level including the stack
     * trace of the {@link Throwable} <code>t</code> passed as parameter.
     * 
     * <p>
     * See {@link #debug(Object)} form for more detailed information.
     * 
     * @param message the message object to log.
     * @param t       the exception to log, including its stack trace.
     */
    public void debug(Object message, Throwable t) {
	m_delegate.debug(message == null ? null : message.toString(), t);
    }

    /**
     * Log a message with the <code>DEBUG</code> level with message formatting
     * done according to the value of <code>messagePattern</code> and
     * <code>arg</code> parameters.
     * <p>
     * This form avoids superflous parameter construction. Whenever possible,
     * you should use this form instead of constructing the message parameter
     * using string concatenation.
     *
     * @param messagePattern The message pattern which will be parsed and formatted
     * @param arg The argument to replace the formatting element, i,e,
     * the '{}' pair within <code>messagePattern</code>.
     * @since 1.3
     */
    public void debug(Object messagePattern, Object arg) {
        if (m_delegate.isDebugEnabled()) {
            String msgStr = (String) messagePattern;
            msgStr = MessageFormatter.format(msgStr, arg);
            m_delegate.debug(msgStr, null);
        }
    }

    /**
     * Log a message with the <code>DEBUG</code> level with message formatting
     * done according to the messagePattern and the arguments arg1 and arg2.
     * <p>
     * This form avoids superflous parameter construction. Whenever possible,
     * you should use this form instead of constructing the message parameter
     * using string concatenation.
     *
     * @param messagePattern The message pattern which will be parsed and formatted
     * @param arg1 The first argument to replace the first formatting element
     * @param arg2 The second argument to replace the second formatting element
     * @since 1.3
     */
    public void debug(String messagePattern, Object arg1, Object arg2) {
        if (m_delegate.isDebugEnabled()) {
            String msgStr = MessageFormatter.format(messagePattern, arg1, arg2);
            m_delegate.debug(msgStr, null);
        }
    }

    /**
     * Log a message object with the {@link org.apache.log4j.Level#TRACE TRACE} level.
     *
     * @param message the message object to log.
     * @see #debug(Object) for an explanation of the logic applied.
     * @since 1.2.12
     */
    public void trace(Object message) {
        m_delegate.trace(message == null ? null : message.toString(), null);
    }

    /**
     * Log a message object with the <code>TRACE</code> level including the
     * stack trace of the {@link Throwable}<code>t</code> passed as parameter.
     *
     * <p>
     * See {@link #debug(Object)} form for more detailed information.
     * </p>
     *
     * @param message the message object to log.
     * @param t the exception to log, including its stack trace.
     * @since 1.2.12
     */
    public void trace(Object message, Throwable t) {
        m_delegate.trace(message == null ? null : message.toString(), t);
    }

    /**
     * Log a message with the <code>TRACE</code> level with message formatting
     * done according to the value of <code>messagePattern</code> and
     * <code>arg</code> parameters.
     * <p>
     * This form avoids superflous parameter construction. Whenever possible,
     * you should use this form instead of constructing the message parameter
     * using string concatenation.
     *
     * @param messagePattern The message pattern which will be parsed and formatted
     * @param arg The argument to replace the formatting element, i,e,
     * the '{}' pair within <code>messagePattern</code>.
     * @since 1.3
     */
    public void trace(Object messagePattern, Object arg) {
        if (m_delegate.isTraceEnabled()) {
            String msgStr = (String) messagePattern;
            msgStr = MessageFormatter.format(msgStr, arg);
            m_delegate.trace(msgStr, null);
        }
    }

    /**
     * Log a message with the <code>TRACE</code> level with message formatting
     * done according to the messagePattern and the arguments arg1 and arg2.
     * <p>
     * This form avoids superflous parameter construction. Whenever possible,
     * you should use this form instead of constructing the message parameter
     * using string concatenation.
     *
     * @param messagePattern The message pattern which will be parsed and formatted
     * @param arg1 The first argument to replace the first formatting element
     * @param arg2 The second argument to replace the second formatting element
     * @since 1.3
     */
    public void trace(String messagePattern, Object arg1, Object arg2) {
        if (m_delegate.isTraceEnabled()) {
            String msgStr = MessageFormatter.format(messagePattern, arg1, arg2);
            m_delegate.trace(msgStr, null);
        }
    }

    /**
     * Check whether this category is enabled for the ERROR Level. See also
     * {@link #isDebugEnabled()}.
     *
     * @return boolean - <code>true</code> if this category is enabled for level
     *         ERROR, <code>false</code> otherwise.
     */
    public boolean isErrorEnabled() {
        return m_delegate.isErrorEnabled();
    }

    /**
     * Log a message object with the {@link Level#ERROR ERROR} Level.
     * 
     * <p>
     * This method first checks if this category is <code>ERROR</code> enabled by
     * comparing the level of this category with {@link Level#ERROR ERROR} Level. If
     * this category is <code>ERROR</code> enabled, then it converts the message
     * object passed as parameter to a string by invoking the appropriate
     * {@link org.apache.log4j.or.ObjectRenderer}. It proceeds to call all the
     * registered appenders in this category and also higher in the hierarchy
     * depending on the value of the additivity flag.
     * 
     * <p>
     * <b>WARNING</b> Note that passing a {@link Throwable} to this method will
     * print the name of the <code>Throwable</code> but no stack trace. To print a
     * stack trace use the {@link #error(Object, Throwable)} form instead.
     * 
     * @param message the message object to log
     */
    public void error(Object message) {
	m_delegate.error(message == null ? null : message.toString(), null);
    }

    /**
     * Log a message object with the <code>ERROR</code> level including the stack
     * trace of the {@link Throwable} <code>t</code> passed as parameter.
     * 
     * <p>
     * See {@link #error(Object)} form for more detailed information.
     * 
     * @param message the message object to log.
     * @param t       the exception to log, including its stack trace.
     */
    public void error(Object message, Throwable t) {
	m_delegate.error(message == null ? null : message.toString(), t);
    }

    /**
     * Log a message with the <code>ERROR</code> level with message formatting
     * done according to the value of <code>messagePattern</code> and
     * <code>arg</code> parameters.
     * <p>
     * This form avoids superflous parameter construction. Whenever possible,
     * you should use this form instead of constructing the message parameter
     * using string concatenation.
     *
     * @param messagePattern The message pattern which will be parsed and formatted
     * @param arg The argument to replace the formatting element, i,e,
     * the '{}' pair within <code>messagePattern</code>.
     * @since 1.3
     */
    public void error(Object messagePattern, Object arg) {
        if (m_delegate.isErrorEnabled()) {
            String msgStr = (String) messagePattern;
            msgStr = MessageFormatter.format(msgStr, arg);
            m_delegate.error(msgStr, null);
        }
    }

    /**
     * Log a message with the <code>ERROR</code> level with message formatting
     * done according to the messagePattern and the arguments arg1 and arg2.
     * <p>
     * This form avoids superflous parameter construction. Whenever possible,
     * you should use this form instead of constructing the message parameter
     * using string concatenation.
     *
     * @param messagePattern The message pattern which will be parsed and formatted
     * @param arg1 The first argument to replace the first formatting element
     * @param arg2 The second argument to replace the second formatting element
     * @since 1.3
     */
    public void error(String messagePattern, Object arg1, Object arg2) {
        if (m_delegate.isErrorEnabled()) {
            String msgStr = MessageFormatter.format(messagePattern, arg1, arg2);
            m_delegate.error(msgStr, null);
        }
    }

    /**
     * If the named category exists (in the default hierarchy) then it returns a
     * reference to the category, otherwise it returns <code>null</code>.
     * 
     * @deprecated Please use {@link LogManager#exists} instead.
     * 
     * @since 0.8.5
     */
    public static Logger exists(String name) {
	throw new UnsupportedOperationException("Deprecated in log4j since Sep 5, 2001");
    }

    /**
     * Log a message object with the {@link Level#FATAL FATAL} Level.
     * 
     * <p>
     * This method first checks if this category is <code>FATAL</code> enabled by
     * comparing the level of this category with {@link Level#FATAL FATAL} Level. If
     * the category is <code>FATAL</code> enabled, then it converts the message
     * object passed as parameter to a string by invoking the appropriate
     * {@link org.apache.log4j.or.ObjectRenderer}. It proceeds to call all the
     * registered appenders in this category and also higher in the hierarchy
     * depending on the value of the additivity flag.
     * 
     * <p>
     * <b>WARNING</b> Note that passing a {@link Throwable} to this method will
     * print the name of the Throwable but no stack trace. To print a stack trace
     * use the {@link #fatal(Object, Throwable)} form instead.
     * 
     * @param message the message object to log
     */
    public void fatal(Object message) {
	m_delegate.fatal(message == null ? null : message.toString(), null);
    }

    /**
     * Log a message with the <code>FATAL</code> level with message formatting
     * done according to the value of <code>messagePattern</code> and
     * <code>arg</code> parameters.
     * <p>
     * This form avoids superflous parameter construction. Whenever possible,
     * you should use this form instead of constructing the message parameter
     * using string concatenation.
     *
     * @param messagePattern The message pattern which will be parsed and formatted
     * @param arg The argument to replace the formatting element, i,e,
     * the '{}' pair within <code>messagePattern</code>.
     * @since 1.3
     */
    public void fatal(Object messagePattern, Object arg) {
        if (m_delegate.isFatalEnabled()) {
            String msgStr = (String) messagePattern;
            msgStr = MessageFormatter.format(msgStr, arg);
            m_delegate.fatal(msgStr, null);
        }
    }

    /**
     * Log a message with the <code>FATAL</code> level with message formatting
     * done according to the messagePattern and the arguments arg1 and arg2.
     * <p>
     * This form avoids superflous parameter construction. Whenever possible,
     * you should use this form instead of constructing the message parameter
     * using string concatenation.
     *
     * @param messagePattern The message pattern which will be parsed and formatted
     * @param arg1 The first argument to replace the first formatting element
     * @param arg2 The second argument to replace the second formatting element
     * @since 1.3
     */
    public void fatal(String messagePattern, Object arg1, Object arg2) {
        if (m_delegate.isFatalEnabled()) {
            String msgStr = MessageFormatter.format(messagePattern, arg1, arg2);
            m_delegate.fatal(msgStr, null);
        }
    }

    /**
     * Log a message object with the <code>FATAL</code> level including the stack
     * trace of the {@link Throwable} <code>t</code> passed as parameter.
     * 
     * <p>
     * See {@link #fatal(Object)} for more detailed information.
     * 
     * @param message the message object to log.
     * @param t       the exception to log, including its stack trace.
     */
    public void fatal(Object message, Throwable t) {
	m_delegate.fatal(message == null ? null : message.toString(), t);
    }

    /**
     * This method creates a new logging event and logs the event without further
     * checks.
     */
    protected void forcedLog(String fqcn, Priority level, Object message, Throwable t) {
	log(fqcn, level, message, t);
    }

    /**
     * Get the additivity flag for this Category instance.
     */
    public boolean getAdditivity() {
	return additive;
    }

    /**
     * Get the appenders contained in this category as an {@link Enumeration}. If no
     * appenders can be found, then a {@link NullEnumeration} is returned.
     * 
     * @return Enumeration An enumeration of the appenders in this category.
     */
    synchronized public Enumeration getAllAppenders() {
	return NullEnumeration.getInstance();
    }

    /**
     * Look for the appender named as <code>name</code>.
     * 
     * <p>
     * Return the appender with that name if in the list. Return <code>null</code>
     * otherwise.
     */
    synchronized public Appender getAppender(String name) {
	throw new UnsupportedOperationException("Operation not supported in pax-logging");
    }

    /**
     * Starting from this category, search the category hierarchy for a non-null
     * level and return it. Otherwise, return the level of the root category.
     * 
     * <p>
     * The Category class is designed so that this method executes as quickly as
     * possible.
     */
    public Level getEffectiveLevel() {
        int level = m_delegate.getLogLevel();
        if (level == PaxLogger.LEVEL_TRACE) {
            return Level.TRACE;
        }
        if (level == PaxLogger.LEVEL_DEBUG) {
            return Level.DEBUG;
        }
        if (level == PaxLogger.LEVEL_INFO) {
            return Level.INFO;
        }
        if (level == PaxLogger.LEVEL_WARNING) {
            return Level.WARN;
        }
        if (level == PaxLogger.LEVEL_ERROR) {
            return Level.ERROR;
        }
        return null;
    }

    /**
     *
     * @deprecated Please use the the {@link #getEffectiveLevel} method instead.
     */
    public Priority getChainedPriority() {
	throw new UnsupportedOperationException("Deprecated in log4j since Mar 12, 2002");
    }

    /**
     * Returns all the currently defined categories in the default hierarchy as an
     * {@link java.util.Enumeration Enumeration}.
     * 
     * <p>
     * The root category is <em>not</em> included in the returned
     * {@link Enumeration}.
     * 
     * @deprecated Please use {@link LogManager#getCurrentLoggers()} instead.
     */
    public static Enumeration getCurrentCategories() {
	throw new UnsupportedOperationException("Deprecated in log4j since Sep 5, 2001");
    }

    /**
     * Return the default Hierarchy instance.
     * 
     * @deprecated Please use {@link LogManager#getLoggerRepository()} instead.
     * 
     * @since 1.0
     */
    public static LoggerRepository getDefaultHierarchy() {
	throw new UnsupportedOperationException("Deprecated in log4j since Nov 18, 2001");
    }

    /**
     * Return the the {@link Hierarchy} where this <code>Category</code> instance is
     * attached.
     * 
     * @deprecated Please use {@link #getLoggerRepository} instead.
     * 
     * @since 1.1
     */
    public LoggerRepository getHierarchy() {
	throw new UnsupportedOperationException("Deprecated in log4j since Sep 5, 2001");
    }

    /**
     * Return the the {@link LoggerRepository} where this <code>Category</code> is
     * attached.
     * 
     * @since 1.2
     */
    public LoggerRepository getLoggerRepository() {
	return repository;
    }

    /**
     * @deprecated Make sure to use {@link Logger#getLogger(String)} instead.
     */
    public static Category getInstance(String name) {
	return Logger.getLogger(name);
    }

    /**
     * @deprecated Please make sure to use {@link Logger#getLogger(Class)} instead.
     */
    public static Category getInstance(Class clazz) {
	return Logger.getLogger(clazz);
    }

    /**
     * Return the category name.
     */
    public final String getName() {
	return m_delegate.getName();
    }

    /**
     * Returns the parent of this category. Note that the parent of a given category
     * may change during the lifetime of the category.
     * 
     * <p>
     * The root category will return <code>null</code>.
     * 
     * @since 1.2
     */
    final public Category getParent() {
	return null;
    }

    /**
     * Returns the assigned {@link Level}, if any, for this Category.
     * 
     * @return Level - the assigned Level, can be <code>null</code>.
     */
    final public Level getLevel() {
	return getEffectiveLevel();
    }

    /**
     * @deprecated Please use {@link #getLevel} instead.
     */
    final public Level getPriority() {
	return getEffectiveLevel();
    }

    /**
     * @deprecated Please use {@link Logger#getRootLogger()} instead.
     */
    final public static Category getRoot() {
	throw new UnsupportedOperationException("Deprecated in log4j since Sep 5, 2001");
    }

    /**
     * Return the <em>inherited</em> {@link ResourceBundle} for this category.
     * 
     * <p>
     * This method walks the hierarchy to find the appropriate resource bundle. It
     * will return the resource bundle attached to the closest ancestor of this
     * category, much like the way priorities are searched. In case there is no
     * bundle in the hierarchy then <code>null</code> is returned.
     * 
     * @since 0.9.0
     */
    public ResourceBundle getResourceBundle() {
	return null;
    }

    /**
     * Returns the string resource coresponding to <code>key</code> in this
     * category's inherited resource bundle. See also {@link #getResourceBundle}.
     * 
     * <p>
     * If the resource cannot be found, then an {@link #error error} message will be
     * logged complaining about the missing resource.
     */
    protected String getResourceBundleString(String key) {
		return null;
    }

    /**
     * Log a message object with the {@link Level#INFO INFO} Level.
     * 
     * <p>
     * This method first checks if this category is <code>INFO</code> enabled by
     * comparing the level of this category with {@link Level#INFO INFO} Level. If
     * the category is <code>INFO</code> enabled, then it converts the message
     * object passed as parameter to a string by invoking the appropriate
     * {@link org.apache.log4j.or.ObjectRenderer}. It proceeds to call all the
     * registered appenders in this category and also higher in the hierarchy
     * depending on the value of the additivity flag.
     * 
     * <p>
     * <b>WARNING</b> Note that passing a {@link Throwable} to this method will
     * print the name of the Throwable but no stack trace. To print a stack trace
     * use the {@link #info(Object, Throwable)} form instead.
     * 
     * @param message the message object to log
     */
    public void info(Object message) {
	m_delegate.inform(message == null ? null : message.toString(), null);
    }

    /**
     * Log a message with the <code>INFO</code> level with message formatting
     * done according to the value of <code>messagePattern</code> and
     * <code>arg</code> parameters.
     * <p>
     * This form avoids superflous parameter construction. Whenever possible,
     * you should use this form instead of constructing the message parameter
     * using string concatenation.
     *
     * @param messagePattern The message pattern which will be parsed and formatted
     * @param arg The argument to replace the formatting element, i,e,
     * the '{}' pair within <code>messagePattern</code>.
     * @since 1.3
     */
    public void info(Object messagePattern, Object arg) {
        if (m_delegate.isInfoEnabled()) {
            String msgStr = (String) messagePattern;
            msgStr = MessageFormatter.format(msgStr, arg);
            m_delegate.inform(msgStr, null);
        }
    }

    /**
     * Log a message with the <code>INFO</code> level with message formatting
     * done according to the messagePattern and the arguments arg1 and arg2.
     * <p>
     * This form avoids superflous parameter construction. Whenever possible,
     * you should use this form instead of constructing the message parameter
     * using string concatenation.
     *
     * @param messagePattern The message pattern which will be parsed and formatted
     * @param arg1 The first argument to replace the first formatting element
     * @param arg2 The second argument to replace the second formatting element
     * @since 1.3
     */
    public void info(String messagePattern, Object arg1, Object arg2) {
        if (m_delegate.isInfoEnabled()) {
            String msgStr = MessageFormatter.format(messagePattern, arg1, arg2);
            m_delegate.inform(msgStr, null);
        }
    }

    /**
     * Log a message object with the <code>INFO</code> level including the stack
     * trace of the {@link Throwable} <code>t</code> passed as parameter.
     * 
     * <p>
     * See {@link #info(Object)} for more detailed information.
     * 
     * @param message the message object to log.
     * @param t       the exception to log, including its stack trace.
     */
    public void info(Object message, Throwable t) {
	m_delegate.inform(message == null ? null : message.toString(), t);
    }

    /**
     * Is the appender passed as parameter attached to this category?
     */
    public boolean isAttached(Appender appender) {
	return false;
    }

    /**
     * Check whether this category is enabled for the <code>DEBUG</code> Level.
     *
     * <p>
     * This function is intended to lessen the computational cost of disabled log
     * debug statements.
     *
     * <p>
     * For some <code>cat</code> Category object, when you write,
     * 
     * <pre>
     * cat.debug("This is entry number: " + i);
     * </pre>
     *
     * <p>
     * You incur the cost constructing the message, concatenatiion in this case,
     * regardless of whether the message is logged or not.
     *
     * <p>
     * If you are worried about speed, then you should write
     * 
     * <pre>
     * if (cat.isDebugEnabled()) {
     *     cat.debug("This is entry number: " + i);
     * }
     * </pre>
     *
     * <p>
     * This way you will not incur the cost of parameter construction if debugging
     * is disabled for <code>cat</code>. On the other hand, if the <code>cat</code>
     * is debug enabled, you will incur the cost of evaluating whether the category
     * is debug enabled twice. Once in <code>isDebugEnabled</code> and once in the
     * <code>debug</code>. This is an insignificant overhead since evaluating a
     * category takes about 1%% of the time it takes to actually log.
     *
     * @return boolean - <code>true</code> if this category is debug enabled,
     *         <code>false</code> otherwise.
     */
    public boolean isDebugEnabled() {
        return m_delegate.isDebugEnabled();
    }

    /**
     * Check whether this category is enabled for the TRACE  Level. See also
     * {@link #isDebugEnabled()}.
     *
     * @return boolean - <code>true</code> if this category is enabled for level
     *         TRACE, <code>false</code> otherwise.
     */
    public boolean isTraceEnabled() {
        return m_delegate.isTraceEnabled();
    }

    /**
     * Check whether this category is enabled for a given {@link Level} passed as
     * parameter.
     * 
     * See also {@link #isDebugEnabled}.
     * 
     * @return boolean True if this category is enabled for <code>level</code>.
     */
    public boolean isEnabledFor(Priority level) {
	if (level == null)
	    return false;
        switch (level.level) {
            case Level.FATAL_INT:
                return m_delegate.isFatalEnabled();
            case Level.ERROR_INT:
                return m_delegate.isErrorEnabled();
            case Level.WARN_INT:
                return m_delegate.isWarnEnabled();
            case Level.INFO_INT:
                return m_delegate.isInfoEnabled();
            case Level.DEBUG_INT:
                return m_delegate.isDebugEnabled();
            case Level.TRACE_INT:
            case Level.ALL_INT:
                return m_delegate.isTraceEnabled();
            default:
                return false;
        }
    }

    /**
     * Check whether this category is enabled for the info Level. See also
     * {@link #isDebugEnabled}.
     * 
     * @return boolean - <code>true</code> if this category is enabled for level
     *         info, <code>false</code> otherwise.
     */
    public boolean isInfoEnabled() {
	return m_delegate.isInfoEnabled();
    }

    /**
     * Log a localized message. The user supplied parameter <code>key</code> is
     * replaced by its localized version from the resource bundle.
     * 
     * @see #setResourceBundle
     * 
     * @since 0.8.4
     */
    public void l7dlog(Priority priority, String key, Throwable t) {
	log(FQCN, priority, key, t);
    }

    /**
     * Log a localized and parameterized message. First, the user supplied
     * <code>key</code> is searched in the resource bundle. Next, the resulting
     * pattern is formatted using
     * {@link java.text.MessageFormat#format(String,Object[])} method with the user
     * supplied object array <code>params</code>.
     * 
     * @since 0.8.4
     */
    public void l7dlog(Priority priority, String key, Object[] params, Throwable t) {
	log(FQCN, priority, key, t);
    }

    /**
     * This generic form is intended to be used by wrappers.
     */
    public void log(Priority priority, Object message, Throwable t) {
	if (priority == null) {
	    return;
	}
        switch (priority.level) {
            case Level.FATAL_INT:
                fatal(message, t);
                break;
            case Level.ERROR_INT:
                error(message, t);
                break;
            case Level.WARN_INT:
                warn(message, t);
                break;
            case Level.INFO_INT:
                info(message, t);
                break;
            case Level.DEBUG_INT:
                debug(message, t);
                break;
            case Level.TRACE_INT:
            case Level.ALL_INT:
                trace(message, t);
            default:
                break;
        }
    }

    /**
     * This generic form is intended to be used by wrappers.
     */
    public void log(Priority priority, Object message) {
	log(priority, message, null);
    }

    /**
     * 
     * This is the most generic printing method. It is intended to be invoked by
     * <b>wrapper</b> classes.
     * 
     * @param callerFQCN The wrapper class' fully qualified class name.
     * @param level      The level of the logging request.
     * @param message    The message of the logging request.
     * @param t          The throwable of the logging request, may be null.
     */
    public void log(String callerFQCN, Priority level, Object message, Throwable t) {
	log(level, message, t);
    }

    /**
     * Remove all previously added appenders from this Category instance.
     * 
     * <p>
     * This is useful when re-reading configuration information.
     */
    synchronized public void removeAllAppenders() {
	throw new UnsupportedOperationException("Operation not supported in pax-logging");
    }

    /**
     * Remove the appender passed as parameter form the list of appenders.
     * 
     * @since 0.8.2
     */
    synchronized public void removeAppender(Appender appender) {
	throw new UnsupportedOperationException("Operation not supported in pax-logging");
    }

    /**
     * Remove the appender with the name passed as parameter form the list of
     * appenders.
     * 
     * @since 0.8.2
     */
    synchronized public void removeAppender(String name) {
	throw new UnsupportedOperationException("Operation not supported in pax-logging");
    }

    /**
     * Set the additivity flag for this Category instance.
     * 
     * @since 0.8.1
     */
    public void setAdditivity(boolean additive) {
	this.additive = additive;
    }

    /**
     * Only the Hiearchy class can set the hiearchy of a category. Default package
     * access is MANDATORY here.
     */
    final void setHierarchy(LoggerRepository repository) {
	throw new UnsupportedOperationException("Operation not supported in pax-logging");
    }

    /**
     * Set the level of this Category. If you are passing any of
     * <code>Level.DEBUG</code>, <code>Level.INFO</code>, <code>Level.WARN</code>,
     * <code>Level.ERROR</code>, <code>Level.FATAL</code> as a parameter, you need
     * to case them as Level.
     * 
     * <p>
     * As in
     * 
     * <pre>
     * logger.setLevel((Level) Level.DEBUG);
     * </pre>
     * 
     * 
     * <p>
     * Null values are admitted.
     */
    public void setLevel(Level level) {
	this.level = level;
    }

    /**
     * Set the level of this Category.
     * 
     * <p>
     * Null values are admitted.
     * 
     * @deprecated Please use {@link #setLevel} instead.
     */
    public void setPriority(Priority priority) {
	this.level = (Level) priority;
    }

    /**
     * Set the resource bundle to be used with localized logging methods
     * {@link #l7dlog(Priority,String,Throwable)} and
     * {@link #l7dlog(Priority,String,Object[],Throwable)}.
     * 
     * @since 0.8.4
     */
    public void setResourceBundle(ResourceBundle bundle) {
	throw new UnsupportedOperationException("Operation not supported in pax-logging");
    }

    /**
     * Calling this method will <em>safely</em> close and remove all appenders in
     * all the categories including root contained in the default hierachy.
     * 
     * <p>
     * Some appenders such as {@link org.apache.log4j.net.SocketAppender} and
     * {@link AsyncAppender} need to be closed before the application exists.
     * Otherwise, pending logging events might be lost.
     * 
     * <p>
     * The <code>shutdown</code> method is careful to close nested appenders before
     * closing regular appenders. This is allows configurations where a regular
     * appender is attached to a category and again to a nested appender.
     * 
     * @deprecated Please use {@link LogManager#shutdown()} instead.
     * 
     * @since 1.0
     */
    public static void shutdown() {
    }

    /**
     * Check whether this category is enabled for the WARN Level. See also
     * {@link #isDebugEnabled()}.
     *
     * @return boolean - <code>true</code> if this category is enabled for level
     *         WARN, <code>false</code> otherwise.
     */
    public boolean isWarnEnabled() {
        return m_delegate.isWarnEnabled();
    }

    /**
     * Log a message object with the {@link Level#WARN WARN} Level.
     * 
     * <p>
     * This method first checks if this category is <code>WARN</code> enabled by
     * comparing the level of this category with {@link Level#WARN WARN} Level. If
     * the category is <code>WARN</code> enabled, then it converts the message
     * object passed as parameter to a string by invoking the appropriate
     * {@link org.apache.log4j.or.ObjectRenderer}. It proceeds to call all the
     * registered appenders in this category and also higher in the hieararchy
     * depending on the value of the additivity flag.
     * 
     * <p>
     * <b>WARNING</b> Note that passing a {@link Throwable} to this method will
     * print the name of the Throwable but no stack trace. To print a stack trace
     * use the {@link #warn(Object, Throwable)} form instead.
     * <p>
     * 
     * @param message the message object to log.
     */
    public void warn(Object message) {
	m_delegate.warn(message == null ? null : message.toString(), null);
    }

    /**
     * Log a message with the <code>WARN</code> level including the stack trace of
     * the {@link Throwable} <code>t</code> passed as parameter.
     * 
     * <p>
     * See {@link #warn(Object)} for more detailed information.
     * 
     * @param message the message object to log.
     * @param t       the exception to log, including its stack trace.
     */
    public void warn(Object message, Throwable t) {
	m_delegate.warn(message == null ? null : message.toString(), t);
    }

    /**
     * Log a message with the <code>WARN</code> level with message formatting
     * done according to the value of <code>messagePattern</code> and
     * <code>arg</code> parameters.
     * <p>
     * This form avoids superflous parameter construction. Whenever possible,
     * you should use this form instead of constructing the message parameter
     * using string concatenation.
     *
     * @param messagePattern The message pattern which will be parsed and formatted
     * @param arg The argument to replace the formatting element, i,e,
     * the '{}' pair within <code>messagePattern</code>.
     * @since 1.3
     */
    public void warn(Object messagePattern, Object arg) {
        if (m_delegate.isWarnEnabled()) {
            String msgStr = (String) messagePattern;
            msgStr = MessageFormatter.format(msgStr, arg);
            m_delegate.warn(msgStr, null);
        }
    }

    /**
     * Log a message with the <code>WARN</code> level with message formatting
     * done according to the messagePattern and the arguments arg1 and arg2.
     * <p>
     * This form avoids superflous parameter construction. Whenever possible,
     * you should use this form instead of constructing the message parameter
     * using string concatenation.
     *
     * @param messagePattern The message pattern which will be parsed and formatted
     * @param arg1 The first argument to replace the first formatting element
     * @param arg2 The second argument to replace the second formatting element
     * @since 1.3
     */
    public void warn(String messagePattern, Object arg1, Object arg2) {
        if (m_delegate.isWarnEnabled()) {
            String msgStr = MessageFormatter.format(messagePattern, arg1, arg2);
            m_delegate.warn(msgStr, null);
        }
    }
}
