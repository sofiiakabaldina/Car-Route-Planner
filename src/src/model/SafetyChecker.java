package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Analyzes a planned route for safety hazards and suggests alternative routes
 * when the primary route is unsafe. Hazards detected include blocked roads,
 * blocked intersections, sharp or hairpin turns, and dangerous weather and traffic
 * combinations. Fires a "hazardsUpdated" property change event after each analysis
 * so the view can refresh the safety log panel.
 *
 * @author Abby Hinds, Sofiia Kabaldina
 * @version Spring 2026
 */
public class SafetyChecker extends AbstractModel {

    /**
     * Average combined risk threshold above which a route is considered unsafe.
     * Roads with risk exceeding this are blocked when searching for alternatives.
     */
    private static final double UNSAFE_RISK_THRESHOLD = 0.6;

    /** Minimum angle change in degrees to classify a turn as anything other than straight. */
    private static final double GENTLE_TURN  = 30.0;
    /** Angle change in degrees at or above which a turn is classified as sharp. */
    private static final double SHARP_TURN   = 135.0;
    /** Angle change in degrees at or above which a turn is classified as a hairpin. */
    private static final double HAIRPIN_TURN = 170.0;

    //    private List<Road> myPlannedRoute;
    /** Roads in the most recently analyzed route. */
    private List<Road> myPlannedRoads;
    /** Intersections in the most recently analyzed route, two per road (start and end). */
    private List<Intersection> myPlannedIntersections;
    /** Hazard messages found during the last analyzeRoute call. */
    private final List<String> myHazards;
    /** Reference to the RoutePlanner, used to request alternative routes. */
    private RoutePlanner myRoutePlanner;

    /**
     * Constructs a SafetyChecker with empty road, intersection, and hazard lists.
     */
    public SafetyChecker() {
        myPlannedRoads = new ArrayList<>();
        myPlannedIntersections = new ArrayList<>();
        myHazards = new ArrayList<>();
    }

    /**
     * Analyzes the given route for safety hazards and updates the hazard list.
     * Checks for blocked roads, blocked intersections, sharp or hairpin turns,
     * and dangerous weather and traffic combinations.
     * Fires a "hazardsUpdated" property change event with the old and new hazard lists.
     *
     * @param theRoads - the ordered list of roads in the route
     * @param theIntersections - the list of intersections in the route (start and end per road)
     */
    public void analyzeRoute(final List<Road> theRoads, final List<Intersection> theIntersections) {

        List<String> oldHazards = new ArrayList<>(myHazards);

        myPlannedRoads = theRoads;
        myPlannedIntersections = theIntersections;

        //clear previous hazards from a different route
        myHazards.clear();

        if (hasBlockedRoads()) {
            myHazards.add("Route has blocked roads.");
        }

        if (hasBlockedIntersections()) {
            myHazards.add("Route has blocked intersections.");
        }

        if (hasSharpTurns()) {
            myHazards.add("Route contains sharp or hairpin turns.");
        }

        if (hasHighRiskCombination()) {
            myHazards.add("Route has a dangerous weather and traffic combination.");
        } else if (hasHighRisk()) {
            myHazards.add("Route has high overall risk.");
        }

        myChanges.firePropertyChange("hazardsUpdates", oldHazards, new ArrayList<>(myHazards));
    }

    /**
     * Returns the sum of combined risk factors across all roads in the current route.
     *
     * @return the total combined risk factor
     */
    public double getTotalRiskFactor() {
        double totalRiskFactor = 0.0;
        for (Road road : myPlannedRoads) {
            totalRiskFactor += road.getCombinedRiskFactor();
        }
        return totalRiskFactor;
    }

    /**
     * Returns the average combined risk factor per road in the current route.
     * Returns 0.0 if the route has no roads.
     *
     * @return the average risk factor
     */
    public double getAverageRiskFactor() {
        if (myPlannedRoads.isEmpty()) {
            return 0.0;
        }
        return getTotalRiskFactor() / myPlannedRoads.size();
    }

    /**
     * Returns true if the current route has no detected hazards.
     *
     * @return true if the route is safe, false if any hazards were found
     */
    public boolean isRouteSafe() {
        return myHazards.isEmpty();
    }

