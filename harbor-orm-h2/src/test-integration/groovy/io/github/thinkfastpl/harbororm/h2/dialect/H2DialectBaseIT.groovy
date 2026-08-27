// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2.dialect

import io.github.thinkfastpl.harbororm.api.HarborSession
import io.github.thinkfastpl.harbororm.core.HarborSessionFactory
import io.github.thinkfastpl.harbororm.core.sql.SqlConnectionAccessor
import io.github.thinkfastpl.harbororm.h2.ScriptLoader
import spock.lang.Specification

import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement

abstract class H2DialectBaseIT extends Specification {
    private static final Connection connection = DriverManager.getConnection("jdbc:h2:mem:H2DialectBaseIT;CASE_INSENSITIVE_IDENTIFIERS=TRUE")

    protected static final HarborSession session = HarborSessionFactory.builder()
            .connectionAccessor(SqlConnectionAccessor.of(connection))
            .rdbmsSupport(new H2RdbmsSupport())
            .build()

    static {
        connection.setAutoCommit(false)
    }

    void loadScript(String scriptName) {
        ScriptLoader.load(connection, scriptName)
    }

    void dropAllObjects() {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DROP ALL OBJECTS")
        }
    }

    void cleanup() {
        connection.rollback()
    }
}
