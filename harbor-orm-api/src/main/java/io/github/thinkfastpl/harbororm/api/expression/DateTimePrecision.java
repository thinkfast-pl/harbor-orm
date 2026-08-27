// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Precision levels for DATE_TRUNC function.
 */
@Getter
@RequiredArgsConstructor
public enum DateTimePrecision {
    MICROSECOND,
    MILLISECOND,
    SECOND,
    MINUTE,
    HOUR,
    DAY,
    WEEK,
    MONTH,
    QUARTER,
    YEAR,
}
