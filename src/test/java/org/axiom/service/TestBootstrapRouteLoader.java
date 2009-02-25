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

package org.axiom.service;

import org.junit.runner.RunWith;
import org.junit.Test;
import org.junit.Before;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.jmock.Mockery;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JMock;
import org.springframework.core.io.ClassPathResource;
import org.axiom.configuration.RouteConfigurationScriptEvaluator;

import java.io.IOException;

@RunWith(JMock.class)
public class TestBootstrapRouteLoader {

    private Mockery mockery = new Mockery();
    private Configuration config;
    private RouteConfigurationScriptEvaluator evaluator;

    @Before
    public void beforeEach() throws Exception {
        config = mockery.mock(Configuration.class);
        evaluator = mockery.mock(RouteConfigurationScriptEvaluator.class);
    }

    @Test(expected=IllegalArgumentException.class)
    public void itShouldPukeWhenInitializedWithMissingConfiguration() {
        new BootstrapRouteLoader(null, evaluator);
    }

    @Test
    public void itShouldPullThePathToTheControlChannelBootstrapScript() {
        mockery.checking(new Expectations() {{
            one(config).getString("axiom.bootstrap.script.url", "classpath:default-bootstrap.rb");
            will(returnValue("classpath:test-boot.rb"));
            allowing(evaluator);
        }});
        new BootstrapRouteLoader(config, evaluator).load();
    }

    @Test(expected=LifecycleException.class)
    public void itShouldPukeIfTheSuppliedFileDoesNotExitAtTheGivenUri() {
        mockery.checking(new Expectations() {{
            one(config).getString(with(any(String.class)), with(any(String.class)));
            will(returnValue("no-such-file.rb"));
            allowing(evaluator);
        }});
        new BootstrapRouteLoader(config, evaluator).load();
    }

    @Test
    public void itShouldPassTheLoadedScriptToTheScriptEvaluator() throws IOException {
        final String bootstrapCode = FileUtils.readFileToString(
            new ClassPathResource("test-boot.rb").getFile()
        );

        mockery.checking(new Expectations() {{
            allowing(config).getString(with(any(String.class)), with(any(String.class)));
            will(returnValue("classpath:test-boot.rb"));

            one(evaluator).configure(bootstrapCode);
        }});
        new BootstrapRouteLoader(config, evaluator).load();
    }
}
