package org.axiom.systest;

import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.spring.SpringCamelContext;
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

    final CamelContext camelContext = new SpringCamelContext(applicationContext);

    public class WhenBootstrappingAnEmbeddedControlChannel {
        private Launcher launcher;

        public Launcher create() {
            return launcher = new Launcher();
        }

        /*public*/ private void itShouldRedirectTerminationCallsToTheShutdownChannel() throws Exception {
            ControlChannel channel = launcher.launch(camelContext);

            channel.terminate();

            Endpoint shutdownEp =
                channel.getContext().getEndpoint(Environment.TERMINATION_CHANNEL);
            shutdownEp.createPollingConsumer().receive(TEN_SECOND_TIMEOUT);
        }
    }

}
