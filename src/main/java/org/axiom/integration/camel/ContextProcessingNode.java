package org.axiom.integration.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.Processor;

public interface ContextProcessingNode extends Processor {
    void setContext(CamelContext context);
    CamelContext getContext();
}
