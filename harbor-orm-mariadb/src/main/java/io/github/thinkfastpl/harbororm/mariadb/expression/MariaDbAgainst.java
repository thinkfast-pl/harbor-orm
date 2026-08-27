// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.mariadb.expression;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * MariaDB AGAINST clause holder: query string plus search mode.
 * <p>
 * Used together with {@link MariaDbMatch} to form {@code MATCH(cols) AGAINST(? <mode>)}.
 */
@RequiredArgsConstructor
@Getter
public class MariaDbAgainst {

    public enum Mode {
        NATURAL,
        BOOLEAN,
        WITH_QUERY_EXPANSION,
    }

    @NonNull
    private final String query;

    @NonNull
    private final Mode mode;
}
