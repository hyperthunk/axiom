package org.axiom.systest;

import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.apache.camel.*;
import org.apache.camel.component.mock.MockEndpoint;
import org.axiom.integration.Environment;
import org.axiom.service.ControlChannel;
import org.axiom.service.Launcher;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@RunWith(JDaveRunner.class)
public class BootstrappedLaunchSpec extends Specification<ControlChannel> {

    private static final int TEN_SECOND_TIMEOUT = 10000;

    final ApplicationContext applicationContext =
        new ClassPathXmlApplicationContext("axiom-core-default-context.xml");

    CamelContext camelContext;

    public class WhenBootstrappingAnEmbeddedControlChannel {

        private Launcher launcher;
        private ControlChannel channel;

        public ControlChannel create() {
            camelContext = (CamelContext) applicationContext.getBean(
                Environment.HOST_CONTEXT, CamelContext.class);
            launcher = new Launcher();
            return channel = launcher.launch(camelContext);
        }

        /*private void anotherKindOfTest() throws Exception {
            RouteBuilder builder = new RouteBuilder() {
                @Override public void configure() throws Exception {
                    from("direct:axiomControlChannel").
                        to(Environment.TERMINATION_CHANNEL);
                }
            };
            camelContext.addRoutes(builder);
            camelContext.start();

            camelContext.createProducerTemplate().sendBody("direct:axiomControlChannel", "BODY");
            ShutdownChannel shutdown = camelContext.getRegistry().
                lookup(Environment.SHUTDOWN_CHANNEL_ID, ShutdownChannel.class);
            specify(shutdown.waitShutdown(1000), equal(true));
        }*/

        private void itShouldRedirectTerminationCallsToTheShutdownChannel() throws Exception {
            channel.sendShutdownSignal();
            specify(channel.waitShutdown(TEN_SECOND_TIMEOUT), should.equal(true));
        }

        public void itShouldInterceptScriptCodePassingMessageBodyThroughAnEvaluatorNode() throws InterruptedException {
            final String codeFragment =
                "route { from(\"direct:start\").filter(body().contains(\"stuff\")).to(\"mock:result\") }";

            // send the code configuration update
            final ProducerTemplate<Exchange> template = camelContext.createProducerTemplate();
            template.sendBodyAndHeader(Environment.CONTROL_CHANNEL,
                codeFragment, Environment.PAYLOAD_CLASSIFIER, "code");

            // set up expectations on our mock endpoint
            final MockEndpoint mockEndpoint =
                (MockEndpoint) camelContext.getEndpoint("mock:result");

            mockEndpoint.expectedMessageCount(1);

            // send the actual message
            camelContext.createProducerTemplate()
                .sendBody("direct:start", "<stuff/>");

            mockEndpoint.assertIsSatisfied();
        }
    }

}
