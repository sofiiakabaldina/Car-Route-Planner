package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnvironmentTest {

    @Test
    void testRiskFactor() {
        assertEquals(0.0, Environment.CLEAR.getRiskFactor());
        assertEquals(0.2,  Environment.LIGHT_RAIN.getRiskFactor());
        assertEquals(0.3,  Environment.LIGHT_FOG.getRiskFactor());
        assertEquals(0.4,  Environment.LIGHT_SNOW.getRiskFactor());
        assertEquals(0.6, Environment.HEAVY_RAIN.getRiskFactor());
        assertEquals(0.7, Environment.DENSE_FOG.getRiskFactor());
        assertEquals(0.8, Environment.HEAVY_SNOW.getRiskFactor());
        assertEquals(0.9, Environment.ICY.getRiskFactor());
    }

    @Test
    void testEnvironmentEnumOrdering() {
        assertEquals(Environment.CLEAR, Environment.values()[0]);
        assertEquals(Environment.LIGHT_RAIN, Environment.values()[1]);
        assertEquals(Environment.LIGHT_FOG, Environment.values()[2]);
        assertEquals(Environment.LIGHT_SNOW, Environment.values()[3]);
        assertEquals(Environment.HEAVY_RAIN, Environment.values()[4]);
        assertEquals(Environment.DENSE_FOG, Environment.values()[5]);
        assertEquals(Environment.HEAVY_SNOW, Environment.values()[6]);
        assertEquals(Environment.ICY, Environment.values()[7]);
    }


    @Test
    void testEnvironmentValueOf() {
        assertEquals(Environment.CLEAR, Environment.valueOf("CLEAR"));
        assertEquals(Environment.LIGHT_RAIN, Environment.valueOf("LIGHT_RAIN"));
        assertEquals(Environment.LIGHT_FOG, Environment.valueOf("LIGHT_FOG"));
        assertEquals(Environment.LIGHT_SNOW, Environment.valueOf("LIGHT_SNOW"));
        assertEquals(Environment.HEAVY_RAIN, Environment.valueOf("HEAVY_RAIN"));
        assertEquals(Environment.DENSE_FOG, Environment.valueOf("DENSE_FOG"));
        assertEquals(Environment.HEAVY_SNOW, Environment.valueOf("HEAVY_SNOW"));
        assertEquals(Environment.ICY, Environment.valueOf("ICY"));

    }
}