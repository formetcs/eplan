package eplan.conditions;

import org.jdom2.Element;

/**
 * Condition class to perform a disjunction (OR operation) over two other conditions.
 * The condition evaluates to false only if both conditions evaluate to false.
 * Once a condition is evaluated to true, the whole disjunction evaluates to true and the remaining
 * condition is not evaluated anymore (short-circuit).
 * 
 * @author Stefan Dillmann
 *
 */
public class ConditionDisjunctionBinary implements Evaluable {
	
	/**
	 * The conditions that should be evaluated.
	 */
	private Evaluable condition1;
	private Evaluable condition2;
	
	/**
	 * Creates a new ConditionDisjunctionBinary.
	 * 
	 * @param cond1 first condition that should be evaluated
	 * @param cond2 second condition that should be evaluated
	 */
	public ConditionDisjunctionBinary(Evaluable cond1, Evaluable cond2) {
		condition1 = cond1;
		condition2 = cond2;
	}
	

	
	
	
	
	@Override
	public boolean evaluate(Element e) {
		return (condition1.evaluate(e) || condition2.evaluate(e));
	}

}
