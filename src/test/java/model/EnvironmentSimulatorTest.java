package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnvironmentSimulatorTest {
    private EnvironmentSimulator myEnvironmentSimulator;
    private MapGraph myMapGraph;
    private Road myRoad;
    private Intersection myIntersectionA;
    private Intersection myIntersectionB;

    @BeforeEach
    void setUp() {
        myMapGraph = new MapGraph();
        myIntersectionA = new Intersection("A", 0.0, 0.0, "Main & 1st");
        myIntersectionB = new Intersection("B", 1.0, 1.0, "Main & 2nd");

        myRoad = new Road(myIntersectionA, myIntersectionB, 2.0, 40);
        myMapGraph.addIntersection(myIntersectionA);
        myMapGraph.addIntersection(myIntersectionB);
        myMapGraph.addRoad(myRoad);

        myEnvironmentSimulator = new EnvironmentSimulator(myMapGraph);
    }

    @Test
    void testApplyWeatherRoad() {
        myEnvironmentSimulator.applyWeatherRoad(Environment.HEAVY_RAIN, myRoad);

        assertEquals(Environment.HEAVY_RAIN, myRoad.getCondition());

    }

    @Test
    void testApplyWeatherRoadNullCondition() {
        assertThrows(NullPointerException.class, () -> myEnvironmentSimulator.applyWeatherRoad(null, myRoad));
    }

    @Test
    void testApplyWeatherRoadEmptyCondition() {
        assertThrows(NullPointerException.class, () -> myEnvironmentSimulator.applyWeatherRoad(Environment.HEAVY_RAIN, null));
    }

    @Test
    void testApplyTrafficRoadValidDensity() {
        myEnvironmentSimulator.applyTrafficRoad(myRoad, 0.5);
        assertEquals(0.5, myRoad.getTrafficDensity());
    }

    @Test
    void testApplyTrafficRoadInvalidDensity() {
        myEnvironmentSimulator.applyTrafficRoad(myRoad, -0.5);
        assertEquals(0.0, myRoad.getTrafficDensity());
    }

    @Test
    void applyTrafficRoadNullRoad() {
        myEnvironmentSimulator.blockRoad(myRoad);

        assertTrue(myRoad.isBlocked());
    }

    @Test
    void testBlockedNullRoad() {
        assertThrows(NullPointerException.class, () -> myEnvironmentSimulator.blockRoad(null));
    }

    @Test
    void testBlockIntersection() {
        myEnvironmentSimulator.blockIntersection(myIntersectionA);
        assertTrue(myIntersectionA.isBlocked());
    }
    @Test
    void testBlockIntersectionNullIntersection() {
        assertThrows(NullPointerException.class, () -> myEnvironmentSimulator.blockIntersection(null));
    }

    @Test
    void testResetConditions() {
        myRoad.setCondition(Environment.ICY);
        myRoad.setTrafficDensity(0.8);
        myRoad.setBlocked(true);
        myIntersectionA.setBlocked(true);
        myIntersectionB.setBlocked(true);

        myEnvironmentSimulator.resetConditions(myMapGraph);

        assertEquals(Environment.CLEAR, myRoad.getCondition());
        assertEquals(0.0, myRoad.getTrafficDensity());
        assertFalse( myRoad.isBlocked());
        assertFalse(myIntersectionA.isBlocked());
        assertFalse(myIntersectionB.isBlocked());

    }

    @Test
    void testGetRoadCondition() {
        myRoad.setCondition(Environment.LIGHT_SNOW);

        assertEquals(Environment.LIGHT_SNOW, myEnvironmentSimulator.getRoadCondition(myRoad));
    }

    @Test
    void testGetRoadConditionNull() {
        assertThrows(NullPointerException.class, () -> myEnvironmentSimulator.getRoadCondition(null));
    }

    @Test
    void testGetRiskFactorRoad() {

        myRoad.setCondition(Environment.HEAVY_SNOW);
        myRoad.setTrafficDensity(0.2);

        assertEquals(1.0, myEnvironmentSimulator.getRiskFactorRoad(myRoad));
    }

    @Test
    void testGetRiskFactorRoadNull() {
        assertThrows(NullPointerException.class, () -> myEnvironmentSimulator.getRiskFactorRoad(null));
    }
}