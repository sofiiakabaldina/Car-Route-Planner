package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SafetyCheckerTest {

    private MapGraph myMapGraph;
    private SafetyChecker mySafetyChecker;

    private Intersection myIntersectionA;
    private Intersection myIntersectionB;
    private Intersection myIntersectionC;

    private Road myRoadA;
    private Road myRoadB;


    @BeforeEach
    void setUp() {
        myMapGraph = new MapGraph();

        myIntersectionA = new Intersection("A", 0.0, 0.0, "A");
        myIntersectionB = new Intersection("B", 1.0, 0.0, "B");
        myIntersectionC = new Intersection("C", 2.0, 0.0, "C");

        myMapGraph.addIntersection(myIntersectionA);
        myMapGraph.addIntersection(myIntersectionB);
        myMapGraph.addIntersection(myIntersectionC);

        myRoadA = new Road(myIntersectionA, myIntersectionB, 1.0, 35);
        myRoadB = new Road(myIntersectionB, myIntersectionC, 1.0, 35);

        myMapGraph.addRoad(myRoadA);
        myMapGraph.addRoad(myRoadB);

        mySafetyChecker = new SafetyChecker();
    }

    @Test
    void testTotalRiskFactor() {
        myRoadA.setCondition(Environment.HEAVY_RAIN);
        myRoadB.setTrafficDensity(0.4);

        mySafetyChecker.analyzeRoute(List.of(myRoadA, myRoadB), List.of(myIntersectionA, myIntersectionB, myIntersectionC));

        assertEquals(1.0, mySafetyChecker.getTotalRiskFactor(), 0.0001);

    }

    @Test
    void testAverageRiskFactor() {

        myRoadA.setCondition(Environment.HEAVY_RAIN);
        myRoadB.setTrafficDensity(0.4);

        mySafetyChecker.analyzeRoute(List.of(myRoadA, myRoadB),  List.of(myIntersectionA, myIntersectionB, myIntersectionC));
        assertEquals(0.5, mySafetyChecker.getAverageRiskFactor(), 0.0001);
    }

    @Test
    void testAverageRiskFactorEmptyRoute() {
        mySafetyChecker.analyzeRoute(List.of(),List.of());

        assertEquals(0.0, mySafetyChecker.getAverageRiskFactor(), 0.0001);
    }

    @Test
    void testBlockedRoad() {
        myRoadA.setBlocked(true);

        mySafetyChecker.analyzeRoute(List.of(myRoadA, myRoadB),  List.of(myIntersectionA, myIntersectionB, myIntersectionC));
        assertTrue(mySafetyChecker.hasBlockedRoads());

    }

    @Test
    void testBlockedIntersectionDetect() {

        myIntersectionB.setBlocked(true);

        mySafetyChecker.analyzeRoute(List.of(myRoadA, myRoadB),   List.of(myIntersectionA, myIntersectionB, myIntersectionC));
        assertTrue(mySafetyChecker.hasBlockedIntersections());
    }
    @Test
    void testRouteSafe() {

        mySafetyChecker.analyzeRoute(List.of(myRoadA,myRoadB), List.of(myIntersectionA, myIntersectionB, myIntersectionC));
        assertTrue(mySafetyChecker.isRouteSafe());
    }

    @Test
    void testUnsafeBlockedRoad() {
        myRoadA.setBlocked(true);

        mySafetyChecker.analyzeRoute(List.of(myRoadA,myRoadB), List.of(myIntersectionA, myIntersectionB, myIntersectionC));
        assertFalse(mySafetyChecker.isRouteSafe());
    }

    @Test
    void testBlockedRoadMessage() {
        myRoadA.setBlocked(true);
        mySafetyChecker.analyzeRoute(List.of(myRoadA,myRoadB), List.of(myIntersectionA, myIntersectionB, myIntersectionC));

        assertTrue(mySafetyChecker.getHazards().contains("Route has blocked roads."));
    }

    @Test
    void testHighRiskHazard() {
        myRoadA.setCondition(Environment.ICY);
        mySafetyChecker.analyzeRoute(List.of(myRoadA), List.of(myIntersectionA, myIntersectionB));

        assertTrue(mySafetyChecker.getHazards().contains("Route has high overall risk."));

    }

    @Test
    void testDangerousWeatherAndTraffic() {
        myRoadA.setCondition(Environment.HEAVY_RAIN);
        myRoadA.setTrafficDensity(0.7);

        mySafetyChecker.analyzeRoute(List.of(myRoadA,myRoadB), List.of(myIntersectionA, myIntersectionB, myIntersectionC));

        assertTrue(mySafetyChecker.getHazards().contains("Route has a dangerous weather and traffic combination."));
    }

    @Test
    void testPlannedRoads() {
        mySafetyChecker.analyzeRoute(List.of(myRoadA,myRoadB), List.of(myIntersectionA, myIntersectionB, myIntersectionC));

        assertEquals(2, mySafetyChecker.getPlannedRoads().size());
    }

    @Test
    void testSuggestAlternativeEmpty(){
        mySafetyChecker.analyzeRoute(List.of(myRoadA,myRoadB), List.of(myIntersectionA, myIntersectionB, myIntersectionC));

        assertTrue(mySafetyChecker.suggestAlternative().isEmpty());
    }


    @Test
    void testSharpTurnStraightLine(){
        Intersection intersectionD = new Intersection("D", 2, 0, "D");

        mySafetyChecker.analyzeRoute(List.of(), List.of(myIntersectionA, myIntersectionB, intersectionD));

        assertFalse(mySafetyChecker.hasSharpTurns());
    }

}