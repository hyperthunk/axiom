package org.axiom.integration.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.Processor;

/**
 * Describes a {@link org.apache.camel.Processor} that contains
 * a {@link org.apache.camel.CamelContext} which is the target
 * of incoming processing instructions.
 *
 * 
 */
public interface ContextProcessingNode extends Processor {
    
    /**
     * Sets the {@link org.apache.camel.CamelContext} to
     * which processing instructions will be applied.
     * @param context
     */
    void setContext(CamelContext context);

    /**
     * Gets the {@link org.apache.camel.CamelContext} to
     * which processing instructions are being applied.
     * @return
     */
    CamelContext getContext();
}
