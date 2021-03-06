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

import org.apache.camel.model.LoggingLevel;
import org.apache.camel.processor.interceptor.TraceFormatter;
import org.apache.camel.processor.interceptor.Tracer;
import org.apache.commons.configuration.Configuration;
import static org.apache.commons.lang.Validate.*;

import static java.text.MessageFormat.*;

public class TraceBuilder {

    private static final String TRACE_CONFIG_ROOT = "axiom.configuration.trace.";
    protected static final String TRACE_INTERCEPTORS = format("{0}include.interceptors", TRACE_CONFIG_ROOT);
    protected static final String TRACE_EXCEPTIONS = format("{0}include.exceptions", TRACE_CONFIG_ROOT);
    protected static final String TRACE_ENABLED = format("{0}enabled", TRACE_CONFIG_ROOT);
    protected static final String TRACE_LEVEL = format("{0}logLevel", TRACE_CONFIG_ROOT);
    protected static final String TRACE_NAME = format("{0}logName", TRACE_CONFIG_ROOT);
    protected static final String TRACE_BREADCRUMB_LENGTH = format("{0}format.breadCrumbLength", TRACE_CONFIG_ROOT);
    protected static final String TRACE_SHOW_BREADCRUMBS = format("{0}format.showBreadCrumb", TRACE_CONFIG_ROOT);
    protected static final String TRACE_SHOW_EXCHANGE_PROPS = format("{0}format.showProperties", TRACE_CONFIG_ROOT);
    protected static final String TRACE_SHOW_EXCHANGE_HDRS = format("{0}format.showHeaders", TRACE_CONFIG_ROOT);
    protected static final String TRACE_SHOW_EXCHANGE_BODY_TYPE = format("{0}format.showBodyType", TRACE_CONFIG_ROOT);
    protected static final String TRACE_SHOW_EXCHANGE_BODY = format("{0}format.showBody", TRACE_CONFIG_ROOT);

    private final Configuration config;
    private final Tracer tracer;

    //TODO: move these out into a resource bundle
    protected static final String MISSING_CONFIG_MSG = "Configuration instance cannot be null.";
    protected static final String MISSING_TRACER_MSG = "Tracer instance cannot be null" ;

    public TraceBuilder(final Configuration config, final Tracer tracer) {
        notNull(config, MISSING_CONFIG_MSG);
        notNull(tracer, MISSING_TRACER_MSG);
        this.config = config;
        this.tracer = tracer;
    }

    public Tracer build() {
        tracer.setEnabled(config.getBoolean(TRACE_ENABLED));
        if (tracer.isEnabled()) {
            tracer.setLogLevel(getTraceLevel());
            final String logName = config.getString(TRACE_NAME, null);
            if (logName != null) {
                tracer.setLogName(logName);
            }
            tracer.setTraceInterceptors(config.getBoolean(TRACE_INTERCEPTORS));
            tracer.setTraceExceptions(config.getBoolean(TRACE_EXCEPTIONS));
            configureTraceFormat(tracer.getFormatter());
        }
        return tracer;
    }

    private void configureTraceFormat(final TraceFormatter formatter) {
        formatter.setShowBreadCrumb(config.getBoolean(TRACE_SHOW_BREADCRUMBS));
        formatter.setShowProperties(config.getBoolean(TRACE_SHOW_EXCHANGE_PROPS));
        formatter.setShowHeaders(config.getBoolean(TRACE_SHOW_EXCHANGE_HDRS));
        formatter.setShowBodyType(config.getBoolean(TRACE_SHOW_EXCHANGE_BODY_TYPE));
        formatter.setShowBody(config.getBoolean(TRACE_SHOW_EXCHANGE_BODY));
    }

    private LoggingLevel getTraceLevel() {
        final String level = config.getString(TRACE_LEVEL).toUpperCase();
        return LoggingLevel.valueOf(level);
    }
}
