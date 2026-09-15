package model;

/**
 * Represents a geographic intersection point in the road map graph.
 * Each intersection has a unique ID, latitude/longitude coordinates, a display
 * label, and a blocked status used by the routing algorithm to avoid impassable nodes.
 * Equality and hashing are based solely on the intersection ID so that the same
 * real-world node is always treated as the same graph vertex regardless of other fields.
 *
 * @author Sofia Kabaldina
 * @version Spring 2026
 */
public class Intersection {

    /** Unique OSM node identifier for this intersection. */
    private final String myId;
    /** Latitude coordinate of this intersection. */
    private final double myLatitude;
    /** Longitude coordinate of this intersection. */
    private final double myLongitude;
    /** Human-readable display label, or the node ID if no name tag is present. */
    private final String myLabel;
    /** Whether this intersection is currently blocked and should be avoided by routing. */
    private boolean myIsBlocked;

    /**
     * Constructs an Intersection with the given ID, coordinates, and label.
     * The intersection is unblocked by default.
     *
     * @param theId - the unique OSM node ID
     * @param theLatitude - the latitude of this intersection
     * @param theLongitude - the longitude of this intersection
     * @param theLabel - the display name, or the node ID if no name is available
     */
    public Intersection(final String theId, final double theLatitude,
                        final double theLongitude, final String theLabel) {
        myId = theId;
        myLatitude = theLatitude;
        myLongitude = theLongitude;
        myLabel = theLabel;
        myIsBlocked = false; // not blocked by default
    }

    /**
     * Returns the latitude of this intersection.
     *
     * @return the latitude as a double
     */
    public double getX() {
        return myLatitude;
    }

    /**
     * Returns the longitude of this intersection.
     *
     * @return the longitude as a double
     */
    public double getY() {
        return myLongitude;
    }

    /**
     * Returns the unique OSM node ID of this intersection.
     *
     * @return the node ID
     */
    public String getId()    {
        return myId;
    }

    /**
     * Returns the human-readable display label of this intersection.
     *
     * @return the label
     */
    public String getLabel() {
        return myLabel;
    }

    /**
     * Returns whether this intersection is currently blocked.
     *
     * @return true if blocked, false otherwise
     */
    public boolean isBlocked() {
        return myIsBlocked;
    }

    /**
     * Sets the blocked status of this intersection.
     *
     * @param theBlocked - true to block this intersection, false to unblock it
     */
    public void setBlocked(final boolean theBlocked) {
        myIsBlocked = theBlocked;
    }

    /**
     * Returns the geographic position of this intersection as a [latitude, longitude] array.
     *
     * @return a double array where index 0 is latitude and index 1 is longitude
     */
    public double[] getPosition() {
        return new double[] {myLatitude, myLongitude};
    }

    /**
     * Returns a string representation of this intersection in the format "Label (ID)".
     *
     * @return the formatted string
     */
    @Override
    public String toString() {
        return myLabel + " (" + myId + ")";
    }

    /**
     * Checks equality based on intersection ID only.
     * Two intersections with the same ID are considered the same graph node
     * regardless of label or coordinate differences.
     *
     * @param theOther - the object to compare to
     * @return true if theOther is an Intersection with the same ID
     */
    @Override
    public boolean equals(final Object theOther) {
        if (this == theOther) {
            return true;
        }
        if (!(theOther instanceof Intersection)) {
            return false;
        }
        Intersection other = (Intersection) theOther;
        return myId.equals(other.myId);
    }

    /**
     * Returns a hash code based on the intersection ID.
     * Consistent with equals so that Intersection can be used as a HashMap key.
     *
     * @return the hash code of the ID string
     */
    @Override
    public int hashCode() {
        return myId.hashCode();
    }
}