// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core;

import io.github.thinkfastpl.harbororm.api.expression.DSL;
import io.github.thinkfastpl.harbororm.api.expression.DefaultSelectExpression;
import io.github.thinkfastpl.harbororm.api.expression.Expression;
import io.github.thinkfastpl.harbororm.api.query.data.SelectQueryData;
import io.github.thinkfastpl.harbororm.api.query.executor.QueryExecutor;
import io.github.thinkfastpl.harbororm.api.query.result.Page;
import io.github.thinkfastpl.harbororm.api.query.result.Record;
import lombok.NonNull;

import java.util.List;

class PageFetcher {

    static <T> Page<T> fetchPage(@NonNull QueryExecutor queryExecutor, @NonNull DefaultSelectExpression<T> expression, int pageSize, int page) {
        if (pageSize <= 0) {
            throw new IllegalArgumentException("Page size must be greater than zero");
        }
        if (page < 0) {
            throw new IllegalArgumentException("Page number must be greater than or equal to zero");
        }

        final SelectQueryData queryData = expression.toQueryData();

        if (queryData.isDistinct()) {
            final List<Record> records = queryExecutor.fetchAll(queryData);
            final Long totalRows = fetchRowsNumber(queryExecutor, queryData);

            return new Page<>(
                    records.stream().map(expression.getRecordMapper()).toList(),
                    pageSize,
                    page,
                    totalRows,
                    countTotalPages(pageSize, totalRows)
            );
        }

        final Expression<Long> countExpression = DSL.count().over();

        final List<QueryExecutor.RecordWithExtra> records = queryExecutor.fetchAll(queryData, List.of(countExpression));
        final long totalRows;

        if (!records.isEmpty()) {
            totalRows = records.get(0).getExtraRecord().get(countExpression);
        } else if (page > 0) {
            totalRows = fetchRowsNumber(queryExecutor, queryData);
        } else {
            totalRows = 0;
        }

        return new Page<>(
                records.stream().map(QueryExecutor.RecordWithExtra::getRecord).map(expression.getRecordMapper()).toList(),
                pageSize,
                page,
                totalRows,
                countTotalPages(pageSize, totalRows)
        );
    }

    private static Long fetchRowsNumber(@NonNull QueryExecutor queryExecutor, @NonNull SelectQueryData queryData) {
        final List<Record> records = queryExecutor.fetchAll(new SelectQueryData(
                queryData.getCtes(),
                queryData.isWithRecursive(),
                false,
                List.of(DSL.count()),
                queryData.getFrom(),
                queryData.getJoins(),
                queryData.getWhereConditions(),
                queryData.getGroupByExpressions(),
                queryData.getHavingConditions(),
                null,
                null,
                null,
                null,
                false
        ));
        return records.get(0).get(1, Long.class);
    }

    private static long countTotalPages(int pageSize, long totalElements) {
        return (long) Math.ceil((double) totalElements / pageSize);
    }
}
