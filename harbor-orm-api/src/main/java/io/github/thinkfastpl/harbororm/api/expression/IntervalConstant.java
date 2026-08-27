// SPDX-License-Identifier: Apache-2.0
package io.github.thinkfastpl.harbororm.api.expression;

import io.github.thinkfastpl.harbororm.api.interval.Interval;
import io.github.thinkfastpl.harbororm.api.metadata.ColumnContext;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IntervalConstant implements Expression<Interval> {

    @NonNull
    @Getter
    private final Interval interval;

    @Override
    public ColumnContext getColumnContext(@NonNull String dialectName) {
        return null;
    }

    @Override
    public Class<Interval> getJavaType() {
        return Interval.class;
    }
}
