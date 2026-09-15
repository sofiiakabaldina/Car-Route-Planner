package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TurnSharpnessTest {

    @Test
    void testOrdinal() {
        assertEquals(0, TurnSharpness.STRAIGHT.ordinal());
        assertEquals(1, TurnSharpness.GENTLE.ordinal());
        assertEquals(2, TurnSharpness.SHARP.ordinal());
        assertEquals(3, TurnSharpness.HAIRPIN.ordinal());
    }

    @Test
    void getRiskFactor_straight() {
        assertEquals(0.0, TurnSharpness.STRAIGHT.getRiskFactor());
    }

    @Test
    void getRiskFactor_gentle() {
        assertEquals(1.0, TurnSharpness.GENTLE.getRiskFactor());
    }

    @Test
    void getRiskFactor_sharp() {
        assertEquals(1.5, TurnSharpness.SHARP.getRiskFactor());
    }

    @Test
    void getRiskFactor_hairpin() {
        assertEquals(2.0, TurnSharpness.HAIRPIN.getRiskFactor());
    }

    @Test
    void testRiskFactor_increasesWithSharpness() {
        assertTrue(TurnSharpness.STRAIGHT.getRiskFactor() <  TurnSharpness.GENTLE.getRiskFactor());
        assertTrue(TurnSharpness.GENTLE.getRiskFactor() <  TurnSharpness.SHARP.getRiskFactor());
        assertTrue(TurnSharpness.STRAIGHT.getRiskFactor() <  TurnSharpness.HAIRPIN.getRiskFactor());
    }

   @Test
    void testEnumValues_count() {
        assertEquals(4, TurnSharpness.values().length);
   }

   @Test
    void testValueOf_invalid() {
        assertThrows(IllegalArgumentException.class, () -> {TurnSharpness.valueOf("invalid");});
    }
    @Test
    void testValueOf_caseSensitivity() {
        assertThrows(IllegalArgumentException.class, () -> {TurnSharpness.valueOf("straight");});
    }

    @Test
    void testEqual() {
        assertSame(TurnSharpness.STRAIGHT, TurnSharpness.valueOf("STRAIGHT"));
    }
}