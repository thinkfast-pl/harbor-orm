// SPDX-License-Identifier: Apache-2.0
package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.expression.DSL
import io.github.thinkfastpl.harbororm.api.expression.DateTimePrecision
import io.github.thinkfastpl.harbororm.api.interval.Interval

import java.sql.Date
import java.sql.Time
import java.sql.Timestamp
import java.time.*

class PortableFunctionExpressionDateTimeIT extends AbstractHarborIT {

    def "select currentTime()"() {
        when:
            def result = session.select(DSL.currentTime()).fetchSingle()

        then:
            result != null
            result instanceof LocalTime

        where:
            session << allSessions
    }

    def "select currentDate()"() {
        given:
            LocalDate today = LocalDate.now(ZoneOffset.UTC)

        when:
            def result = session.select(DSL.currentDate()).fetchSingle()

        then:
            result instanceof LocalDate
            (result == today || result == today.minusDays(1) || result == today.plusDays(1))

        where:
            session << allSessions
    }

    def "select currentTimestamp()"() {
        when:
            def result = session.select(DSL.currentTimestamp()).fetchSingle()
            System.out.println(result.toString())

        then:
            result instanceof OffsetDateTime

        where:
            session << allSessions
    }

    def "select currentDateTime()"() {
        when:
            def result = session.select(DSL.currentDateTime()).fetchSingle()

        then:
            result != null
            result instanceof OffsetDateTime

        where:
            session << allSessions
    }

    def "select currentDateTime() cast to LocalDateTime"() {
        expect:
            session.select(DSL.currentDateTime().cast(LocalDateTime.class)).fetchSingle() != null

        where:
            session << allSessions
    }

    def "select currentLocalDateTime()"() {
        when:
            def result = session.select(DSL.currentLocalDateTime()).fetchSingle()

        then:
            result != null
            result instanceof LocalDateTime

        where:
            session << allSessions
    }

    def "select date()"() {
        when:
            def result = session.select(DSL.date(value)).fetchSingle()

        then:
            result == expected
            result instanceof LocalDate

        where:
            session << allSessions

        combined:
            value || expected
            '2020-02-03' || LocalDate.of(2020, 2, 3)
            '1999-12-31' || LocalDate.of(1999, 12, 31)
            '2025-01-01' || LocalDate.of(2025, 1, 1)
            '2000-02-29' || LocalDate.of(2000, 2, 29)
    }

    def "select dateAdd(Expression, Expression)"() {
        when:
            def result = session.select(DSL.dateAdd(DSL.date(dateStr), DSL.constant(days))).fetchSingle()

        then:
            result == expected
            result instanceof LocalDate

        where:
            session << allSessions

        combined:
            dateStr | days || expected
            '2020-02-03' | 3 || LocalDate.of(2020, 2, 6)
            '2020-02-03' | 0 || LocalDate.of(2020, 2, 3)
            '2020-02-27' | 3 || LocalDate.of(2020, 3, 1)
            '2020-02-03' | -5 || LocalDate.of(2020, 1, 29)
            '2019-12-31' | 1 || LocalDate.of(2020, 1, 1)
    }

    def "select dateAdd(Expression, Integer)"() {
        when:
            def result = session.select(DSL.dateAdd(DSL.date('2020-02-03'), 3)).fetchSingle()

        then:
            result == LocalDate.of(2020, 2, 6)
            result instanceof LocalDate

        where:
            session << allSessions
    }

    def "select dateAdd(LocalDate, Expression)"() {
        when:
            def result = session.select(DSL.dateAdd(LocalDate.of(2020, 2, 3), DSL.constant(3))).fetchSingle()

        then:
            result == LocalDate.of(2020, 2, 6)
            result instanceof LocalDate

        where:
            session << allSessions
    }

    def "select dateAdd(LocalDate, Integer)"() {
        when:
            def result = session.select(DSL.dateAdd(LocalDate.of(2020, 2, 3), 3)).fetchSingle()

        then:
            result == LocalDate.of(2020, 2, 6)
            result instanceof LocalDate

        where:
            session << allSessions
    }

    def "select dateAdd(Expression<LocalDateTime>, Interval)"() {
        when:
            def result = session.select(DSL.dateAdd(DSL.constant(datetime), interval)).fetchSingle()

        then:
            result == expected
            result instanceof LocalDateTime

        where:
            session << allSessions

        combined:
            datetime | interval || expected
            LocalDateTime.of(2020, 1, 15, 10, 30, 0) | Interval.years(2) || LocalDateTime.of(2022, 1, 15, 10, 30, 0)
            LocalDateTime.of(2020, 1, 15, 10, 30, 0) | Interval.months(3) || LocalDateTime.of(2020, 4, 15, 10, 30, 0)
            LocalDateTime.of(2020, 1, 15, 10, 30, 0) | Interval.months(-1) || LocalDateTime.of(2019, 12, 15, 10, 30, 0)
            LocalDateTime.of(2020, 1, 15, 10, 30, 0) | Interval.weeks(2) || LocalDateTime.of(2020, 1, 29, 10, 30, 0)
            LocalDateTime.of(2020, 1, 15, 10, 30, 0) | Interval.days(10) || LocalDateTime.of(2020, 1, 25, 10, 30, 0)
            LocalDateTime.of(2020, 1, 15, 10, 30, 0) | Interval.days(0) || LocalDateTime.of(2020, 1, 15, 10, 30, 0)
            LocalDateTime.of(2020, 3, 1, 10, 30, 0) | Interval.days(-1) || LocalDateTime.of(2020, 2, 29, 10, 30, 0)
            LocalDateTime.of(2020, 1, 15, 10, 30, 0) | Interval.hours(2) || LocalDateTime.of(2020, 1, 15, 12, 30, 0)
            LocalDateTime.of(2020, 1, 15, 10, 30, 0) | Interval.hours(-3) || LocalDateTime.of(2020, 1, 15, 7, 30, 0)
            LocalDateTime.of(2020, 1, 15, 10, 30, 0) | Interval.minutes(45) || LocalDateTime.of(2020, 1, 15, 11, 15, 0)
            LocalDateTime.of(2020, 1, 15, 23, 59, 50) | Interval.seconds(15) || LocalDateTime.of(2020, 1, 16, 0, 0, 5)
    }

    // Sub-second tests: MariaDB's CAST(? AS DATETIME) is precision 0, which truncates
    // the LocalDateTime constant's fractional seconds before the interval is added,
    // independent of the IntervalConstant rendering. Exclude MariaDB here.
    def "select dateAdd(Expression<LocalDateTime>, Interval) sub-second"() {
        when:
            def result = session.select(DSL.dateAdd(DSL.constant(datetime), interval)).fetchSingle()

        then:
            result == expected
            result instanceof LocalDateTime

        where:
            session << getSessionsExcept(DbType.MARIADB, DbType.MYSQL)

        combined:
            datetime | interval || expected
            LocalDateTime.of(2020, 1, 15, 10, 30, 0, 0) | Interval.milliseconds(500) || LocalDateTime.of(2020, 1, 15, 10, 30, 0, 500_000_000)
            LocalDateTime.of(2020, 1, 15, 10, 30, 0, 0) | Interval.milliseconds(1500) || LocalDateTime.of(2020, 1, 15, 10, 30, 1, 500_000_000)
            LocalDateTime.of(2020, 1, 15, 10, 30, 0, 0) | Interval.microseconds(250_000) || LocalDateTime.of(2020, 1, 15, 10, 30, 0, 250_000_000)
    }



