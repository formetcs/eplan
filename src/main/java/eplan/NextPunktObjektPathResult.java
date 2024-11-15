/**
 * 
 */
package eplan;

import java.util.List;

import org.jdom2.Element;

/**
 * Return value for {@link eplan.PlanProModel#getNextPunktObjektPaths(PunktObjekt startpos, String type, boolean forward)}.
 * It holds the DOM element of a found Punkt_Objekt, a list of DOM elements representing the TOP_Kante objects on the route between the starting object and the found object,
 * and the distance between these points.
 * 
 * @author Stefan Dillmann
 *
 */
public class NextPunktObjektPathResult {
	
	/**
	 * A list of DOM elements representing the TOP_Kante objects on the route between the starting Punkt_Objekt and the found Punkt_Objekt.
	 */
	public List topKantenList;
	
	/**
	 * The DOM element of a found Punkt_Objekt.
	 */
	public Element punktObjektElement;
	
	/**
	 * The distance between the starting point and the found Punkt_Objekt in millimeters.
	 */
	public int distance;
	
	/**
	 * Creates a new result value.
	 * 
	 * @param tkl a list of DOM elements representing the TOP_Kante objects on the route between the starting Punkt_Objekt and the found Punkt_Objekt
	 * @param e the DOM element of a found Punkt_Objekt
	 * @param dist the distance between the starting point and the found Punkt_Objekt in millimeters
	 */
	public NextPunktObjektPathResult(List tkl, Element e, int dist) {
		this.topKantenList = tkl;
		this.punktObjektElement = e;
		this.distance = dist;
	}
	
	/**
	 * Convenience function to get the NextPunktObjektPathResult with the lowest distance value from a list of NextPunktObjektPathResult objects.
	 * Note that such a list is the return value of {@link eplan.PlanProModel#getNextPunktObjektPaths(PunktObjekt startpos, String type, boolean forward)},
	 * so this function can be used to get the nearest Punkt_Objekt from all possible paths.
	 * 
	 * @param list a list of NextPunktObjektPathResult objects
	 * @return the NextPunktObjektPathResult with the lowest distance value or null if the list was empty
	 */
	public static NextPunktObjektPathResult nearest(List list) {
		NextPunktObjektPathResult bestPath = null;
		int bestDistance = 999999999;
		for(int i = 0; i < list.size(); i++) {
			NextPunktObjektPathResult currentPath = ((NextPunktObjektPathResult) list.get(i));
			int currentDistance = currentPath.distance;
			if(currentDistance < bestDistance) {
				bestPath = currentPath;
				bestDistance = currentDistance;
			}
		}
		return bestPath;
	}
	
	/**
	 * Convenience function to get the NextPunktObjektPathResult with the highest distance value from a list of NextPunktObjektPathResult objects.
	 * Note that such a list is the return value of {@link eplan.PlanProModel#getNextPunktObjektPaths(PunktObjekt startpos, String type, boolean forward)},
	 * so this function can be used to get the farthest Punkt_Objekt from all possible paths.
	 * 
	 * @param list a list of NextPunktObjektPathResult objects
	 * @return the NextPunktObjektPathResult with the highest distance value or null if the list was empty
	 */
	public static NextPunktObjektPathResult farthest(List list) {
		NextPunktObjektPathResult bestPath = null;
		int bestDistance = 0;
		for(int i = 0; i < list.size(); i++) {
			NextPunktObjektPathResult currentPath = ((NextPunktObjektPathResult) list.get(i));
			int currentDistance = currentPath.distance;
			if(currentDistance > bestDistance) {
				bestPath = currentPath;
				bestDistance = currentDistance;
			}
		}
		return bestPath;
	}
	
	public String toString() {
		StringBuffer tklString = new StringBuffer();
		tklString.append("[");
		if(topKantenList.size() > 0) {
			String id = ((Element) topKantenList.get(0)).getChild("Identitaet").getChild("Wert").getText();
			tklString.append(id);
		}
		for(int i = 1; i < topKantenList.size(); i++) {
			String id = ((Element) topKantenList.get(i)).getChild("Identitaet").getChild("Wert").getText();
			tklString.append(",").append(id);
		}
		tklString.append("]");
		
		String poid = punktObjektElement.getChild("Identitaet").getChild("Wert").getText();
		return "[" + tklString.toString() + "," + poid + "," + distance + "]";
	}
}
