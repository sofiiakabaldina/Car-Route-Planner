package model;

/**
 * Represents a directed edge in the road network graph, connecting two intersections.
 * Each road stores its distance, speed limit, and dynamic conditions such as weather,
 * traffic density, and blocked status. Travel time and effective speed are computed
 * on demand from these conditions and are used by the A* routing algorithm.
 *
 * @author Sofiia Kabaldina
 * @version Spring 2026
 */
public class Road {

    /** Start coordinate of the road. */
    private final Intersection myStart;
    /** End coordinate of the road. */
    private final Intersection myEnd;
    /** The length of this road in miles. */
    private final double myDistanceInMiles;
    /** The posted speed limit of this road in miles per hour. */
    private final int mySpeedLimit;
    /** The current weather condition applied to this road. */
    private Environment myCondition;
    /** Whether this road is currently blocked and impassable. */
    private boolean myIsBlocked;
    /** The current traffic density on this road, ranging from 0.0 (none) to 1.0 (gridlock). */
    private double myTrafficDensity;

    /**
     * Constructs a Road between two intersections with a given distance and speed limit.
     * Weather condition defaults to CLEAR, blocked status to false, and traffic density to 0.0.
     *
     * @param theStart - the starting intersection of this road
     * @param theEnd - the ending intersection of this road
     * @param theDistanceInMiles - the length of this road segment in miles
     * @param theSpeedLimit - the posted speed limit in miles per hour
     */
    public Road(final Intersection theStart, final Intersection theEnd,
                final double theDistanceInMiles, final int theSpeedLimit) {
        myStart = theStart;
        myEnd = theEnd;
        myDistanceInMiles = theDistanceInMiles;
        mySpeedLimit = theSpeedLimit;
        myCondition = Environment.CLEAR; // no/clear weather by default
        myIsBlocked = false;
        myTrafficDensity = 0.0;
    }

    /**
     * Returns the starting intersection of this road.
     *
     * @return the start intersection
     */
    public Intersection getStart() {
        return myStart;
    }

    /**
     * Returns the ending intersection of this road.
     *
     * @return the end intersection
     */
    public Intersection getEnd() {
        return myEnd;
    }

    /**
     * Returns the intersection at the opposite end of this road from the given intersection.
     * Used by the A* algorithm to traverse the graph in reverse during backtracking.
     *
     * @param thePrevIntersection - the intersection currently being visited
     * @return the other endpoint of this road, or null if thePrevIntersection
     *         does not match either endpoint
     */
    public Intersection getOtherEnd(final Intersection thePrevIntersection) {
        if (thePrevIntersection.equals(myStart)) {
            return myEnd;
        } else if (thePrevIntersection.equals(myEnd)) {
            return myStart;
        }
        return null;
    }

    /**
     * Returns the length of this road segment in miles.
     *
     * @return the distance in miles
     */
    public double getDistanceInMiles() {
        return myDistanceInMiles;
    }

    /**
     * Returns the posted speed limit of this road in miles per hour.
     *
     * @return the speed limit in mph
     */
    public int getSpeedLimit() {
        return mySpeedLimit;
    }

    /**
     * Returns the current weather condition applied to this road.
     *
     * @return the current Environment condition
     */
    public Environment getCondition() {
        return myCondition;
    }

    /**
     * Sets the weather condition on this road.
     *
     * @param theCondition - the new Environment condition to apply
     */
    public void setCondition(final Environment theCondition) {
        myCondition = theCondition;
    }

    /**
     * Returns whether this road is currently blocked.
     *
     * @return true if the road is blocked, false otherwise
     */
    public boolean isBlocked() {
        return myIsBlocked;
    }

    /**
     * Sets the blocked status of this road.
     *
     * @param theBlocked - true to block this road, false to unblock it
     */
    public void setBlocked(final boolean theBlocked) {
        myIsBlocked = theBlocked;
    }

    /**
     * Returns the current traffic density on this road as a value between 0.0 and 1.0.
     *
     * @return the traffic density where 0.0 is no traffic and 1.0 is gridlock
     */
    public double getTrafficDensity() {
        return myTrafficDensity;
    }

