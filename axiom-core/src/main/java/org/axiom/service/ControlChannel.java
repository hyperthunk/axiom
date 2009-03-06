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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a managed message channel that can be used to
 * control {@link CamelContext}s. The control channel is itself
 * managed by a private {@link CamelContext}, which is in turn
 * configured using JRuby scripts and/or by passing Camel Spring
 * XML to one of the uris exposed as a consumer.
 */
public class ControlChannel implements ManagedComponent {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final CamelContext context;
    private final Tracer tracer;

    public ControlChannel(final CamelContext context) {
        this(context, getTracer(context));
    }

    public ControlChannel(final CamelContext context, final Tracer tracer) {
        notNull(context, "Camel context cannot be null.");
        notNull(tracer, "Tracer cannot be null.");
        this.tracer = tracer;
        this.context = context;
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
            context.addRoutes(loader.load());
        } catch (Exception e) {
            throw new LifecycleException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override public void start() {
        try {
            log.info("Starting control channel.");
            Configuration config = getRegisteredInstance(getContext());
            log.info("Configuring tracer for {}.", getContext());
            TraceBuilder builder = new TraceBuilder(config, tracer);
            getContext().addInterceptStrategy(builder.build());
            getContext().start();
        } catch (Exception e) {
            throw new LifecycleException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */    
    @Override public void stop() {
        log.info("Stopping control channel.");
        try {
            getContext().stop();
        } catch (Exception e) {
            throw new LifecycleException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Gets the underlying {@link CamelContext}. It is recommended that
     * you do not interfere with this service unless you *really* know what
     * you're doing.
     * @return
     */
    public CamelContext getContext() {
        return context;
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
}
