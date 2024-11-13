/**
 * 
 */
package eplan;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jdom2.Element;

import eplan.PlanProModel.Direction;
import eplan.conditions.ConditionConjunction;
import eplan.conditions.ConditionDisjunction;
import eplan.conditions.Evaluable;
import eplan.conditions.ExistenceCondition;
import eplan.conditions.IntegerCondition;
import eplan.conditions.SignalAspectCondition;
import eplan.conditions.StringCondition;
import eplan.conditions.TypeCondition;
import eplan.conditions.AbstractComparisonCondition.Operator;

/**
 * Class containing the main placement algorithms.
 * 
 * @author Stefan Dillmann
 *
 */
public class Constructor {
	
	private PlanProModel ppm;
	private int etcslevel;
	private String[] addlist;
	private String[] removelist;
	private boolean compatibilityMode;
	List<Element> alreadyHandledDp24;
	
	/**
	 * Set the ETCS level which should be used for planning.
	 * 
	 * 
	 * @param etcslevel the ETCS level
	 */
	public void setEtcslevel(int etcslevel) {
		this.etcslevel = etcslevel;
	}

	
	/**
	 * Turn on compatibility mode.
	 * This is necessary for older PlanPro files, where the signals for
	 * boundary marker and lantern of a switch are transposed.
	 * 
	 * 
	 * @param compatibilityMode true to turn on compatibility mode, false to turn off
	 */
	public void setCompatibilityMode(boolean compatibilityMode) {
		this.compatibilityMode = compatibilityMode;
	}
	
	
	/**
	 * Set the lists of datapoint types, which should be included or omitted in the final plan.
	 * The default behavior is that all implemented datapoint types are considered.
	 * To restrict the datapoints that should be planned to certain types, two lists can be provided:
	 * An addlist and a removelist.
	 * If an addlist is provided, only the datapoint types in the addlist are considered.
	 * If a removelist is provided, all datapoint types except of the types in the removelist are considered.
	 * Only one list can be used at the same time, the other should be set to null.
	 * If both lists are provided, the addlist will be used.
	 * If both lists are null, all datapoint types will be planned (default behavior).
	 * 
	 * 
	 * @param addlist Array of Strings, each containing one datapoint type to include, or null if the list should not be considered
	 * @param removelist Array of Strings, each containing one datapoint type to omit, or null if the list should not be considered
	 */
	public void setSelectionLists(String[] addlist, String[] removelist) {
		this.addlist = addlist;
		this.removelist = removelist;
	}

	
	/**
	 * Initialize the constructor object.
	 * The default behavior is the construction of an ETCS Level 2 plan,
	 * all available datapoint types are considered and compatibility mode
	 * is turned off.
	 * To change this behavior, the respective setter methods have to be used
	 * before calling the {@link eplan.Constructor#constructEtcsLine()} method.
	 * 
	 * 
	 * @param p The PlanPro model which should be extended to an ETCS plan
	 */
	public Constructor(PlanProModel p) {
		this.ppm = p;
		this.etcslevel = 2;
		this.addlist = null;
		this.removelist = null;
		this.compatibilityMode = false;
		alreadyHandledDp24 = new ArrayList<Element>();
	}
	
	
	/**
	 * Generate a new random GUID.
	 * 
	 * 
	 * @return the new GUID as string
	 */
	public static String generateGuid() {
		UUID guid = UUID.randomUUID();
		return guid.toString().toUpperCase();
	}
	
	
	/**
	 * Convert speed values from km/h to m/s.
	 * 
	 * 
	 * @param km_h the input value in km/h
	 * @return the converted value in m/s
	 */
	public static double toM_s(double km_h) {
		return km_h / 3.6;
	}
	
	
	/**
	 * Convert speed values from m/s to km/h.
	 * 
	 * 
	 * @param m_s the input value in m/s
	 * @return the converted value in km/h
	 */
	public static double toKm_h(double m_s) {
		return m_s * 3.6;
	}
	
	
	/**
	 * Print out the kilometer mark value of a Punkt_Objekt subtype.
	 * If the object is no Punkt_Objekt subtype or does not have a
	 * kilometer mark value, an empty string is returned.
	 * 
	 * 
	 * @param punktObjekt the DOM element of the Punkt_Objekt subtype
	 * @return the kilometer value, or an empty string if not available
	 */
	public static String printKmValue(Element punktObjekt) {
		if(punktObjekt.getChild("Punkt_Objekt_Strecke") == null) {
			return "";
		}
		if(punktObjekt.getChild("Punkt_Objekt_Strecke").getChild("Strecke_Km") == null) {
			return "";
		}
		return punktObjekt.getChild("Punkt_Objekt_Strecke").getChild("Strecke_Km").getChild("Wert").getText();
	}
	
	
	/**
	 * Print out the name of a signal.
	 * This is the long name (Bezeichnung_Lageplan_Lang).
	 * If the object is no signal or does not have a name, an empty string is returned.
	 * 
	 * 
	 * @param punktObjekt the DOM element of the signal
	 * @return the signal name
	 */
	public static String printSignalBezeichnung(Element punktObjekt) {
		if(punktObjekt.getChild("Bezeichnung") == null) {
			return "";
		}
		if(punktObjekt.getChild("Bezeichnung").getChild("Bezeichnung_Lageplan_Lang") == null) {
			return "";
		}
		return punktObjekt.getChild("Bezeichnung").getChild("Bezeichnung_Lageplan_Lang").getChild("Wert").getText();
	}
	
	
	/**
	 * Create a new balise element.
	 * 
	 * 
	 * @param idDatenpunkt id of the corresponding data point
	 * @param anordnung position of the balise within the balise group
	 * @return DOM element of the balise
	 */
	private Element createBalise(String idDatenpunkt, int anordnung) {
		Element bal = new Element("Balise");
		Element iddp = new Element("ID_Datenpunkt");
		Element iddpWert = new Element("Wert");
		iddpWert.setText(idDatenpunkt);
		iddp.addContent(iddpWert);
		Element balallg = new Element("Balise_Allg");
		Element anord = new Element("Anordnung_im_DP");
		Element anordWert = new Element("Wert");
		anordWert.setText(String.valueOf(anordnung));
		anord.addContent(anordWert);
		balallg.addContent(anord);
		
		bal.addContent(createIdentitaetElement(generateGuid()));
		bal.addContent(createBasisObjektElement(LocalDate.now()));
		bal.addContent(iddp);
		bal.addContent(balallg);
		return bal;
	}
	
	
	/**
	 * Create a new Identitaet subgroup element.
	 * 
	 * 
	 * @param idstring the GUID string
	 * @return DOM element of the Identitaet subgroup
	 */
	private Element createIdentitaetElement(String idstring) {
		Element id = new Element("Identitaet");
		Element wert = new Element("Wert");
		wert.setText(idstring);
		id.addContent(wert);
		return id;
	}
	
	
	/**
	 * Create a new Basis_Objekt_Allg subgroup element.
	 * 
	 * 
	 * @param datumRegelwerk date of the rulebook
	 * @return DOM element of the Basis_Objekt_Allg subgroup
	 */
	private Element createBasisObjektElement(LocalDate datumRegelwerk) {
		Element boa = new Element("Basis_Objekt_Allg");
		Element dr = new Element("Datum_Regelwerk");
		Element drWert = new Element("Wert");
		drWert.setText(datumRegelwerk.toString());
		dr.addContent(drWert);
		boa.addContent(dr);
		return boa;
	}
	
	
	/**
	 * Create a new Punkt_Objekt_TOP_Kante subgroup element.
	 * 
	 * 
	 * @param idTopKante id of the referenced TOP edge
	 * @param abstand distance from the beginning of the edge in mm
	 * @param seitlAbstand lateral distance from the middle of the edge in mm (negative values are on the left side)
	 * @param wirkrichtung effective direction relative to the edge direction
	 * @return DOM element of the Punkt_Objekt_TOP_Kante subgroup
	 */
	private Element createPunktObjektTopKanteElement(String idTopKante, int abstand, int seitlAbstand, String wirkrichtung) {
		Element potk = new Element("Punkt_Objekt_TOP_Kante");
		
		Element idtk = new Element("ID_TOP_Kante");
		Element idtkWert = new Element("Wert");
		idtkWert.setText(idTopKante);
		idtk.addContent(idtkWert);
		Element abst = new Element("Abstand");
		Element abstWert = new Element("Wert");
		double doubleAbstand = ((double) abstand / 1000.0);
		abstWert.setText(String.format("%.3f", doubleAbstand).replace(',', '.'));
		abst.addContent(abstWert);
		Element seitAbst = new Element("Seitlicher_Abstand");
		Element seitAbstWert = new Element("Wert");
		double doubleSeitlAbstand = ((double) seitlAbstand / 1000.0);
		seitAbstWert.setText(String.format("%.3f", doubleSeitlAbstand).replace(',', '.'));
		seitAbst.addContent(seitAbstWert);
		Element wr = new Element("Wirkrichtung");
		Element wrWert = new Element("Wert");
		wrWert.setText(wirkrichtung);
		wr.addContent(wrWert);
		
		potk.addContent(idtk);
		potk.addContent(abst);
		potk.addContent(seitAbst);
		potk.addContent(wr);
		return potk;
	}
	
	
	/**
	 * Create a new Punkt_Objekt_Strecke subgroup element.
	 * This method automatically calculates the kilometer mark value for the Punkt_Objekt
	 * by locating already placed neighbor objects and calculating the distances to them.
	 * 
	 * 
	 * @param referencePoint the PunktObjekt structure
	 * @return DOM element of the Punkt_Objekt_Strecke subgroup
	 */
	private Element createPunktObjektStreckeElement(PunktObjekt referencePoint) {
		Evaluable poCond = new ExistenceCondition("Punkt_Objekt_Strecke");
		List<NextPunktObjektPathResult> forwardElementList = ppm.getNextPunktObjektPaths(referencePoint, poCond, Direction.BOTH, true);
		Element forwardElement = null;
		if(forwardElementList.size() > 0) {
			forwardElement = forwardElementList.get(0).punktObjektElement;
		}
		List<NextPunktObjektPathResult> backwardElementList = ppm.getNextPunktObjektPaths(referencePoint, poCond, Direction.BOTH, false);
		Element backwardElement = null;
		if(backwardElementList.size() > 0) {
			backwardElement = backwardElementList.get(0).punktObjektElement;
		}
		if(forwardElement == null || backwardElement == null) {
			return null;
		}
		
		String idStreckeRefPoint = "";
		String streckeKmRefPoint = "";
		
		List<Element> fwStreckenList = forwardElement.getChildren("Punkt_Objekt_Strecke");
		for(int i = 0; i < fwStreckenList.size(); i++) {
			List<Element> bwStreckenList = backwardElement.getChildren("Punkt_Objekt_Strecke");
			for(int j = 0; j < bwStreckenList.size(); j++) {
				String fwStreckenId = fwStreckenList.get(i).getChild("ID_Strecke").getChild("Wert").getText();
				String bwStreckenId = bwStreckenList.get(j).getChild("ID_Strecke").getChild("Wert").getText();
				if(fwStreckenId.equals(bwStreckenId)) {
					idStreckeRefPoint = fwStreckenId;
					String fwStreckenKm = fwStreckenList.get(i).getChild("Strecke_Km").getChild("Wert").getText().replace(',', '.');
					String bwStreckenKm = bwStreckenList.get(j).getChild("Strecke_Km").getChild("Wert").getText().replace(',', '.');
					double fwStreckenKmValue = Double.parseDouble(fwStreckenKm);
					double bwStreckenKmValue = Double.parseDouble(bwStreckenKm);
					boolean increasingKm = true;
					if(fwStreckenKmValue < bwStreckenKmValue) {
						increasingKm = false;
					}
					int distance = ppm.calculateDistance(referencePoint, PunktObjekt.valueOf(forwardElement));
					if(!increasingKm) {
						distance *= -1;
					}
					// distance is in mm, fwStreckenKmValue in km -> conversion needed
					double targetKm = fwStreckenKmValue - ((double) distance / 1000000.0);
					streckeKmRefPoint = String.format("%.3f", targetKm).replace('.', ',');
					//streckeKmRefPoint = String.valueOf(targetKm).replace('.', ',');
				}
			}
		}
		
		if(idStreckeRefPoint.isEmpty() || streckeKmRefPoint.isEmpty()) {
			return null;
		}
		
		Element pos = new Element("Punkt_Objekt_Strecke");
		Element ids = new Element("ID_Strecke");
		Element idsWert = new Element("Wert");
		idsWert.setText(idStreckeRefPoint);
		ids.addContent(idsWert);
		Element skm = new Element("Strecke_Km");
		Element skmWert = new Element("Wert");
		skmWert.setText(streckeKmRefPoint);
		skm.addContent(skmWert);
		
		pos.addContent(ids);
		pos.addContent(skm);
		return pos;
	}
	
	
	/**
	 * Create a new DP_Bezug_Betrieblich subgroup element.
	 * 
	 * 
	 * @param art type of the reference point
	 * @param idBezug id of the reference point
	 * @return DOM element of the DP_Bezug_Betrieblich subgroup
	 */
	private Element createDpBezugBetrieblElement(String art, String idBezug) {
		Element dpbb = new Element("DP_Bezug_Betrieblich");
		
		Element bart = new Element("DP_Bezug_Betrieblich_Art");
		Element bartWert = new Element("Wert");
		bartWert.setText(art);
		bart.addContent(bartWert);
		Element id = new Element("ID_DP_Bezugspunkt");
		Element idWert = new Element("Wert");
		idWert.setText(idBezug);
		id.addContent(idWert);
		
		dpbb.addContent(bart);
		dpbb.addContent(id);
		return dpbb;
	}
	
	
	/**
	 * Create a new Datenpunkt_Allg subgroup element.
	 * 
	 * 
	 * @param anwendSys application system of the data point
	 * @param ausrichtung orientation of the data point
	 * @param beschreibung description of the data point
	 * @param laenge lenght of the data point (distance between first and last balise)
	 * @param standort location of the data point
	 * @return DOM element of the Datenpunkt_Allg subgroup
	 */
	private Element createDpAllgElement(String anwendSys, String ausrichtung, String beschreibung, double laenge, String standort) {
		Element dpallg = new Element("Datenpunkt_Allg");
		
		Element anw = new Element("Anwendungssystem");
		Element anwWert = new Element("Wert");
		anwWert.setText(anwendSys);
		anw.addContent(anwWert);
		Element ausr = new Element("Ausrichtung");
		Element ausrWert = new Element("Wert");
		ausrWert.setText(ausrichtung);
		ausr.addContent(ausrWert);
		Element beschr = new Element("Datenpunkt_Beschreibung");
		Element beschrWert = new Element("Wert");
		beschrWert.setText(beschreibung);
		beschr.addContent(beschrWert);
		Element len = new Element("Datenpunkt_Laenge");
		Element lenWert = new Element("Wert");
		//lenWert.setText(String.valueOf(laenge));
		lenWert.setText(String.format("%.3f", laenge).replace(',', '.'));
		len.addContent(lenWert);
		Element sto = new Element("Standortangabe");
		Element stoWert = new Element("Wert");
		stoWert.setText(standort);
		sto.addContent(stoWert);
		
		dpallg.addContent(anw);
		dpallg.addContent(ausr);
		dpallg.addContent(beschr);
		dpallg.addContent(len);
		dpallg.addContent(sto);
		return dpallg;
	}
	
	
	/**
	 * Create a new DP_Typ subgroup element for ETCS Level 1.
	 * 
	 * 
	 * @param typ type of the data point
	 * @return DOM element of the DP_Typ subgroup
	 */
	private Element createTypEsgElement(String typ) {
		Element dptyp = new Element("DP_Typ");
		Element dptypart = new Element("DP_Typ_Art");
		Element dptypartWert = new Element("Wert");
		dptypartWert.setText("primaer");
		dptypart.addContent(dptypartWert);
		Element dptypgesg = new Element("DP_Typ_GESG");
		Element dptypesg = new Element("DP_Typ_ESG");
		Element dptypesgWert = new Element("Wert");
		dptypesgWert.setText(typ);
		dptypesg.addContent(dptypesgWert);
		dptypgesg.addContent(dptypesg);
		
		
		dptyp.addContent(dptypart);
		dptyp.addContent(dptypgesg);
		return dptyp;
	}
	
	
	/**
	 * Create a new DP_Typ subgroup element for ETCS Level 2.
	 * 
	 * 
	 * @param types array with all types the data point should contain
	 * @return DOM element of the DP_Typ subgroup
	 */
	private Element createTypEtcsElement(int[] types) {
		Element dptyp = new Element("DP_Typ");
		Element dptypart = new Element("DP_Typ_Art");
		Element dptypartWert = new Element("Wert");
		dptypartWert.setText("primaer");
		dptypart.addContent(dptypartWert);
		Element dptypgetcs = new Element("DP_Typ_GETCS");
		for(int i = 0; i < types.length; i++) {
			Element dptypetcs = new Element("DP_Typ_ETCS");
			Element dptypetcsWert = new Element("Wert");
			dptypetcsWert.setText(String.valueOf(types[i]));
			dptypetcs.addContent(dptypetcsWert);
			dptypgetcs.addContent(dptypetcs);
		}
		
		
		dptyp.addContent(dptypart);
		dptyp.addContent(dptypgetcs);
		return dptyp;
	}
	
	
	/**
	 * Calculates the distance from a signal to the next trailing point.
	 * If a point is passed in facing or trailing direction is determined by checking if the
	 * point blade or fouling point indicator is closer to the start signal.
	 * 
	 * 
	 * @param signal DOM element of the signal which is the starting point of the search
	 * @return the distance from the signal to the next trailing point, or -1 if there is no such point
	 */
	private int calculateDistanceSignalStumpfeWeiche(Element signal) {
		Evaluable typeCond = new TypeCondition("W_Kr_Gsp_Komponente");
		Evaluable wExistCond = new ExistenceCondition("Zungenpaar");
		Evaluable krExistCond = new ExistenceCondition("Kreuzung");
		ConditionDisjunction disjunc = new ConditionDisjunction(wExistCond, krExistCond);
		ConditionConjunction conjunc = new ConditionConjunction(typeCond, disjunc);
		while(true) {
			List<NextPunktObjektPathResult> tempWKrList = ppm.getNextPunktObjektPaths(PunktObjekt.valueOf(signal), conjunc, Direction.BOTH, true);
			NextPunktObjektPathResult tempWKr = NextPunktObjektPathResult.nearest(tempWKrList);
			if(tempWKr == null) {
				return -1;
			}
			String idWKrElement = tempWKr.punktObjektElement.getChild("ID_W_Kr_Gsp_Element").getChild("Wert").getText();
			Element wKrElement = ppm.getElementbyId(idWKrElement);
			String attributeName = "ID_Grenzzeichen";
			if(compatibilityMode) {
				attributeName = "ID_Signal";
			}
			if(wKrElement.getChild("Weiche_Element").getChild(attributeName) == null) {
				return -1;
			}
			String idGrenzzeichen = wKrElement.getChild("Weiche_Element").getChild(attributeName).getChild("Wert").getText();
			Element grenzzeichen = ppm.getElementbyId(idGrenzzeichen);
			int distToGrenzzeichen = ppm.calculateDistance(PunktObjekt.valueOf(signal), PunktObjekt.valueOf(grenzzeichen));
			if(distToGrenzzeichen < tempWKr.distance) {
				return distToGrenzzeichen;
			}
			Evaluable cond = new StringCondition("Identitaet/Wert", Operator.NOT_EQUAL, tempWKr.punktObjektElement.getChild("Identitaet").getChild("Wert").getText());
			conjunc.addCondition(cond);
		}
	}
	
	
	/**
	 * Calculates the distance from a signal to the next danger point.
	 * The next danger point is either the distance to the next top node
	 * (which is also the position of a point blade), the distance to the
	 * next Ra 10 signal or the distance to the next trailing point,
	 * depending which of them is closest.
	 * 
	 * 
	 * @param signal DOM element of the signal which is the starting point of the search
	 * @return the distance from the signal to the next danger point
	 */
	private int calculateDistanceSignalDangerPoint(Element signal) {
		String idTopkante = signal.getChild("Punkt_Objekt_TOP_Kante").getChild("ID_TOP_Kante").getChild("Wert").getText();
		double tempAbstand = Double.parseDouble(signal.getChild("Punkt_Objekt_TOP_Kante").getChild("Abstand").getChild("Wert").getText());
		tempAbstand *= 1000.0;
		int abstand = (int) tempAbstand;
		String wirkrichtung = signal.getChild("Punkt_Objekt_TOP_Kante").getChild("Wirkrichtung").getChild("Wert").getText();
		
		Element topKanteElement = ppm.getElementbyId(idTopkante);
		double tempTopLaenge = Double.parseDouble(topKanteElement.getChild("TOP_Kante_Allg").getChild("TOP_Laenge").getChild("Wert").getText());
		tempTopLaenge *= 1000.0;
		int topLaenge = (int) tempTopLaenge;
		int finalDistance = 999999999;
		
		if(wirkrichtung.equals("gegen")) {
			finalDistance = abstand;
		}
		else {
			finalDistance = topLaenge - abstand;
		}
		
		Evaluable aspectCond = new SignalAspectCondition(ppm, "Ra_10");
		List<NextPunktObjektPathResult> ra10List = ppm.getNextPunktObjektPaths(PunktObjekt.valueOf(signal), aspectCond, Direction.OPPOSITE, true);
		NextPunktObjektPathResult nearestRa10 = NextPunktObjektPathResult.nearest(ra10List);
		if(nearestRa10 != null) {
			int ra10dist = nearestRa10.distance;
			if(ra10dist < finalDistance) {
				finalDistance = ra10dist;
			}
		}
		
		int distSW = calculateDistanceSignalStumpfeWeiche(signal);
		if(distSW >= 0 && distSW < finalDistance) {
			finalDistance = distSW;
		}
		
		return finalDistance;
	}
	
	
	/**
	 * Calculates the maximum allowed approach speed towards a signal.
	 * This is determined by the branch speed of the point that was passed last,
	 * but only if it was closer than 1000 m. Otherwise, a default speed of 160 km/h is used.
	 * 
	 * 
	 * @param signal DOM element of the signal
	 * @return the maximum allowed approach speed, or 160 if not available
	 */
	private double calculateVmax(Element signal) {
		String idTopkante = signal.getChild("Punkt_Objekt_TOP_Kante").getChild("ID_TOP_Kante").getChild("Wert").getText();
		String wirkrichtung = signal.getChild("Punkt_Objekt_TOP_Kante").getChild("Wirkrichtung").getChild("Wert").getText();
		
		Element topKanteElement = ppm.getElementbyId(idTopkante);
		String topAnschluss;
		
		if(wirkrichtung.equals("gegen")) {
			topAnschluss = topKanteElement.getChild("TOP_Kante_Allg").getChild("TOP_Anschluss_B").getChild("Wert").getText();
		}
		else {
			topAnschluss = topKanteElement.getChild("TOP_Kante_Allg").getChild("TOP_Anschluss_A").getChild("Wert").getText();
		}
		
		Evaluable typeCond = new TypeCondition("W_Kr_Gsp_Komponente");
		Evaluable wExistCond = new ExistenceCondition("Zungenpaar");
		Evaluable krExistCond = new ExistenceCondition("Kreuzung");
		ConditionDisjunction disjunc = new ConditionDisjunction(wExistCond, krExistCond);
		ConditionConjunction conjunc = new ConditionConjunction(typeCond, disjunc);
		
		List<NextPunktObjektPathResult> resultlist = ppm.getNextPunktObjektPaths(PunktObjekt.valueOf(signal), conjunc, Direction.BOTH, false);
		if(resultlist.isEmpty()) { // no switch found -> use line speed
			return 160;
		}
		
		NextPunktObjektPathResult resultitem = resultlist.get(0); // Only first element, others have same position
		Element tempswitch = resultitem.punktObjektElement;
		
		int finalDistance = ppm.calculateDistance(PunktObjekt.valueOf(signal), PunktObjekt.valueOf(tempswitch));
		if(finalDistance > 1000000) { // don't restrict speed if distance is too far
			return 160;
		}
		
		double leftspeed = 160;
		double rightspeed = 160;
		if(tempswitch.getChild("Kreuzung") != null) {
			leftspeed = Double.parseDouble(tempswitch.getChild("Kreuzung").getChild("Geschwindigkeit_L").getChild("Wert").getText());
			rightspeed = Double.parseDouble(tempswitch.getChild("Kreuzung").getChild("Geschwindigkeit_R").getChild("Wert").getText());
		}
		else if(tempswitch.getChild("Zungenpaar") != null) {
			leftspeed = Double.parseDouble(tempswitch.getChild("Zungenpaar").getChild("Geschwindigkeit_L").getChild("Wert").getText());
			rightspeed = Double.parseDouble(tempswitch.getChild("Zungenpaar").getChild("Geschwindigkeit_R").getChild("Wert").getText());
		}
		
		if(topAnschluss.equals("Links")) {
			return leftspeed;
		}
		else if(topAnschluss.equals("Rechts")) {
			return rightspeed;
		}
		else if(topAnschluss.equals("Spitze")) {
			return Math.max(leftspeed, rightspeed);
		}
		
		return 160;
	}
	
	
	/**
	 * Check if a given datapoint type is selected for plan creation.
	 * This method checks the addlist and removelist to determine which
	 * datapoint types should be considered for the plan creation.
	 * If both lists contain values, the addlist is preferred.
	 * 
	 * 
	 * @param searchtype the data point type to ckeck
	 * @return true if the type is selected, false otherwise
	 */
	private boolean isTypeSelected(String searchtype) {
		if(addlist != null) {
			for(int i = 0; i < addlist.length; i++) {
				String currenttype = addlist[i];
				if (searchtype.equals(currenttype)) {
					return true;
				}
				
			}
			return false;
		}
		
		if(removelist != null) {
			for(int i = 0; i < removelist.length; i++) {
				String currenttype = removelist[i];
				if (searchtype.equals(currenttype)) {
					return false;
				}
				
			}
			return true;
		}
		
		return true;
	}
	
	
	/**
	 * Construct the ETCS line.
	 * This method adds ETCS objects to the PlanPro model,
	 * depending on the already existing reference objects.
	 * Please note that the planning parameters like ETCS Level and
	 * the selection of datatypes have to be set using the respective
	 * setter methods before calling this method.
	 */
	public void constructEtcsLine() {
		if(compatibilityMode) {
			Logger.log("compatibility mode activated");
		}
		if (etcslevel == 1) {
			if(isTypeSelected("HS")) {
				placeDpHs();
			}
			if(isTypeSelected("MS")) {
			    placeDpMs();
			}
		    if(isTypeSelected("VS")) {
			    placeDpVs();
		    }
		    if(isTypeSelected("VW")) {
			    placeDpVw();
		    }
		    if(isTypeSelected("AW")) {
			    placeDpAw();
		    }
		}
		else {
			if(isTypeSelected("20")) {
				placeDp20();
			}
			if(isTypeSelected("21")) {
				placeDp21();
			}
			if(isTypeSelected("22")) {
				placeDp22();
			}
			if(isTypeSelected("23")) {
				placeDp23();
			}
			if(isTypeSelected("28")) {
				placeDp28();
			}
			if(isTypeSelected("24")) {
				placeDp24();
			}
			if(isTypeSelected("26")) {
				placeDp26();
			}
			if(isTypeSelected("9")) {
				placeDp9();
			}
			if(isTypeSelected("25")) {
				placeDp25FaultySwitch();
				placeDp25GapFill();
			}
		}
		Logger.log("placement finished!");
	}
	
	
	/**
	 * Contains the placement rules for datapoint type HS (ETCS Level 1).
	 */
	private void placeDpHs() {
		Logger.log("placing DP HS...");
		Element containerElement = ppm.getContainerElement();
		List<Element> objectList = containerElement.getChildren();
		for(int i = 0; i < objectList.size(); i++) {
			Element currentObject = objectList.get(i);
			if(!currentObject.getName().equals("Signal")) {
				continue;
			}
			if(currentObject.getChild("Signal_Real") == null) {
				continue;
			}
			if(currentObject.getChild("Signal_Real").getChild("Signal_Real_Aktiv_Schirm") == null) {
				continue;
			}
			String signalArt = currentObject.getChild("Signal_Real").getChild("Signal_Real_Aktiv_Schirm").getChild("Signal_Art").getChild("Wert").getText();
			if(signalArt.equals("Hauptsignal") || signalArt.equals("Hauptsperrsignal")) {
				Logger.log("-Signal " + printSignalBezeichnung(currentObject) + " (km " + printKmValue(currentObject) + ")");
				String signalId = currentObject.getChild("Identitaet").getChild("Wert").getText();
				List<PunktObjekt> nextposlist = ppm.calculatePosition(PunktObjekt.valueOf(currentObject), -3000);
				for(int j = 0; j < nextposlist.size(); j++) {
					PunktObjekt nextpos = nextposlist.get(j);
					PunktObjektTopKante nextpotk = nextpos.punktObjektTopKante[0];
					Element dp = new Element("Datenpunkt");
					String guid = generateGuid();
					dp.addContent(createIdentitaetElement(guid));
					dp.addContent(createBasisObjektElement(LocalDate.now()));
					dp.addContent(createPunktObjektTopKanteElement(nextpotk.idTopKante, nextpotk.abstand, 0, nextpotk.wirkrichtung));
					Element poStrecke = createPunktObjektStreckeElement(nextpos);
					if(poStrecke != null) {
						dp.addContent(poStrecke);
					}
					dp.addContent(createDpBezugBetrieblElement("unmittelbar", signalId));
					dp.addContent(createDpAllgElement("ESG", nextpotk.wirkrichtung, "Hauptsignal-DP (Typ HS)", 3, "Signal"));
					dp.addContent(createTypEsgElement("HS"));
					Logger.log("--placing DP at km " + printKmValue(dp));
					containerElement.addContent(dp);
					containerElement.addContent(createBalise(guid, 1));
					containerElement.addContent(createBalise(guid, 2));
				}
			}
		}
	}
	
	
	/**
	 * Contains the placement rules for datapoint type MS (ETCS Level 1).
	 */
	private void placeDpMs() {
		Logger.log("placing DP MS...");
		Element containerElement = ppm.getContainerElement();
		List<Element> objectList = containerElement.getChildren();
		for(int i = 0; i < objectList.size(); i++) {
			Element currentObject = objectList.get(i);
			if(!currentObject.getName().equals("Signal")) {
				continue;
			}
			if(currentObject.getChild("Signal_Real") == null) {
				continue;
			}
			if(currentObject.getChild("Signal_Real").getChild("Signal_Real_Aktiv_Schirm") == null) {
				continue;
			}
			String signalArt = currentObject.getChild("Signal_Real").getChild("Signal_Real_Aktiv_Schirm").getChild("Signal_Art").getChild("Wert").getText();
			if(signalArt.equals("Mehrabschnittssignal") || signalArt.equals("Mehrabschnittssperrsignal")) {
				Logger.log("-Signal " + printSignalBezeichnung(currentObject) + " (km " + printKmValue(currentObject) + ")");
				String signalId = currentObject.getChild("Identitaet").getChild("Wert").getText();
				List<PunktObjekt> nextposlist = ppm.calculatePosition(PunktObjekt.valueOf(currentObject), -3000);
				for(int j = 0; j < nextposlist.size(); j++) {
					PunktObjekt nextpos = nextposlist.get(j);
					PunktObjektTopKante nextpotk = nextpos.punktObjektTopKante[0];
					Element dp = new Element("Datenpunkt");
					String guid = generateGuid();
					dp.addContent(createIdentitaetElement(guid));
					dp.addContent(createBasisObjektElement(LocalDate.now()));
					dp.addContent(createPunktObjektTopKanteElement(nextpotk.idTopKante, nextpotk.abstand, 0, nextpotk.wirkrichtung));
					Element poStrecke = createPunktObjektStreckeElement(nextpos);
					if(poStrecke != null) {
						dp.addContent(poStrecke);
					}
					dp.addContent(createDpBezugBetrieblElement("unmittelbar", signalId));
					dp.addContent(createDpAllgElement("ESG", nextpotk.wirkrichtung, "Mehrabschnittssignal-DP (Typ MS)", 3, "Signal"));
					dp.addContent(createTypEsgElement("MS"));
					Logger.log("--placing DP at km " + printKmValue(dp));
					containerElement.addContent(dp);
					containerElement.addContent(createBalise(guid, 1));
					containerElement.addContent(createBalise(guid, 2));
				}
			}
		}
	}

	
	/**
	 * Contains the placement rules for datapoint type VS (ETCS Level 1).
	 */
	private void placeDpVs() {
		Logger.log("placing DP VS...");
		Element containerElement = ppm.getContainerElement();
		List<Element> objectList = containerElement.getChildren();
		for(int i = 0; i < objectList.size(); i++) {
			Element currentObject = objectList.get(i);
			if(!currentObject.getName().equals("Signal")) {
				continue;
			}
			if(currentObject.getChild("Signal_Real") == null) {
				continue;
			}
			if(currentObject.getChild("Signal_Real").getChild("Signal_Real_Aktiv_Schirm") == null) {
				continue;
			}
			String signalArt = currentObject.getChild("Signal_Real").getChild("Signal_Real_Aktiv_Schirm").getChild("Signal_Art").getChild("Wert").getText();
			if(signalArt.equals("Vorsignal")) {
				Logger.log("-Signal " + printSignalBezeichnung(currentObject) + " (km " + printKmValue(currentObject) + ")");
				String signalId = currentObject.getChild("Identitaet").getChild("Wert").getText();
				List<PunktObjekt> nextposlist = ppm.calculatePosition(PunktObjekt.valueOf(currentObject), -3000);
				for(int j = 0; j < nextposlist.size(); j++) {
					PunktObjekt nextpos = nextposlist.get(j);
					PunktObjektTopKante nextpotk = nextpos.punktObjektTopKante[0];
					Element dp = new Element("Datenpunkt");
					String guid = generateGuid();
					dp.addContent(createIdentitaetElement(guid));
					dp.addContent(createBasisObjektElement(LocalDate.now()));
					dp.addContent(createPunktObjektTopKanteElement(nextpotk.idTopKante, nextpotk.abstand, 0, nextpotk.wirkrichtung));
					Element poStrecke = createPunktObjektStreckeElement(nextpos);
					if(poStrecke != null) {
						dp.addContent(poStrecke);
					}
					dp.addContent(createDpBezugBetrieblElement("unmittelbar", signalId));
					dp.addContent(createDpAllgElement("ESG", nextpotk.wirkrichtung, "Vorsignal-DP (Typ VS)", 3, "Signal"));
					dp.addContent(createTypEsgElement("VS"));
					Logger.log("--placing DP at km " + printKmValue(dp));
					containerElement.addContent(dp);
					containerElement.addContent(createBalise(guid, 1));
					containerElement.addContent(createBalise(guid, 2));
				}
			}
		}
	}

	
	/**
	 * Contains the placement rules for datapoint type VW (ETCS Level 1).
	 */
	private void placeDpVw() {
		Logger.log("placing DP VW...");
		Element containerElement = ppm.getContainerElement();
		List<Element> objectList = containerElement.getChildren();
		for(int i = 0; i < objectList.size(); i++) {
			Element currentObject = objectList.get(i);
			if(!currentObject.getName().equals("Signal")) {
				continue;
			}
			if(currentObject.getChild("Signal_Real") == null) {
				continue;
			}
			if(currentObject.getChild("Signal_Real").getChild("Signal_Real_Aktiv_Schirm") == null) {
				continue;
			}
			String signalArt = currentObject.getChild("Signal_Real").getChild("Signal_Real_Aktiv_Schirm").getChild("Signal_Art").getChild("Wert").getText();
			if(signalArt.equals("Vorsignalwiederholer")) {
				Logger.log("-Signal " + printSignalBezeichnung(currentObject) + " (km " + printKmValue(currentObject) + ")");
				String signalId = currentObject.getChild("Identitaet").getChild("Wert").getText();
				List<PunktObjekt> nextposlist = ppm.calculatePosition(PunktObjekt.valueOf(currentObject), -3000);
				for(int j = 0; j < nextposlist.size(); j++) {
					PunktObjekt nextpos = nextposlist.get(j);
					PunktObjektTopKante nextpotk = nextpos.punktObjektTopKante[0];
					Element dp = new Element("Datenpunkt");
					String guid = generateGuid();
					dp.addContent(createIdentitaetElement(guid));
					dp.addContent(createBasisObjektElement(LocalDate.now()));
					dp.addContent(createPunktObjektTopKanteElement(nextpotk.idTopKante, nextpotk.abstand, 0, nextpotk.wirkrichtung));
					Element poStrecke = createPunktObjektStreckeElement(nextpos);
					if(poStrecke != null) {
						dp.addContent(poStrecke);
					}
					dp.addContent(createDpBezugBetrieblElement("unmittelbar", signalId));
					dp.addContent(createDpAllgElement("ESG", nextpotk.wirkrichtung, "Vorsignalwiederholer-DP (Typ VW)", 3, "Signal"));
					dp.addContent(createTypEsgElement("VW"));
					Logger.log("--placing DP at km " + printKmValue(dp));
					containerElement.addContent(dp);
					containerElement.addContent(createBalise(guid, 1));
					containerElement.addContent(createBalise(guid, 2));
				}
			}
		}
	}

	
	/**
	 * Contains the placement rules for datapoint type AW (ETCS Level 1).
	 */
	private void placeDpAw() {
		Logger.log("placing DP AW...");
		Element containerElement = ppm.getContainerElement();
		List<Element> objectList = containerElement.getChildren();
		for(int i = 0; i < objectList.size(); i++) {
			Element currentObject = objectList.get(i);
			if(!currentObject.getName().equals("Signal")) {
				continue;
			}
			if(currentObject.getChild("Signal_Real") == null) {
				continue;
			}
			if(currentObject.getChild("Signal_Real").getChild("Signal_Real_Aktiv_Schirm") == null) {
				continue;
			}
			String signalArt = currentObject.getChild("Signal_Real").getChild("Signal_Real_Aktiv_Schirm").getChild("Signal_Art").getChild("Wert").getText();
			if(signalArt.equals("Hauptsignal") || signalArt.equals("Hauptsperrsignal") || signalArt.equals("Mehrabschnittssignal") || signalArt.equals("Mehrabschnittssperrsignal")) {
				Logger.log("-Signal " + printSignalBezeichnung(currentObject) + " (km " + printKmValue(currentObject) + ")");
				String signalId = currentObject.getChild("Identitaet").getChild("Wert").getText();
				List<PunktObjekt> nextposlist = ppm.calculatePosition(PunktObjekt.valueOf(currentObject), -250000);
				for(int j = 0; j < nextposlist.size(); j++) {
					PunktObjekt nextpos = nextposlist.get(j);
					PunktObjektTopKante nextpotk = nextpos.punktObjektTopKante[0];
					Element dp = new Element("Datenpunkt");
					String guid = generateGuid();
					dp.addContent(createIdentitaetElement(guid));
					dp.addContent(createBasisObjektElement(LocalDate.now()));
					dp.addContent(createPunktObjektTopKanteElement(nextpotk.idTopKante, nextpotk.abstand, 0, nextpotk.wirkrichtung));
					Element poStrecke = createPunktObjektStreckeElement(nextpos);
					if(poStrecke != null) {
						dp.addContent(poStrecke);
					}
					dp.addContent(createDpBezugBetrieblElement("mittelbar", signalId));
					int dpNr = j + 1;
					dp.addContent(createDpAllgElement("ESG", nextpotk.wirkrichtung, "Aufwerte-DP " + dpNr + "/" + nextposlist.size() + " (Typ AW)", 3, "Signal Gleis"));
					dp.addContent(createTypEsgElement("AW"));
					Logger.log("--placing DP at km " + printKmValue(dp));
					containerElement.addContent(dp);
					containerElement.addContent(createBalise(guid, 1));
					containerElement.addContent(createBalise(guid, 2));
				}
			}
		}
	}
	
	
	/**
	 * Contains the placement rules for datapoint type 9 (ETCS Level 2).
	 */
	private void placeDp9() {
		Logger.log("placing DP 9...");
		Element containerElement = ppm.getContainerElement();
		List<Element> objectList = containerElement.getChildren();
		for(int i = 0; i < objectList.size(); i++) {
			Element currentObject = objectList.get(i);
			if(!currentObject.getName().equals("Datenpunkt")) {
				continue;
			}
			Element dptypgetcs = currentObject.getChild("DP_Typ").getChild("DP_Typ_GETCS");
			List<Element> typelist = dptypgetcs.getChildren("DP_Typ_ETCS");
			for(int j = 0; j < typelist.size(); j++) {
				Element currentType = typelist.get(j);
				int dpType = Integer.parseInt(currentType.getChild("Wert").getText());
				if(dpType != 28) {
					continue;
				}
				double dplength = Double.parseDouble(currentObject.getChild("Datenpunkt_Allg").getChild("Datenpunkt_Laenge").getChild("Wert").getText());
				if(dplength > 0.5) { // DP consists of two balises
					Element dptypetcs = new Element("DP_Typ_ETCS");
					Element dptypetcsWert = new Element("Wert");
					dptypetcsWert.setText("9");
					dptypetcs.addContent(dptypetcsWert);
					dptypgetcs.addContent(dptypetcs);
					Logger.log("--added type 9 to DP at km " + printKmValue(currentObject));
				}
			}
		}
	}
	
