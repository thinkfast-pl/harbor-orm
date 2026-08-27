// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core.utils;

import lombok.NonNull;

import java.util.*;

/**
 * Utility methods for list operations: merging, simultaneous iteration, batch splitting,
 * and emptiness checks.
 */
public class HarborListUtils {

    /**
     * A functional interface that accepts three arguments and returns no result.
     * This is the three-arity specialization of {@link java.util.function.Consumer}.
     *
     * @param <T> the type of the first argument
     * @param <U> the type of the second argument
     * @param <V> the type of the third argument
     */
    @FunctionalInterface
    public interface TriConsumer<T, U, V> {

        /**
         * Performs this operation on the given arguments.
         *
         * @param t the first input argument
         * @param u the second input argument
         * @param v the third input argument
         */
        void accept(T t, U u, V v);
    }

    /**
     * Merges two lists into a new list. Returns an empty list if both inputs are empty.
     *
     * @param list1 the first list
     * @param list2 the second list
     * @param <T> the element type
     * @return a new list containing all elements from both lists, or an empty list if both are empty
     */
    public static <T> List<T> merge(@NonNull List<T> list1, @NonNull List<T> list2) {
        if (list1.isEmpty() && list2.isEmpty()) {
            return Collections.emptyList();
        }

        final List<T> merged = new ArrayList<>(list1.size() + list2.size());
        merged.addAll(list1);
        merged.addAll(list2);
        return merged;
    }

    /**
     * Merges three lists into a new list. Returns an empty list if all inputs are empty.
     *
     * @param list1 the first list
     * @param list2 the second list
     * @param list3 the third list
     * @param <T> the element type
     * @return a new list containing all elements from all three lists, or an empty list if all are empty
     */
    public static <T> List<T> merge(@NonNull List<T> list1, @NonNull List<T> list2, @NonNull List<T> list3) {
        if (list1.isEmpty() && list2.isEmpty() && list3.isEmpty()) {
            return Collections.emptyList();
        }

        final List<T> merged = new ArrayList<>(list1.size() + list2.size() + list3.size());
        merged.addAll(list1);
        merged.addAll(list2);
        merged.addAll(list3);
        return merged;
    }

    /**
     * Iterates two iterables in lockstep, invoking the consumer with each pair of elements
     * and the current zero-based index. Iteration stops when either iterable is exhausted.
     *
     * @param list1 the first iterable
     * @param list2 the second iterable
     * @param consumer the consumer called with each element pair and the index
     * @param <T> the element type of the first iterable
     * @param <U> the element type of the second iterable
     */
    public static <T, U> void iterateSimultaneously(@NonNull Iterable<T> list1, @NonNull Iterable<U> list2, @NonNull TriConsumer<T, U, Integer> consumer) {
        int index = 0;
        final Iterator<T> it1 = list1.iterator();
        final Iterator<U> it2 = list2.iterator();

        while (it1.hasNext() && it2.hasNext()) {
            final T item1 = it1.next();
            final U item2 = it2.next();
            consumer.accept(item1, item2, index++);
        }
    }

    /**
     * Returns {@code true} if the collection is non-null and contains at least one element.
     *
     * @param collection the collection to check
     * @return {@code true} if the collection is non-null and non-empty, {@code false} otherwise
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }

    /**
     * Returns an iterable that yields fixed-size sublists (batches) of the given list.
     * The last batch may contain fewer elements if the list size is not evenly divisible.
     *
     * @param list the list to split into batches
     * @param batchSize the maximum number of elements per batch
     * @param <T> the element type
     * @return an iterable of sublists, each containing at most {@code batchSize} elements
     */
    public static <T> Iterable<List<T>> batch(@NonNull List<T> list, int batchSize) {
        return new BatchIterableAdapter<>(list, batchSize);
    }
}
