package eplan.conditions;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;

/**
 * Condition class to perform a disjunction (OR operation) over multiple other conditions.
 * The condition evaluates to false only if all other conditions evaluate to false.
 * Once a condition is evaluated to true, the whole disjunction evaluates to true and the remaining
 * conditions are not evaluated anymore (short-circuit).
 * Note that an empty list of conditions evaluates to false, which is conform to the mathematical convention.
 * 
 * @author Stefan Dillmann
 *
 */
public class ConditionDisjunction implements Evaluable {

	/**
	 * The list of conditions that should be evaluated.
	 */
	private List<Evaluable> conditionList;
	
	/**
	 * Creates a new ConditionDisjunction.
	 * 
	 * @param evaluables a variable number of conditions that should be evaluated
	 */
	public ConditionDisjunction(Evaluable...evaluables) {
		conditionList = new ArrayList<Evaluable>();
		for(Evaluable e : evaluables) {
			addCondition(e);
		}
	}
	
	/**
	 * Add a new condition to the end of the condition list.
	 * 
	 * @param e the new condition to add
	 */
	public void addCondition(Evaluable e) {
		conditionList.add(e);
	}

	@Override
	public boolean evaluate(Element e) {
		for(int i = 0; i < conditionList.size(); i++) {
			if(conditionList.get(i).evaluate(e)) {
				return true;
			}
		}
		return false;
	}

}
