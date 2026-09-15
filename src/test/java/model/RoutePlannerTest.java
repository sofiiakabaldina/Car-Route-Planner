package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import org.jxmapviewer.viewer.GeoPosition;

import java.util.List;


class RoutePlannerTest {

    private RoutePlanner rp;
    private MapGraph graph;
    private GeoPosition startPoint;
    private GeoPosition endPoint;

    private Intersection int1, int2, int3;
    private Road road1, road2, road3;


    @BeforeEach
    void setUp() {

        graph = new MapGraph();
        rp = new RoutePlanner(graph);
        startPoint = new GeoPosition(1,1);
        endPoint = new GeoPosition(2,2);

        //building graph
        int1 = new Intersection ("1", 1, 1, "1");
        int2 = new Intersection ("2", 2, 1, "2");
        int3 = new Intersection ("3", 2, 6, "3");

        graph.addIntersection(int1);
        graph.addIntersection(int2);
        graph.addIntersection(int3);

        road1 = new Road(int1, int2, 3.0, 30);
        road2 = new Road(int2, int3, 4.0, 30);
        road3 = new Road(int1, int3, 5.0, 30);

        graph.addRoad(road1);
        graph.addRoad(road2);
        graph.addRoad(road3);

        //|int1| --r1-- |int2|
        //     \           |
        //      \          |
        //       \         |
        //       r3        r2
        //         \       |
        //          \      |
        //           \     |
        //            \    |
        //             |int3|
    }

    @Test
    void addStartPoint() {

        //first click >> set to start
        rp.addPoint(startPoint);

        assertEquals(startPoint, rp.getMyStartPoint());
        assertNull(rp.getMyEndPoint());

    }

    @Test
    void addEndPoint() {

        rp.addPoint(startPoint);
        rp.addPoint(endPoint);

        assertEquals(endPoint, rp.getMyEndPoint());

    }

    @Test
    void clearPoints() {

        rp.addPoint(startPoint);
        rp.addPoint(endPoint);

        rp.clearPoints();

        assertNull(rp.getMyStartPoint());
        assertNull(rp.getMyEndPoint());
    }

    @Test
    void clearPointsOnePoint() {

        rp.addPoint(startPoint);

        rp.clearPoints();

        assertNull(rp.getMyStartPoint());
        assertNull(rp.getMyEndPoint());

    }

    @Test
    void routeSelected() {

        assertFalse(rp.routeSelected());

        rp.addPoint(startPoint);
        rp.addPoint(endPoint);

        assertTrue(rp.routeSelected());
    }

    @Test
    void calculateRouteEmptyRoute() {

        //should do nothing
        rp.calculateRoute();
        assertTrue(rp.getCalculatedRoute().isEmpty());
    }

    @Test
    void calculateRouteNoEndPoint() {

        rp.addPoint(startPoint);

        //should do nothing
        rp.calculateRoute();
        assertTrue(rp.getCalculatedRoute().isEmpty());

    }


    //|int1| --r1-- |int2|
    //     \           |
    //      \          |
    //       \         |
    //       r3        r2
    //         \       |
    //          \      |
    //           \     |
    //            \    |
    //             |int3|

    //normal fastest path
    @Test
    void calculateRouteInt1ToInt3() {

        //should pick r3
        List<Road> path = rp.findRoute(int1, int3);

        assertEquals(path.size(), 1);
        assertEquals(road3, path.get(0));

    }

    @Test
    void calculateRouteBlockedRoad() {

        //algorithm skips blocked roads
        road3.setBlocked(true);

        //should pick r3
        List<Road> path = rp.findRoute(int1, int3);

        assertEquals(path.size(), 2);
        assertEquals(road1, path.get(0));
        assertEquals(road2, path.get(1));

    }

    @Test
    void calculateRouteAllBlocked() {

        road1.setBlocked(true);
        road2.setBlocked(true);
        road3.setBlocked(true);

        //no route will exist
        List<Road> path = rp.findRoute(int1, int3);

        assertEquals(path.size(), 0);

    }

    @Test
    void calculateRouteUnsafeRoad() {

        //exceeds threshold, algorithm should redirect and not choose road3
        road3.setCondition(Environment.HEAVY_RAIN);
        road3.setTrafficDensity(0.5);

        //int1
        rp.addPoint(new GeoPosition(1, 1));
        //int2
        rp.addPoint(new GeoPosition(2, 6));
        rp.calculateRoute();

        List<GeoPosition> route = rp.getCalculatedRoute();

        assertFalse(route.isEmpty());

        assertEquals(route.size(), 4);

    }

    @Test
    void calculateRoutePickFastestRoute() {

        //both paths have a RF but algorithm should choose fastest still

        //longer path has 0.4
        road1.setCondition(Environment.LIGHT_RAIN);
        road2.setTrafficDensity(0.2);

        //shorter has 0.5
        road3.setTrafficDensity(0.5);

        //algorithm should choose shorter path even though it's riskier (would choose longer if it exceeded the
        //threshold
        List<Road> path = rp.findRoute(int1, int3);

        assertEquals(path.size(), 1);
        assertEquals(road3, path.get(0));
    }

}