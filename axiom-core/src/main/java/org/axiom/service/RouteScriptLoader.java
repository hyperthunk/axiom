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

import org.apache.camel.Route;
import org.apache.camel.builder.RouteBuilder;
import static org.apache.commons.io.FileUtils.*;
import static org.apache.commons.lang.StringUtils.*;
import static org.apache.commons.lang.Validate.*;
import org.axiom.integration.Environment;
import org.axiom.integration.camel.RouteConfigurationScriptEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * A {@link RouteLoader} that takes a script from the file system
 * (or a classpath resource) and evaluates it, generating a
 * list of {@link Route} instances.
 */
public class RouteScriptLoader implements RouteLoader {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final RouteConfigurationScriptEvaluator scriptEvaluator;
    private final String pathToScript;

    public RouteScriptLoader(final String pathToScript,
        final RouteConfigurationScriptEvaluator scriptEvaluator) {
        notEmpty(pathToScript, "Null or empty script path is not allowed.");
        notNull(scriptEvaluator, "Null evaluator is not allowed.");
        this.pathToScript = pathToScript;
        this.scriptEvaluator = scriptEvaluator;
    }

    /**
     * {@inheritDoc}
     */
    @Override public List<Route> load() {
        try {
            return getBuilder().getRouteList();
        } catch (LifecycleException lEx) {
            throw lEx;
        } catch (Exception e) {
            throw new LifecycleException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override public RouteBuilder getBuilder() {
        try {
            final String uri = getPathToScript();
            log.info("Loading route list from {}.", uri);
            final String bootstrapCode = readFileToString(new File(uri));
            log.debug("Applying {}:{}{}", new Object[] {uri, Environment.NEWLINE, bootstrapCode});
            return scriptEvaluator.configure(bootstrapCode);
        } catch (IOException e) {
            throw new LifecycleException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Gets the path to the script to be loaded by this.
     * @return
     */
    public String getPathToScript() {
        return normalizedScriptUri(pathToScript);
    }

    private String normalizedScriptUri(final String uri) {
        try {
            if (startsWithIgnoreCase(uri, "classpath:")) {
                Resource resource =
                    new ClassPathResource(substringAfter(uri, "classpath:"));
                log.debug("Resolved '{}' to '{}'.", uri, resource);
                return resource.getFile().getAbsolutePath();
            }
            return uri;
        } catch (IOException e) {
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }
}
