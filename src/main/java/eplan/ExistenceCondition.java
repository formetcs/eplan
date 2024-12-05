package eplan;

import org.jdom2.Element;

/**
 * Condition class to test if a path is reachable from a given DOM element.
 * 
 * @author Stefan Dillmann
 *
 */
public class ExistenceCondition extends AbstractComparisonCondition {
	
	/**
	 * The path below the given DOM element.
	 */
	private String comparePath;
	
	/**
	 * Creates a new ExistenceCondition.
	 * 
	 * @param s the path below the given DOM element
	 */
	public ExistenceCondition(String s) {
		comparePath = s;
	}

	/**
	 * Evaluation function.
	 * 
	 * @param e the DOM element to test
	 * @return true if the object has the required path, otherwise false
	 */
	@Override
	public boolean evaluate(Element e) {
		return isPathExisting(e, comparePath);
	}

}
