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
import org.apache.camel.CamelException;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.processor.interceptor.Tracer;
import org.apache.camel.spi.Registry;
import org.apache.camel.spi.InterceptStrategy;
import org.apache.commons.configuration.Configuration;
import org.axiom.SpecSupport;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings({"ThrowableInstanceNeverThrown", "unchecked"})
@RunWith(JDaveRunner.class)
public class ControlChannelSpec extends Specification<ControlChannel> {

    private CamelContext mockContext = mock(CamelContext.class);
    private RouteLoader loader = mock(RouteLoader.class);
    private Tracer tracer = mock(Tracer.class);
    private Configuration config = dummy(Configuration.class);
    private Registry registry = mock(Registry.class);
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
            return new ControlChannel(mockContext, dummy(Tracer.class, "dummy-trace"));
        }

        public void itShouldPukeIfTheSuppliedLoaderIsNull() {
            specify(new Block() {
                @Override public void run() throws Throwable {
                    new ControlChannel(mockContext).load(null);
                }
            }, should.raise(IllegalArgumentException.class));            
        }

        public void itShouldLoadTheBuildlerUsingTheSuppliedLoader() {
            one(loader).load();
            will(returnValue(null));
            justIgnore(mockContext);
            checking(this);

            new ControlChannel(mockContext).load(loader);
        }

        public void itShouldPassTheLoadedRoutesToTheSuppliedContext() throws Exception {
            final Collection<Route> routes = new ArrayList<Route>();
            allowing(loader).load();
            will(returnValue(routes));
            one(mockContext).addRoutes(routes);
            checking(this);

            new ControlChannel(mockContext).load(loader);
        }

        public void itShouldWrapCheckedExceptionsWithRuntime() throws Exception {
            allowing(loader);
            one(mockContext).addRoutes((Collection<Route>) with(anything()));
            will(throwException(new Exception()));
            checking(this);

            specify(new Block() {
                @Override public void run() throws Throwable {
                    new ControlChannel(mockContext).load(loader);
                }
            }, should.raise(LifecycleException.class));
        }

    }

    public class WhenConfiguringTheChannel extends SpecSupport {

        public ControlChannel create() {
            return channel = new ControlChannel(mockContext, tracer);
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
            stubConfiguration(mockContext, registry, config);
            ignoring(tracer).setEnabled(with(any(Boolean.class)));

            one(tracer).isEnabled();
            will(returnValue(false));

            justIgnore(tracer, mockContext);
            checking(this);

            channel.start();
        }
        
    }

    public class WhenStartingTheChannel extends SpecSupport {

        public ControlChannel create() {
            mockContext = mock(CamelContext.class, "startable-cc");
            registry = mock(Registry.class, "mocked-reg");
            return channel = new ControlChannel(mockContext, tracer);
        }

        public void itShouldWrapAnyRegistryLookupExceptions() {
            allowing(mockContext).getRegistry();
            will(returnValue(registry));
            allowing(registry).lookup(with(any(String.class)), with(any(Class.class)));
            will(throwException(new RuntimeException()));
            checking(this);
            
            specify(new Block() {
                @Override public void run() throws Throwable { channel.start(); }
            }, should.raise(LifecycleException.class));
        }

        public void itShouldWrapAnyStartupExceptions() throws Exception {
            stubConfiguration(mockContext, registry, config);
            allowing(registry).lookup(with(any(String.class)), with(any(Class.class)));
            will(returnValue(config));
            justIgnore(registry, config, tracer);

            allowing(mockContext).addInterceptStrategy((InterceptStrategy) with(anything()));
            allowing(mockContext).start();
            will(throwException(new CamelException()));
            checking(this);

            specify(new Block() {
                @Override public void run() throws Throwable { channel.start(); }
            }, should.raise(LifecycleException.class));
        }

        public void itShouldConfigureTheTracerBasedOnSuppliedProperties() throws Throwable {
            stubConfiguration(mockContext, registry, config);
            allowing(registry).lookup(with(any(String.class)), with(any(Class.class)));
            will(returnValue(config));
            justIgnore(registry, config, tracer);

            allowing(mockContext).addInterceptStrategy((InterceptStrategy) with(anything()));
            one(mockContext).start();
            checking(this);

            specify(new Block() {
                @Override public void run() throws Throwable { channel.start(); }
            }, must.not().raiseAnyException());
        }

    }
}
