/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
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
// Modified to fit into Pax Logging. Not same as original class.

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

import org.ops4j.pax.logging.PaxLogger;

/**
 * <font color="#AA2222"><b>This class has been deprecated and
 * replaced by the {@link Logger} <em>subclass</em></b></font>. It
 * will be kept around to preserve backward compatibility until mid
 * 2003.
 *
 * <p><code>Logger</code> is a subclass of Category, i.e. it extends
 * Category. In other words, a logger <em>is</em> a category. Thus,
 * all operations that can be performed on a category can be
 * performed on a logger. Internally, whenever log4j is asked to
 * produce a Category object, it will instead produce a Logger
 * object. Log4j 1.2 will <em>never</em> produce Category objects but
 * only <code>Logger</code> instances. In order to preserve backward
 * compatibility, methods that previously accepted category objects
 * still continue to accept category objects.
 *
 * <p>For example, the following are all legal and will work as
 * expected.
 *
 * <pre>
 * &nbsp;&nbsp;&nbsp;// Deprecated form:
 * &nbsp;&nbsp;&nbsp;Category cat = Category.getInstance("foo.bar")
 *
 * &nbsp;&nbsp;&nbsp;// Preferred form for retrieving loggers:
 * &nbsp;&nbsp;&nbsp;Logger logger = Logger.getLogger("foo.bar")
 * </pre>
 *
 * <p>The first form is deprecated and should be avoided.
 *
 * <p><b>There is absolutely no need for new client code to use or
 * refer to the <code>Category</code> class.</b> Whenever possible,
 * please avoid referring to it or using it.
 *
 * <p>See the <a href="../../../../manual.html">short manual</a> for an
 * introduction on this class.
 * <p>
 * See the document entitled <a href="http://www.qos.ch/logging/preparingFor13.html">preparing
 * for log4j 1.3</a> for a more detailed discussion.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author Anders Kristensen
 */
public abstract class Category
{

    protected PaxLogger m_delegate;

    public Category( PaxLogger delegate )
    {
        m_delegate = delegate;
    }

    /**
     * If <code>assertion</code> parameter is <code>false</code>, then
     * logs <code>msg</code> as an {@link #error(Object) error} statement.
     *
     * <p>The <code>assert</code> method has been renamed to
     * <code>assertLog</code> because <code>assert</code> is a language
     * reserved word in JDK 1.4.
     *
     * @param assertion if false, log the message.
     * @param msg       The message to print if <code>assertion</code> is
     *                  false.
     *
     * @since 1.2
     */
    public void assertLog( boolean assertion, String msg )
    {
        if( !assertion )
        {
            this.error( msg );
        }
    }

//    /**
//     * Close all attached appenders implementing the AppenderAttachable
//     * interface.
//     *
//     * @since 1.0
//     */
//    synchronized void closeNestedAppenders()
//    {
//    }

    /**
     * Log a message object with the DEBUG level.
     *
     * <p>
     * This method first checks if this category is <code>DEBUG</code> enabled
     * by comparing the level of this category with the
     * DEBUG level. If this category is <code>DEBUG</code> enabled, then it
     * converts the message object (passed as parameter) to a string by
     * invoking the appropriate org.apache.log4j.or.ObjectRenderer. It
     * then proceeds to call all the registered appenders in this category and
     * also higher in the hierarchy depending on the value of the additivity
     * flag.
     * </p>
     *
     * <p>
     * <b>WARNING</b> Note that passing a {@link Throwable} to this method will
     * print the name of the <code>Throwable</code> but no stack trace. To
     * print a stack trace use the {@link #debug(Object,Throwable)} form
     * instead.
     * </p>
     *
     * @param message the message object to log.
     */
    public void debug( Object message )
    {
        if( m_delegate.isDebugEnabled() && message != null )
        {
            m_delegate.debug( message.toString(), null );
        }
    }

