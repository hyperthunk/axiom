/*
 * Copyright (c) 2009, Tim Watson
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of the author nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.axiom.service;

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.interceptor.Tracer;
import org.apache.commons.configuration.Configuration;
import static org.apache.commons.lang.Validate.*;
import static org.axiom.configuration.ExternalConfigurationSourceFactory.*;
import org.axiom.integration.Environment;
import org.axiom.integration.camel.RouteConfigurationScriptEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.rmi.registry.Registry;

/**
 * Provides a managed message channel that can be used to
 * control {@link CamelContext}s. Once activated, the control channel's
 * behavior can be controlled by adding routing instructions via the
 * {@code load} method. Managed {@link CamelContext}s (i.e. those stored
 * as components in the host {@link CamelContext}'s {@link Registry}) can
 * be configured via the {@link ControlChannel#configure} methods.
 * <p>
 * This component is <b>not thread safe</b>. Specifically, there is no
 * guarantee that calls to {@link ControlChannel#activate()} and/or
 * {@link ControlChannel#destroy()} will not stomp on each other,
 * nor are additional control channel and/or managed context route configuration
 * updates synchronized. This component should therefore be considered
 * to be <b>thread-hostile</b> and explicit synchronization used if/when required.
 * </p>
 */
public class ControlChannel {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final CamelContext hostContext;
    private final Tracer tracer;

    private Configuration config;

    public ControlChannel(final CamelContext hostContext) {
        this(hostContext, getTracer(hostContext));
    }

    public ControlChannel(final CamelContext hostContext, final Tracer tracer) {
        notNull(hostContext, "Camel context cannot be null.");
        notNull(tracer, "Tracer cannot be null.");
        this.tracer = tracer;
        this.hostContext = hostContext;
    }

    private static Tracer getTracer(final CamelContext context) {
        Tracer tracer = Tracer.getTracer(context);
        if (tracer == null) {
            return new Tracer();
        } else {
            return tracer;
        }
    }

    /**
     * Adds a set of routing/configuration rules to the host {@link CamelContext}
     * using the supplied {@link RouteLoader}. 
     * @param loader The loader for the routes you wish to load.
     */
    public void load(final RouteLoader loader) {
        notNull(loader, "Route loader cannot be null.");
        try {
            log.debug("Adding routes to context {}.", hostContext.getName());
            //TODO: consider whether this should call via producer using the 'axiom:host' uri
            /*
            //e.g.,
            hostContext.createProducerTemplate().
                sendBodyAndHeader(Environment.AXIOM_HOST_URI,
                    loader.getBuilder(), "command", "configure");*/
            hostContext.addRoutes(loader.load());
        } catch (Exception e) {
            throw new LifecycleException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Sends new routing configuration to the control channel, which is
     * subsequently processed based on the chosen startup mode (as set by
     * the {@code axiom.bootstrap.startup.mode} system property).
     * @param builder The builder containing the configuration you wish to apply
     */
    public void configure(final RouteBuilder builder) {
        final String channelUri = getConfig().getString(Environment.CONTROL_CHANNEL);
        final ProducerTemplate<Exchange> producer = getContext().createProducerTemplate();
        producer.sendBodyAndHeader(channelUri, builder, "command", "configure");
    }

    /**
     * Sends new routing configuration to the control channel, which is
     * subsequently processed based on the chosen startup mode (as set by
     * the {@code axiom.bootstrap.startup.mode} system property).
     * @param routeLoader An object which can load the configuration you wish to apply
     */
    public void configure(final RouteLoader routeLoader) {
        configure(routeLoader.getBuilder());
    }

    /**
     * Activates the control channel, which will from now on behave in
     * accordance with the routes you set up in your bootstrap script.
     */
    public void activate() {
        try {
            log.info("Activating control channel.");
            Configuration config = getRegisteredConfiguration(getContext());
            log.info("Configuring tracer for {}.", getContext());
            TraceBuilder builder = new TraceBuilder(config, tracer);
            getContext().addInterceptStrategy(builder.build());
            getContext().start();
        } catch (Exception e) {
            throw new LifecycleException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Closes the control channel and stops the underlying service(s) -
     * after this call completes, all channel services are terminated
     * and therefore the channel is effectively useless.
     */
    public void destroy() {
        log.info("Stopping control channel.");
        try {
            getContext().stop();
        } catch (Exception e) {
            throw new LifecycleException(e.getLocalizedMessage(), e);
        }
    }

    public RouteConfigurationScriptEvaluator getRouteScriptEvaluator() {
        return lookup(Environment.ROUTE_SCRIPT_EVALUATOR,
                RouteConfigurationScriptEvaluator.class);
    }

    /**
     * Gets the underlying {@link CamelContext}. It is recommended that
     * you do not interfere with this service unless you *really* know what
     * you're doing.
     * @return
     */
    public CamelContext getContext() {
        return hostContext;
    }

    /**
     * Gets the {@link Tracer} instance attached to the underlying
     * {@link CamelContext}. This can be used to configure and enable/disable
     * tracing dynamically at runtime.
     * @return
     */
    public Tracer getTracer() {
        return tracer;
    }

    /**
     * Looks up a service in the registry underlying the backing {@link CamelContext},
     * returning the service or null if it could not be found.
     * @param key The registered name of the service
     * @param clazz The expected type of the service instance
     * @param <T>
     * @return A service/object of the requisite type, or {@code null} if no registered
     * instance(s) match the supplied {@code key}.
     */
    public <T> T lookup(final String key, Class<T> clazz) {
        return getContext().getRegistry().lookup(key, clazz);
    }

    public Configuration getConfig() {
        //TODO: consider whether lazy init is really needed here!?
        //NB: configuration instance is a singleton so potential
        //    overwrite stomping due to concurrent access isn't going to cause any issues
        if (config == null) {
            config = getRegisteredConfiguration(hostContext);
            if (config == null) {
                throw new LifecycleException(MessageFormat.format(
                    "Context Registry is incorrectly configured: bean for id {0} is not present.",
                    Environment.CONFIG_BEAN
                ));
            }
        }
        return config;
    }
}
