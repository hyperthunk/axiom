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
import org.apache.camel.processor.interceptor.Tracer;
import org.apache.camel.processor.LoggingLevel;
import org.apache.camel.spi.Registry;
import org.apache.camel.spi.InterceptStrategy;
import org.apache.commons.configuration.Configuration;
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
    private Tracer tracer;
    private Configuration config;
    private Registry registry;

    @Before
    public void beforeEach() {
        mockery = new Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);
        }};
        context = mockery.mock(CamelContext.class);
        loader = mockery.mock(RouteLoader.class);
        builder = mockery.mock(RouteBuilder.class);
        tracer = mockery.mock(Tracer.class);
        config = mockery.mock(Configuration.class);
        registry = mockery.mock(Registry.class);
    }

    @Test(expected=IllegalArgumentException.class)
    public void itShouldPukeIfTheSuppliedContextIsNull() {
        new ControlChannel(null, tracer);
    }

    @Test(expected=IllegalArgumentException.class)
    public void itShouldPukeIfTheSuppliedTracerIsNull() {
        new ControlChannel(context, null);
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

    @Test
    public void itShouldPullTheConfigurationDataFromTheContextRegistry() {
        mockery.checking(new Expectations() {{
            stubConfigurationData();
            allowing(config);
            allowing(context);
        }});

        new ControlChannel(context).configure();
    }

    @Test
    public void itShouldNotExplicitlySetupTracingWhenDisabledInConfigurationSettings() {
        stubConfigurationData().enableTracing(false);
        mockery.checking(new Expectations() {{
            never(context).addInterceptStrategy((InterceptStrategy) with(anything()));
            allowing(registry);
        }});

        new ControlChannel(context).configure();
    }

    @Test
    public void itShouldAddTracerWhenTracingIsEnabled() {
        final Tracer tracer = new Tracer();
        stubConfigurationData().
            enableTracing().
            stubTraceLevel("error");
        mockery.checking(new Expectations() {{
            one(context).addInterceptStrategy(tracer);
            allowing(registry);
        }});

        new ControlChannel(context, tracer).configure();
    }

    @Test
    public void itShouldRetrieveAndSetTheLoggingLevelForTracing() {
        stubConfigurationData().
            enableTracing().
            stubTraceLevel("error");
        mockery.checking(new Expectations() {{
            one(tracer).setLogLevel(LoggingLevel.ERROR);
            allowing(context);
            allowing(registry);
        }});

        new ControlChannel(context, tracer).configure();
    }    

    private TestControlChannel stubConfigurationData() {
        mockery.checking(new Expectations() {{
            allowing(context).getRegistry();will(returnValue(registry));
            allowing(registry).lookup(ControlChannel.CONFIG_BEAN_ID, Configuration.class);
            will(returnValue(config));
        }});
        return this;
    }

    private TestControlChannel enableTracing() {return enableTracing(true);}

    private TestControlChannel enableTracing(final boolean setting) {
        mockery.checking(new Expectations() {{
            allowing(config).getBoolean(ControlChannel.TRACE_ENABLED_KEY, true);
            will(returnValue(setting));
        }});
        return this;
    }

    private void stubTraceLevel(final String level) {
        mockery.checking(new Expectations() {{
            allowing(config).getString(ControlChannel.TRACE_LEVEL_STRING, "INFO");
            will(returnValue(level));
        }});
    }
}
