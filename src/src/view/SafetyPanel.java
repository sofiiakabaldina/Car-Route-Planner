package view;

import model.Environment;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;

/**
 * Displays route safety information, active simulation conditions,
 * and detected route hazards.
 *
 * @author Chene Van der Walt
 * @version Spring 2026
 */
public class SafetyPanel extends JPanel {

    /** Label displaying the route distance. */
    private JLabel myDistanceLabel;

    /** Label displaying the estimated travel time. */
    private JLabel myTravelTimeLabel;

    /** Label displaying the average route risk. */
    private JLabel myAvgRiskLabel;

    //  Conditions
    /** Label displaying the active weather condition. */
    private JLabel myWeatherLabel;

    /** Label displaying the active traffic percentage. */
    private JLabel myTrafficLabel;

    /** Label displaying the combined weather and traffic risk. */
    private JLabel myCombinedRiskLabel;

    //  Safety Log
    /** Text area displaying route hazards or safety messages. */
    private JTextArea myHazardLog;

    /** Label displaying the overall safety status. */
    private JLabel mySafetyStatusLabel;

    // Colors for the status
    /** Color used when the route is considered safe. */
    private static final Color COLOR_SAFE = new Color(0, 140, 0);

    /** Color used when the route has moderate risk. */
    private static final Color COLOR_MODERATE = new Color(190, 120, 0);

    /** Color used when the route has high risk. */
    private static final Color COLOR_HIGH = Color.RED;

    /**
     * Constructs the safety panel and initializes all display sections.
     */
    public SafetyPanel() {
        setPreferredSize(new Dimension(0, 180));
        setLayout(new GridLayout(1, 3, 6, 0));
        setBorder(new EmptyBorder(4, 6, 4, 6));
        setBackground(new Color(245, 245, 245));

        add(buildSafetyLogSection());
        add(buildConditionsSection());
        add(buildRouteInfoSection());

        // Start with blank/default values
        resetRouteInfo();
        resetConditions();
        resetHazards();
    }

