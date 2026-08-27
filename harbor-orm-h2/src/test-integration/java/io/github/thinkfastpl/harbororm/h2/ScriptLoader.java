// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.h2;

import lombok.NonNull;
import org.h2.tools.RunScript;

import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.Objects;

public class ScriptLoader {

    public static void load(@NonNull Connection connection, @NonNull String scriptName) {
        try (InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(ScriptLoader.class.getResourceAsStream("/sql/" + scriptName)))) {
            RunScript.execute(connection, reader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
