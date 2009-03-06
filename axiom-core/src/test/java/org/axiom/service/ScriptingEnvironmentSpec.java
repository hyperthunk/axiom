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

import jdave.Block;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.apache.camel.CamelContext;
import org.apache.camel.spi.Registry;
import org.apache.commons.configuration.Configuration;
import org.axiom.SpecSupport;
import org.axiom.integration.jruby.JRubyScriptEvaluator;
import static org.axiom.service.ScriptingEnvironment.*;
import static org.hamcrest.Matchers.*;
import org.junit.runner.RunWith;

import java.io.File;
import java.text.MessageFormat;

@RunWith(JDaveRunner.class)
public class ScriptingEnvironmentSpec
    extends Specification<ScriptingEnvironment> {

    private ScriptingEnvironment scriptEnv;
    private CamelContext mockContext = mock(CamelContext.class);
    private Registry registry = mock(Registry.class);
    private Configuration config = mock(Configuration.class);
    private JRubyScriptEvaluator evaluator = mock(JRubyScriptEvaluator.class, "evaluator");

    private final String evaluatorBeanId = "axiomCoreScriptEvaluator";

    public class WhenConfiguringAndStartingUp extends SpecSupport {

        public ScriptingEnvironment create() {
            return scriptEnv = new ScriptingEnvironment(mockContext, config);
        }

        public void itShouldLookupTheConfigurationAndEvaluatorBeansInTheSuppliedCamelContext() {
            stubEvalBeanId();
            one(registry).lookup(evaluatorBeanId, JRubyScriptEvaluator.class);
            will(returnValue(dummy(JRubyScriptEvaluator.class)));
            
            justIgnore(config, registry);
            checking(this);

            scriptEnv.start();
        }

        public void itShouldUnshiftEachPluginDirectoryOntoTheJRubyLoadPath() {
            final String pluginPaths =
                MessageFormat.format("plugins{0}~/.axiom/plugins{0}/usr/local/axiom/plugins", 
                    File.pathSeparator);

            stubEvalBeanId();
            allowing(config).getString(ENDORSED_PLUGINS_FOLDER_PROPERTY);
            will(returnValue(pluginPaths));

            allowing(registry).lookup(evaluatorBeanId, JRubyScriptEvaluator.class);
            will(returnValue(evaluator));
            justIgnore(config, registry);

            one(evaluator).evaluate(with(
                equalToIgnoringWhiteSpace(String.format("'%s'.split(File.PATH_SEPARATOR)." +
                    "each { |path| $LOAD_PATH.unshift path unless $LOAD_PATH.include? path }", 
                    pluginPaths))));
            will(returnValue(true));
            checking(this);

            scriptEnv.start();
        }

        public void itShouldPukeIfTheSuppliedContextIsNull() {
            specify(new Block() {
                @Override public void run() throws Throwable {
                    new ScriptingEnvironment(null, dummy(Configuration.class));
                }
            }, should.raise(IllegalArgumentException.class));
        }

        private void stubEvalBeanId() {
            stubConfiguration(mockContext, registry, config);
            allowing(config).getString("axiom.core.script.evaluator.id");
            will(returnValue(evaluatorBeanId));
        }
    }
}
