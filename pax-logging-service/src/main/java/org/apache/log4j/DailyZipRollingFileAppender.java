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

package org.apache.log4j;

import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.Serializable;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * DailyZipRollingFileAppender extends {@link FileAppender} so that the
 * underlying file is rolled over at a user chosen frequency.
 * 
 * 
 * <p>
 * The rolling schedule is specified by the <b>DatePattern</b> option. This
 * pattern should follow the {@link SimpleDateFormat} conventions. In
 * particular, you <em>must</em> escape literal text within a pair of single
 * quotes. A formatted version of the date pattern is used as the suffix for the
 * rolled file name.
 * 
 * <p>
 * For example, if the <b>File</b> option is set to <code>/foo/bar.log</code>
 * and the <b>DatePattern</b> set to <code>'.'yyyy-MM-dd</code>, on 2001-02-16
 * at midnight, the logging file <code>/foo/bar.log</code> will be copied to
 * <code>/foo/bar.log.2001-02-16</code> and logging for 2001-02-17 will continue
 * in <code>/foo/bar.log</code> until it rolls over the next day.
 * 
 * <p>
 * Is is possible to specify monthly, weekly, half-daily, daily, hourly, or
 * minutely rollover schedules.
 * 
 * <p>
 * <table border="1" cellpadding="2">
 * <tr>
 * <th>DatePattern</th>
 * <th>Rollover schedule</th>
 * <th>Example</th>
 * 
 * <tr>
 * <td><code>'.'yyyy-MM</code>
 * <td>Rollover at the beginning of each month</td>
 * 
 * <td>At midnight of May 31st, 2002 <code>/foo/bar.log</code> will be copied to
 * <code>/foo/bar.log.2002-05</code>. Logging for the month of June will be
 * output to <code>/foo/bar.log</code> until it is also rolled over the next
 * month.
 * 
 * <tr>
 * <td><code>'.'yyyy-ww</code>
 * 
 * <td>Rollover at the first day of each week. The first day of the week depends
 * on the locale.</td>
 * 
 * <td>Assuming the first day of the week is Sunday, on Saturday midnight, June
 * 9th 2002, the file <i>/foo/bar.log</i> will be copied to
 * <i>/foo/bar.log.2002-23</i>. Logging for the 24th week of 2002 will be output
 * to <code>/foo/bar.log</code> until it is rolled over the next week.
 * 
 * <tr>
 * <td><code>'.'yyyy-MM-dd</code>
 * 
 * <td>Rollover at midnight each day.</td>
 * 
 * <td>At midnight, on March 8th, 2002, <code>/foo/bar.log</code> will be copied
 * to <code>/foo/bar.log.2002-03-08</code>. Logging for the 9th day of March
 * will be output to <code>/foo/bar.log</code> until it is rolled over the next
 * day.
 * 
 * <tr>
 * <td><code>'.'yyyy-MM-dd-a</code>
 * 
 * <td>Rollover at midnight and midday of each day.</td>
 * 
 * <td>At noon, on March 9th, 2002, <code>/foo/bar.log</code> will be copied to
 * <code>/foo/bar.log.2002-03-09-AM</code>. Logging for the afternoon of the 9th
 * will be output to <code>/foo/bar.log</code> until it is rolled over at
 * midnight.
 * 
 * <tr>
 * <td><code>'.'yyyy-MM-dd-HH</code>
 * 
 * <td>Rollover at the top of every hour.</td>
 * 
 * <td>At approximately 11:00.000 o'clock on March 9th, 2002,
 * <code>/foo/bar.log</code> will be copied to
 * <code>/foo/bar.log.2002-03-09-10</code>. Logging for the 11th hour of the 9th
 * of March will be output to <code>/foo/bar.log</code> until it is rolled over
 * at the beginning of the next hour.
 * 
 * 
 * <tr>
 * <td><code>'.'yyyy-MM-dd-HH-mm</code>
 * 
 * <td>Rollover at the beginning of every minute.</td>
 * 
 * <td>At approximately 11:23,000, on March 9th, 2001, <code>/foo/bar.log</code>
 * will be copied to <code>/foo/bar.log.2001-03-09-10-22</code>. Logging for the
 * minute of 11:23 (9th of March) will be output to <code>/foo/bar.log</code>
 * until it is rolled over the next minute.
 * 
 * </table>
 * 
 * <p>
 * Do not use the colon ":" character in anywhere in the <b>DatePattern</b>
 * option. The text before the colon is interpeted as the protocol specificaion
 * of a URL which is probably not what you want.
 * 
 * @author Giuseppe Gerla
 */
