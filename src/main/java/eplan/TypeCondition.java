package eplan;

import org.jdom2.Element;

/**
 * Condition class to test if a DOM element is of a given type.
 * 
 * @author Stefan Dillmann
 *
 */
public class TypeCondition implements Evaluable {
	
	/**
	 * The type value to test against.
	 */
	private String compareValue;
	
	
	/**
	 * Creates a new TypeCondition.
	 * 
	 * @param val the type value to test against
	 */
	public TypeCondition(String val) {
		compareValue = val;
	}

	@Override
	public boolean evaluate(Element e) {
		if(e.getName().equals(compareValue)) return true;
		return false;
	}

}