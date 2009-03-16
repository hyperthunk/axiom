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
import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.apache.camel.processor.interceptor.Tracer;
import org.apache.commons.configuration.Configuration;
import static org.axiom.integration.Environment.*;
import org.axiom.integration.Environment;
import org.junit.runner.RunWith;

@SuppressWarnings({"ThrowableInstanceNeverThrown", "unchecked"})
@RunWith(JDaveRunner.class)
public class ControlChannelSpec extends Specification<ControlChannel> {

    private RouteLoader loader = mock(RouteLoader.class);
    private ControlChannel channel;
    private ShutdownChannel shutdownChannel;

    public class WhenInitializingNewInstances extends ServiceSpecSupport {
        
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

    public class WhenConsumingServicesViaTheChannel extends ServiceSpecSupport {

        public ControlChannel create() throws ClassNotFoundException {
            prepareMocks(mockery());
            stubRegistry();
            shutdownChannel = mock(mockery(), ShutdownChannel.class);
            stubLookup(Environment.SHUTDOWN_CHANNEL_ID, shutdownChannel);
            checking(this);
            return channel = new ControlChannel(mockContext, dummy(Tracer.class));
        }

        public void itShouldPerformLookupsOnBehalfOfTheConsumer() throws ClassNotFoundException {
            stubConfiguration(mockContext, mockRegistry, mockConfig);
            
            final Object baz = new Object();
            final String key = "foobar";
            stubLookup(key, baz);
            checking(this);

            specify(channel.lookup(key, baz.getClass()), same(baz));
        }

        public void itShouldLocateTheConfigurationInstanceAndCacheItForFutureUse() throws Throwable {
            one(mockRegistry).lookup(CONFIG_BEAN, Configuration.class);
            will(returnValue(mockConfig));
            checking(this);

            final Block lookup = new Block() {
                @Override public void run() throws Throwable { channel.getConfig(); }
            };

            specify(repeat(lookup, times(2)), should.not().raiseAnyException());
        }

        public void itShouldConfigureViaProducerTemplateUsingConfiguredEndpoint() throws ClassNotFoundException {
            final String channelUri = "direct:control-channel";
            final DefaultProducerTemplate mockTemplate =
                mock(mockery(), DefaultProducerTemplate.class);
            final RouteBuilder builder = dummy(RouteBuilder.class);

            stubConfiguration(mockContext, mockRegistry, mockConfig);
            allowing(mockContext).createProducerTemplate();
            will(returnValue(mockTemplate));

            one(mockTemplate).sendBodyAndHeader(CONTROL_CHANNEL,
                builder, SIGNAL, SIG_CONFIGURE);
            checking(this);

            channel.configure(builder);
        }

        public void itShouldPullTheRouteBuilderInsteadOfLoadingRoutes() {
            stubConfiguration(mockContext, mockRegistry, mockConfig);
            allowing(mockContext).createProducerTemplate();
            will(returnValue(dummy(ProducerTemplate.class)));

            final RouteBuilder dummyBuilder = dummy(RouteBuilder.class);
            one(loader).load();
            will(returnValue(dummyBuilder));
            checking(this);
            
            channel.configure(loader);
        }

        public void itShouldSetWaitTimeoutIfOneIsSupplied() throws Exception {
            final long timeout = 1000;
            prepForWait();
            one(shutdownChannel).waitShutdown(timeout);
            will(returnValue(true));

            one(mockContext).stop();
            checking(this);

            specify(channel.waitShutdown(timeout), should.equal(true));
        }

        public void itShouldReturnFalseIfShutdownDidNotOccurInTimelyManner() throws Exception {
            final long timeout = 10;
            prepForWait();
            allowing(shutdownChannel).waitShutdown(timeout);
            will(returnValue(false));

            never(mockContext).stop();
            checking(this);

            specify(channel.waitShutdown(timeout), should.equal(false));
        }

        public void itShouldSendSigTermToTheControlChannelViaProducerTemplate() {
            final ProducerTemplate mockProducer =
                mock(mockery(), ProducerTemplate.class);

            stubConfiguration(mockContext, mockRegistry, mockConfig);
            allowing(mockContext).createProducerTemplate();
            will(returnValue(mockProducer));

            one(mockProducer).sendBodyAndHeader(
                TERMINATION_CHANNEL, null, SIGNAL, SIG_TERMINATE);
            checking(this);

            channel.sendShutdownSignal();
        }

        public void itShouldProvideWaitHookForGracefulTermination() throws Exception {
            prepForWait();
            final ProducerTemplate mockProducer =
                mock(mockery(), ProducerTemplate.class);

            allowing(mockContext).createProducerTemplate();
            will(returnValue(mockProducer));
            justIgnore(mockProducer);
            one(shutdownChannel).waitShutdown();

            allowing(mockContext).stop();
            checking(this);

            channel.sendShutdownSignalAndWait();
        }

        public void itShouldProvideWaitHookWithTimeoutForGracefulTermination() throws Exception {
            prepForWait();
            final ProducerTemplate mockProducer =
                mock(mockery(), ProducerTemplate.class);
            final long timeout = 1000;

            allowing(mockContext).createProducerTemplate();
            will(returnValue(mockProducer));
            justIgnore(mockProducer);

            one(shutdownChannel).waitShutdown(timeout);
            will(returnValue(true));
            
            allowing(mockContext).stop();
            checking(this);

            channel.sendShutdownSignalAndWait(timeout);
        }