    def "select dateAdd(Expression<LocalDateTime>, Expression<Integer>)"() {
        when:
            def result = session.select(DSL.dateAdd(DSL.constant(dt), DSL.constant(days))).fetchSingle()

        then:
            result == expected
            result instanceof LocalDateTime

        where:
            session << allSessions

        combined:
            dt | days || expected
            LocalDateTime.of(2020, 2, 3, 10, 30, 0) | 3 || LocalDateTime.of(2020, 2, 6, 10, 30, 0)
            LocalDateTime.of(2020, 2, 3, 10, 30, 0) | 0 || LocalDateTime.of(2020, 2, 3, 10, 30, 0)
            LocalDateTime.of(2020, 2, 27, 10, 30, 0) | 3 || LocalDateTime.of(2020, 3, 1, 10, 30, 0)
            LocalDateTime.of(2020, 2, 3, 10, 30, 0) | -5 || LocalDateTime.of(2020, 1, 29, 10, 30, 0)
            LocalDateTime.of(2019, 12, 31, 23, 0, 0) | 1 || LocalDateTime.of(2020, 1, 1, 23, 0, 0)
    }

    def "select dateAdd(Expression<LocalDateTime>, Integer)"() {
        when:
            def result = session.select(DSL.dateAdd(DSL.constant(LocalDateTime.of(2020, 2, 3, 10, 30, 0)), 3)).fetchSingle()

        then:
            result == LocalDateTime.of(2020, 2, 6, 10, 30, 0)
            result instanceof LocalDateTime

        where:
            session << allSessions
    }

    def "select dateAdd(LocalDateTime, Expression<Integer>)"() {
        when:
            def result = session.select(DSL.dateAdd(LocalDateTime.of(2020, 2, 3, 10, 30, 0), DSL.constant(3))).fetchSingle()

        then:
            result == LocalDateTime.of(2020, 2, 6, 10, 30, 0)
            result instanceof LocalDateTime

        where:
            session << allSessions
    }

    def "select dateAdd(LocalDateTime, Integer)"() {
        when:
            def result = session.select(DSL.dateAdd(LocalDateTime.of(2020, 2, 3, 10, 30, 0), 3)).fetchSingle()

        then:
            result == LocalDateTime.of(2020, 2, 6, 10, 30, 0)
            result instanceof LocalDateTime

        where:
            session << allSessions
    }

    def "select dateAdd(Expression<OffsetDateTime>, Expression<Integer>)"() {
        when:
            def result = session.select(DSL.dateAdd(DSL.constant(dt), DSL.constant(days))).fetchSingle()

        then:
            result == expected
            result instanceof OffsetDateTime

        where:
            session << getSessionsExcept(DbType.MARIADB, DbType.MYSQL)

        combined:
            dt | days || expected
            OffsetDateTime.of(2020, 2, 3, 10, 30, 0, 0, ZoneOffset.UTC) | 3 || OffsetDateTime.of(2020, 2, 6, 10, 30, 0, 0, ZoneOffset.UTC)
            OffsetDateTime.of(2020, 2, 3, 10, 30, 0, 0, ZoneOffset.UTC) | 0 || OffsetDateTime.of(2020, 2, 3, 10, 30, 0, 0, ZoneOffset.UTC)
            OffsetDateTime.of(2020, 2, 27, 10, 30, 0, 0, ZoneOffset.UTC) | 3 || OffsetDateTime.of(2020, 3, 1, 10, 30, 0, 0, ZoneOffset.UTC)
            OffsetDateTime.of(2020, 2, 3, 10, 30, 0, 0, ZoneOffset.UTC) | -5 || OffsetDateTime.of(2020, 1, 29, 10, 30, 0, 0, ZoneOffset.UTC)
            OffsetDateTime.of(2019, 12, 31, 23, 0, 0, 0, ZoneOffset.UTC) | 1 || OffsetDateTime.of(2020, 1, 1, 23, 0, 0, 0, ZoneOffset.UTC)
    }

    def "select dateAdd(Expression<OffsetDateTime>, Integer)"() {
        when:
            def result = session.select(DSL.dateAdd(DSL.constant(OffsetDateTime.of(2020, 2, 3, 10, 30, 0, 0, ZoneOffset.UTC)), 3)).fetchSingle()

        then:
            result == OffsetDateTime.of(2020, 2, 6, 10, 30, 0, 0, ZoneOffset.UTC)
            result instanceof OffsetDateTime

        where:
            session << getSessionsExcept(DbType.MARIADB, DbType.MYSQL)
    }

    def "select dateAdd(OffsetDateTime, Expression<Integer>)"() {
        when:
            def result = session.select(DSL.dateAdd(OffsetDateTime.of(2020, 2, 3, 10, 30, 0, 0, ZoneOffset.UTC), DSL.constant(3))).fetchSingle()

        then:
            result == OffsetDateTime.of(2020, 2, 6, 10, 30, 0, 0, ZoneOffset.UTC)
            result instanceof OffsetDateTime

        where:
            session << getSessionsExcept(DbType.MARIADB, DbType.MYSQL)
    }

    def "select dateAdd(OffsetDateTime, Integer)"() {
        when:
            def result = session.select(DSL.dateAdd(OffsetDateTime.of(2020, 2, 3, 10, 30, 0, 0, ZoneOffset.UTC), 3)).fetchSingle()

        then:
            result == OffsetDateTime.of(2020, 2, 6, 10, 30, 0, 0, ZoneOffset.UTC)
            result instanceof OffsetDateTime

        where:
            session << getSessionsExcept(DbType.MARIADB, DbType.MYSQL)
    }

    def "select dateSub(Expression, Expression)"() {
        when:
            def result = session.select(DSL.dateSub(DSL.date(dateStr), DSL.constant(days))).fetchSingle()

        then:
            result == expected
            result instanceof LocalDate

        where:
            session << allSessions

        combined:
            dateStr | days || expected
            '2020-02-03' | 3 || LocalDate.of(2020, 1, 31)
            '2020-02-03' | 0 || LocalDate.of(2020, 2, 3)
            '2020-03-01' | 3 || LocalDate.of(2020, 2, 27)
            '2020-02-03' | -5 || LocalDate.of(2020, 2, 8)
            '2020-01-01' | 1 || LocalDate.of(2019, 12, 31)
    }

    def "select dateSub(Expression, Integer)"() {
        when:
            def result = session.select(DSL.dateSub(DSL.date('2020-02-03'), 3)).fetchSingle()

        then:
            result == LocalDate.of(2020, 1, 31)
            result instanceof LocalDate

        where:
            session << allSessions
    }

    def "select dateSub(LocalDate, Expression)"() {
        when:
            def result = session.select(DSL.dateSub(LocalDate.of(2020, 2, 3), DSL.constant(3))).fetchSingle()

        then:
            result == LocalDate.of(2020, 1, 31)
            result instanceof LocalDate

        where:
            session << allSessions
    }

    def "select dateSub(LocalDate, Integer)"() {
        when:
            def result = session.select(DSL.dateSub(LocalDate.of(2020, 2, 3), 3)).fetchSingle()

        then:
            result == LocalDate.of(2020, 1, 31)
            result instanceof LocalDate

        where:
            session << allSessions
    }

