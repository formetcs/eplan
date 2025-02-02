/**
 * EPlan - Automated ETCS Planning Tool
 * 
 * Copyright (c) 2017-2024, The FormETCS Project. All rights reserved.
 * This file is licensed under the terms of the Modified (3-Clause) BSD License.
 * 
 * SPDX-License-Identifier: BSD-3-Clause
 */

package eplan;

import java.util.List;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Condition class to test if a DOM element is a signal with a given signal aspect.
 * 
 * @author Stefan Dillmann
 *
 */
public class SignalAspectCondition implements Evaluable {
	
	/**
	 * The ID of the signal aspect to test against.
	 */
	private String signalAspectId;
	
	/**
	 * The PlanPro model.
	 */
	private PlanProModel ppm;
	
	/**
	 * Creates a new SignalAspectCondition
	 * 
	 * @param p the PlanPro model
	 * @param signalbegriffId the ID of the signal aspect to test against
	 */
	public SignalAspectCondition(PlanProModel p, String signalbegriffId) {
		ppm = p;
		signalAspectId = signalbegriffId;
	}

	/**
	 * Evaluation function.
	 * 
	 * @param e the DOM element to test
	 * @return true if the object is a signal and has the required signal aspect, otherwise false
	 */
	@Override
	public boolean evaluate(Element e) {
		String testingAspect = "nsSignalbegriffe_Ril_301:" + signalAspectId;
		List<Element> objectList1 = ppm.getPlanProObjectList();
		for(int i = 0; i < objectList1.size(); i++) {
			Element currentObject1 = objectList1.get(i);
			if(!currentObject1.getName().equals("Signal_Signalbegriff")) {
				continue;
			}
			String signalAspect = currentObject1.getChild("Signalbegriff_ID").getAttribute("type", Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance")).getValue();
			if(signalAspect.equals(testingAspect)) {
				String idSignalRahmen = currentObject1.getChild("ID_Signal_Rahmen").getChild("Wert").getText();
				List<Element> objectList2 = ppm.getPlanProObjectList();
				for(int j = 0; j < objectList2.size(); j++) {
					Element currentObject2 = objectList2.get(j);
					if(!currentObject2.getName().equals("Signal_Rahmen")) {
						continue;
					}
					if(currentObject2.getChild("Identitaet").getChild("Wert").getText().equals(idSignalRahmen)) {
						String idSignal = currentObject2.getChild("ID_Signal").getChild("Wert").getText();
						if(e.getChild("Identitaet").getChild("Wert").getText().equals(idSignal)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

}
