package eplan.conditions;

import org.jdom2.Element;

/**
 * Condition class that evaluates to a constant value, regardless of the content of a given DOM element.
 * 
 * @author Stefan Dillmann
 *
 */
public class ConstantCondition implements Evaluable {
	
	//@ represents depth = 0;
	// represents footprint_depth = \empty;
	
	/**
	 * The constant value that should be returned during evaluation.
	 */
	private /*@ spec_public @*/ boolean constantValue;
	
	/**
	 * Creates a new ConstantCondition.
	 * 
	 * @param b the constant value that should be returned during evaluation
	 */
	/*@ public normal_behavior
	  @ ensures this.constantValue == b;
	  @ assignable \nothing;
	  @*/
	public ConstantCondition(boolean b) {
		constantValue = b;
	}

	/*@ public normal_behavior
	  @ requires \invariant_for(e);
	  @ ensures \result == constantValue;
	  @ ensures \invariant_for(e);
	  @ assignable \nothing;
	  @ accessible constantValue;
	  @*/
	@Override
	public boolean evaluate(Element e) {
		return constantValue;
	}

}