    /**
     * Log a message object with the <code>DEBUG</code> level including the
     * stack trace of the {@link Throwable}<code>t</code> passed as parameter.
     *
     * <p>
     * See {@link #debug(Object)} form for more detailed information.
     * </p>
     *
     * @param message the message object to log.
     * @param t       the exception to log, including its stack trace.
     */
    public void debug( Object message, Throwable t )
    {
        if( m_delegate.isDebugEnabled() )
        {
            if( message != null )
            {
                m_delegate.debug( message.toString(), t );
            }
            else
            {
                m_delegate.debug( null, t );
            }
        }
    }

    /**
     * Log a message object with the ERROR Level.
     *
     * <p>
     * This method first checks if this category is <code>ERROR</code> enabled
     * by comparing the level of this category with ERROR
     * Level. If this category is <code>ERROR</code> enabled, then it converts
     * the message object passed as parameter to a string by invoking the
     * appropriate org.apache.log4j.or.ObjectRenderer. It proceeds to
     * call all the registered appenders in this category and also higher in
     * the hierarchy depending on the value of the additivity flag.
     * </p>
     *
     * <p>
     * <b>WARNING</b> Note that passing a {@link Throwable} to this method will
     * print the name of the <code>Throwable</code> but no stack trace. To
     * print a stack trace use the {@link #error(Object,Throwable)} form
     * instead.
     * </p>
     *
     * @param message the message object to log
     */
    public void error( Object message )
    {
        if( m_delegate.isErrorEnabled() && message != null )
        {
            m_delegate.error( message.toString(), null );
        }
    }

    /**
     * Log a message object with the <code>ERROR</code> level including the
     * stack trace of the {@link Throwable}<code>t</code> passed as parameter.
     *
     * <p>
     * See {@link #error(Object)} form for more detailed information.
     * </p>
     *
     * @param message the message object to log.
     * @param t       the exception to log, including its stack trace.
     */
    public void error( Object message, Throwable t )
    {
        if( m_delegate.isErrorEnabled() )
        {
            if( message != null )
            {
                m_delegate.error( message.toString(), t );
            }
            else
            {
                m_delegate.error( null, t );
            }
        }
    }

//    /**
//     * If the named category exists (in the default hierarchy) then it
//     * returns a reference to the category, otherwise it returns
//     * <code>null</code>.
//     *
//     * @since 0.8.5
//     * @deprecated
//     */
//    public static Logger exists( String name )
//    {
//    }

    //
/**
 * Log a message object with the FATAL Level.
 *
 * <p>
 * This method first checks if this category is <code>FATAL</code> enabled
 * by comparing the level of this category with FATAL
 * Level. If the category is <code>FATAL</code> enabled, then it converts
 * the message object passed as parameter to a string by invoking the
 * appropriate org.apache.log4j.or.ObjectRenderer. It proceeds to
 * call all the registered appenders in this category and also higher in
 * the hierarchy depending on the value of the additivity flag.
 * </p>
 *
 * <p>
 * <b>WARNING</b> Note that passing a {@link Throwable} to this method will
 * print the name of the Throwable but no stack trace. To print a stack
 * trace use the {@link #fatal(Object,Throwable)} form instead.
 * </p>
 *
 * @param message the message object to log
 */
    public void fatal( Object message )
    {
        if( m_delegate.isFatalEnabled() && message != null )
        {
            m_delegate.fatal( message.toString(), null );
        }
    }

