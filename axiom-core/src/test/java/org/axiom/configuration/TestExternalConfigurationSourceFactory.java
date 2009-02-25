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

package org.axiom.configuration;

import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Test;
import static org.junit.Assert.assertThat;
import org.apache.commons.configuration.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import static org.jmock.Expectations.equal;

import java.io.File;
import java.util.ArrayList;

@RunWith(JUnit4.class)
public class TestExternalConfigurationSourceFactory {

    @Test
    public void cascadingConfigurationSetupPrefersSystemConfig() throws ConfigurationException {
        final String key = "axiom.control.channel.in";
        System.setProperty(key, "direct:control-channel");

        final ExternalConfigurationSourceFactory factory =
            new ExternalConfigurationSourceFactory("axiom.test.properties");
        //axiom.test.properties contains axiom.control.channel.in=direct:start

        final Configuration config = factory.createConfiguration();
        final String expectedValue = "direct:control-channel";
        assertThat(String.valueOf(config.getProperty(key)),
            equal(expectedValue));
    }

    @Test
    public void cascadingConfigurationAddsExternalPropertiesInCascadingOrder() throws ConfigurationException {
        final String sysKey = "axiom.configuration.externals";
        System.setProperty(sysKey,
                StringUtils.join(new Object[]{
                        "yet.another.axiom.properties",
                        "another.axiom.properties"
                }, File.pathSeparator));

        final ExternalConfigurationSourceFactory factory =
            new ExternalConfigurationSourceFactory("axiom.test.properties");
        
        final Configuration config = factory.createConfiguration();
        final String expectedValue = "yet-another-foo";
        assertThat(config.getString("axiom.test.foo"), equal(expectedValue));
    }

    @Test(expected = RuntimeException.class)
    public void configurationExceptionsResultInFatalRuntimeErrors() {
        new ExternalConfigurationSourceFactory("no-such-file-name").createConfiguration();
    }
}
