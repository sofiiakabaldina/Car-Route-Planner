package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RoadTest {

    private Intersection myStart;
    private Intersection myEnd;
    private Road myRoad;

    @BeforeEach
    void setUp() {
        myStart = new Intersection("1", 47.25, -122.45, "Start");
        myEnd   = new Intersection("2", 47.26, -122.44, "End");
        myRoad  = new Road(myStart, myEnd, 1.5, 30);
    }

    @Test
    void getStart() {
        assertEquals(myStart, myRoad.getStart());
    }

    @Test
    void getEnd() {
        assertEquals(myEnd, myRoad.getEnd());
    }

    @Test
    void getOtherEnd_fromStart() {
        assertEquals(myEnd, myRoad.getOtherEnd(myStart));
    }

    @Test
    void getOtherEnd_fromEnd() {
        assertEquals(myStart, myRoad.getOtherEnd(myEnd));
    }

    @Test
    void getOtherEnd_unknownIntersection() {
        Intersection other = new Intersection("3", 0.0, 0.0, "Other");
        assertNull(myRoad.getOtherEnd(other));
    }

    @Test
    void getDistanceInMiles() {
        assertEquals(1.5, myRoad.getDistanceInMiles(), 0.0001);
    }

    @Test
    void getSpeedLimit() {
        assertEquals(30, myRoad.getSpeedLimit());
    }

    @Test
    void getCondition_defaultClear() {
        assertEquals(Environment.CLEAR, myRoad.getCondition());
    }

    @Test
    void setCondition() {
        myRoad.setCondition(Environment.HEAVY_SNOW);
        assertEquals(Environment.HEAVY_SNOW, myRoad.getCondition());
    }

    @Test
    void isBlocked_defaultFalse() {
        assertFalse(myRoad.isBlocked());
    }

    @Test
    void setBlocked_true() {
        myRoad.setBlocked(true);
        assertTrue(myRoad.isBlocked());
    }

    @Test
    void setBlocked_falseAfterTrue() {
        myRoad.setBlocked(true);
        myRoad.setBlocked(false);
        assertFalse(myRoad.isBlocked());
    }

    @Test
    void getTrafficDensity_defaultZero() {
        assertEquals(0.0, myRoad.getTrafficDensity(), 0.0001);
    }

    @Test
    void setTrafficDensity_valid() {
        myRoad.setTrafficDensity(0.5);
        assertEquals(0.5, myRoad.getTrafficDensity(), 0.0001);
    }

    @Test
    void setTrafficDensity_clampsAboveOne() {
        myRoad.setTrafficDensity(1.5);
        assertEquals(1.0, myRoad.getTrafficDensity(), 0.0001);
    }

    @Test
    void setTrafficDensity_clampsBelowZero() {
        myRoad.setTrafficDensity(-0.5);
        assertEquals(0.0, myRoad.getTrafficDensity(), 0.0001);
    }

    @Test
    void getEffectiveSpeed_clearNoTraffic() {
        // 30 * (1-0) * (1-0) = 30
        assertEquals(30.0, myRoad.getEffectiveSpeed(), 0.0001);
    }

    @Test
    void getEffectiveSpeed_withTraffic() {
        myRoad.setTrafficDensity(0.5);
        // 30 * 1.0 * 0.5 = 15
        assertEquals(15.0, myRoad.getEffectiveSpeed(), 0.0001);
    }

    @Test
    void getEffectiveSpeed_withWeatherAndTraffic() {
        myRoad.setCondition(Environment.HEAVY_SNOW); // risk 0.8
        myRoad.setTrafficDensity(0.5);
        // 30 * 0.4 * 0.5 = 3.0
        assertEquals(3.0, myRoad.getEffectiveSpeed(), 0.0001);
    }

    @Test
    void getTravelTimeHours_normal() {
        // 1.5 miles / 30 mph = 0.05 hours
        assertEquals(0.05, myRoad.getTravelTimeHours(), 0.0001);
    }

    @Test
    void getTravelTimeHours_blocked() {
        myRoad.setBlocked(true);
        assertEquals(Double.MAX_VALUE, myRoad.getTravelTimeHours());
    }

    @Test
    void getTravelTimeHours_zeroSpeed() {
        myRoad.setCondition(Environment.HEAVY_SNOW);  // risk 1.0 if available
        myRoad.setTrafficDensity(1.0);
        // speed = 0, should return MAX_VALUE
        assertEquals(Double.MAX_VALUE, myRoad.getTravelTimeHours());
    }

    @Test
    void getCombinedRiskFactor_clearNoTraffic() {
        assertEquals(0.0, myRoad.getCombinedRiskFactor(), 0.0001);
    }

    @Test
    void getCombinedRiskFactor_withTraffic() {
        myRoad.setTrafficDensity(0.4);
        // CLEAR risk=0.0 + 0.4 traffic = 0.4
        assertEquals(0.4, myRoad.getCombinedRiskFactor(), 0.0001);
    }

    @Test
    void getCombinedRiskFactor_withWeather() {
        myRoad.setCondition(Environment.HEAVY_SNOW); // risk 0.8
        assertEquals(0.8, myRoad.getCombinedRiskFactor(), 0.0001);
    }

    @Test
    void testToString_containsLabels() {
        String result = myRoad.toString();
        assertTrue(result.contains("Start"));
        assertTrue(result.contains("End"));
        assertTrue(result.contains("1.50"));
        assertTrue(result.contains("30"));
    }
}