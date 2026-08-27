// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Date/time field parts for EXTRACT and date arithmetic functions.
 */
@Getter
@RequiredArgsConstructor
public enum DateTimePart {
    YEAR("YEAR"),
    MONTH("MONTH"),
    DAY("DAY"),
    HOUR("HOUR"),
    MINUTE("MINUTE"),
    SECOND("SECOND"),
    MILLISECOND("MILLISECOND"),
    MICROSECOND("MICROSECOND"),
    DAY_OF_WEEK("DOW"),
    WEEK("WEEK"),
    QUARTER("QUARTER"),
    EPOCH("EPOCH");

    private final String sql;
}
