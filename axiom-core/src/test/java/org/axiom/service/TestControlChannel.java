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
import org.apache.camel.Route;
import org.apache.camel.builder.RouteBuilder;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collection;

@RunWith(JMock.class)
public class TestControlChannel {

    private Mockery mockery;
    private CamelContext context;
    private RouteLoader loader;
    private RouteBuilder builder;

    @Before
    public void beforeEach() {
        mockery = new Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);
        }};
        context = mockery.mock(CamelContext.class);
        loader = mockery.mock(RouteLoader.class);
        builder = mockery.mock(RouteBuilder.class);
    }

    @Test(expected=IllegalArgumentException.class)
    public void itShouldPukeIfTheSuppliedContextIsNull() {
        new ControlChannel(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void itShouldPukeIfTryingToLoadFromNullRouteLoader() {
        new ControlChannel(context).load(null);
    }

    @Test
    public void itShouldLoadTheBuildlerUsingTheSuppliedLoader() {
        mockery.checking(new Expectations() {{
            one(loader).load();
            will(returnValue(null));
            allowing(context);
        }});
        new ControlChannel(context).load(loader);
    }

    @Test
    public void itShouldPassTheLoadedRoutesToTheSuppliedContext() throws Exception {
        final Collection<Route> routes = new ArrayList<Route>();
        mockery.checking(new Expectations() {{
            allowing(loader).load();
            will(returnValue(routes));

            one(context).addRoutes(routes);
        }});
        new ControlChannel(context).load(loader);
    }

    @Test(expected = LifecycleException.class)
    public void itShouldWrapCheckedExceptionsWithRuntime() throws Exception {
        final Exception ex = new Exception();
        mockery.checking(new Expectations() {{
            allowing(loader);
            one(context).addRoutes((Collection<Route>) with(anything()));
            will(throwException(ex));
        }});
        new ControlChannel(context).load(loader);
    }

}
