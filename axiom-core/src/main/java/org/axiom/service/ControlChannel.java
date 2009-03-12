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

import org.apache.camel.CamelContext;
import org.apache.camel.processor.interceptor.Tracer;
import org.apache.commons.configuration.Configuration;
import static org.apache.commons.lang.Validate.*;
import static org.axiom.configuration.ExternalConfigurationSourceFactory.*;
import org.axiom.integration.Environment;
import org.axiom.integration.camel.RouteConfigurationScriptEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

/**
 * Provides a managed message channel that can be used to
 * control {@link CamelContext}s. The control channel is itself
 * managed by a private {@link CamelContext}, which is in turn
 * configured using JRuby scripts and/or by passing Camel Spring
 * XML to one of the uris exposed as a consumer.
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
     * Adds a set of routing/configuration rules to the control
     * channel using the supplied {@link RouteLoader}. 
     * @param loader
     */
    public void load(final RouteLoader loader) {
        notNull(loader, "Route loader cannot be null.");
        try {
            log.debug("Adding routes to context {}.", hostContext.getName());
            hostContext.addRoutes(loader.load());
        } catch (Exception e) {
            throw new LifecycleException(e.getLocalizedMessage(), e);
        }
    }

    public void configure(final RouteLoader routeLoader) {
        //To change body of created methods use File | Settings | File Templates.
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
        //TODO: reuse this elsewhere instead of duplicating this code
        return hostContext.getRegistry().
            lookup(RouteConfigurationScriptEvaluator.PROVIDER_BEAN_ID,
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

    public <T> T lookup(final String key, Class<T> clazz) {
        return getContext().getRegistry().lookup(key, clazz);
    }

    public Configuration getConfig() {
        //NB: configuration instance is a singleton so potential
        //    overwrite stomping due to concurrent access isn't going to cause any issues
        if (config == null) {
            config = getRegisteredConfiguration(hostContext);
            if (config == null) {
                throw new LifecycleException(MessageFormat.format(
                    "Context Registry is incorrectly configured: bean for id {0} is not present.",
                    Environment.CONFIG_BEAN_ID
                ));
            }
        }
        return config;
    }
}
