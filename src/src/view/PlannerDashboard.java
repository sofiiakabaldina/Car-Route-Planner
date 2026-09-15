package view;

import model.Environment;
import org.jxmapviewer.viewer.GeoPosition;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.function.Consumer;
/**
 * Main application window for the route planner.
 * Displays the map, control buttons, menu bar, safety panel,
 * and simulation dialog.
 *
 * @author Chene Van der Walt
 * @version Spring 2026
 */
public class PlannerDashboard extends JFrame {
    /** The current screen size used to size the main window. */
    private final Dimension myScreenSize = Toolkit.getDefaultToolkit().getScreenSize();

    /** The map panel used to display routes, waypoints, and obstacles. */
    private final MapPanel        myMapPanel;

    /** The menu bar containing file, help, and route options. */
    private final MenuBar         myMenuBar;

    /** The control panel containing main route-planning buttons. */
    private final ControlPanel    myControlPanel;

    /** The safety panel displaying route risk and hazard information. */
    private final SafetyPanel     mySafetyPanel;

    /** The simulation dialog used to apply weather, traffic, and obstacle conditions. */
    private final SimulationPanel mySimulationPanel;

    /**
     * Constructs the planner dashboard and initializes all main UI components.
     */
    public PlannerDashboard() {
        setTitle("Self-Driving Car Route Planner and Safety Checker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final int width = (int) (myScreenSize.width * 0.70);
        final int height = (int) (myScreenSize.height * 0.80);

        myControlPanel    = new ControlPanel();

        styleButtons(myControlPanel.getSimulateConditions());
        styleButtons(myControlPanel.getGetDirections());
        styleButtons(myControlPanel.getClear());

        myMenuBar         = new MenuBar();
        setJMenuBar(myMenuBar);

        // Left sidebar — control buttons stacked vertically
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.X_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        sidebar.add(myControlPanel.getSimulateConditions());
        sidebar.add(Box.createHorizontalStrut(20));
        sidebar.add(myControlPanel.getGetDirections());   // renamed getter
        sidebar.add(Box.createHorizontalStrut(20));
        sidebar.add(myControlPanel.getClear());

        myMapPanel        = new MapPanel();
        mySafetyPanel     = new SafetyPanel();
        mySimulationPanel = new SimulationPanel(this);

        setLayout(new BorderLayout());
        add(sidebar,        BorderLayout.NORTH);
        add(myMapPanel,     BorderLayout.CENTER);
        add(mySafetyPanel,  BorderLayout.SOUTH);

        setSize(width, height);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }

    /**
     * Sets the painter used to draw obstacles on the map.
     *
     * @param thePainter the obstacle painter to display
     */
    public void setObstaclePainter(final ObstaclePainter thePainter) {
        myMapPanel.setObstaclePainter(thePainter);
    }

    /**
     * Returns the map panel.
     *
     * @return the map panel used by the dashboard
     */
    public MapPanel getMapPanel() {
        return myMapPanel;
    }

    /**
     * Registers a listener for map click events.
     *
     * @param theListener the listener that receives clicked geographic positions
     */
    public void setMapClickedListener(final Consumer<GeoPosition> theListener) {
        myMapPanel.setMapClickListener(theListener);
    }

    /**
     * Sets the start and end waypoints on the map.
     *
     * @param theStart the starting geographic position
     * @param theEnd the ending geographic position
     */
    public void setWaypoints(final GeoPosition theStart, final GeoPosition theEnd) {
        myMapPanel.setWaypoints(theStart, theEnd);
    }

    /**
     * Clears the map and resets displayed route and hazard information.
     */
    public void clearMap() {
        myMapPanel.clearAll();
        mySafetyPanel.resetRouteInfo();
        mySafetyPanel.resetHazards();
    }

    /**
     * Sets the painter used to draw the calculated route.
     *
     * @param theRoutePainter the route painter to display
     */
    public void setRoutePainter(final RoutePainter theRoutePainter) {
        myMapPanel.setRoutePainter(theRoutePainter);
    }

    /**
     * Updates the safety log with a message.
     *
     * @param theMessage the message to display
     */
    public void updateSafetyLog(final String theMessage) {
        mySafetyPanel.updateLog(theMessage);
    }


    /**
     * Updates the route information displayed in the safety panel.
     *
     * @param theDistanceMiles the total route distance in miles
     * @param theTravelTimeHours the estimated travel time in hours
     * @param theRoadCount the number of roads included in the calculated route
     * @param theAvgRisk the average risk value for the route
     */
    public void updateRouteInfo(final double theDistanceMiles,
                                final double theTravelTimeHours,
                                final int theRoadCount,
                                final double theAvgRisk) {
        mySafetyPanel.updateRouteInfo(
                theDistanceMiles, theTravelTimeHours, theAvgRisk);
    }

    /**
     * Updates the displayed weather and traffic conditions.
     *
     * @param theWeather the current weather condition
     * @param theTrafficPercent the traffic density as a percentage
     */
    public void updateConditions(final Environment theWeather,
                                 final int theTrafficPercent) {
        mySafetyPanel.updateConditions(theWeather, theTrafficPercent);
    }


    /**
     * Updates the safety report with the specified hazards.
     *
     * @param theHazards the list of hazards detected along the route
     */
    public void updateSafetyReport(final List<String> theHazards) {
        mySafetyPanel.updateSafetyReport(theHazards);
    }

    /**
     * Resets the displayed simulation conditions to their default values.
     */
    public void resetConditionsDisplay() {
        mySafetyPanel.resetConditions();
    }


    /**
     * Enables or disables the Get Directions button.
     *
     * @param theEnabled true to enable the button; false to disable it
     */
    public void setGetDirectionsEnabled(final boolean theEnabled) {
        myControlPanel.getGetDirections().setEnabled(theEnabled);
    }

    /**
     * Returns the simulation panel used to configure route conditions.
     *
     * @return the simulation panel
     */
    public SimulationPanel getSimulationPanel() {
        return mySimulationPanel;
    }

    /**
     * Displays the simulation panel dialog.
     */
    public void showSimulationPanel() {
        mySimulationPanel.setVisible(true);
    }

    /**
     * Applies consistent styling to a dashboard button.
     *
     * @param theButton the button to style
     */
    private void styleButtons(final JButton theButton) {
        Dimension size = new Dimension(190, 42);

        theButton.setPreferredSize(size);
        theButton.setMinimumSize(size);
        theButton.setMaximumSize(size);

        theButton.setFocusPainted(false);
        theButton.setHorizontalAlignment(SwingConstants.LEFT);
        theButton.setHorizontalTextPosition(SwingConstants.RIGHT);
        theButton.setIconTextGap(6);

    }

    /**
     * Sets the painter used to display route information on the map.
     *
     * @param thePainter the route information painter to display
     */
    public void setRouteInfoPainter(final RouteInfoPainter thePainter) {
        myMapPanel.setRouteInfoPainter(thePainter);
    }


    /**
     * Registers a listener for the Save Route menu item.
     *
     * @param theListener the listener to notify when Save Route is selected
     */
    public void addSaveRouteListener(final ActionListener theListener) {
        myMenuBar.getSaveRoute().addActionListener(theListener);
    }

    /**
     * Registers a listener for the Load Route menu item.
     *
     * @param theListener the listener to notify when Load Route is selected
     */
    public void addLoadRouteListener(final ActionListener theListener) {
        myMenuBar.getLoadRoute().addActionListener(theListener);
    }

    /**
     * Registers a listener for the Delete Route menu item.
     *
     * @param theListener the listener to notify when Delete Route is selected
     */
    public void addDeleteRouteListener(final ActionListener theListener) {
        myMenuBar.getDeleteRoute().addActionListener(theListener);
    }

    /**
     * Registers a listener for the Exit menu item.
     *
     * @param theListener the listener to notify when Exit is selected
     */

    public void addExitListener(final ActionListener theListener) {
        myMenuBar.getExit().addActionListener(theListener);
    }

    /**
     * Registers a listener for the About menu item.
     *
     * @param theListener the listener to notify when About is selected
     */
    public void addAboutListener(final ActionListener theListener) {
        myMenuBar.getAbout().addActionListener(theListener);
    }

    /**
     * Registers a listener for the Instructions menu item.
     *
     * @param theListener the listener to notify when Instructions is selected
     */
    public void addInstructionsListener(final ActionListener theListener) {
        myMenuBar.getInstructions().addActionListener(theListener);
    }

    /**
     * Registers a listener for the Get Directions button.
     *
     * @param theListener the listener to notify when route planning is requested
     */
    public void addPlanRouteListener(final ActionListener theListener) {
        myControlPanel.getGetDirections().addActionListener(theListener);
    }

    /**
     * Registers a listener for the Simulate Conditions button.
     *
     * @param theListener the listener to notify when simulation settings are requested
     */
    public void addSimulateConditionsListener(final ActionListener theListener) {
        myControlPanel.getSimulateConditions().addActionListener(theListener);
    }

    /**
     * Registers a listener for the Clear button.
     *
     * @param theListener the listener to notify when the map should be cleared
     */
    public void addClearListener(final ActionListener theListener) {
        myControlPanel.getClear().addActionListener(theListener);
    }
}