    def "select dateSub(Expression<LocalDateTime>, Interval)"() {
        when:
            def result = session.select(DSL.dateSub(DSL.constant(datetime), interval)).fetchSingle()

        then:
            result == expected
            result instanceof LocalDateTime

        where:
            session << allSessions

        combined:
            datetime | interval || expected
            LocalDateTime.of(2022, 1, 15, 10, 30, 0) | Interval.years(2) || LocalDateTime.of(2020, 1, 15, 10, 30, 0)
            LocalDateTime.of(2020, 4, 15, 10, 30, 0) | Interval.months(3) || LocalDateTime.of(2020, 1, 15, 10, 30, 0)
            LocalDateTime.of(2019, 12, 15, 10, 30, 0) | Interval.months(-1) || LocalDateTime.of(2020, 1, 15, 10, 30, 0)
            LocalDateTime.of(2020, 1, 29, 10, 30, 0) | Interval.weeks(2) || LocalDateTime.of(2020, 1, 15, 10, 30, 0)
            LocalDateTime.of(2020, 1, 25, 10, 30, 0) | Interval.days(10) || LocalDateTime.of(2020, 1, 15, 10, 30, 0)
            LocalDateTime.of(2020, 1, 15, 10, 30, 0) | Interval.days(0) || LocalDateTime.of(2020, 1, 15, 10, 30, 0)
            LocalDateTime.of(2020, 2, 29, 10, 30, 0) | Interval.days(-1) || LocalDateTime.of(2020, 3, 1, 10, 30, 0)
            LocalDateTime.of(2020, 1, 15, 12, 30, 0) | Interval.hours(2) || LocalDateTime.of(2020, 1, 15, 10, 30, 0)
            LocalDateTime.of(2020, 1, 15, 7, 30, 0) | Interval.hours(-3) || LocalDateTime.of(2020, 1, 15, 10, 30, 0)
            LocalDateTime.of(2020, 1, 15, 11, 15, 0) | Interval.minutes(45) || LocalDateTime.of(2020, 1, 15, 10, 30, 0)
            LocalDateTime.of(2020, 1, 16, 0, 0, 5) | Interval.seconds(15) || LocalDateTime.of(2020, 1, 15, 23, 59, 50)
    }

    // Sub-second tests: MariaDB's CAST(? AS DATETIME) is precision 0, which truncates
    // the LocalDateTime constant's fractional seconds before the interval is subtracted,
    // independent of the IntervalConstant rendering. Exclude MariaDB here.
    def "select dateSub(Expression<LocalDateTime>, Interval) sub-second"() {
        when:
            def result = session.select(DSL.dateSub(DSL.constant(datetime), interval)).fetchSingle()

        then:
            result == expected
            result instanceof LocalDateTime

        where:
            session << getSessionsExcept(DbType.MARIADB, DbType.MYSQL)

        combined:
            datetime | interval || expected
            LocalDateTime.of(2020, 1, 15, 10, 30, 0, 500_000_000) | Interval.milliseconds(500) || LocalDateTime.of(2020, 1, 15, 10, 30, 0, 0)
            LocalDateTime.of(2020, 1, 15, 10, 30, 1, 500_000_000) | Interval.milliseconds(1500) || LocalDateTime.of(2020, 1, 15, 10, 30, 0, 0)
            LocalDateTime.of(2020, 1, 15, 10, 30, 0, 250_000_000) | Interval.microseconds(250_000) || LocalDateTime.of(2020, 1, 15, 10, 30, 0, 0)
    }

    def "select dateSub(Expression<LocalDateTime>, Expression<Integer>)"() {
        when:
            def result = session.select(DSL.dateSub(DSL.constant(dt), DSL.constant(days))).fetchSingle()

        then:
            result == expected
            result instanceof LocalDateTime

        where:
            session << allSessions

        combined:
            dt | days || expected
            LocalDateTime.of(2020, 2, 3, 10, 30, 0) | 3 || LocalDateTime.of(2020, 1, 31, 10, 30, 0)
            LocalDateTime.of(2020, 2, 3, 10, 30, 0) | 0 || LocalDateTime.of(2020, 2, 3, 10, 30, 0)
            LocalDateTime.of(2020, 3, 1, 10, 30, 0) | 3 || LocalDateTime.of(2020, 2, 27, 10, 30, 0)
            LocalDateTime.of(2020, 2, 3, 10, 30, 0) | -5 || LocalDateTime.of(2020, 2, 8, 10, 30, 0)
            LocalDateTime.of(2020, 1, 1, 23, 0, 0) | 1 || LocalDateTime.of(2019, 12, 31, 23, 0, 0)
    }

    def "select dateSub(Expression<LocalDateTime>, Integer)"() {
        when:
            def result = session.select(DSL.dateSub(DSL.constant(LocalDateTime.of(2020, 2, 3, 10, 30, 0)), 3)).fetchSingle()

        then:
            result == LocalDateTime.of(2020, 1, 31, 10, 30, 0)
            result instanceof LocalDateTime

        where:
            session << allSessions
    }

    def "select dateSub(LocalDateTime, Expression<Integer>)"() {
        when:
            def result = session.select(DSL.dateSub(LocalDateTime.of(2020, 2, 3, 10, 30, 0), DSL.constant(3))).fetchSingle()

        then:
            result == LocalDateTime.of(2020, 1, 31, 10, 30, 0)
            result instanceof LocalDateTime

        where:
            session << allSessions
    }

    def "select dateSub(LocalDateTime, Integer)"() {
        when:
            def result = session.select(DSL.dateSub(LocalDateTime.of(2020, 2, 3, 10, 30, 0), 3)).fetchSingle()

        then:
            result == LocalDateTime.of(2020, 1, 31, 10, 30, 0)
            result instanceof LocalDateTime

        where:
            session << allSessions
    }

    def "select dateSub(Expression<OffsetDateTime>, Expression<Integer>)"() {
        when:
            def result = session.select(DSL.dateSub(DSL.constant(dt), DSL.constant(days))).fetchSingle()

        then:
            result == expected
            result instanceof OffsetDateTime

        where:
            session << getSessionsExcept(DbType.MARIADB, DbType.MYSQL)

        combined:
            dt | days || expected
            OffsetDateTime.of(2020, 2, 3, 10, 30, 0, 0, ZoneOffset.UTC) | 3 || OffsetDateTime.of(2020, 1, 31, 10, 30, 0, 0, ZoneOffset.UTC)
            OffsetDateTime.of(2020, 2, 3, 10, 30, 0, 0, ZoneOffset.UTC) | 0 || OffsetDateTime.of(2020, 2, 3, 10, 30, 0, 0, ZoneOffset.UTC)
            OffsetDateTime.of(2020, 3, 1, 10, 30, 0, 0, ZoneOffset.UTC) | 3 || OffsetDateTime.of(2020, 2, 27, 10, 30, 0, 0, ZoneOffset.UTC)
            OffsetDateTime.of(2020, 2, 3, 10, 30, 0, 0, ZoneOffset.UTC) | -5 || OffsetDateTime.of(2020, 2, 8, 10, 30, 0, 0, ZoneOffset.UTC)
            OffsetDateTime.of(2020, 1, 1, 23, 0, 0, 0, ZoneOffset.UTC) | 1 || OffsetDateTime.of(2019, 12, 31, 23, 0, 0, 0, ZoneOffset.UTC)
    }

    def "select dateSub(Expression<OffsetDateTime>, Integer)"() {
        when:
            def result = session.select(DSL.dateSub(DSL.constant(OffsetDateTime.of(2020, 2, 3, 10, 30, 0, 0, ZoneOffset.UTC)), 3)).fetchSingle()

        then:
            result == OffsetDateTime.of(2020, 1, 31, 10, 30, 0, 0, ZoneOffset.UTC)
            result instanceof OffsetDateTime

        where:
            session << getSessionsExcept(DbType.MARIADB, DbType.MYSQL)
    }

