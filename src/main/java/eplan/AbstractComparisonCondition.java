package eplan;

import org.jdom2.Element;

/**
 * Abstract base class for conditions based on (numerical or string) comparison.
 * 
 * @author Stefan Dillmann
 *
 */
public abstract class AbstractComparisonCondition implements Evaluable {
	
	/**
	 * Enum defining comparison operators.
	 * 
	 * @author Stefan Dillmann
	 *
	 */
	public enum Operator {
		EQUAL, NOT_EQUAL, LESS_THAN, LESS_EQUAL, GREATER_THAN, GREATER_EQUAL
	}
	
	
	/**
	 * Retrieve the value at the end of a path, starting from the given DOM element.
	 * The element names in the path must be separated with a "/" character.
	 * 
	 * @param e the starting DOM element
	 * @param pathString the path below the starting element
	 * @return the value of the last element in the path, or an empty string if the path is not existing
	 */
	protected String getElementValue(Element e, String pathString) {
		Element currentElement = getLastElementInPath(e, pathString);
		if(currentElement == null) {
			return "";
		}
		
		return currentElement.getText();
	}
	
	
	/**
	 * Checks if a path is existing, starting from the given DOM element.
	 * The element names in the path must be separated with a "/" character.
	 * 
	 * @param e the starting DOM element
	 * @param pathString the path below the starting element
	 * @return true if the path is existing, otherwise false
	 */
	protected boolean isPathExisting(Element e, String pathString) {
		Element currentElement = getLastElementInPath(e, pathString);
		return (currentElement != null);
	}
	
	
	/**
	 * Retrieve the element at the end of a path, starting from the given DOM element.
	 * The element names in the path must be separated with a "/" character.
	 * 
	 * @param e the starting DOM element
	 * @param pathString the path below the starting element
	 * @return the last element in the path, or an empty string if the path is not existing
	 */
	private Element getLastElementInPath(Element e, String pathString) {
		String[] patternlist = pathString.split("/");
		Element currentElement = e;
		for(int i = 0; i < patternlist.length; i++) {
			String currentPattern = patternlist[i];
			if(currentElement.getChild(currentPattern) == null) {
				return null;
			}
			currentElement = currentElement.getChild(currentPattern);
		}
		
		return currentElement;
	}

}
