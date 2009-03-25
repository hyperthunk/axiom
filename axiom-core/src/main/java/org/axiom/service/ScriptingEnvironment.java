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
import org.apache.commons.configuration.Configuration;
import static org.apache.commons.lang.StringUtils.*;
import static org.apache.commons.lang.Validate.*;
import static org.axiom.configuration.ExternalConfigurationSourceFactory.*;
import org.axiom.integration.Environment;
import org.axiom.integration.jruby.JRubyScriptEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.*;

/**
 * Provides managed access to the underlying scripting environment.
 */
public class ScriptingEnvironment {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Configuration configuration;
    private final CamelContext context;
    private JRubyScriptEvaluator evaluator;

    public ScriptingEnvironment(final CamelContext context) {
        this(context, getRegisteredConfiguration(context));
    }

    public ScriptingEnvironment(final CamelContext context, final Configuration configuration) {
        notNull(context, "Camel context cannot be null.");
        this.context = context;
        this.configuration = configuration;
        evaluator = lookupEvaluatorService();
    }

    public void activate() {
        log.info("Starting jruby scripting environment.");
        log.debug("Requiring jruby jar from {}.", Environment.JRUBY_JAR);
        evaluateScriptFragment("require '" + Environment.JRUBY_JAR + "'");
        final String pluginUris = configuration.getString(Environment.ENDORSED_PLUGINS, null);
        log.debug("Plugin uris: [{}]", pluginUris);
        if (isNotEmpty(pluginUris)) {
            log.info("Adding {} to the jruby LOAD_PATH.", pluginUris);
            final String scriptFragment =
                format("'%s'.split(File::PATH_SEPARATOR).each { |path| " +
                    "$LOAD_PATH.unshift path unless $LOAD_PATH.include? path }", pluginUris);
            evaluateScriptFragment(scriptFragment);
        }
    }

    public void registerContext() {

    }

    /**
     * Evaluates the supplied script fragment. This is done in a global
     * context, so you shouldn't make too many assumptions when calling it.
     * @param scriptFragment A fragment of ruby code.
     * @return the last value on the stack after evaluating the supplied code.
     */
    public Object evaluateScriptFragment(final String scriptFragment) {
        log.debug("[Script <eval>]{}{}", Environment.NEWLINE, scriptFragment);
        return evaluator.evaluate(scriptFragment);
    }

    private JRubyScriptEvaluator lookupEvaluatorService() {
        return context.getRegistry().lookup(
            Environment.CODE_EVALUATOR, JRubyScriptEvaluator.class);
    }
}