    /**
     * Builds the route information section.
     *
     * @return a panel containing distance, travel time, and average risk labels
     */
    private JPanel buildRouteInfoSection() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new TitledBorder("Route Info"));
        panel.setOpaque(false);

        myDistanceLabel = makeInfoLabel("Distance: \n");
        myTravelTimeLabel = makeInfoLabel("Estimate time: \n");
        myAvgRiskLabel = makeInfoLabel("Average risk:  \n");

        panel.add(Box.createVerticalStrut(4));
        panel.add(myDistanceLabel);

        panel.add(Box.createVerticalStrut(3));
        panel.add(myTravelTimeLabel);


        panel.add(Box.createVerticalStrut(3));
        panel.add(myAvgRiskLabel);

        panel.add(Box.createVerticalGlue());

        return panel;
    }

    /**
     * Builds the active conditions section.
     *
     * @return a panel containing weather, traffic, and combined risk labels
     */
    private JPanel buildConditionsSection() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new TitledBorder("Active Conditions"));
        panel.setOpaque(false);

        myWeatherLabel = makeInfoLabel("Weather: — \n");
        myTrafficLabel = makeInfoLabel("Traffic: — \n");
        myCombinedRiskLabel = makeInfoLabel("Combined risk: — \n");

        panel.add(Box.createVerticalStrut(4));
        panel.add(myWeatherLabel);
        panel.add(Box.createVerticalStrut(3));
        panel.add(myTrafficLabel);
        panel.add(Box.createVerticalStrut(3));
        panel.add(myCombinedRiskLabel);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    /**
     * Builds the safety report section.
     *
     * @return a panel containing the safety status and hazard log
     */
    private JPanel buildSafetyLogSection() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new TitledBorder("Safety Report"));
        panel.setOpaque(false);

        mySafetyStatusLabel = new JLabel("No route calculated.");
        mySafetyStatusLabel.setFont(
                mySafetyStatusLabel.getFont().deriveFont(Font.BOLD, 12f));
        mySafetyStatusLabel.setBorder(new EmptyBorder(2, 4, 2, 4));


        myHazardLog = new JTextArea();
        myHazardLog.setEditable(false);
        myHazardLog.setLineWrap(true);
        myHazardLog.setWrapStyleWord(true);
        myHazardLog.setRows(8);
        myHazardLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        myHazardLog.setBackground(new Color(245, 245, 245));

        JScrollPane scrollPane = new JScrollPane(myHazardLog);
        scrollPane.setBorder(null);

        panel.add(mySafetyStatusLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Updates the route distance, estimated travel time, and average risk display.
     *
     * @param theDistanceMiles the total route distance in miles
     * @param theTravelTimeHours the estimated travel time in hours
     * @param theAvgRisk the average risk value for the route
     */
    public void updateRouteInfo(final double theDistanceMiles,
                                final double theTravelTimeHours,
                                final double theAvgRisk) {
        myDistanceLabel.setText(String.format("Distance:      %.2f mi", theDistanceMiles));
        myTravelTimeLabel.setText("Estimate time:     " + formatTravelTime(theTravelTimeHours));
        myAvgRiskLabel.setText("Average risk:      " + String.format("%.2f", theAvgRisk));
        myAvgRiskLabel.setForeground(riskColor(theAvgRisk));
    }

    /**
     * Converts a travel time in hours to a formatted string.
     *
     * @param theTravelTimeHours the travel time in hours
     * @return a formatted string like "1 hr 30 min" or "45 min"
     */
    public static String formatTravelTime(final double theTravelTimeHours) {
        int totalMinutes = (int) Math.round(theTravelTimeHours * 60);
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        return (hours > 0)
                ? String.format("%d hr %d min", hours, minutes)
                : String.format("%d min", minutes);
    }


    /**
     * Updates the displayed weather, traffic, and combined risk conditions.
     *
     * @param theWeather the active weather condition
     * @param theTrafficPercent the active traffic density as a percentage
     */
    public void updateConditions(final Environment theWeather, final int theTrafficPercent) {
        double weatherRisk = theWeather.getRiskFactor();
        double trafficRisk = theTrafficPercent / 100.0;
        double combined = weatherRisk + trafficRisk;

        myWeatherLabel.setText(String.format("Weather:       %s (risk %.1f)",
                theWeather.name(), weatherRisk));

        myTrafficLabel.setText(String.format("Traffic:       %d%%", theTrafficPercent));

        myCombinedRiskLabel.setText(String.format("Combined risk: %.2f", combined));
        myCombinedRiskLabel.setForeground(riskColor(combined));
    }


    /**
     * Updates the safety report based on detected hazards.
     *
     * @param theHazards the list of detected hazard messages
     */
    public void updateSafetyReport(final List<String> theHazards) {
        if (theHazards == null || theHazards.isEmpty()) {
            mySafetyStatusLabel.setText("Route is safe.");
            mySafetyStatusLabel.setForeground(COLOR_SAFE);
            myHazardLog.setText("No hazards detected.");
        } else {
            mySafetyStatusLabel.setText("Hazards detected (" + theHazards.size() + ")");
            mySafetyStatusLabel.setForeground(COLOR_HIGH);

            StringBuilder hazardText = new StringBuilder();
            for (String hazard : theHazards) {
                hazardText.append("• ").append(hazard).append("\n");
            }
            myHazardLog.setText(hazardText.toString().trim());
        }
        myHazardLog.setCaretPosition(0);
    }


    /**
     * Displays a custom message in the safety log.
     *
     * @param theMessage the message to display
     */
    public void updateLog(final String theMessage) {
        mySafetyStatusLabel.setForeground(UIManager.getColor("Label.foreground"));

        myHazardLog.setText(theMessage);
        myHazardLog.setCaretPosition(0);
    }

    // ── Reset helpers ─────────────────────────────────────────────────────────

    /**
     * Resets the route information labels to their default values.
     */
    public void resetRouteInfo() {
        myDistanceLabel.setText("Distance: —");
        myTravelTimeLabel.setText("Estimate time: —");
        myAvgRiskLabel.setText("Average risk: —");
        myAvgRiskLabel.setForeground(UIManager.getColor("Label.foreground"));
    }

    /**
     * Resets the active condition labels to their default values.
     */
    public void resetConditions() {
        myWeatherLabel.setText("Weather:       CLEAR (risk 0.0)");
        myTrafficLabel.setText("Traffic:       0%");
        myCombinedRiskLabel.setText("Combined risk: 0.00");
        myCombinedRiskLabel.setForeground(COLOR_SAFE);
    }

    /**
     * Clears the hazard log and resets the safety status.
     */
    public void resetHazards() {
        mySafetyStatusLabel.setText("No route calculated.");
        mySafetyStatusLabel.setForeground(UIManager.getColor("Label.foreground"));
        myHazardLog.setText("");
    }

    /**
     * Creates a formatted information label.
     *
     * @param theText the text to display in the label
     * @return the formatted JLabel
     */
    private static JLabel makeInfoLabel(final String theText) {
        JLabel label = new JLabel(theText);
        label.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    /**
     * Returns the display color associated with a risk value.
     *
     * @param theRisk the risk value to evaluate
     * @return the color representing the risk level
     */
    private static Color riskColor(final double theRisk) {
        if (theRisk < 0.3) {
            return COLOR_SAFE;
        } else if (theRisk < 0.6) {
            return COLOR_MODERATE;
        } else {
            return COLOR_HIGH;
        }
    }
}