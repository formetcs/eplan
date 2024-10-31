/**
 * 
 */
package eplan;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.net.URL;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 * Representation of the PlanPro data model.
 * 
 * @author Stefan Dillmann
 *
 */
public class PlanProModel {
	
	/**
	 * Enum to describe the relative orientation of two Punkt_Objekt objects.
	 *
	 */
	public enum Direction {
		NOT_CONNECTED, /** No direct connection */
		EQUAL, /** Facing in equal direction */
		OPPOSITE, /** Facing in opposite direction */
		BOTH /** Facing in both directions (at least one object has effective direction "beide") */
	}
	
	/**
	 * The DOM document.
	 */
	private Document doc;
	
	
	/**
	 * Read the DOM tree from an XML file.
	 * 
	 * 
	 * @param filename the name of the file to read from
	 * @throws JDOMException
	 * @throws IOException
	 */
	public void readFile(String filename) throws JDOMException, IOException {
		doc = new SAXBuilder().build(filename);
	}
	
	
	/**
	 * Read the DOM tree from an URL.
	 * 
	 * 
	 * @param url URL to read from
	 * @throws JDOMException
	 * @throws IOException
	 */
	public void readFile(URL url) throws JDOMException, IOException {
		doc = new SAXBuilder().build(url);
	}
	
	
	/**
	 * Write the DOM tree into an XML file.
	 * 
	 * 
	 * @param filename the name of the file to write
	 * @throws IOException
	 */
	public void writeFile(String filename) throws IOException {
		FileWriter fw = new FileWriter(filename);
		XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
		out.output(doc, fw);
	}
	
	
	/**
	 * Get the container element, which is the parent of all PlanPro objects.
	 * 
	 * 
	 * @return the DOM element of the container element
	 */
	public Element getContainerElement() {
		Element rootElem = doc.getRootElement();
		Element containerElem = rootElem.getChild("LST_Planung_Projekt").getChild("LST_Planung_Gruppe").getChild("LST_Planung_Einzel").getChild("LST_Zustand_Ziel").getChild("Container");
		return containerElem;
	}
	
	
	/**
	 * Get the list of all PlanPro objects.
	 * 
	 * 
	 * @return a list of DOM elements, containing all PlanPro objects
	 */
	public List<Element> getPlanProObjectList() {
		return getContainerElement().getChildren();
	}
	
	
	/**
	 * Retrieve the DOM element of a PlanPro object corresponding to the given id.
	 * 
	 * 
	 * @param guid the GUID of the object to search for
	 * @return the corresponding DOM element, or null if no object exists for the given id
	 */
	public Element getElementbyId(String guid) {
		List<Element> objectList = getPlanProObjectList();
		for(int i = 0; i < objectList.size(); i++) {
			Element e = objectList.get(i);
			if(e.getChild("Identitaet").getChild("Wert").getText().equals(guid))
			{
				return e;
			}
		}
		return null;
	}
	
	
	/**
	 * Checks if the given DOM element holds a Punkt_Objekt subtype.
	 * 
	 * 
	 * @param e the DOM element to check
	 * @return true if it is a Punkt_Objekt subtype, otherwise false
	 */
	public static boolean isPunktObjekt(Element e) {
		return (e.getChild("Punkt_Objekt_TOP_Kante") != null);
	}
	
	
	/**
	 * Find the TOP_Kante objects which are directly connected to a given TOP_Kante in a specified search direction.
	 * 
	 * 
	 * @param topKante the DOM element of the TOP_Kante which neighbors should be searched
	 * @param forward if the search direction should be the same as the topological direction (A -> B) of the starting edge.
	 * @return a list of {@link eplan.NextTopKanteResult} objects, containing all connected edges.
	 */
	public List<NextTopKanteResult> getNextTopKante(Element topKante, boolean forward) {
		List<NextTopKanteResult> returnval = new ArrayList<NextTopKanteResult>();
		String srcIdTopKnotenA = topKante.getChild("ID_TOP_Knoten_A").getChild("Wert").getText();
		String srcIdTopKnotenB = topKante.getChild("ID_TOP_Knoten_B").getChild("Wert").getText();
		String srcTopAnschlussA = topKante.getChild("TOP_Kante_Allg").getChild("TOP_Anschluss_A").getChild("Wert").getText();
		String srcTopAnschlussB = topKante.getChild("TOP_Kante_Allg").getChild("TOP_Anschluss_B").getChild("Wert").getText();
		List<Element> objectList = getPlanProObjectList();
		for(int i = 0; i < objectList.size(); i++) {
			Element e = objectList.get(i);
			if (e.getName().equals("TOP_Kante") && e != topKante) {
				String dstIdTopKnotenA = e.getChild("ID_TOP_Knoten_A").getChild("Wert").getText();
				String dstIdTopKnotenB = e.getChild("ID_TOP_Knoten_B").getChild("Wert").getText();
				String dstTopAnschlussA = e.getChild("TOP_Kante_Allg").getChild("TOP_Anschluss_A").getChild("Wert").getText();
				String dstTopAnschlussB = e.getChild("TOP_Kante_Allg").getChild("TOP_Anschluss_B").getChild("Wert").getText();
				
				if (forward && (srcIdTopKnotenB.equals(dstIdTopKnotenA)) && srcTopAnschlussB.equals("Verbindung")
						&& dstTopAnschlussA.equals("Verbindung")) {
					NextTopKanteResult res = new NextTopKanteResult(e, true);
					returnval.add(res);
				} else if (forward && (srcIdTopKnotenB.equals(dstIdTopKnotenA)) && srcTopAnschlussB.equals("Links")
						&& dstTopAnschlussA.equals("Spitze")) {
					NextTopKanteResult res = new NextTopKanteResult(e, true);
					returnval.add(res);
				} else if (forward && (srcIdTopKnotenB.equals(dstIdTopKnotenA)) && srcTopAnschlussB.equals("Rechts")
						&& dstTopAnschlussA.equals("Spitze")) {
					NextTopKanteResult res = new NextTopKanteResult(e, true);
					returnval.add(res);
				} else if (forward && (srcIdTopKnotenB.equals(dstIdTopKnotenA)) && srcTopAnschlussB.equals("Spitze")
						&& dstTopAnschlussA.equals("Links")) {
					NextTopKanteResult res = new NextTopKanteResult(e, true);
					returnval.add(res);
				} else if (forward && (srcIdTopKnotenB.equals(dstIdTopKnotenA)) && srcTopAnschlussB.equals("Spitze")
						&& dstTopAnschlussA.equals("Rechts")) {
					NextTopKanteResult res = new NextTopKanteResult(e, true);
					returnval.add(res);
				}

				else if (forward && (srcIdTopKnotenB.equals(dstIdTopKnotenB)) && srcTopAnschlussB.equals("Verbindung")
						&& dstTopAnschlussB.equals("Verbindung")) {
					NextTopKanteResult res = new NextTopKanteResult(e, false);
					returnval.add(res);
				} else if (forward && (srcIdTopKnotenB.equals(dstIdTopKnotenB)) && srcTopAnschlussB.equals("Links")
						&& dstTopAnschlussB.equals("Spitze")) {
					NextTopKanteResult res = new NextTopKanteResult(e, false);
					returnval.add(res);
				} else if (forward && (srcIdTopKnotenB.equals(dstIdTopKnotenB)) && srcTopAnschlussB.equals("Rechts")
						&& dstTopAnschlussB.equals("Spitze")) {
					NextTopKanteResult res = new NextTopKanteResult(e, false);
					returnval.add(res);
				} else if (forward && (srcIdTopKnotenB.equals(dstIdTopKnotenB)) && srcTopAnschlussB.equals("Spitze")
						&& dstTopAnschlussB.equals("Links")) {
					NextTopKanteResult res = new NextTopKanteResult(e, false);
					returnval.add(res);
				} else if (forward && (srcIdTopKnotenB.equals(dstIdTopKnotenB)) && srcTopAnschlussB.equals("Spitze")
						&& dstTopAnschlussB.equals("Rechts")) {
					NextTopKanteResult res = new NextTopKanteResult(e, false);
					returnval.add(res);
				}

				else if (!forward && (srcIdTopKnotenA.equals(dstIdTopKnotenB)) && srcTopAnschlussA.equals("Verbindung")
						&& dstTopAnschlussB.equals("Verbindung")) {
					NextTopKanteResult res = new NextTopKanteResult(e, false);
					returnval.add(res);
				} else if (!forward && (srcIdTopKnotenA.equals(dstIdTopKnotenB)) && srcTopAnschlussA.equals("Links")
						&& dstTopAnschlussB.equals("Spitze")) {
					NextTopKanteResult res = new NextTopKanteResult(e, false);
					returnval.add(res);
				} else if (!forward && (srcIdTopKnotenA.equals(dstIdTopKnotenB)) && srcTopAnschlussA.equals("Rechts")
						&& dstTopAnschlussB.equals("Spitze")) {
					NextTopKanteResult res = new NextTopKanteResult(e, false);
					returnval.add(res);
				} else if (!forward && (srcIdTopKnotenA.equals(dstIdTopKnotenB)) && srcTopAnschlussA.equals("Spitze")
						&& dstTopAnschlussB.equals("Links")) {
					NextTopKanteResult res = new NextTopKanteResult(e, false);
					returnval.add(res);
				} else if (!forward && (srcIdTopKnotenA.equals(dstIdTopKnotenB)) && srcTopAnschlussA.equals("Spitze")
						&& dstTopAnschlussB.equals("Rechts")) {
					NextTopKanteResult res = new NextTopKanteResult(e, false);
					returnval.add(res);
				}

				else if (!forward && (srcIdTopKnotenA.equals(dstIdTopKnotenA)) && srcTopAnschlussA.equals("Verbindung")
						&& dstTopAnschlussA.equals("Verbindung")) {
					NextTopKanteResult res = new NextTopKanteResult(e, true);
					returnval.add(res);
				} else if (!forward && (srcIdTopKnotenA.equals(dstIdTopKnotenA)) && srcTopAnschlussA.equals("Links")
						&& dstTopAnschlussA.equals("Spitze")) {
					NextTopKanteResult res = new NextTopKanteResult(e, true);
					returnval.add(res);
				} else if (!forward && (srcIdTopKnotenA.equals(dstIdTopKnotenA)) && srcTopAnschlussA.equals("Rechts")
						&& dstTopAnschlussA.equals("Spitze")) {
					NextTopKanteResult res = new NextTopKanteResult(e, true);
					returnval.add(res);
				} else if (!forward && (srcIdTopKnotenA.equals(dstIdTopKnotenA)) && srcTopAnschlussA.equals("Spitze")
						&& dstTopAnschlussA.equals("Links")) {
					NextTopKanteResult res = new NextTopKanteResult(e, true);
					returnval.add(res);
				} else if (!forward && (srcIdTopKnotenA.equals(dstIdTopKnotenA)) && srcTopAnschlussA.equals("Spitze")
						&& dstTopAnschlussA.equals("Rechts")) {
					NextTopKanteResult res = new NextTopKanteResult(e, true);
					returnval.add(res);
				}
			}
		}
		return returnval;
	}
	
	
	/**
	 * Calculates positions where a Punkt_Objekt can be created, using a starting Punkt_Objekt and a distance.
	 * A positive distance value means forward related to the effective direction of the starting Punkt_Objekt, a negative value means backwards
	 * (The effective direction "beide" will be handled like effective direction "in").
	 * If the track branches, the result positions will be calculated for all possible branches.
	 * 
	 * 
	 * @param startpos the starting Punkt_Objekt
	 * @param distance the distance from the starting Punkt_Objekt in millimeters (negative value means reverse direction)
	 * @return a list of {@link eplan.PunktObjekt} objects, containing all positions where a Punkt_Objekt must be created.
	 */
	public List<PunktObjekt> calculatePosition(PunktObjekt startpos, int distance) {
		List<PunktObjekt> returnval = new ArrayList<PunktObjekt>();
		for(PunktObjektTopKante potk : startpos.punktObjektTopKante) {
			String startGuid = potk.idTopKante;
			int startAbstand = potk.abstand;
			String startWirkrichtung = potk.wirkrichtung;
			Element startEdge = getElementbyId(startGuid);
			double tempEdgeLength = Double.parseDouble(startEdge.getChild("TOP_Kante_Allg").getChild("TOP_Laenge").getChild("Wert").getText());
			tempEdgeLength *= 1000.0;
			int edgeLength = (int) tempEdgeLength;
			int newPos = startAbstand;
			if (!startWirkrichtung.equals("gegen")) {
				newPos = startAbstand + distance;
			} else {
				newPos = startAbstand - distance;
			}
	
			if (newPos >= 0 && newPos <= edgeLength) { // new position is on the same edge
				PunktObjekt target = new PunktObjekt(startGuid, newPos, startWirkrichtung);
				returnval.add(target);
			} else {
				int remainingDistance = distance;
				boolean direction = true;
				if (newPos < 0 && !startWirkrichtung.equals("gegen")) { // continue on previous edge
					remainingDistance = distance + startAbstand;
					direction = false;
				} else if (newPos < 0 && startWirkrichtung.equals("gegen")) { // continue on previous edge
					remainingDistance = distance - startAbstand;
					direction = false;
				} else if (newPos > edgeLength && !startWirkrichtung.equals("gegen")) { // continue on following edge
					remainingDistance = distance - (edgeLength - startAbstand);
					direction = true;
				} else if (newPos > edgeLength && startWirkrichtung.equals("gegen")) { // continue on following edge
					remainingDistance = distance + (edgeLength - startAbstand);
					direction = true;
				}
				List<NextTopKanteResult> edgelist = getNextTopKante(startEdge, direction);
				for (int i = 0; i < edgelist.size(); i++) {
					NextTopKanteResult edgeresult = edgelist.get(i);
					Element tka = edgeresult.topKanteElement;
					String tka_id = tka.getChild("Identitaet").getChild("Wert").getText();
					boolean newdir = edgeresult.direction;
					
					int newAbstand = 0;
					String newWirkrichtung = "";
					if (newdir && remainingDistance < 0) {
						newAbstand = 0;
						newWirkrichtung = "gegen";
					} else if (newdir && remainingDistance >= 0) {
						newAbstand = 0;
						newWirkrichtung = "in";
					} else if (!newdir && remainingDistance < 0) {
						double tka_len = Double.parseDouble(tka.getChild("TOP_Kante_Allg").getChild("TOP_Laenge").getChild("Wert").getText());
						tka_len *= 1000.0;
						newAbstand = (int) tka_len;
						newWirkrichtung = "in";
					} else if (!newdir && remainingDistance >= 0) {
						double tka_len = Double.parseDouble(tka.getChild("TOP_Kante_Allg").getChild("TOP_Laenge").getChild("Wert").getText());
						tka_len *= 1000.0;
						newAbstand = (int) tka_len;
						newWirkrichtung = "gegen";
					}
					PunktObjekt newStart = new PunktObjekt(tka_id, newAbstand, newWirkrichtung);
					List<PunktObjekt> resultlist = calculatePosition(newStart, remainingDistance);
					returnval.addAll(resultlist);
				}
			}
		}

		return returnval;
	}
	

