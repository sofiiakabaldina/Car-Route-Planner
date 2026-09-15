package view;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * Creates and manages the main control buttons used by the route planner.
 * Provides buttons for route generation, simulation settings, and clearing
 * the current map state.
 *
 * @author Chene Van der Walt
 * @version Spring 2026
 */
public class ControlPanel {

    /** Button used to generate directions between selected locations. */
    private final JButton myGetDirections;

    /** Button used to open the simulation conditions dialog. */
    private final JButton mySimulateConditionsButton;

    /** Button used to clear the current route and map state. */
    private final JButton myClear;

    /**
     * Constructs the control panel and initializes all control buttons,
     * icons, and default button settings.
     */
    public ControlPanel() {

        ImageIcon directionsIcon = new ImageIcon(
                Objects.requireNonNull(getClass().getClassLoader().getResource("directions-icon-size_128.png")));

        Image scaledDirectiionIcon = directionsIcon.getImage()
                .getScaledInstance(20, 20, Image.SCALE_SMOOTH);

        myGetDirections = new JButton(
                "Directions",
                new ImageIcon(scaledDirectiionIcon));
        myGetDirections.setHorizontalAlignment(SwingConstants.LEFT);
        myGetDirections.setHorizontalTextPosition(SwingConstants.RIGHT);
        myGetDirections.setIconTextGap(6);
        myGetDirections.setEnabled(false);

        mySimulateConditionsButton = new JButton("Simulate Conditions");

        ImageIcon clearIcon = new ImageIcon(
                Objects.requireNonNull(getClass().getClassLoader().getResource("clear.png")));

        Image scaledClearIcon = clearIcon.getImage()
                .getScaledInstance(20, 20, Image.SCALE_SMOOTH);

        myClear = new JButton(
                "Clear",
                new ImageIcon(scaledClearIcon));
        myClear.setHorizontalAlignment(SwingConstants.RIGHT);
        myClear.setIconTextGap(0);

    }

    /**
     * Returns the Simulate Conditions button.
     *
     * @return the simulation conditions button
     */
    public JButton getSimulateConditions() {
        return mySimulateConditionsButton;
    }

    /**
     * Returns the Get Directions button.
     *
     * @return the directions button
     */
    public JButton getGetDirections() {
        return myGetDirections;
    }

    /**
     * Returns the Clear button.
     *
     * @return the clear button
     */
    public JButton getClear() {
        return myClear;
    }


}
