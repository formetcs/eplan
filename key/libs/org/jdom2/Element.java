package org.jdom2;

public class Element {

	//@ accessible \inv:this.*;
	public Element();

	public Element(java.lang.String name);


	public org.jdom2.Element addContent(org.jdom2.Element child);

	public java.lang.String getName();

	public java.lang.String getText();

	public org.jdom2.Element setText(java.lang.String text);

	public org.jdom2.Element getChild(java.lang.String cname);

	public java.util.List getChildren();

	public java.util.List getChildren(java.lang.String cname);

	public org.jdom2.Attribute getAttribute(java.lang.String attname, org.jdom2.Namespace ns);

	public org.jdom2.Attribute getAttribute(java.lang.String attname);

}
