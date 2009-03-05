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

package org.axiom.configuration;

import jdave.Block;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import static org.apache.commons.lang.StringUtils.*;
import org.axiom.SpecSupport;
import org.junit.runner.RunWith;

import java.io.File;

@RunWith(JDaveRunner.class)
public class ExternalConfigurationSourceFactorySpec
    extends Specification<ExternalConfigurationSourceFactory> {

    public class WhenComposingMultipleConfigurationSources extends SpecSupport {

        private ExternalConfigurationSourceFactory factory;

        public ExternalConfigurationSourceFactory create() {
            return factory =
                new ExternalConfigurationSourceFactory("axiom.test.properties");
        }

        public void itShouldPreferSystemConfigOverProperties() throws ConfigurationException {
            final String expectedValue = "direct:control-channel";
            final String key = "axiom.control.channel.in";
            System.setProperty(key, expectedValue);
            //axiom.test.properties contains axiom.control.channel.in=direct:start

            final Configuration config = factory.createConfiguration();
            specify(config.getProperty(key), should.equal(expectedValue));
        }

        public void itShouldAddExternalPropertiesInCascadingOrder() throws ConfigurationException {
            final String sysKey = ExternalConfigurationSourceFactory.AXIOM_CONFIGURATION_EXTERNALS;
            System.setProperty(sysKey,
                join(new Object[] {
                    "yet.another.axiom.properties",
                    "another.axiom.properties"
                }, File.pathSeparator));

            final Configuration config = factory.createConfiguration();
            final String expectedValue = "yet-another-foo";
            specify(config.getString("axiom.test.foo"), equal(expectedValue));
        }

        public void itShouldPreferUserDefinedPropertiesToDefaultOnes() {
            final String sysKey = ExternalConfigurationSourceFactory.AXIOM_CONFIGURATION_EXTERNALS;
            System.setProperty(sysKey, "another.axiom.properties");

            //axiom.test.properties contains axiom.test.overridden=foobarbaz
            //another.axiom.properties contains axiom.test.overridden=overriden
            final Configuration config = factory.createConfiguration();
            final String expectedValue = "overriden";
            specify(config.getString("axiom.test.overridden"), equal(expectedValue));
        }

        public void itShouldWrapConfigurationExceptionsInFatalRuntimeErrors() throws Exception{
            specify(new Block() {
                @Override public void run() throws Throwable {
                    new ExternalConfigurationSourceFactory("no-such-file-name").createConfiguration();
                }
            }, should.raise(RuntimeException.class));
        }

    }

    /*@Test
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
    }*/
}
