package controller;

import model.*;
import view.PlannerDashboard;

import javax.swing.*;
import java.sql.SQLException;

/**
 * Entry point for the Car Route Planner application.
 * Initializes the map graph, route planner, database manager, and main dashboard,
 * then wires them together via the controller.
 *
 *  @author Chene van der Walt, Sofiia Kabaldina
 *  @version Spring 2026
 */
public class CarPlannerApp {

    /**
     * Launches the Car Route Planner application on the Swing event dispatch thread.
     * Loads the map from {@code map1.osm}, sets up the {@link model.RoutePlanner},
     * {@link model.DatabaseManager}, and {@link view.PlannerDashboard}, and
     * connects them through the {@link CarPlannerController}.
     *
     * @param args command-line arguments (not used)
     * @throws RuntimeException if the {@link model.DatabaseManager} cannot be initialized
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MapGraph graph = new MapGraph();

            try {
                MapLoader.loadFromFile(graph, "map1.osm");
            } catch (Exception e) {
                System.out.println("Could not load map.osm, using simulated map. " + e.getMessage());
            }

            RoutePlanner routePlanner = new RoutePlanner(graph);
            PlannerDashboard myDashboard = new PlannerDashboard();
            DatabaseManager databaseManager = null;
            try {
                databaseManager = new DatabaseManager();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            CarPlannerController carPlannerController = new CarPlannerController(myDashboard, routePlanner, graph, databaseManager);
            myDashboard.setVisible(true);
        });
    }
}