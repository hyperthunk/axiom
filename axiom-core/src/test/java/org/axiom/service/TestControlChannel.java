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
import org.apache.camel.processor.LoggingLevel;
import org.apache.camel.processor.interceptor.Tracer;
import org.apache.camel.spi.InterceptStrategy;
import org.apache.camel.spi.Registry;
import org.apache.commons.configuration.Configuration;
import org.axiom.SpecSupport;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collection;

@RunWith(JMock.class)
public class TestControlChannel extends SpecSupport {

    private Mockery mockery;
    private CamelContext context;
    private RouteLoader loader;
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
        tracer = mockery.mock(Tracer.class);
        config = mockery.mock(Configuration.class);
        registry = mockery.mock(Registry.class);
    }

    private void verify() {
        mockery.checking(this);
    }

    @Test(expected = IllegalArgumentException.class)
    public void itShouldPukeIfTheSuppliedContextIsNull() {
        new ControlChannel(null, tracer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void itShouldPukeIfTheSuppliedTracerIsNull() {
        new ControlChannel(context, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void itShouldPukeIfTryingToLoadFromNullRouteLoader() {
        new ControlChannel(context).load(null);
    }

    @Test
    public void itShouldLoadTheBuildlerUsingTheSuppliedLoader() {
        one(loader).load();
        will(returnValue(null));
        justIgnore(context);
        this.verify();

        new ControlChannel(context).load(loader);
    }

    @Test
    public void itShouldPassTheLoadedRoutesToTheSuppliedContext() throws Exception {
        final Collection<Route> routes = new ArrayList<Route>();
        allowing(loader).load();
        will(returnValue(routes));
        one(context).addRoutes(routes);
        this.verify();

        new ControlChannel(context).load(loader);
    }

    @Test(expected = LifecycleException.class)
    public void itShouldWrapCheckedExceptionsWithRuntime() throws Exception {
        allowing(loader);
        one(context).addRoutes((Collection<Route>) with(anything()));
        will(throwException(new Exception()));
        this.verify();

        new ControlChannel(context).load(loader);
    }

    @Test
    public void itShouldPullTheConfigurationDataFromTheContextRegistry() {
        stubConfigurationData();
        justIgnore(config, context);
        this.verify();

        new ControlChannel(context).configure();
    }

    @Test
    public void itShouldNotExplicitlySetupTracingWhenDisabledInConfigurationSettings() {
        stubConfigurationData().enableTracing(false);
        never(context).addInterceptStrategy((InterceptStrategy) with(anything()));
        justIgnore(registry);
        this.verify();

        new ControlChannel(context).configure();
    }

    @Test
    public void itShouldAddTracerWhenTracingIsEnabled() {
        stubConfigurationData().
            enableTracing().
            stubTraceLevel("error").
            checkTraceInterceptors();
        one(context).addInterceptStrategy(tracer);

        justIgnore(registry, tracer);
        this.verify();

        new ControlChannel(context, tracer).configure();
    }

    @Test
    public void itShouldRetrieveAndSetTheLoggingLevelForTracing() {
        checkTraceLogLevel().
        justIgnore(config, tracer);
        this.verify();
        new ControlChannel(context, tracer).configure();
    }

    @Test
    public void itShouldRetrieveAndSetTheInterceptorTraceFlags() {
        checkTraceLogLevel().
        checkTraceInterceptors();
        this.verify();

        new ControlChannel(context, tracer).configure();
    }

    /*@Test
    public void itShouldRetrieveAndSetTheExceptionTraceFlags() {
        checkTraceLogLevel().
        checkTraceInterceptors().
        allowing(config).getBoolean("", true);
        will((returnValue(false)));
        one(tracer).setTraceExceptions(false);
        verify();

        new ControlChannel(context, tracer).configure();
    }*/

    private TestControlChannel checkTraceInterceptors() {
        allowing(config).getBoolean(TraceBuilder.TRACE_INTERCEPTORS_KEY, false);
        will(returnValue(true));
        one(tracer).setTraceInterceptors(true);
        return this;
    }

    private TestControlChannel checkTraceLogLevel() {
        stubConfigurationData().
            enableTracing().
            stubTraceLevel("error");
        one(tracer).setLogLevel(LoggingLevel.ERROR);
        justIgnore(context, registry);
        return this;
    }

    private TestControlChannel stubConfigurationData() {
        allowing(context).getRegistry();
        will(returnValue(registry));
        allowing(registry).lookup(ControlChannel.CONFIG_BEAN_ID, Configuration.class);
        will(returnValue(config));
        return this;
    }

    private TestControlChannel enableTracing() {
        return enableTracing(true);
    }

    private TestControlChannel enableTracing(final boolean setting) {
        allowing(config).getBoolean(TraceBuilder.TRACE_ENABLED_KEY, true);
        will(returnValue(setting));
        return this;
    }

    private TestControlChannel stubTraceLevel(final String level) {
        allowing(config).getString(TraceBuilder.TRACE_LEVEL_KEY, "INFO");
        will(returnValue(level));
        return this;
    }
}
