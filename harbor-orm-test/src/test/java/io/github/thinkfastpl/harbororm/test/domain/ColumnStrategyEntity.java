// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test.domain;

import io.github.thinkfastpl.harbororm.api.annotations.Column;
import io.github.thinkfastpl.harbororm.api.annotations.Entity;
import io.github.thinkfastpl.harbororm.api.annotations.Id;
import lombok.*;

/**
 * Test entity that relies on the default SNAKE_CASE column name strategy.
 * Fields intentionally omit explicit {@code @Column(name = "...", nullable = false)} to verify
 * that the annotation processor applies the strategy from {@code @Entity}.
 */
@Entity(table = "column_strategy_test")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ColumnStrategyEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = true)
    private String phoneNumber;
}
