package org.apache.commons.logging.impl;

import org.apache.commons.logging.Log;

public class NoOpLog implements Log
{

    public void debug(Object message)
    {
        // do nothing

    }

    public void debug(Object message, Throwable t)
    {
        // do nothing

    }

    public void error(Object message)
    {
        // do nothing

    }

    public void error(Object message, Throwable t)
    {
        // do nothing

    }

    public void fatal(Object message)
    {
        // do nothing

    }

    public void fatal(Object message, Throwable t)
    {
        // do nothing

    }

    public int getLogLevel()
    {
        // do nothing
        return 0;
    }

    public void info(Object message)
    {
        // do nothing

    }

    public void info(Object message, Throwable t)
    {
        // do nothing

    }

    public boolean isDebugEnabled()
    {
        // do nothing
        return false;
    }

    public boolean isErrorEnabled()
    {
        // do nothing
        return false;
    }

    public boolean isFatalEnabled()
    {
        // do nothing
        return false;
    }

    public boolean isInfoEnabled()
    {
        // do nothing
        return false;
    }

    public boolean isTraceEnabled()
    {
        // do nothing
        return false;
    }

    public boolean isWarnEnabled()
    {
        // do nothing
        return false;
    }

    public void trace(Object message)
    {
        // do nothing

    }

    public void trace(Object message, Throwable t)
    {
        // do nothing

    }

    public void warn(Object message)
    {
        // do nothing

    }

    public void warn(Object message, Throwable t)
    {
        // do nothing

    }

}
