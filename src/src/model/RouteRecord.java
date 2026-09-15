package model;

import java.util.List;

/**
 * Represents a saved route record for display in the route history table.
 * Stores the route's unique identifier, start and end location labels,
 * total distance, estimated duration, and any hazards detected along the route.
 * Each field has a getter and setter to support table model updates.
 *
 * @author Abby Hinds
 * @version Spring 2026
 */
public class RouteRecord {

    /** Unique identifier for this route record */
    private int myID;
    /** Display label of the route's start intersection. */
    private String myStartLabel;
    /** Display label of the route's end intersection. */
    private String myEndLabel;
    /** Total route distance in miles. */
    private double myDistance;
    /** Estimated travel duration in hours. */
    private double myDuration;
    /** List of hazard description strings detected along this route. */
    private List<String> myHazards;

    /**
     * Constructs a RouteRecord with all fields set at creation time.
     *
     * @param theId - the unique record identifier
     * @param theStartLabel - the display label for the start intersection
     * @param theEndLabel - the display label for the end intersection
     * @param theDistance - the total distance in miles
     * @param theDuration - the estimated travel duration in hours
     * @param theHazards - the list of hazard messages for this route
     */
    public RouteRecord(final int theId, final String theStartLabel,
                       final String theEndLabel, final double theDistance,
                       final double theDuration, final List<String> theHazards) {
        this.myID = theId;
        this.myStartLabel = theStartLabel;
        this.myEndLabel = theEndLabel;
        this.myDistance = theDistance;
        this.myDuration = theDuration;
        this.myHazards = theHazards;
    }

    /**
     * Returns the unique identifier of this route record.
     *
     * @return the record ID
     */
    public int getMyID() {
        return myID;
    }

    /**
     * Sets the unique identifier of this route record.
     *
     * @param theID - the new record ID
     */
    public void setMyID(final int theID) {
        this.myID = theID;
    }

    /**
     * Returns the display label of the start intersection.
     *
     * @return the start label string
     */
    public String getMyStartLabel() {
        return myStartLabel;
    }

    /**
     * Sets the display label of the start intersection.
     *
     * @param theStartLabel - the new start label
     */
    public void setMyStartLabel(final String theStartLabel) {
        this.myStartLabel = theStartLabel;
    }

    /**
     * Returns the display label of the end intersection.
     *
     * @return the end label string
     */
    public String getMyEndLabel() {
        return myEndLabel;
    }

    /**
     * Sets the display label of the end intersection.
     *
     * @param theEndLabel - the new end label
     */
    public void setMyEndLabel(final String theEndLabel) {
        this.myEndLabel = theEndLabel;
    }

    /**
     * Returns the total route distance in miles.
     *
     * @return the distance in miles
     */
    public double getMyDistance() {
        return myDistance;
    }

    /**
     * Sets the total route distance in miles.
     *
     * @param theDistance - the new distance in miles
     */
    public void setMyDistance(final double theDistance) {
        this.myDistance = theDistance;
    }

    /**
     * Returns the estimated travel duration in hours.
     *
     * @return the duration in hours
     */
    public double getMyDuration() {
        return myDuration;
    }

    /**
     * Sets the estimated travel duration in hours.
     *
     * @param theDuration - the new duration in hours
     */
    public void setMyDuration(final double theDuration) {
        this.myDuration = theDuration;
    }

    /**
     * Returns the list of hazard messages detected along this route.
     *
     * @return the hazard list
     */
    public List<String> getMyHazards() {
        return myHazards;
    }

    /**
     * Sets the list of hazard messages for this route.
     *
     * @param theHazards - the new hazard list
     */
    public void setMyHazards(final List<String> theHazards) {
        this.myHazards = theHazards;
    }

}
