package org.axiom.management;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.Processor;

/**
 *  Provides a means to configure a jruby routing script
 *  and obtain a  {@link org.apache.camel.builder.RouteBuilder}.
 */
public interface RouteConfigurationScriptEvaluator {
    /**
     * Runs the provided script source to obtain a
     * {@link org.apache.camel.builder.RouteBuilder}.
     * @param scriptSource
     * @return
     */
    RouteBuilder configure(final String scriptSource);
}
