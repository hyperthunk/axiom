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
import static org.apache.commons.lang.Validate.*;
import static org.axiom.configuration.ExternalConfigurationSourceFactory.*;
import org.axiom.integration.jruby.JRubyScriptEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;

public class ScriptingEnvironment {

    public static final String ENDORSED_PLUGINS_FOLDER_PROPERTY = "axiom.plugins.endorsed.uri";

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Configuration configuration;
    private final CamelContext context;

    public ScriptingEnvironment(final CamelContext context) {
        this(context, getRegisteredInstance(context));
    }

    public ScriptingEnvironment(final CamelContext context, final Configuration configuration) {
        notNull(context, "Camel context cannot be null.");
        this.context = context;
        this.configuration = configuration;
    }

    public void start() throws LifecycleException {
        final JRubyScriptEvaluator evaluator = lookupEvaluatorService();
        //bootstrap plugins directory onto the $LOAD_PATH
        final String pluginUris = configuration.getString(ENDORSED_PLUGINS_FOLDER_PROPERTY);
        log.info("Adding {} to the jruby LOAD_PATH.", pluginUris);
        final String scriptFragment =
            format("'%s'.split(File.PATH_SEPARATOR).each { |path| " +
                "$LOAD_PATH.unshift path unless $LOAD_PATH.include? path }", pluginUris);
        evaluator.evaluate(scriptFragment);
    }

    private JRubyScriptEvaluator lookupEvaluatorService() {
        final String evalServiceId =
            configuration.getString(JRubyScriptEvaluator.PROVIDER_BEAN_ID);
        return context.getRegistry().lookup(evalServiceId, JRubyScriptEvaluator.class);
    }
}