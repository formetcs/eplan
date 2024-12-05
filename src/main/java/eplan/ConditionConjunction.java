package eplan;

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
	
	/**
	 * The list of conditions that should be evaluated.
	 */
	private List<Evaluable> conditionList;
	
	/**
	 * Creates a new ConditionConjunction.
	 * 
	 * @param evaluables a variable number of conditions that should be evaluated
	 */
	public ConditionConjunction(Evaluable...evaluables) {
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

	/**
	 * Evaluation function.
	 * 
	 * @param e the DOM element to test
	 * @return true if all containing conditions are fulfilled for the given object, otherwise false
	 */
	@Override
	public boolean evaluate(Element e) {
		for(int i = 0; i < conditionList.size(); i++) {
			if(!conditionList.get(i).evaluate(e)) {
				return false;
			}
		}
		return true;
	}

}
