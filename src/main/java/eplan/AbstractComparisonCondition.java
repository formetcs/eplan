/**
 * EPlan - Automated ETCS Planning Tool
 * 
 * Copyright (c) 2017-2024, The FormETCS Project. All rights reserved.
 * This file is licensed under the terms of the Modified (3-Clause) BSD License.
 * 
 * SPDX-License-Identifier: BSD-3-Clause
 */

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
		/** Both values have to be equal */
		EQUAL,
		
		/** Both values have to be not equal */
		NOT_EQUAL,
		
		/** The first value has to be less than the second value */
		LESS_THAN,
		
		/** The first value has to be less or equal than the second value */
		LESS_EQUAL,
		
		/** The first value has to be greater than the second value */
		GREATER_THAN,
		
		/** The first value has to be greater or equal than the second value */
		GREATER_EQUAL
	}
	
	
	/**
	 * Default constructor.
	 */
	protected AbstractComparisonCondition() {
		
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
