package org.example.eiscuno.model.ObserverPatron;
/**
 * The Subject interface provides methods for managing observers and notifying them of state changes.
 */
public interface Subject {
    /**
     * Adds an observer to the list of observers.
     *
     * @param observer The observer to be added.
     */
    void addObserver(Observer observer);
    /**
     * Removes an observer from the list of observers.
     *
     * @param observer The observer to be removed.
     */
    void removeObserver(Observer observer);
    /**
     * Notifies all registered observers of a state change.
     */
    void notifyObservers();
}