    /**
     * Log a message object with the <code>FATAL</code> level including the
     * stack trace of the {@link Throwable}<code>t</code> passed as parameter.
     *
     * <p>
     * See {@link #fatal(Object)} for more detailed information.
     * </p>
     *
     * @param message the message object to log.
     * @param t       the exception to log, including its stack trace.
     */
    public void fatal( Object message, Throwable t )
    {
        if( m_delegate.isFatalEnabled() )
        {
            if( message != null )
            {
                m_delegate.fatal( message.toString(), t );
            }
            else
            {
                m_delegate.fatal( null, t );
            }
        }
    }

//    /**
//     * Get the additivity flag for this Category instance.
//     *
//     * @return always return false.
//     */
//    public boolean getAdditivity()
//    {
//        return false;
//    }

//    /**
//     * Get the appenders contained in this category as an {@link
//     * Enumeration}. If no appenders can be found, then a {@link NullEnumeration}
//     * is returned.
//     *
//     * @return Enumeration An enumeration of the appenders in this category.
//     */
//    synchronized
//    public Enumeration getAllAppenders()
//    {
//        if( aai == null )
//        {
//            return NullEnumeration.getInstance();
//        }
//        else
//        {
//            return aai.getAllAppenders();
//        }
//    }
//
//    /**
//     * Look for the appender named as <code>name</code>.
//     *
//     * <p>Return the appender with that name if in the list. Return
//     * <code>null</code> otherwise.
//     */
//    synchronized
//    public Appender getAppender( String name )
//    {
//        if( aai == null || name == null )
//        {
//            return null;
//        }
//
//        return aai.getAppender( name );
//    }

//    /**
//     * Starting from this category, search the category hierarchy for a
//     * non-null level and return it. Otherwise, return the level of the
//     * root category.
//     *
//     * <p>The Category class is designed so that this method executes as
//     * quickly as possible.
//     */
//    public Level getEffectiveLevel()
//    {
//        for( Category c = this; c != null; c = c.parent )
//        {
//            if( c.level != null )
//            {
//                return c.level;
//            }
//        }
//        return null; // If reached will cause an NullPointerException.
//    }

//    /**
//     * @deprecated Please use the the {@link #getEffectiveLevel} method
//     *             instead.
//     */
//    public Priority getChainedPriority()
//    {
//        for( Category c = this; c != null; c = c.parent )
//        {
//            if( c.level != null )
//            {
//                return c.level;
//            }
//        }
//        return null; // If reached will cause an NullPointerException.
//    }

//    /**
//     * Returns all the currently defined categories in the default
//     * hierarchy as an {@link java.util.Enumeration Enumeration}.
//     *
//     * <p>The root category is <em>not</em> included in the returned
//     * {@link Enumeration}.
//     *
//     * @deprecated Please use {@link LogManager#getCurrentLoggers()} instead.
//     */
//    public
//    static Enumeration getCurrentCategories()
//    {
//        return LogManager.getCurrentLoggers();
//    }

//    /**
//     * Return the default Hierarchy instance.
//     *
//     * @since 1.0
//     * @deprecated Please use {@link LogManager#getLoggerRepository()} instead.
//     */
//    public
//    static LoggerRepository getDefaultHierarchy()
//    {
//        return LogManager.getLoggerRepository();
//    }
//
//    /**
//     * Return the the {@link Hierarchy} where this <code>Category</code>
//     * instance is attached.
//     *
//     * @since 1.1
//     * @deprecated Please use {@link #getLoggerRepository} instead.
//     */
//    public LoggerRepository getHierarchy()
//    {
//        return repository;
//    }
//
//    /**
//     * Return the the {@link LoggerRepository} where this
//     * <code>Category</code> is attached.
//     *
//     * @since 1.2
//     */
//    public LoggerRepository getLoggerRepository()
//    {
//        return repository;
//    }

    /**
     * @param name of the Logger/Category to return.
     *
     * @return a Logger of the requested name.
     *
     * @deprecated Make sure to use {@link Logger#getLogger(String)} instead.
     */
    public static Category getInstance( String name )
    {
        return Logger.getLogger( name );
    }

    /**
     * @param clazz name of the Logger/Category to return.
     *
     * @return a Logger of the requested name.
     *
     * @deprecated Please make sure to use {@link Logger#getLogger(Class)} instead.
     */
    public static Category getInstance( Class clazz )
    {
        return Logger.getLogger( clazz );
    }

