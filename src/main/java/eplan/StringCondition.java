/**
 * EPlan - Automated ETCS Planning Tool
 * 
 * Copyright (c) 2017-2024, The FormETCS Project. All rights reserved.
 * This file is licensed under the terms of the Modified (3-Clause) BSD License.
 * 
 * SPDX-License-Identifier: BSD-3-Clause
 */

package eplan;

import org.jdom2.Element;

/**
 * Condition class to test if a DOM element (and its child elements) contains a given String value.
 * 
 * @author Stefan Dillmann
 *
 */
public class StringCondition extends AbstractComparisonCondition {
	
	/**
	 * The path below the given DOM element.
	 */
	private String comparePath;
	
	/**
	 * The operator used for comparison.
	 */
	private Operator compareOperator;
	
	/**
	 * The value to compare with.
	 */
	private String compareValue;
	
	/**
	 * Creates a new StringCondition.
	 * 
	 * @param s the path below the given DOM element
	 * @param op the operator used for comparison
	 * @param val the value to compare with
	 */
	public StringCondition(String s, Operator op, String val) {
		comparePath = s;
		compareOperator = op;
		compareValue = val;
	}

	/**
	 * Evaluation function.
	 * 
	 * @param e the DOM element to test
	 * @return the comparison result of the value from the object at the given path and the given comparison value, using the given comparison operator
	 */
	@Override
	public boolean evaluate(Element e) {
		String finalValue = getElementValue(e, comparePath);
		if(compareOperator == Operator.EQUAL && finalValue.equals(compareValue)) return true;
		if(compareOperator == Operator.NOT_EQUAL && !finalValue.equals(compareValue)) return true;
		if(compareOperator == Operator.LESS_THAN && finalValue.compareTo(compareValue) < 0) return true;
		if(compareOperator == Operator.LESS_EQUAL && finalValue.compareTo(compareValue) <= 0) return true;
		if(compareOperator == Operator.GREATER_THAN && finalValue.compareTo(compareValue) > 0) return true;
		if(compareOperator == Operator.GREATER_EQUAL && finalValue.compareTo(compareValue) >= 0) return true;
		return false;
	}

}
