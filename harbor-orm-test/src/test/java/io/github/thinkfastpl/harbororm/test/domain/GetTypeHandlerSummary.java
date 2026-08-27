// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.StoredFunction;
import io.github.thinkfastpl.harbororm.api.annotations.TypeHandler;
import io.github.thinkfastpl.harbororm.test.domain.handler.DurationMillisTypeHandler;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;

@StoredFunction(name = "get_type_handler_summary")
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GetTypeHandlerSummary {

    @Column(nullable = false)
    private String name;

    @Column(name = "duration_ms", nullable = false)
    @TypeHandler(DurationMillisTypeHandler.class)
    private Duration duration;

    @Column(name = "nullable_duration_ms", nullable = true)
    @TypeHandler(DurationMillisTypeHandler.class)
    private Duration nullableDuration;
}
