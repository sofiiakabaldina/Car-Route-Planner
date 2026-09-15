package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MapGraphTest {

    private MapGraph mp;
    private Intersection int1;
    private Intersection int2;
    private Intersection int3;

    private Road r1;
    private Road r2;
    private Road r3;

    @BeforeEach
    void setUp() {

        mp = new MapGraph();

        int1 = new Intersection("1", 1, 1, "1");
        int2 = new Intersection("2", 2, 2, "2");
        int3 = new Intersection("3", 3, 1, "3");

        r1 = new Road(int1, int2, 1, 30);
        r2 = new Road(int2, int3, 1, 30);
        r3 = new Road(int1, int3, 2, 30);

    }

    @Test
    void addIntersection() {

        //empty list before adding anything
        assertEquals(0, mp.getIntersections().size());

        mp.addIntersection(int1);
        assertEquals(1, mp.getIntersections().size());

    }

    @Test
    void addRoad() {

        mp.addIntersection(int1);
        mp.addIntersection(int2);
        mp.addIntersection(int3);

        //empty list before adding anything
        assertEquals(0, mp.getRoads().size());

        mp.addRoad(r1);
        mp.addRoad(r2);
        mp.addRoad(r3);

        assertEquals(3, mp.getRoads().size());

    }

    @Test
    void getNeighbors() {

        mp.addIntersection(int1);
        mp.addIntersection(int2);
        mp.addIntersection(int3);
        mp.addRoad(r1);
        mp.addRoad(r2);
        mp.addRoad(r3);

        List<Road> neighbors1 = mp.getNeighbors(int1);
        assertEquals(2, neighbors1.size());
        assertTrue(neighbors1.contains(r1));
        assertTrue(neighbors1.contains(r3));

        List<Road> neighbors2 = mp.getNeighbors(int2);
        assertEquals(2, neighbors2.size());
        assertTrue(neighbors2.contains(r1));
        assertTrue(neighbors2.contains(r2));

    }

    @Test
    void getIntersections() {

        mp.addIntersection(int1);
        mp.addIntersection(int2);
        mp.addIntersection(int3);

        List<Intersection> result = mp.getIntersections();
        assertEquals(3, result.size());
        assertTrue(result.contains(int1));
        assertTrue(result.contains(int2));
        assertTrue(result.contains(int3));

    }

    @Test
    void getRoads() {

        mp.addIntersection(int1);
        mp.addIntersection(int2);
        mp.addIntersection(int3);
        mp.addRoad(r1);
        mp.addRoad(r2);
        mp.addRoad(r3);

        List<Road> result = mp.getRoads();
        assertEquals(3, result.size());
        assertTrue(result.contains(r1));
        assertTrue(result.contains(r2));
        assertTrue(result.contains(r3));

    }

    @Test
    void getNearestIntersection() {

        mp.addIntersection(int1);
        mp.addIntersection(int2);
        mp.addIntersection(int3);

        //(1.1, 1.1) is closest to int1 (1, 1)
        assertEquals(int1, mp.getNearestIntersection(1.1, 1.1));

        //(2.1, 2.1) is closest to int2 (2, 2)
        assertEquals(int2, mp.getNearestIntersection(2.1, 2.1));

        //(3.1, 1.1) is closest to int3 (3, 1)
        assertEquals(int3, mp.getNearestIntersection(3.1, 1.1));

    }
}