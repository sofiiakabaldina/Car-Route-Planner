package controller;

import model.*;
import view.*;
import org.jxmapviewer.viewer.GeoPosition;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * MVC controller that connects the {@link view.PlannerDashboard} (view) to the
 * {@link model.RoutePlanner}, {@link model.MapGraph}, {@link model.EnvironmentSimulator},
 * {@link model.DatabaseManager}, and {@link model.SafetyChecker} (model).
 * Registers listeners on both the view and the model, and reacts to property-change
 * events fired by the model to keep the UI in sync.
 *
 * @author Chene van der Walt, Sofiia Kabaldina
 * @version Spring 2026
 *
 */
public class CarPlannerController implements PropertyChangeListener {

    private final PlannerDashboard    myPlannerDashboard;
    private final RoutePlanner        myRoutePlanner;
    private final MapGraph            myGraph;
    private final EnvironmentSimulator myEnvironmentSimulator;
    private final DatabaseManager      myDatabaseManager;
    private final SafetyChecker     mySafetyChecker;

    /**
     * Creates the controller, wires all model-to-view and view-to-model listeners,
     * and shows the initial status prompt in the safety log.
     *
     * @param thePlannerDashboard the main application window
     * @param theRoutePlanner     the route-planning model
     * @param theGraph            the map graph containing intersections and roads
     * @param theDatabaseManager  the database manager for saving and loading routes
     */
    public CarPlannerController(final PlannerDashboard thePlannerDashboard,
                                final RoutePlanner theRoutePlanner,
                                final MapGraph theGraph,
                                final DatabaseManager theDatabaseManager) {

        myPlannerDashboard    = thePlannerDashboard;
        myRoutePlanner        = theRoutePlanner;
        myGraph               = theGraph;
        myEnvironmentSimulator = new EnvironmentSimulator(theGraph);
        myDatabaseManager = theDatabaseManager;
        mySafetyChecker = myRoutePlanner.getSafetyChecker();


        myEnvironmentSimulator.addPropertyChangeListener(this);
        myRoutePlanner.addPropertyChangeListener(this);
        myDatabaseManager.addPropertyChangeListener(this);
        myPlannerDashboard.addDeleteRouteListener(e -> deleteRoute());

        // Wire simulation panel buttons
        SimulationPanel simPanel = myPlannerDashboard.getSimulationPanel();
        simPanel.addApplyListener(e -> applyConditions());
        simPanel.addResetListener(e -> resetConditions());

        // Listen to model property changes
        myRoutePlanner.addPropertyChangeListener(this);

        addListeners();
        myPlannerDashboard.updateSafetyLog("Click on the map to set your starting point.");
        myPlannerDashboard.resetConditionsDisplay();
    }

    // ── Listener wiring ───────────────────────────────────────────────────────

    /** Registers all view listeners (menu bar, control panel, and map). */
    private void addListeners() {
        addMenuBarListeners();
        addControlPanelListeners();
        addMapListener();
    }

    /** Registers listeners for all menu-bar actions (About, Exit, Instructions, Load, Save). */
    private void addMenuBarListeners() {
        myPlannerDashboard.addAboutListener(e -> about());
        myPlannerDashboard.addExitListener(e -> exit());
        myPlannerDashboard.addInstructionsListener(e -> instructions());
        myPlannerDashboard.addLoadRouteListener(e -> loadRoute());
        myPlannerDashboard.addSaveRouteListener(e -> saveRoute());
    }

    /** Registers listeners for the control panel buttons (Plan Route, Simulate, Clear). */
    private void addControlPanelListeners() {
        myPlannerDashboard.addPlanRouteListener(e -> planRoute());
        myPlannerDashboard.addSimulateConditionsListener(e -> simulateConditions());
        myPlannerDashboard.addClearListener(e -> clearMap());
    }

    /** Registers the single map-click listener on the dashboard. */
    private void addMapListener() {
        // Single listener registered once — no double-registration
        myPlannerDashboard.setMapClickedListener(this::onMapClicked);
    }

    // ── Map click ─────────────────────────────────────────────────────────────

