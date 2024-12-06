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
 * Interface for condition classes, testing a DOM element (and its child elements) for logical conditions.
 * 
 * @author Stefan Dillmann
 *
 */
public interface Evaluable {
	
	/**
	 * Evaluation function.
	 * 
	 * @param e the DOM element to test
	 * @return true if the condition is fulfilled, otherwise false
	 */
	boolean evaluate(Element e);

}
