/**
 * 
 */
package eplan;

import org.jdom2.Element;

/**
 * Return value for {@link eplan.PlanProModel#getNextTopKante(Element topKante, boolean forward)}.
 * It holds the DOM element of the resulting edge as well as the following search direction.
 * This is important if the topological direction of the edge changes, e.g. a node B of the first edge is also connected to a node B of the following edge.
 * 
 * @author Stefan Dillmann
 *
 */
public class NextTopKanteResult {
	
	/**
	 * The DOM element of the following TOP_Kante.
	 */
	public Element topKanteElement;
	
	/**
	 * The search direction of the returned edge, to continue searching in the same direction.
	 */
	public boolean direction;
	
	/**
	 * Creates a new result value.
	 * 
	 * @param tke the DOM element of the following TOP_Kante.
	 * @param dir the search direction of the returned node, to continue searching in the same direction.
	 */
	public NextTopKanteResult(Element tke, boolean dir) {
		this.topKanteElement = tke;
		this.direction = dir;
	}
	
	public String toString() {
		String id = topKanteElement.getChild("Identitaet").getChild("Wert").getText();
		return "[" + id + "," + direction + "]";
	}
}