package org.axiom.mediation.routing;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/server-context.xml"})
public class TestRulebaseRefreshIntegration implements ApplicationContextAware {

    ApplicationContext applicationContext;
    @Autowired
    CamelContext camelContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Before
    public void setUp() throws Exception {
        assertNotNull(camelContext);
    }

    @Test
    public void addingAdditionalEndpointOnDefinedRoute() throws Exception {
        //TODO: refactor this to use our components later on

        RouteBuilder builder = new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start").
                        filter(body().contains("stuff")).
                        to("mock:result");
            }
        };
        camelContext.addRoutes(builder.getRouteList());
        camelContext.start();

        try {
            MockEndpoint resultEndpoint = (MockEndpoint) camelContext.getEndpoint("mock:result");
            resultEndpoint.expectedMessageCount(2);

            ProducerTemplate template = camelContext.createProducerTemplate();
            template.sendBody("mock:result", "<stuff/>");

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

            template.sendBody("mock:result", "other");

            resultEndpoint.assertIsSatisfied();
        } finally {
            camelContext.stop();
        }
    }

}
