// SPDX-License-Identifier: Apache-2.0
package io.github.thinkfastpl.harbororm.api.expression;

import io.github.thinkfastpl.harbororm.api.metadata.ColumnContext;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NextvalExpression implements Expression<Long> {

    @Getter
    private final String sequence;

    @Override
    public Class<Long> getJavaType() {
        return Long.class;
    }

    @Override
    public ColumnContext getColumnContext(@NonNull String dialectName) {
        return null;
    }
}
