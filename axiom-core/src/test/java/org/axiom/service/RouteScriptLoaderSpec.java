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
import org.apache.camel.builder.RouteBuilder;
import static org.apache.commons.io.FileUtils.*;
import org.apache.commons.lang.StringUtils;
import org.axiom.SpecSupport;
import org.axiom.integration.camel.RouteConfigurationScriptEvaluator;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;

import java.io.File;

@RunWith(JDaveRunner.class)
public class RouteScriptLoaderSpec extends Specification<RouteScriptLoader> {

    private RouteConfigurationScriptEvaluator evaluator =
        mock(RouteConfigurationScriptEvaluator.class);

    protected static final String TEST_BOOT_SCRIPT = "test-boot.rb";
    protected static final String CP_BOOT_SCRIPT = String.format("classpath:%s", TEST_BOOT_SCRIPT);

    public class WhenInitializingNewInstances extends SpecSupport {
        public void itShouldPukeWhenInitializedWithMissingPath() {
            specify(new Block() {
                @Override public void run() throws Throwable {
                    new RouteScriptLoader(null,
                        dummy(RouteConfigurationScriptEvaluator.class));
                }
            }, should.raise(IllegalArgumentException.class));
        }

        public void itShouldPukeWhenInitialiezdWithEmptyPath() {
            specify(new Block() {
                @Override public void run() throws Throwable {
                    new RouteScriptLoader(StringUtils.EMPTY,
                        dummy(RouteConfigurationScriptEvaluator.class));
                }
            }, should.raise(IllegalArgumentException.class));
        }

        public void itShouldPukeWhenInitializedWithMissingEvaluator() {
            specify(new Block() {
                @Override public void run() throws Throwable {
                    new RouteScriptLoader("valid-path.rb", null);
                }
            }, should.raise(IllegalArgumentException.class));
        }
    }

    public class WhenLoadingRoutesFromScriptsResidingOnTheFileSystem extends SpecSupport {
        private RouteScriptLoader loader;

        public RouteScriptLoader create() {
            return loader = new RouteScriptLoader(CP_BOOT_SCRIPT, evaluator);
        }

        public void itShouldPukeIfTheSuppliedFileDoesNotExitAtTheGivenUri() {
            specify(new Block() {
                @Override public void run() throws Throwable {
                    new RouteScriptLoader("no-such-file.rb", evaluator).load();
                }
            }, should.raise(LifecycleException.class));
        }

        public void itShouldPassTheLoadedScriptToTheScriptEvaluator() throws Throwable {
            final File file = new ClassPathResource(TEST_BOOT_SCRIPT).getFile();
            final String bootstrapCode = readFileToString(file);

            one(evaluator).configure(bootstrapCode);
            checking(this);

            specify(new Block() {
                @Override public void run() throws Throwable { loader.load(); }
            }, should.not().raiseAnyException());
        }

        public void itShouldReturnTheRouteBuilderGeneratedByTheEvaluator() {
            final RouteBuilder routeBuilder = new RouteBuilder() {
                @Override public void configure() throws Exception {}
            };

            allowing(evaluator).configure(with(any(String.class)));
            will(returnValue(routeBuilder));
            checking(this);

            specify(loader.load(), same(routeBuilder));
        }
    }

    /*public void itShouldPullThePathToTheControlChannelBootstrapScript() {
        one(config).getString(ControlChannelBootstrapper.DEFAULT_SCRIPT_URI,
            "classpath:default-bootstrap.rb");
        will(returnValue(CP_BOOT_SCRIPT));
        allowing(evaluator).configure(with(any(String.class)));
        will(returnValue(builder));
        allowing(builder);
        checking(this);

        load();
    }*/
}
