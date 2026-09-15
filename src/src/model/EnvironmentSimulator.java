package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Simulates environmental conditions on roads and intersections in the map graph.
 * Applies weather conditions, traffic density to all roads, and blockages to individual
 * roads or intersections, and fires property change events to notify listeners of updates.
 *
 * @author Abby Hinds, Sofiia Kabaldina
 * @version Spring 2026
 */
public class EnvironmentSimulator extends AbstractModel {

    /** List of currently active environmental conditions. */
    private List<Environment> myActiveConditions;

    /**
     * Constructs an EnvironmentSimulator for the given map graph.
     * Initializes the active conditions list to empty.
     *
     * @param theGraph - the map graph this simulator operates on
     */
    public EnvironmentSimulator(final MapGraph theGraph) {

        myActiveConditions = new ArrayList<Environment>();

    }

    /**
     * Applies a weather condition to a specific road and notifies listeners.
     *
     * @param theCondition - the environment condition to apply
     * @param theRoad - the road to apply the condition to
     * @throws NullPointerException if theCondition or theRoad is null
     */
    public void applyWeatherRoad(final Environment theCondition, final Road theRoad) {

        Objects.requireNonNull(theCondition, "Environment condition cannot be null");
        Objects.requireNonNull(theRoad, "Road cannot be null");

        Environment oldCondition = theRoad.getCondition();
        theRoad.setCondition(theCondition);

        myChanges.firePropertyChange("weatherChanged", oldCondition, theCondition);
    }

    /**
     * Applies a traffic density value to a specific road and notifies listeners.
     * Density is clamped to [0.0, 1.0] by the Road class.
     *
     * @param theRoad - the road to apply traffic to
     * @param theDensity - the traffic density, where 0.0 is no traffic and 1.0 is gridlock
     * @throws NullPointerException if theRoad is null
     */
    public void applyTrafficRoad(final Road theRoad, final double theDensity) {

        Objects.requireNonNull(theRoad, "Road cannot be null");

        double oldDensity = theRoad.getTrafficDensity();
        theRoad.setTrafficDensity(theDensity);

        myChanges.firePropertyChange("trafficDensityChanged", oldDensity, theDensity);
    }

    /**
     * Marks a road as blocked, preventing A* from routing through it.
     *
     * @param theRoad - the road to block
     * @throws NullPointerException if theRoad is null
     */
    public void blockRoad(final Road theRoad) {

        Objects.requireNonNull(theRoad, "Road cannot be null");
        theRoad.setBlocked(true);
    }

    /**
     * Marks an intersection as blocked, preventing routing through it.
     *
     * @param theIntersection - the intersection to block
     * @throws NullPointerException if theIntersection is null
     */
    public void blockIntersection(final Intersection theIntersection) {

        Objects.requireNonNull(theIntersection, "Intersection cannot be null");
        theIntersection.setBlocked(true);
    }

    /**
     * Resets all roads and intersections in the graph to default conditions:
     * clear weather, zero traffic, and unblocked. Clears the active conditions
     * list and notifies listeners that conditions have been reset.
     *
     * @param theGraph the map graph whose roads and intersections to reset
     */
    public void resetConditions(final MapGraph theGraph) {
        for (Road road : theGraph.getRoads()) {
            road.setCondition(Environment.CLEAR);
            road.setTrafficDensity(0.0);
            road.setBlocked(false);
        }
        for (Intersection intersection : theGraph.getIntersections()) {
            intersection.setBlocked(false);
        }
        myActiveConditions.clear();

        myChanges.firePropertyChange("conditionReset", false, true);
    }

    /**
     * Returns the current environment condition of the given road.
     *
     * @param theRoad - the road to query
     * @return the current condition of the road
     * @throws NullPointerException if theRoad is null
     */
    public Environment getRoadCondition(final Road theRoad) {

        Objects.requireNonNull(theRoad, "Road cannot be null");
        return theRoad.getCondition();
    }

    /**
     * Returns the combined risk factor of the given road, accounting for
     * both weather conditions and traffic density.
     *
     * @param theRoad - the road to query
     * @return the combined risk factor as a double
     * @throws NullPointerException if theRoad is null
     */
    public double getRiskFactorRoad(final Road theRoad) {

        Objects.requireNonNull(theRoad, "Road cannot be null");
        return theRoad.getCombinedRiskFactor();
    }

}
