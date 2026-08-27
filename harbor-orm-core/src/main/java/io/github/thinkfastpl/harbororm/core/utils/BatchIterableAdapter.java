// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core.utils;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
class BatchIterableAdapter<T> implements Iterable<List<T>> {

    @RequiredArgsConstructor
    private class BatchIterableAdapterIterator implements Iterator<List<T>> {
        private int position = 0;

        @Override
        public boolean hasNext() {
            return position < source.size();
        }

        @Override
        public List<T> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            List<T> sublist = source.subList(position, Math.min(position + batchSize, source.size()));
            position += batchSize;
            return sublist;
        }
    }

    @NonNull
    private final List<T> source;
    private final int batchSize;


    @Override
    public Iterator<List<T>> iterator() {
        return new BatchIterableAdapterIterator();
    }
}
