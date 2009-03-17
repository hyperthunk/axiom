package org.axiom.service;

import org.apache.camel.CamelContext;
import org.apache.camel.processor.interceptor.Tracer;
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
import java.util.Collections;

/**
 * Provides a simple API for launching a control channel
 * with its environment and dependencies configured correctly.
 */
public class Launcher {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private ControlChannelBootstrapper bootstrapper;

    public Launcher() { this(new ControlChannelBootstrapper()); }

    public Launcher(final ControlChannelBootstrapper bootstrapper) {
        this.bootstrapper = bootstrapper;
    }

    /**
     * See {@link Launcher#launch(ControlChannel)}.
     * @param hostContext The host {@link CamelContext}.
     * @return
     */
    public ControlChannel launch(final CamelContext hostContext) {
        return launch(new ControlChannel(hostContext));
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
     * @return
     */
    public ControlChannel launch(final ControlChannel channel) {
        bootstrapper.bootstrap(channel);
        channel.activate();
        reconfigureExistingRoutes(channel);
        return channel;
    }

    /**
     * Reconfigure any routes that already exist in the relevant
     * location on the file system.
     * @param channel The channel to reconfigure.
     */
    @SuppressWarnings({"unchecked"})
    private void reconfigureExistingRoutes(final ControlChannel channel) {
        Configuration config = channel.getConfig();
        //NB: This unchecked operation is actually quite safe in practise
        map(typedCollection(locateRouteScripts(config), File.class),
            new Operation<File>() {
                @Override public void apply(final File input) {
                    final String script = input.getAbsolutePath();
                    log.debug("Restoring routes from '{}'.", script);
                    channel.configure(new RouteScriptLoader(script,
                        channel.getRouteScriptEvaluator()));
                }
            });
    }

    private Collection locateRouteScripts(final Configuration config) {
        final String scriptPath = config.getString(Environment.SCRIPT_REPOSITORY_URI);
        log.info("Restoring existing routes from '{}'.", scriptPath);
        final String[] extensions =
            config.getStringArray(Environment.SCRIPT_FILE_EXTENSIONS);
        final File directory = new File(scriptPath);
        if (directory.isDirectory()) {
            return listFiles(directory, extensions, false);
        } else {
            return Collections.EMPTY_LIST;
        }
    }
}
