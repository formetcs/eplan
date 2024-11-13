package eplan.conditions;

import org.jdom2.Element;

/**
 * Interface for condition classes, testing a DOM element (and its child elements) for logical conditions.
 * 
 * @author Stefan Dillmann
 *
 */
public interface Evaluable {
	
	/**
	 * Evaluation function.
	 * 
	 * @param e the DOM element to test
	 * @return true if the condition is fulfilled, otherwise false
	 */
	boolean evaluate(Element e);

}
