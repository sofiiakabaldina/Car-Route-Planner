package model;

import org.jxmapviewer.viewer.GeoPosition;

import java.util.*;

import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Plans optimal routes between two geographic points using the A* pathfinding algorithm.
 * Snaps user-clicked map coordinates to the nearest graph intersections, runs A* over
 * the road network, checks the resulting route for safety hazards, and falls back to
 * an alternative route when the primary route is deemed unsafe.
 * Fires property change events so the view layer can repaint the route and update
 * the safety log whenever a new route is calculated.
 *
 * @author Abby Hinds, Chene Van der Walt, and Sofiia Kabaldina
 * @version Spring 2026
 */
public class RoutePlanner extends AbstractModel {
    /** The user-selected start coordinate, or null if not yet set. */
    private GeoPosition myStartPoint;
    /** The user-selected end coordinate, or null if not yet set. */
    private GeoPosition myEndPoint;
    /** The graph used for pathfinding. */
    private final MapGraph myMapGraph;
    /** Safety checker that analyzes routes and suggests alternatives. */
    private final SafetyChecker mySafetyChecker;
    /** The most recently calculated route as an ordered list of geographic positions. */
    private List<GeoPosition> myCalculatedRoute = new ArrayList<>();

    /**
     * Constructs a RoutePlanner for the given map graph.
     * Initializes the SafetyChecker and registers this planner with it so
     * the checker can request alternative routes when needed.
     *
     * @param theGraph the road network graph to route over
     */
    public RoutePlanner(final MapGraph theGraph) {
        myMapGraph = theGraph;
        mySafetyChecker = new SafetyChecker();
        mySafetyChecker.setRoutePlanner(this);
    }

    /**
     * Adds a geographic point as either the start or end of the route.
     * The first call sets the start point, the second sets the end point,
     * and any subsequent call resets the start and clears the end,
     * allowing the user to begin selecting a new route.
     * Fires a "routeUpdated" property change event after each call.
     *
     * @param thePoint - the clicked geographic position to add
     */
    public void addPoint(final GeoPosition thePoint) {

        if (myStartPoint == null) {
            myStartPoint = thePoint;
        } else if (myEndPoint == null) {
            myEndPoint = thePoint;
        } else {
            myStartPoint = thePoint;
            myEndPoint = null;
        }
        myChanges.firePropertyChange("routeUpdated", false, true);
    }

    /**
     * Returns the current start point, or null if not yet set.
     *
     * @return the start point, or null
     */
    public GeoPosition getMyStartPoint() {
        return myStartPoint;
    }

    /**
     * Returns the current end point, or null if not yet set.
     *
     * @return the end point, or null
     */
    public GeoPosition getMyEndPoint() {
        return myEndPoint;
    }

    /**
     * Clears both the start and end points and fires a "routeUpdated" event
     * so the view resets its waypoint markers.
     */
    public void clearPoints() {
        myStartPoint = null;
        myEndPoint = null;
        myChanges.firePropertyChange("routeUpdated", true, false);
    }

    /**
     * Returns true if both a start and end point have been selected.
     *
     * @return true if a route can be calculated, false otherwise
     */
    public boolean routeSelected() {
        return myStartPoint != null && myEndPoint != null;
    }

