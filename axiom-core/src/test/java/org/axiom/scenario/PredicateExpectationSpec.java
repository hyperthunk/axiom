package org.axiom.scenario;

import jdave.Block;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.axiom.SpecSupport;
import org.junit.runner.RunWith;

@RunWith(JDaveRunner.class)
public class PredicateExpectationSpec extends Specification<PredicateExpectation> {

    public class WhenUsedToVerifyTheGivenExchange extends SpecSupport {

        private Predicate<Exchange> predicate;
        private PredicateExpectation<Exchange> expectation;

        public PredicateExpectation<Exchange> create() {
            predicate = mock(Predicate.class);
            return expectation = new PredicateExpectation<Exchange>(predicate);
        }

        public void itShouldPukeIfTheSuppliedPredicateIsNull() {
            specify(new Block() {
                @Override public void run() throws Throwable {
                    new PredicateExpectation<Exchange>(null);
                }
            }, should.raise(IllegalArgumentException.class));
        }

        public void itShouldReturnWithoutFurtherAdoWhenTheUnderlyingPredicateIsTrue() throws VerificationFailureException {
            one(predicate).assertMatches(with(any(String.class)), with(any(Exchange.class)));
            checking(this);

            expectation.verify(dummy(Exchange.class));
        }

        public void itShouldPassTheExpectationTextToTheUnderlyingPredicateWhenItIsPresent() throws VerificationFailureException {
            final String message = "message";

            one(predicate).assertMatches(with(equal(message)), with(any(Exchange.class)));
            checking(this);

            new PredicateExpectation<Exchange>(message, predicate).verify(dummy(Exchange.class));
        }

    }

}
