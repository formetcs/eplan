package org.jdom2.output;

public class XMLOutputter {

	public XMLOutputter();

	public XMLOutputter(org.jdom2.output.Format format);
	

	public final void output(org.jdom2.Document doc, java.io.FileWriter out) throws java.io.IOException;

	public final java.lang.String outputString(org.jdom2.Document doc);

}
