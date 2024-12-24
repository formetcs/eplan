package eplan.conditions;

import org.jdom2.Element;

/**
 * Condition class to negate a given other condition.
 * 
 * @author Stefan Dillmann
 *
 */
public class ConditionNegation implements Evaluable {
	
	//@ represents depth = condition.depth + 1;
	//@ represents footprint_depth = condition,condition.footprint_depth;
	//@ represents footprint = this.*,condition.footprint;
		
	//@ public invariant \invariant_for(condition) && condition != this;
	
	
	/**
	 * The condition that should be negated.
	 */
	private /*@ spec_public @*/ Evaluable condition;
	
	/**
	 * Creates a new ConditionNegation.
	 * 
	 * @param ev the condition that should be negated
	 */
	/*@ public normal_behavior
	  @ requires \invariant_for(ev);
	  @ requires ev != this;
	  @ ensures this.condition == ev;
	  @ assignable \nothing;
	  @*/
	public ConditionNegation(Evaluable ev) {
		condition = ev;
	}

	/*@ public normal_behavior
	  @ requires \invariant_for(e);
	  @ measured_by depth;
	  @ diverges false;
	  @ assignable \nothing;
	  @ ensures \result != \old(this.condition.evaluate(e));
	  @ ensures \invariant_for(e);
	  @*/
	@Override
	public boolean evaluate(Element e) {
		return (! condition.evaluate(e));
	}

}
