// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.mysql.expression;

import io.github.thinkfastpl.harbororm.api.expression.Expression;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * MySQL MATCH clause holder: the column list to search.
 * <p>
 * Combine with a {@link MySqlAgainst} via {@link #matches(MySqlAgainst)} to obtain
 * a {@link MySqlMatchAgainstCondition} usable as a WHERE/HAVING predicate.
 */
@RequiredArgsConstructor
@Getter
public class MySqlMatch {

    @NonNull
    private final List<Expression<?>> columns;

    public MySqlMatchAgainstCondition matches(@NonNull MySqlAgainst against) {
        return new MySqlMatchAgainstCondition(this, against);
    }
}
