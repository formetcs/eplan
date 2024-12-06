/**
 * EPlan - Automated ETCS Planning Tool
 * 
 * Copyright (c) 2017-2024, The FormETCS Project. All rights reserved.
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
	private static final String VERSION = "0.2.0";
	
	/**
	 * Version string of the supported PlanPro version.
	 */
	private static final String PLANPRO_VERSION = "1.8.0";
	
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
		options.addOption("h", "help", false, "print this message");
		Option aboutOption = Option.builder().longOpt("about").desc("print information about the program and exit").build();
		options.addOption(aboutOption);
		Option outfileOption = Option.builder("o").hasArg().argName("file").desc("write generated plan to <file> instead of stdout").build();
		options.addOption(outfileOption);
		Option levelOption = Option.builder("l").longOpt("level").hasArg().argName("lv").desc("generate a plan for ETCS Level <lv>").build();
		options.addOption(levelOption);
		Option addOption = Option.builder("a").longOpt("add").hasArg().argName("type").desc("add data point <type> to the plan").build();
		options.addOption(addOption);
		Option removeOption = Option.builder("r").longOpt("remove").hasArg().argName("type").desc("remove data point <type> from the plan").build();
		options.addOption(removeOption);
		Option verboseOption = Option.builder().longOpt("verbose").desc("verbose mode (prints out more information)").build();
		options.addOption(verboseOption);
		Option debugOption = Option.builder().longOpt("debug").desc("print debug information").build();
		options.addOption(debugOption);
		Option logfileOption = Option.builder().longOpt("logfile").hasArg().argName("file").desc("write verbose or debug info to <file> instead of stdout").build();
		options.addOption(logfileOption);
		Option compatibilityOption = Option.builder("c").longOpt("compatibility").desc("compatibility mode for files in an older PlanPro format").build();
		options.addOption(compatibilityOption);
		
		String infile = null;
		String outfile = null;
		int etcslevel = 2;
		String[] addlist = null;
		String[] removelist = null;
		boolean compatibilityMode = false;
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
				System.out.println("Copyright (c) 2017-2024, The FormETCS Project. All rights reserved.");
				System.out.println("This program is licensed under the terms of the Modified (3-Clause) BSD License.");
				System.exit(0);
			}
			if(cmd.hasOption("o")) {
				outfile = cmd.getOptionValue("o");
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
			if(cmd.hasOption("verbose")) {
				Logger.enable(true);
			}
			if(cmd.hasOption("debug")) {
				Logger.enableDebug(true);
			}
			if(cmd.hasOption("logfile")) {
				String logfile = cmd.getOptionValue("logfile");
				Logger.setLogfile(logfile);
			}
			if(cmd.hasOption("c")) {
				compatibilityMode = true;
			}
			
			String[] remainingArgs = cmd.getArgs();
			if(remainingArgs.length != 1) {
				System.out.println("Wrong argument count, type 'eplan -h' for help");
				System.exit(0);
			}
			else {
				infile = remainingArgs[0];
			}
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			PlanProModel ppm = new PlanProModel();
			ppm.readFile(infile);
			
			Constructor constructor = new Constructor(ppm);
			constructor.setEtcslevel(etcslevel);
			constructor.setSelectionLists(addlist, removelist);
			constructor.setCompatibilityMode(compatibilityMode);
			constructor.constructEtcsLine();
			
			if(outfile != null) {
				ppm.writeFile(outfile);
			}
			else {
				System.out.println(ppm);
			}
			Logger.writeLogfile();
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
