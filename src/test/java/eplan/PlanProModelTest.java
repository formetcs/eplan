package eplan;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import eplan.PlanProModel.Direction;
import eplan.conditions.Evaluable;
import eplan.conditions.TypeCondition;

class PlanProModelTest {
	
	private static  PlanProModel ppm;

	@BeforeAll
	static void setupModel() {
		ppm = new PlanProModel();
		try {
			ppm.readFile(PlanProModelTest.class.getResource("branches.ppxml"));
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	void testInitialization() {
		assertNotNull(ppm);
		assertTrue(ppm.getPlanProObjectList().size() > 0);
	}
	
	
	// Naming schema for UUIDs in file branches.ppxml:
	//
	// GEO_Knoten: 11111111-2222-3333-4444-00000011xxxx
	// GEO_Punkt:  11111111-2222-3333-4444-00000022xxxx
	// GEO_Kante:  11111111-2222-3333-4444-00000033xxxx
	// TOP_Knoten: 11111111-2222-3333-4444-00000044xxxx
	// TOP_Kante:  11111111-2222-3333-4444-00000055xxxx
	// Signal:     11111111-2222-3333-4444-00000066xxxx
	//
	// where xxxx is a consecutive decimal number
	
	
	@Test
	void testGetNextTopKante() {
		Element tka1 = ppm.getElementbyId("11111111-2222-3333-4444-000000550001");
		Element tka2 = ppm.getElementbyId("11111111-2222-3333-4444-000000550002");
		Element tka3 = ppm.getElementbyId("11111111-2222-3333-4444-000000550003");
		Element tka4 = ppm.getElementbyId("11111111-2222-3333-4444-000000550004");
		Element tka5 = ppm.getElementbyId("11111111-2222-3333-4444-000000550005");
		Element tka11 = ppm.getElementbyId("11111111-2222-3333-4444-000000550011");
		Element tka14 = ppm.getElementbyId("11111111-2222-3333-4444-000000550014");
		Element tka16 = ppm.getElementbyId("11111111-2222-3333-4444-000000550016");
		assertNotNull(tka1);
		assertNotNull(tka2);
		assertNotNull(tka3);
		assertNotNull(tka4);
		assertNotNull(tka5);
		assertNotNull(tka11);
		assertNotNull(tka14);
		assertNotNull(tka16);
		
		// TOP_Kante TKa3 in forward search direction
		// Result must be TKa2 backward
		List<NextTopKanteResult> resultlist = ppm.getNextTopKante(tka3, true);
		assertNotNull(resultlist);
		assertTrue(resultlist.size() == 1);
		NextTopKanteResult result = resultlist.get(0);
		assertEquals(tka2, result.topKanteElement);
		assertFalse(result.direction);
		
		// TOP_Kante TKa3 in backward search direction
		// Result must be TKa4 backward
		resultlist = ppm.getNextTopKante(tka3, false);
		assertNotNull(resultlist);
		assertTrue(resultlist.size() == 1);
		result = resultlist.get(0);
		assertEquals(tka4, result.topKanteElement);
		assertFalse(result.direction);
		
		// TOP_Kante TKa4 in forward search direction
		// Result must be TKa3 forward and TKa 16 backward
		resultlist = ppm.getNextTopKante(tka4, true);
		assertNotNull(resultlist);
		assertTrue(resultlist.size() == 2);
		NextTopKanteResult result0 = resultlist.get(0);
		NextTopKanteResult result1 = resultlist.get(1);
		assertNotEquals(result0, result1);
		assertTrue(result0.topKanteElement.equals(tka3) || result0.topKanteElement.equals(tka16));
		assertTrue(result1.topKanteElement.equals(tka3) || result1.topKanteElement.equals(tka16));
		assertFalse(result0.topKanteElement.equals(tka3) && result1.topKanteElement.equals(tka3));
		assertFalse(result0.topKanteElement.equals(tka16) && result1.topKanteElement.equals(tka16));
		if(result0.topKanteElement.equals(tka3)) {
			assertTrue(result0.direction);
			assertFalse(result1.direction);
		}
		else {
			assertFalse(result0.direction);
			assertTrue(result1.direction);
		}
		
		// TOP_Kante TKa4 in backward search direction
		// Result must be TKa5 forward and TKa11 forward
		resultlist = ppm.getNextTopKante(tka4, false);
		assertNotNull(resultlist);
		assertTrue(resultlist.size() == 2);
		result0 = resultlist.get(0);
		result1 = resultlist.get(1);
		assertNotEquals(result0, result1);
		assertTrue(result0.topKanteElement.equals(tka5) || result0.topKanteElement.equals(tka11));
		assertTrue(result1.topKanteElement.equals(tka5) || result1.topKanteElement.equals(tka11));
		assertFalse(result0.topKanteElement.equals(tka5) && result1.topKanteElement.equals(tka5));
		assertFalse(result0.topKanteElement.equals(tka11) && result1.topKanteElement.equals(tka11));
		if(result0.topKanteElement.equals(tka5)) {
			assertTrue(result0.direction);
			assertTrue(result1.direction);
		}
		else {
			assertTrue(result0.direction);
			assertTrue(result1.direction);
		}
		
		// TOP_Kante TKa1 in backward search direction
		// Result must be empty
		resultlist = ppm.getNextTopKante(tka1, false);
		assertNotNull(resultlist);
		assertTrue(resultlist.size() == 0);
		
		// TOP_Kante TKa14 in forward search direction
		// Result must be empty
		resultlist = ppm.getNextTopKante(tka14, true);
		assertNotNull(resultlist);
		assertTrue(resultlist.size() == 0);
	}
	
	@Test
	void testCalculatePosition() {
		Element s1 = ppm.getElementbyId("11111111-2222-3333-4444-000000660001");
		Element s4 = ppm.getElementbyId("11111111-2222-3333-4444-000000660004");
		Element s6 = ppm.getElementbyId("11111111-2222-3333-4444-000000660006");
		Element s13 = ppm.getElementbyId("11111111-2222-3333-4444-000000660013");
		String idTKa5 = "11111111-2222-3333-4444-000000550005";
		String idTKa14 = "11111111-2222-3333-4444-000000550014";
		String idTKa18 = "11111111-2222-3333-4444-000000550018";
		assertNotNull(s1);
		assertNotNull(s4);
		assertNotNull(s6);
		assertNotNull(s13);
		PunktObjekt poS1 = new PunktObjekt(s1);
		PunktObjekt poS4 = new PunktObjekt(s4);
		PunktObjekt poS6 = new PunktObjekt(s6);
		PunktObjekt poS13 = new PunktObjekt(s13);
		assertNotNull(poS1);
		assertNotNull(poS4);
		assertNotNull(poS6);
		assertNotNull(poS13);
		
		// Signal S6 100 m in effective direction
		// Result must be TKa14, 50 m distance, in edge direction
		List<PunktObjekt> resultlist = ppm.calculatePosition(poS6, 100000);
		assertNotNull(resultlist);
		assertTrue(resultlist.size() == 1);
		PunktObjekt result0 = resultlist.get(0);
		assertTrue(result0.punktObjektTopKante.length == 1);
		PunktObjektTopKante potk0 = result0.punktObjektTopKante[0];
		assertEquals(idTKa14, potk0.idTopKante);
		assertEquals("in", potk0.wirkrichtung);
		assertEquals(50000, potk0.abstand);
		
		// Signal S6 100 m against effective direction
		// Result must be TKa5, 50 m distance, in edge direction
		// and TKa18, 50 m distance, against edge direction
		resultlist = ppm.calculatePosition(poS6, -100000);
		assertNotNull(resultlist);
		assertTrue(resultlist.size() == 2);
		result0 = resultlist.get(0);
		PunktObjekt result1 = resultlist.get(1);
		assertTrue(result0.punktObjektTopKante.length == 1);
		assertTrue(result1.punktObjektTopKante.length == 1);
		potk0 = result0.punktObjektTopKante[0];
		PunktObjektTopKante potk1 = result1.punktObjektTopKante[0];
		assertNotEquals(potk0, potk1);
		assertTrue(potk0.idTopKante.equals(idTKa5) || potk0.idTopKante.equals(idTKa18));
		assertTrue(potk1.idTopKante.equals(idTKa5) || potk1.idTopKante.equals(idTKa18));
		assertFalse(potk0.idTopKante.equals(idTKa5) && potk1.idTopKante.equals(idTKa5));
		assertFalse(potk0.idTopKante.equals(idTKa18) && potk1.idTopKante.equals(idTKa18));
		if(potk0.idTopKante.equals(idTKa5)) {
			assertEquals("in", potk0.wirkrichtung);
			assertEquals(50000, potk0.abstand);
			assertEquals("gegen", potk1.wirkrichtung);
			assertEquals(50000, potk1.abstand);
		}
		else {
			assertEquals("gegen", potk0.wirkrichtung);
			assertEquals(50000, potk0.abstand);
			assertEquals("in", potk1.wirkrichtung);
			assertEquals(50000, potk1.abstand);
		}
		
		// Signal S6 200 m in effective direction
		// Result must be empty
		resultlist = ppm.calculatePosition(poS6, 200000);
		assertNotNull(resultlist);
		assertTrue(resultlist.size() == 0);
	}
	
	@Test
	void testCalculatePositionOnPath() {
		Element s1 = ppm.getElementbyId("11111111-2222-3333-4444-000000660001");
		Element s13 = ppm.getElementbyId("11111111-2222-3333-4444-000000660013");
		Element tka1 = ppm.getElementbyId("11111111-2222-3333-4444-000000550001");
		Element tka2 = ppm.getElementbyId("11111111-2222-3333-4444-000000550002");
		Element tka3 = ppm.getElementbyId("11111111-2222-3333-4444-000000550003");
		Element tka4 = ppm.getElementbyId("11111111-2222-3333-4444-000000550004");
		Element tka8 = ppm.getElementbyId("11111111-2222-3333-4444-000000550008");
		Element tka9 = ppm.getElementbyId("11111111-2222-3333-4444-000000550009");
		Element tka12 = ppm.getElementbyId("11111111-2222-3333-4444-000000550012");
		Element tka13 = ppm.getElementbyId("11111111-2222-3333-4444-000000550013");
		String idTKa4 = "11111111-2222-3333-4444-000000550004";
		String idTKa9 = "11111111-2222-3333-4444-000000550009";
		assertNotNull(s1);
		assertNotNull(s13);
		assertNotNull(tka1);
		assertNotNull(tka2);
		assertNotNull(tka3);
		assertNotNull(tka4);
		assertNotNull(tka8);
		assertNotNull(tka9);
		assertNotNull(tka12);
		assertNotNull(tka13);
		PunktObjekt poS1 = new PunktObjekt(s1);
		PunktObjekt poS13 = new PunktObjekt(s13);
		assertNotNull(poS1);
		assertNotNull(poS13);
		List<Element> inputList = new ArrayList<Element>();
		
		// Signal S1 300 m in effective direction, using path [TKa1,TKa2,TKa3,TKa4]
		// Result must be TKa4, 50 m distance, against edge direction
		inputList.add(tka1);
		inputList.add(tka2);
		inputList.add(tka3);
		inputList.add(tka4);
		PunktObjekt returnval = ppm.calculatePositionOnPath(poS1, inputList, 300000, true);
		assertNotNull(returnval);
		assertTrue(returnval.punktObjektTopKante.length == 1);
		PunktObjektTopKante potk = returnval.punktObjektTopKante[0];
		assertEquals(idTKa4, potk.idTopKante);
		assertEquals("gegen", potk.wirkrichtung);
		assertEquals(50000, potk.abstand);
		
		// Signal S13 210 m against effective direction, using path [TKa13,TKa12,TKa9,TKa8]
		// Result must be TKa9, 60 m distance, against edge direction
		inputList.clear();
		inputList.add(tka13);
		inputList.add(tka12);
		inputList.add(tka9);
		inputList.add(tka8);
		returnval = ppm.calculatePositionOnPath(poS13, inputList, 210000, false);
		assertNotNull(returnval);
		assertTrue(returnval.punktObjektTopKante.length == 1);
		potk = returnval.punktObjektTopKante[0];
		assertEquals(idTKa9, potk.idTopKante);
		assertEquals("gegen", potk.wirkrichtung);
		assertEquals(60000, potk.abstand);
	}
	
	@Test
	void testCalculateDistance() {
		Element s1 = ppm.getElementbyId("11111111-2222-3333-4444-000000660001");
		Element s4 = ppm.getElementbyId("11111111-2222-3333-4444-000000660004");
		Element s6 = ppm.getElementbyId("11111111-2222-3333-4444-000000660006");
		Element s13 = ppm.getElementbyId("11111111-2222-3333-4444-000000660013");
		assertNotNull(s1);
		assertNotNull(s4);
		assertNotNull(s6);
		assertNotNull(s13);
		PunktObjekt poS1 = new PunktObjekt(s1);
		PunktObjekt poS4 = new PunktObjekt(s4);
		PunktObjekt poS6 = new PunktObjekt(s6);
		PunktObjekt poS13 = new PunktObjekt(s13);
		assertNotNull(poS1);
		assertNotNull(poS4);
		assertNotNull(poS6);
		assertNotNull(poS13);
		
		// Distance of signals S1 and S4, must be 300 m
		int result = ppm.calculateDistance(poS1, poS4);
		assertEquals(300000, result);
		
		// Distance of signals S1 and S13, must be 500 m
		result = ppm.calculateDistance(poS1, poS13);
		assertEquals(500000, result);
		
		// Distance of signals S6 and S13, must be -1 (not connected)
		result = ppm.calculateDistance(poS6, poS13);
		assertEquals(-1, result);
	}
	
	@Test
	void testCheckDirection() {
		Element s1 = ppm.getElementbyId("11111111-2222-3333-4444-000000660001");
		Element s4 = ppm.getElementbyId("11111111-2222-3333-4444-000000660004");
		Element s6 = ppm.getElementbyId("11111111-2222-3333-4444-000000660006");
		Element s9 = ppm.getElementbyId("11111111-2222-3333-4444-000000660009");
		Element s13 = ppm.getElementbyId("11111111-2222-3333-4444-000000660013");
		assertNotNull(s1);
		assertNotNull(s4);
		assertNotNull(s6);
		assertNotNull(s9);
		assertNotNull(s13);
		PunktObjekt poS1 = new PunktObjekt(s1);
		PunktObjekt poS4 = new PunktObjekt(s4);
		PunktObjekt poS6 = new PunktObjekt(s6);
		PunktObjekt poS9 = new PunktObjekt(s9);
		PunktObjekt poS13 = new PunktObjekt(s13);
		assertNotNull(poS1);
		assertNotNull(poS4);
		assertNotNull(poS6);
		assertNotNull(poS9);
		assertNotNull(poS13);
		
		// Signals S1 and S4 have equal orientation
		Direction result = ppm.checkDirection(poS1, poS4);
		assertEquals(Direction.EQUAL, result);
		
		// Signals S1 and S13 have equal orientation
		result = ppm.checkDirection(poS1, poS13);
		assertEquals(Direction.EQUAL, result);
		
		// Signals S1 and S9 have opposite orientation
		result = ppm.checkDirection(poS1, poS9);
		assertEquals(Direction.OPPOSITE, result);
		
		// Signals S6 and S13 are not connected
		result = ppm.checkDirection(poS6, poS13);
		assertEquals(Direction.NOT_CONNECTED, result);
	}
	
	@Test
	void testGetNextPunktObjekt() {
		Element s1 = ppm.getElementbyId("11111111-2222-3333-4444-000000660001");
		Element s4 = ppm.getElementbyId("11111111-2222-3333-4444-000000660004");
		Element s6 = ppm.getElementbyId("11111111-2222-3333-4444-000000660006");
		Element s9 = ppm.getElementbyId("11111111-2222-3333-4444-000000660009");
		Element s13 = ppm.getElementbyId("11111111-2222-3333-4444-000000660013");
		assertNotNull(s1);
		assertNotNull(s4);
		assertNotNull(s6);
		assertNotNull(s9);
		assertNotNull(s13);
		PunktObjekt poS1 = new PunktObjekt(s1);
		PunktObjekt poS4 = new PunktObjekt(s4);
		PunktObjekt poS6 = new PunktObjekt(s6);
		PunktObjekt poS9 = new PunktObjekt(s9);
		PunktObjekt poS13 = new PunktObjekt(s13);
		assertNotNull(poS1);
		assertNotNull(poS4);
		assertNotNull(poS6);
		assertNotNull(poS9);
		assertNotNull(poS13);
		
		// Search Signal S6 backwards
		// Result must be S4
		Element result = ppm.getNextPunktObjekt(poS6, "Signal", false);
		assertEquals(s4, result);
		
		// Search Signal S9 forward
		// Result must be S1
		result = ppm.getNextPunktObjekt(poS9, "Signal", true);
		assertEquals(s1, result);
		
		// Search Signal S13 backwards
		// Result must be S4 or S9
		result = ppm.getNextPunktObjekt(poS13, "Signal", false);
		assertTrue(result.equals(s4) || result.equals(s9));
		
		// Search Signal S13 backwards, but look for "Datenpunkt"
		// Result must be null
		result = ppm.getNextPunktObjekt(poS13, "Datenpunkt", false);
		assertNull(result);
		
		// Search Signal S13 forward
		// Result must be null
		result = ppm.getNextPunktObjekt(poS13, "Signal", true);
		assertNull(result);
	}
	
	@Test
	void testGetNextPunktObjektPaths() {
		Element s1 = ppm.getElementbyId("11111111-2222-3333-4444-000000660001");
		Element s4 = ppm.getElementbyId("11111111-2222-3333-4444-000000660004");
		Element s6 = ppm.getElementbyId("11111111-2222-3333-4444-000000660006");
		Element s9 = ppm.getElementbyId("11111111-2222-3333-4444-000000660009");
		Element s13 = ppm.getElementbyId("11111111-2222-3333-4444-000000660013");
		assertNotNull(s1);
		assertNotNull(s4);
		assertNotNull(s6);
		assertNotNull(s9);
		assertNotNull(s13);
		PunktObjekt poS1 = new PunktObjekt(s1);
		PunktObjekt poS4 = new PunktObjekt(s4);
		PunktObjekt poS6 = new PunktObjekt(s6);
		PunktObjekt poS9 = new PunktObjekt(s9);
		PunktObjekt poS13 = new PunktObjekt(s13);
		assertNotNull(poS1);
		assertNotNull(poS4);
		assertNotNull(poS6);
		assertNotNull(poS9);
		assertNotNull(poS13);
		
		// Search Signal S1 forward
		// Result must be S4 (on two different paths), S6, S9
		Evaluable condition = new TypeCondition("Signal");
		List<NextPunktObjektPathResult> resultlist = ppm.getNextPunktObjektPaths(poS1, condition, Direction.BOTH, true);
		assertNotNull(resultlist);
		assertTrue(resultlist.size() == 4);
		NextPunktObjektPathResult result0 = resultlist.get(0);
		NextPunktObjektPathResult result1 = resultlist.get(1);
		NextPunktObjektPathResult result2 = resultlist.get(2);
		NextPunktObjektPathResult result3 = resultlist.get(3);
		assertTrue(result0.punktObjektElement.equals(s4) || result0.punktObjektElement.equals(s6) || result0.punktObjektElement.equals(s9));
		assertTrue(result1.punktObjektElement.equals(s4) || result1.punktObjektElement.equals(s6) || result1.punktObjektElement.equals(s9));
		assertTrue(result2.punktObjektElement.equals(s4) || result2.punktObjektElement.equals(s6) || result2.punktObjektElement.equals(s9));
		assertTrue(result3.punktObjektElement.equals(s4) || result3.punktObjektElement.equals(s6) || result3.punktObjektElement.equals(s9));
		assertTrue(result0.punktObjektElement.equals(s4) || result1.punktObjektElement.equals(s4) || result2.punktObjektElement.equals(s4) || result3.punktObjektElement.equals(s4));
		assertTrue(result0.punktObjektElement.equals(s6) || result1.punktObjektElement.equals(s6) || result2.punktObjektElement.equals(s6) || result3.punktObjektElement.equals(s6));
		assertTrue(result0.punktObjektElement.equals(s9) || result1.punktObjektElement.equals(s9) || result2.punktObjektElement.equals(s9) || result3.punktObjektElement.equals(s9));
		
		// Search Signal S1 forward, look only for signals facing in equal direction
		// Result must be S4 (on two different paths), S6, S13
		resultlist = ppm.getNextPunktObjektPaths(poS1, condition, Direction.EQUAL, true);
		assertNotNull(resultlist);
		assertTrue(resultlist.size() == 4);
		result0 = resultlist.get(0);
		result1 = resultlist.get(1);
		result2 = resultlist.get(2);
		result3 = resultlist.get(3);
		assertTrue(result0.punktObjektElement.equals(s4) || result0.punktObjektElement.equals(s6) || result0.punktObjektElement.equals(s13));
		assertTrue(result1.punktObjektElement.equals(s4) || result1.punktObjektElement.equals(s6) || result1.punktObjektElement.equals(s13));
		assertTrue(result2.punktObjektElement.equals(s4) || result2.punktObjektElement.equals(s6) || result2.punktObjektElement.equals(s13));
		assertTrue(result3.punktObjektElement.equals(s4) || result3.punktObjektElement.equals(s6) || result3.punktObjektElement.equals(s13));
		assertTrue(result0.punktObjektElement.equals(s4) || result1.punktObjektElement.equals(s4) || result2.punktObjektElement.equals(s4) || result3.punktObjektElement.equals(s4));
		assertTrue(result0.punktObjektElement.equals(s6) || result1.punktObjektElement.equals(s6) || result2.punktObjektElement.equals(s6) || result3.punktObjektElement.equals(s6));
		assertTrue(result0.punktObjektElement.equals(s13) || result1.punktObjektElement.equals(s13) || result2.punktObjektElement.equals(s13) || result3.punktObjektElement.equals(s13));
		
		// Search Signal S1 forward, look only for signals facing in opposite direction
		// Result must be S9
		resultlist = ppm.getNextPunktObjektPaths(poS1, condition, Direction.OPPOSITE, true);
		assertNotNull(resultlist);
		assertTrue(resultlist.size() == 1);
		result0 = resultlist.get(0);
		assertTrue(result0.punktObjektElement.equals(s9));
	}
}
