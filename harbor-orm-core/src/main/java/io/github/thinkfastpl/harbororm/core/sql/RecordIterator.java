// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core.sql;

import io.github.thinkfastpl.harbororm.api.query.result.Record;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

class RecordIterator<T> implements Iterator<T> {

    private final ExpressionsBasedRecordFactory factory;
    private final Function<Record, T> recordMapper;
    private final int fetchSize;
    private final Deque<T> buffer = new ArrayDeque<>();
    private boolean endReached = false;

    RecordIterator(ExpressionsBasedRecordFactory factory, Function<Record, T> recordMapper, int fetchSize) {
        this.factory = factory;
        this.recordMapper = recordMapper;
        // JDBC allows fetchSize 0 (driver default); each batch must still load at least one record
        this.fetchSize = Math.max(1, fetchSize);
    }

    @Override
    public boolean hasNext() {
        if (buffer.isEmpty() && !endReached) {
            fillBuffer();
        }
        return !buffer.isEmpty();
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return buffer.poll();
    }

    private void fillBuffer() {
        for (int i = 0; i < fetchSize; i++) {
            Record record = factory.fetchNextRecord();
            if (record == null) {
                endReached = true;
                return;
            }
            buffer.add(recordMapper.apply(record));
        }
    }
}