    /**
     * Return the category name.
     *
     * @return the name of this logger/category.
     */
    public final String getName()
    {
        return m_delegate.getName();
    }

//    /**
//     * Returns the parent of this category. Note that the parent of a
//     * given category may change during the lifetime of the category.
//     *
//     * <p>The root category will return <code>null</code>.
//     *
//     * @since 1.2
//     */
//    final public Category getParent()
//    {
//        return this.parent;
//    }
//
//    /**
//     * Returns the assigned {@link Level}, if any, for this Category.
//     *
//     * @return Level - the assigned Level, can be <code>null</code>.
//     */
//    final
//    public Level getLevel()
//    {
//        return this.level;
//    }
//
//    /**
//     * @deprecated Please use {@link #getLevel} instead.
//     */
//    final
//    public Level getPriority()
//    {
//        return this.level;
//    }
//
//    /**
//     * @deprecated Please use {@link Logger#getRootLogger()} instead.
//     */
//    public static Category getRoot()
//    {
//        return LogManager.getRootLogger();
//    }

//    /**
//     * Return the <em>inherited</em> {@link ResourceBundle} for this
//     * category.
//     *
//     * <p>This method walks the hierarchy to find the appropriate
//     * resource bundle. It will return the resource bundle attached to
//     * the closest ancestor of this category, much like the way
//     * priorities are searched. In case there is no bundle in the
//     * hierarchy then <code>null</code> is returned.
//     *
//     * @since 0.9.0
//     */
//    public ResourceBundle getResourceBundle()
//    {
//        for( Category c = this; c != null; c = c.parent )
//        {
//            if( c.resourceBundle != null )
//            {
//                return c.resourceBundle;
//            }
//        }
//        // It might be the case that there is no resource bundle
//        return null;
//    }

//    /**
//     * Returns the string resource coresponding to <code>key</code> in
//     * this category's inherited resource bundle. See also {@link
//     * #getResourceBundle}.
//     *
//     * <p>If the resource cannot be found, then an {@link #error error}
//     * message will be logged complaining about the missing resource.
//     */
//    protected String getResourceBundleString( String key )
//    {
//        ResourceBundle rb = getResourceBundle();
//        // This is one of the rare cases where we can use logging in order
//        // to report errors from within log4j.
//        if( rb == null )
//        {
//            //if(!hierarchy.emittedNoResourceBundleWarning) {
//            //error("No resource bundle has been set for category "+name);
//            //hierarchy.emittedNoResourceBundleWarning = true;
//            //}
//            return null;
//        }
//        else
//        {
//            try
//            {
//                return rb.getString( key );
//            }
//            catch( MissingResourceException mre )
//            {
//                error( "No resource is associated with key \"" + key + "\"." );
//                return null;
//            }
//        }
//    }

    /**
     * Log a message object with the INFO Level.
     *
     * <p>
     * This method first checks if this category is <code>INFO</code> enabled by
     * comparing the level of this category with INFO Level.
     * If the category is <code>INFO</code> enabled, then it converts the
     * message object passed as parameter to a string by invoking the
     * appropriate org.apache.log4j.or.ObjectRenderer. It proceeds to
     * call all the registered appenders in this category and also higher in
     * the hierarchy depending on the value of the additivity flag.
     * </p>
     *
     * <p>
     * <b>WARNING</b> Note that passing a {@link Throwable} to this method will
     * print the name of the Throwable but no stack trace. To print a stack
     * trace use the {@link #info(Object,Throwable)} form instead.
     * </p>
     *
     * @param message the message object to log
     */
    public void info( Object message )
    {
        if( m_delegate.isInfoEnabled() && message != null )
        {
            m_delegate.inform( message.toString(), null );
        }
    }

