package org.axiom.util;

/**
 * Type safe operation wrapper.
 */
public interface Operation<T> {

    /**
     * Apply the operation to the supplied {@code input}.
     * @param input The object to apply the operation to.
     */
    void apply(final T input);
    
}
