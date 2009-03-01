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
import org.apache.camel.processor.LoggingLevel;
import org.apache.camel.processor.interceptor.Tracer;
import org.apache.commons.configuration.Configuration;
import static org.apache.commons.lang.Validate.*;

public class ControlChannel {

    static final String TRACE_ENABLED_KEY = "axiom.core.configuration.trace.enabled";
    static final String CONFIG_BEAN_ID = "axiom.core.configuration.id";

    private final CamelContext context;
    private final Tracer tracer;
    static final String TRACE_LEVEL_STRING = "axiom.core.configuration.trace.level";

    public ControlChannel(final CamelContext context) {
        this(context, new Tracer());
    }

    public ControlChannel(final CamelContext context, final Tracer tracer) {
        notNull(context, "Camel context cannot be null.");
        notNull(tracer, "Tracer cannot be null.");
        this.tracer = tracer;
        this.context = context;
    }

    public void load(final RouteLoader loader) {
        notNull(loader, "Route loader cannot be null.");
        try {
            context.addRoutes(loader.load());
        } catch (Exception e) {
            throw new LifecycleException(e.getLocalizedMessage(), e);
        }
    }

    public void configure() {
        Configuration config =
            context.getRegistry().lookup(CONFIG_BEAN_ID, Configuration.class);
        setupTrace(config);
        
    }

    private void setupTrace(final Configuration config) {
        final boolean traceEnabled = config.getBoolean(TRACE_ENABLED_KEY, true);
        if (traceEnabled) {
            tracer.setLogLevel(getTraceLevel(config));
            context.addInterceptStrategy(tracer);
        }
    }

    private LoggingLevel getTraceLevel(final Configuration config) {
        final String level = config.getString(TRACE_LEVEL_STRING, "INFO").toUpperCase();
        return LoggingLevel.valueOf(level);
    }
}
