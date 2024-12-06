/**
 * EPlan - Automated ETCS Planning Tool
 * 
 * Copyright (c) 2017-2024, The FormETCS Project. All rights reserved.
 * This file is licensed under the terms of the Modified (3-Clause) BSD License.
 * 
 * SPDX-License-Identifier: BSD-3-Clause
 */

package eplan;

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

	/**
	 * Evaluation function.
	 * 
	 * @param e the DOM element to test
	 * @return true if at least one containing condition is fulfilled for the given object, otherwise false
	 */
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