    /**
     * Handles a map click by snapping to the nearest intersection and registering
     * it as either the start or end point. If both points are already set, the
     * existing route is cleared and a fresh selection begins.
     *
     * @param thePosition the geographic position where the user clicked
     */
    public void onMapClicked(final GeoPosition thePosition) {
        // If both points are already set, clicking starts a fresh route
        if (myRoutePlanner.getMyStartPoint() != null
                && myRoutePlanner.getMyEndPoint() != null) {
            myRoutePlanner.clearPoints();
            myPlannerDashboard.clearMap();
        }

        Intersection nearest = myGraph.getNearestIntersection(
                thePosition.getLatitude(),
                thePosition.getLongitude()
        );

        if (nearest == null) {
            myPlannerDashboard.updateSafetyLog(
                    "No nearby road/intersection found. Try clicking closer to a road."
            );
            return;
        }

        // IMPORTANT:
        // Keep the visible pin exactly where the user clicked.
        // Do NOT snap the pin here.
        myRoutePlanner.addPoint(thePosition);

        myPlannerDashboard.setWaypoints(
                myRoutePlanner.getMyStartPoint(),
                myRoutePlanner.getMyEndPoint()
        );

        updateStatusLog();
    }

    // ── Route planning ────────────────────────────────────────────────────────