    /**
     * Calculates the best route between the current start and end points.
     * Snaps both points to the nearest graph intersections, runs A*,
     * checks safety, and uses an alternative route if the primary is unsafe.
     * Converts the final road list to a GeoPosition list for the painter and
     * fires a "routeCalculated" property change event when done.
     * Does nothing if both points are not set.
     */
    public void calculateRoute() {
        if (!routeSelected()) {
            return;
        }

        System.out.println("Calculating route from " + myStartPoint + " to " + myEndPoint);

        final GeoPosition oldStartPoint = myStartPoint;
        myChanges.firePropertyChange("routeStart", oldStartPoint, myStartPoint);
        final GeoPosition oldEndPoint = myEndPoint;
        myChanges.firePropertyChange("routeEnd", oldEndPoint, myEndPoint);

        // Snap clicked GeoPositions to nearest real intersections in the graph
        final Intersection start = myMapGraph.getNearestIntersection(
                myStartPoint.getLatitude(), myStartPoint.getLongitude());
        final Intersection end = myMapGraph.getNearestIntersection(
                myEndPoint.getLatitude(), myEndPoint.getLongitude());

        if (start == null || end == null) {
            System.out.println("Could not snap points to intersections.");
            myChanges.firePropertyChange("routeCalculated", null, false);
            return;
        }

        // Run A* algorithm
        List<Road> path = findRoute(start, end);

        if (path.isEmpty()) {
            System.out.println("No route found between selected points.");
            myChanges.firePropertyChange("routeCalculated", null, false);
            return;
        }

        // If route is unsafe, try to find a safer alternative
        if (!mySafetyChecker.isRouteSafe()) {
            System.out.println("Route is unsafe, looking for alternative...");
            List<Road> alternative = mySafetyChecker.suggestAlternative();
            if (alternative != null && !alternative.isEmpty()) {
                System.out.println("Alternative route found.");
                path = alternative;
                // Re-analyze the alternative so hazards list is up to date
                List<Intersection> altIntersections = new ArrayList<>();
                for (Road road : path) {
                    altIntersections.add(road.getStart());
                    altIntersections.add(road.getEnd());
                }
                mySafetyChecker.analyzeRoute(path, altIntersections);
            } else {
                System.out.println("No safe alternative found, using original route.");
            }
        }

        // Convert road list to GeoPositions for RoutePainter
        myCalculatedRoute = convertPathToGeoPositions(path);
        myChanges.firePropertyChange("routeCalculated", null, true);
    }

    /**
     * Runs the A* algorithm to find the fastest route from theStart to theEnd
     * through the road network. Uses actual travel time as the g-score and
     * Haversine-based estimated time as the h-score heuristic.
     * Blocked roads are skipped. Calls SafetyChecker, analyzeRoute() on
     * the result before returning so hazards are always up to date.
     * <p>
     * f = g + h, where:
     * g = cumulative travel time from start in hours
     * h = straight-line estimated travel time to end in hours
     *
     * @param theStart - the starting intersection
     * @param theEnd - the destination intersection
     * @return an ordered list of roads forming the fastest route,
     *         or an empty list if no path exists
     */
    public List<Road> findRoute(final Intersection theStart, final Intersection theEnd) {

        /* setup */
        //nodes that need to be explored, sorted by lowest f score
        PriorityQueue<Node> toExplore = new PriorityQueue<>(
                //sorts the PQ by best f score
                (nodeA, nodeB) -> Double.compare(nodeA.f, nodeB.f));

        //tracks best travel time from start at each intersection
        //intersection, g
        HashMap<Intersection, Double> gScore = new HashMap<>();

        //tracks previous intersections
        HashMap<Intersection, Intersection> cameFrom = new HashMap<>();

        //for walkBack, tracks the road taken at a certain intersection
        HashMap<Intersection, Road> roadTaken = new HashMap<>();

        //set start g to 0.0
        gScore.put(theStart, 0.0);
        //add start to PQ - algorithm starts at the beginning and looks at adjacent nodes to see what has the best score
        toExplore.add(new Node((theStart), estimatedTime(theStart, theEnd)));


        /* algorithm */
        while (!toExplore.isEmpty()) {

            //pop the fastest node off (first case would be start)
            Node current = toExplore.poll();

            if (current.intersection.equals(theEnd)) {

                List<Road> path = walkBack(cameFrom, roadTaken, theEnd);

                List<Intersection> intersections = new ArrayList<>();
                for (Road road : path) {
                    intersections.add(road.getStart());
                    intersections.add(road.getEnd());
                }

                mySafetyChecker.analyzeRoute(path, intersections);
                return path;
            }

            //looks at neighbors and gets f scores
            for (Road road : myMapGraph.getNeighbors(current.intersection)) {

                //skip, can't go this way
                if (road.isBlocked()) {
                    continue;
                }

                //setup for next loop
                Intersection next = road.getOtherEnd(current.intersection);
                //tentative g score
                double tentGScore = gScore.getOrDefault(current.intersection, Double.MAX_VALUE)
                        + road.getTravelTimeHours();

                //if the next intersection has a higher g score
                if (tentGScore < gScore.getOrDefault(next, Double.MAX_VALUE)) {
                    //update cameFrom HashMap for tracking path
                    //next came from current.intersection
                    cameFrom.put(next, current.intersection);
                    //put new higher gScore into HashMap for next intersection
                    gScore.put(next, tentGScore);
                    //add next as a new Node to be explored
                    toExplore.add(new Node((next), tentGScore + estimatedTime(next, theEnd)));
                    //this is for walkBack, makes it easier to track what Road was taken at an Intersection
                    roadTaken.put(next, road);
                }
            }
        }
        //returns empty list in case there is no path
        return Collections.emptyList();
    }