    /**
     * Log a message object with the <code>INFO</code> level including the stack
     * trace of the {@link Throwable}<code>t</code> passed as parameter.
     *
     * <p>
     * See {@link #info(Object)} for more detailed information.
     * </p>
     *
     * @param message the message object to log.
     * @param t       the exception to log, including its stack trace.
     */
    public void info( Object message, Throwable t )
    {
        if( m_delegate.isInfoEnabled() )
        {
            if( message != null )
            {
                m_delegate.inform( message.toString(), t );
            }
            else
            {
                m_delegate.inform( null, t );
            }
        }
    }

//    /**
//     * Is the appender passed as parameter attached to this category?
//     */
//    public boolean isAttached( Appender appender )
//    {
//        if( appender == null || aai == null )
//        {
//            return false;
//        }
//        else
//        {
//            return aai.isAttached( appender );
//        }
//    }

    /**
     * Check whether this category is enabled for the <code>DEBUG</code> Level.
     *
     * <p>
     * This function is intended to lessen the computational cost of disabled
     * log debug statements.
     * </p>
     *
     * <p>
     * For some <code>cat</code> Category object, when you write,
     * <pre>
     *      cat.debug("This is entry number: " + i );
     *  </pre>
     * </p>
     *
     * <p>
     * You incur the cost constructing the message, concatenatiion in this case,
     * regardless of whether the message is logged or not.
     * </p>
     *
     * <p>
     * If you are worried about speed, then you should write
     * <pre>
     *          if(cat.isDebugEnabled()) {
     *            cat.debug("This is entry number: " + i );
     *          }
     *  </pre>
     * </p>
     *
     * <p>
     * This way you will not incur the cost of parameter construction if
     * debugging is disabled for <code>cat</code>. On the other hand, if the
     * <code>cat</code> is debug enabled, you will incur the cost of evaluating
     * whether the category is debug enabled twice. Once in
     * <code>isDebugEnabled</code> and once in the <code>debug</code>.  This is
     * an insignificant overhead since evaluating a category takes about 1%% of
     * the time it takes to actually log.
     * </p>
     *
     * @return boolean - <code>true</code> if this category is debug enabled,
     *         <code>false</code> otherwise.
     */
    public boolean isDebugEnabled()
    {
        return m_delegate.isDebugEnabled();
    }

//    /**
//     * Check whether this category is enabled for a given {@link
//     * Level} passed as parameter.
//     *
//     * See also {@link #isDebugEnabled}.
//     *
//     * @return boolean True if this category is enabled for <code>level</code>.
//     */
//    public boolean isEnabledFor( Priority level )
//    {
//        if( repository.isDisabled( level.level ) )
//        {
//            return false;
//        }
//        return level.isGreaterOrEqual( this.getEffectiveLevel() );
//    }

    /**
     * Check whether this category is enabled for the info Level. See also
     * {@link #isDebugEnabled()}.
     *
     * @return boolean - <code>true</code> if this category is enabled for level
     *         info, <code>false</code> otherwise.
     */
    public boolean isInfoEnabled()
    {
        return m_delegate.isInfoEnabled();
    }

//    /**
//     * Log a localized message. The user supplied parameter
//     * <code>key</code> is replaced by its localized version from the
//     * resource bundle.
//     *
//     * @see #setResourceBundle
//     * @since 0.8.4
//     */
//    public void l7dlog( Priority priority, String key, Throwable t )
//    {
//        if( repository.isDisabled( priority.level ) )
//        {
//            return;
//        }
//        if( priority.isGreaterOrEqual( this.getEffectiveLevel() ) )
//        {
//            String msg = getResourceBundleString( key );
//            // if message corresponding to 'key' could not be found in the
//            // resource bundle, then default to 'key'.
//            if( msg == null )
//            {
//                msg = key;
//            }
//            forcedLog( FQCN, priority, msg, t );
//        }
//    }

//    /**
//     * Log a localized and parameterized message. First, the user
//     * supplied <code>key</code> is searched in the resource
//     * bundle. Next, the resulting pattern is formatted using
//     * {@link java.text.MessageFormat#format(String,Object[])} method with the
//     * user supplied object array <code>params</code>.
//     *
//     * @since 0.8.4
//     */
//    public void l7dlog( Priority priority, String key, Object[] params, Throwable t )
//    {
//        if( repository.isDisabled( priority.level ) )
//        {
//            return;
//        }
//        if( priority.isGreaterOrEqual( this.getEffectiveLevel() ) )
//        {
//            String pattern = getResourceBundleString( key );
//            String msg;
//            if( pattern == null )
//            {
//                msg = key;
//            }
//            else
//            {
//                msg = java.text.MessageFormat.format( pattern, params );
//            }
//            forcedLog( FQCN, priority, msg, t );
//        }
//    }