public class DailyZipRollingFileAppender extends FileAppender {

	class ModifiedTimeSortableFile extends File implements Serializable,
			Comparable<File> {
		private static final long serialVersionUID = 1373373728209668895L;

		public ModifiedTimeSortableFile(String parent, String child) {
			super(parent, child);
		}

		public ModifiedTimeSortableFile(URI uri) {
			super(uri);
		}

		public ModifiedTimeSortableFile(File parent, String child) {
			super(parent, child);
		}

		public ModifiedTimeSortableFile(String string) {
			super(string);
		}

		public int compareTo(File anotherPathName) {
			long thisVal = this.lastModified();
			long anotherVal = anotherPathName.lastModified();
			return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
		}
	}

	class CleanUpThread implements Runnable {

		private Map<String, List<ModifiedTimeSortableFile>> getAllFiles(String perentDirectory) {
			Map<String, List<ModifiedTimeSortableFile>> groupedFiles = null;
			try {
				// Files grouped by bundle, if using Sift appender.
				// Otherwise, a single group containing the karaf.log (and backups).
				groupedFiles = new HashMap<String, List<ModifiedTimeSortableFile>>();
				List<ModifiedTimeSortableFile> files = null;
				File dir = new File(perentDirectory);
				String[] names = dir.list();
				String uniquePartOfName;

				for (int i = 0; i < names.length; i++) {
					int truncateToLength = names[i].length() - datePattern.length();
					if (names[i].endsWith(typeOfCompression)) {
						truncateToLength = truncateToLength - (typeOfCompression.length() + 1);
					}
					uniquePartOfName = names[i].substring(0, truncateToLength);
					if (groupedFiles.containsKey(uniquePartOfName)) {
						files = groupedFiles.get(uniquePartOfName);
					} else {
						files = new ArrayList<ModifiedTimeSortableFile>();
					}
					files.add(new ModifiedTimeSortableFile(dir + System.getProperty("file.separator") + names[i]));
					groupedFiles.put(uniquePartOfName, files);
				}
			} catch (Exception e) {
				LogLog.error("LogLog Error during retrieving all files of parent folder. ", e);
			}
			return groupedFiles;
		}

		public void run() {
			Map<String, List<ModifiedTimeSortableFile>> groupedFiles = getAllFiles(perentDirectory);
			Iterator iterator = groupedFiles.entrySet().iterator();
			List<ModifiedTimeSortableFile> files = null;
			// For each group of files, delete the backups that exceed the maxBackupIndex.
			// Note: there will only be single group if Sift appender is not used.
			while(iterator.hasNext()) {
				Map.Entry<String, List<ModifiedTimeSortableFile>> pair = (Map.Entry) iterator.next();
				files = pair.getValue();
				if (files.size() > (maxBackupIndex + 1)) {
					Collections.sort(files);

					int index = 0;
					int diff = files.size() - (maxBackupIndex + 1);

					for (ModifiedTimeSortableFile file : files) {
						if (index >= diff)
							break;

						file.delete();
						index++;
					}
				}
			}
		}
	}

	/**
	 * Compresses the passed file to a .zip file, stores the .zip in the same
	 * directory as the passed file, and then deletes the original, leaving only
	 * the .zipped archive.
	 * 
	 * @param file
	 */
	class CompressFile implements Runnable {
		File file;

