// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Entity;
import io.github.thinkfastpl.harbororm.api.annotations.Id;
import io.github.thinkfastpl.harbororm.api.annotations.TypeHandler;
import io.github.thinkfastpl.harbororm.api.dialect.StandardDialects;
import io.github.thinkfastpl.harbororm.test.domain.handler.DurationMillisTypeHandler;
import io.github.thinkfastpl.harbororm.test.domain.handler.DurationSecondsTypeHandler;
import lombok.*;

import java.time.Duration;

@Entity(table = "type_handler_dialect_test")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class TypeHandlerDialectTestEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "duration_val", nullable = false)
    @TypeHandler(dialect = StandardDialects.POSTGRES, value = DurationSecondsTypeHandler.class)
    @TypeHandler(value = DurationMillisTypeHandler.class)
    private Duration duration;
}
