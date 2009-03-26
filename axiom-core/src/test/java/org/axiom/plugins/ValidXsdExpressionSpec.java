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

package org.axiom.plugins;

import jdave.Block;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.apache.camel.Exchange;
import org.apache.camel.InvalidPayloadRuntimeException;
import org.apache.camel.impl.DefaultMessage;
import org.axiom.SpecSupport;
import org.hamcrest.Description;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.junit.runner.RunWith;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

@RunWith(JDaveRunner.class)
public class ValidXsdExpressionSpec extends Specification<ValidXsdExpression> {

    public class WhenInitializingNewInstances extends SpecSupport {
        public void itShouldPukeIfTheSuppliedXsdIsInvalid() {
            final Schema missingSchema = null;
            specify(new Block() {
                @Override public void run() throws Throwable {
                    new ValidXsdExpression(missingSchema);
                }
            }, should.raise(IllegalArgumentException.class));
        }
    }

    public class WhenValidatingAnExchangeInputChannelBody extends SpecSupport {

        private ErrorHandler errorHandlerCapture;
        private ValidXsdExpression expression;
        private final Exchange exchange = mock(Exchange.class, "Exchange-mock");
        private final Schema schema = mock(Schema.class);
        private final Validator validator = mock(Validator.class, "Validator-mock");

        public ValidXsdExpression create() throws SAXException {
            return expression = new ValidXsdExpression(schema);
        }

        public void itShouldPukeIfTheInputChannelHasNoBody() {
            stubInputChannel(new DefaultMessage());
            
            specify(new Block() {
                @Override public void run() throws Throwable {
                    expression.evaluate(exchange);
                }
            }, should.raise(InvalidPayloadRuntimeException.class));
        }

        public void itShouldObtainValidatorFromTheSchema() {
            final DefaultMessage message = new DefaultMessage();
            message.setBody("<body />");
            stubInputChannel(message);
            stubValidator(dummy(Validator.class));

            expression.evaluate(dummy(Exchange.class));
        }

        public void itShouldExplicitlySetAnErrorHandler() {
            final DefaultMessage message = new DefaultMessage();
            message.setBody("<body />");
            stubInputChannel(message);
            stubValidator(validator);

            one(validator).setErrorHandler(with(any(ErrorHandler.class)));
            checking(this);

            expression.evaluate(dummy(Exchange.class, "ignored-exchange"));
        }

        private void stubInputChannel(final DefaultMessage message) {
            allowing(exchange).getIn();
            will(returnValue(message));
            checking(this);
        }

        private void stubValidator() {
            stubValidator(validator);
        }

        private void stubValidator(final Validator validator) {
            one(schema).newValidator();
            will(returnValue(validator));
            checking(this);
        }

        private Action storeTheErrorHandlerForTesting() {
            return new Action() {
                @Override public Object invoke(final Invocation invocation) throws Throwable {
                    Object input = invocation.getParameter(0);
                    errorHandlerCapture = (ErrorHandler) input;
                    return null;
                }
                @Override public void describeTo(final Description description) {}
            };
        }

    }

}
