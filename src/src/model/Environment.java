package model;

/**
 * Represents environmental driving conditions and their associated risk factors.
 * Each condition has a risk factor between 0.0 (no risk) and 1.0 (maximum risk)
 * that affects effective road speed and overall route safety scoring.
 *
 * @author Abby Hinds, Sofiia Kabaldina
 * @version Spring 2026
 */
public enum Environment {

    /** Clear conditions — no weather impact on driving. Risk factor: 0.0 */
    CLEAR(0.0),
    /** Light rain — minor reduction in visibility and traction. Risk factor: 0.2 */
    LIGHT_RAIN(0.2),
    /** Light fog — minor visibility reduction. Risk factor: 0.3 */
    LIGHT_FOG(0.3),
    /** Light snow — minor traction and visibility reduction. Risk factor: 0.4 */
    LIGHT_SNOW(0.4),
    /** Heavy rain — significant reduction in visibility and traction. Risk factor: 0.6 */
    HEAVY_RAIN(0.6),
    /** Dense fog — severe visibility reduction. Risk factor: 0.7 */
    DENSE_FOG(0.7),
    /** Heavy snow — severe traction and visibility reduction. Risk factor: 0.8 */
    HEAVY_SNOW(0.8),
    /** Icy roads — maximum risk, near-impassable conditions. Risk factor: 0.9 */
    ICY(0.9);

    /** The risk factor associated with this condition, in the range [0.0, 1.0]. */
    private final double myRiskFactor;

    /**
     * Constructs an Environment constant with the given risk factor.
     *
     * @param myRiskFactor - the risk factor for this condition, between 0.0 and 1.0
     */
    Environment(final double myRiskFactor) {
        this.myRiskFactor = myRiskFactor;
    }

    /**
     * Returns the risk factor for this environment condition.
     * Used to reduce effective road speed and calculate route safety scores.
     *
     * @return the risk factor as a double in the range [0.0, 1.0]
     */
    public double getRiskFactor() {
        return myRiskFactor;
    }
}