    /**
     * Log a message with the code level priority.
     *
     * @param priority, the code level of the message
     * @param msg       the message object to log.
     */

    public void log( Priority priority, Object msg )
    {
        log( priority, msg, null );
    }

    /**
     * This generic form is intended to be used by wrappers.
     *
     * @param priority priority of log to be generated.
     * @param message  the message to log
     * @param t        an Exception to be logged, if any.
     */
    public void log( Priority priority, Object message, Throwable t )
    {
        if( priority == Level.FATAL )
        {
            this.fatal( message, t );
        }
        if( priority == Level.ERROR )
        {
            this.error( message, t );
        }
        if( priority == Level.WARN )
        {
            this.warn( message, t );
        }
        if( priority == Level.INFO )
        {
            this.info( message, t );
        }
        if( priority == Level.DEBUG )
        {
            this.debug( message, t );
        }
        if( priority == Level.ALL )
        {
            this.trace( message, t );
        }
    }

    protected abstract void trace( Object message, Throwable t );

    /**
     * This is the most generic printing method. It is intended to be
     * invoked by <b>wrapper</b> classes.
     *
     * <b>Note:</b>In Pax Logging the callerFQCN is ignored.
     *
     * @param callerFQCN The wrapper class' fully qualified class name.
     * @param level      The level of the logging request.
     * @param message    The message of the logging request.
     * @param t          The throwable of the logging request, may be null.
     */
    public void log( String callerFQCN, Priority level, Object message, Throwable t )
    {
        log( level, message, t );
    }

//    /**
//     * LoggerRepository forgot the fireRemoveAppenderEvent method,
//     * if using the stock Hierarchy implementation, then call its fireRemove.
//     * Custom repositories can implement HierarchyEventListener if they
//     * want remove notifications.
//     *
//     * @param appender appender, may be null.
//     */
//    private void fireRemoveAppenderEvent( final Appender appender )
//    {
//        if( appender != null )
//        {
//            if( repository instanceof Hierarchy )
//            {
//                ( (Hierarchy) repository ).fireRemoveAppenderEvent( this, appender );
//            }
//            else if( repository instanceof HierarchyEventListener )
//            {
//                ( (HierarchyEventListener) repository ).removeAppenderEvent( this, appender );
//            }
//        }
//    }
//
//    /**
//     * Remove all previously added appenders from this Category
//     * instance.
//     *
//     * <p>This is useful when re-reading configuration information.
//     */
//    synchronized
//    public void removeAllAppenders()
//    {
//        if( aai != null )
//        {
//            Vector appenders = new Vector();
//            for( Enumeration iter = aai.getAllAppenders(); iter.hasMoreElements(); )
//            {
//                appenders.add( iter.nextElement() );
//            }
//            aai.removeAllAppenders();
//            for( Enumeration iter = appenders.elements(); iter.hasMoreElements(); )
//            {
//                fireRemoveAppenderEvent( (Appender) iter.nextElement() );
//            }
//            aai = null;
//        }
//    }
//
//    /**
//     * Remove the appender passed as parameter form the list of appenders.
//     *
//     * @since 0.8.2
//     */
//    synchronized
//    public void removeAppender( Appender appender )
//    {
//        if( appender == null || aai == null )
//        {
//            return;
//        }
//        boolean wasAttached = aai.isAttached( appender );
//        aai.removeAppender( appender );
//        if( wasAttached )
//        {
//            fireRemoveAppenderEvent( appender );
//        }
//    }
//
//    /**
//     * Remove the appender with the name passed as parameter form the
//     * list of appenders.
//     *
//     * @since 0.8.2
//     */
//    synchronized
//    public void removeAppender( String name )
//    {
//        if( name == null || aai == null )
//        {
//            return;
//        }
//        Appender appender = aai.getAppender( name );
//        aai.removeAppender( name );
//        if( appender != null )
//        {
//            fireRemoveAppenderEvent( appender );
//        }
//    }
//
//    /**
//     * Set the additivity flag for this Category instance.
//     *
//     * @since 0.8.1
//     */
//    public void setAdditivity( boolean additive )
//    {
//        this.additive = additive;
//    }
//
//    /**
//     * Only the Hiearchy class can set the hiearchy of a
//     * category. Default package access is MANDATORY here.
//     */
//    final void setHierarchy( LoggerRepository repository )
//    {
//        this.repository = repository;
//    }

//    /**
//     * Set the level of this Category. If you are passing any of
//     * <code>Level.DEBUG</code>, <code>Level.INFO</code>,
//     * <code>Level.WARN</code>, <code>Level.ERROR</code>,
//     * <code>Level.FATAL</code> as a parameter, you need to case them as
//     * Level.
//     *
//     * <p>As in <pre> &nbsp;&nbsp;&nbsp;logger.setLevel((Level) Level.DEBUG); </pre>
//     *
//     *
//     * <p>Null values are admitted.
//     */
//    public void setLevel( Level level )
//    {
//        this.level = level;
//    }
//
//    /**
//     * Set the level of this Category.
//     *
//     * <p>Null values are admitted.
//     *
//     * @deprecated Please use {@link #setLevel} instead.
//     */
//    public void setPriority( Priority priority )
//    {
//        this.level = (Level) priority;
//    }
//
//    /**
//     * Set the resource bundle to be used with localized logging
//     * methods {@link #l7dlog(Priority,String,Throwable)} and {@link
//     * #l7dlog(Priority,String,Object[],Throwable)}.
//     *
//     * @since 0.8.4
//     */
//    public void setResourceBundle( ResourceBundle bundle )
//    {
//        resourceBundle = bundle;
//    }

