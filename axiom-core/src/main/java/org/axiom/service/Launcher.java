package org.axiom.service;

import org.apache.camel.spi.Registry;
import static org.apache.commons.collections.CollectionUtils.typedCollection;
import org.apache.commons.configuration.Configuration;
import static org.apache.commons.io.FileUtils.*;
import org.axiom.integration.Environment;
import static org.axiom.util.CollectionUtils.*;
import org.axiom.util.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;

/**
 * Provides a simple API for launching a control channel
 * with its environment and dependencies configured correctly.
 */
public class Launcher {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private ControlChannelBootstrapper bootstrapper;

    public Launcher(final ControlChannelBootstrapper bootstrapper) {
        this.bootstrapper = bootstrapper;
    }

    /**
     * Launches the supplied {@link ControlChannel}. The channel is
     * first bootstrapped with its own routing configuration. Once
     * configured, the channel is activated. Finally, any existing
     * route configurations for the target environment(s) which are
     * present in the route scripts directory (whose location is
     * indicated by the {@code axiom.scripts.repository.uri} system
     * property) are re-activated in an arbitrary order.
     * @param channel The channel to launch.
     */
    public void launch(final ControlChannel channel) {
        bootstrapper.bootstrap(channel);
        channel.activate();
        reconfigureExistingRoutes(channel);
    }

    /**
     * Gets the {@link ControlChannelBootstrapper} used by this.
     * @return The bootstrapper instance in used by this class.
     */
    public ControlChannelBootstrapper getBootstrapper() {
        if (bootstrapper == null) {
            return bootstrapper = new ControlChannelBootstrapper();
        }
        return bootstrapper;
    }

    /**
     * Reconfigure any routes that already exist in the relevant
     * location on the file system.
     * @param channel The channel to reconfigure.
     */
    @SuppressWarnings({"unchecked"})
    private void reconfigureExistingRoutes(final ControlChannel channel) {
        final Registry registry = channel.getContext().getRegistry();
        Configuration config =
            registry.lookup(Environment.CONFIG_BEAN_ID, Configuration.class);
        final String scriptPath = config.getString(Environment.SCRIPT_REPOSITORY_URI);
        log.info("Restoring existing routes from '{}'.", scriptPath);
        //NB: This unchecked operation is actually quite safe in practise
        map(typedCollection(locateRouteScripts(scriptPath), File.class),
            new Operation<File>() {
                @Override public void apply(final File input) {
                    final String script = input.getAbsolutePath();
                    log.debug("Restoring routes from '{}'.", script);
                    channel.configure(new RouteScriptLoader(script,
                        channel.getRouteScriptEvaluator()));
                }
            });
    }

    private Collection locateRouteScripts(final String scriptPath) {
        //TODO: move the array of script extension suffixes to axiom.properties
        return listFiles(new File(scriptPath), new String[] { "rb" }, false);
    }
}
