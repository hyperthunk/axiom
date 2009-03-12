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

import org.apache.camel.spi.Registry;
import org.apache.commons.configuration.Configuration;
import static org.apache.commons.lang.Validate.*;
import org.axiom.integration.camel.RouteConfigurationScriptEvaluator;
import static org.axiom.integration.camel.RouteConfigurationScriptEvaluator.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * Provides a means of bootstrapping a {@link ControlChannel} with its
 * own set of routes and (optionally) bootstrapping a set of routes for
 * each of its managed (target) components also.
 */
public class ControlChannelBootstrapper {

    /**
     * The property key against which the uri/path of the
     * (default) bootstrap route script is set.
     */
    public static final String DEFAULT_SCRIPT_URI = "axiom.bootstrap.script.url";

    /**
     * The property key prefix with which custom (user defined) bootstrap extension
     * scripts can be defined. Setting a system (or file based) property such as
     * {@code axiom.bootstrap.extended.script.uri=/path/to/extension.rb} will result
     * in the script at this location being evaluated and its routes added to the
     * control channel. The final part/name of the property key is arbitrary and can
     * be set to anything you like (allowing for multiple extensions to be registered
     * concurrently).
     */
    public static final String EXTENDED_SCRIPTS_PREFIX = "axiom.bootstrap.extended.script";

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Bootstraps the supplied {@link ControlChannel}. The value of the property {@code axiom.bootstrap.script.url} is first retrieved from the configuration store, and the script at this location is evaluated directly.
     * If one of the {@code axiom.bootstrap.extended.script.url} or user defined {@code axiom.bootstrap.extended.script.&lt;name&gt;} properties is set,
     * then these scripts are evaluated next (the user defined ones only if they represent valid file system resources).
     *
     * @param channel The {@link ControlChannel} to bootstrap.
     */
    public void bootstrap(final ControlChannel channel) {
        log.info("Bootstrapping control channel.");
        notNull(channel, "Control channel cannot be null.");
        final Registry registry = channel.getContext().getRegistry();
        final Configuration config = channel.getConfig();

        RouteConfigurationScriptEvaluator evaluator =
            registry.lookup(config.getString(PROVIDER_BEAN_ID),
                RouteConfigurationScriptEvaluator.class);

        RouteScriptLoader loader =
            new RouteScriptLoader(config.getString(DEFAULT_SCRIPT_URI), evaluator);
        channel.load(loader);

        Iterator iter = config.getKeys(EXTENDED_SCRIPTS_PREFIX);
        while (iter.hasNext())  {
            final String entry = iter.next().toString();
            channel.load(new RouteScriptLoader(config.getString(entry), evaluator));
        }
    }

}
