package model;
import java.beans.PropertyChangeListener;

/**
 * Implemented by model classes that support property-change notification.
 * Provides a standard contract for registering and removing
 * {@link PropertyChangeListener}s so that views and controllers can
 * react to model state changes without tight coupling.
 *
 * @author Sofiia Kabaldina
 * @version Spring 2026
 */
public interface PropertyChangeEnabled {

    /**
     * Registers a listener to be notified when a bound property changes.
     *
     * @param listener the listener to add
     */
    void addPropertyChangeListener(final PropertyChangeListener listener);

    /**
     * Removes a previously registered property-change listener.
     *
     * @param listener the listener to remove
     */
    void removePropertyChangeListener(final PropertyChangeListener listener);
}