	/**
	 * Calculates a position where a Punkt_Objekt can be created, using a starting Punkt_Objekt and a distance.
	 * The resulting position will be calculated only for a given path of TOP_Kante objects and does not consider branches.
	 * The distance value must be positive, and the topKantenList must contain all TOP_Kante objects in the order they are traversed,
	 * beginning with the TOP_Kante where the starting point is located.
	 * If the forward value is true, the search follows the effective direction of the starting Punkt_Objekt, otherwise it searches backwards
	 * (The effective direction "beide" will be handled like effective direction "in").
	 * 
	 * @param startpos the starting Punkt_Objekt
	 * @param topKantenList a list of DOM elements of the TOP_Kante objects, defining the search path
	 * @param distance the distance from the starting Punkt_Objekt in millimeters (must be positive!)
	 * @param forward if the search direction should be the same as the effective direction of the starting Punkt_Objekt
	 * @return a {@link eplan.PunktObjekt} object, containing the position where the Punkt_Objekt must be created
	 */
	public PunktObjekt calculatePositionOnPath(PunktObjekt startpos, List<Element> topKantenList, int distance, boolean forward) {
		PunktObjekt returnval = null;
		for(PunktObjektTopKante potk : startpos.punktObjektTopKante) {
			String startGuid = potk.idTopKante;
			int startAbstand = potk.abstand;
			String startWirkrichtung = potk.wirkrichtung;
			Element startEdge = getElementbyId(startGuid);
			if (startEdge != null && !topKantenList.isEmpty() && startEdge == topKantenList.get(0)) {
				double tempEdgeLength = Double.parseDouble(startEdge.getChild("TOP_Kante_Allg").getChild("TOP_Laenge").getChild("Wert").getText());
				tempEdgeLength *= 1000.0;
				int edgeLength = (int) tempEdgeLength;
				int newPos = startAbstand;
				if (!startWirkrichtung.equals("gegen") && forward) {
					newPos = startAbstand + distance;
				} else if (!startWirkrichtung.equals("gegen") && !forward) {
					newPos = startAbstand - distance;
				} else if (startWirkrichtung.equals("gegen") && forward) {
					newPos = startAbstand - distance;
				} else if (startWirkrichtung.equals("gegen") && !forward) {
					newPos = startAbstand + distance;
				}
	
				if (newPos >= 0 && newPos <= edgeLength) { // new position is on the same edge
					PunktObjekt target = new PunktObjekt(startGuid, newPos, startWirkrichtung);
					returnval = target;
				} else if (topKantenList.size() > 1) {
					Element nextEdge = topKantenList.get(1);
					String nextEdgeId = nextEdge.getChild("Identitaet").getChild("Wert").getText();
					double tempNextEdgeLength = Double.parseDouble(nextEdge.getChild("TOP_Kante_Allg").getChild("TOP_Laenge").getChild("Wert").getText());
					tempNextEdgeLength *= 1000.0;
					int nextEdgeLength = (int) tempNextEdgeLength;
					String src_knoten_a = startEdge.getChild("ID_TOP_Knoten_A").getChild("Wert").getText();
					String src_knoten_b = startEdge.getChild("ID_TOP_Knoten_B").getChild("Wert").getText();
					String dst_knoten_a = nextEdge.getChild("ID_TOP_Knoten_A").getChild("Wert").getText();
					String dst_knoten_b = nextEdge.getChild("ID_TOP_Knoten_B").getChild("Wert").getText();
					List<Element> sublist = topKantenList.subList(1, topKantenList.size());
					if (!startWirkrichtung.equals("gegen") && forward && src_knoten_b.equals(dst_knoten_a)) {
						int remainingDistance = distance - (edgeLength - startAbstand);
						PunktObjekt newStart = new PunktObjekt(nextEdgeId, 0, "in");
						returnval = calculatePositionOnPath(newStart, sublist, remainingDistance, true);
					} else if (!startWirkrichtung.equals("gegen") && forward && src_knoten_b.equals(dst_knoten_b)) {
						int remainingDistance = distance - (edgeLength - startAbstand);
						PunktObjekt newStart = new PunktObjekt(nextEdgeId, nextEdgeLength, "gegen");
						returnval = calculatePositionOnPath(newStart, sublist, remainingDistance, true);
					} else if (!startWirkrichtung.equals("gegen") && !forward && src_knoten_a.equals(dst_knoten_b)) {
						int remainingDistance = distance - startAbstand;
						PunktObjekt newStart = new PunktObjekt(nextEdgeId, nextEdgeLength, "in");
						returnval = calculatePositionOnPath(newStart, sublist, remainingDistance, false);
					} else if (!startWirkrichtung.equals("gegen") && !forward && src_knoten_a.equals(dst_knoten_a)) {
						int remainingDistance = distance - startAbstand;
						PunktObjekt newStart = new PunktObjekt(nextEdgeId, 0, "gegen");
						returnval = calculatePositionOnPath(newStart, sublist, remainingDistance, false);
					} else if (startWirkrichtung.equals("gegen") && forward && src_knoten_a.equals(dst_knoten_b)) {
						int remainingDistance = distance - startAbstand;
						PunktObjekt newStart = new PunktObjekt(nextEdgeId, nextEdgeLength, "gegen");
						returnval = calculatePositionOnPath(newStart, sublist, remainingDistance, true);
					} else if (startWirkrichtung.equals("gegen") && forward && src_knoten_a.equals(dst_knoten_a)) {
						int remainingDistance = distance - startAbstand;
						PunktObjekt newStart = new PunktObjekt(nextEdgeId, 0, "in");
						returnval = calculatePositionOnPath(newStart, sublist, remainingDistance, true);
					} else if (startWirkrichtung.equals("gegen") && !forward && src_knoten_b.equals(dst_knoten_a)) {
						int remainingDistance = distance - (edgeLength - startAbstand);
						PunktObjekt newStart = new PunktObjekt(nextEdgeId, 0, "gegen");
						returnval = calculatePositionOnPath(newStart, sublist, remainingDistance, false);
					} else if (startWirkrichtung.equals("gegen") && !forward && src_knoten_b.equals(dst_knoten_b)) {
						int remainingDistance = distance - (edgeLength - startAbstand);
						PunktObjekt newStart = new PunktObjekt(nextEdgeId, nextEdgeLength, "in");
						returnval = calculatePositionOnPath(newStart, sublist, remainingDistance, false);
					}
				}
			}
		
		
		}

		return returnval;
	}
	
	
	/**
	 * Calculates the distance between two Punkt_Objekt objects.
	 * The search is performed both in forward and reverse direction (related to startpos)
	 * and also multiple Punkt_Objekt_TOP_Kante attribute groups in both startpos and endpos are considered.
	 * 
	 * @param startpos the starting Punkt_Objekt
	 * @param endpos the ending Punkt_Objekt
	 * @return the (positive) distance between the objects in millimeters, or -1 if endpos is not reachable
	 */
	public int calculateDistance(PunktObjekt startpos, PunktObjekt endpos) {
		String identitaetStart = startpos.identitaet;
		for(PunktObjektTopKante potkStart : startpos.punktObjektTopKante) {
			String idTopKanteStart = potkStart.idTopKante;
			int abstandStart = potkStart.abstand;
			String wirkrichtungStart = potkStart.wirkrichtung;
			
			String identitaetEnd = endpos.identitaet;
			for(PunktObjektTopKante potkEnd : endpos.punktObjektTopKante) {
				String idTopKanteEnd = potkEnd.idTopKante;
				int abstandEnd = potkEnd.abstand;
				String wirkrichtungEnd = potkEnd.wirkrichtung;
				
				PunktObjekt poStart = new PunktObjekt(identitaetStart, idTopKanteStart, abstandStart, wirkrichtungStart);
				PunktObjekt poEnd = new PunktObjekt(identitaetEnd, idTopKanteEnd, abstandEnd, wirkrichtungEnd);
				
				int result = calculateDistance(poStart, poEnd, true);
				if(result >= 0) {
					return result;
				}
				result = calculateDistance(poStart, poEnd, false);
				if(result >= 0) {
					return result;
				}
			}
		}
		
		return -1;
	}
	

