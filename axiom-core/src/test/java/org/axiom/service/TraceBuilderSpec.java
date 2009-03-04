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

        public void itShouldSetTheLogLevelToTheProvidedValue() {
            fakeLogLevelToError();
            ignoreFurtherCalls();

            specify(builder.build(),
                satisfies(propertyValueContract("logLevel", setTo(LoggingLevel.ERROR))));
        }

        public void itShouldSetInterceptorTracingToTheConfiguredValue() {
            fakeLogLevelToError();
            fakeInterceptorTraceOn();
            ignoreFurtherCalls();

            specify(builder.build(),
                satisfies(propertyValueContract("traceInterceptors", setTo(true))));
        }

        //traceExceptions
        public void itShouldSetExceptionTracingToTheConfiguredValue() {
            fakeLogLevelToError();
            fakeInterceptorTraceOn();
            allowing(config).getBoolean(TRACE_EXCEPTIONS_KEY);
            will(returnValue(true));
            ignoreFurtherCalls();

            specify(builder.build(),
                satisfies(propertyValueContract("traceExceptions", setTo(true))));
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

        private void ignoreFurtherCalls() {
            ignoring(config);
            checking(this);
        }

        private void fakeInterceptorTraceOn() {
            allowing(config).getBoolean(TRACE_INTERCEPTORS_KEY);
            will(returnValue(true));
        }

        private void fakeLogLevelToError() {
            allowing(config).getString(TRACE_LEVEL_KEY);
            will(returnValue("error"));
        }

    }

}
