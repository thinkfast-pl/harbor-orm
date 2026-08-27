// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect;

import io.github.thinkfastpl.harbororm.core.sql.RdbmsSupport;
import io.github.thinkfastpl.harbororm.core.sql.dialect.SqlDialect;

/**
 * H2 database {@link RdbmsSupport} implementation.
 * <p>
 * Relies entirely on the default JDBC-based LOB handling and parameter binding
 * provided by {@link RdbmsSupport}; only {@link #createDialect()} is overridden
 * to supply the H2-specific SQL dialect.
 */
public class H2RdbmsSupport implements RdbmsSupport {

    /** {@inheritDoc} */
    @Override
    public SqlDialect createDialect() {
        return new H2SqlDialect();
    }
}