		public CompressFile(File file) {
			this.file = file;
		}

		
		public void run() {
			try {
				if (!file.getName().endsWith("." + typeOfCompression)) {
					if ("zip".equals(typeOfCompression)) {
						File zipFile = new File(file.getParent(),
								file.getName() + ".zip");
						FileInputStream fis = new FileInputStream(file);
						FileOutputStream fos = new FileOutputStream(zipFile);
						ZipOutputStream zos = new ZipOutputStream(fos);
						ZipEntry zipEntry = new ZipEntry(file.getName());
						zos.putNextEntry(zipEntry);

						byte[] buffer = new byte[4096];
						while (true) {
							int bytesRead = fis.read(buffer);
							if (bytesRead == -1)
								break;
							else {
								zos.write(buffer, 0, bytesRead);
							}
						}
						zos.closeEntry();
						fis.close();
						zos.close();
						file.delete();
					} else if ("gz".equals(typeOfCompression)) {
						File zipFile = new File(file.getParent(),
								file.getName() + ".gz");
						FileInputStream fis = new FileInputStream(file);
						FileOutputStream fos = new FileOutputStream(zipFile);
						GZIPOutputStream zos = new GZIPOutputStream(fos);

						byte[] buffer = new byte[4096];
						while (true) {
							int bytesRead = fis.read(buffer);
							if (bytesRead == -1)
								break;
							else {
								zos.write(buffer, 0, bytesRead);
							}
						}
						fis.close();
						zos.close();
						file.delete();
					}
				}
			} catch (IOException e) {
				LogLog.error("Error during compression of file " + fileName
						+ ".", e);
			}
		}
	}

	// The code assumes that the following constants are in a increasing
	// sequence.
	static final int TOP_OF_TROUBLE = -1;
	static final int TOP_OF_MINUTE = 0;
	static final int TOP_OF_HOUR = 1;
	static final int HALF_DAY = 2;
	static final int TOP_OF_DAY = 3;
	static final int TOP_OF_WEEK = 4;
	static final int TOP_OF_MONTH = 5;

	/**
	 * The date pattern. By default, the pattern is set to "'.'yyyy-MM-dd"
	 * meaning daily rollover.
	 */
	private String datePattern = "'.'yyyy-MM-dd";

	/**
	 * The log file will be renamed to the value of the scheduledFilename
	 * variable when the next interval is entered. For example, if the rollover
	 * period is one hour, the log file will be renamed to the value of
	 * "scheduledFilename" at the beginning of the next hour.
	 * 
	 * The precise time when a rollover occurs depends on logging activity.
	 */
	private String scheduledFilename;

	/**
	 * The next time we estimate a rollover should occur.
	 */
	private long nextCheck = System.currentTimeMillis() - 1;

	Date now = null;

	SimpleDateFormat sdf;

	RollingCalendar rc = new RollingCalendar();

	int checkPeriod = TOP_OF_TROUBLE;
	protected int maxBackupIndex = 1;
	private String compressBackups = "false";
	private String typeOfCompression = "gz";
	private String baseFileName;
	private String perentDirectory;

	// The gmtTimeZone is used only in computeCheckPeriod() method.
	static final TimeZone gmtTimeZone = TimeZone.getTimeZone("GMT");

	/**
	 * The default constructor does nothing.
	 */
	public DailyZipRollingFileAppender() {
	}

	/**
	 * Instantiate a <code>DailyRollingFileAppender</code> and open the file
	 * designated by <code>filename</code>. The opened filename will become the
	 * ouput destination for this appender.
	 */
	public DailyZipRollingFileAppender(Layout layout, String filename,
			String datePattern) throws IOException {
		super(layout, filename, true);
		this.datePattern = datePattern;
		activateOptions();
	}

	/**
	 * The <b>DatePattern</b> takes a string in the same format as expected by
	 * {@link SimpleDateFormat}. This options determines the rollover schedule.
	 */
	public void setDatePattern(String pattern) {
		datePattern = pattern;
	}

	/** Returns the value of the <b>DatePattern</b> option. */
	public String getDatePattern() {
		return datePattern;
	}

	public int getMaxBackupIndex() {
		return maxBackupIndex;
	}

	public void setMaxBackupIndex(int maxBackups) {
		this.maxBackupIndex = maxBackups;
	}

	public String getCompressBackups() {
		return compressBackups;
	}

	public void setCompressBackups(String compressBackups) {
		this.compressBackups = compressBackups;
	}

	public String getTypeOfCompression() {
		return typeOfCompression;
	}

