// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Entity;
import io.github.thinkfastpl.harbororm.api.annotations.Id;
import io.github.thinkfastpl.harbororm.api.annotations.TypeHandler;
import io.github.thinkfastpl.harbororm.core.sql.OffsetDateTimeAsTimestampTypeHandler;
import lombok.*;

import java.time.OffsetDateTime;

@Entity(table = "offset_date_time_handler_test")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class OffsetDateTimeTypeHandlerTestEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "event_time", nullable = false)
    @TypeHandler(OffsetDateTimeAsTimestampTypeHandler.class)
    private OffsetDateTime eventTime;

    @Column(name = "nullable_event_time", nullable = true)
    @TypeHandler(OffsetDateTimeAsTimestampTypeHandler.class)
    private OffsetDateTime nullableEventTime;
}