    def "select dateSub(OffsetDateTime, Expression<Integer>)"() {
        when:
            def result = session.select(DSL.dateSub(OffsetDateTime.of(2020, 2, 3, 10, 30, 0, 0, ZoneOffset.UTC), DSL.constant(3))).fetchSingle()

        then:
            result == OffsetDateTime.of(2020, 1, 31, 10, 30, 0, 0, ZoneOffset.UTC)
            result instanceof OffsetDateTime

        where:
            session << getSessionsExcept(DbType.MARIADB, DbType.MYSQL)
    }

    def "select dateSub(OffsetDateTime, Integer)"() {
        when:
            def result = session.select(DSL.dateSub(OffsetDateTime.of(2020, 2, 3, 10, 30, 0, 0, ZoneOffset.UTC), 3)).fetchSingle()

        then:
            result == OffsetDateTime.of(2020, 1, 31, 10, 30, 0, 0, ZoneOffset.UTC)
            result instanceof OffsetDateTime

        where:
            session << getSessionsExcept(DbType.MARIADB, DbType.MYSQL)
    }

    def "select dateDiff(Expression, Expression)"() {
        when:
            def result = session.select(DSL.dateDiff(DSL.date(date1Str), DSL.date(date2Str))).fetchSingle()

        then:
            result == expected
            result instanceof Integer

        where:
            session << allSessions

        combined:
            date1Str | date2Str || expected
            '2020-02-03' | '2020-02-01' || 2
            '2020-02-01' | '2020-02-03' || -2
            '2020-02-03' | '2020-02-03' || 0
            '2020-03-01' | '2020-02-27' || 3
            '2020-01-01' | '2019-12-31' || 1
            '2021-01-01' | '2020-01-01' || 366
    }

    def "select dateDiff(Expression, LocalDate)"() {
        when:
            def result = session.select(DSL.dateDiff(DSL.date('2020-02-03'), LocalDate.of(2020, 2, 1))).fetchSingle()

        then:
            result == 2
            result instanceof Integer

        where:
            session << allSessions
    }

    def "select dateDiff(LocalDate, Expression)"() {
        when:
            def result = session.select(DSL.dateDiff(LocalDate.of(2020, 2, 3), DSL.date('2020-02-01'))).fetchSingle()

        then:
            result == 2
            result instanceof Integer

        where:
            session << allSessions
    }

    def "select dateDiff(LocalDate, LocalDate)"() {
        when:
            def result = session.select(DSL.dateDiff(LocalDate.of(2020, 2, 3), LocalDate.of(2020, 2, 1))).fetchSingle()

        then:
            result == 2
            result instanceof Integer

        where:
            session << allSessions
    }

    def "select extractDay(Expression<LocalDate>)"() {
        when:
            def result = session.select(DSL.extractDay(DSL.constant(date))).fetchSingle()

        then:
            result == expected
            result instanceof Integer

        where:
            session << allSessions

        combined:
            date || expected
            LocalDate.of(2020, 2, 3) || 3
            LocalDate.of(2020, 1, 1) || 1
            LocalDate.of(2019, 12, 31) || 31
            LocalDate.of(2020, 2, 29) || 29
    }

    def "select extractDay(Expression<LocalDateTime>)"() {
        when:
            def result = session.select(DSL.extractDay(DSL.constant(dt))).fetchSingle()

        then:
            result == expected
            result instanceof Integer

        where:
            session << allSessions

        combined:
            dt || expected
            LocalDateTime.of(2020, 2, 3, 0, 0, 0) || 3
            LocalDateTime.of(2020, 1, 1, 23, 59, 59) || 1
            LocalDateTime.of(2019, 12, 31, 12, 0, 0) || 31
            LocalDateTime.of(2020, 6, 15, 10, 30, 45) || 15
    }

    def "select extractDay(Expression<OffsetDateTime>)"() {
        when:
            def result = session.select(DSL.extractDay(DSL.constant(dt))).fetchSingle()

        then:
            result == expected
            result instanceof Integer

        where:
            session << allSessions

        combined:
            dt || expected
            OffsetDateTime.of(2020, 2, 3, 12, 0, 0, 0, ZoneOffset.UTC) || 3
            OffsetDateTime.of(2020, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC) || 1
            OffsetDateTime.of(2019, 12, 31, 12, 0, 0, 0, ZoneOffset.UTC) || 31
            OffsetDateTime.of(2020, 6, 15, 10, 30, 45, 0, ZoneOffset.ofHours(2)) || 15
    }

    def "select extractDay(LocalDate)"() {
        when:
            def result = session.select(DSL.extractDay(LocalDate.of(2020, 2, 3))).fetchSingle()

        then:
            result == 3
            result instanceof Integer

        where:
            session << allSessions
    }

    def "select extractDay(LocalDateTime)"() {
        when:
            def result = session.select(DSL.extractDay(LocalDateTime.of(2020, 2, 3, 10, 30, 0))).fetchSingle()

        then:
            result == 3
            result instanceof Integer

        where:
            session << allSessions
    }

    def "select extractDay(OffsetDateTime)"() {
        when:
            def result = session.select(DSL.extractDay(OffsetDateTime.of(2020, 2, 3, 10, 30, 0, 0, ZoneOffset.UTC))).fetchSingle()

        then:
            result == 3
            result instanceof Integer

        where:
            session << allSessions
    }

    def "EXTRACT DAY"() {
        when:
            def result = session.select(DSL.day(DSL.name(Date.class, "event_date")))
                    .from("events")
                    .where(DSL.name(Long.class, "id").eq(1L))
                    .fetchSingle()

        then:
            result == 15

        where:
            session << allSessions
    }

    def "select extractDayOfYear(Expression<LocalDate>)"() {
        when:
            def result = session.select(DSL.extractDayOfYear(DSL.constant(date))).fetchSingle()

        then:
            result == expected
            result instanceof Integer

        where:
            session << allSessions

        combined:
            date || expected
            LocalDate.of(2020, 2, 3) || 34
            LocalDate.of(2020, 1, 1) || 1
            LocalDate.of(2019, 12, 31) || 365
            LocalDate.of(2020, 12, 31) || 366
    }

    def "select extractDayOfYear(Expression<LocalDateTime>)"() {
        when:
            def result = session.select(DSL.extractDayOfYear(DSL.constant(dt))).fetchSingle()

        then:
            result == expected
            result instanceof Integer

        where:
            session << allSessions

        combined:
            dt || expected
            LocalDateTime.of(2020, 2, 3, 0, 0, 0) || 34
            LocalDateTime.of(2020, 1, 1, 23, 59, 59) || 1
            LocalDateTime.of(2019, 12, 31, 12, 0, 0) || 365
            LocalDateTime.of(2020, 6, 15, 10, 30, 45) || 167
    }

    def "select extractDayOfYear(Expression<OffsetDateTime>)"() {
        when:
            def result = session.select(DSL.extractDayOfYear(DSL.constant(dt))).fetchSingle()

        then:
            result == expected
            result instanceof Integer

        where:
            session << allSessions

        combined:
            dt || expected
            OffsetDateTime.of(2020, 2, 3, 12, 0, 0, 0, ZoneOffset.UTC) || 34
            OffsetDateTime.of(2020, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC) || 1
            OffsetDateTime.of(2019, 12, 31, 12, 0, 0, 0, ZoneOffset.UTC) || 365
            OffsetDateTime.of(2020, 6, 15, 10, 30, 45, 0, ZoneOffset.ofHours(2)) || 167
    }

