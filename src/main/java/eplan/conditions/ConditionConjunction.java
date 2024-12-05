package eplan.conditions;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;

/**
 * Condition class to perform a conjunction (AND operation) over multiple other conditions.
 * The condition evaluates to true only if all other conditions evaluate to true.
 * Once a condition is evaluated to false, the whole conjunction evaluates to false and the remaining
 * conditions are not evaluated anymore (short-circuit).
 * Note that an empty list of conditions evaluates to true, which is conform to the mathematical convention.
 * 
 * @author Stefan Dillmann
 *
 */
public class ConditionConjunction implements Evaluable {
	
	// represents depth = (\max int i; i >= 0 && i < conditionList.size(); ((Evaluable) conditionList.get(i)).depth) + 1;
	// represents depth = maxdepth_list(conditionList) + 1;
	//@ represents depth \such_that (\forall int i; i >= 0 && i < conditionList.size(); this.depth > ((Evaluable) conditionList.get(i)).depth);


	// represents footprint = this.*,conditionList[*],(\infinite_union int k; k >= 0 && k < this.conditionList.size(); ((Evaluable) this.conditionList.get(k)).footprint);
	// represents footprint_depth = conditionList,conditionList[*],(\infinite_union int k; k >= 0 && k < this.conditionList.size(); ((Evaluable) this.conditionList.get(k)).footprint_depth);


	//@ public invariant (\forall int i; 0 <= i && i < conditionList.seq.length; \invariant_for((Evaluable) conditionList.seq[i]));
	//@ public invariant (\forall int i; 0 <= i && i < conditionList.seq.length;  conditionList.seq[i] instanceof Evaluable);
	//@ public invariant (\forall int i; 0 <= i && i < conditionList.seq.length; conditionList.seq[i] != this);
	// public invariant \invariant_for(conditionList) && (\forall Evaluable e; e != this; \disjoint(\set_union(e.footprint,e.footprint_depth),\set_union(\all_fields(this.conditionList), \singleton(this.conditionList))));
	
	/**
	 * The list of conditions that should be evaluated.
	 */
	private /*@ spec_public @*/ List conditionList;
	
	/**
	 * Creates a new ConditionConjunction.
	 * 
	 * @param evaluables a variable number of conditions that should be evaluated
	 */
	/*@ public normal_behavior
	  @ requires (\forall int i; 0 <= i && i < evaluables.length; \invariant_for(evaluables[i]));
	  @ requires (\forall int i; 0 <= i && i < evaluables.length; evaluables[i] != this);
	  @ assignable \nothing;
	  @ ensures \fresh(conditionList);
	  @ ensures (\forall int i; 0 <= i && i < conditionList.size(); conditionList.get(i) == evaluables[i]);
	  @ ensures conditionList.size() == evaluables.length;
	  @ ensures \new_elems_fresh(footprint);
	  @*/
	public ConditionConjunction(Evaluable[] evaluables) {
		conditionList = new ArrayList();
		/*@ loop_invariant i >= 0 && i <= evaluables.length && (\forall int j; 0 <= j && j < i; conditionList.get(j) == evaluables[j]);
		  @ assignable conditionList;
		  @ decreases evaluables.length - i;
		  @*/
		for(int i = 0; i < evaluables.length; i++) {
			addCondition(evaluables[i]);
		}
	}
	
	/**
	 * Add a new condition to the end of the condition list.
	 * 
	 * @param e the new condition to add
	 */
	/*@ public normal_behavior
	  @ requires  \invariant_for(e);
	  @ requires e != this;
	  @ assignable conditionList.seq;
	  @ ensures conditionList.seq == \seq_concat(\old(conditionList.seq),\seq_singleton(e));
	  @ ensures \invariant_for(e);
	  @*/
	public void addCondition(Evaluable e) {
		conditionList.add(e);
	}

	
	
	
	
	

	/*@ public normal_behavior 
	  @ requires \invariant_for(e);
	  @ measured_by depth;
	  @ assignable \nothing;
	  @ ensures \result != (\exists int j; j >= 0 && j < conditionList.size(); !((Evaluable) conditionList.get(j)).evaluate(e));
	  @*/
	@Override
	public boolean evaluate(Element e) {
		/*@ loop_invariant i >= 0 && i <= conditionList.size() &&
		  @    (\forall int j; 0 <= j && j < i; ((Evaluable) conditionList.get(j)).evaluate(e));
		  @ assignable \nothing;
		  @ decreases conditionList.size() - i;
		  @*/
		for(int i = 0; i < conditionList.size(); i++) {
			if(!((Evaluable)conditionList.get(i)).evaluate(e)) {
				return false;
			}
		}
		return true;
	}

}