    /**
     * Does nothing.
     *
     * @since 1.0
     * @deprecated
     */
    public static void shutdown()
    {
    }

    /**
     * Log a message object with the WARN Level.
     *
     * <p>
     * This method first checks if this category is <code>WARN</code> enabled by
     * comparing the level of this category with WARN Level.
     * If the category is <code>WARN</code> enabled, then it converts the
     * message object passed as parameter to a string by invoking the
     * appropriate org.apache.log4j.or.ObjectRenderer. It proceeds to
     * call all the registered appenders in this category and also higher in
     * the hieararchy depending on the value of the additivity flag.
     * </p>
     *
     * <p>
     * <b>WARNING</b> Note that passing a {@link Throwable} to this method will
     * print the name of the Throwable but no stack trace. To print a stack
     * trace use the {@link #warn(Object,Throwable)} form instead.
     * </p>
     *
     * <p></p>
     *
     * @param message the message object to log.
     */
    public void warn( Object message )
    {
        if( m_delegate.isWarnEnabled() && message != null )
        {
            m_delegate.warn( message.toString(), null );
        }
    }

    /**
     * Log a message with the <code>WARN</code> level including the stack trace
     * of the {@link Throwable}<code>t</code> passed as parameter.
     *
     * <p>
     * See {@link #warn(Object)} for more detailed information.
     * </p>
     *
     * @param message the message object to log.
     * @param t       the exception to log, including its stack trace.
     */
    public void warn( Object message, Throwable t )
    {
        if( m_delegate.isWarnEnabled() )
        {
            if( message != null )
            {
                m_delegate.warn( message.toString(), t );
            }
            else
            {
                m_delegate.warn( null, t );
            }
        }
    }
}
