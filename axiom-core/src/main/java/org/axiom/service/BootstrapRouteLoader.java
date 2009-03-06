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
import org.apache.commons.configuration.Configuration;
import static org.apache.commons.io.FileUtils.*;
import static org.apache.commons.lang.StringUtils.*;
import static org.apache.commons.lang.Validate.*;
import org.axiom.integration.camel.RouteConfigurationScriptEvaluator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.util.List;

class BootstrapRouteLoader implements RouteLoader {

    protected static final String DEFAULT_SCRIPT_URI = "axiom.bootstrap.script.url";
    protected static final String DEFAULT_SCRIPT_PATH = "classpath:default-bootstrap.rb";

    private final Configuration configuration;
    private final RouteConfigurationScriptEvaluator scriptEvaluator;

    public BootstrapRouteLoader(final Configuration configuration,
            final RouteConfigurationScriptEvaluator scriptEvaluator) {
        notNull(configuration, "Null configuration is not allowed.");
        notNull(scriptEvaluator, "Null evaluator is not allowed.");
        this.scriptEvaluator = scriptEvaluator;
        this.configuration = configuration;
    }

    public List<Route> load() {
        try {
            final String bootstrapUri = normalizedScriptUri();
            final String bootstrapCode = readFileToString(new File(bootstrapUri));
            return scriptEvaluator.configure(bootstrapCode).getRouteList();
        } catch (Exception e) {
            throw new LifecycleException(e.getLocalizedMessage(), e);
        }
    }

    private String normalizedScriptUri() throws IOException {
        final String uri = configuration.getString(DEFAULT_SCRIPT_URI, DEFAULT_SCRIPT_PATH);
        if (startsWithIgnoreCase(uri, "classpath:")) {
            Resource resource =
                new ClassPathResource(substringAfter(uri, "classpath:"));
            return resource.getFile().getAbsolutePath();
        }
        return uri;
    }
}
