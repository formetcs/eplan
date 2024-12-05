package eplan;

import org.jdom2.Element;

/**
 * Condition class to negate a given other condition.
 * 
 * @author Stefan Dillmann
 *
 */
public class ConditionNegation implements Evaluable {
	
	/**
	 * The condition that should be negated.
	 */
	private Evaluable condition;
	
	/**
	 * Creates a new ConditionNegation.
	 * 
	 * @param ev the condition that should be negated
	 */
	public ConditionNegation(Evaluable ev) {
		condition = ev;
	}

	/**
	 * Evaluation function.
	 * 
	 * @param e the DOM element to test
	 * @return true if the condition is NOT fulfilled for the given object, otherwise false
	 */
	@Override
	public boolean evaluate(Element e) {
		return (! condition.evaluate(e));
	}

}
