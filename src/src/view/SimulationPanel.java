package view;

import model.Environment;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * A dialog for setting weather, traffic, and obstacle conditions for the simulation.
 * Provides a live risk preview and buttons to apply or reset the selected conditions.
 *
 * @author Sofiia Kabaldina
 * @version Spring 2026
 */
public class SimulationPanel extends JDialog {

    /** Dropdown for selecting the current weather condition */
    private final JComboBox<Environment> myWeatherComboBox;

    /**
     * Slider controlling traffic density from 0 (no traffic) to 100 (gridlock).
     * Divided by 100.0 when passed to EnvironmentSimulator to produce a 0.0–1.0 value.
     */
    private final JSlider myTrafficSlider;
    /** Label displaying the current traffic density percentage as the slider is dragged. */
    private final JLabel myTrafficLabel;
    /** Slider controlling what percentage of roads should be randomly blocked as obstacles. */
    private JSlider myObstacleSlider;
    /** Label displaying the current obstacle density percentage as the slider is dragged. */
    private JLabel myObstacleLabel;

    // Buttons
    /** Button that triggers application of the selected conditions to the map graph. */
    private final JButton myApplyButton;
    /** Button that resets all controls to their default values and clears applied conditions. */
    private final JButton myResetButton;
    /** Button that hides the dialog without resetting or applying conditions. */
    private final JButton myCloseButton;

