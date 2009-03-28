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

import org.apache.camel.*;
import org.apache.commons.io.IOUtils;
import static org.apache.commons.lang.Validate.*;
import org.axiom.integration.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.*;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

public class ValidXsdExpression implements Expression, Predicate<Exchange> {

    private static final SchemaFactory factory =
        SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Schema schema;

    public ValidXsdExpression(final String pathToXsd) throws SAXException {
        this(factory.newSchema(new File(pathToXsd)));
    }

    public ValidXsdExpression(final URL url) throws SAXException {
        this(factory.newSchema(url));
    }

    public ValidXsdExpression(final Schema schema) {
        notNull(schema, "Schema cannot be null.");
        this.schema = schema;
    }

    /**
     * Utility method to create a {@link Schema} object for a given xml string.
     * @param schemaXml A {@link String} containing the schema xml.
     * @return A new {@link Schema} instance.
     * @throws SAXException if the schema cannot be loaded.
     */
    public static ValidXsdExpression forSchema(final String schemaXml) throws SAXException {
        final Schema schema =
            factory.newSchema(new StreamSource(IOUtils.toInputStream(schemaXml)));
        return new ValidXsdExpression(schema);
    }

    /**
     * {@inheritDoc}
     */    
    @Override public Boolean evaluate(final Exchange exchange) {
        //TODO: when input body is null, make the choice between a runtime exception and validation failure a configurable policy
        final List<Exception> errors = new LinkedList<Exception>();
        final List<Exception> warnings = new LinkedList<Exception>();
        final Message inputChannel = exchange.getIn();
        final String body = inputChannel.getBody(String.class);
        if (body == null ) {
            throw new InvalidPayloadRuntimeException(exchange, String.class);
        }
        try {
            final Validator validator = schema.newValidator();
            validator.setErrorHandler(
                new ErrorHandler() {
                    @Override public void warning(final SAXParseException e) throws SAXException {
                        warnings.add(e);
                    }

                    @Override public void error(final SAXParseException e) throws SAXException {
                        errors.add(e);
                    }

                    @Override public void fatalError(final SAXParseException e) throws SAXException {
                        error(e);
                    }
                }
            );
            logger.debug("Validating xml schema against {}", body);
            validator.validate(new StreamSource(IOUtils.toInputStream(body)));
        } catch (SAXException e) {
            errors.add(e);
        } catch (IOException e) {
            errors.add(e);
        }
        final Message outputChannel = exchange.getOut();
        outputChannel.setBody(body, String.class);
        setTraceInfoHeader(outputChannel);
        if (!warnings.isEmpty()) {
            logger.debug("Validation yielded warnings.");
            outputChannel.setHeader(Environment.TRACE_WARNINGS, warnings);
        }
        if (!errors.isEmpty()) {
            logger.debug("Validation failed.");
            outputChannel.setHeader(Environment.TRACE_ERRORS, errors);
            return false;
        } else {
            return true;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override public boolean matches(final Exchange exchange) {
        return evaluate(exchange);
    }

    /**
     * {@inheritDoc}
     */
    @Override public void assertMatches(final String text, final Exchange exchange) throws AssertionError {
        if (!matches(exchange)) {
            //TODO: something a bit more explanatory!?
            throw new AssertionError(text);
        }
    }

    private void setTraceInfoHeader(final Message message) {
        message.setHeader(Environment.META_CONTENT, Environment.TRACE_INFO_HEADER);
    }
}