    /**
     * Returns true if any road in the current route is blocked.
     *
     * @return true if a blocked road exists on the route
     */
    public boolean hasBlockedRoads() {

        for (Road road : myPlannedRoads) {
            if (road.isBlocked()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if any intersection in the current route is blocked.
     *
     * @return true if a blocked intersection exists on the route
     */
    public boolean hasBlockedIntersections() {
        for (Intersection intersection : myPlannedIntersections) {
            if (intersection.isBlocked()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the average risk factor of the route exceeds the unsafe threshold.
     *
     * @return true if average risk is above an UNSAFE_RISK_THRESHOLD (0.6)
     */
    private boolean hasHighRisk() {
        return getAverageRiskFactor() > UNSAFE_RISK_THRESHOLD;
    }

    /**
     * Returns a copy of the hazard messages found during the last analyzeRoute call.
     *
     * @return a new list of hazard description strings
     */
    public List<String> getHazards() {
        return new ArrayList<>(myHazards);
    }

    /**
     * Returns an unmodifiable view of the planned roads in the current route.
     *
     * @return an unmodifiable list of roads
     */
    public List<Road> getPlannedRoads() {
        return Collections.unmodifiableList(myPlannedRoads);
    }

    /**
     * Sets the RoutePlanner reference so this checker can request alternative
     * routes.
     *
     * @param theRoutePlanner - the RoutePlanner to use for alternative route searches
     */
    public void setRoutePlanner(final RoutePlanner theRoutePlanner) {
        myRoutePlanner = theRoutePlanner;
    }

    /**
     * Suggests an alternative route by temporarily blocking high-risk roads and
     * roads involved in sharp or hairpin turns, then re-running A* from the same
     * start and end intersections. All temporarily blocked roads are unblocked
     * after the search regardless of outcome.
     * Returns an empty list if no roads were blocked or no alternative exists.
     *
     * @return an ordered list of roads forming the alternative route,
     *         or an empty list if no alternative could be found
     */
    public List<Road> suggestAlternative() {

        if (myPlannedRoads.isEmpty() || myRoutePlanner == null) {
            return Collections.emptyList();
        }

        // Get true start and end from the road list directly
        Intersection routeStart = myPlannedRoads.get(0).getStart();
        Intersection routeEnd   = myPlannedRoads.get(myPlannedRoads.size() - 1).getEnd();

        // Temporarily block all high-risk roads
        List<Road> blockedRoads = new ArrayList<>();

        for (Road road : myPlannedRoads) {
            if (road.getCombinedRiskFactor() > UNSAFE_RISK_THRESHOLD || road.isBlocked()) {
                road.setBlocked(true);
                blockedRoads.add(road);
            }
        }

        // Also block roads involved in sharp/hairpin turns
        if (myPlannedIntersections.size() >= 3) {
            for (int i = 0; i < myPlannedIntersections.size() - 2; i++) {
                Intersection a = myPlannedIntersections.get(i);
                Intersection b = myPlannedIntersections.get(i + 1);
                Intersection c = myPlannedIntersections.get(i + 2);

                TurnSharpness turn = classifyTurn(a, b, c);
                if (turn == TurnSharpness.SHARP || turn == TurnSharpness.HAIRPIN) {
                    // Block the road going INTO the sharp turn
                    int roadIndex = i / 2;
                    if (roadIndex < myPlannedRoads.size()) {
                        Road sharpRoad = myPlannedRoads.get(roadIndex);
                        if (!sharpRoad.isBlocked()) {
                            sharpRoad.setBlocked(true);
                            blockedRoads.add(sharpRoad);
                        }
                    }
                }
            }
        }

        System.out.println("Blocked for alt: risk roads=" +
                myPlannedRoads.stream().filter(r -> r.getCombinedRiskFactor() > UNSAFE_RISK_THRESHOLD).count() +
                ", sharp turn roads blocked=" + blockedRoads.size() +
                ", intersections=" + myPlannedIntersections.size());

        // If nothing was blocked, there's nothing we can do differently
        if (blockedRoads.isEmpty()) {
            return Collections.emptyList();
        }

        // Find alternative route avoiding those roads
        List<Road> newPath = myRoutePlanner.findRoute(routeStart, routeEnd);

        // Always unblock them after, no matter what
        for (Road road : blockedRoads) {
            road.setBlocked(false);
        }

        return newPath;
    }

    /**
     * Returns true if the route contains any sharp or hairpin turns by examining
     * every three consecutive intersections A → B → C and measuring the angle
     * change at B.
     *
     * @return true if at least one sharp or hairpin turn exists on the route
     */
    public boolean hasSharpTurns() {
        if (myPlannedIntersections.size() < 3) return false;

        for (int i = 0; i < myPlannedIntersections.size() - 2; i++) {
            Intersection pointA = myPlannedIntersections.get(i);
            Intersection pointB = myPlannedIntersections.get(i + 1);
            Intersection pointC = myPlannedIntersections.get(i + 2);

            TurnSharpness turn = classifyTurn(pointA, pointB, pointC);
            if (turn == TurnSharpness.SHARP || turn == TurnSharpness.HAIRPIN) {
                return true;
            }
        }
        return false;
    }


    /**
     * Returns a list of turn classifications for every three consecutive intersections
     * on the route.
     *
     * @return a list of TurnSharpness values, one per consecutive triple
     */
    public List<TurnSharpness> getAllTurns() {
        List<TurnSharpness> turns = new ArrayList<>();
        if (myPlannedIntersections.size() < 3) return turns;

        for (int i = 0; i < myPlannedIntersections.size() - 2; i++) {
            turns.add(classifyTurn(
                    myPlannedIntersections.get(i),
                    myPlannedIntersections.get(i + 1),
                    myPlannedIntersections.get(i + 2)
            ));
        }
        return turns;
    }

    /**
     * Classifies the sharpness of the turn at thePointB when travelling from
     * thePointA to thePointC, based on the change in compass bearing.
     *
     * @param thePointA - the intersection before the turn
     * @param thePointB - the intersection at the turn vertex
     * @param thePointC - the intersection after the turn
     * @return the TurnSharpness classification for this turn
     */
    public TurnSharpness classifyTurn(final Intersection thePointA,
                                      final Intersection thePointB,
                                      final Intersection thePointC) {
        double angleAB = calculateAngle(
                thePointA.getX(), thePointA.getY(),
                thePointB.getX(), thePointB.getY()
        );
        double angleBC = calculateAngle(
                thePointB.getX(), thePointB.getY(),
                thePointC.getX(), thePointC.getY()
        );

        double change = Math.abs(angleBC - angleAB);

        // Adjust to 0-180 range since a 270° change and a 90° change
        // are the same turn, just measured from opposite sides
        if (change > 180) {
            change = 360 - change;
        }

        if (change < GENTLE_TURN)  return TurnSharpness.STRAIGHT;
        if (change < SHARP_TURN)   return TurnSharpness.GENTLE;
        if (change < HAIRPIN_TURN) return TurnSharpness.SHARP;
        return TurnSharpness.HAIRPIN;
    }

    /**
     * Calculates the travel angle (0-360°) from one lat/lon point to another.
     * 0° = north, 90° = east, 180° = south, 270° = west.
     * This tells us which direction we are travelling on that road segment.
     *
     * @param theStartLat - Start Y point
     * @param theStartLon - Start X point
     * @param theEndLat - End Y point
     * @param theEndLon - End X point
     * @return angle in degrees, adjusted to stay within 0-360
     */
    private double calculateAngle(final double theStartLat, final double theStartLon,
                                  final double theEndLat,   final double theEndLon) {
        double startLatRad = Math.toRadians(theStartLat);
        double endLatRad   = Math.toRadians(theEndLat);
        double lonDiffRad  = Math.toRadians(theEndLon - theStartLon);

        double x = Math.sin(lonDiffRad) * Math.cos(endLatRad);
        double y = Math.cos(startLatRad) * Math.sin(endLatRad)
                - Math.sin(startLatRad) * Math.cos(endLatRad) * Math.cos(lonDiffRad);

        // Convert from radians to degrees and adjust to stay within 0-360
        double angle = Math.toDegrees(Math.atan2(x, y));
        return (angle + 360) % 360;
    }


    /**
     * Returns true if any road on the route has both bad weather (risk ≥ 0.5)
     * and heavy traffic (density ≥ 0.5) simultaneously, which is considered
     * a particularly dangerous combination.
     *
     * @return true if a dangerous weather and traffic combination exists
     */
    private boolean hasHighRiskCombination() {
        for (Road road : myPlannedRoads) {
            boolean badWeather = road.getCondition().getRiskFactor() >= 0.5;
            boolean heavyTraffic = road.getTrafficDensity() >= 0.5;
            if (badWeather && heavyTraffic) {
                return true;
            }
        }
        return false;
    }
}