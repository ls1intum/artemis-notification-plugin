package de.tum.in.ase.notification;

import de.tum.in.ase.notification.configuration.Context;
import de.tum.in.ase.notification.configuration.EnvReader;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.IOException;

public class CLIPlugin extends NotificationPlugin {
    public CLIPlugin() {
        super(new EnvReader());
        Configurator.setRootLevel(Level.ALL);
    }



    public static void main(String[] args) throws IOException {
        CLIPlugin plugin = new CLIPlugin();
        new CLIPlugin().run(plugin.provideContext());
    }

    @Override
    public Context provideContext() {
        return contextFactory.buildContext();
    }
}
