// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.postgres.expression;

final class PostgresExpressionUtils {

    private PostgresExpressionUtils() {
    }

    static String escapeSqlString(String value) {
        return value.replace("'", "''");
    }
}
