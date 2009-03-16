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

package org.axiom.systest;

import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import static org.apache.camel.builder.xml.XPathBuilder.*;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.axiom.integration.Environment;

@RunWith(JDaveRunner.class)
public class RulebaseRefreshIntegrationSpec extends Specification<CamelContext> {

    ApplicationContext applicationContext;
    CamelContext camelContext;
    private final Processor doNothingProcessor = new Processor() {
        @Override public void process(Exchange exchange) throws Exception {
            // deliberately ignoring exchange...
        }
    };

    public class WhenRefreshingTheCamelContextRuleBase {

        public CamelContext create() {
            applicationContext =
                new ClassPathXmlApplicationContext("axiom-core-default-context.xml");
            return camelContext = (CamelContext) applicationContext.getBean(Environment.HOST_CONTEXT); 
                //new SpringCamelContext(applicationContext);
        }

        //@Ignore //TODO: move this into an integration testing module and re-enable
        @Test
        public void addingAdditionalEndpointOnDefinedRoute() throws Exception {
            //TODO: refactor this to use our components later on

            RouteBuilder builder = new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from("direct:start").
                        filter(body().contains("stuff")).
                    to("mock:result");

                    from("direct:foobar").intercept(xpath("/foo/bar[count(child::*) > 0]")).
                        tryBlock().filter().xpath("*");
                }
            };
            camelContext.addRoutes(builder);
            camelContext.start();

            try {
                MockEndpoint resultEndpoint = (MockEndpoint) camelContext.getEndpoint("mock:result");
                resultEndpoint.expectedMessageCount(2);

                ProducerTemplate template = camelContext.createProducerTemplate();
                template.sendBody("direct:start", "<stuff/>");

                camelContext.addRoutes(
                    new RouteBuilder() {
                        @Override
                        public void configure() throws Exception {
                            from("direct:start").
                            filter(body().contains("other")).
                            to("mock:result");
                        }
                    }
                );

                template.sendBody("direct:start", "other");

                resultEndpoint.assertIsSatisfied();
            } finally {
                camelContext.stop();
            }
        }

        @Test
        public void shouldExchangesBeCopiedByProcessorsOrNot() throws Exception {
            final String body = "<body />";
            final String foo = "foo";
            final String bar = "bar";
            final String mockOutUri = "mock:output";

            RouteBuilder builder = new RouteBuilder() {
                @Override public void configure() throws Exception {
                    from("direct:start").
                        process(doNothingProcessor).to(mockOutUri);
                }
            };
            camelContext.addRoutes(builder.getRouteList());
            camelContext.start();

            try {
                MockEndpoint resultEndpoint = (MockEndpoint) camelContext.getEndpoint(mockOutUri);
                resultEndpoint.expectedMessageCount(1);
                resultEndpoint.expectedHeaderReceived("foo", "bar");
                resultEndpoint.expectedBodiesReceived(body);

                ProducerTemplate template = camelContext.createProducerTemplate();
                template.sendBodyAndHeader(mockOutUri, body, foo, bar);
                resultEndpoint.assertIsSatisfied();
            } finally {
                camelContext.stop();
            }
        }

    }
}
