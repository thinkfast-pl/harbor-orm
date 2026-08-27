// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.postgres;

import lombok.NonNull;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Objects;

public class ScriptLoader {

    public static void load(@NonNull Connection connection, @NonNull String scriptName) {
        try (InputStream is = Objects.requireNonNull(ScriptLoader.class.getResourceAsStream("/sql/" + scriptName));
             Statement statement = connection.createStatement()) {
            String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            statement.execute(sql);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
