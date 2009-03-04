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

package org.axiom;

import jdave.ExpectationFailedException;
import jdave.IContract;
import org.apache.commons.beanutils.BeanToPropertyValueTransformer;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;
import static org.apache.commons.collections.TransformerUtils.*;
import org.apache.commons.lang.ObjectUtils;
import org.jmock.Expectations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

@SuppressWarnings({"SuspiciousToArrayCall"})
public abstract class SpecSupport extends Expectations {

    private final static Logger log = LoggerFactory.getLogger(SpecSupport.class);

    protected SpecSupport justIgnore(final Object... things) {
        for (final Object o : things) allowing(o);
        return this;
    }

    public static IContract propertyValueContract(final String propertyName, final Object expectedValue) {
        return new IContract() {
            @Override
            public void isSatisfied(final Object obj) throws ExpectationFailedException {
                BeanToPropertyValueTransformer transformer =
                    new BeanToPropertyValueTransformer(propertyName);
                if (!ObjectUtils.equals(transformer.transform(obj), expectedValue)) {
                    throw new ExpectationFailedException(
                        MessageFormat.format("Expected {0} equal to {1} but was {2}.",
                            propertyName, expectedValue, obj));
                }
                log.debug("Object {0} satisfied propertyValueContract{propertyName={1}, expectedValue={2}}.",
                    new Object[] {obj, propertyName, expectedValue});
            }
        };
    };

    public static Object setTo(final Object object) { return object; }

    public interface ExpectedSideEffect {
        <T extends SpecSupport> void effect(final T instance);
    }

    public static Transformer becomes(final Object constantToReturn) {
        return constantTransformer(constantToReturn);
    }

    public static Transformer transform(final Predicate condition, final Transformer replacement) {
        return switchTransformer(condition, replacement, nopTransformer());
    }

    public static Transformer transform(final Predicate condition, final Transformer replacement,
            final Transformer noReplacement) {
        return switchTransformer(condition, replacement, noReplacement);
    }

    public static Transformer property(final String propertyName) {
        return new BeanToPropertyValueTransformer(propertyName);
    }
}
