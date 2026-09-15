package model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RouteRecordTest {

    @Test
    void testConstructor() {
        List<String> hazards = Arrays.asList("Heavy Rain", "Traffic");

        RouteRecord record = new RouteRecord(1, "Start", "End", 12.5, 18.5, hazards);

        assertEquals(1, record.getMyID());
        assertEquals("Start", record.getMyStartLabel());
        assertEquals("End", record.getMyEndLabel());
        assertEquals(12.5, record.getMyDistance());
        assertEquals(18.5, record.getMyDuration());
        assertEquals(hazards, record.getMyHazards());
    }
    @Test
    void testSetMyID() {
        RouteRecord record = new RouteRecord(1, "A", "B", 5.5, 8.5, List.of());
        record.setMyID(100);
        assertEquals(100, record.getMyID());
    }

    @Test
    void testSetMyStartLabel() {
        RouteRecord record = new RouteRecord(1, "A", "B", 5.5, 8.5, List.of());

        record.setMyStartLabel("New Start Label");
        assertEquals("New Start Label", record.getMyStartLabel());
    }
    @Test
    void testSetMyEndLabel() {
        RouteRecord record = new RouteRecord(1, "A", "B", 5.5, 8.5, List.of());
        record.setMyEndLabel("New End Label");
        assertEquals("New End Label", record.getMyEndLabel());
    }
    @Test
    void testSetMyDistance() {
        RouteRecord record = new RouteRecord(1, "A", "B", 5.5, 8.5, List.of());
        record.setMyDistance(100.00);
        assertEquals(100.00, record.getMyDistance());
    }

    @Test
    void testSetMyDuration() {
        RouteRecord record = new RouteRecord(1, "A", "B", 5.5, 8.5, List.of());
        record.setMyDuration(20.3);
        assertEquals(20.3, record.getMyDuration());
    }

    @Test
    void testSetMyHazards() {
        RouteRecord record = new RouteRecord(1, "A", "B", 5.5, 8.5, List.of());

        List<String> hazards = Arrays.asList("Heavy Snow", "Blocked Roads");
        record.setMyHazards(hazards);
        assertEquals(hazards, record.getMyHazards());
    }
    @Test
    void testEmptyHazardsList() {
        RouteRecord record = new RouteRecord(1, "A", "B", 5.5, 8.5, List.of());

        assertTrue(record.getMyHazards().isEmpty());
    }
}