// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core.sql;

import io.github.thinkfastpl.harbororm.api.HarborSession;
import io.github.thinkfastpl.harbororm.api.converter.AttributeConverterSupplier;
import io.github.thinkfastpl.harbororm.api.converter.JsonSerializer;
import io.github.thinkfastpl.harbororm.core.HarborSessionFactory;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.sql.Connection;

public class SqlHarborSessionFactory {

    @Value
    @Builder
    public static class Params {
        AttributeConverterSupplier attributeConverterSupplier;
        JsonSerializer jsonSerializer;
        SqlQueryMonitor sqlQueryMonitor;
    }

    public static HarborSession forConnection(@NonNull Connection connection, @NonNull RdbmsSupport rdbmsSupport) {
        return forConnectionAccessor(SqlConnectionAccessor.of(connection), rdbmsSupport);
    }

    public static HarborSession forConnectionAccessor(@NonNull SqlConnectionAccessor accessor, @NonNull RdbmsSupport rdbmsSupport) {
        return HarborSessionFactory.of(
                new SqlQueryExecutor(accessor, rdbmsSupport, rdbmsSupport.createDialect(), null, null, null),
                new SqlPortableLobSupport(accessor, rdbmsSupport)
        );
    }

    public static HarborSession forConnectionAccessor(@NonNull SqlConnectionAccessor accessor, @NonNull RdbmsSupport rdbmsSupport, @NonNull Params params) {
        return HarborSessionFactory.of(
                new SqlQueryExecutor(accessor, rdbmsSupport, rdbmsSupport.createDialect(), params.getAttributeConverterSupplier(), params.getJsonSerializer(), params.getSqlQueryMonitor()),
                new SqlPortableLobSupport(accessor, rdbmsSupport)
        );
    }
}
