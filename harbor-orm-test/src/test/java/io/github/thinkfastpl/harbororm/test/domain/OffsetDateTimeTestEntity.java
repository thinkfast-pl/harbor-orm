// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Entity;
import io.github.thinkfastpl.harbororm.api.annotations.Id;
import lombok.*;

import java.time.OffsetDateTime;

@Entity(table = "offset_date_time_test")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class OffsetDateTimeTestEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "event_time", nullable = false)
    private OffsetDateTime eventTime;

    @Column(name = "nullable_event_time", nullable = true)
    private OffsetDateTime nullableEventTime;
}
