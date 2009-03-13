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
import org.apache.camel.processor.LoggingLevel;
import org.apache.camel.processor.interceptor.Tracer;
import org.apache.commons.configuration.Configuration;
import org.axiom.SpecSupport;
import static org.axiom.service.TraceBuilder.*;
import org.junit.runner.RunWith;

@RunWith(JDaveRunner.class)
public class TraceBuilderSpec extends Specification<TraceBuilder> {

    public class WhenConfiguringTheTraceBuilder extends SpecSupport {

        private Configuration config;
        private Tracer tracer;
        private TraceBuilder builder;

        public TraceBuilder create() {
            config = mock(Configuration.class);
            tracer = new Tracer();
            return builder = new TraceBuilder(config, tracer);
        }

        public void itShouldOnlySetupTracingWhenItIsEnabled() {
            Tracer trace = mock(Tracer.class);
            allowing(config).getBoolean(TRACE_ENABLED);
            will(returnValue(false));
            fakeLogLevelToError();
            one(trace).setEnabled(false);
            allowing(trace).isEnabled();will(returnValue(false));
            never(trace);
            ignoreFurtherCalls();

            new TraceBuilder(config, trace).build();
        }

        public void itShouldSetTheLogNameToTheProvidedValue() {
            final String logname = stubTracerConfig();
            ignoreFurtherCalls();

            specify(builder.build(),
                satisfies(propertyValueContract("logName", is(logname))));
        }

        public void itShouldNotSetTheLogNameIfItIsUndefined() {
            Tracer trace = mock(Tracer.class);
            enableTrace();
            fakeLogLevelToError();
            fakeLogName(null);
            never(trace).setLogName(with(any(String.class)));
            allowing(trace);
            ignoreFurtherCalls();

            new TraceBuilder(config, trace).build();
        }

        public void itShouldSetTheLogLevelToTheProvidedValue() {
            enableTrace();
            fakeLogLevelToError();
            ignoreFurtherCalls();

            specify(builder.build(),
                satisfies(propertyValueContract("logLevel", is(LoggingLevel.ERROR))));
        }

        public void itShouldSetInterceptorTracingToTheConfiguredValue() {
            enableTrace();
            fakeLogLevelToError();
            fakeInterceptorTraceOn();
            ignoreFurtherCalls();

            specify(builder.build(),
                satisfies(propertyValueContract("traceInterceptors", is(true))));
        }

        public void itShouldConfigureTheFormatterToShowBreadcrumbs() {
            stubTracerConfig();
            formatShowBreadCrumbs();
            ignoreFurtherCalls();

            final boolean showBreadCrumbs = builder.build().getFormatter().isShowBreadCrumb();
            specify(showBreadCrumbs, should.equal(true));
        }

        public void itShouldConfigureTheFormatterToShowExchangeProperties() {
            stubTracerConfig();
            formatShowBreadCrumbs();
            allowing(config).getBoolean(TraceBuilder.TRACE_SHOW_EXCHANGE_PROPS);
            will(returnValue(false));
            ignoreFurtherCalls();

            final boolean showProperties = builder.build().getFormatter().isShowProperties();
            specify(showProperties, should.equal(false));
        }

        public void itShouldConfigureTheFormatterToShowExchangeHeaders() {
            stubTracerConfig();
            formatShowBreadCrumbs();
            allowing(config).getBoolean(TraceBuilder.TRACE_SHOW_EXCHANGE_PROPS);
            will(returnValue(false));
            ignoreFurtherCalls();

            final boolean showHeaders = builder.build().getFormatter().isShowHeaders();
            specify(showHeaders, should.equal(false));
        }

        public void itShouldConfigureTheFormatterToShowBodyType() {
            stubTracerConfig();
            formatShowBreadCrumbs();
            allowing(config).getBoolean(TraceBuilder.TRACE_SHOW_EXCHANGE_BODY_TYPE);
            will(returnValue(false));
            ignoreFurtherCalls();

            final boolean showBodyType = builder.build().getFormatter().isShowBodyType();
            specify(showBodyType, should.equal(false));
        }

        public void itShouldConfigureTheFormatterToShowBodyContent() {
            stubTracerConfig();
            formatShowBreadCrumbs();
            allowing(config).getBoolean(TraceBuilder.TRACE_SHOW_EXCHANGE_BODY);
            will(returnValue(false));
            ignoreFurtherCalls();

            final boolean showBody = builder.build().getFormatter().isShowBody();
            specify(showBody, should.equal(false));
        }

        public void itShouldSetExceptionTracingToTheConfiguredValue() {
            enableTrace();
            fakeLogLevelToError();
            fakeInterceptorTraceOn();
            allowing(config).getBoolean(TRACE_EXCEPTIONS);
            will(returnValue(true));
            ignoreFurtherCalls();

            specify(builder.build(),
                satisfies(propertyValueContract("traceExceptions", is(true))));
        }

        public void itShouldPukeIfTheConfigurationInstanceIsMissing() {
            specify(new Block() {
                @Override public void run() throws Throwable {
                    new TraceBuilder(null, tracer);
                }
            },
            should.raise(IllegalArgumentException.class, MISSING_CONFIG_MSG));
        }

        public void itShouldPukeIfTheTracerInstanceIsMissing() {
            specify(new Block() {
                @Override public void run() throws Throwable {
                    new TraceBuilder(config, null);
                }
            },
            should.raise(IllegalArgumentException.class, MISSING_TRACER_MSG));
        }

        private void formatShowBreadCrumbs() {
            allowing(config).getBoolean(TRACE_SHOW_BREADCRUMBS);
            will(returnValue(true));
        }

        private String stubTracerConfig() {
            enableTrace();
            fakeLogLevelToError();
            final String logname = "foobar";
            fakeLogName(logname);
            return logname;
        }

        private void fakeLogName(final String logname) {
            allowing(config).getString(TRACE_NAME, null);
            will(returnValue(logname));
        }

        private void enableTrace() {
            allowing(config).getBoolean(TRACE_ENABLED);
            will(returnValue(true));
            //tracer.setEnabled(true);
        }

        private void ignoreFurtherCalls() {
            ignoring(config);
            checking(this);
        }

        private void fakeInterceptorTraceOn() {
            allowing(config).getBoolean(TRACE_INTERCEPTORS);
            will(returnValue(true));
        }

        private void fakeLogLevelToError() {
            allowing(config).getString(TRACE_LEVEL);
            will(returnValue("error"));
        }

    }

}
