/**
 * EPlan - Automated ETCS Planning Tool
 * 
 * Copyright (c) 2017-2025, The FormETCS Project. All rights reserved.
 * This file is licensed under the terms of the Modified (3-Clause) BSD License.
 * 
 * SPDX-License-Identifier: BSD-3-Clause
 */

package eplan;

import java.io.IOException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jdom2.JDOMException;

/**
 * Entry point of the program.
 * 
 * @author Stefan Dillmann
 *
 */
public class Main {
	
	/**
	 * Version string of the program.
	 */
	private static final String VERSION = "0.3.0";
	
	/**
	 * Version string of the supported PlanPro version.
	 */
	private static final String PLANPRO_VERSION = "1.9.0";
	
	/**
	 * Disable instance creation.
	 */
	private Main() {
		
	}

	
	/**
	 * Entry point of the program.
	 * 
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
		Options options = new Options();
		options.addOption("v", "version", false, "print version information and exit");
		options.addOption("h", "help", false, "print this message and exit");
		Option aboutOption = Option.builder().longOpt("about").desc("print information about the program and exit").build();
		options.addOption(aboutOption);
		Option outfileOption = Option.builder("o").hasArg().argName("file").desc("write generated plan to <file> instead of stdout").build();
		options.addOption(outfileOption);
		Option stdinOption = Option.builder().longOpt("stdin").desc("read input plan from stdin instead of from file").build();
		options.addOption(stdinOption);
		Option levelOption = Option.builder("l").longOpt("level").hasArg().argName("lv").desc("generate a plan for ETCS Level <lv>").build();
		options.addOption(levelOption);
		Option addOption = Option.builder("a").longOpt("add").hasArg().argName("type").desc("add <type> to the list of data point types to be planned").build();
		options.addOption(addOption);
		Option removeOption = Option.builder("r").longOpt("remove").hasArg().argName("type").desc("remove <type> from the list of data point types to be planned").build();
		options.addOption(removeOption);
		Option quietOption = Option.builder("q").longOpt("quiet").desc("turn off all logging messages").build();
		options.addOption(quietOption);
		Option debugOption = Option.builder().longOpt("debug").desc("print additional debug information").build();
		options.addOption(debugOption);
		Option stderrOption = Option.builder().longOpt("stderr").desc("print all logging/debug info to stderr instead of stdout").build();
		options.addOption(stderrOption);
		Option logfileOption = Option.builder().longOpt("logfile").hasArg().argName("file").desc("write all logging/debug info to <file> instead of stdout").build();
		options.addOption(logfileOption);
		Option compatibilityOption = Option.builder("c").longOpt("compatibility").desc("compatibility mode for files converted from older PlanPro formats").build();
		options.addOption(compatibilityOption);
		
		String infile = null;
		String outfile = null;
		int etcslevel = 2;
		String[] addlist = null;
		String[] removelist = null;
		boolean compatibilityMode = false;
		boolean readFromStdin = false;
		boolean enableLog = true;
		boolean enableDebug = false;
		boolean writeToStderr = false;
		String logfile = null;
		Logger.enable(true);
		Logger.enableDebug(false);
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine cmd = parser.parse(options, args);
			if(cmd.hasOption("h")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp( "eplan [options] FILE", options );
				System.exit(0);
			}
			if(cmd.hasOption("v")) {
				System.out.println("Version: " + VERSION);
				System.exit(0);
			}
			if(cmd.hasOption("about")) {
				System.out.println("EPlan - Automated ETCS Planning Tool");
				System.out.println("Version: " + VERSION);
				System.out.println("Supports PlanPro Version " + PLANPRO_VERSION);
				System.out.println();
				System.out.println("Copyright (c) 2017-2025, The FormETCS Project. All rights reserved.");
				System.out.println("This program is licensed under the terms of the Modified (3-Clause) BSD License.");
				System.exit(0);
			}
			if(cmd.hasOption("o")) {
				outfile = cmd.getOptionValue("o");
			}
			if(cmd.hasOption("stdin")) {
				readFromStdin = true;
			}
			if(cmd.hasOption("l")) {
				etcslevel = Integer.parseInt(cmd.getOptionValue("l"));
			}
			if(cmd.hasOption("a")) {
				addlist = cmd.getOptionValues("a");
			}
			if(cmd.hasOption("r")) {
				removelist = cmd.getOptionValues("r");
			}
			if(cmd.hasOption("debug")) {
				enableDebug = true;
			}
			if(cmd.hasOption("stderr")) {
				writeToStderr = true;
			}
			if(cmd.hasOption("q")) {
				enableLog = false;
				enableDebug = false;
			}
			if(cmd.hasOption("logfile")) {
				logfile = cmd.getOptionValue("logfile");
			}
			if(cmd.hasOption("c")) {
				compatibilityMode = true;
			}
			
			String[] remainingArgs = cmd.getArgs();
			if(remainingArgs.length == 0 && readFromStdin) {
				// do nothing
			}
			else if(remainingArgs.length == 1){
				infile = remainingArgs[0];
			}
			else {
				System.out.println("Wrong argument count, type 'eplan -h' for help");
				System.exit(0);
			}
		} catch (ParseException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
		
		if(outfile == null && logfile == null && !writeToStderr) {
			enableLog = false;
			enableDebug = false;
		}
		
		Logger.enable(enableLog);
		Logger.enableDebug(enableDebug);
		Logger.writeToStderr(writeToStderr);
		Logger.setLogfile(logfile);
		
		try {
			PlanProModel ppm = new PlanProModel();
			if(readFromStdin) {
				ppm.readFromStdin();
			}
			else {
				ppm.readFile(infile);
			}
			Constructor constructor = new Constructor(ppm);
			constructor.setEtcslevel(etcslevel);
			constructor.setSelectionLists(addlist, removelist);
			constructor.setCompatibilityMode(compatibilityMode);
			constructor.constructEtcsLine();
			
			ppm.updatePlanProHeader("EPlan", VERSION);
			
			if(outfile != null) {
				ppm.writeFile(outfile);
			}
			else {
				System.out.println(ppm);
			}
			Logger.writeLogfile();
		} catch (JDOMException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
