package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IntersectionTest {

    private Intersection myIntersection;

    @BeforeEach
    void setUp() {
        myIntersection = new Intersection("1", 47.25, -122.45, "Main St");
    }

    @Test
    void getX() {
        assertEquals(47.25, myIntersection.getX(), 0.0001);
    }

    @Test
    void getY() {
        assertEquals(-122.45, myIntersection.getY(), 0.0001);
    }

    @Test
    void getId() {
        assertEquals("1", myIntersection.getId());
    }

    @Test
    void getLabel() {
        assertEquals("Main St", myIntersection.getLabel());
    }

    @Test
    void isBlockedDefaultFalse() {
        assertFalse(myIntersection.isBlocked());
    }

    @Test
    void setBlocked() {
        myIntersection.setBlocked(true);
        assertTrue(myIntersection.isBlocked());
    }

    @Test
    void setBlockedFalseAfterTrue() {
        myIntersection.setBlocked(true);
        myIntersection.setBlocked(false);
        assertFalse(myIntersection.isBlocked());
    }

    @Test
    void getPosition() {
        double[] pos = myIntersection.getPosition();
        assertEquals(2, pos.length);
        assertEquals(47.25, pos[0], 0.0001);
        assertEquals(-122.45, pos[1], 0.0001);
    }

    @Test
    void testToString() {
        assertEquals("Main St (1)", myIntersection.toString());
    }

    @Test
    void testToStringUsesIdWhenNoLabel() {
        Intersection i = new Intersection("42", 0.0, 0.0, "42");
        assertEquals("42 (42)", i.toString());
    }

    @Test
    void testEquals_sameObject() {
        assertEquals(myIntersection, myIntersection);
    }

    @Test
    void testEquals_sameId() {
        Intersection other = new Intersection("1", 0.0, 0.0, "Other Label");
        assertEquals(myIntersection, other);
    }

    @Test
    void testEquals_differentId() {
        Intersection other = new Intersection("2", 47.25, -122.45, "Main St");
        assertNotEquals(myIntersection, other);
    }

    @Test
    void testEquals_null() {
        assertNotEquals(myIntersection, null);
    }

    @Test
    void testEquals_differentType() {
        assertNotEquals(myIntersection, "not an intersection");
    }

    @Test
    void testHashCode_sameId() {
        Intersection other = new Intersection("1", 99.0, 99.0, "Different");
        assertEquals(myIntersection.hashCode(), other.hashCode());
    }

    @Test
    void testHashCode_differentId() {
        Intersection other = new Intersection("2", 47.25, -122.45, "Main St");
        assertNotEquals(myIntersection.hashCode(), other.hashCode());
    }
}