package de.tum.cit.ase.artemis_notification_plugin;

import de.tum.cit.ase.artemis_notification_plugin.configuration.Context;
import de.tum.cit.ase.artemis_notification_plugin.configuration.EnvReader;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.IOException;

public class CLIPlugin extends NotificationPlugin {

    public CLIPlugin() {
        super(new EnvReader());
        Configurator.setRootLevel(Level.INFO);
    }

    public static void main(String[] args) throws IOException {
        CLIPlugin plugin = new CLIPlugin();
        plugin.run(plugin.provideContext());
    }

    @Override
    public Context provideContext() {
        return contextFactory.buildContext();
    }
}
