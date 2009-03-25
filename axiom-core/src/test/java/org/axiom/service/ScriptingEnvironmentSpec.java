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
import org.apache.commons.configuration.Configuration;
import org.axiom.integration.Environment;
import org.axiom.integration.jruby.JRubyScriptEvaluator;
import static org.hamcrest.Matchers.*;
import org.junit.runner.RunWith;

import java.io.File;
import java.text.MessageFormat;

@RunWith(JDaveRunner.class)
public class ScriptingEnvironmentSpec
    extends Specification<ScriptingEnvironment> {

    private ScriptingEnvironment scriptEnv;
    private JRubyScriptEvaluator evaluator = mock(JRubyScriptEvaluator.class, "evaluator");

    private final String evaluatorBeanId = "axiom.processors.code.eval.id";

    public class WhenConfiguringAndStartingUp extends ServiceSpecSupport {

        public ScriptingEnvironment create() throws ClassNotFoundException {
            prepareMocks(mockery());
            allowing(mockContext).getRegistry();
            will(returnValue(mockRegistry)) ;
            stubLookup(Environment.CODE_EVALUATOR, evaluator);
            checking(this);
            return scriptEnv = new ScriptingEnvironment(mockContext, mockConfig);
        }

        public void itShouldLookupThemockConfigurationAndEvaluatorBeansInTheSuppliedCamelContext()
            throws ClassNotFoundException {
            stubConfiguration(mockContext, mockRegistry, mockConfig);
            one(mockConfig).getString(Environment.ENDORSED_PLUGINS, null);
            will(returnValue(null));

            justIgnore(mockConfig, mockRegistry, evaluator);
            checking(this);

            scriptEnv.activate();
        }

        public void itShouldUnshiftEachPluginDirectoryOntoTheJRubyLoadPath() {
            final String pluginPaths =
                MessageFormat.format("plugins{0}~/.axiom/plugins{0}/usr/local/axiom/plugins", 
                    File.pathSeparator);

            stubConfiguration(mockContext, mockRegistry, mockConfig);
            allowing(mockConfig).getString(Environment.ENDORSED_PLUGINS, null);
            will(returnValue(pluginPaths));

            /*allowing(mockRegistry).lookup(evaluatorBeanId, JRubyScriptEvaluator.class);
            will(returnValue(evaluator));*/
            justIgnore(mockConfig, mockRegistry);

            allowing(evaluator).evaluate("require '" + Environment.JRUBY_JAR + "'");
            will(returnValue(true));
            
            one(evaluator).evaluate(with(
                equalToIgnoringWhiteSpace(String.format("'%s'.split(File::PATH_SEPARATOR)." +
                    "each { |path| $LOAD_PATH.unshift path unless $LOAD_PATH.include? path }", 
                    pluginPaths))));
            will(returnValue(true));
            checking(this);

            scriptEnv.activate();
        }

        public void itShouldPukeIfTheSuppliedContextIsNull() {
            specify(new Block() {
                @Override public void run() throws Throwable {
                    new ScriptingEnvironment(null, dummy(Configuration.class));
                }
            }, should.raise(IllegalArgumentException.class));
        }
    }
}
