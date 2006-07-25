package org.knopflerfish.service.log;

import java.io.File;
import java.util.HashMap;

public interface LogConfig {

    public void commit();

    public boolean isDefaultConfig();

    /**
     * Set number of log entries that are kept in memory.
     * 
     * @param size
     *            the new maximum number of log entries in memory.
     */
    public void setMemorySize(int size);

    public int getMemorySize();

    /**
     * Set the default filter level.
     * 
     * @param filter
     *            the new default filter level.
     */
    public void setFilter(int filter);

    public int getFilter();

    /**
     * Set the default filter level.
     * 
     * @param filter
     *            the new default filter level.
     */
    public void setFilter(String bundleLocation, int filter);

    public HashMap getFilters();

    /**
     * Property controling if log entries are written to <code>System.out</code>
     * or not.
     * 
     * @param b
     *            if <code>true</code> log entries will be written to
     *            <code>System.out</code>.
     */
    public void setOut(boolean b);

    public boolean getOut();

    public void setFile(boolean f);

    public boolean getFile();

    public File getDir();

    public void setFileSize(int fS);

    public int getFileSize();

    public void setMaxGen(int maxGen);

    public int getMaxGen();

    public void setFlush(boolean f);

    public boolean getFlush();

}
