/*
 * Copyright 2010 Ralf Haug
 * Copyright 2012 Achim Nierbeck.
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

package org.apache.log4j.zip;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.helpers.CountingQuietWriter;
import org.apache.log4j.helpers.LogLog;

/**
 * A log4j appender which acts as a RollingFileAppender. 
 * When the file is rolled over its zipped to a zip file. 
 */
public class ZipRollingFileAppender extends RollingFileAppender {

	private static final String ZIP = ".zip";

	public void rollOver() {

		File target;
		File file;

		if (qw != null) {
			LogLog.debug("rolling over count="
					+ ((CountingQuietWriter) qw).getCount());
		}
		LogLog.debug("maxBackupIndex=" + maxBackupIndex);

		// If maxBackups <= 0, then there is no file renaming to be done.
		if (maxBackupIndex > 0) {
			// Delete the oldest file, to keep Windows happy.
			file = new File(fileName + '.' + maxBackupIndex + ZIP); 
			if (file.exists())
				file.delete();

			// Map {(maxBackupIndex - 1), ..., 2, 1} to {maxBackupIndex, ..., 3,
			// 2}
			for (int i = maxBackupIndex - 1; i >= 1; i--) {
				file = new File(fileName + "." + i + ZIP);
				if (file.exists()) {
					target = new File(fileName + '.' + (i + 1) + ZIP); 
					LogLog.debug("Renaming file " + file + " to " + target);
					file.renameTo(target);
				}
			}

			// Rename fileName to fileName.1
			target = new File(fileName + "." + 1);

			this.closeFile(); // keep windows happy.

			file = new File(fileName);
			LogLog.debug("Renaming file " + file + " to " + target);
			file.renameTo(target);

			// added by me - call ZIP facility
			boolean archiveResult = archiveFile(target);

			if (archiveResult) {
				target.delete();
			} else {
				LogLog.error("Failed to zip file [" + target.getPath() + "].");
			}
		}

		try {
			// This will also close the file. This is OK since multiple
			// close operations are safe.
			this.setFile(fileName, false, bufferedIO, bufferSize);
		} catch (IOException e) {
			LogLog.error("setFile(" + fileName + ", false) call failed.", e);
		}

	}

	// archive log file
	boolean archiveFile(File logFile) {

		FileOutputStream fOut;
		ZipOutputStream zOut;

		// necessary because of possible IOException
		try {
			fOut = new FileOutputStream(logFile.getPath() + ZIP);
			zOut = new ZipOutputStream(fOut);

			FileInputStream fIn = new FileInputStream(logFile);
			BufferedInputStream bIn = new BufferedInputStream(fIn);

			// new ZipEntry to put into the archive
			ZipEntry entry = new ZipEntry(logFile.getCanonicalFile().getName());
			zOut.putNextEntry(entry);

			// create a byte array
			byte[] barray = new byte[1024];
			// byte count variable
			int bytes;

			// read the BufferedInputStream and write it entirely to the archive
			while ((bytes = bIn.read(barray, 0, 1024)) > -1) {
				zOut.write(barray, 0, bytes);
			}

			// clean up
			zOut.flush();
			zOut.close();
			fOut.close();

			return true;

		} catch (IOException ioE) {
			return false;
		}

	}

}
