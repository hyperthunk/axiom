package org.axiom.util;

import static org.apache.commons.collections.CollectionUtils.*;

import java.util.*;

public class CollectionUtils {

    private CollectionUtils() {}

    /**
     * A generic wrap around the commons collections class/method of the same name.
     * @param collection The collection to limit to a specific type
     * @param type The type of objects which may be added to the collection
     * @param <T> The type of {@code type}.
     * @return A new, generic collection containing all the underlying items from the
     * orginal {@code collection} which are of the requisite type.
     */
    @SuppressWarnings({"unchecked"})
    public static <T> Collection<T> typedCollection(final Collection collection, Class<T> type) {
        final Collection runtimeCheckedCollection =
            org.apache.commons.collections.CollectionUtils.typedCollection(collection, type);
        return new LinkedList<T>(runtimeCheckedCollection);
    }

    /**
     * A generic wrap around map/collect on an iterable reference, using a type mapped transformer.
     * @param iterable The {@link Iterable} to map over
     * @param transformer The {@link TypeMappedTransformer} to transform items with
     * @return A transformed {@link Iterable}.
     */
    @SuppressWarnings({"unchecked"})
    public static <TIn,TOut> Iterable<TOut> map(final Iterable<TIn> iterable,
        final TypeMappedTransformer<TIn,TOut> transformer) {
        //The type constraints of the inputs make this a safe assumption
        return new ArrayList<TOut>(collect(iterable.iterator(), transformer));
    }
}