    /**
     * Constructs a SimulationPanel and builds the weather, traffic,
     * obstacle, and risk preview sections.
     *
     * @param theParent - the parent JFrame that owns this dialog
     */
    public SimulationPanel(final JFrame theParent) {
        // true = modal, blocks the map until user closes this dialog
        super(theParent, "Simulate Conditions", true);

        setLayout(new BorderLayout(10, 10));
        setResizable(false);

        // ── Weather Section ──────────────────────────────────────────────────
        JPanel weatherPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        weatherPanel.setBorder(new TitledBorder("Weather Condition"));


        JLabel weatherLabel = new JLabel("Select weather:");
        myWeatherComboBox = new JComboBox<>(Environment.values());
        myWeatherComboBox.setSelectedItem(Environment.CLEAR);

        // Show risk factor next to each option using a custom renderer
        myWeatherComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (value instanceof Environment) {
                    Environment env = (Environment) value;
                    setText(env.name() + "  (risk: " + env.getRiskFactor() + ")");
                }
                return this;
            }
        });

        weatherPanel.add(weatherLabel);
        weatherPanel.add(myWeatherComboBox);

        // ── Traffic Section ───────────────────────────────────────────────────
        JPanel trafficPanel = new JPanel(new BorderLayout(10, 10));
        trafficPanel.setBorder(new TitledBorder("Traffic Density"));

        // Labels under the slider
        JPanel sliderLabels = new JPanel(new BorderLayout());
        sliderLabels.add(new JLabel("No Traffic"), BorderLayout.WEST);
        sliderLabels.add(new JLabel("Gridlock", SwingConstants.RIGHT), BorderLayout.EAST);

        myTrafficSlider = new JSlider(0, 100, 0);
        myTrafficSlider.setMajorTickSpacing(25);
        myTrafficSlider.setMinorTickSpacing(5);
        myTrafficSlider.setPaintTicks(true);

        // Live label that updates as you drag the slider
        myTrafficLabel = new JLabel("Current traffic density: 0%",
                SwingConstants.CENTER);

        myTrafficSlider.addChangeListener(theEvent -> {
            int value = myTrafficSlider.getValue();
            myTrafficLabel.setText("Current traffic density: " + value + "%");
        });

        trafficPanel.add(myTrafficLabel, BorderLayout.NORTH);
        trafficPanel.add(myTrafficSlider, BorderLayout.CENTER);
        trafficPanel.add(sliderLabels, BorderLayout.SOUTH);

        // ── Risk Preview Section ──────────────────────────────────────────────
        // Shows the user what the combined risk will look like before applying
        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBorder(new TitledBorder("Risk Preview"));
        JLabel previewLabel = new JLabel("Select conditions above to preview risk.",
                SwingConstants.CENTER);
        previewLabel.setFont(previewLabel.getFont().deriveFont(Font.ITALIC));
        previewPanel.add(previewLabel, BorderLayout.CENTER);

        // Update preview when weather or traffic changes
        ActionListener updatePreview = e -> {
            Environment selectedWeather =
                    (Environment) myWeatherComboBox.getSelectedItem();
            double trafficDensity = myTrafficSlider.getValue() / 100.0;

            if (selectedWeather != null) {
                double combinedRisk = selectedWeather.getRiskFactor()
                        + trafficDensity;
                String riskLevel;
                Color riskColor;

                if (combinedRisk < 0.3) {
                    riskLevel = "LOW";
                    riskColor = new Color(0, 150, 0); // green
                } else if (combinedRisk < 0.8) {
                    riskLevel = "MODERATE";
                    riskColor = new Color(200, 130, 0); // orange
                } else {
                    riskLevel = "HIGH";
                    riskColor = Color.RED;
                }

                previewLabel.setText(
                        "Combined risk factor: " +
                                String.format("%.2f", combinedRisk) +
                                "  →  " + riskLevel
                );
                previewLabel.setForeground(riskColor);
                previewLabel.setFont(
                        previewLabel.getFont().deriveFont(Font.BOLD, 13f));
            }
        };

        myWeatherComboBox.addActionListener(updatePreview);
        myTrafficSlider.addChangeListener(theEvent -> updatePreview.actionPerformed(null));

        // ── Buttons ───────────────────────────────────────────────────────────
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        myApplyButton = new JButton("Apply Conditions");
        myApplyButton.setBackground(new Color(30, 80, 140));
        myApplyButton.setForeground(Color.WHITE);
        myApplyButton.setFocusPainted(false);

        myResetButton = new JButton("Reset All");
        myResetButton.setBackground(new Color(180, 60, 60));
        myResetButton.setForeground(Color.WHITE);
        myResetButton.setFocusPainted(false);

        myCloseButton = new JButton("Close");

        buttonPanel.add(myApplyButton);
        buttonPanel.add(myResetButton);
        buttonPanel.add(myCloseButton);

        // ── Layout ────────────────────────────────────────────────────────────
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        centerPanel.add(weatherPanel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(trafficPanel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(buildObstacleSection());
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(previewPanel);

        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        // Center over parent window
        setLocationRelativeTo(theParent);

        // Close button just hides the dialog, doesn't destroy it
        myCloseButton.addActionListener(theEvent -> setVisible(false));
    }

    /**
     * Builds and returns the obstacle density section containing a slider and label.
     * The slider ranges from 0 to 100, representing the percentage of roads to block.
     *
     * @return a JPanel containing the obstacle slider and its labels
     */
    private JPanel buildObstacleSection() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new TitledBorder("Road Obstacles"));

        JPanel sliderLabels = new JPanel(new BorderLayout());
        sliderLabels.add(new JLabel("None"), BorderLayout.WEST);
        sliderLabels.add(new JLabel("Many", SwingConstants.RIGHT), BorderLayout.EAST);

        myObstacleSlider = new JSlider(0, 100, 0);
        myObstacleSlider.setMajorTickSpacing(25);
        myObstacleSlider.setMinorTickSpacing(5);
        myObstacleSlider.setPaintTicks(true);

        myObstacleLabel = new JLabel("Roads blocked: 0%", SwingConstants.CENTER);

        myObstacleSlider.addChangeListener(theEvent -> {
            myObstacleLabel.setText("Roads blocked: " + myObstacleSlider.getValue() + "%");
        });

        panel.add(myObstacleLabel, BorderLayout.NORTH);
        panel.add(myObstacleSlider, BorderLayout.CENTER);
        panel.add(sliderLabels, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Returns the proportion of roads that should be randomly blocked as obstacles.
     * Converts the obstacle slider value (0–10000) to the range [0.0, 1.0].
     *
     * @return the obstacle density as a double
     */
    public double getObstacleDensity() {
        return myObstacleSlider.getValue() / 10000.0;
    }

    /**
     * Returns the weather condition currently selected in the dropdown.
     *
     * @return the selected Environment value
     */
    public Environment getSelectedWeather() {
        return (Environment) myWeatherComboBox.getSelectedItem();
    }

    /**
     * Returns the traffic density as a value between 0.0 and 1.0.
     * Converts the traffic slider value (0–100) to a decimal fraction.
     *
     * @return the traffic density as a double between 0.0 and 1.0
     */
    public double getTrafficDensity() {
        return myTrafficSlider.getValue() / 100.0;
    }

    /**
     * Resets all panel controls to their default values visually.
     */
    public void resetControls() {
        myWeatherComboBox.setSelectedItem(Environment.CLEAR);
        myTrafficSlider.setValue(0);
        myObstacleSlider.setValue(0);
    }

    /**
     * Registers an ActionListener to be called when the Apply Conditions button is clicked.
     * The controller uses this to read selected values and push them to EnvironmentSimulator.
     *
     * @param theListener - the ActionListener to notify on apply
     */
    public void addApplyListener(final ActionListener theListener) {
        myApplyButton.addActionListener(theListener);
    }

    /**
     * Registers an ActionListener to be called when the Reset All button is clicked.
     * The controller uses this to clear all applied conditions from the model.
     *
     * @param theListener - the ActionListener to notify on reset
     */
    public void addResetListener(final ActionListener theListener) {
        myResetButton.addActionListener(theListener);
    }
}