package org.apache.commons.cli;

public interface CommandLineParser {

	public org.apache.commons.cli.CommandLine parse(org.apache.commons.cli.Options options, java.lang.String[] arguments) throws org.apache.commons.cli.ParseException;

	public org.apache.commons.cli.CommandLine parse(org.apache.commons.cli.Options options, java.lang.String[] arguments, boolean stopAtNonOption) throws org.apache.commons.cli.ParseException;

}