    def "select extractDayOfYear(LocalDate)"() {
        when:
            def result = session.select(DSL.extractDayOfYear(LocalDate.of(2020, 2, 3))).fetchSingle()

        then:
            result == 34
            result instanceof Integer

        where:
            session << allSessions
    }

    def "select extractDayOfYear(LocalDateTime)"() {
        when:
            def result = session.select(DSL.extractDayOfYear(LocalDateTime.of(2020, 2, 3, 10, 30, 0))).fetchSingle()

        then:
            result == 34
            result instanceof Integer

        where:
            session << allSessions
    }

    def "select extractDayOfYear(OffsetDateTime)"() {
        when:
            def result = session.select(DSL.extractDayOfYear(OffsetDateTime.of(2020, 2, 3, 10, 30, 0, 0, ZoneOffset.UTC))).fetchSingle()

        then:
            result == 34
            result instanceof Integer

        where:
            session << allSessions
    }

    def "select isoDayOfWeek(Expression<LocalDate>)"() {
        when:
            def result = session.select(DSL.extractIsoDayOfWeek(DSL.constant(date))).fetchSingle()

        then:
            result == expected
            result instanceof Integer

        where:
            session << allSessions

        combined:
            date || expected
            LocalDate.of(2020, 2, 3) || 1
            LocalDate.of(2020, 2, 4) || 2
            LocalDate.of(2020, 2, 5) || 3
            LocalDate.of(2020, 2, 6) || 4
            LocalDate.of(2020, 2, 7) || 5
            LocalDate.of(2020, 2, 8) || 6
            LocalDate.of(2020, 2, 9) || 7
    }

    def "select isoDayOfWeek(Expression<LocalDateTime>)"() {
        when:
            def result = session.select(DSL.extractIsoDayOfWeek(DSL.constant(dt))).fetchSingle()

        then:
            result == expected
            result instanceof Integer

        where:
            session << allSessions

        combined:
            dt || expected
            LocalDateTime.of(2020, 2, 3, 0, 0, 0) || 1
            LocalDateTime.of(2020, 2, 5, 23, 59, 59) || 3
            LocalDateTime.of(2020, 2, 9, 12, 0, 0) || 7
    }

    def "select isoDayOfWeek(Expression<OffsetDateTime>)"() {
        when:
            def result = session.select(DSL.extractIsoDayOfWeek(DSL.constant(dt))).fetchSingle()

        then:
            result == expected
            result instanceof Integer

        where:
            session << allSessions

        combined:
            dt || expected
            OffsetDateTime.of(2020, 2, 3, 12, 0, 0, 0, ZoneOffset.UTC) || 1
            OffsetDateTime.of(2020, 2, 5, 12, 0, 0, 0, ZoneOffset.UTC) || 3
            OffsetDateTime.of(2020, 2, 9, 10, 30, 45, 0, ZoneOffset.ofHours(2)) || 7
    }

    def "select isoDayOfWeek(LocalDate)"() {
        when:
            def result = session.select(DSL.extractIsoDayOfWeek(LocalDate.of(2020, 2, 3))).fetchSingle()

        then:
            result == 1
            result instanceof Integer

        where:
            session << allSessions
    }

    def "select isoDayOfWeek(LocalDateTime)"() {
        when:
            def result = session.select(DSL.extractIsoDayOfWeek(LocalDateTime.of(2020, 2, 3, 10, 30, 0))).fetchSingle()

        then:
            result == 1
            result instanceof Integer

        where:
            session << allSessions
    }

    def "select isoDayOfWeek(OffsetDateTime)"() {
        when:
            def result = session.select(DSL.extractIsoDayOfWeek(OffsetDateTime.of(2020, 2, 3, 10, 30, 0, 0, ZoneOffset.UTC))).fetchSingle()

        then:
            result == 1
            result instanceof Integer

        where:
            session << allSessions
    }

    def "select extractHour(Expression<LocalDateTime>)"() {
        when:
            def result = session.select(DSL.extractHour(DSL.constant(dt))).fetchSingle()

        then:
            result == expected
            result instanceof Integer

        where:
            session << allSessions

        combined:
            dt || expected
            LocalDateTime.of(2020, 2, 3, 15, 30, 45) || 15
            LocalDateTime.of(2020, 1, 1, 0, 0, 0) || 0
            LocalDateTime.of(2019, 12, 31, 23, 59, 59) || 23
            LocalDateTime.of(2020, 6, 15, 12, 0, 0) || 12
    }

    def "select extractHour(Expression<OffsetDateTime>)"() {
        given:
            def localDt = LocalDateTime.of(year, month, day, hour, 30, 45)
            def dt = localDt.atZone(ZoneId.systemDefault()).toOffsetDateTime()

        when:
            def result = session.select(DSL.extractHour(DSL.constant(dt))).fetchSingle()

        then:
            result == hour
            result instanceof Integer

        where:
            session << allSessions

        combined:
            year | month | day | hour
            2020 | 2 | 3 | 15
            2020 | 1 | 1 | 0
            2019 | 12 | 31 | 23
            2020 | 6 | 15 | 12
    }

    def "select extractHour(LocalDateTime)"() {
        when:
            def result = session.select(DSL.extractHour(LocalDateTime.of(2020, 2, 3, 15, 30, 45))).fetchSingle()

        then:
            result == 15
            result instanceof Integer

        where:
            session << allSessions
    }

    def "select extractHour(OffsetDateTime)"() {
        given:
            def dt = LocalDateTime.of(2020, 2, 3, 15, 30, 45).atZone(ZoneId.systemDefault()).toOffsetDateTime()

        when:
            def result = session.select(DSL.extractHour(dt)).fetchSingle()

        then:
            result == 15
            result instanceof Integer

        where:
            session << allSessions
    }

    def "EXTRACT HOUR"() {
        when:
            def result = session.select(DSL.hour(DSL.name(Time.class, "event_time")))
                    .from("events")
                    .where(DSL.name(Long.class, "id").eq(1L))
                    .fetchSingle()

        then:
            result == 14

        where:
            session << allSessions
    }

    def "select extractMinute(Expression<LocalDateTime>)"() {
        when:
            def result = session.select(DSL.extractMinute(DSL.constant(dt))).fetchSingle()

        then:
            result == expected
            result instanceof Integer

        where:
            session << allSessions

        combined:
            dt || expected
            LocalDateTime.of(2020, 2, 3, 15, 30, 45) || 30
            LocalDateTime.of(2020, 1, 1, 0, 0, 0) || 0
            LocalDateTime.of(2019, 12, 31, 23, 59, 59) || 59
            LocalDateTime.of(2020, 6, 15, 12, 45, 0) || 45
    }

    def "select extractMinute(Expression<OffsetDateTime>)"() {
        given:
            def localDt = LocalDateTime.of(year, month, day, hour, minute, 45)
            def dt = localDt.atZone(ZoneId.systemDefault()).toOffsetDateTime()

        when:
            def result = session.select(DSL.extractMinute(DSL.constant(dt))).fetchSingle()

        then:
            result == minute
            result instanceof Integer

        where:
            session << allSessions

        combined:
            year | month | day | hour | minute
            2020 | 2 | 3 | 15 | 30
            2020 | 1 | 1 | 0 | 0
            2019 | 12 | 31 | 23 | 59
            2020 | 6 | 15 | 12 | 45
    }

    def "select extractMinute(LocalDateTime)"() {
        when:
            def result = session.select(DSL.extractMinute(LocalDateTime.of(2020, 2, 3, 15, 30, 45))).fetchSingle()

        then:
            result == 30
            result instanceof Integer

        where:
            session << allSessions
    }