    /**
     * Sets the traffic density on this road within the range [0.0, 1.0].
     *
     * @param theDensity - the desired traffic density from 0.0 (none) to 1.0 (gridlock)
     */
    public void setTrafficDensity(final double theDensity) {
        myTrafficDensity = Math.max(0.0, Math.min(1.0, theDensity));
    }

    /**
     * Calculates how fast you can actually travel on this road right now,
     * accounting for weather conditions and traffic density.
     * <p>
     * Formula: effectiveSpeed = speedLimit * (1 - weatherRisk) * (1 - trafficDensity)
     * <p>
     * weatherReduction: 1.0 means no weather impact, 0.0 means road is undriveable
     * trafficReduction: 1.0 means no traffic, 0.0 means gridlock
     * <p>
     * Example — HEAVY_SNOW (risk 0.6) + heavy traffic (density 0.7) on 35mph road:
     *   effectiveSpeed = 35 * (1 - 0.6) * (1 - 0.7)
     *                  = 35 * 0.4 * 0.3
     *                  = 4.2 mph
     * Example — CLEAR (risk 0.0) + no traffic (density 0.0) on 35mph road:
     *   effectiveSpeed = 35 * (1 - 0.0) * (1 - 0.0)
     *                  = 35 * 1.0 * 1.0
     *                  = 35 mph  (full speed, no reduction)
     *
     * @return the effective speed in mph after applying weather and traffic reductions
     */
    public double getEffectiveSpeed() {
        double weatherReduction = 1.0 - myCondition.getRiskFactor();
        double trafficReduction = 1.0 - myTrafficDensity;
        return (int) mySpeedLimit * weatherReduction * trafficReduction;
    }

    /**
     * Calculates how long it takes to drive this road segment in hours.
     * Returns Double.MAX_VALUE (essentially infinity) if the road is blocked
     * or if effective speed drops to zero, so A* never selects it.
     * <p>
     *  Example — 2 mile road, CLEAR (risk 0.0), no traffic (density 0.0):
     *   effectiveSpeed = 35 * 1.0 * 1.0 = 35 mph
     *   travelTime     = 2.0 / 35 = 0.057 hours (~3.4 minutes)
     * Example — 2 mile road, HEAVY_SNOW (risk 0.6), heavy traffic (density 0.7):
     *   effectiveSpeed = 35 * (1 - 0.6) * (1 - 0.7) = 4.2 mph
     *   travelTime     = 2.0 / 4.2 = 0.476 hours (~28 minutes)
     *
     * @return the travel time in hours, or Double.MAX_VALUE if impassable
     */
    public double getTravelTimeHours() {
        if (myIsBlocked) {
            return Double.MAX_VALUE;
        }
        double effectiveSpeed = getEffectiveSpeed();
        if (effectiveSpeed <= 0){
            return Double.MAX_VALUE;
        }
        return myDistanceInMiles / effectiveSpeed;
    }

    /**
     * Returns the combined risk factor for this road segment by summing the
     * weather risk factor and the current traffic density.
     * Used by SafetyChecker to evaluate route hazards.
     *
     * @return the combined risk as a double, where higher values indicate greater danger
     */
    public double getCombinedRiskFactor() {
        return myCondition.getRiskFactor() + myTrafficDensity;
    }

    /**
     * Returns true if current conditions make this road effectively undriveable,
     * defined as an effective speed below 2 mph.
     *
     * @return true if effective speed is below 2 mph, false otherwise
     */
    public boolean isEffectivelyUndrivable() {
        return getEffectiveSpeed() < 2.0;
    }

    /**
     * Returns a human-readable description of this road showing its endpoints,
     * distance, speed limit, and current weather condition.
     *
     * @return a formatted string in the form "Start → End (X.XX mi, Y mph, CONDITION)"
     */
    @Override
    public String toString() {
        return myStart.getLabel() + " → " + myEnd.getLabel() +
                " (" + String.format("%.2f", myDistanceInMiles) + " mi, " +
                mySpeedLimit + " mph, " + myCondition + ")";
    }
}