	public void setTypeOfCompression(String typeOfCompression) {
		this.typeOfCompression = typeOfCompression;
	}

	public void activateOptions() {
		if (datePattern != null && fileName != null) {
			if(now==null) {
				now = new Date();
				now.setTime(System.currentTimeMillis());
			}
			sdf = new SimpleDateFormat(datePattern);
			int type = computeCheckPeriod();
			printPeriodicity(type);
			rc.setType(type);
			nextCheck = rc.getNextCheckMillis(now);
			if (this.baseFileName == null)
				this.baseFileName = fileName;
			scheduledFilename = baseFileName + sdf.format(now);
			fileName = scheduledFilename;

			if (perentDirectory == null) {
				File file = new File(fileName);
				if (file != null) {
					perentDirectory = file.getParent();
					if (file.exists()) {
						if (file.getParent() == null) {
							String absolutePath = file.getAbsolutePath();
							perentDirectory = absolutePath.substring(0,
									absolutePath.lastIndexOf(fileName));
						}
					}
				}
			}
		} else {
			LogLog.error("Either File or DatePattern options are not set for appender ["
					+ name + "].");
		}
		super.activateOptions();
	}

	void printPeriodicity(int type) {
		switch (type) {
		case TOP_OF_MINUTE:
			LogLog.debug("Appender [" + name + "] to be rolled every minute.");
			break;
		case TOP_OF_HOUR:
			LogLog.debug("Appender [" + name
					+ "] to be rolled on top of every hour.");
			break;
		case HALF_DAY:
			LogLog.debug("Appender [" + name
					+ "] to be rolled at midday and midnight.");
			break;
		case TOP_OF_DAY:
			LogLog.debug("Appender [" + name + "] to be rolled at midnight.");
			break;
		case TOP_OF_WEEK:
			LogLog.debug("Appender [" + name
					+ "] to be rolled at start of week.");
			break;
		case TOP_OF_MONTH:
			LogLog.debug("Appender [" + name
					+ "] to be rolled at start of every month.");
			break;
		default:
			LogLog.warn("Unknown periodicity for appender [" + name + "].");
		}
	}

	int computeCheckPeriod() {
		RollingCalendar rollingCalendar = new RollingCalendar(gmtTimeZone,
				Locale.getDefault());
		// set sate to 1970-01-01 00:00:00 GMT
		Date epoch = new Date(0);
		if (datePattern != null) {
			for (int i = TOP_OF_MINUTE; i <= TOP_OF_MONTH; i++) {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
						datePattern);
				simpleDateFormat.setTimeZone(gmtTimeZone); // do all date
															// formatting in GMT
				String r0 = simpleDateFormat.format(epoch);
				rollingCalendar.setType(i);
				Date next = new Date(rollingCalendar.getNextCheckMillis(epoch));
				String r1 = simpleDateFormat.format(next);
				// System.out.println("Type = "+i+", r0 = "+r0+", r1 = "+r1);
				if (r0 != null && r1 != null && !r0.equals(r1)) {
					return i;
				}
			}
		}
		return TOP_OF_TROUBLE; // Deliberately head for trouble...
	}

	/**
	 * Rollover the current file to a new file.
	 */
	void rollOver() throws IOException {

		// If compression is enabled we start a separated thread
		if (getCompressBackups().equalsIgnoreCase("YES")
				|| getCompressBackups().equalsIgnoreCase("TRUE")) {
			File file = new File(fileName);
			Thread fileCompressor = new Thread(new CompressFile(file));
			fileCompressor.start();
		}

		/* Compute filename, but only if datePattern is specified */
		if (datePattern == null) {
			errorHandler.error("Missing DatePattern option in rollOver().");
			return;
		}

		String datedFilename = baseFileName + sdf.format(now);
		// It is too early to roll over because we are still within the
		// bounds of the current interval. Rollover will occur once the
		// next interval is reached.
		if (scheduledFilename.equals(datedFilename)) {
			return;
		}

		activateOptions();
	}

	/*
	 * This method checks to see if we're exceeding the number of log backups
	 * that we are supposed to keep, and if so, deletes the offending files. It
	 * then delegates to the rollover method to rollover to a new file if
	 * required.
	 */
	protected void cleanupAndRollOver() throws IOException {
		// Since the deletion of older files can be slow, it will launch a
		// separate
		// thread
		Thread clth = new Thread(new CleanUpThread());
		clth.start();

		rollOver();
	}

	/**
	 * This method differentiates DailyRollingFileAppender from its super class.
	 * 
	 * <p>
	 * Before actually logging, this method will check whether it is time to do
	 * a rollover. If it is, it will schedule the next rollover time and then
	 * rollover.
	 * */
	protected void subAppend(LoggingEvent event) {
		long n = event.timeStamp;
		if (n >= nextCheck) {
			try {
				now.setTime(n);
				cleanupAndRollOver();
			} catch (IOException ioe) {
				if (ioe instanceof InterruptedIOException) {
					Thread.currentThread().interrupt();
				}
				LogLog.error("rollOver() failed.", ioe);
			}
		}
		super.subAppend(event);
	}
}

