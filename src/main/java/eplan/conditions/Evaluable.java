package eplan.conditions;

import org.jdom2.Element;

/**
 * Interface for condition classes, testing a DOM element (and its child elements) for logical conditions.
 * 
 * @author Stefan Dillmann
 *
 */
public interface Evaluable {
	
	//@ public instance model \locset footprint;
	//@ public instance model \locset footprint_depth;
	
	//@ accessible footprint: footprint;
	//@ accessible footprint_depth: footprint_depth;

	
	//@ accessible \inv: footprint;


	//@ public model instance int depth;	
	//@ accessible depth: footprint_depth;
	//@ public instance invariant depth >= 0;

	/* public model_behavior
	   requires (\forall int i; 0 <= i && i < arr.length; arr[i].depth >= 0);
	   ensures arr.length > 0 ==> ((\forall int y; y >= 0 && y < arr.length; arr[y].depth <= \result) && (\exists int y; y >= 0 && y < arr.length; arr[y].depth == \result));
	   ensures arr.length > 0 ? \result >= 0 : \result == -1;
	   assignable \strictly_nothing;
	   accessible arr,arr[*],(\infinite_union int k; k >= 0 && k < arr.length; arr[k].footprint_depth);
	   model instance helper int maxdepth(Evaluable[] arr);
	  */
	
	/* public model_behavior
	   requires (\forall int i; 0 <= i && i < list.size(); ((Evaluable) list.get(i)).depth >= 0);
	   ensures list.size() > 0 ==> ((\forall int y; y >= 0 && y < list.size(); ((Evaluable) list.get(y)).depth <= \result) && (\exists int y; y >= 0 && y < list.size(); ((Evaluable) list.get(y)).depth == \result));
	   ensures list.size() > 0 ? \result >= 0 : \result == -1;
	   assignable \strictly_nothing;
	   accessible list,(\infinite_union int k; k >= 0 && k < list.size(); ((Evaluable) list.get(k)).footprint_depth);
	   model helper int maxdepth_list(java.util.List list);
	  */
	
	/**
	 * Evaluation function.
	 * 
	 * @param e the DOM element to test
	 * @return true if the condition is fulfilled, otherwise false
	 */
	/*@ public normal_behavior
	  @ requires \invariant_for(e);
	  @ ensures \result == \old(this.evaluate(e));
	  @ ensures \invariant_for(e);
	  @ measured_by depth;
	  @ assignable \nothing;
	  @*/
	boolean evaluate(Element e);

}
