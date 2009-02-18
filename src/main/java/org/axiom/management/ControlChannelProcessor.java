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
