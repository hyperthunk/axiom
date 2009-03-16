package org.axiom.systest;

import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.apache.camel.CamelContext;
import org.axiom.integration.Environment;
import org.axiom.service.ControlChannel;
import org.axiom.service.Launcher;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@RunWith(JDaveRunner.class)
public class BootstrappedLaunchSpec extends Specification<Launcher> {

    private static final int TEN_SECOND_TIMEOUT = 10000;

    final ApplicationContext applicationContext =
        new ClassPathXmlApplicationContext("axiom-core-default-context.xml");

    CamelContext camelContext;

    public class WhenBootstrappingAnEmbeddedControlChannel {

        private Launcher launcher;

        public Launcher create() {
            camelContext = (CamelContext) applicationContext.getBean(
                Environment.HOST_CONTEXT, CamelContext.class);
            return launcher = new Launcher();
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

        public void itShouldRedirectTerminationCallsToTheShutdownChannel() throws Exception {
            ControlChannel channel = launcher.launch(camelContext);

            channel.sendShutdownSignal();
            specify(channel.waitShutdown(TEN_SECOND_TIMEOUT), should.equal(true));
        }

        public void itShouldInterceptScriptCodePassingMessageBodyThroughAnEvaluatorNode() {
            ControlChannel channel = launcher.launch(camelContext);

            
        }
    }

}