    def "select extractMinute(OffsetDateTime)"() {
        given:
            def dt = LocalDateTime.of(2020, 2, 3, 15, 30, 45).atZone(ZoneId.systemDefault()).toOffsetDateTime()

        when:
            def result = session.select(DSL.extractMinute(dt)).fetchSingle()

        then:
            result == 30
            result instanceof Integer

        where:
            session << allSessions
    }

    def "EXTRACT MINUTE"() {
        when:
            def result = session.select(DSL.minute(DSL.name(Time.class, "event_time")))
                    .from("events")
                    .where(DSL.name(Long.class, "id").eq(1L))
                    .fetchSingle()

        then:
            result == 30

        where:
            session << allSessions
    }

    def "select extractMonth(Expression<LocalDate>)"() {
        when:
            def result = session.select(DSL.extractMonth(DSL.constant(date))).fetchSingle()

        then:
            result == expected
            result instanceof Integer

        where:
            session << allSessions

        combined:
            date || expected
            LocalDate.of(2020, 2, 3) || 2
            LocalDate.of(2020, 1, 1) || 1
            LocalDate.of(2019, 12, 31) || 12
            LocalDate.of(2020, 6, 15) || 6
    }

    def "select extractMonth(Expression<LocalDateTime>)"() {
        when:
            def result = session.select(DSL.extractMonth(DSL.constant(dt))).fetchSingle()

        then:
            result == expected
            result instanceof Integer

        where:
            session << allSessions

        combined:
            dt || expected
            LocalDateTime.of(2020, 2, 3, 0, 0, 0) || 2
            LocalDateTime.of(2020, 1, 1, 23, 59, 59) || 1
            LocalDateTime.of(2019, 12, 31, 12, 0, 0) || 12
            LocalDateTime.of(2020, 6, 15, 10, 30, 45) || 6
    }

    def "select extractMonth(Expression<OffsetDateTime>)"() {
        when:
            def result = session.select(DSL.extractMonth(DSL.constant(dt))).fetchSingle()

        then:
            result == expected
            result instanceof Integer

        where:
            session << allSessions

        combined:
            dt || expected
            OffsetDateTime.of(2020, 2, 3, 12, 0, 0, 0, ZoneOffset.UTC) || 2
            OffsetDateTime.of(2020, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC) || 1
            OffsetDateTime.of(2019, 12, 31, 12, 0, 0, 0, ZoneOffset.UTC) || 12
            OffsetDateTime.of(2020, 6, 15, 10, 30, 45, 0, ZoneOffset.ofHours(2)) || 6
    }

    def "select extractMonth(LocalDate)"() {
        when:
            def result = session.select(DSL.extractMonth(LocalDate.of(2020, 2, 3))).fetchSingle()

        then:
            result == 2
            result instanceof Integer

        where:
            session << allSessions
    }

    def "select extractMonth(LocalDateTime)"() {
        when:
            def result = session.select(DSL.extractMonth(LocalDateTime.of(2020, 2, 3, 10, 30, 0))).fetchSingle()

        then:
            result == 2
            result instanceof Integer

        where:
            session << allSessions
    }

    def "select extractMonth(OffsetDateTime)"() {
        when:
            def result = session.select(DSL.extractMonth(OffsetDateTime.of(2020, 2, 3, 10, 30, 0, 0, ZoneOffset.UTC))).fetchSingle()

        then:
            result == 2
            result instanceof Integer

        where:
            session << allSessions
    }

    def "EXTRACT MONTH"() {
        when:
            def result = session.select(DSL.month(DSL.name(Date.class, "event_date")))
                    .from("events")
                    .where(DSL.name(Long.class, "id").eq(1L))
                    .fetchSingle()

        then:
            result == 6

        where:
            session << allSessions
    }

    def "select extractQuarter(Expression<LocalDate>)"() {
        when:
            def result = session.select(DSL.extractQuarter(DSL.constant(date))).fetchSingle()

        then:
            result == expected
            result instanceof Integer

        where:
            session << allSessions

        combined:
            date || expected
            LocalDate.of(2020, 2, 3) || 1
            LocalDate.of(2020, 1, 1) || 1
            LocalDate.of(2020, 3, 31) || 1
            LocalDate.of(2020, 4, 1) || 2
            LocalDate.of(2020, 6, 30) || 2
            LocalDate.of(2020, 7, 1) || 3
            LocalDate.of(2020, 9, 30) || 3
            LocalDate.of(2020, 10, 1) || 4
            LocalDate.of(2019, 12, 31) || 4
    }

    def "select extractQuarter(Expression<LocalDateTime>)"() {
        when:
            def result = session.select(DSL.extractQuarter(DSL.constant(dt))).fetchSingle()

        then:
            result == expected
            result instanceof Integer

        where:
            session << allSessions

        combined:
            dt || expected
            LocalDateTime.of(2020, 2, 3, 0, 0, 0) || 1
            LocalDateTime.of(2020, 1, 1, 23, 59, 59) || 1
            LocalDateTime.of(2020, 4, 1, 12, 0, 0) || 2
            LocalDateTime.of(2020, 6, 15, 10, 30, 45) || 2
            LocalDateTime.of(2020, 9, 30, 23, 59, 59) || 3
            LocalDateTime.of(2019, 12, 31, 12, 0, 0) || 4
    }

    def "select extractQuarter(Expression<OffsetDateTime>)"() {
        when:
            def result = session.select(DSL.extractQuarter(DSL.constant(dt))).fetchSingle()

        then:
            result == expected
            result instanceof Integer

        where:
            session << allSessions

        combined:
            dt || expected
            OffsetDateTime.of(2020, 2, 3, 12, 0, 0, 0, ZoneOffset.UTC) || 1
            OffsetDateTime.of(2020, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC) || 1
            OffsetDateTime.of(2020, 4, 1, 12, 0, 0, 0, ZoneOffset.UTC) || 2
            OffsetDateTime.of(2020, 6, 15, 10, 30, 45, 0, ZoneOffset.ofHours(2)) || 2
            OffsetDateTime.of(2020, 9, 30, 12, 0, 0, 0, ZoneOffset.UTC) || 3
            OffsetDateTime.of(2019, 12, 31, 12, 0, 0, 0, ZoneOffset.UTC) || 4
    }

    def "select extractQuarter(LocalDate)"() {
        when:
            def result = session.select(DSL.extractQuarter(LocalDate.of(2020, 2, 3))).fetchSingle()

        then:
            result == 1
            result instanceof Integer

        where:
            session << allSessions
    }

    def "select extractQuarter(LocalDateTime)"() {
        when:
            def result = session.select(DSL.extractQuarter(LocalDateTime.of(2020, 2, 3, 10, 30, 0))).fetchSingle()

        then:
            result == 1
            result instanceof Integer

        where:
            session << allSessions
    }

    def "select extractQuarter(OffsetDateTime)"() {
        when:
            def result = session.select(DSL.extractQuarter(OffsetDateTime.of(2020, 2, 3, 10, 30, 0, 0, ZoneOffset.UTC))).fetchSingle()

        then:
            result == 1
            result instanceof Integer

        where:
            session << allSessions
    }

    def "select extractSecond(Expression<LocalDateTime>)"() {
        when:
            def result = session.select(DSL.extractSecond(DSL.constant(dt))).fetchSingle()

        then:
            result == expected
            result instanceof Integer

        where:
            session << allSessions

        combined:
            dt || expected
            LocalDateTime.of(2020, 2, 3, 15, 30, 45) || 45
            LocalDateTime.of(2020, 1, 1, 0, 0, 0) || 0
            LocalDateTime.of(2019, 12, 31, 23, 59, 59) || 59
            LocalDateTime.of(2020, 6, 15, 12, 45, 30) || 30
    }