	/**
	 * Calculates the distance between two Punkt_Objekt objects, using a specified search direction.
	 * If the forward value is true, the search follows the effective direction of the starting Punkt_Objekt, otherwise it searches backwards
	 * (The effective direction "beide" will be handled like effective direction "in").
	 * 
	 * @param startpos the starting Punkt_Objekt
	 * @param endpos the ending Punkt_Objekt
	 * @param forward if the search direction should be the same as the effective direction of the starting Punkt_Objekt
	 * @return the (positive) distance between the objects in millimeters, or -1 if endpos is not reachable
	 */
	private int calculateDistance(PunktObjekt startpos, PunktObjekt endpos, boolean forward) {
		int returnval = -1;
		PunktObjektTopKante potkStart = startpos.punktObjektTopKante[0];
		PunktObjektTopKante potkEnd = endpos.punktObjektTopKante[0];
		String startGuid = potkStart.idTopKante;
		int startAbstand = potkStart.abstand;
		String startWirkrichtung = potkStart.wirkrichtung;
		String endGuid = potkEnd.idTopKante;
		int endAbstand = potkEnd.abstand;
		//String endWirkrichtung = potkEnd.wirkrichtung;
		if (startGuid.equals(endGuid) && !startWirkrichtung.equals("gegen") && forward) { // both positions are on the
																							// same edge
			returnval = endAbstand - startAbstand;
		} else if (startGuid.equals(endGuid) && !startWirkrichtung.equals("gegen") && !forward) { // both positions are
																									// on the same edge
			returnval = startAbstand - endAbstand;
		} else if (startGuid.equals(endGuid) && startWirkrichtung.equals("gegen") && !forward) { // both positions are
																									// on the same edge
			returnval = endAbstand - startAbstand;
		} else if (startGuid.equals(endGuid) && startWirkrichtung.equals("gegen") && forward) { // both positions are on
																								// the same edge
			returnval = startAbstand - endAbstand;
		} else {
			Element startEdge = getElementbyId(startGuid);
			if (startEdge != null) {
				int currentDistance = -1;
				int remainingDistance = -1;
				boolean direction = true;
				if (!startWirkrichtung.equals("gegen") && forward) {
					double tempEdgeLength = Double.parseDouble(startEdge.getChild("TOP_Kante_Allg").getChild("TOP_Laenge").getChild("Wert").getText());
					tempEdgeLength *= 1000.0;
					int edgeLength = (int) tempEdgeLength;
					currentDistance = edgeLength - startAbstand;
					direction = true;
				} else if (!startWirkrichtung.equals("gegen") && !forward) {
					currentDistance = startAbstand;
					direction = false;
				} else if (startWirkrichtung.equals("gegen") && forward) {
					currentDistance = startAbstand;
					direction = false;
				} else if (startWirkrichtung.equals("gegen") && !forward) {
					double tempEdgeLength = Double.parseDouble(startEdge.getChild("TOP_Kante_Allg").getChild("TOP_Laenge").getChild("Wert").getText());
					tempEdgeLength *= 1000.0;
					int edgeLength = (int) tempEdgeLength;
					currentDistance = edgeLength - startAbstand;
					direction = true;
				}
				List<NextTopKanteResult> edgelist = getNextTopKante(startEdge, direction);
				for (int i = 0; i < edgelist.size(); i++) {
					NextTopKanteResult edgeresult = edgelist.get(i);
					Element tka = edgeresult.topKanteElement;
					String tka_id = tka.getChild("Identitaet").getChild("Wert").getText();
					boolean newdir = edgeresult.direction;
					int newAbstand = 0;
					if (newdir) {
						newAbstand = 0;
					} else {
						double tka_len = Double.parseDouble(tka.getChild("TOP_Kante_Allg").getChild("TOP_Laenge").getChild("Wert").getText());
						tka_len *= 1000.0;
						newAbstand = (int) tka_len;
					}
					PunktObjekt newStart = new PunktObjekt(tka_id, newAbstand, "in");
					int tempdist = calculateDistance(newStart, endpos, newdir);
					if ((remainingDistance < 0 && tempdist >= 0)
							|| (remainingDistance >= 0 && tempdist >= 0 && tempdist < remainingDistance)) {
						remainingDistance = tempdist;
					}
				}
				if (remainingDistance >= 0) {
					returnval = currentDistance + remainingDistance;
				}
			}
		}

		return returnval;
	}
	
	
	/**
	 * Calculates the relative orientation of two Punkt_Objekt objects.
	 * The search is performed both in forward and reverse direction (related to startpos)
	 * and also multiple Punkt_Objekt_TOP_Kante attribute groups in both startpos and endpos are considered.
	 * 
	 * @param startpos the starting Punkt_Objekt
	 * @param endpos the ending Punkt_Objekt
	 * @return a {@link eplan.PlanProModel.Direction} object, describing the relative orientation of the objects
	 */
	public Direction checkDirection(PunktObjekt startpos, PunktObjekt endpos) {
		String identitaetStart = startpos.identitaet;
		for(PunktObjektTopKante potkStart : startpos.punktObjektTopKante) {
			String idTopKanteStart = potkStart.idTopKante;
			int abstandStart = potkStart.abstand;
			String wirkrichtungStart = potkStart.wirkrichtung;
			
			String identitaetEnd = endpos.identitaet;
			for(PunktObjektTopKante potkEnd : endpos.punktObjektTopKante) {
				String idTopKanteEnd = potkEnd.idTopKante;
				int abstandEnd = potkEnd.abstand;
				String wirkrichtungEnd = potkEnd.wirkrichtung;
				
				PunktObjekt poStart = new PunktObjekt(identitaetStart, idTopKanteStart, abstandStart, wirkrichtungStart);
				PunktObjekt poEnd = new PunktObjekt(identitaetEnd, idTopKanteEnd, abstandEnd, wirkrichtungEnd);
				
				Direction result = checkDirection(poStart, poEnd, true);
				if(result != Direction.NOT_CONNECTED) {
					return result;
				}
				result = checkDirection(poStart, poEnd, false);
				if(result != Direction.NOT_CONNECTED) {
					return result;
				}
			}
		}
		
		return Direction.NOT_CONNECTED;
	}
	
	
	/**
	 * Calculates the relative orientation of two Punkt_Objekt objects, using a specified search direction.
	 * If the forward value is true, the search follows the effective direction of the starting Punkt_Objekt, otherwise it searches backwards
	 * (The effective direction "beide" will be handled like effective direction "in").
	 * 
	 * @param startpos the starting Punkt_Objekt
	 * @param endpos the ending Punkt_Objekt
	 * @param forward if the search direction should be the same as the effective direction of the starting Punkt_Objekt
	 * @return a {@link eplan.PlanProModel.Direction} object, describing the relative orientation of the objects
	 */
	private Direction checkDirection(PunktObjekt startpos, PunktObjekt endpos, boolean forward) {
		Direction returnval = Direction.NOT_CONNECTED;
		PunktObjektTopKante potkStart = startpos.punktObjektTopKante[0];
		PunktObjektTopKante potkEnd = endpos.punktObjektTopKante[0];
		String startGuid = potkStart.idTopKante;
		String startWirkrichtung = potkStart.wirkrichtung;
		String endGuid = potkEnd.idTopKante;
		String endWirkrichtung = potkEnd.wirkrichtung;
		if (startGuid.equals(endGuid) && startWirkrichtung.equals("in") && endWirkrichtung.equals("in")) { // both positions are on the
																							// same edge
			returnval = Direction.EQUAL;
		} else if (startGuid.equals(endGuid) && startWirkrichtung.equals("gegen") && endWirkrichtung.equals("gegen")) { // both positions are
																									// on the same edge
			returnval = Direction.EQUAL;
		} else if (startGuid.equals(endGuid) && startWirkrichtung.equals("in") && endWirkrichtung.equals("gegen")) { // both positions are
																									// on the same edge
			returnval = Direction.OPPOSITE;
		} else if (startGuid.equals(endGuid) && startWirkrichtung.equals("gegen") && endWirkrichtung.equals("in")) { // both positions are on
																								// the same edge
			returnval = Direction.OPPOSITE;
		} else if (startGuid.equals(endGuid)) { // both positions are on the same edge, at least one direction is "beide"
			returnval = Direction.BOTH;
		}
		else {
			Element startEdge = getElementbyId(startGuid);
			if (startEdge != null) {
				boolean direction = true;
				if (!startWirkrichtung.equals("gegen") && forward) {
					direction = true;
				} else if (!startWirkrichtung.equals("gegen") && !forward) {
					direction = false;
				} else if (startWirkrichtung.equals("gegen") && forward) {
					direction = false;
				} else if (startWirkrichtung.equals("gegen") && !forward) {
					direction = true;
				}
				List<NextTopKanteResult> edgelist = getNextTopKante(startEdge, direction);
				for (int i = 0; i < edgelist.size(); i++) {
					NextTopKanteResult edgeresult = edgelist.get(i);
					Element tka = edgeresult.topKanteElement;
					String tka_id = tka.getChild("Identitaet").getChild("Wert").getText();
					boolean newdir = edgeresult.direction;
					String newWirkrichtung = "in";
					if (direction && newdir || !direction && !newdir) {
						newWirkrichtung = startWirkrichtung;
					} else {
						if (startWirkrichtung.equals("in")) {
							newWirkrichtung = "gegen";
						}
						else if (startWirkrichtung.equals("gegen")) {
							newWirkrichtung = "in";
						}
						else {
							newWirkrichtung = "beide";
						}
					}
					PunktObjekt newStart = new PunktObjekt(tka_id, 0, newWirkrichtung);
					Direction tempdir = checkDirection(newStart, endpos, forward);
					if (tempdir != Direction.NOT_CONNECTED) {
						returnval = tempdir;
						break;
					}
				}
			}
		}

		return returnval;
	}

	
	/**
	 * Finds the Punkt_Objekt of a given type and arbitrary effective direction, which immediately follows a given Punkt_Objekt in a specified search direction.
	 * The type argument must be the PlanPro type name of a concrete Punkt_Objekt subtype (e.g. "Signal", "Datenpunkt").
	 * If the type argument is an empty string, the search will cover any Punkt_Objekt subtype.
	 * If the forward value is true, the search follows the effective direction of the starting Punkt_Objekt, otherwise it searches backwards
	 * (The effective direction "beide" will be handled like effective direction "in").
	 * If the track branches, the element with the shortest distance is returned.
	 * 
	 * 
	 * @param startpos the PunktObjekt where the search starts
	 * @param type the PlanPro type name of a concrete Punkt_Objekt subtype (e.g. "Signal", "Datenpunkt"), or an empty string to cover any Punkt_Objekt
	 * @param forward if the search direction should be the same as the effective direction of the starting Punkt_Objekt
	 * @return the DOM element of the Punkt_Objekt with the shortest distance from the starting Punkt_Objekt, or null of no such object exists
	 */
	public Element getNextPunktObjekt(PunktObjekt startpos, String type, boolean forward) {
		Evaluable cond;
		if(type.isEmpty()) {
			cond = new ConstantCondition(true);
		}
		else {
			cond = new TypeCondition(type);
		}
		List<NextPunktObjektPathResult> resultlist = getNextPunktObjektPaths(startpos, cond, Direction.BOTH, forward);
		NextPunktObjektPathResult resultval = NextPunktObjektPathResult.nearest(resultlist);
		if(resultval != null ) {
			return resultval.punktObjektElement;
		}
		return null;
	}
	
	
	/**
	 * Finds the Punkt_Objekt of a given type and arbitrary effective direction, which immediately follows a given Punkt_Objekt in a specified search direction.
	 * The type argument must be the PlanPro type name of a concrete Punkt_Objekt subtype (e.g. "Signal", "Datenpunkt").
	 * If the type argument is an empty string, the search will cover any Punkt_Objekt subtype.
	 * If the forward value is true, the search follows the effective direction of the starting Punkt_Objekt, otherwise it searches backwards
	 * (The effective direction "beide" will be handled like effective direction "in").
	 * If the track branches, all directly reachable elements and their path information are returned.
	 * 
	 * @param startpos the starting Punkt_Objekt
	 * @param type the PlanPro type name of a concrete Punkt_Objekt subtype (e.g. "Signal", "Datenpunkt"), or an empty string to cover any Punkt_Objekt
	 * @param forward if the search direction should be the same as the effective direction of the starting Punkt_Objekt
	 * @return a list of {@link eplan.NextPunktObjektPathResult} objects, containing element and path information
	 */
	public List<NextPunktObjektPathResult> getNextPunktObjektPaths(PunktObjekt startpos, String type, boolean forward) {
		Evaluable cond;
		if(type.isEmpty()) {
			cond = new ConstantCondition(true);
		}
		else {
			cond = new TypeCondition(type);
		}
		return getNextPunktObjektPaths(startpos, cond, Direction.BOTH, forward);
		
	}
	
	
	/**
	 * Finds the Punkt_Objekt which immediately follows a given Punkt_Objekt in a specified search direction.
	 * The condition argument must be an object implementing the {@link eplan.Evaluable} interface.
	 * It defines conditions the searched object must satisfy.
	 * The orientation argument describes the relative orientation the found object and the starting object should have.
	 * If the forward value is true, the search follows the effective direction of the starting Punkt_Objekt, otherwise it searches backwards
	 * (The effective direction "beide" will be handled like effective direction "in").
	 * If the track branches, all directly reachable elements and their path information are returned.
	 * 
	 * @param startpos the starting Punkt_Objekt
	 * @param condition an object implementing {@link eplan.Evaluable}, restricting the search to these conditions
	 * @param orientation a {@link eplan.PlanProModel.Direction} enum value, describing the relative orientation of the objects
	 * @param forward if the search direction should be the same as the effective direction of the starting Punkt_Objekt
	 * @return a list of {@link eplan.NextPunktObjektPathResult} objects, containing element and path information
	 */
	public List<NextPunktObjektPathResult> getNextPunktObjektPaths(PunktObjekt startpos, Evaluable condition, Direction orientation, boolean forward) {
		List<NextPunktObjektPathResult> returnval = new ArrayList<NextPunktObjektPathResult>();
		if(orientation == Direction.NOT_CONNECTED) {
			return returnval;
		}
		for(PunktObjektTopKante potk : startpos.punktObjektTopKante) {
			int minimumDistance = -1;
			Element minimumPunktObj = null;
			String startIdTopKante = potk.idTopKante;
			int startAbstand = potk.abstand;
			String startWirkrichtung = potk.wirkrichtung;
			Element startElement = getElementbyId(startpos.identitaet);
			List<Element> objectList = getPlanProObjectList();
			for (int i = 0; i < objectList.size(); i++) {
				Element temp = objectList.get(i);
				if (temp != startElement && isPunktObjekt(temp)) {
					List<Element> topKantenList = temp.getChildren("Punkt_Objekt_TOP_Kante");
					for(Element topKantenElement : topKantenList) {
						String tempIdTopKante = topKantenElement.getChild("ID_TOP_Kante").getChild("Wert").getText();
						if(!startIdTopKante.equals(tempIdTopKante)) {
							continue;
						}
						if(!condition.evaluate(temp)) {
							continue;
						}
						double doubleTempAbstand = Double.parseDouble(topKantenElement.getChild("Abstand").getChild("Wert").getText());
						doubleTempAbstand *= 1000.0;
						int tempAbstand = (int) doubleTempAbstand;
						String tempWirkrichtung = "beide";
						if(topKantenElement.getChild("Wirkrichtung") != null) {
							tempWirkrichtung = topKantenElement.getChild("Wirkrichtung").getChild("Wert").getText();
						}
						if(orientation == Direction.EQUAL && startWirkrichtung.equals("in") && tempWirkrichtung.equals("gegen") ||
								orientation == Direction.EQUAL && startWirkrichtung.equals("gegen") && tempWirkrichtung.equals("in") ||
								orientation == Direction.OPPOSITE && startWirkrichtung.equals("in") && tempWirkrichtung.equals("in") ||
								orientation == Direction.OPPOSITE && startWirkrichtung.equals("gegen") && tempWirkrichtung.equals("gegen")) {
							continue;
						}
						int dist = -1;
						if (!startWirkrichtung.equals("gegen") && forward) {
							dist = tempAbstand - startAbstand;
						} else if (!startWirkrichtung.equals("gegen") && !forward) {
							dist = startAbstand - tempAbstand;
						} else if (startWirkrichtung.equals("gegen") && !forward) {
							dist = tempAbstand - startAbstand;
						} else if (startWirkrichtung.equals("gegen") && forward) {
							dist = startAbstand - tempAbstand;
						}
		
						if (dist >= 0 && (minimumDistance < 0 || dist < minimumDistance)) {
							minimumDistance = dist;
							minimumPunktObj = temp;
						}
					}
				}
			}
	
			Element tempEdge = getElementbyId(startIdTopKante);
			double tempEdgeLength = Double.parseDouble(tempEdge.getChild("TOP_Kante_Allg").getChild("TOP_Laenge").getChild("Wert").getText());
			tempEdgeLength *= 1000.0;
			int edgeLength = (int) tempEdgeLength;;
	
			if (minimumDistance >= 0) { // Punkt_Objekt found on the same edge
				List<Element> klist = new ArrayList<Element>();
				klist.add(tempEdge);
				NextPunktObjektPathResult res = new NextPunktObjektPathResult(klist, minimumPunktObj, minimumDistance);
				returnval.add(res);
			} else { // continue searching on the next edge(s)
				boolean searchdirection = true;
				if (!startWirkrichtung.equals("gegen") && forward) {
					searchdirection = true;
				} else if (!startWirkrichtung.equals("gegen") && !forward) {
					searchdirection = false;
				} else if (startWirkrichtung.equals("gegen") && forward) {
					searchdirection = false;
				} else if (startWirkrichtung.equals("gegen") && !forward) {
					searchdirection = true;
				}
				List<NextTopKanteResult> edgelist = getNextTopKante(tempEdge, searchdirection);
				for (int i = 0; i < edgelist.size(); i++) {
					NextTopKanteResult temppair = edgelist.get(i);
					Element newKante = temppair.topKanteElement;
					boolean newDirection = temppair.direction;
					String newKanteId = newKante.getChild("Identitaet").getChild("Wert").getText();
					double tempNewKanteLength = Double.parseDouble(newKante.getChild("TOP_Kante_Allg").getChild("TOP_Laenge").getChild("Wert").getText());
					tempNewKanteLength *= 1000.0;
					int newKanteLength = (int) tempNewKanteLength;
	
					if (searchdirection && newDirection) {
						String finalWirkrichtung = "beide";
						boolean finalForward = true;
						if(startWirkrichtung.equals("in")) {
							finalWirkrichtung = "in";
							finalForward = true;
						}
						if(startWirkrichtung.equals("gegen")) {
							finalWirkrichtung = "gegen";
							finalForward = false;
						}
						PunktObjekt newStartPunktObjekt = new PunktObjekt(newKanteId, 0, finalWirkrichtung);
						List<NextPunktObjektPathResult> tempresultlist = getNextPunktObjektPaths(newStartPunktObjekt, condition, orientation, finalForward);
						for (int j = 0; j < tempresultlist.size(); j++) {
							NextPunktObjektPathResult tempresult = tempresultlist.get(j);
							List<Element> trKantenList = tempresult.topKantenList;
							Element trPoElem = tempresult.punktObjektElement;
							int trDist = tempresult.distance;
							int additionalDistance = edgeLength - startAbstand;
							trKantenList.add(0, tempEdge);
							trDist = trDist + additionalDistance;
							tempresult = new NextPunktObjektPathResult(trKantenList, trPoElem, trDist);
							returnval.add(tempresult);
						}
					} else if (searchdirection && !newDirection) {
						String finalWirkrichtung = "beide";
						boolean finalForward = false;
						if(startWirkrichtung.equals("in")) {
							finalWirkrichtung = "gegen";
							finalForward = true;
						}
						if(startWirkrichtung.equals("gegen")) {
							finalWirkrichtung = "in";
							finalForward = false;
						}
						PunktObjekt newStartPunktObjekt = new PunktObjekt(newKanteId, newKanteLength, finalWirkrichtung);
						List<NextPunktObjektPathResult> tempresultlist = getNextPunktObjektPaths(newStartPunktObjekt, condition, orientation, finalForward);
						for (int j = 0; j < tempresultlist.size(); j++) {
							NextPunktObjektPathResult tempresult = tempresultlist.get(j);
							List<Element> trKantenList = tempresult.topKantenList;
							Element trPoElem = tempresult.punktObjektElement;
							int trDist = tempresult.distance;
							int additionalDistance = edgeLength - startAbstand;
							trKantenList.add(0, tempEdge);
							trDist = trDist + additionalDistance;
							tempresult = new NextPunktObjektPathResult(trKantenList, trPoElem, trDist);
							returnval.add(tempresult);
						}
					} else if (!searchdirection && newDirection) {
						String finalWirkrichtung = "beide";
						boolean finalForward = true;
						if(startWirkrichtung.equals("in")) {
							finalWirkrichtung = "gegen";
							finalForward = false;
						}
						if(startWirkrichtung.equals("gegen")) {
							finalWirkrichtung = "in";
							finalForward = true;
						}
						PunktObjekt newStartPunktObjekt = new PunktObjekt(newKanteId, 0, finalWirkrichtung);
						List<NextPunktObjektPathResult> tempresultlist = getNextPunktObjektPaths(newStartPunktObjekt, condition, orientation, finalForward);
						for (int j = 0; j < tempresultlist.size(); j++) {
							NextPunktObjektPathResult tempresult = tempresultlist.get(j);
							List<Element> trKantenList = tempresult.topKantenList;
							Element trPoElem = tempresult.punktObjektElement;
							int trDist = tempresult.distance;
							int additionalDistance = startAbstand;
							trKantenList.add(0, tempEdge);
							trDist = trDist + additionalDistance;
							tempresult = new NextPunktObjektPathResult(trKantenList, trPoElem, trDist);
							returnval.add(tempresult);
						}
					} else if (!searchdirection && !newDirection) {
						String finalWirkrichtung = "beide";
						boolean finalForward = false;
						if(startWirkrichtung.equals("in")) {
							finalWirkrichtung = "in";
							finalForward = false;
						}
						if(startWirkrichtung.equals("gegen")) {
							finalWirkrichtung = "gegen";
							finalForward = true;
						}
						PunktObjekt newStartPunktObjekt = new PunktObjekt(newKanteId, newKanteLength, finalWirkrichtung);
						List<NextPunktObjektPathResult> tempresultlist = getNextPunktObjektPaths(newStartPunktObjekt, condition, orientation, finalForward);
						for (int j = 0; j < tempresultlist.size(); j++) {
							NextPunktObjektPathResult tempresult = tempresultlist.get(j);
							List<Element> trKantenList = tempresult.topKantenList;
							Element trPoElem = tempresult.punktObjektElement;
							int trDist = tempresult.distance;
							int additionalDistance = startAbstand;
							trKantenList.add(0, tempEdge);
							trDist = trDist + additionalDistance;
							tempresult = new NextPunktObjektPathResult(trKantenList, trPoElem, trDist);
							returnval.add(tempresult);
						}
					}
				}
			}
		}
		return returnval;
	}
	
	
	/**
	 * Prints out the XML representation of the DOM tree.
	 * 
	 * @return the XML representation of the DOM tree
	 */
	public String toString() {
		XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
		return out.outputString(doc);
	}
	

}
