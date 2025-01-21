/**
 * EPlan - Automated ETCS Planning Tool
 * 
 * Copyright (c) 2017-2025, The FormETCS Project. All rights reserved.
 * This file is licensed under the terms of the Modified (3-Clause) BSD License.
 * 
 * SPDX-License-Identifier: BSD-3-Clause
 */

package eplan;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Logging functionality.
 * 
 * @author Stefan Dillmann
 *
 */
public class Logger {
	
	/**
	 * Flag if general messages are enabled.
	 */
	private static boolean enabled = false;
	
	/**
	 * Flag if debug messages are enabled.
	 */
	private static boolean debugEnabled = false;
	
	/**
	 * Flag to write to stderr instead of stdout.
	 */
	private static boolean stderr = false;
	
	/**
	 * Name of the logfile.
	 */
	private static String logfile = null;
	
	/**
	 * Buffer where all messages are stored temporarily when they should be written to file.
	 */
	private static String messageString = "";
	
	
	/**
	 * Disable instance creation.
	 */
	private Logger() {
		
	}
	
	/**
	 * Enable logging of general messages.
	 * 
	 * @param enable true to enable logging, false to disable
	 */
	public static void enable(boolean enable) {
		enabled = enable;
	}
	
	/**
	 * Enable logging of debug messages.
	 * The logging of general messages will also be activated implicitly.
	 * 
	 * @param enable true to enable logging, false to disable
	 */
	public static void enableDebug(boolean enable) {
		debugEnabled = enable;
	}
	
	/**
	 * Enable logging to standard error instead of standard output.
	 * 
	 * @param err true to write to stderr, false to write to stdout
	 */
	public static void writeToStderr(boolean err) {
		stderr = err;
	}
	
	/**
	 * Set the name of the logfile.
	 * If a file name is provided, all general and debug messages will be written into this file.
	 * If null is provided, all messages are printed to standard output or standard error.
	 * 
	 * @param file the name of the logfile to write into, or null to print to stdout/stderr
	 */
	public static void setLogfile(String file) {
		logfile = file;
	}
	
	/**
	 * Print a general message.
	 * This is only effective if logging of general messages is enabled.
	 * 
	 * @param message the message to print
	 */
	public static void log(String message) {
		if(enabled || debugEnabled) {
			if(logfile != null) {
				messageString += message;
				messageString += "\n";
			}
			else if(stderr) {
				System.err.println(message);
			}
			else {
				System.out.println(message);
			}
		}
	}
	
	/**
	 * Print a debug message.
	 * This is only effective if logging of debug messages is enabled.
	 * 
	 * @param message the message to print
	 */
	public static void debug(String message) {
		if(debugEnabled) {
			if(logfile != null) {
				messageString += message;
				messageString += "\n";
			}
			else if(stderr) {
				System.err.println(message);
			}
			else {
				System.out.println(message);
			}
		}
	}
	
	/**
	 * Write the logfile to disk.
	 * If writing to file is activated, all messages are stored temporarily in memory first and not written to file immediately.
	 * Calling this method before terminating the program is required to store the memory content finally to disk.
	 * 
	 * @throws IOException if a file error occurs
	 */
	public static void writeLogfile() throws IOException {
		if(logfile == null) {
			return;
		}
		FileWriter fw = new FileWriter(logfile);
		fw.write(messageString);
		fw.close();
	}
}
