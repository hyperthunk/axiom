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

package org.axiom.management;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import static org.apache.commons.lang.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang.Validate.notNull;

public class ControlChannelProcessor implements Processor {

    private final Object lockObj;
    private final RouteConfigurationScriptEvaluator configEvaluator;
    private CamelContext context;

    public ControlChannelProcessor(CamelContext context,
            RouteConfigurationScriptEvaluator configEvaluator) {
        notNull(context, "cannot initialize with null context");
        notNull(configEvaluator, "cannot initialize with null evaluator");
        lockObj = new Object();
        this.context = context;
        this.configEvaluator = configEvaluator;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        final Message inChannel = exchange.getIn();
        final String command = inChannel.getHeader("command", String.class);
        if (equalsIgnoreCase(command, "stop")) {
            stop();
        } else if (equalsIgnoreCase(command, "start")) {
            start();
        } else if (equalsIgnoreCase(command, "configure")) {
            configureRoutes(inChannel.getBody(RouteBuilder.class));
        }
    }

    private void configureRoutes(RouteBuilder builder) throws Exception {
        synchronized (lockObj) {
            context.addRoutes(builder.getRouteList());
        }
    }

    private void start() throws Exception {
        synchronized (lockObj) {
            context.start();
        }
    }

    private void stop() throws Exception {
        synchronized (lockObj) {
            context.stop();
        }
    }
}
