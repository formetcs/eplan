package eplan;

/**
 * Subclass to store all position information of a PlanPro Punkt_Objekt.
 * It corresponds to the Punkt_Objekt_TOP_Kante attribute group of a Punkt_Objekt
 * and can be instantiated multiple times inside {@link eplan.PunktObjekt}
 * to allow referencing different edges (e.g. on branches).
 * 
 * @author Stefan Dillmann
 *
 */
public class PunktObjektTopKante {
	
	/**
	 * The identity of the related PlanPro TOP_Kante.
	 */
	public String idTopKante;
	
	/**
	 * The distance from node A of the related PlanPro TOP_Kante in millimeters.
	 */
	public int abstand;
	
	/**
	 * The effective direction of the PlanPro Punkt_Objekt, related to the topological direction (A -> B) of the related PlanPro TOP_Kante.
	 * Possible values are "in", "gegen" and "beide".
	 * Note that this value is required here, while it is optional in the DOM representation.
	 * So if the information is not present in the DOM representation, the value "beide" can be used.
	 */
	public String wirkrichtung;
	
	/**
	 * The lateral distance of the PlanPro Punkt_Objekt from the track axis in millimeters.
	 * A negative value means left related to the topological direction (A -> B) of the related PlanPro TOP_Kante.
	 * A positive value means right related to the topological direction (A -> B) of the related PlanPro TOP_Kante.
	 * If this attribute is not used used, a zero value must be provided.
	 */
	public int seitlicherAbstand;
	
	/**
	 * The lateral position of the PlanPro Punkt_Objekt, related to the topological direction (A -> B) of the related PlanPro TOP_Kante.
	 * Possible values are "links" and "rechts".
	 * If this attribute is not used, null must be provided.
	 */
	public String seitlicheLage;
	
	/**
	 * Creates a new empty PunktObjektTopKante.
	 */
	public PunktObjektTopKante() {
		this.idTopKante = null;
		this.abstand = 0;
		this.wirkrichtung = null;
		this.seitlicherAbstand = 0;
		this.seitlicheLage = null;
	}
	
	
	public String toString() {
		return "[" + idTopKante + "," + abstand + "," + wirkrichtung + "," + seitlicherAbstand + "," + seitlicheLage + "]";
	}
}