    def "select extractSecond(Expression<OffsetDateTime>)"() {
        given:
            def localDt = LocalDateTime.of(year, month, day, hour, 30, second)
            def dt = localDt.atZone(ZoneId.systemDefault()).toOffsetDateTime()

        when:
            def result = session.select(DSL.extractSecond(DSL.constant(dt))).fetchSingle()

        then:
            result == second
            result instanceof Integer

        where:
            session << allSessions

        combined:
            year | month | day | hour | second
            2020 | 2 | 3 | 15 | 45
            2020 | 1 | 1 | 0 | 0
            2019 | 12 | 31 | 23 | 59
            2020 | 6 | 15 | 12 | 30
    }

    def "select extractSecond(LocalDateTime)"() {
        when:
            def result = session.select(DSL.extractSecond(LocalDateTime.of(2020, 2, 3, 15, 30, 45))).fetchSingle()

        then:
            result == 45
            result instanceof Integer

        where:
            session << allSessions
    }

    def "select extractSecond(OffsetDateTime)"() {
        given:
            def dt = LocalDateTime.of(2020, 2, 3, 15, 30, 45).atZone(ZoneId.systemDefault()).toOffsetDateTime()

        when:
            def result = session.select(DSL.extractSecond(dt)).fetchSingle()

        then:
            result == 45
            result instanceof Integer

        where:
            session << allSessions
    }

    def "EXTRACT SECOND"() {
        when:
            def result = session.select(DSL.second(DSL.name(Time.class, "event_time")))
                    .from("events")
                    .where(DSL.name(Long.class, "id").eq(1L))
                    .fetchSingle()

        then:
            result == 45

        where:
            session << allSessions
    }

    def "select extractYear(Expression<LocalDate>)"() {
        when:
            def result = session.select(DSL.extractYear(DSL.constant(date))).fetchSingle()

        then:
            result == expected
            result instanceof Integer

        where:
            session << allSessions

        combined:
            date || expected
            LocalDate.of(2020, 2, 3) || 2020
            LocalDate.of(2019, 12, 31) || 2019
            LocalDate.of(1999, 1, 1) || 1999
            LocalDate.of(2024, 6, 15) || 2024
    }

    def "select extractYear(Expression<LocalDateTime>)"() {
        when:
            def result = session.select(DSL.extractYear(DSL.constant(dt))).fetchSingle()

        then:
            result == expected
            result instanceof Integer

        where:
            session << allSessions

        combined:
            dt || expected
            LocalDateTime.of(2020, 2, 3, 0, 0, 0) || 2020
            LocalDateTime.of(2019, 12, 31, 23, 59, 59) || 2019
            LocalDateTime.of(1999, 1, 1, 12, 0, 0) || 1999
            LocalDateTime.of(2024, 6, 15, 10, 30, 45) || 2024
    }

    def "select extractYear(Expression<OffsetDateTime>)"() {
        when:
            def result = session.select(DSL.extractYear(DSL.constant(dt))).fetchSingle()

        then:
            result == expected
            result instanceof Integer

        where:
            session << allSessions

        combined:
            dt || expected
            OffsetDateTime.of(2020, 2, 3, 12, 0, 0, 0, ZoneOffset.UTC) || 2020
            OffsetDateTime.of(2019, 6, 15, 12, 0, 0, 0, ZoneOffset.UTC) || 2019
            OffsetDateTime.of(1999, 6, 1, 12, 0, 0, 0, ZoneOffset.UTC) || 1999
            OffsetDateTime.of(2024, 6, 15, 10, 30, 45, 0, ZoneOffset.ofHours(2)) || 2024
    }

    def "select extractYear(LocalDate)"() {
        when:
            def result = session.select(DSL.extractYear(LocalDate.of(2020, 2, 3))).fetchSingle()

        then:
            result == 2020
            result instanceof Integer

        where:
            session << allSessions
    }

    def "select extractYear(LocalDateTime)"() {
        when:
            def result = session.select(DSL.extractYear(LocalDateTime.of(2020, 2, 3, 10, 30, 0))).fetchSingle()

        then:
            result == 2020
            result instanceof Integer

        where:
            session << allSessions
    }

    def "select extractYear(OffsetDateTime)"() {
        when:
            def result = session.select(DSL.extractYear(OffsetDateTime.of(2020, 2, 3, 10, 30, 0, 0, ZoneOffset.UTC))).fetchSingle()

        then:
            result == 2020
            result instanceof Integer

        where:
            session << allSessions
    }

    def "EXTRACT YEAR"() {
        when:
            def result = session.select(DSL.year(DSL.name(Date.class, "event_date")))
                    .from("events")
                    .where(DSL.name(Long.class, "id").eq(1L))
                    .fetchSingle()

        then:
            result == 2024

        where:
            session << allSessions
    }

    def "select epoch(Expression<LocalDate>)"() {
        when:
            def result = session.select(DSL.epoch(DSL.constant(date))).fetchSingle()

        then:
            result == expected
            result instanceof Long

        where:
            session << allSessions

        combined:
            date || expected
            LocalDate.of(2020, 1, 1) || 1577836800L
            LocalDate.of(2026, 6, 23) || 1782172800L
    }

    def "select epoch(Expression<LocalDateTime>)"() {
        when:
            def result = session.select(DSL.epoch(DSL.constant(dt))).fetchSingle()

        then:
            result == expected
            result instanceof Long

        where:
            session << allSessions

        combined:
            dt || expected
            LocalDateTime.of(1970, 1, 1, 0, 0, 15) || 15L
            LocalDateTime.of(2020, 1, 1, 0, 0, 0) || 1577836800L
            LocalDateTime.of(2026, 6, 23, 12, 30, 45) || 1782217845L
    }

