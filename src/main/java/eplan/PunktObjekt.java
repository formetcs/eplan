/**
 * 
 */
package eplan;

import java.util.List;

import org.jdom2.Element;

/**
 * Object containing all position information of a PlanPro Punkt_Objekt.
 * It can act as a wrapper for the DOM representation of a Punkt_Objekt, especially if
 * no concrete object is existing and only the position is needed
 * (e.g. for temporal objects or if the concrete object is created in a later step).
 * 
 * @author Stefan Dillmann
 *
 */
public class PunktObjekt {
	
	/**
	 * The identity (GUID) of the Punkt_Objekt.
	 */
	public String identitaet;
	
	/**
	 * Contains the list of Punkt_Objekt_TOP_Kante attribute groups.
	 */
	public PunktObjektTopKante[] punktObjektTopKante;
	
	/**
	 * Creates a new empty PunktObjekt.
	 */
	public PunktObjekt() {
		this.punktObjektTopKante = null;
		this.identitaet = null;
	}
	
	/**
	 * Creates a new PunktObjekt and copies the contents from another PunktObjekt.
	 * 
	 * @param o the PunktObjekt which should be copied.
	 */
	public PunktObjekt(PunktObjekt o) {
		int potkCount = o.punktObjektTopKante.length;
		this.punktObjektTopKante = new PunktObjektTopKante[potkCount];
		this.identitaet = o.identitaet;
		for(int i = 0; i < potkCount; i++) {
			this.punktObjektTopKante[i] = new PunktObjektTopKante();
			this.punktObjektTopKante[i].idTopKante = o.punktObjektTopKante[i].idTopKante;
			this.punktObjektTopKante[i].abstand = o.punktObjektTopKante[i].abstand;
			this.punktObjektTopKante[i].wirkrichtung = o.punktObjektTopKante[i].wirkrichtung;
			this.punktObjektTopKante[i].seitlicherAbstand = o.punktObjektTopKante[i].seitlicherAbstand;
			this.punktObjektTopKante[i].seitlicheLage = o.punktObjektTopKante[i].seitlicheLage;
		}
	}
	
	/**
	 * Creates a new PunktObjekt, referencing only one PlanPro TOP_Kante.
	 * 
	 * @param idtk the identity of the related PlanPro TOP_Kante.
	 * @param abst the distance from node A of the related PlanPro TOP_Kante in millimeters.
	 * @param wirkr the effective direction of the PlanPro Punkt_Objekt, related to the topological direction (A -> B) of the related PlanPro TOP_Kante.
	 */
	public PunktObjekt(String idtk, int abst, String wirkr) {
		this(null, idtk, abst, wirkr);
	}
	
	/**
	 * Creates a new PunktObjekt, referencing only one PlanPro TOP_Kante.
	 * 
	 * @param guid the identity (GUID) of the Punkt_Objekt.
	 * @param idtk the identity of the related PlanPro TOP_Kante.
	 * @param abst the distance from node A of the related PlanPro TOP_Kante in millimeters.
	 * @param wirkr the effective direction of the PlanPro Punkt_Objekt, related to the topological direction (A -> B) of the related PlanPro TOP_Kante.
	 */
	public PunktObjekt(String guid, String idtk, int abst, String wirkr) {
		this.punktObjektTopKante = new PunktObjektTopKante[1];
		this.identitaet = guid;
		this.punktObjektTopKante[0] = new PunktObjektTopKante();
		this.punktObjektTopKante[0].idTopKante = idtk;
		this.punktObjektTopKante[0].abstand = abst;
		this.punktObjektTopKante[0].wirkrichtung = wirkr;
		this.punktObjektTopKante[0].seitlicherAbstand = 0;
		this.punktObjektTopKante[0].seitlicheLage = null;
	}
	
	/**
	 * Creates a new PunktObjekt from a DOM element describing a PlanPro Punkt_Objekt.
	 * A missing value of Wirkrichtung is interpreted as "beide".
	 * 
	 * @param e the DOM element describing a PlanPro Punkt_Objekt
	 */
	public PunktObjekt(Element e) {
		this(PunktObjekt.valueOf(e));
	}
	
	/**
	 * Returns the PunktObjekt representation of the DOM element describing a PlanPro Punkt_Objekt.
	 * A missing value of Wirkrichtung is interpreted as "beide".
	 * 
	 * @param e the DOM element describing a PlanPro Punkt_Objekt
	 * @return the PunktObjekt representation
	 */
	public static PunktObjekt valueOf(Element e) {
		PunktObjekt returnval = new PunktObjekt();
		returnval.identitaet = e.getChild("Identitaet").getChild("Wert").getText();
		List<Element> punktObjektTopKantenList = e.getChildren("Punkt_Objekt_TOP_Kante");
		int potkCount = punktObjektTopKantenList.size();
		returnval.punktObjektTopKante = new PunktObjektTopKante[potkCount];
		for(int i = 0; i < potkCount; i++) {
			returnval.punktObjektTopKante[i] = new PunktObjektTopKante();
			returnval.punktObjektTopKante[i].idTopKante = punktObjektTopKantenList.get(i).getChild("ID_TOP_Kante").getChild("Wert").getText();
			double tempAbstand = Double.parseDouble(punktObjektTopKantenList.get(i).getChild("Abstand").getChild("Wert").getText());
			tempAbstand *= 1000.0;
			returnval.punktObjektTopKante[i].abstand = (int) tempAbstand;
			returnval.punktObjektTopKante[i].wirkrichtung = "beide";
			if(punktObjektTopKantenList.get(i).getChild("Wirkrichtung") != null) {
				returnval.punktObjektTopKante[i].wirkrichtung = punktObjektTopKantenList.get(i).getChild("Wirkrichtung").getChild("Wert").getText();
			}
			returnval.punktObjektTopKante[i].seitlicheLage = null;
			if(punktObjektTopKantenList.get(i).getChild("Seitliche_Lage") != null) {
				returnval.punktObjektTopKante[i].seitlicheLage = punktObjektTopKantenList.get(i).getChild("Seitliche_Lage").getChild("Wert").getText();
			}
			returnval.punktObjektTopKante[i].seitlicherAbstand = 0;
			if(punktObjektTopKantenList.get(i).getChild("Seitlicher_Abstand") != null) {
				double tempSeitlicherAbstand = Double.parseDouble(punktObjektTopKantenList.get(i).getChild("Seitlicher_Abstand").getChild("Wert").getText());
				tempSeitlicherAbstand *= 1000.0;
				returnval.punktObjektTopKante[i].seitlicherAbstand = (int) tempSeitlicherAbstand;
			}
		}
		return returnval;
	}
	
	/**
	 * String representation of the object (only for debugging purposes).
	 * 
	 * @return String representation of the object
	 */
	@Override
	public String toString() {
		StringBuffer tklString = new StringBuffer();
		tklString.append("[");
		if(punktObjektTopKante.length > 0) {
			String val = punktObjektTopKante[0].toString();
			tklString.append(val);
		}
		for(int i = 1; i < punktObjektTopKante.length; i++) {
			String val = punktObjektTopKante[i].toString();
			tklString.append(",").append(val);
		}
		tklString.append("]");
		
		return "[" + identitaet + tklString.toString() + "]";
	}
}
