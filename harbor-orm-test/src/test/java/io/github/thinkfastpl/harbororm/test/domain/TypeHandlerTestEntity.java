// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Entity;
import io.github.thinkfastpl.harbororm.api.annotations.Id;
import io.github.thinkfastpl.harbororm.api.annotations.TypeHandler;
import io.github.thinkfastpl.harbororm.test.domain.handler.DurationMillisTypeHandler;
import lombok.*;

import java.time.Duration;

@Entity(table = "type_handler_test")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class TypeHandlerTestEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "duration_ms", nullable = false)
    @TypeHandler(DurationMillisTypeHandler.class)
    private Duration duration;

    @Column(name = "nullable_duration_ms", nullable = true)
    @TypeHandler(DurationMillisTypeHandler.class)
    private Duration nullableDuration;
}
