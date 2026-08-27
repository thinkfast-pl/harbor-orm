// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.dialect;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Dialect name constants used with the {@link io.github.thinkfastpl.harbororm.api.annotations.Type @Type} annotation's
 * {@code dialect} parameter to scope database type overrides to specific RDBMS implementations.
 *
 * <p>Use {@link #ANY} when a type override should apply to all supported databases.
 * Use a specific constant (e.g. {@link #POSTGRES}) when the override is dialect-specific,
 * such as mapping a Java enum to a PostgreSQL custom enum type.
 *
 * <h3>Example</h3>
 * <pre>{@code
 * @Column(nullable = false)
 * @Enumerated
 * @Type(dialect = StandardDialects.POSTGRES, columnType = "account_status")
 * private Status status;
 * }</pre>
 *
 * @see io.github.thinkfastpl.harbororm.api.annotations.Type
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StandardDialects {

    /** Dialect name for the H2 database. */
    public static final String H2 = "H2";

    /** Dialect name for PostgreSQL. */
    public static final String POSTGRES = "postgres";

    /** Dialect name for MariaDB. */
    public static final String MARIADB = "mariadb";

    /** Dialect name for MySQL. */
    public static final String MYSQL = "mysql";

    /**
     * Wildcard dialect that matches all supported databases.
     * Use this when a {@link io.github.thinkfastpl.harbororm.api.annotations.Type @Type} override should apply
     * regardless of the underlying RDBMS.
     */
    public static final String ANY = "*";
}
