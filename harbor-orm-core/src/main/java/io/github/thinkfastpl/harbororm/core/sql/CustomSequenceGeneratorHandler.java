// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core.sql;

import lombok.NonNull;

import java.sql.Connection;

public interface CustomSequenceGeneratorHandler {

    Long nextVal(@NonNull Connection connection, @NonNull String sequenceName);
}
