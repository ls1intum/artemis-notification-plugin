package de.tum.in.ase.notification.configuration;

public abstract class ContextFactory {

    /**
     * Builds a context for the current environment.
     *
     * @return The context object.
     */
    public abstract Context buildContext();
}
