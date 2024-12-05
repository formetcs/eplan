package eplan;

import org.jdom2.Element;

/**
 * Condition class that evaluates to a constant value, regardless of the content of a given DOM element.
 * 
 * @author Stefan Dillmann
 *
 */
public class ConstantCondition implements Evaluable {
	
	/**
	 * The constant value that should be returned during evaluation.
	 */
	private boolean constantValue;
	
	/**
	 * Creates a new ConstantCondition.
	 * 
	 * @param b the constant value that should be returned during evaluation
	 */
	public ConstantCondition(boolean b) {
		constantValue = b;
	}

	/**
	 * Evaluation function.
	 * 
	 * @param e the DOM element to test
	 * @return the predefined constant value (regardless of the given object)
	 */
	@Override
	public boolean evaluate(Element e) {
		return constantValue;
	}

}
