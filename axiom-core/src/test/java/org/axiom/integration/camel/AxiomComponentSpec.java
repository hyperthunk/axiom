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
 *
 *
 */

package org.axiom.integration.camel;

import jdave.Block;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.apache.camel.CamelContext;
import org.axiom.integration.Environment;
import org.axiom.service.ServiceSpecSupport;
import org.junit.runner.RunWith;

@RunWith(JDaveRunner.class)
public class AxiomComponentSpec extends Specification<AxiomComponent> {

    private ContextProcessingNode mockProcessor;
    private AxiomComponent component;
    private static final String PROCESSOR = "processor";

    public class WhenIntegratedIntoAnExistingCamelContext extends ServiceSpecSupport {

        public AxiomComponent create() {
            prepareMocks(mockery());
            mockProcessor = mock(mockery(), ContextProcessingNode.class);

            component = new AxiomComponent();
            component.setCamelContext(mockContext);
            component.setConfiguration(mockConfig);
            return component;
        }

        public void itShouldLookInTheRegistryForTheNamedContext() throws Exception {
            //TODO: refactor this - it's a mess
            final String contextBeanName = "camelContextBean1";
            allowing(mockContext).getRegistry();
            will(returnValue(mockRegistry));

            // the return value is unimportant for this test, so we'll just use the
            // mockContext we created to return this registry!
            one(mockRegistry).lookup(contextBeanName, CamelContext.class);
            will(returnValue(mockContext));

            one(mockConfig).getString(Environment.DEFAULT_PROCESSOR_BEAN_ID);
            will(returnValue(PROCESSOR));

            allowing(mockRegistry).lookup(PROCESSOR, ContextProcessingNode.class);
            will(returnValue(mockProcessor));

            allowing(mockProcessor);
            allowing(mockContext);
            checking(this);

            component.createEndpoint(String.format("axiom:%s", contextBeanName));
        }

        public void itShouldPukeIfTheNamedBeanIsNotFoundInTheRegistry() throws Exception {
            //TODO: refactor this - it's a mess
            allowing(mockContext).getRegistry();
            will(returnValue(mockRegistry));

            one(mockRegistry).lookup(with(any(String.class)), with(any(Class.class)));
            will(returnValue(null));
            checking(this);

            specify(new Block() {
                @Override public void run() throws Throwable {
                    component.createEndpoint("axiom:no-such-registered-camel-context");
                }
            }, should.raise(IllegalArgumentException.class));
        }

        public void itShouldResolveSpecialHostAddressToTheCurrentContext() throws Exception {
            //TODO: refactor this - it's a mess
            allowing(mockContext).getRegistry();
            will(returnValue(mockRegistry));

            allowing(mockConfig).getString(Environment.DEFAULT_PROCESSOR_BEAN_ID);
            will(returnValue(PROCESSOR));

            never(mockRegistry).lookup(with(any(String.class)), with(equal(CamelContext.class)));

            allowing(mockRegistry).lookup(PROCESSOR, ContextProcessingNode.class);
            will(returnValue(mockProcessor));

            allowing(mockContext);
            allowing(mockProcessor);
            checking(this);

            component.createEndpoint("axiom:host");
        }

        public void itShouldLookupTheProcessingNodeInTheRegistry() throws Exception {
            //TODO: refactor this - it's a mess
            final CamelContext mockTargetContext = mockContext;
            final String beanId = "axiom.control.processor.default";

            allowing(mockContext).getRegistry();
            will(returnValue(mockRegistry));

            one(mockConfig).getString(Environment.DEFAULT_PROCESSOR_BEAN_ID);
            will(returnValue(beanId));

            one(mockRegistry).lookup(with(any(String.class)), with(any(Class.class)));
            will(returnValue(mockTargetContext));

            one(mockRegistry).lookup(beanId, ContextProcessingNode.class);
            will(returnValue(mockProcessor));

            allowing(mockContext);
            allowing(mockProcessor);
            checking(this);

            component.createEndpoint("axiom:ignored");
        }

        public void itShouldSetTheTargetContextOnTheProcessingNode() throws Exception {
            final CamelContext mockTargetContext = mockContext;
            //TODO: refactor this - it's a mess
            allowing(mockContext).getRegistry();
            will(returnValue(mockRegistry));

            allowing(mockConfig).getString(Environment.DEFAULT_PROCESSOR_BEAN_ID);
            will(returnValue(PROCESSOR));

            allowing(mockRegistry).lookup(PROCESSOR, ContextProcessingNode.class);
            will(returnValue(mockProcessor));

            one(mockRegistry).lookup(with(any(String.class)), with(any(Class.class)));
            will(returnValue(mockTargetContext));

            allowing(mockContext);
            one(mockProcessor).setContext(mockContext);
            checking(this);

            component.createEndpoint("axiom:ignored");
        }
    }
}
