package org.apache.commons.cli;

public class Option {

	public static final class Builder {

		public org.apache.commons.cli.Option.Builder hasArg();

		public org.apache.commons.cli.Option.Builder argName(java.lang.String argName);

		public org.apache.commons.cli.Option.Builder longOpt(java.lang.String longOption);

		public org.apache.commons.cli.Option.Builder desc(java.lang.String description);

		public org.apache.commons.cli.Option build();

	}


	public Option(java.lang.String option, boolean hasArg, java.lang.String description);

	public Option(java.lang.String option, java.lang.String description);

	public Option(java.lang.String option, java.lang.String longOption, boolean hasArg, java.lang.String description);


	public static org.apache.commons.cli.Option.Builder builder();

	public static org.apache.commons.cli.Option.Builder builder(java.lang.String option);

}
