package eplan.conditions;

import org.jdom2.Element;

/**
 * Condition class to perform a conjunction (AND operation) over two other conditions.
 * The condition evaluates to true only if both conditions evaluate to true.
 * Once a condition is evaluated to false, the whole conjunction evaluates to false and the remaining
 * condition is not evaluated anymore (short-circuit).
 * 
 * @author Stefan Dillmann
 *
 */
public class ConditionConjunctionBinary implements Evaluable {
	
	//@ represents depth = condition1.depth > condition2.depth ? condition1.depth + 1 : condition2.depth + 1;
	// represents footprint_depth = condition1,condition2,condition1.footprint_depth,condition2.footprint_depth;
	//@ represents footprint = this.*,condition1.footprint,condition2.footprint;
			
	//@ public invariant \invariant_for(condition1) && condition1 != this && \invariant_for(condition2) && condition2 != this;
	
	/**
	 * The conditions that should be evaluated.
	 */
	private /*@ spec_public @*/ Evaluable condition1;
	private /*@ spec_public @*/ Evaluable condition2;
	
	/**
	 * Creates a new ConditionConjunctionBinary.
	 * 
	 * @param cond1 first condition that should be evaluated
	 * @param cond2 second condition that should be evaluated
	 */
	/*@ public normal_behavior
	  @ requires \invariant_for(cond1);
	  @ requires \invariant_for(cond2);
	  @ requires cond1 != this;
	  @ requires cond2 != this;
	  @ ensures this.condition1 == cond1;
	  @ ensures this.condition2 == cond2;
	  @ assignable \nothing;
	  @*/
	public ConditionConjunctionBinary(Evaluable cond1, Evaluable cond2) {
		condition1 = cond1;
		condition2 = cond2;
	}


	/*@ public normal_behavior
	  @ requires \invariant_for(e);
	  @ measured_by depth;
	  @ assignable \nothing;
	  @ ensures \result == this.condition1.evaluate(e) && this.condition2.evaluate(e);
	  @ ensures \invariant_for(e);
	  @*/
	@Override
	public boolean evaluate(Element e) {
		return (condition1.evaluate(e) && condition2.evaluate(e));
	}

}