/**
 * RollingCalendar is a helper class to DailyRollingFileAppender. Given a
 * periodicity type and the current time, it computes the start of the next
 * interval.
 * */
class RollingCalendar extends GregorianCalendar {
	private static final long serialVersionUID = -3560331770601814177L;

	int type = DailyZipRollingFileAppender.TOP_OF_TROUBLE;

	RollingCalendar() {
		super();
	}

	RollingCalendar(TimeZone tz, Locale locale) {
		super(tz, locale);
	}

	void setType(int type) {
		this.type = type;
	}

	public long getNextCheckMillis(Date now) {
		return getNextCheckDate(now).getTime();
	}

	public Date getNextCheckDate(Date now) {
		this.setTime(now);

		switch (type) {
		case DailyZipRollingFileAppender.TOP_OF_MINUTE:
			this.set(Calendar.SECOND, 0);
			this.set(Calendar.MILLISECOND, 0);
			this.add(Calendar.MINUTE, 1);
			break;
		case DailyZipRollingFileAppender.TOP_OF_HOUR:
			this.set(Calendar.MINUTE, 0);
			this.set(Calendar.SECOND, 0);
			this.set(Calendar.MILLISECOND, 0);
			this.add(Calendar.HOUR_OF_DAY, 1);
			break;
		case DailyZipRollingFileAppender.HALF_DAY:
			this.set(Calendar.MINUTE, 0);
			this.set(Calendar.SECOND, 0);
			this.set(Calendar.MILLISECOND, 0);
			int hour = get(Calendar.HOUR_OF_DAY);
			if (hour < 12) {
				this.set(Calendar.HOUR_OF_DAY, 12);
			} else {
				this.set(Calendar.HOUR_OF_DAY, 0);
				this.add(Calendar.DAY_OF_MONTH, 1);
			}
			break;
		case DailyZipRollingFileAppender.TOP_OF_DAY:
			this.set(Calendar.HOUR_OF_DAY, 0);
			this.set(Calendar.MINUTE, 0);
			this.set(Calendar.SECOND, 0);
			this.set(Calendar.MILLISECOND, 0);
			this.add(Calendar.DATE, 1);
			break;
		case DailyZipRollingFileAppender.TOP_OF_WEEK:
			this.set(Calendar.DAY_OF_WEEK, getFirstDayOfWeek());
			this.set(Calendar.HOUR_OF_DAY, 0);
			this.set(Calendar.MINUTE, 0);
			this.set(Calendar.SECOND, 0);
			this.set(Calendar.MILLISECOND, 0);
			this.add(Calendar.WEEK_OF_YEAR, 1);
			break;
		case DailyZipRollingFileAppender.TOP_OF_MONTH:
			this.set(Calendar.DATE, 1);
			this.set(Calendar.HOUR_OF_DAY, 0);
			this.set(Calendar.MINUTE, 0);
			this.set(Calendar.SECOND, 0);
			this.set(Calendar.MILLISECOND, 0);
			this.add(Calendar.MONTH, 1);
			break;
		default:
			throw new IllegalStateException("Unknown periodicity type.");
		}
		return getTime();
	}
}