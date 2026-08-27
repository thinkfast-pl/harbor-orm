// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.utils;

import lombok.NonNull;

import java.util.Arrays;
import java.util.stream.Stream;

public class StreamUtils {

    @SafeVarargs
    public static <T> Stream<T> concat(@NonNull Stream<T>... streams) {
        return Arrays.stream(streams)
                .reduce(Stream.empty(), Stream::concat);
    }
}