    /**
     * Estimates the travel time in hours between two intersections using the
     * Haversine great-circle distance formula divided by a nominal 60 mph.
     *
     * @param theStart - the starting intersection
     * @param theEnd - the destination intersection
     * @return the estimated travel time in hours
     */
    private double estimatedTime(final Intersection theStart, final Intersection theEnd) {

        //looked up haversine formula for calculating this
        final double R = 3958.8;
        double lat1 = Math.toRadians(theStart.getX());
        double lat2 = Math.toRadians(theEnd.getX());
        double dLat = Math.toRadians(theStart.getX() - theEnd.getX());
        double dLon = Math.toRadians(theStart.getY() - theEnd.getY());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double miles = R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return miles / 60.0;
    }

    /**
     * Reconstructs the road path from start to end by walking backwards
     * through the cameFrom map built during A*.
     *
     * @param cameFrom - maps each intersection to the intersection it was reached from
     * @param roadTaken - maps each intersection to the road used to reach it
     * @param theEnd - the destination intersection to walk back from
     * @return an ordered list of roads from start to end
     */
    private List<Road> walkBack(final HashMap<Intersection, Intersection> cameFrom,
                                final HashMap<Intersection, Road> roadTaken, final Intersection theEnd) {

        //pointer
        Intersection current = theEnd;
        //stores the path
        LinkedList<Road> path = new LinkedList<>();

        //while roadTaken has the current Intersection in it
        while (roadTaken.containsKey(current)) {
            //add intersection to front of list
            path.addFirst(roadTaken.get(current));
            //walk backwards to next intersection
            current = cameFrom.get(current);
        }
        return path;
    }

    /**
     * Internal node used by the A* priority queue.
     * Holds an intersection and its f-score (g + h).
     */
    private static class Node {
        Intersection intersection;
        //total
        double f;

        /**
         * Constructs a Node with the given intersection and f-score.
         *
         * @param theIntersection - the intersection at this node
         * @param theF - the f-score (cumulative g + heuristic h)
         */
        Node(Intersection theIntersection, double theF) {
            intersection = theIntersection;
            f = theF;
        }
    }

    /**
     * Converts an ordered list of roads into an ordered list of GeoPositions
     * suitable for the route painter. Follows the road chain correctly in either
     * direction since roads in the path may be stored with start/end in either order.
     *
     * @param thePath - the ordered list of roads forming the route
     * @return the ordered list of geographic positions along the route
     */
    private List<GeoPosition> convertPathToGeoPositions(final List<Road> thePath) {
        List<GeoPosition> positions = new ArrayList<>();

        if (thePath.isEmpty()) {
            return positions;
        }

        positions.add(myStartPoint);
        Intersection current = thePath.get(0).getStart();

        for (Road road : thePath) {
            Intersection next;

            if (road.getStart().equals(current)) {
                next = road.getEnd();
            } else if (road.getEnd().equals(current)) {
                next = road.getStart();
            } else {
                current = road.getStart();
                next = road.getEnd();
            }

            positions.add(new GeoPosition(next.getX(), next.getY()));
            current = next;
        }

        positions.add(myEndPoint);
        return positions;
    }

    /**
     * Returns the most recently calculated route as an ordered list of GeoPositions.
     *
     * @return the calculated route, or an empty list if no route has been calculated
     */
    public List<GeoPosition> getCalculatedRoute() {
        return myCalculatedRoute;
    }

    /**
     * Returns the SafetyChecker associated with this planner.
     * Used by the controller to read hazards and display them in the safety log.
     *
     * @return the SafetyChecker instance
     */
    public SafetyChecker getSafetyChecker() {
        return mySafetyChecker;
    }
}