package org.axiom.service;

import org.apache.camel.spi.Registry;
import static org.apache.commons.collections.CollectionUtils.typedCollection;
import org.apache.commons.configuration.Configuration;
import static org.apache.commons.io.FileUtils.*;
import org.axiom.integration.Environment;
import static org.axiom.util.CollectionUtils.*;
import org.axiom.util.TypeMappedTransformer;
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
     * Launch the supplied {@link ControlChannel}.
     * @param channel
     */
    public void launch(final ControlChannel channel) {
        bootstrapper.bootstrap(channel);
        channel.activate();

        final Registry registry = channel.getContext().getRegistry();
        Configuration config =
            registry.lookup(Environment.CONFIG_BEAN_ID, Configuration.class);
        final String scriptPath = config.getString(Environment.SCRIPT_REPOSITORY_URI);
        log.info("Restoring existing routes from '{}'.", scriptPath);
        map(typedCollection(locateRouteScripts(scriptPath), File.class), 
            new TypeMappedTransformer<File, String>() {
                @Override public String apply(final File input) {
                    final String script = input.getAbsolutePath();
                    log.debug("Restoring routes from '{}'.", script);
                    channel.configure(new RouteScriptLoader(script, 
                        channel.getRouteScriptEvaluator()));
                    return script;
                }
            });
    }

    private Collection locateRouteScripts(final String scriptPath) {
        Collection routeScripts =
            listFiles(new File(scriptPath), new String[] { "rb"  }, false);
        return routeScripts;
    }

    /**
     * Gets the {@link ControlChannelBootstrapper} used by this.
     * @return The bootstrapper instance in used by this class
     */
    public ControlChannelBootstrapper getBootstrapper() {
        if (bootstrapper == null) {
            return bootstrapper = new ControlChannelBootstrapper();
        }
        return bootstrapper;
    }
}
