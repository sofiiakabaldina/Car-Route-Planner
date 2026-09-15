package model;

import java.util.*;

/**
 * Represents the road network as a directed graph of intersections and roads.
 * Stores intersections as vertices and roads as directed edges in an adjacency list.
 * Provides nearest-intersection lookup for snapping user-clicked map coordinates
 * to real graph nodes before routing.
 *
 * @author Abby Hinds
 * @version Spring 2026
 */
public class MapGraph {

    /** All intersections (vertices) in the graph. */
    private final List<Intersection> myIntersections;
    /** All roads (edges) in the graph. */
    private final List<Road> myRoads;
    /**
     * Adjacency list mapping each intersection to the roads connected to it.
     * Each road appears in the lists of both its start and end intersections.
     */
    private final Map<Intersection, List<Road>> myAdjacencyList;

    /**
     * Constructs an empty MapGraph with no intersections or roads.
     */
    public MapGraph() {
        myIntersections = new ArrayList<>();
        myRoads = new ArrayList<>();
        myAdjacencyList = new HashMap<>();
    }

    /**
     * Adds an intersection to the graph and initializes its adjacency list entry.
     *
     * @param theIntersection - the intersection to add
     */
    public void addIntersection(final Intersection theIntersection) {
        myIntersections.add(theIntersection);
        myAdjacencyList.put(theIntersection, new ArrayList<>());
    }

    /**
     * Adds a road to the graph and registers it in the adjacency lists of both
     * its start and end intersections, allowing traversal in either direction.
     *
     * @param theRoad - the road to add
     */
    public void addRoad(final Road theRoad) {
        myRoads.add(theRoad);
        // add to adjacency list so we can look up neighbors
        myAdjacencyList
                .get(theRoad.getStart())
                .add(theRoad);

        myAdjacencyList
                .get(theRoad.getEnd())
                .add(theRoad);
    }

    /**
     * Returns all roads connected to the given intersection.
     * Used by the A* routing algorithm to explore neighbors.
     *
     * @param theIntersection - the intersection to look up
     * @return a list of roads connected to the intersection,
     *         or an empty list if the intersection is not in the graph
     */
    public List<Road> getNeighbors(final Intersection theIntersection) {
        return myAdjacencyList.getOrDefault(theIntersection, new ArrayList<>());
    }

    /**
     * Returns all intersections in the graph.
     *
     * @return the list of all intersections
     */
    public List<Intersection> getIntersections() {
        return myIntersections;
    }

    /**
     * Returns all roads in the graph.
     *
     * @return the list of all roads
     */
    public List<Road> getRoads() {
        return myRoads;
    }

    /**
     * Finds the intersection closest to the given latitude and longitude.
     * Used to snap a raw map click to the nearest real graph node before routing.
     *
     * @param theLat - the latitude of the point
     * @param theLon - the longitude of the point
     * @return the nearest intersection, or null if the graph is empty
     */
    public Intersection getNearestIntersection(final double theLat, final double theLon) {

        Intersection nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Intersection intersection : myIntersections) {
            //difference in distance between geographic point and intersection
            double distance = Math.abs(intersection.getX() - theLat)
                    + Math.abs(intersection.getY() - theLon);
            //found valid intersection
            if (distance < minDistance) {
                minDistance = distance;
                nearest = intersection;
            }
        }
        return nearest;
    }
}