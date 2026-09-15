package model;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;


/**
 * Base class for model objects that need to fire property-change events.
 * Provides a shared {@link PropertyChangeSupport} instance and default
 * implementations of {@link PropertyChangeEnabled#addPropertyChangeListener}
 * and {@link PropertyChangeEnabled#removePropertyChangeListener}, eliminating
 * boilerplate across model classes.
 *
 * @author Abby Hinds, Sofiia Kabaldina
 * @version Spring 2026
 */
public abstract class AbstractModel implements PropertyChangeEnabled {

    /** Manages property-change listener registration and event dispatch. */
    protected final PropertyChangeSupport myChanges = new PropertyChangeSupport(this);

    /**
     * Registers a listener to receive property-change events fired by this model.
     *
     * @param theListener the listener to add
     */
    @Override
    public void addPropertyChangeListener(final PropertyChangeListener theListener) {
        myChanges.addPropertyChangeListener(theListener);
    }

    /**
     * Removes a previously registered property-change listener.
     *
     * @param theListener the listener to remove
     */
    @Override
    public void removePropertyChangeListener(final PropertyChangeListener theListener) {
        myChanges.removePropertyChangeListener(theListener);
    }
}
