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

package org.axiom.systest;

import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.apache.camel.CamelContext;
import org.apache.commons.io.FileUtils;
import org.axiom.integration.Environment;
import org.axiom.integration.camel.ContextFactory;
import org.axiom.service.ScriptingEnvironment;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import static java.lang.String.format;

@RunWith(JDaveRunner.class)
public class JRubyIntegrationTestRunner extends Specification<CamelContext> {

    public class WhenExecutingJRubyIntegrationTests {

        private CamelContext camel;
        private String rootSpecPath;
        private ScriptingEnvironment jruby;
        private final Logger log = LoggerFactory.getLogger(getClass());

        public CamelContext create() throws IOException {
            final String pathToSpecs = getClass()
                .getClassLoader()
                .getResource("ruby/integration/http_routing_spec.rb")
                .getPath()
                .replaceAll("file:", "");
            final String integrationTestPath =
                new File(pathToSpecs).getParentFile().getAbsolutePath();
            rootSpecPath = integrationTestPath + "/**/*.rb";
            camel = new ContextFactory().create();
            jruby = camel.getRegistry().lookup(
                Environment.SCRIPTING_ENVIRONMENT, ScriptingEnvironment.class);
            System.setProperty("axiom.integration.test.source.path", integrationTestPath);
            jruby.evaluateScriptFragment(format("$axiom_testdir = '%s'", integrationTestPath));
            jruby.evaluateScriptFragment(
                "$LOAD_PATH.unshift " + "File.expand_path(['"
                    + integrationTestPath + "', '..', 'rspec', 'lib'].join(File::Separator))");
            return camel;
        }

        public void executeRSpecTestsUsingEmbeddedJRubyRuntime() throws IOException {
            final String specRunner = FileUtils.readFileToString(
                new File(
                    getClass().getClassLoader()
                        .getResource("spec_runner.rb")
                        .getPath().replaceAll("file:", "")
                )
            );
            Object o = jruby.evaluateScriptFragment(specRunner);
            if (o == null) {
                log.warn("RSpec run returned null.");
                throw new RuntimeException("RSpec Tests Failed!");
            }
            final Boolean result = (Boolean)o;
            log.info("Verifying RSpec results.");
            specify(result, equal(true));
        }

    }

}
