package model;

/**
 * Enumerates the possible sharpness levels of a turn between two road segments,
 * each associated with a risk factor used by the safety checker.
 * Sharper turns contribute more to the overall route risk score evaluated
 * by SafetyChecker when checking for hazardous turn sequences.
 *
 * @author Abby Hinds
 * @version Spring 2026
 */
public enum TurnSharpness {

    /** No change in direction, zero risk. */
    STRAIGHT(0.0),
    /** A mild curve, minimal risk. */
    GENTLE(1.0),
    /** A significant turn requiring reduced speed, moderate risk. */
    SHARP(1.5),
    /** An extreme near-180-degree turn, the highest risk. */
    HAIRPIN(2.0);

    /** The risk factor associated with this turn sharpness level. */
    private final double myRiskFactor;

    /**
     * Constructs a TurnSharpness constant with the given risk factor.
     *
     * @param myRiskFactor - the risk value assigned to this turn sharpness level
     */
    TurnSharpness(final double myRiskFactor) {
        this.myRiskFactor = myRiskFactor;
    }

    /**
     * Returns the risk factor for this turn sharpness level.
     *
     * @return the risk factor as a double
     */
    public double getRiskFactor() {
        return myRiskFactor;
    }

}
