package org.jdom2;

public class Element {

public String getName();

public String getText();

public Element 	getChild(String cname);

public Attribute getAttribute(String attname, Namespace ns);

public Attribute getAttribute(String attname);

}
