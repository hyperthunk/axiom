package org.axiom.util;

import org.apache.commons.collections.Transformer;

/**
 * Represents a computation that can be applied to an input of one type,
 * returning an output of another. Uses an overriden covariant return type to
 * make the {@code transform} method of the commons collections {@link Transformer}
 * interface a bit more friendly to use in practise.
 */
public abstract class Computation<TInput,TOutput> implements Transformer {

    //If only the awful type erasure didn't prevent @Override public T transform(final T input)
    @Override public TOutput transform(final Object input) {
        // yet more proof of java broken type system - why am I having to do this!? :-(
        // I can't *even* write a safe_cast<T> function because all the type info is lost!
        return apply((TInput) input);
    }

    /**
     * Type safe application of a function from a => b
     * @param input The input to transform by function application.
     * @return 
     */
    public abstract TOutput apply(final TInput input);
}
