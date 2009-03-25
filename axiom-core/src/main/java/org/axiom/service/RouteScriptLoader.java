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
import org.apache.commons.io.IOUtils;
import static org.apache.commons.lang.StringUtils.*;
import static org.apache.commons.lang.Validate.*;
import org.axiom.integration.Environment;
import org.axiom.integration.camel.RouteConfigurationScriptEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.*;

import java.io.IOException;

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
    @Override public RouteBuilder load() {
        try {
            final Resource script = getScript();
            final String bootstrapCode = IOUtils.toString(script.getInputStream());
            log.debug("Applying {}:{}{}",
                new Object[] {
                    script.getURI(),
                    Environment.NEWLINE,
                    bootstrapCode
                }
            );
            return scriptEvaluator.configure(bootstrapCode);
        } catch (IOException e) {
            throw new LifecycleException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Gets the path to the script to be loaded by this.
     * @return
     */
    public Resource getScript() {
        return getScriptResource(pathToScript);
    }

    private Resource getScriptResource(final String uri) {
        try {
            if (startsWithIgnoreCase(uri, "classpath:")) {
                final String classPathUri = substringAfter(uri, "classpath:");
                log.info("Loading route list from classpath uri {}.", classPathUri);
                Resource resource =
                    new ClassPathResource(classPathUri);
                log.debug("Resolved '{}' to '{}'.", classPathUri, resource);
                return resource;
            }
            log.info("Loading route list from file system path {}.", uri);
            return new FileSystemResource(uri);
        } catch (Exception e) {
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }
}
