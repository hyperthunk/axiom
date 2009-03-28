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
import org.apache.camel.*;
import org.apache.camel.impl.DefaultMessage;
import org.axiom.SpecSupport;
import org.axiom.integration.Environment;
import org.hamcrest.Description;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.junit.runner.RunWith;
import org.xml.sax.*;

import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.util.List;

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
        private final Exchange exchange = mock(Exchange.class, "mock-exchange");
        private final Schema schema = mock(Schema.class);
        private final Validator validator = mock(Validator.class, "mock-validator");
        private Message outputChannel = mock(Message.class, "mock-message");

        public ValidXsdExpression create() throws SAXException {
            expectThatBodyIsCopiedToOutputChannel();
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
            expectMetaContentHeadersSet();
            allowing(exchange).getOut();
            will(returnValue(outputChannel));
            checking(this);
            stubInputChannel("<body />");
            stubValidator(dummy(Validator.class));

            expression.evaluate(exchange);
        }

        public void itShouldExplicitlySetAnErrorHandler() {
            allowing(exchange).getOut();
            will(returnValue(outputChannel));
            stubInputChannel("<body />");
            stubValidator(validator);
            expectMetaContentHeadersSet();
            one(validator).setErrorHandler(with(any(ErrorHandler.class)));
            justIgnore(validator);
            checking(this);

            expression.evaluate(exchange);
        }

        public void itShouldReturnFalseIfValidationErrorsOccur() throws IOException, SAXException {
            stubValidationError(new Action() {
                @SuppressWarnings({"ThrowableInstanceNeverThrown"})
                @Override public Object invoke(final Invocation invocation) throws Throwable {
                    errorHandlerCapture.error(new SAXParseException("", dummy(Locator.class)));
                    return null;
                }
                @Override public void describeTo(final Description description) {}
            });
            justIgnore(outputChannel);
            checking(this);

            specify(expression,
                shouldEvaluateExchangeAndReturn(exchange, false));
        }

        public void itShouldReturnFalseIfFatalValidationErrorsOccur() throws IOException, SAXException {
            expectThatBodyIsCopiedToOutputChannel();
            stubValidationError(new Action() {
                @SuppressWarnings({"ThrowableInstanceNeverThrown"})
                @Override public Object invoke(final Invocation invocation) throws Throwable {
                    errorHandlerCapture.fatalError(new SAXParseException("", dummy(Locator.class)));
                    return null;
                }
                @Override public void describeTo(final Description description) {}
            });
            justIgnore(outputChannel);
            checking(this);

            specify(expression,
                shouldEvaluateExchangeAndReturn(exchange, false));
        }

        public void itShouldLogWarningsInTheOutboundHeadersButValidationShouldSucceed() throws IOException,
            SAXException {
            stubValidationError(new Action() {
                @SuppressWarnings({"ThrowableInstanceNeverThrown"})
                @Override public Object invoke(final Invocation invocation) throws Throwable {
                    final SAXParseException exception =
                        new SAXParseException("", dummy(Locator.class));
                    errorHandlerCapture.warning(exception);
                    return null;
                }
                @Override public void describeTo(final Description description) {}
            });

            expectMetaContentHeadersSet();

            specify(expression,
                shouldEvaluateExchangeAndReturn(exchange, true));
        }

        @SuppressWarnings({"ThrowableInstanceNeverThrown"})
        public void itShouldReturnFalseIfValidationRaisesExceptions() throws IOException, SAXException {
            expectThatBodyIsCopiedToOutputChannel();
            stubValidationFailureException(new SAXParseException("", dummy(Locator.class)));
            allowing(outputChannel).setHeader(
                Environment.META_CONTENT, Environment.TRACE_WARNINGS);
            exactly(2).of(outputChannel).setHeader(
                with(any(String.class)), with(any(List.class)));
            checking(this);

            specify(expression,
                shouldEvaluateExchangeAndReturn(exchange, false));
        }

        @SuppressWarnings({"ThrowableInstanceNeverThrown"})
        public void itShouldReturnFalseIfIOInteractionRaisesExceptions() throws IOException, SAXException {
            stubValidationFailureException(new IOException());
            justIgnore(outputChannel);
            checking(this);

            specify(expression,
                shouldEvaluateExchangeAndReturn(exchange, false));
        }

        private void expectMetaContentHeadersSet() {
            allowing(outputChannel).setHeader(
                Environment.META_CONTENT, Environment.TRACE_WARNINGS);
            allowing(outputChannel).setHeader(
                with(any(String.class)), with(any(List.class)));
            checking(this);
        }

        private void stubValidationError(final Action action) throws SAXException, IOException {
            stubInputChannel("<body />");
            stubValidator(validator);

            allowing(validator).setErrorHandler(with(any(ErrorHandler.class)));
            will(storeTheErrorHandlerForTesting());

            allowing(validator).validate(with(any(Source.class)));
            will(action);

            allowing(exchange).getOut();
            will(returnValue(outputChannel));
            checking(this);
        }

        @SuppressWarnings({"ThrowableInstanceNeverThrown"})
        private void stubValidationFailureException(final Exception ex) throws SAXException, IOException {
            stubInputChannel("<body />");
            stubValidator(validator);

            allowing(validator).setErrorHandler(with(any(ErrorHandler.class)));
            will(storeTheErrorHandlerForTesting());

            allowing(validator).validate(with(any(Source.class)));
            will(throwException(ex));

            allowing(exchange).getOut();
            will(returnValue(outputChannel));

            checking(this);
        }

        private void expectThatBodyIsCopiedToOutputChannel() {
            allowing(outputChannel).setBody(
                with(any(String.class)), with(equal(String.class)));
            checking(this);
        }

        private void stubInputChannel(final Object messageBody) {
            final DefaultMessage message = new DefaultMessage();
            message.setBody(messageBody, String.class);
            stubInputChannel(message);
        }

        private void stubInputChannel(final DefaultMessage message) {
            allowing(exchange).getIn();
            will(returnValue(message));
            checking(this);
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