    def "select epoch(Expression<OffsetDateTime>)"() {
        when:
            def result = session.select(DSL.epoch(DSL.constant(dt))).fetchSingle()

        then:
            result == expected
            result instanceof Long

        where:
            session << getSessionsExcept(DbType.MARIADB, DbType.MYSQL)

        combined:
            dt || expected
            OffsetDateTime.of(1970, 1, 1, 0, 0, 15, 0, ZoneOffset.UTC) || 15L
            OffsetDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC) || 1577836800L
            OffsetDateTime.of(2020, 1, 1, 2, 0, 0, 0, ZoneOffset.ofHours(2)) || 1577836800L
    }

    def "select epoch(LocalDate)"() {
        when:
            def result = session.select(DSL.epoch(LocalDate.of(2020, 1, 1))).fetchSingle()

        then:
            result == 1577836800L
            result instanceof Long

        where:
            session << allSessions
    }

    def "select epoch(LocalDateTime)"() {
        when:
            def result = session.select(DSL.epoch(LocalDateTime.of(1970, 1, 1, 0, 0, 15))).fetchSingle()

        then:
            result == 15L
            result instanceof Long

        where:
            session << allSessions
    }

    def "select epoch(OffsetDateTime)"() {
        when:
            def result = session.select(DSL.epoch(OffsetDateTime.of(1970, 1, 1, 0, 0, 15, 0, ZoneOffset.UTC))).fetchSingle()

        then:
            result == 15L
            result instanceof Long

        where:
            session << getSessionsExcept(DbType.MARIADB, DbType.MYSQL)
    }

    def "DATE_TRUNC with YEAR precision truncates to start of year"() {
        when:
            def result = session.select(
                    DSL.dateTrunc(DateTimePrecision.YEAR, DSL.name(Timestamp.class, "event_timestamp"))
            )
                    .from("events")
                    .where(DSL.name(Long.class, "id").eq(1L))
                    .fetchSingle()

        then:
            result != null
            // 2024-06-15 14:30:45 truncated to YEAR -> 2024-01-01 00:00:00
            result.toLocalDateTime().year == 2024
            result.toLocalDateTime().monthValue == 1
            result.toLocalDateTime().dayOfMonth == 1
            result.toLocalDateTime().hour == 0
            result.toLocalDateTime().minute == 0

        where:
            session << allSessions
    }

    def "DATE_TRUNC with MONTH precision truncates to start of month"() {
        when:
            def result = session.select(
                    DSL.dateTrunc(DateTimePrecision.MONTH, DSL.name(Timestamp.class, "event_timestamp"))
            )
                    .from("events")
                    .where(DSL.name(Long.class, "id").eq(1L))
                    .fetchSingle()

        then:
            result != null
            // 2024-06-15 14:30:45 truncated to MONTH -> 2024-06-01 00:00:00
            result.toLocalDateTime().year == 2024
            result.toLocalDateTime().monthValue == 6
            result.toLocalDateTime().dayOfMonth == 1
            result.toLocalDateTime().hour == 0
            result.toLocalDateTime().minute == 0

        where:
            session << allSessions
    }

    def "DATE_TRUNC with DAY precision truncates to start of day"() {
        when:
            def result = session.select(
                    DSL.dateTrunc(DateTimePrecision.DAY, DSL.name(Timestamp.class, "event_timestamp"))
            )
                    .from("events")
                    .where(DSL.name(Long.class, "id").eq(1L))
                    .fetchSingle()

        then:
            result != null
            // 2024-06-15 14:30:45 truncated to DAY -> 2024-06-15 00:00:00
            result.toLocalDateTime().year == 2024
            result.toLocalDateTime().monthValue == 6
            result.toLocalDateTime().dayOfMonth == 15
            result.toLocalDateTime().hour == 0
            result.toLocalDateTime().minute == 0
            result.toLocalDateTime().second == 0

        where:
            session << allSessions
    }

    def "DATE_TRUNC with HOUR precision truncates to start of hour"() {
        when:
            def result = session.select(
                    DSL.dateTrunc(DateTimePrecision.HOUR, DSL.name(Timestamp.class, "event_timestamp"))
            )
                    .from("events")
                    .where(DSL.name(Long.class, "id").eq(1L))
                    .fetchSingle()

        then:
            result != null
            // 2024-06-15 14:30:45 truncated to HOUR -> 2024-06-15 14:00:00
            result.toLocalDateTime().year == 2024
            result.toLocalDateTime().monthValue == 6
            result.toLocalDateTime().dayOfMonth == 15
            result.toLocalDateTime().hour == 14
            result.toLocalDateTime().minute == 0
            result.toLocalDateTime().second == 0

        where:
            session << allSessions
    }

    def "DATE_TRUNC with MINUTE precision truncates to start of minute"() {
        when:
            def result = session.select(
                    DSL.dateTrunc(DateTimePrecision.MINUTE, DSL.name(Timestamp.class, "event_timestamp"))
            )
                    .from("events")
                    .where(DSL.name(Long.class, "id").eq(1L))
                    .fetchSingle()

        then:
            result != null
            // 2024-06-15 14:30:45 truncated to MINUTE -> 2024-06-15 14:30:00
            result.toLocalDateTime().year == 2024
            result.toLocalDateTime().monthValue == 6
            result.toLocalDateTime().dayOfMonth == 15
            result.toLocalDateTime().hour == 14
            result.toLocalDateTime().minute == 30
            result.toLocalDateTime().second == 0

        where:
            session << allSessions
    }

    def "DATE_TRUNC with SECOND precision truncates to start of second"() {
        when:
            def result = session.select(
                    DSL.dateTrunc(DateTimePrecision.SECOND, DSL.name(Timestamp.class, "event_timestamp"))
            )
                    .from("events")
                    .where(DSL.name(Long.class, "id").eq(1L))
                    .fetchSingle()

        then:
            result != null
            // 2024-06-15 14:30:45 truncated to SECOND -> 2024-06-15 14:30:45 (no fractional seconds)
            result.toLocalDateTime().year == 2024
            result.toLocalDateTime().monthValue == 6
            result.toLocalDateTime().dayOfMonth == 15
            result.toLocalDateTime().hour == 14
            result.toLocalDateTime().minute == 30
            result.toLocalDateTime().second == 45
            result.toLocalDateTime().nano == 0

        where:
            session << allSessions
    }

    def "DATE_TRUNC with QUARTER precision truncates to start of quarter"() {
        when:
            def result = session.select(
                    DSL.dateTrunc(DateTimePrecision.QUARTER, DSL.name(Timestamp.class, "event_timestamp"))
            )
                    .from("events")
                    .where(DSL.name(Long.class, "id").eq(1L))
                    .fetchSingle()

        then:
            result != null
            // 2024-06-15 14:30:45 is in Q2 (Apr-Jun), truncated to QUARTER -> 2024-04-01 00:00:00
            result.toLocalDateTime().year == 2024
            result.toLocalDateTime().monthValue == 4
            result.toLocalDateTime().dayOfMonth == 1
            result.toLocalDateTime().hour == 0
            result.toLocalDateTime().minute == 0

        where:
            session << allSessions
    }

    def "DATE_TRUNC with WEEK precision truncates to start of week"() {
        when:
            def result = session.select(
                    DSL.dateTrunc(DateTimePrecision.WEEK, DSL.name(Timestamp.class, "event_timestamp"))
            )
                    .from("events")
                    .where(DSL.name(Long.class, "id").eq(1L))
                    .fetchSingle()

        then:
            result != null
            // 2024-06-15 is Saturday, truncated to WEEK -> 2024-06-10 (Monday) for PostgreSQL
            // Note: The exact start of week may vary by database but both should truncate to a week start
            result.toLocalDateTime().year == 2024
            result.toLocalDateTime().monthValue == 6
            result.toLocalDateTime().hour == 0
            result.toLocalDateTime().minute == 0

        where:
            session << allSessions
    }

    def "DATE_TRUNC can be used in GROUP BY clause"() {
        when:
            def results = session.select(
                    DSL.dateTrunc(DateTimePrecision.MONTH, DSL.name(Timestamp.class, "event_timestamp")),
                    DSL.count()
            )
                    .from("events")
                    .groupBy(DSL.dateTrunc(DateTimePrecision.MONTH, DSL.name(Timestamp.class, "event_timestamp")))
                    .fetchAll()

        then:
            results.size() == 1
            // Index 2 is count column (1-based indexing from Record)
            results[0].get(2) == 1L // One event in June 2024

        where:
            session << allSessions
    }

    def "DATE_TRUNC can be used in ORDER BY clause"() {
        when:
            // Use DATE_TRUNC result in ORDER BY
            def results = session.select(
                    DSL.name(Long.class, "id"),
                    DSL.dateTrunc(DateTimePrecision.MONTH, DSL.name(Timestamp.class, "event_timestamp"))
            )
                    .from("events")
                    .orderBy(DSL.asc(2))
                    .fetchAll()

        then:
            results.size() == 1
            results[0].get(1) == 1L

        where:
            session << allSessions
    }
}
