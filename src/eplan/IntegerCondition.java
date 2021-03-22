package eplan;

import org.jdom2.Element;

/**
 * Condition class to compare an integer value from a DOM element (and its child elements) with a given comparison value.
 * 
 * @author Stefan Dillmann
 *
 */
public class IntegerCondition extends AbstractComparisonCondition {

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
	private int compareValue;
	
	/**
	 * Creates a new IntegerCondition.
	 * 
	 * @param s the path below the given DOM element
	 * @param op the operator used for comparison
	 * @param val the value to compare with
	 */
	public IntegerCondition(String s, Operator op, int val) {
		comparePath = s;
		compareOperator = op;
		compareValue = val;
	}
	
	@Override
	public boolean evaluate(Element e) {
		int finalValue = Integer.parseInt(getElementValue(e, comparePath));
		if(compareOperator == Operator.EQUAL && finalValue == compareValue) return true;
		if(compareOperator == Operator.NOT_EQUAL && finalValue != compareValue) return true;
		if(compareOperator == Operator.LESS_THAN && finalValue < compareValue) return true;
		if(compareOperator == Operator.LESS_EQUAL && finalValue <= compareValue) return true;
		if(compareOperator == Operator.GREATER_THAN && finalValue > compareValue) return true;
		if(compareOperator == Operator.GREATER_EQUAL && finalValue >= compareValue) return true;
		return false;
	}

}