        private void prepForWait() throws Exception {
            stubConfiguration(mockContext, mockRegistry, mockConfig);
            stubLookup(SHUTDOWN_CHANNEL_ID, shutdownChannel);
        }

    }

    public class WhenLoadingRoutesAndAddingThenToTheChannel extends ServiceSpecSupport {

        public ControlChannel create() throws ClassNotFoundException {
            prepareMocks(mockery());
            stubRegistry();
            shutdownChannel = mock(mockery(), ShutdownChannel.class);
            stubLookup(Environment.SHUTDOWN_CHANNEL_ID, shutdownChannel);
            checking(this);
            return channel =
                new ControlChannel(mockContext, dummy(Tracer.class, "dummy-trace"));
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

        public void itShouldWrapCheckedExceptionsWithRuntime() throws Exception {
            allowing(loader);
            allowing(mockContext).getName();
            one(mockContext).addRoutes((RouteBuilder) with(anything()));
            will(throwException(new Exception()));
            checking(this);

            specify(new Block() {
                @Override public void run() throws Throwable {
                    new ControlChannel(mockContext).load(loader);
                }
            }, should.raise(LifecycleException.class));
        }

    }

    public class WhenConfiguringTheChannel extends ServiceSpecSupport {

        public ControlChannel create() throws ClassNotFoundException {
            prepareMocks(mockery());
            stubRegistry();
            shutdownChannel = mock(mockery(), ShutdownChannel.class);
            stubLookup(Environment.SHUTDOWN_CHANNEL_ID, shutdownChannel);
            checking(this);
            return channel = new ControlChannel(mockContext, mockTracer);
        }

        public void itShouldAttemptObtainingTracerInstanceFromTheContextInitially() {
            DefaultCamelContext context = new DefaultCamelContext();
            context.addInterceptStrategy(mockTracer);
            ControlChannel channel = new ControlChannel(context);

            specify(channel.getTracer(), same(mockTracer));
        }

        public void itShouldCreateNewTracerInstanceIfNoneIsPresent() {
            final ControlChannel channel = new ControlChannel(new DefaultCamelContext());
            specify(channel.getTracer(), isNotNull());
        }

        @SuppressWarnings({"unchecked"})
        public void itShouldConfigureTheTracerBasedOnSuppliedProperties() {
            stubConfiguration(mockContext, mockRegistry, mockConfig);
            stubConfig(TraceBuilder.TRACE_ENABLED, false);
            ignoring(mockTracer).setEnabled(with(any(Boolean.class)));

            one(mockTracer).isEnabled();
            will(returnValue(false));

            justIgnore(mockTracer, mockContext);
            checking(this);

            channel.activate();
        }
        
    }

    public class WhenStartingTheChannel extends ServiceSpecSupport {

        public ControlChannel create() throws ClassNotFoundException {
            prepareMocks(mockery());
            stubRegistry();
            shutdownChannel = mock(mockery(), ShutdownChannel.class);
            stubLookup(Environment.SHUTDOWN_CHANNEL_ID, shutdownChannel);
            checking(this);
            return channel = new ControlChannel(mockContext, mockTracer);
        }

        public void itShouldWrapAnyRegistryLookupExceptions() {
            allowing(mockContext).getRegistry();
            will(returnValue(mockRegistry));
            allowing(mockRegistry).lookup(with(any(String.class)), with(any(Class.class)));
            will(throwException(new RuntimeException()));
            checking(this);
            
            specify(new Block() {
                @Override public void run() throws Throwable { channel.activate(); }
            }, should.raise(LifecycleException.class));
        }

        public void itShouldWrapAnyStartupExceptions() throws Exception {
            stubReadyToRunContext(mockery());
            allowing(mockContext).start();
            allowing(mockContext).getName();
            will(throwException(new CamelException()));
            checking(this);

            specify(new Block() {
                @Override public void run() throws Throwable { channel.activate(); }
            }, should.raise(LifecycleException.class));
        }

        public void itShouldStartTheCamelContext() throws Throwable {
            stubReadyToRunContext(mockery());
            one(mockContext).start();
            allowing(mockContext).getName();
            checking(this);

            specify(new Block() {
                @Override public void run() throws Throwable { channel.activate(); }
            }, must.not().raiseAnyException());
        }

    }

    public class WhenStoppingTheCamelContext extends ServiceSpecSupport {

        public ControlChannel create() throws ClassNotFoundException {
            prepareMocks(mockery());
            stubRegistry();
            shutdownChannel = mock(mockery(), ShutdownChannel.class);
            stubLookup(Environment.SHUTDOWN_CHANNEL_ID, shutdownChannel);
            checking(this);
            return channel = new ControlChannel(mockContext, mockTracer);
        }

        public void itShouldStopTheUnderlyingContext() throws Exception {
            one(mockContext).stop();
            checking(this);

            channel.destroy();
        }

        public void itShouldWrapAnyUnderlyingExceptions() throws Exception {
            one(mockContext).stop();
            will(throwException(new AlreadyStoppedException()));
            checking(this);

            specify(new Block() {
                @Override public void run() throws Throwable {
                    channel.destroy();
                }
            }, should.raise(LifecycleException.class));
        }
        
    }
}
