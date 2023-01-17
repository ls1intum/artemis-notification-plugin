package de.tum.cit.ase.artemis_notification_plugin.configuration;

public abstract class ContextFactory {

    /**
     * Builds a context for the current environment.
     *
     * @return The context object.
     */
    public abstract Context buildContext();
}
