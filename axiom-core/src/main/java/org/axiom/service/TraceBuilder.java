package org.axiom.service;

import org.apache.camel.processor.interceptor.Tracer;
import org.apache.camel.processor.LoggingLevel;
import org.apache.commons.configuration.Configuration;
import static org.apache.commons.lang.Validate.notNull;

public class TraceBuilder {

    protected static final String TRACE_INTERCEPTORS_KEY = "axiom.core.configuration.trace.include.interceptors";
    protected static final String TRACE_EXCEPTIONS_KEY = "axiom.core.configuration.trace.include.exceptions";
    protected static final String TRACE_ENABLED_KEY = "axiom.core.configuration.trace.enabled";
    protected static final String TRACE_LEVEL_KEY = "axiom.core.configuration.trace.logLevel";

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

    public TraceBuilder(final Configuration config) {
        this(config, new Tracer());
    }

    public Tracer build() {
        tracer.setLogLevel(getTraceLevel());
        tracer.setTraceInterceptors(config.getBoolean(TraceBuilder.TRACE_INTERCEPTORS_KEY));
        return tracer;
    }

    private LoggingLevel getTraceLevel() {
        final String level = config.getString(TRACE_LEVEL_KEY).toUpperCase();
        return LoggingLevel.valueOf(level);
    }
}
