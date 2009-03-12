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

package org.axiom.integration.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultComponent;
import org.apache.camel.impl.ProcessorEndpoint;
import org.apache.commons.configuration.Configuration;
import static org.apache.commons.lang.StringUtils.equalsIgnoreCase;
import org.axiom.integration.Environment;

import java.util.Map;

public class AxiomComponent extends DefaultComponent<Exchange> {

    protected static final String NO_REGISTERED_CONTEXT =
        "No org.apache.camel.CamelContext registered under the name %s";

    private Configuration config;

    @Override
    protected ProcessorEndpoint createEndpoint(String uri, String remaining, Map parameters) throws Exception {
        final CamelContext targetContext;
        if (equalsIgnoreCase(uri, Environment.AXIOM_HOST_URI)) {
            targetContext = getCamelContext();
        } else {
            targetContext = lookup(remaining, CamelContext.class);
            if (targetContext == null) {
                throw new IllegalArgumentException(
                    String.format(NO_REGISTERED_CONTEXT, targetContext));
            }
        }
        final ContextProcessingNode processor = lookupProcessingNode();
        processor.setContext(targetContext);
        return new ProcessorEndpoint(uri, this, processor);
    }

    private ContextProcessingNode lookupProcessingNode() {
        return lookup(config.getString(Environment.DEFAULT_PROCESSOR), ContextProcessingNode.class);
    }

    public Configuration getConfiguration() {
        return config;
    }

    public void setConfiguration(Configuration config) {
        this.config = config;
    }
}