	/**
	 * Contains the placement rules for datapoint type 20 (Back-to-back case) (ETCS Level 2).
	 * Two signals are considered as back-to-back if they are facing in opposite direction
	 * and have a distance of less than 500 mm.
	 */
	private void placeDp20() {
		Logger.log("placing DP 20...");
		List<Element> alreadyHandledSignals = new ArrayList<Element>();
		Element containerElement = ppm.getContainerElement();
		List<Element> objectList = containerElement.getChildren();
		for(int i = 0; i < objectList.size(); i++) {
			Element currentObject = objectList.get(i);
			if(!currentObject.getName().equals("Signal")) {
				continue;
			}
			if(alreadyHandledSignals.contains(currentObject)) {
				Logger.log("-Signal " + printSignalBezeichnung(currentObject) + " (km " + printKmValue(currentObject) + ")");
				Logger.log("--already handled, no DP required");
				continue;
			}
			if(currentObject.getChild("Signal_Real") == null) {
				continue;
			}
			if(currentObject.getChild("Signal_Real").getChild("Signal_Real_Aktiv") == null) {
				continue;
			}
			String signalFunc = currentObject.getChild("Signal_Real").getChild("Signal_Real_Aktiv").getChild("Signal_Funktion").getChild("Wert").getText();
			if(signalFunc.equals("Block_Signal") || signalFunc.equals("Einfahr_Signal")) {
				Logger.log("-Signal " + printSignalBezeichnung(currentObject) + " (km " + printKmValue(currentObject) + ")");
				String signalId = currentObject.getChild("Identitaet").getChild("Wert").getText();
				Evaluable cond1 = new StringCondition("Signal_Real/Signal_Real_Aktiv/Signal_Funktion/Wert", Operator.EQUAL, "Block_Signal");
				Evaluable cond2 = new StringCondition("Signal_Real/Signal_Real_Aktiv/Signal_Funktion/Wert", Operator.EQUAL, "Einfahr_Signal");
				ConditionDisjunction disjunc = new ConditionDisjunction(cond1, cond2);
				List<NextPunktObjektPathResult> otherSignalsForward = ppm.getNextPunktObjektPaths(PunktObjekt.valueOf(currentObject), disjunc, Direction.OPPOSITE, true);
				List<NextPunktObjektPathResult> otherSignalsBackward = ppm.getNextPunktObjektPaths(PunktObjekt.valueOf(currentObject), disjunc, Direction.OPPOSITE, false);
				boolean alreadyPlacedDp = false;
				for(int i1 = 0; i1 < otherSignalsForward.size(); i1++) {
					NextPunktObjektPathResult current = otherSignalsForward.get(i1);
					if (current.distance <= 500) {
						// place DP 20 on the common position of both signals, each balise 1.5 m in front of one signal
						PunktObjekt target = ppm.calculatePositionOnPath(PunktObjekt.valueOf(currentObject), current.topKantenList, 1500, false);
						PunktObjektTopKante nextpotk = target.punktObjektTopKante[0];
						Element dp = new Element("Datenpunkt");
						String guid = generateGuid();
						dp.addContent(createIdentitaetElement(guid));
						dp.addContent(createBasisObjektElement(LocalDate.now()));
						dp.addContent(createPunktObjektTopKanteElement(nextpotk.idTopKante, nextpotk.abstand, 0, nextpotk.wirkrichtung));
						Element poStrecke = createPunktObjektStreckeElement(target);
						if(poStrecke != null) {
							dp.addContent(poStrecke);
						}
						dp.addContent(createDpBezugBetrieblElement("unmittelbar", signalId));
						dp.addContent(createDpAllgElement("ETCS", "beide", "Datenpunkt an Signalen (kombiniert) (Typ 20)", 3, "Signal"));
						int[] types = {20};
						dp.addContent(createTypEtcsElement(types));
						Logger.log("--combining with " + printSignalBezeichnung(current.punktObjektElement) + " (km " + printKmValue(current.punktObjektElement) + ")");
						Logger.log("--placing DP (combined) at km " + printKmValue(dp));
						containerElement.addContent(dp);
						containerElement.addContent(createBalise(guid, 1));
						containerElement.addContent(createBalise(guid, 2));
						alreadyHandledSignals.add(current.punktObjektElement);
						alreadyPlacedDp = true;
						break; // there should be no other path to be equipped with DP 20
					}
				}
				for(int i1 = 0; !alreadyPlacedDp && i1 < otherSignalsBackward.size(); i1++) {
					NextPunktObjektPathResult current = otherSignalsBackward.get(i1);
					if (current.distance <= 500) {
						// place DP 20 on the common position of both signals, each balise 1.5 m in front of one signal
						PunktObjekt target = ppm.calculatePositionOnPath(PunktObjekt.valueOf(currentObject), current.topKantenList, 1500, false);
						PunktObjektTopKante nextpotk = target.punktObjektTopKante[0];
						Element dp = new Element("Datenpunkt");
						String guid = generateGuid();
						dp.addContent(createIdentitaetElement(guid));
						dp.addContent(createBasisObjektElement(LocalDate.now()));
						dp.addContent(createPunktObjektTopKanteElement(nextpotk.idTopKante, nextpotk.abstand, 0, nextpotk.wirkrichtung));
						Element poStrecke = createPunktObjektStreckeElement(target);
						if(poStrecke != null) {
							dp.addContent(poStrecke);
						}
						dp.addContent(createDpBezugBetrieblElement("unmittelbar", signalId));
						dp.addContent(createDpAllgElement("ETCS", "beide", "Datenpunkt an Signalen (kombiniert) (Typ 20)", 3, "Signal"));
						int[] types = {20};
						dp.addContent(createTypEtcsElement(types));
						Logger.log("--combining with " + printSignalBezeichnung(current.punktObjektElement) + " (km " + printKmValue(current.punktObjektElement) + ")");
						Logger.log("--placing DP (combined) at km " + printKmValue(dp));
						containerElement.addContent(dp);
						containerElement.addContent(createBalise(guid, 1));
						containerElement.addContent(createBalise(guid, 2));
						alreadyHandledSignals.add(current.punktObjektElement);
						alreadyPlacedDp = true;
						break; // there should be no other path to be equipped with DP 20
					}
				}
				if(!alreadyPlacedDp) {
					placeDp20Regular(currentObject);
				}
			}
		}
	}
	
	
	/**
	 * Contains the placement rules for datapoint type 20 (Regular case) (ETCS Level 2).
	 * 
	 * 
	 * @param currentObject DOM element of the reference point for the datapoint
	 */
	private void placeDp20Regular(Element currentObject) {
		Element containerElement = ppm.getContainerElement();
		String signalId = currentObject.getChild("Identitaet").getChild("Wert").getText();
		List<PunktObjekt> nextposlist = ppm.calculatePosition(PunktObjekt.valueOf(currentObject), -6000);
		for(int j = 0; j < nextposlist.size(); j++) {
			PunktObjekt nextpos = nextposlist.get(j);
			PunktObjektTopKante nextpotk = nextpos.punktObjektTopKante[0];
			Element dp = new Element("Datenpunkt");
			String guid = generateGuid();
			dp.addContent(createIdentitaetElement(guid));
			dp.addContent(createBasisObjektElement(LocalDate.now()));
			dp.addContent(createPunktObjektTopKanteElement(nextpotk.idTopKante, nextpotk.abstand, 0, nextpotk.wirkrichtung));
			Element poStrecke = createPunktObjektStreckeElement(nextpos);
			if(poStrecke != null) {
				dp.addContent(poStrecke);
			}
			dp.addContent(createDpBezugBetrieblElement("unmittelbar", signalId));
			dp.addContent(createDpAllgElement("ETCS", nextpotk.wirkrichtung, "Datenpunkt an Signalen (Typ 20)", 3, "Signal"));
			int[] types = {20};
			dp.addContent(createTypEtcsElement(types));
			Logger.log("--placing DP at km " + printKmValue(dp));
			containerElement.addContent(dp);
			containerElement.addContent(createBalise(guid, 1));
			containerElement.addContent(createBalise(guid, 2));
		}
	}
	
	
	/**
	 * Contains the placement rules for datapoint type 21 (Back-to-back case) (ETCS Level 2).
	 * Two signals are considered as back-to-back if they are facing in opposite direction
	 * and have a distance of less than 500 mm.
	 */
	private void placeDp21() {
		Logger.log("placing DP 21...");
		List<Element> alreadyHandledSignals = new ArrayList<Element>();
		Element containerElement = ppm.getContainerElement();
		List<Element> objectList = containerElement.getChildren();
		for(int i = 0; i < objectList.size(); i++) {
			Element currentObject = objectList.get(i);
			if(!currentObject.getName().equals("Signal")) {
				continue;
			}
			if(alreadyHandledSignals.contains(currentObject)) {
				Logger.log("-Signal " + printSignalBezeichnung(currentObject) + " (km " + printKmValue(currentObject) + ")");
				Logger.log("--already handled, no DP required");
				continue;
			}
			if(currentObject.getChild("Signal_Real") == null) {
				continue;
			}
			if(currentObject.getChild("Signal_Real").getChild("Signal_Real_Aktiv") == null) {
				continue;
			}
			String signalFunc = currentObject.getChild("Signal_Real").getChild("Signal_Real_Aktiv").getChild("Signal_Funktion").getChild("Wert").getText();
			if(signalFunc.equals("Ausfahr_Signal") || signalFunc.equals("Ausfahr_Zwischen_Signal") ||
					signalFunc.equals("Einfahr_Ausfahr_Signal") || signalFunc.equals("Gruppenausfahr_Gruppenzwischen_Signal") ||
					signalFunc.equals("Gruppenausfahr_Signal") || signalFunc.equals("Gruppenzwischen_Signal") ||
					signalFunc.equals("Zugdeckungs_Signal") || signalFunc.equals("Zwischen_Signal")) {
				Logger.log("-Signal " + printSignalBezeichnung(currentObject) + " (km " + printKmValue(currentObject) + ")");
				String signalId = currentObject.getChild("Identitaet").getChild("Wert").getText();
				Evaluable cond1 = new StringCondition("Signal_Real/Signal_Real_Aktiv/Signal_Funktion/Wert", Operator.EQUAL, "Ausfahr_Signal");
				Evaluable cond2 = new StringCondition("Signal_Real/Signal_Real_Aktiv/Signal_Funktion/Wert", Operator.EQUAL, "Ausfahr_Zwischen_Signal");
				Evaluable cond3 = new StringCondition("Signal_Real/Signal_Real_Aktiv/Signal_Funktion/Wert", Operator.EQUAL, "Einfahr_Ausfahr_Signal");
				Evaluable cond4 = new StringCondition("Signal_Real/Signal_Real_Aktiv/Signal_Funktion/Wert", Operator.EQUAL, "Gruppenausfahr_Gruppenzwischen_Signal");
				Evaluable cond5 = new StringCondition("Signal_Real/Signal_Real_Aktiv/Signal_Funktion/Wert", Operator.EQUAL, "Gruppenausfahr_Signal");
				Evaluable cond6 = new StringCondition("Signal_Real/Signal_Real_Aktiv/Signal_Funktion/Wert", Operator.EQUAL, "Gruppenzwischen_Signal");
				Evaluable cond7 = new StringCondition("Signal_Real/Signal_Real_Aktiv/Signal_Funktion/Wert", Operator.EQUAL, "Zugdeckungs_Signal");
				Evaluable cond8 = new StringCondition("Signal_Real/Signal_Real_Aktiv/Signal_Funktion/Wert", Operator.EQUAL, "Zwischen_Signal");
				ConditionDisjunction disjunc = new ConditionDisjunction(cond1, cond2, cond3, cond4, cond5, cond6, cond7, cond8);
				List<NextPunktObjektPathResult> otherSignalsForward = ppm.getNextPunktObjektPaths(PunktObjekt.valueOf(currentObject), disjunc, Direction.OPPOSITE, true);
				List<NextPunktObjektPathResult> otherSignalsBackward = ppm.getNextPunktObjektPaths(PunktObjekt.valueOf(currentObject), disjunc, Direction.OPPOSITE, false);
				boolean alreadyPlacedDp = false;
				for(int i1 = 0; i1 < otherSignalsForward.size(); i1++) {
					NextPunktObjektPathResult current = otherSignalsForward.get(i1);
					if (current.distance <= 500) {
						// place DP 21 on the common position of both signals, each balise 1.5 m in front of one signal
						PunktObjekt target = ppm.calculatePositionOnPath(PunktObjekt.valueOf(currentObject), current.topKantenList, 1500, false);
						PunktObjektTopKante nextpotk = target.punktObjektTopKante[0];
						Element dp = new Element("Datenpunkt");
						String guid = generateGuid();
						dp.addContent(createIdentitaetElement(guid));
						dp.addContent(createBasisObjektElement(LocalDate.now()));
						dp.addContent(createPunktObjektTopKanteElement(nextpotk.idTopKante, nextpotk.abstand, 0, nextpotk.wirkrichtung));
						Element poStrecke = createPunktObjektStreckeElement(target);
						if(poStrecke != null) {
							dp.addContent(poStrecke);
						}
						dp.addContent(createDpBezugBetrieblElement("unmittelbar", signalId));
						dp.addContent(createDpAllgElement("ETCS", "beide", "Datenpunkt an Ausfahrsignalen (kombiniert) (Typ 21)", 3, "Signal"));
						int[] types = {21};
						dp.addContent(createTypEtcsElement(types));
						Logger.log("--combining with " + printSignalBezeichnung(current.punktObjektElement) + " (km " + printKmValue(current.punktObjektElement) + ")");
						Logger.log("--placing DP (combined) at km " + printKmValue(dp));
						containerElement.addContent(dp);
						containerElement.addContent(createBalise(guid, 1));
						containerElement.addContent(createBalise(guid, 2));
						alreadyHandledSignals.add(current.punktObjektElement);
						alreadyPlacedDp = true;
						break; // there should be no other path to be equipped with DP 21
					}
				}
				for(int i1 = 0; !alreadyPlacedDp && i1 < otherSignalsBackward.size(); i1++) {
					NextPunktObjektPathResult current = otherSignalsBackward.get(i1);
					if (current.distance <= 500) {
						// place DP 21 on the common position of both signals, each balise 1.5 m in front of one signal
						PunktObjekt target = ppm.calculatePositionOnPath(PunktObjekt.valueOf(currentObject), current.topKantenList, 1500, false);
						PunktObjektTopKante nextpotk = target.punktObjektTopKante[0];
						Element dp = new Element("Datenpunkt");
						String guid = generateGuid();
						dp.addContent(createIdentitaetElement(guid));
						dp.addContent(createBasisObjektElement(LocalDate.now()));
						dp.addContent(createPunktObjektTopKanteElement(nextpotk.idTopKante, nextpotk.abstand, 0, nextpotk.wirkrichtung));
						Element poStrecke = createPunktObjektStreckeElement(target);
						if(poStrecke != null) {
							dp.addContent(poStrecke);
						}
						dp.addContent(createDpBezugBetrieblElement("unmittelbar", signalId));
						dp.addContent(createDpAllgElement("ETCS", "beide", "Datenpunkt an Ausfahrsignalen (kombiniert) (Typ 21)", 3, "Signal"));
						int[] types = {21};
						dp.addContent(createTypEtcsElement(types));
						Logger.log("--combining with " + printSignalBezeichnung(current.punktObjektElement) + " (km " + printKmValue(current.punktObjektElement) + ")");
						Logger.log("--placing DP (combined) at km " + printKmValue(dp));
						containerElement.addContent(dp);
						containerElement.addContent(createBalise(guid, 1));
						containerElement.addContent(createBalise(guid, 2));
						alreadyHandledSignals.add(current.punktObjektElement);
						alreadyPlacedDp = true;
						break; // there should be no other path to be equipped with DP 21
					}
				}
				if(!alreadyPlacedDp) {
					placeDp21Regular(currentObject);
				}
			}
		}
	}
	
	
	/**
	 * Contains the placement rules for datapoint type 21 (Regular case) (ETCS Level 2).
	 * 
	 * 
	 * @param currentObject DOM element of the reference point for the datapoint
	 */
	private void placeDp21Regular(Element currentObject) {
		Element containerElement = ppm.getContainerElement();
		String signalId = currentObject.getChild("Identitaet").getChild("Wert").getText();
		List<PunktObjekt> nextposlist = ppm.calculatePosition(PunktObjekt.valueOf(currentObject), -6000);
		for(int j = 0; j < nextposlist.size(); j++) {
			PunktObjekt nextpos = nextposlist.get(j);
			PunktObjektTopKante nextpotk = nextpos.punktObjektTopKante[0];
			Element dp = new Element("Datenpunkt");
			String guid = generateGuid();
			dp.addContent(createIdentitaetElement(guid));
			dp.addContent(createBasisObjektElement(LocalDate.now()));
			dp.addContent(createPunktObjektTopKanteElement(nextpotk.idTopKante, nextpotk.abstand, 0, nextpotk.wirkrichtung));
			Element poStrecke = createPunktObjektStreckeElement(nextpos);
			if(poStrecke != null) {
				dp.addContent(poStrecke);
			}
			dp.addContent(createDpBezugBetrieblElement("unmittelbar", signalId));
			dp.addContent(createDpAllgElement("ETCS", nextpotk.wirkrichtung, "Datenpunkt an Ausfahrsignalen (Typ 21)", 3, "Signal"));
			int[] types = {21};
			dp.addContent(createTypEtcsElement(types));
			Logger.log("--placing DP at km " + printKmValue(dp));
			containerElement.addContent(dp);
			containerElement.addContent(createBalise(guid, 1));
			containerElement.addContent(createBalise(guid, 2));
		}
	}
	
	
	/**
	 * Contains the placement rules for datapoint type 22 (Back-to-back case) (ETCS Level 2).
	 * Two signals are considered as back-to-back if they are facing in opposite direction
	 * and have a distance of less than 500 mm.
	 */
	private void placeDp22() {
		Logger.log("placing DP 22...");
		List<Element> alreadyHandledSignals = new ArrayList<Element>();
		Element containerElement = ppm.getContainerElement();
		List<Element> objectList = containerElement.getChildren();
		for(int i = 0; i < objectList.size(); i++) {
			Element currentObject = objectList.get(i);
			if(!currentObject.getName().equals("Signal")) {
				continue;
			}
			if(alreadyHandledSignals.contains(currentObject)) {
				Logger.log("-Signal " + printSignalBezeichnung(currentObject) + " (km " + printKmValue(currentObject) + ")");
				Logger.log("--already handled, no DP required");
				continue;
			}
			Evaluable aspectCond = new SignalAspectCondition(ppm, "Oz_Bk");
			if(aspectCond.evaluate(currentObject)) {
				Logger.log("-Signal " + printSignalBezeichnung(currentObject) + " (km " + printKmValue(currentObject) + ")");
				String signalId = currentObject.getChild("Identitaet").getChild("Wert").getText();
				List<NextPunktObjektPathResult> otherSignalsForward = ppm.getNextPunktObjektPaths(PunktObjekt.valueOf(currentObject), aspectCond, Direction.OPPOSITE, true);
				List<NextPunktObjektPathResult> otherSignalsBackward = ppm.getNextPunktObjektPaths(PunktObjekt.valueOf(currentObject), aspectCond, Direction.OPPOSITE, false);
				boolean alreadyPlacedDp = false;
				for(int i1 = 0; i1 < otherSignalsForward.size(); i1++) {
					NextPunktObjektPathResult current = otherSignalsForward.get(i1);
					if (current.distance <= 500) {
						// place DP 22 on the common position of both block markers
						PunktObjekt target = ppm.calculatePositionOnPath(PunktObjekt.valueOf(currentObject), current.topKantenList, 0, false);
						PunktObjektTopKante nextpotk = target.punktObjektTopKante[0];
						Element dp = new Element("Datenpunkt");
						String guid = generateGuid();
						dp.addContent(createIdentitaetElement(guid));
						dp.addContent(createBasisObjektElement(LocalDate.now()));
						dp.addContent(createPunktObjektTopKanteElement(nextpotk.idTopKante, nextpotk.abstand, 0, nextpotk.wirkrichtung));
						Element poStrecke = createPunktObjektStreckeElement(target);
						if(poStrecke != null) {
							dp.addContent(poStrecke);
						}
						dp.addContent(createDpBezugBetrieblElement("unmittelbar", signalId));
						dp.addContent(createDpAllgElement("ETCS", "beide", "Datenpunkt an Blockkennzeichen (kombiniert) (Typ 22)", 0, "Signal"));
						int[] types = {22};
						dp.addContent(createTypEtcsElement(types));
						Logger.log("--combining with " + printSignalBezeichnung(current.punktObjektElement) + " (km " + printKmValue(current.punktObjektElement) + ")");
						Logger.log("--placing DP (combined) at km " + printKmValue(dp));
						containerElement.addContent(dp);
						containerElement.addContent(createBalise(guid, 1));
						alreadyHandledSignals.add(current.punktObjektElement);
						alreadyPlacedDp = true;
						break; // there should be no other path to be equipped with DP 22
					}
				}
				for(int i1 = 0; !alreadyPlacedDp && i1 < otherSignalsBackward.size(); i1++) {
					NextPunktObjektPathResult current = otherSignalsBackward.get(i1);
					if (current.distance <= 500) {
						// place DP 22 on the common position of both block markers
						PunktObjekt target = ppm.calculatePositionOnPath(PunktObjekt.valueOf(currentObject), current.topKantenList, 0, false);
						PunktObjektTopKante nextpotk = target.punktObjektTopKante[0];
						Element dp = new Element("Datenpunkt");
						String guid = generateGuid();
						dp.addContent(createIdentitaetElement(guid));
						dp.addContent(createBasisObjektElement(LocalDate.now()));
						dp.addContent(createPunktObjektTopKanteElement(nextpotk.idTopKante, nextpotk.abstand, 0, nextpotk.wirkrichtung));
						Element poStrecke = createPunktObjektStreckeElement(target);
						if(poStrecke != null) {
							dp.addContent(poStrecke);
						}
						dp.addContent(createDpBezugBetrieblElement("unmittelbar", signalId));
						dp.addContent(createDpAllgElement("ETCS", "beide", "Datenpunkt an Blockkennzeichen (kombiniert) (Typ 22)", 0, "Signal"));
						int[] types = {22};
						dp.addContent(createTypEtcsElement(types));
						Logger.log("--combining with " + printSignalBezeichnung(current.punktObjektElement) + " (km " + printKmValue(current.punktObjektElement) + ")");
						Logger.log("--placing DP (combined) at km " + printKmValue(dp));
						containerElement.addContent(dp);
						containerElement.addContent(createBalise(guid, 1));
						alreadyHandledSignals.add(current.punktObjektElement);
						alreadyPlacedDp = true;
						break; // there should be no other path to be equipped with DP 22
					}
				}
				if(!alreadyPlacedDp) {
					placeDp22Regular(currentObject);
				}
			}
		}
	}
	
	
	/**
	 * Contains the placement rules for datapoint type 22 (Regular case) (ETCS Level 2).
	 * 
	 * 
	 * @param currentObject DOM element of the reference point for the datapoint
	 */
	private void placeDp22Regular(Element currentObject) {
		Element containerElement = ppm.getContainerElement();
		String signalId = currentObject.getChild("Identitaet").getChild("Wert").getText();
		List<PunktObjekt> nextposlist = ppm.calculatePosition(PunktObjekt.valueOf(currentObject), 0);
		for(int j = 0; j < nextposlist.size(); j++) {
			PunktObjekt nextpos = nextposlist.get(j);
			PunktObjektTopKante nextpotk = nextpos.punktObjektTopKante[0];
			Element dp = new Element("Datenpunkt");
			String guid = generateGuid();
			dp.addContent(createIdentitaetElement(guid));
			dp.addContent(createBasisObjektElement(LocalDate.now()));
			dp.addContent(createPunktObjektTopKanteElement(nextpotk.idTopKante, nextpotk.abstand, 0, nextpotk.wirkrichtung));
			Element poStrecke = createPunktObjektStreckeElement(nextpos);
			if(poStrecke != null) {
				dp.addContent(poStrecke);
			}
			dp.addContent(createDpBezugBetrieblElement("unmittelbar", signalId));
			dp.addContent(createDpAllgElement("ETCS", nextpotk.wirkrichtung, "Datenpunkt an Blockkennzeichen (Typ 22)", 0, "Signal"));
			int[] types = {22};
			dp.addContent(createTypEtcsElement(types));
			Logger.log("--placing DP at km " + printKmValue(dp));
			containerElement.addContent(dp);
			containerElement.addContent(createBalise(guid, 1));
		}
	}
	
	
	/**
	 * Contains the placement rules for datapoint type 23 (Special cases) (ETCS Level 2).
	 * These special rules will be applied if there are two signals, facing towards each other
	 * and with a distance between 250 m and 350 m or between 350 m and 700 m.
	 */
	private void placeDp23() {
		Logger.log("placing DP 23...");
		List<Element> alreadyHandledSignals = new ArrayList<Element>();
		Element containerElement = ppm.getContainerElement();
		List<Element> objectList = containerElement.getChildren();
		for(int i = 0; i < objectList.size(); i++) {
			Element currentObject = objectList.get(i);
			if(!currentObject.getName().equals("Signal")) {
				continue;
			}
			if(alreadyHandledSignals.contains(currentObject)) {
				Logger.log("-Signal " + printSignalBezeichnung(currentObject) + " (km " + printKmValue(currentObject) + ")");
				Logger.log("--already handled, no DP required");
				continue;
			}
			if(currentObject.getChild("Signal_Real") == null) {
				continue;
			}
			if(currentObject.getChild("Signal_Real").getChild("Signal_Real_Aktiv_Schirm") == null) {
				continue;
			}
			String signalArt = currentObject.getChild("Signal_Real").getChild("Signal_Real_Aktiv_Schirm").getChild("Signal_Art").getChild("Wert").getText();
			if(signalArt.equals("Hauptsignal") || signalArt.equals("Hauptsperrsignal") || signalArt.equals("Mehrabschnittssignal") || signalArt.equals("Mehrabschnittssperrsignal") ||
					signalArt.equals("Zugdeckungssignal")) {
				Logger.log("-Signal " + printSignalBezeichnung(currentObject) + " (km " + printKmValue(currentObject) + ")");
				String signalId = currentObject.getChild("Identitaet").getChild("Wert").getText();
				Evaluable cond1 = new StringCondition("Signal_Real/Signal_Real_Aktiv_Schirm/Signal_Art/Wert", Operator.EQUAL, "Hauptsignal");
				Evaluable cond2 = new StringCondition("Signal_Real/Signal_Real_Aktiv_Schirm/Signal_Art/Wert", Operator.EQUAL, "Hauptsperrsignal");
				Evaluable cond3 = new StringCondition("Signal_Real/Signal_Real_Aktiv_Schirm/Signal_Art/Wert", Operator.EQUAL, "Mehrabschnittssignal");
				Evaluable cond4 = new StringCondition("Signal_Real/Signal_Real_Aktiv_Schirm/Signal_Art/Wert", Operator.EQUAL, "Mehrabschnittssperrsignal");
				Evaluable cond5 = new StringCondition("Signal_Real/Signal_Real_Aktiv_Schirm/Signal_Art/Wert", Operator.EQUAL, "Zugdeckungssignal");
				ConditionDisjunction disjunc = new ConditionDisjunction(cond1, cond2, cond3, cond4, cond5);
				List<NextPunktObjektPathResult> otherSignals = ppm.getNextPunktObjektPaths(PunktObjekt.valueOf(currentObject), disjunc, Direction.OPPOSITE, false);
				if(otherSignals.size() > 0) {
					for(int i1 = 0; i1 < otherSignals.size(); i1++) {
						NextPunktObjektPathResult current = otherSignals.get(i1);
						if (current.distance >= 250000 && current.distance <= 350000) {
							// do not place any DP 23
							Logger.log("--combining with " + printSignalBezeichnung(current.punktObjektElement) + " (km " + printKmValue(current.punktObjektElement) + ")");
							Logger.log("--short distance, no DP required");
							alreadyHandledSignals.add(current.punktObjektElement);
							break; // there should be no other path to be equipped with DP 23
						}
						else if(current.distance > 350000 && current.distance <= 700000) {
							// place DP 23 centered between both signals
							PunktObjekt target = ppm.calculatePositionOnPath(PunktObjekt.valueOf(currentObject), current.topKantenList, current.distance / 2, false);
							PunktObjektTopKante nextpotk = target.punktObjektTopKante[0];
							Element dp = new Element("Datenpunkt");
							String guid = generateGuid();
							dp.addContent(createIdentitaetElement(guid));
							dp.addContent(createBasisObjektElement(LocalDate.now()));
							dp.addContent(createPunktObjektTopKanteElement(nextpotk.idTopKante, nextpotk.abstand, 0, nextpotk.wirkrichtung));
							Element poStrecke = createPunktObjektStreckeElement(target);
							if(poStrecke != null) {
								dp.addContent(poStrecke);
							}
							dp.addContent(createDpBezugBetrieblElement("mittelbar", signalId));
							dp.addContent(createDpAllgElement("ETCS", "keine", "Erster Ortungs-Datenpunkt vor Signalen (kombiniert) (Typ 23)", 0, "Signal Gleis"));
							int[] types = {23};
							dp.addContent(createTypEtcsElement(types));
							Logger.log("--combining with " + printSignalBezeichnung(current.punktObjektElement) + " (km " + printKmValue(current.punktObjektElement) + ")");
							Logger.log("--placing DP (combined) at km " + printKmValue(dp));
							containerElement.addContent(dp);
							containerElement.addContent(createBalise(guid, 1));
							alreadyHandledSignals.add(current.punktObjektElement);
							break; // there should be no other path to be equipped with DP 23
						}
						else {
							placeDp23Regular(currentObject);
						}
					}
				}
				else {
					placeDp23Regular(currentObject);
				}
			}
		}
	}
	
	
	/**
	 * Contains the placement rules for datapoint type 23 (Regular case) (ETCS Level 2).
	 * 
	 * 
	 * @param currentObject DOM element of the reference point for the datapoint
	 */
	private void placeDp23Regular(Element currentObject) {
		Element containerElement = ppm.getContainerElement();
		String signalId = currentObject.getChild("Identitaet").getChild("Wert").getText();
		List<PunktObjekt> nextposlist = ppm.calculatePosition(PunktObjekt.valueOf(currentObject), -300000);
		for(int j = 0; j < nextposlist.size(); j++) {
			PunktObjekt nextpos = nextposlist.get(j);
			PunktObjektTopKante nextpotk = nextpos.punktObjektTopKante[0];
			Element dp = new Element("Datenpunkt");
			String guid = generateGuid();
			dp.addContent(createIdentitaetElement(guid));
			dp.addContent(createBasisObjektElement(LocalDate.now()));
			dp.addContent(createPunktObjektTopKanteElement(nextpotk.idTopKante, nextpotk.abstand, 0, nextpotk.wirkrichtung));
			Element poStrecke = createPunktObjektStreckeElement(nextpos);
			if(poStrecke != null) {
				dp.addContent(poStrecke);
			}
			dp.addContent(createDpBezugBetrieblElement("mittelbar", signalId));
			int dpNr = j + 1;
			dp.addContent(createDpAllgElement("ETCS", "keine", "Erster Ortungs-Datenpunkt vor Signalen " + dpNr + "/" + nextposlist.size() + " (Typ 23)", 0, "Signal Gleis"));
			int[] types = {23};
			dp.addContent(createTypEtcsElement(types));
			Logger.log("--placing DP at km " + printKmValue(dp));
			containerElement.addContent(dp);
			containerElement.addContent(createBalise(guid, 1));
		}
	}
	
	
	/**
	 * Contains the placement rules for datapoint type 24 (ETCS Level 2).
	 */
	private void placeDp24() {
		Logger.log("placing DP 24...");
		Element containerElement = ppm.getContainerElement();
		List<Element> objectList = containerElement.getChildren();
		for(int i = 0; i < objectList.size(); i++) {
			Element currentObject = objectList.get(i);
			if(!currentObject.getName().equals("Signal")) {
				continue;
			}
			if(alreadyHandledDp24.contains(currentObject)) {
				Logger.log("-Signal " + printSignalBezeichnung(currentObject) + " (km " + printKmValue(currentObject) + ")");
				Logger.log("--already handled, no DP required");
				continue;
			}
			if(currentObject.getChild("Signal_Real") == null) {
				continue;
			}
			if(currentObject.getChild("Signal_Real").getChild("Signal_Real_Aktiv") == null) {
				continue;
			}
			String signalFunc = currentObject.getChild("Signal_Real").getChild("Signal_Real_Aktiv").getChild("Signal_Funktion").getChild("Wert").getText();
			// all variants of Ausfahr_Signal already handled with DP 28
			
			if(signalFunc.equals("Ausfahr_Signal") || signalFunc.equals("Ausfahr_Zwischen_Signal") ||
					signalFunc.equals("Einfahr_Ausfahr_Signal") || signalFunc.equals("Gruppenausfahr_Gruppenzwischen_Signal") ||
					signalFunc.equals("Gruppenausfahr_Signal") || signalFunc.equals("Gruppenzwischen_Signal") ||
					signalFunc.equals("Zugdeckungs_Signal") || signalFunc.equals("Zwischen_Signal")) {
				Logger.log("-Signal " + printSignalBezeichnung(currentObject) + " (km " + printKmValue(currentObject) + ")");
				String signalId = currentObject.getChild("Identitaet").getChild("Wert").getText();
				List<PunktObjekt> nextposlist = ppm.calculatePosition(PunktObjekt.valueOf(currentObject), -50000);
				for(int j = 0; j < nextposlist.size(); j++) {
					PunktObjekt nextpos = nextposlist.get(j);
					PunktObjektTopKante nextpotk = nextpos.punktObjektTopKante[0];
					Element dp = new Element("Datenpunkt");
					String guid = generateGuid();
					dp.addContent(createIdentitaetElement(guid));
					dp.addContent(createBasisObjektElement(LocalDate.now()));
					dp.addContent(createPunktObjektTopKanteElement(nextpotk.idTopKante, nextpotk.abstand, 0, nextpotk.wirkrichtung));
					Element poStrecke = createPunktObjektStreckeElement(nextpos);
					if(poStrecke != null) {
						dp.addContent(poStrecke);
					}
					dp.addContent(createDpBezugBetrieblElement("mittelbar", signalId));
					int dpNr = j + 1;
					dp.addContent(createDpAllgElement("ETCS", "keine", "Zweiter Ortungs-Datenpunkt vor Signalen " + dpNr + "/" + nextposlist.size() + " (Typ 24)", 0, "Signal Gleis"));
					int[] types = {24};
					dp.addContent(createTypEtcsElement(types));
					Logger.log("--placing DP at km " + printKmValue(dp));
					containerElement.addContent(dp);
					containerElement.addContent(createBalise(guid, 1));
				}
			}
		}
	}
	
	
	/**
	 * Contains the placement rules for datapoint type 25 (Faulty switch use case) (ETCS Level 2).
	 */
	private void placeDp25FaultySwitch() {
		Logger.log("placing DP 25 (faulty switch)...");
	}
	
	
	/**
	 * Contains the placement rules for datapoint type 25 (Gap fill use case) (ETCS Level 2).
	 */
	private void placeDp25GapFill() {
		Logger.log("placing DP 25 (gap fill)...");
		boolean finished = false;
		boolean modified = false;
		while(!finished) {
			Element containerElement = ppm.getContainerElement();
			List<Element> objectList = containerElement.getChildren();
			for(int i = 0; i < objectList.size() && !modified; i++) {
				Element currentObject = objectList.get(i);
				if(!currentObject.getName().equals("Datenpunkt")) {
					continue;
				}
				int dpType = Integer.parseInt(currentObject.getChild("DP_Typ").getChild("DP_Typ_GETCS").getChild("DP_Typ_ETCS").getChild("Wert").getText());
				if(dpType == 26 || dpType == 32 || dpType == 34 || dpType == 36 || dpType == 37) {
					continue;
				}
				Evaluable typeCond = new TypeCondition("Datenpunkt");
				Evaluable cond1 = new IntegerCondition("DP_Typ/DP_Typ_GETCS/DP_Typ_ETCS/Wert", Operator.NOT_EQUAL, 26);
				Evaluable cond2 = new IntegerCondition("DP_Typ/DP_Typ_GETCS/DP_Typ_ETCS/Wert", Operator.NOT_EQUAL, 32);
				Evaluable cond3 = new IntegerCondition("DP_Typ/DP_Typ_GETCS/DP_Typ_ETCS/Wert", Operator.NOT_EQUAL, 34);
				Evaluable cond4 = new IntegerCondition("DP_Typ/DP_Typ_GETCS/DP_Typ_ETCS/Wert", Operator.NOT_EQUAL, 36);
				Evaluable cond5 = new IntegerCondition("DP_Typ/DP_Typ_GETCS/DP_Typ_ETCS/Wert", Operator.NOT_EQUAL, 37);
				ConditionConjunction conjunc = new ConditionConjunction(typeCond, cond1, cond2, cond3, cond4, cond5);
				List<NextPunktObjektPathResult> nextlistForward = ppm.getNextPunktObjektPaths(PunktObjekt.valueOf(currentObject), conjunc, Direction.BOTH, true);
				List<NextPunktObjektPathResult> nextlistReverse = ppm.getNextPunktObjektPaths(PunktObjekt.valueOf(currentObject), conjunc, Direction.BOTH, false);
				List<Element> nodelist = null;
				//Element maxDp = null;
				int maxDist = 0;
				boolean searchForward = true;
				for(int j = 0; j < nextlistForward.size(); j++) {
					NextPunktObjektPathResult nextDp = nextlistForward.get(j);
					int tempdist = nextDp.distance;
					if(tempdist > maxDist) {
						maxDist = tempdist;
						nodelist = nextDp.topKantenList;
						//maxDp = nextDp.punktObjektElement;
					}
				}
				for(int j = 0; j < nextlistReverse.size(); j++) {
					NextPunktObjektPathResult nextDp = nextlistReverse.get(j);
					int tempdist = nextDp.distance;
					if(tempdist > maxDist) {
						maxDist = tempdist;
						nodelist = nextDp.topKantenList;
						//maxDp = nextDp.punktObjektElement;
						searchForward = false;
					}
				}
				if(maxDist > 1800000) {
					int segmentCount = 1;
					while(maxDist / segmentCount > 1800) {
						segmentCount++;
					}
					int segmentLength = maxDist / segmentCount;
					PunktObjekt nextpos = ppm.calculatePositionOnPath(PunktObjekt.valueOf(currentObject), nodelist, segmentLength * (segmentCount - 1), searchForward);
					PunktObjektTopKante nextpotk = nextpos.punktObjektTopKante[0];
					Element dp = new Element("Datenpunkt");
					String guid = generateGuid();
					dp.addContent(createIdentitaetElement(guid));
					dp.addContent(createBasisObjektElement(LocalDate.now()));
					dp.addContent(createPunktObjektTopKanteElement(nextpotk.idTopKante, nextpotk.abstand, 0, nextpotk.wirkrichtung));
					Element poStrecke = createPunktObjektStreckeElement(nextpos);
					if(poStrecke != null) {
						dp.addContent(poStrecke);
					}
					dp.addContent(createDpAllgElement("ETCS", "keine", "Allgemeiner Ortungs-Datenpunkt (Typ 25)", 0, "Streckenkilometer Gleis"));
					int[] types = {25};
					dp.addContent(createTypEtcsElement(types));
					Logger.log("--placing DP at km " + printKmValue(dp));
					containerElement.addContent(dp);
					containerElement.addContent(createBalise(guid, 1));
					modified = true;
				}
			}
			if(!modified) {
				finished = true;
			}
			else {
				modified = false;
			}
		}
	}
	
	
	/**
	 * Contains the placement rules for datapoint type 26 (ETCS Level 2).
	 */
	private void placeDp26() {
		Logger.log("placing DP 26...");
		Element containerElement = ppm.getContainerElement();
		List<Element> objectList = containerElement.getChildren();
		for(int i = 0; i < objectList.size(); i++) {
			Element currentObject = objectList.get(i);
			if(!currentObject.getName().equals("Signal")) {
				continue;
			}
			if(currentObject.getChild("Signal_Real") == null) {
				continue;
			}
			if(currentObject.getChild("Signal_Real").getChild("Signal_Real_Aktiv_Schirm") == null) {
				continue;
			}
			String signalArt = currentObject.getChild("Signal_Real").getChild("Signal_Real_Aktiv_Schirm").getChild("Signal_Art").getChild("Wert").getText();
			if(signalArt.equals("Hauptsignal") || signalArt.equals("Hauptsperrsignal") || signalArt.equals("Mehrabschnittssignal") ||
					signalArt.equals("Mehrabschnittssperrsignal") || signalArt.equals("Zugdeckungssignal")) {
				
				Logger.log("-Signal " + printSignalBezeichnung(currentObject) + " (km " + printKmValue(currentObject) + ")");
				int distanceSignalDangerPoint = calculateDistanceSignalDangerPoint(currentObject);
				Logger.debug("--dangerPoint=" + distanceSignalDangerPoint);
				if(distanceSignalDangerPoint < 0 || distanceSignalDangerPoint >= 325000) {
					continue;
					
				}
				double gradient = 0; // TODO
				double vmax = calculateVmax(currentObject);
				int distPrecedingSignal = 999999999;
				Element elemPrecedingSignal = null;
				
				Evaluable cond1 = new StringCondition("Signal_Real/Signal_Real_Aktiv_Schirm/Signal_Art/Wert", Operator.EQUAL, "Hauptsignal");
				Evaluable cond2 = new StringCondition("Signal_Real/Signal_Real_Aktiv_Schirm/Signal_Art/Wert", Operator.EQUAL, "Hauptsperrsignal");
				Evaluable cond3 = new StringCondition("Signal_Real/Signal_Real_Aktiv_Schirm/Signal_Art/Wert", Operator.EQUAL, "Mehrabschnittssignal");
				Evaluable cond4 = new StringCondition("Signal_Real/Signal_Real_Aktiv_Schirm/Signal_Art/Wert", Operator.EQUAL, "Mehrabschnittssperrsignal");
				Evaluable cond5 = new StringCondition("Signal_Real/Signal_Real_Aktiv_Schirm/Signal_Art/Wert", Operator.EQUAL, "Zugdeckungssignal");
				ConditionDisjunction disjunc = new ConditionDisjunction(cond1, cond2, cond3, cond4, cond5);
				List<NextPunktObjektPathResult> signallist = ppm.getNextPunktObjektPaths(PunktObjekt.valueOf(currentObject), disjunc, Direction.EQUAL, false);
				NextPunktObjektPathResult nearestsignal = NextPunktObjektPathResult.nearest(signallist);
				if(nearestsignal != null ) {
					distPrecedingSignal = nearestsignal.distance;
					elemPrecedingSignal = nearestsignal.punktObjektElement;
					Logger.debug("--precSignal=" + elemPrecedingSignal.getChild("Bezeichnung").getChild("Bezeichnung_Lageplan_Lang").getChild("Wert").getText()
							+ ", dist=" + distPrecedingSignal);
				}
				
				
				
				int resultingDistance = 0;
				
				String signalId = currentObject.getChild("Identitaet").getChild("Wert").getText();
				
				if(gradient < -15) {
					resultingDistance = 780000;
				}
				else if(gradient < 0) {
					resultingDistance = 750000;
				}
				else { // gradient >= 0
					resultingDistance = 720000;
				}
				
				if(distPrecedingSignal < resultingDistance) {
					resultingDistance -= Math.min(distanceSignalDangerPoint, 100000);
				}
				if(distPrecedingSignal < resultingDistance) {
					double speedfactor = 2.6 + 0.2 * toM_s(vmax);
					speedfactor *= 1000.0;
					int additionalDistance = Math.min(distPrecedingSignal - ((int) speedfactor), 100000);
					List<PunktObjekt> nextposlist = ppm.calculatePosition(PunktObjekt.valueOf(currentObject), - additionalDistance);
					for(int j = 0; j < nextposlist.size(); j++) {
						PunktObjekt nextpos = nextposlist.get(j);
						PunktObjektTopKante nextpotk = nextpos.punktObjektTopKante[0];
						Element dp = new Element("Datenpunkt");
						String guid = generateGuid();
						dp.addContent(createIdentitaetElement(guid));
						dp.addContent(createBasisObjektElement(LocalDate.now()));
						dp.addContent(createPunktObjektTopKanteElement(nextpotk.idTopKante, nextpotk.abstand, 0, nextpotk.wirkrichtung));
						Element poStrecke = createPunktObjektStreckeElement(nextpos);
						if(poStrecke != null) {
							dp.addContent(poStrecke);
						}
						dp.addContent(createDpBezugBetrieblElement("mittelbar", signalId));
						int dpNr = j + 1;
						dp.addContent(createDpAllgElement("ETCS", nextpotk.wirkrichtung, "TSR-Datenpunkt " + dpNr + "/" + nextposlist.size() + " (Typ 26)", 3, "Signal Gleis"));
						int[] types = {26};
						dp.addContent(createTypEtcsElement(types));
						Logger.log("--placing DP at km " + printKmValue(dp));
						containerElement.addContent(dp);
						containerElement.addContent(createBalise(guid, 1));
						containerElement.addContent(createBalise(guid, 2));
					}
				}
				
				
				List<PunktObjekt> nextposlist = ppm.calculatePosition(PunktObjekt.valueOf(currentObject), - resultingDistance);
				for(int j = 0; j < nextposlist.size(); j++) {
					PunktObjekt nextpos = nextposlist.get(j);
					PunktObjektTopKante nextpotk = nextpos.punktObjektTopKante[0];
					Element dp = new Element("Datenpunkt");
					String guid = generateGuid();
					dp.addContent(createIdentitaetElement(guid));
					dp.addContent(createBasisObjektElement(LocalDate.now()));
					dp.addContent(createPunktObjektTopKanteElement(nextpotk.idTopKante, nextpotk.abstand, 0, nextpotk.wirkrichtung));
					Element poStrecke = createPunktObjektStreckeElement(nextpos);
					if(poStrecke != null) {
						dp.addContent(poStrecke);
					}
					dp.addContent(createDpBezugBetrieblElement("mittelbar", signalId));
					int dpNr = j + 1;
					dp.addContent(createDpAllgElement("ETCS", nextpotk.wirkrichtung, "TSR-Datenpunkt " + dpNr + "/" + nextposlist.size() + " (Typ 26)", 3, "Signal Gleis"));
					int[] types = {26};
					dp.addContent(createTypEtcsElement(types));
					Logger.log("--placing DP at km " + printKmValue(dp));
					containerElement.addContent(dp);
					containerElement.addContent(createBalise(guid, 1));
					containerElement.addContent(createBalise(guid, 2));
				}
			}
		}
	}
	
	
	/**
	 * Contains the placement rules for datapoint type 28 (ETCS Level 2).
	 */
	private void placeDp28() {
		Logger.log("placing DP 28...");
		Element containerElement = ppm.getContainerElement();
		List<Element> objectList = containerElement.getChildren();
		for(int i = 0; i < objectList.size(); i++) {
			Element currentObject = objectList.get(i);
			if(!currentObject.getName().equals("Signal")) {
				continue;
			}
			if(currentObject.getChild("Signal_Real") == null) {
				continue;
			}
			if(currentObject.getChild("Signal_Real").getChild("Signal_Real_Aktiv") == null) {
				continue;
			}
			String signalFunc = currentObject.getChild("Signal_Real").getChild("Signal_Real_Aktiv").getChild("Signal_Funktion").getChild("Wert").getText();
			if(signalFunc.equals("Ausfahr_Signal") || signalFunc.equals("Ausfahr_Zwischen_Signal") ||
					signalFunc.equals("Einfahr_Ausfahr_Signal") || signalFunc.equals("Gruppenausfahr_Gruppenzwischen_Signal") ||
					signalFunc.equals("Gruppenausfahr_Signal")) {
				Logger.log("-Signal " + printSignalBezeichnung(currentObject) + " (km " + printKmValue(currentObject) + ")");
				int distanceSignalStumpfeWeiche = calculateDistanceSignalStumpfeWeiche(currentObject);
				if(distanceSignalStumpfeWeiche < 0) {
					distanceSignalStumpfeWeiche = 999999999;
				}
				int distanceSignalDangerPoint = calculateDistanceSignalDangerPoint(currentObject);
				if(distanceSignalDangerPoint < 0) {
					distanceSignalDangerPoint = 999999999;
				}
				double vmax = calculateVmax(currentObject);
				
				Logger.debug("--distSW=" + distanceSignalStumpfeWeiche + ", distDP=" + distanceSignalDangerPoint + ", vmax=" + vmax);
				
				int[] singleDpList = {};
				int[] twiceDpList = {};
				int[] combinedDpList = {};
				
				String signalId = currentObject.getChild("Identitaet").getChild("Wert").getText();
				
				// Tabelle 14
				if(vmax <= 160 && distanceSignalStumpfeWeiche < 25000) {
					if(distanceSignalDangerPoint < 30000 && vmax <= 80) {
						singleDpList = new int[] {-12000};
					}
					else {
						singleDpList = new int[] {};
					}
					twiceDpList = new int[] {-18000, -33000, -50000, -77000, -100000, -125000};
					combinedDpList = new int[] {-65000};
				}
				else if(vmax <= 160 && distanceSignalStumpfeWeiche <= 50000) {
					if(distanceSignalDangerPoint < 30000 && vmax <= 80) {
						singleDpList = new int[] {-12000, -77000};
					}
					else {
						singleDpList = new int[] {-77000};
					}
					twiceDpList = new int[] {-18000, -33000, -50000, -100000};
					combinedDpList = new int[] {-65000};
				}
				else if(vmax <= 160 && distanceSignalStumpfeWeiche <= 75000) {
					if(distanceSignalDangerPoint < 30000 && vmax <= 80) {
						singleDpList = new int[] {-12000};
					}
					else {
						singleDpList = new int[] {};
					}
					twiceDpList = new int[] {-18000, -33000, -50000, -77000};
					combinedDpList = new int[] {-65000};
				}
				else if(vmax <= 160 && distanceSignalStumpfeWeiche <= 92000) {
					if(distanceSignalDangerPoint < 30000 && vmax <= 80) {
						singleDpList = new int[] {-12000, -33000, -65000, -80000};
					}
					else if(distanceSignalDangerPoint < 30000 && vmax > 80) {
						singleDpList = new int[] {-33000, -65000, -80000};
					}
					else {
						singleDpList = new int[] {-33000, -65000};
					}
					twiceDpList = new int[] {-18000};
					combinedDpList = new int[] {-50000};
				}
				else if(vmax <= 160 && distanceSignalStumpfeWeiche <= 125000) {
					if(distanceSignalDangerPoint < 30000 && vmax <= 80) {
						singleDpList = new int[] {-12000, -18000, -33000, -80000};
					}
					else if(distanceSignalDangerPoint < 30000 && vmax > 80) {
						singleDpList = new int[] {-18000, -33000, -80000};
					}
					else {
						singleDpList = new int[] {-18000, -33000};
					}
					twiceDpList = new int[] {};
					combinedDpList = new int[] {-50000};
				}
				// Tabelle 15
				else if(vmax > 160 && distanceSignalStumpfeWeiche < 20000) {
					singleDpList = new int[] {-85000};
					twiceDpList = new int[] {-26000, -50000, -105000, -128000};
					combinedDpList = new int[] {-73000};
				}
				else if(vmax > 160 && distanceSignalStumpfeWeiche <= 40000) {
					singleDpList = new int[] {-85000};
					twiceDpList = new int[] {-26000, -50000, -105000};
					combinedDpList = new int[] {-73000};
				}
				else if(vmax > 160 && distanceSignalStumpfeWeiche <= 52000) {
					singleDpList = new int[] {-85000, -105000};
					twiceDpList = new int[] {-26000, -50000};
					combinedDpList = new int[] {-73000};
				}
				else if(vmax > 160 && distanceSignalStumpfeWeiche <= 75000) {
					singleDpList = new int[] {-85000};
					twiceDpList = new int[] {-26000, -50000};
					combinedDpList = new int[] {-73000};
				}
				else if(vmax > 160 && distanceSignalStumpfeWeiche <= 99000) {
					if(distanceSignalDangerPoint < 30000) {
						singleDpList = new int[] {-85000};
					}
					else {
						singleDpList = new int[] {};
					}
					twiceDpList = new int[] {-26000, -50000};
					combinedDpList = new int[] {-73000};
				}
				else if(vmax > 160 && distanceSignalStumpfeWeiche <= 125000) {
					if(distanceSignalDangerPoint < 30000) {
						singleDpList = new int[] {-80000};
					}
					else {
						singleDpList = new int[] {};
					}
					twiceDpList = new int[] {-26000};
					combinedDpList = new int[] {-50000};
				}
				// Tabelle 16
				else if(vmax <= 160 && distanceSignalStumpfeWeiche > 125000 && distanceSignalDangerPoint < 30000) {
					if(vmax <= 80) {
						singleDpList = new int[] {-12000, -18000, -30000, -80000};
					}
					else {
						singleDpList = new int[] {-18000, -30000, -80000};
					}
					twiceDpList = new int[] {};
					combinedDpList = new int[] {-50000};
				}
				else if(vmax <= 160 && distanceSignalStumpfeWeiche > 125000 && distanceSignalDangerPoint <= 62000) {
					singleDpList = new int[] {-18000, -30000};
					twiceDpList = new int[] {};
					combinedDpList = new int[] {-50000};
				}
				else if(vmax <= 160 && distanceSignalStumpfeWeiche > 125000 && distanceSignalDangerPoint <= 80000) {
					singleDpList = new int[] {-18000};
					twiceDpList = new int[] {};
					combinedDpList = new int[] {-50000};
				}
				// Tabelle 17
				else if(vmax > 160 && distanceSignalStumpfeWeiche > 125000 && distanceSignalDangerPoint < 30000) {
					singleDpList = new int[] {-26000, -80000};
					twiceDpList = new int[] {};
					combinedDpList = new int[] {-50000};
				}
				else if(vmax > 160 && distanceSignalStumpfeWeiche > 125000 && distanceSignalDangerPoint <= 80000) {
					singleDpList = new int[] {-26000};
					twiceDpList = new int[] {};
					combinedDpList = new int[] {-50000};
				}
				
				for(int dist : singleDpList) {
					List<PunktObjekt> nextposlist = ppm.calculatePosition(PunktObjekt.valueOf(currentObject), dist);
					for(int j = 0; j < nextposlist.size(); j++) {
						PunktObjekt nextpos = nextposlist.get(j);
						PunktObjektTopKante nextpotk = nextpos.punktObjektTopKante[0];
						Element dp = new Element("Datenpunkt");
						String guid = generateGuid();
						dp.addContent(createIdentitaetElement(guid));
						dp.addContent(createBasisObjektElement(LocalDate.now()));
						dp.addContent(createPunktObjektTopKanteElement(nextpotk.idTopKante, nextpotk.abstand, 0, nextpotk.wirkrichtung));
						Element poStrecke = createPunktObjektStreckeElement(nextpos);
						if(poStrecke != null) {
							dp.addContent(poStrecke);
						}
						dp.addContent(createDpBezugBetrieblElement("mittelbar", signalId));
						int dpNr = j + 1;
						dp.addContent(createDpAllgElement("ETCS", "keine", "Datenpunkt fuer Start of Mission " + dpNr + "/" + nextposlist.size() + " (Typ 28)", 0, "Signal Gleis"));
						int[] types = {28};
						dp.addContent(createTypEtcsElement(types));
						Logger.log("--placing DP at km " + printKmValue(dp));
						containerElement.addContent(dp);
						containerElement.addContent(createBalise(guid, 1));
					}
				}
				for(int dist : twiceDpList) {
					List<PunktObjekt> nextposlist = ppm.calculatePosition(PunktObjekt.valueOf(currentObject), dist);
					for(int j = 0; j < nextposlist.size(); j++) {
						PunktObjekt nextpos = nextposlist.get(j);
						PunktObjektTopKante nextpotk = nextpos.punktObjektTopKante[0];
						Element dp = new Element("Datenpunkt");
						String guid = generateGuid();
						dp.addContent(createIdentitaetElement(guid));
						dp.addContent(createBasisObjektElement(LocalDate.now()));
						dp.addContent(createPunktObjektTopKanteElement(nextpotk.idTopKante, nextpotk.abstand, 0, nextpotk.wirkrichtung));
						Element poStrecke = createPunktObjektStreckeElement(nextpos);
						if(poStrecke != null) {
							dp.addContent(poStrecke);
						}
						dp.addContent(createDpBezugBetrieblElement("mittelbar", signalId));
						int dpNr = j + 1;
						dp.addContent(createDpAllgElement("ETCS", "keine", "Datenpunkt fuer Start of Mission " + dpNr + "/" + nextposlist.size() + " (Typ 28)", 3, "Signal Gleis"));
						int[] types = {28};
						dp.addContent(createTypEtcsElement(types));
						Logger.log("--placing DP at km " + printKmValue(dp));
						containerElement.addContent(dp);
						containerElement.addContent(createBalise(guid, 1));
						containerElement.addContent(createBalise(guid, 2));
					}
				}
				for(int dist : combinedDpList) {
					List<PunktObjekt> nextposlist = ppm.calculatePosition(PunktObjekt.valueOf(currentObject), dist);
					for(int j = 0; j < nextposlist.size(); j++) {
						PunktObjekt nextpos = nextposlist.get(j);
						PunktObjektTopKante nextpotk = nextpos.punktObjektTopKante[0];
						Element dp = new Element("Datenpunkt");
						String guid = generateGuid();
						dp.addContent(createIdentitaetElement(guid));
						dp.addContent(createBasisObjektElement(LocalDate.now()));
						dp.addContent(createPunktObjektTopKanteElement(nextpotk.idTopKante, nextpotk.abstand, 0, nextpotk.wirkrichtung));
						Element poStrecke = createPunktObjektStreckeElement(nextpos);
						if(poStrecke != null) {
							dp.addContent(poStrecke);
						}
						dp.addContent(createDpBezugBetrieblElement("mittelbar", signalId));
						int dpNr = j + 1;
						dp.addContent(createDpAllgElement("ETCS", "keine", "Zweiter Ortungs-Datenpunkt vor Signalen/Datenpunkt fuer Start of Mission " + dpNr + "/" + nextposlist.size() + " (Typ 24/28)", 0, "Signal Gleis"));
						int[] types = {24, 28};
						dp.addContent(createTypEtcsElement(types));
						Logger.log("--placing DP (combined with DP 24) at km " + printKmValue(dp));
						containerElement.addContent(dp);
						containerElement.addContent(createBalise(guid, 1));
						alreadyHandledDp24.add(currentObject);
					}
				}
			}
		}
	}

}
