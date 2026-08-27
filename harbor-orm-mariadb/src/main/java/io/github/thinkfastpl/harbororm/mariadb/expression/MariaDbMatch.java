// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.mariadb.expression;

import io.github.thinkfastpl.harbororm.api.expression.Expression;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * MariaDB MATCH clause holder: the column list to search.
 * <p>
 * Combine with a {@link MariaDbAgainst} via {@link #matches(MariaDbAgainst)} to obtain
 * a {@link MariaDbMatchAgainstCondition} usable as a WHERE/HAVING predicate.
 */
@RequiredArgsConstructor
@Getter
public class MariaDbMatch {

    @NonNull
    private final List<Expression<?>> columns;

    public MariaDbMatchAgainstCondition matches(@NonNull MariaDbAgainst against) {
        return new MariaDbMatchAgainstCondition(this, against);
    }
}
