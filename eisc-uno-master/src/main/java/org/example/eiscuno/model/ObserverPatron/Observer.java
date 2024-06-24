package org.example.eiscuno.model.ObserverPatron;
/**
 * The Observer interface provides the update method that must be implemented by any class that
 * wants to observe changes in a Subject.
 */
public interface Observer {
    /**
     * This method is called to notify the observer of changes in the subject's state.
     *
     * @param isThreadRunning A boolean indicating the current state of the observed thread.
     */
    void update(boolean isThreadRunning);
}