    /**
     * Triggers route calculation if both start and end points have been selected.
     * Displays a warning dialog if the points have not been set yet.
     */
    public void planRoute() {
        if (!myRoutePlanner.routeSelected()) {
            JOptionPane.showMessageDialog(myPlannerDashboard,
                    "Click a start and end point on the map first.",
                    "No points selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        myPlannerDashboard.updateSafetyLog("Calculating route…");
        myRoutePlanner.calculateRoute();
    }

    /**
     * Clears all selected points and route overlays from the map and resets
     * the safety log prompt so the user can begin a new selection.
     */
    public void clearMap() {
        myRoutePlanner.clearPoints();
        myPlannerDashboard.clearMap();
        myPlannerDashboard.updateSafetyLog("Map cleared. Click to set a new start point.");
    }

    // ── Simulation ────────────────────────────────────────────────────────────

    /** Opens the simulation panel so the user can adjust weather, traffic, and obstacle settings. */
    private void simulateConditions() {
        myPlannerDashboard.showSimulationPanel();
    }

    /**
     * Reads the current simulation-panel settings (weather, traffic density, obstacle
     * density) and applies them to every road and intersection in the graph.
     * Randomly blocks roads according to the obstacle density using a fixed seed for
     * reproducibility, updates the obstacle painter, and recalculates the active route.
     */
    private void applyConditions() {
        SimulationPanel simPanel = myPlannerDashboard.getSimulationPanel();
        Environment selectedWeather = simPanel.getSelectedWeather();
        double trafficDensity = simPanel.getTrafficDensity();
        int trafficPercent = (int) Math.round(trafficDensity * 100);
        double obstacleDensity = simPanel.getObstacleDensity();

        // First reset all blocked status so previous obstacles don't stack
        for (Road road : myGraph.getRoads()) {
            road.setBlocked(false);
        }
        for (Intersection intersection : myGraph.getIntersections()) {
            intersection.setBlocked(false);
        }

        // Apply weather and traffic to every road
        for (Road road : myGraph.getRoads()) {
            myEnvironmentSimulator.applyWeatherRoad(selectedWeather, road);
            myEnvironmentSimulator.applyTrafficRoad(road, trafficDensity);
        }

        // Collect positions of blocked roads to show on map
        List<GeoPosition> obstaclePositions = new ArrayList<>();

        // Randomly block roads based on obstacle density
        // Uses a fixed seed so the same slider value always blocks the same roads
        // making results consistent and reproducible
        if (obstacleDensity > 0) {

            List<Road> allRoads = myGraph.getRoads();
            int roadsToBlock = (int) Math.round(allRoads.size() * obstacleDensity);
            Random random = new Random(42); // fixed seed for consistency
            List<Road> shuffled = new ArrayList<>(allRoads);
            Collections.shuffle(shuffled, random);
            for (int i = 0; i < roadsToBlock && i < shuffled.size(); i++) {
                myEnvironmentSimulator.blockRoad(shuffled.get(i));
            }

            for (Road road : shuffled.subList(0, Math.min(roadsToBlock, shuffled.size()))) {
                // Use the midpoint between start and end of the road
                double midLat = (road.getStart().getX() + road.getEnd().getX()) / 2.0;
                double midLon = (road.getStart().getY() + road.getEnd().getY()) / 2.0;
                obstaclePositions.add(new GeoPosition(midLat, midLon));
            }
        }
        myPlannerDashboard.setObstaclePainter(new ObstaclePainter(obstaclePositions));

        // Update the Conditions section in the safety panel
        myPlannerDashboard.updateConditions(selectedWeather, trafficPercent);
        myPlannerDashboard.getSimulationPanel().setVisible(false);
        // Recalculate route automatically so stats update immediately
        if (myRoutePlanner.routeSelected()) {
            myRoutePlanner.calculateRoute();
        } else {
            myPlannerDashboard.updateSafetyLog(
                    "Conditions applied — select a route to see updated results.");
        }
    }

    /**
     * Resets all environmental conditions to their defaults (clear weather, no traffic,
     * no obstacles), clears obstacle overlays, and recalculates the active route if one exists.
     */
    private void resetConditions() {
        myEnvironmentSimulator.resetConditions(myGraph);
        myPlannerDashboard.getSimulationPanel().resetControls();
        myPlannerDashboard.resetConditionsDisplay();
        myPlannerDashboard.updateSafetyLog("Conditions reset — roads are clear with no traffic.");
        myPlannerDashboard.setObstaclePainter(new ObstaclePainter(new ArrayList<>()));
        myPlannerDashboard.getSimulationPanel().setVisible(false);
        // Recalculate if a route is active so stats refresh
        if (myRoutePlanner.routeSelected()) {
            myRoutePlanner.calculateRoute();
        }
    }

    /**
     * Maps an average risk factor to a route color: green for low risk (&lt;0.3),
     * orange for moderate risk (&lt;0.6), and red for high risk (&ge;0.6).
     *
     * @param theRisk the average risk factor in the range [0.0, 1.0]
     * @return the {@link Color} representing the safety level of the route
     */
    private Color getRouteColor(final double theRisk) {
        if(theRisk < 0.3) {
            return new Color(0, 160, 0); // safe = green
        }  else if(theRisk < 0.6) {
            return new Color(230, 150, 0); // moderate = orange
        } else {
            return Color.RED; // high = red
        }

    }

    // ── Property change handler ───────────────────────────────────────────────

    /**
     * Responds to property-change events fired by the model.
     * Handled events:
     * <ul>
     *   <li>{@code "hazardsUpdated"} — refreshes the safety-hazard report</li>
     *   <li>{@code "conditionsReset"} — resets the conditions display</li>
     *   <li>{@code "routeSaved"} — confirms the save in the safety log</li>
     *   <li>{@code "routeUpdated"} — enables or disables the Directions button</li>
     *   <li>{@code "alternativeFound"} — informs the user whether a safer alternative was found</li>
     *   <li>{@code "routeCalculated"} — delegates to {@link #onRouteCalculated}</li>
     * </ul>
     *
     * @param theEvent the property-change event from the model
     */
    @Override
    public void propertyChange(final PropertyChangeEvent theEvent) {
        switch (theEvent.getPropertyName()) {
            case "hazardsUpdated":
                myPlannerDashboard.updateSafetyReport(
                        mySafetyChecker.getHazards()
                );
                break;

            case "conditionsReset":
                myPlannerDashboard.resetConditionsDisplay();
                break;

            case "routeSaved":
                myPlannerDashboard.updateSafetyLog("Route saved successfully.");
                break;

            case "routeUpdated":
                myPlannerDashboard.setGetDirectionsEnabled(
                        (Boolean) theEvent.getNewValue());
                break;
            case "alternativeFound":
                // Show popup before routeCalculated fires and draws the route
                boolean found = (Boolean) theEvent.getNewValue();
                if (found) {
                    JOptionPane.showMessageDialog(
                            myPlannerDashboard,
                            "The original route was unsafe.\n"
                                    + "An alternative safer route has been found and will be displayed.",
                            "Alternative Route Found",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } else {
                    JOptionPane.showMessageDialog(
                            myPlannerDashboard,
                            "The original route was unsafe, but no safer alternative could be found.\n"
                                    + "The original route will be displayed — proceed with caution.",
                            "No Safe Alternative",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
                break;
            case "routeCalculated":
                onRouteCalculated(theEvent);
                break;

            default:
                break;
        }
    }

    /**
     * Handles the {@code "routeCalculated"} event by drawing the route on the map,
     * computing distance and travel time, and updating the route-info and safety panels.
     * If any road along the route is undriveable, the user is prompted to reset conditions.
     *
     * @param theEvent the property-change event (unused; route data is pulled from the model)
     */
    private void onRouteCalculated(final PropertyChangeEvent theEvent) {
        final List<GeoPosition> route = myRoutePlanner.getCalculatedRoute();

        if (route == null || route.isEmpty()) {
            JOptionPane.showMessageDialog(myPlannerDashboard,
                    "No route could be found between those two points.\n"
                            + "Try clicking locations that are on or near roads.",
                        "Route Not Found", JOptionPane.WARNING_MESSAGE);
            myPlannerDashboard.updateSafetyLog("No route found. Try different start/end points.");
            return;
        }

        Color routeColor = getRouteColor(mySafetyChecker.getAverageRiskFactor());
        myPlannerDashboard.setRoutePainter(
                new RoutePainter(route, routeColor));

        myPlannerDashboard.setWaypoints(
                myRoutePlanner.getMyStartPoint(),
                myRoutePlanner.getMyEndPoint());

        // Compute route stats from the planned roads in SafetyChecker
        SafetyChecker safetyChecker = myRoutePlanner.getSafetyChecker();
        List<Road> roads = safetyChecker.getPlannedRoads();

        // Check if any road is undriveable before calculating time
        boolean hasUndriveableRoad = false;
        for (Road road : roads) {
            if (road.isEffectivelyUndrivable()) {
                hasUndriveableRoad = true;
                break;
            }
        }

        if (hasUndriveableRoad) {
            int choice = JOptionPane.showConfirmDialog(
                    myPlannerDashboard,
                    "Current conditions make parts of this route extremely slow or undriveable.\n"
                            + "Would you like to reset conditions and recalculate?",
                    "Route May Be Unsafe",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (choice == JOptionPane.YES_OPTION) {
                resetConditions();
                myRoutePlanner.calculateRoute();
                return;
            }
            // User said no — show distance but flag time as unavailable
            double totalDistanceMiles = 0;
            for (Road road : roads) {
                totalDistanceMiles += road.getDistanceInMiles();
            }
            myPlannerDashboard.updateRouteInfo(
                    totalDistanceMiles, 0,
                    roads.size(), safetyChecker.getAverageRiskFactor());
            myPlannerDashboard.updateSafetyLog(
                    "Warning: Route time cannot be calculated — conditions are too severe.\n"
                            + "Consider resetting conditions or choosing a different route.");
            myPlannerDashboard.updateSafetyReport(safetyChecker.getHazards());
            return;
        }

        // Normal case — calculate distance and time
        double totalDistanceMiles = 0;
        double totalTimeHours     = 0;
        for (Road road : roads) {
            totalDistanceMiles += road.getDistanceInMiles();
            totalTimeHours     += road.getTravelTimeHours();
        }

        String distanceText = String.format("%.2f mi", totalDistanceMiles);
        String timeString = SafetyPanel.formatTravelTime(totalTimeHours);

        GeoPosition popupPosition = route.get(route.size() / 2);

        myPlannerDashboard.setRouteInfoPainter(new RouteInfoPainter(popupPosition, timeString, distanceText));

        myPlannerDashboard.updateRouteInfo(
                totalDistanceMiles,
                totalTimeHours,
                roads.size(),
                safetyChecker.getAverageRiskFactor());

        // Update safety report
        myPlannerDashboard.updateSafetyReport(safetyChecker.getHazards());
    }

    /**
     * Updates the safety log with the current start and end coordinates, or with
     * prompts guiding the user to select the next point.
     */
    public void updateStatusLog() {
        final GeoPosition start = myRoutePlanner.getMyStartPoint();
        final GeoPosition end   = myRoutePlanner.getMyEndPoint();
        final StringBuilder sb  = new StringBuilder();

        if (start == null) {
            sb.append("Click a point on the map to set the start.");
        } else {
            sb.append(String.format("Start:  Lat %.5f  Lon %.5f",
                    start.getLatitude(), start.getLongitude()));

            if (end == null) {
                sb.append("\n\nClick a second point to set the destination.");
            } else {
                sb.append(String.format("End:    Lat %.5f  Lon %.5f",
                        end.getLatitude(), end.getLongitude()));
                sb.append("\n\nRoute ready — click 'Directions' to calculate.");
            }
        }
        myPlannerDashboard.updateSafetyLog(sb.toString());
    }

    /** Prompts the user to confirm exit and terminates the application if confirmed. */
    private void exit() {
        int result = JOptionPane.showConfirmDialog(myPlannerDashboard,
                "Are you sure you want to exit?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    /**
     * Saves the currently selected route. Displays a warning if no route has been selected,
     * or an informational dialog while the save is in progress.
     */
    private void saveRoute() {

        if (!myRoutePlanner.routeSelected()) {
            JOptionPane.showMessageDialog(myPlannerDashboard,
                    "No route to save. Please select a start and end point.",
                    "Save Route", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (myDatabaseManager == null) {
            JOptionPane.showMessageDialog(myPlannerDashboard,
                    "Database unavailable.",
                    "Save Route", JOptionPane.ERROR_MESSAGE);
        }

        SafetyChecker sc = myRoutePlanner.getSafetyChecker();
        List<Road> roads = sc.getPlannedRoads();

        double totalDistance = 0;
        double totalTime = 0;

        for (Road road : roads) {
            totalDistance += road.getDistanceInMiles();
            totalTime += road.getTravelTimeHours();
        }

        int id = myDatabaseManager.getNextId();

        //prints latitude/longitude as the start and end instead of OSM labels (random numbers essentially)
        String start = String.format("%.5f, %.5f",
                myRoutePlanner.getMyStartPoint().getLatitude(),
                myRoutePlanner.getMyStartPoint().getLongitude());

        String end = String.format("%.5f, %.5f",
                myRoutePlanner.getMyEndPoint().getLatitude(),
                myRoutePlanner.getMyEndPoint().getLongitude());

        RouteRecord record = new RouteRecord(
                id,
                start,
                end,
                totalDistance,
                totalTime,
                sc.getHazards()
        );

        myDatabaseManager.saveRoute(record);

        JOptionPane.showMessageDialog(myPlannerDashboard,
                "Route saved.", "Save Route", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Loads a previously saved route and displays an informational dialog during loading. */
    private void loadRoute() {

        if (myDatabaseManager == null) {
            JOptionPane.showMessageDialog(myPlannerDashboard,
                    "Database unavailable.", "Load Route", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<RouteRecord> routes = myDatabaseManager.loadAllRoutes();

        if (routes.isEmpty()) {
            JOptionPane.showMessageDialog(myPlannerDashboard,
                    "No saved routes found.", "Load Route",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        //dropdown format
        String[] options = new String[routes.size()];
        for (int i = 0; i < routes.size(); i++) {
            RouteRecord r = routes.get(i);
            options[i] = String.format("ID: %d  |  %s → %s  (%.2f mi)",
                    r.getMyID(),
                    r.getMyStartLabel(),
                    r.getMyEndLabel(),
                    r.getMyDistance());
        }

        String selected = (String) JOptionPane.showInputDialog(
                myPlannerDashboard,
                "Select a saved route:",
                "Load Route",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);

        if (selected == null) return;

        int index = java.util.Arrays.asList(options).indexOf(selected);
        RouteRecord record = routes.get(index);

        int totalMinutes = (int) Math.round(record.getMyDuration() * 60);
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;

        String time;

        //format
        if (hours > 0) {
            time = String.format("%d hr %d min", hours, minutes);
        } else {
            time = String.format("%d min", minutes);
        }

        myPlannerDashboard.updateSafetyLog(
                String.format("Loaded route: %s → %s%nDistance: %.2f mi%nTime: %s%nHazards: %s",
                        record.getMyStartLabel(),
                        record.getMyEndLabel(),
                        record.getMyDistance(),
                        time,
                        record.getMyHazards().isEmpty() ? "None" :
                                String.join(", ", record.getMyHazards())));

        myPlannerDashboard.updateSafetyReport(record.getMyHazards());
        // Parse saved lat/lon strings back into GeoPositions
        try {
            String[] startParts = record.getMyStartLabel().split(",");
            String[] endParts = record.getMyEndLabel().split(",");

            double startLat = Double.parseDouble(startParts[0].trim());
            double startLon = Double.parseDouble(startParts[1].trim());
            double endLat = Double.parseDouble(endParts[0].trim());
            double endLon = Double.parseDouble(endParts[1].trim());

            GeoPosition startPos = new GeoPosition(startLat, startLon);
            GeoPosition endPos = new GeoPosition(endLat, endLon);

            // Feed points back into the planner and recalculate
            myRoutePlanner.clearPoints();
            myRoutePlanner.addPoint(startPos);
            myRoutePlanner.addPoint(endPos);

            myPlannerDashboard.setWaypoints(startPos, endPos);
            myRoutePlanner.calculateRoute();

        } catch (Exception e) {
            System.out.println("Could not reparse coordinates: " + e.getMessage());
        }
    }

    /**
     * Displays a dropdown of all saved routes and deletes the one selected by the user.
     * Prompts for confirmation before permanently removing the route from the database.
     * Shows an informational dialog if no saved routes exist or the database is unavailable.
     */
    private void deleteRoute() {
        if (myDatabaseManager == null) {
            JOptionPane.showMessageDialog(myPlannerDashboard,
                    "Database unavailable.", "Delete Route", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<RouteRecord> routes = myDatabaseManager.loadAllRoutes();

        if (routes.isEmpty()) {
            JOptionPane.showMessageDialog(myPlannerDashboard,
                    "No saved routes to delete.", "Delete Route",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] options = new String[routes.size()];
        for (int i = 0; i < routes.size(); i++) {
            RouteRecord r = routes.get(i);
            options[i] = String.format("ID: %d  |  %s → %s  (%.2f mi)",
                    r.getMyID(),
                    r.getMyStartLabel(),
                    r.getMyEndLabel(),
                    r.getMyDistance());
        }

        String selected = (String) JOptionPane.showInputDialog(
                myPlannerDashboard,
                "Select a route to delete:",
                "Delete Route",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);

        if (selected == null) return;

        int index = java.util.Arrays.asList(options).indexOf(selected);
        RouteRecord record = routes.get(index);

        int confirm = JOptionPane.showConfirmDialog(myPlannerDashboard,
                "Are you sure you want to delete this route?\n" + selected,
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        myDatabaseManager.deleteRoute(record.getMyID());

        JOptionPane.showMessageDialog(myPlannerDashboard,
                "Route deleted.", "Delete Route", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Displays the About dialog with a brief description of the application. */
    private void about() {
        JOptionPane.showMessageDialog(myPlannerDashboard,
                "Route-planning and safety-checking application for autonomous cars.\n"
                        + "The system generates routes between user-specified points and simulates\n"
                        + "environmental conditions that may affect route safety.",
                "About Self-Driving Car Route Planner and Safety Checker",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /** Displays the Instructions dialog explaining the five steps to plan and simulate a route. */
    private void instructions() {
        JOptionPane.showMessageDialog(myPlannerDashboard,
                "How to use Self-Driving Car Route Planner and Safety Checker:\n\n"
                        + "1. Click on the map to select a starting point.\n"
                        + "2. Click a second location to set the destination.\n"
                        + "3. Press 'Directions' to calculate the fastest path.\n"
                        + "4. Use 'Simulate Conditions' to apply weather and traffic.\n"
                        + "5. Save or load routes from the File menu.",
                "Instructions",
                JOptionPane.INFORMATION_MESSAGE);
    }
}