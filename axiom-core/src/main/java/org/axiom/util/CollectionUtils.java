package org.axiom.util;

import static edu.emory.mathcs.backport.java.util.Arrays.*;
import static org.apache.commons.collections.CollectionUtils.*;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.math.*;

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
     * @param transformer The {@link Computation} to transform items with
     * @return A transformed {@link Iterable}.
     */
    @SuppressWarnings({"unchecked"})
    public static <TIn,TOut> Iterable<TOut> map(final Iterable<TIn> iterable,
        final Computation<TIn,TOut> transformer) {
        //The type constraints of the inputs make this a safe assumption
        return new ArrayList<TOut>(collect(iterable.iterator(), transformer));
    }

    /**
     * A generic version of <i>map</i> that ignores the return values of the supplied operation.
     * @param iterable The iterable items to operate on.
     * @param operation The operation to apply to each item.
     * @param <T> The underlying type of the items we're mapping {@code oepration} over.
     */
    public static <T> void map(final Iterable<T> iterable, final Operation<T> operation) {
        for (final T item : iterable) {
            operation.apply(item);
        }
    }

    //NB: the amount of effort to remove the duplication below just isn't worth it...

    public static Iterable<Long> range(final Long... values) {
        //return typedCollection(asList(values), Long.class);
        final LongRange range = new LongRange(values[0], values[values.length - 1]);
        return typedCollection(asList(ArrayUtils.toObject(range.toArray())), Long.class);        
    }

    public static Iterable<Integer> range(final Integer... values) {
        final IntRange range = new IntRange(values[0], values[values.length - 1]);
        return typedCollection(asList(ArrayUtils.toObject(range.toArray())), Integer.class);
    }
}
