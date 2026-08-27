// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.interval;

import lombok.NonNull;
import lombok.Value;

@Value
public class Interval {

    int value;

    @NonNull
    IntervalUnit unit;

    public static Interval years(int value) {
        return new Interval(value, IntervalUnit.YEAR);
    }

    public static Interval months(int value) {
        return new Interval(value, IntervalUnit.MONTH);
    }

    public static Interval weeks(int value) {
        return new Interval(value, IntervalUnit.WEEK);
    }

    public static Interval days(int value) {
        return new Interval(value, IntervalUnit.DAY);
    }

    public static Interval hours(int value) {
        return new Interval(value, IntervalUnit.HOUR);
    }

    public static Interval minutes(int value) {
        return new Interval(value, IntervalUnit.MINUTE);
    }

    public static Interval seconds(int value) {
        return new Interval(value, IntervalUnit.SECOND);
    }

    public static Interval milliseconds(int value) {
        return new Interval(value, IntervalUnit.MILLISECOND);
    }

    public static Interval microseconds(int value) {
        return new Interval(value, IntervalUnit.MICROSECOND);
    }
}
