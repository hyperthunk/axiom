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

package org.axiom.systest;

import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.axiom.SpecSupport;
import org.axiom.plugins.ValidXsdExpression;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;

@RunWith(JDaveRunner.class)
public class ValidXsdIntegrationSpec extends Specification<ValidXsdExpression> {

    public class WhenValidatingXmlSchemaOnTheFly extends SpecSupport {

        /*private Exchange exchange = new DefaultExchange();
        private Message message = new DefaultMessage();*/
        private ValidXsdExpression expression;

        public ValidXsdExpression create() throws SAXException {
            /*allowing(exchange).getIn();
            will(returnValue(message));
            checking(this);*/
            return expression = ValidXsdExpression.forSchema(
                "<xsd:schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" version=\"1.0\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
                "  <xsd:element nillable=\"false\" name=\"request\">\n" +
                "    <xsd:complexType>\n" +
                "      <xsd:sequence minOccurs=\"0\" maxOccurs=\"unbounded\">\n" +
                "        <xsd:element name=\"data\" type=\"xsd:string\" />\n" +
                "      </xsd:sequence>\n" +
                "      <xsd:attribute name=\"id\" type=\"xsd:string\" default=\"12345\" />\n" +
                "    </xsd:complexType>\n" +
                "  </xsd:element>\n" +
                "</xsd:schema>");
        }

        /*public void itShouldReturnFalseForBadXml() {
            stubMessageBody("<invalid><markup></invalid>");
            specify(expression,
                shouldEvaluateExchangeAndReturn(exchange, false));
        }

        public void itShouldReturnFalseForInvalidXml() {
            stubMessageBody("<request><foobar></request>");
            specify(expression,
                shouldEvaluateExchangeAndReturn(exchange, false));
        }

        public void itShouldNotBounceGoodXml() {
            stubMessageBody("<request><data /></request>");
            specify(expression,
                shouldEvaluateExchangeAndReturn(exchange, true));
        }

        private void stubMessageBody(final String xml) {
            allowing(message).getBody();
            will(returnValue(xml));
            ignoring(exchange);
            checking(this);
        }*/

    }

}
