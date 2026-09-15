package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MapLoaderTest {

    @Test
    void loadFromFile_validFile_loadsIntersections() throws Exception {
        MapGraph graph = new MapGraph();
        MapLoader.loadFromFile(graph, "test_map.osm");
        assertFalse(graph.getIntersections().isEmpty(),
                "Graph should have intersections after loading");
    }

    @Test
    void loadFromFile_validFile_loadsRoads() throws Exception {
        MapGraph graph = new MapGraph();
        MapLoader.loadFromFile(graph, "test_map.osm");
        assertFalse(graph.getRoads().isEmpty(),
                "Graph should have roads after loading");
    }

    @Test
    void loadFromFile_missingFile_throwsException() {
        MapGraph graph = new MapGraph();
        assertThrows(Exception.class,
                () -> MapLoader.loadFromFile(graph, "nonexistent.osm"));
    }

    @Test
    void loadFromFile_roadsBidirectional() throws Exception {
        MapGraph graph = new MapGraph();
        MapLoader.loadFromFile(graph, "test_map.osm");
        // Two-way roads should create 2 Road objects per segment
        // so roads should be >= intersections - 1
        assertTrue(graph.getRoads().size() >= graph.getIntersections().size() - 1);
    }

    @Test
    void loadFromFile_allIntersectionsHaveNeighbors() throws Exception {
        MapGraph graph = new MapGraph();
        MapLoader.loadFromFile(graph, "test_map.osm");
        for (Intersection i : graph.getIntersections()) {
            assertNotNull(graph.getNeighbors(i),
                    "Every intersection should have a neighbor list");
        }
    }
}