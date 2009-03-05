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
import org.apache.camel.Route;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.processor.interceptor.Tracer;
import org.apache.camel.spi.Registry;
import org.apache.commons.configuration.Configuration;
import org.axiom.SpecSupport;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collection;

@RunWith(JDaveRunner.class)
public class ControlChannelSpec extends Specification<ControlChannel> {

    private CamelContext context;
    private RouteLoader loader;
    private Tracer tracer;
    private Configuration config;
    private Registry registry;
    private ControlChannel channel;

    public class WhenInitializingNewInstances extends SpecSupport {

        public void itShouldPukeIfTheTracerOrContextInstanceIsMissing() {
            specify(new Block() {
                @Override public void run() throws Throwable {
                    new ControlChannel(null, dummy(Tracer.class));
                }
            },
            should.raise(IllegalArgumentException.class));

            specify(new Block() {
                @Override public void run() throws Throwable {
                    new ControlChannel(dummy(CamelContext.class), null);
                }
            },
            should.raise(IllegalArgumentException.class));
        }        

    }

    public class WhenLoadingRoutesAndAddingThenToTheChannel extends SpecSupport {

        public ControlChannel create() {
            context = mock(CamelContext.class);
            loader = mock(RouteLoader.class);
            return new ControlChannel(context, dummy(Tracer.class, "dummy-trace"));
        }

        public void itShouldPukeIfTheSuppliedLoaderIsNull() {
            specify(new Block() {
                @Override public void run() throws Throwable {
                    new ControlChannel(context).load(null);
                }
            }, should.raise(IllegalArgumentException.class));            
        }

        public void itShouldLoadTheBuildlerUsingTheSuppliedLoader() {
            one(loader).load();
            will(returnValue(null));
            justIgnore(context);
            checking(this);

            new ControlChannel(context).load(loader);
        }

        public void itShouldPassTheLoadedRoutesToTheSuppliedContext() throws Exception {
            final Collection<Route> routes = new ArrayList<Route>();
            allowing(loader).load();
            will(returnValue(routes));
            one(context).addRoutes(routes);
            checking(this);

            new ControlChannel(context).load(loader);
        }

        public void itShouldWrapCheckedExceptionsWithRuntime() throws Exception {
            allowing(loader);
            one(context).addRoutes((Collection<Route>) with(anything()));
            will(throwException(new Exception()));
            checking(this);

            specify(new Block() {
                @Override public void run() throws Throwable {
                    new ControlChannel(context).load(loader);
                }
            }, should.raise(LifecycleException.class));
        }

    }

    public class WhenConfiguringTheChannel extends SpecSupport {

        public ControlChannel create() {
            context = mock(CamelContext.class);
            tracer = mock(Tracer.class);
            config = dummy(Configuration.class);
            registry = mock(Registry.class);
            return channel = new ControlChannel(context, tracer);
        }

        public void itShouldAttemptObtainingTracerInstanceFromTheContextInitially() {
            DefaultCamelContext context = new DefaultCamelContext();
            context.addInterceptStrategy(tracer);
            ControlChannel channel = new ControlChannel(context);

            specify(channel.getTracer(), same(tracer));
        }

        public void itShouldCreateNewTracerInstanceIfNoneIsPresent() {
            final ControlChannel channel = new ControlChannel(new DefaultCamelContext());
            specify(channel.getTracer(), isNotNull());
        }

        @SuppressWarnings({"unchecked"})
        public void itShouldConfigureTheTracerBasedOnSuppliedProperties() {
            allowing(context).getRegistry();
            will(returnValue(registry));
            allowing(registry).lookup(with(any(String.class)), with(any(Class.class)));
            will(returnValue(config));
            ignoring(tracer).setEnabled(with(any(Boolean.class)));

            one(tracer).isEnabled();
            will(returnValue(false));

            justIgnore(tracer, context);
            checking(this);

            channel.configure();
        }

    }
}
