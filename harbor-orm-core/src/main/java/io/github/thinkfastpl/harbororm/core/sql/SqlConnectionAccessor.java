// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core.sql;

import lombok.NonNull;

import java.sql.Connection;
import java.sql.SQLException;

public interface SqlConnectionAccessor {

    static SqlConnectionAccessor of(@NonNull Connection connection) {
        return new SqlConnectionAccessor() {
            @Override
            public <R> R execute(@NonNull SqlConnectionFunction<R> function) {
                try {
                    return function.execute(connection);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    <R> R execute(@NonNull SqlConnectionFunction<R> function);
}
