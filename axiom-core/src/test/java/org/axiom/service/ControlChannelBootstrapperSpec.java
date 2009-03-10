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
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spi.Registry;
import org.apache.commons.configuration.Configuration;
import org.axiom.SpecSupport;
import org.axiom.integration.camel.RouteConfigurationScriptEvaluator;
import org.axiom.integration.Environment;
import org.junit.runner.RunWith;

import static java.text.MessageFormat.*;
import java.util.ArrayList;

@RunWith(JDaveRunner.class)
public class ControlChannelBootstrapperSpec extends Specification<ControlChannelBootstrapper> {

    private CamelContext mockContext = mock(CamelContext.class);
    private Registry mockRegistry = mock(Registry.class);
    private ControlChannelBootstrapper bootstrapper;
    private ControlChannel channel = new ControlChannel(mockContext);
    private Configuration mockConfig = mock(Configuration.class);
    private RouteConfigurationScriptEvaluator mockRouteBuilder = mock(RouteConfigurationScriptEvaluator.class);
    private final String codeEvaluatorBeanId = "axiomCoreControlCodeEvaluator";

    public class WhenBootstrappingTheControlChannel extends SpecSupport {

        public ControlChannelBootstrapper create() {
            allowing(mockContext).getName();
            will(returnValue("mock-context"));
            return bootstrapper = new ControlChannelBootstrapper();
        }

        public void itShouldPukeIfTheContextIsNull() {
            specify(new Block() {
                @Override public void run() throws Throwable {
                    bootstrapper.bootstrap(null);
                }
            }, should.raise(IllegalArgumentException.class));
        }

        public void itShouldPukeIfTheRegistryIsIncorrectlyConfigured() {
            stubRegistry();
            allowing(mockRegistry).lookup(with(any(String.class)),
                with(equal(Configuration.class)));
            will(returnValue(null));
            checking(this);

            specify(new Block() {
                @Override public void run() throws Throwable {
                    bootstrapper.bootstrap(channel);
                }
            }, should.raise(LifecycleException.class, format(
                "Context Registry is incorrectly configured: bean for id {0} is not present.",
                Environment.CONFIG_BEAN_ID)));
        }

        public void itShouldEvaluateTheConfiguredBootstrapScript() throws Throwable {
            stubForDefaultBootstrap();

            one(mockRouteBuilder).configure(with(any(String.class)));
            will(returnValue(dummy(RouteBuilder.class)));

            justIgnore(mockRouteBuilder, mockConfig, mockContext);
            checking(this);

            specify(new Block() {
                @Override public void run() throws Throwable {
                    bootstrapper.bootstrap(channel);
                }
            }, should.not().raiseAnyException());
        }

        public void itShouldEvaluateEachConfiguredBootstrapExtensionScript() throws Throwable {
            stubForDefaultBootstrap();
            one(mockRouteBuilder).configure(with(any(String.class)));
            will(returnValue(dummy(RouteBuilder.class, "first-routebuilder-mock")));

            final ArrayList<String> extensions = new ArrayList<String>() {{
                add("axiom.bootstrap.extended.script.additions");
                add("axiom.bootstrap.extended.script.url");
            }};

            //the tests doesn't know we're bluffing here
            allowing(mockConfig).getKeys("axiom.bootstrap.extended.script");
            will(returnValue(extensions.iterator()));
            for (final String item : extensions) {
                stubConfig(item, "classpath:test-boot.rb");
            }

            exactly(2).of(mockRouteBuilder).configure(with(any(String.class)));
            will(returnValue(dummy(RouteBuilder.class, "second-routebuilder-mock")));
            justIgnore(mockContext);
            checking(this);

            specify(new Block() {
                @Override public void run() throws Throwable {
                    bootstrapper.bootstrap(channel);
                }
            }, should.not().raiseAnyException());            
        }

        private void stubForDefaultBootstrap() throws ClassNotFoundException {
            stubRegistry();
            stubLookup("axiom.configuration", mockConfig);
            stubConfig("axiom.control.processors.evaluator.id", codeEvaluatorBeanId);
            stubLookup(codeEvaluatorBeanId, mockRouteBuilder);
            stubConfig(ControlChannelBootstrapper.DEFAULT_SCRIPT_URI, "classpath:test-boot.rb");
        }

        //TODO: make some of these stubbing utility methods available to other test classes

        private void stubConfig(final String key, final String returns) {
            allowing(mockConfig).getString(key);
            will(returnValue(returns));
        }

        private <T> void stubLookup(final String key, T value) throws ClassNotFoundException {
            Class<?> clazz = value.getClass();
            final int enhancerTagIdx = clazz.getName().indexOf("$$EnhancerByCGLIB$$");
            if (enhancerTagIdx > 0) {
                clazz = Class.forName(clazz.getName().substring(0, enhancerTagIdx));
            }
            allowing(mockRegistry).lookup(key, clazz);
            will(returnValue(value));
        }

        private void stubRegistry() {
            allowing(mockContext).getRegistry();
            will(returnValue(mockRegistry));
        }

    }

}
