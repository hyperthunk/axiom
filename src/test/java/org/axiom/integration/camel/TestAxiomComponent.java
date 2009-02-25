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

import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.apache.camel.Component;
import org.apache.camel.CamelContext;
import org.apache.camel.spi.Registry;
import org.jmock.integration.junit4.JMock;
import org.jmock.Mockery;
import org.jmock.Expectations;

@RunWith(JMock.class)
public class TestAxiomComponent {

    private Mockery mockery;
    private CamelContext mockContext;
    private Registry registry;

    @Before
    public void beforeEach() {
        mockery = new Mockery();
        mockContext = mockery.mock(CamelContext.class);
        registry = mockery.mock(Registry.class);
    }

    @Test
    public void itShouldLookInTheRegistryForTheNamedContext() throws Exception {
        final Component component = new AxiomComponent();
        component.setCamelContext(mockContext);

        final String contextBeanName = "camelContextBean1";

        mockery.checking(new Expectations() {{
            allowing(mockContext).getRegistry();
            will(returnValue(registry));

            // the return value is unimportant for this test, so we'll just use the
            // mockContext we created to return this registry! 
            one(registry).lookup(contextBeanName, CamelContext.class);
            will(returnValue(mockContext));
        }});

        component.createEndpoint(String.format("axiom:%s", contextBeanName));
    }

    @Test(expected=IllegalArgumentException.class)
    public void itShouldPukeIfTheNamedBeanIsNotFoundInTheRegistry() throws Exception {
        final Component component = new AxiomComponent();
        component.setCamelContext(mockContext);

        mockery.checking(new Expectations() {{
            allowing(mockContext).getRegistry();
            will(returnValue(registry));

            // the return value is unimportant for this test, so we'll just use the
            // mockContext we created to return this registry!
            one(registry).lookup(with(any(String.class)), with(any(Class.class)));
            will(returnValue(null));
        }});

        component.createEndpoint("axiom:no-such-bean-registered-camel-context");
    }

}
