// SPDX-License-Identifier: Apache-2.0
package io.github.thinkfastpl.harbororm.core.sql.dialect;

import io.github.thinkfastpl.harbororm.api.expression.*;
import io.github.thinkfastpl.harbororm.api.metadata.ColumnContext;
import io.github.thinkfastpl.harbororm.api.metadata.QColumn;
import io.github.thinkfastpl.harbororm.api.metadata.QTableName;
import io.github.thinkfastpl.harbororm.api.metadata.QTableSource;
import io.github.thinkfastpl.harbororm.api.query.data.SelectQueryData;
import lombok.NonNull;

public abstract class EmptySqlDialect implements SqlDialect {

    @Override
    public SqlQuery toSqlPart(@NonNull QTableSource tableSource) {
        if (tableSource instanceof QTableName tableName) {
            return renderQTableName(tableName);
        }
        if (tableSource instanceof DefaultSelectExpressionTableSource expressionTableSource) {
            return renderDefaultSelectExpressionTableSource(expressionTableSource);
        }
        if (tableSource instanceof CommonTableExpression commonTableExpression) {
            return renderCommonTableExpression(commonTableExpression);
        }
        if (tableSource instanceof FunctionCallTableSource<?> functionTableSource) {
            return renderFunctionCallTableSource(functionTableSource);
        }

        throw new UnsupportedOperationException("Unable to create sql part for table source type: " + tableSource.getClass().getName());
    }

    protected SqlQuery renderQTableName(@NonNull QTableName tableName) {
        throw new UnsupportedOperationException("Unable to create sql part for table source type: " + tableName.getClass().getName());
    }

    protected SqlQuery renderDefaultSelectExpressionTableSource(@NonNull DefaultSelectExpressionTableSource expressionTableSource) {
        throw new UnsupportedOperationException("Unable to create sql part for table source type: " + expressionTableSource.getClass().getName());
    }

    protected SqlQuery renderCommonTableExpression(@NonNull CommonTableExpression cte) {
        throw new UnsupportedOperationException("Unable to create sql part for table source type: " + cte.getClass().getName());
    }

    protected SqlQuery renderFunctionCallTableSource(@NonNull FunctionCallTableSource<?> functionTableSource) {
        throw new UnsupportedOperationException("Unable to create sql part for table source type: " + functionTableSource.getClass().getName());
    }

    @Override
    public SqlQuery toSqlPart(@NonNull Expression<?> expression, ColumnContext columnContext) {
        if (expression instanceof QColumn<?> column) {
            return renderQColumn(column, columnContext);
        }

        if (expression instanceof ExcludedColumnExpression<?> excluded) {
            return renderExcludedColumnExpression(excluded, columnContext);
        }

        if (expression instanceof CommonTableExpression.Column<?> column) {
            return renderCommonTableExpressionColumn(column, columnContext);
        }

        if (expression instanceof AliasedExpression<?> aliasedExpression) {
            return renderAliasedExpression(aliasedExpression, columnContext);
        }

        if (expression instanceof BooleanConstantExpression booleanConstantExpression) {
            return renderBooleanConstantExpression(booleanConstantExpression, columnContext);
        }

        if (expression instanceof ConstantExpression<?> constantExpression) {
            return renderConstantExpression(constantExpression, columnContext);
        }

        if (expression instanceof ConstantsExpression<?> constantsExpression) {
            return renderConstantsExpression(constantsExpression, columnContext);
        }

        if (expression instanceof BinaryOperatorExpression<?> binaryOperatorExpression) {
            return renderBinaryOperatorExpression(binaryOperatorExpression, columnContext);
        }

        if (expression instanceof ExpressionBooleanCondition expressionBooleanCondition) {
            return renderExpressionBooleanCondition(expressionBooleanCondition, columnContext);
        }

        if (expression instanceof UnaryOperatorCondition condition) {
            return renderUnaryOperatorCondition(condition, columnContext);
        }

        if (expression instanceof BinaryOperatorCondition condition) {
            return renderBinaryOperatorCondition(condition, columnContext);
        }

        if (expression instanceof TernaryOperatorCondition condition) {
            return renderTernaryOperatorCondition(condition, columnContext);
        }

        if (expression instanceof EmbeddableEqCondition embeddableEqCondition) {
            return renderEmbeddableEqCondition(embeddableEqCondition, columnContext);
        }

        if (expression instanceof EmbeddableInCondition embeddableInCondition) {
            return renderEmbeddableInCondition(embeddableInCondition, columnContext);
        }

        if (expression instanceof ComplexCondition condition) {
            return renderComplexCondition(condition, columnContext);
        }

        if (expression instanceof PortableFunctionExpression<?> functionExpression) {
            return renderPortableFunctionExpression(functionExpression, columnContext);
        }

        if (expression instanceof FunctionExpression<?> functionExpression) {
            return renderFunctionExpression(functionExpression, columnContext);
        }

        if (expression instanceof LiteralExpression<?> literalExpression) {
            return renderLiteralExpression(literalExpression, columnContext);
        }

        if (expression instanceof IntervalConstant intervalConstant) {
            return renderIntervalConstant(intervalConstant, columnContext);
        }

        if (expression instanceof NameExpression<?> nameExpression) {
            return renderNameExpression(nameExpression, columnContext);
        }

        if (expression instanceof CastExpression<?> castExpression) {
            return renderCastExpression(castExpression, columnContext);
        }

        if (expression instanceof SelectExpression<?> selectExpression) {
            return renderSelectExpression(selectExpression, columnContext);
        }

        if (expression instanceof FluentSelectExpression<?> selectExpression) {
            return renderFluentSelectExpression(selectExpression, columnContext);
        }

        if (expression instanceof WindowExpression<?> windowExpression) {
            return renderWindowExpression(windowExpression, columnContext);
        }

        if (expression instanceof PortableAggregateExpression<?> portableAggregateExpression) {
            return renderPortableAggregateExpression(portableAggregateExpression, columnContext);
        }

        if (expression instanceof MultisetAggExpression multisetAggExpression) {
            return renderMultisetAggExpression(multisetAggExpression, columnContext);
        }

        if (expression instanceof CaseWhenThenExpression<?> cwtExpression) {
            return renderCaseWhenThenExpression(cwtExpression, columnContext);
        }

        if (expression instanceof ParenthesesExpression<?> parenthesesExpression) {
            return renderParenthesesExpression(parenthesesExpression, columnContext);
        }

        if (expression instanceof JsonExpression jsonExpr) {
            return renderJsonExpression(jsonExpr, columnContext);
        }

        if (expression instanceof JsonTextExpression jsonTextExpr) {
            return renderJsonTextExpression(jsonTextExpr, columnContext);
        }

        if (expression instanceof JsonCondition jsonCondition) {
            return renderJsonCondition(jsonCondition, columnContext);
        }

        if (expression instanceof NextvalExpression nextvalExpression) {
            return renderNextvalExpression(nextvalExpression, columnContext);
        }

        if (expression instanceof DialectSpecificExpression dialectSpecific) {
            if (!dialectSpecific.getDialectName().equals(getName())) {
                throw new UnsupportedOperationException("Expression " + expression.getClass().getName()
                        + " is specific to dialect " + dialectSpecific.getDialectName()
                        + " and cannot be rendered by dialect " + getName());
            }
            return dialectSpecific.toSqlPart(this, columnContext);
        }

        throw new UnsupportedOperationException("Unable to create sql part for expression type: " + expression.getClass().getName());
    }

    protected SqlQuery renderQColumn(@NonNull QColumn<?> column, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for expression type: " + column.getClass().getName());
    }

    protected SqlQuery renderExcludedColumnExpression(@NonNull ExcludedColumnExpression<?> excluded, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for expression type: " + excluded.getClass().getName());
    }

    protected SqlQuery renderCommonTableExpressionColumn(@NonNull CommonTableExpression.Column<?> column, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for expression type: " + column.getClass().getName());
    }

    protected SqlQuery renderAliasedExpression(@NonNull AliasedExpression<?> aliasedExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for expression type: " + aliasedExpression.getClass().getName());
    }

    protected SqlQuery renderBooleanConstantExpression(@NonNull BooleanConstantExpression booleanConstantExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for expression type: " + booleanConstantExpression.getClass().getName());
    }

    protected SqlQuery renderConstantExpression(@NonNull ConstantExpression<?> constantExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for expression type: " + constantExpression.getClass().getName());
    }

    protected SqlQuery renderConstantsExpression(@NonNull ConstantsExpression<?> constantsExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for expression type: " + constantsExpression.getClass().getName());
    }

    protected SqlQuery renderBinaryOperatorExpression(@NonNull BinaryOperatorExpression<?> binaryOperatorExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for expression type: " + binaryOperatorExpression.getClass().getName());
    }

    protected SqlQuery renderExpressionBooleanCondition(@NonNull ExpressionBooleanCondition expressionBooleanCondition, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for expression type: " + expressionBooleanCondition.getClass().getName());
    }

    protected SqlQuery renderUnaryOperatorCondition(@NonNull UnaryOperatorCondition condition, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for expression type: " + condition.getClass().getName());
    }

    protected SqlQuery renderBinaryOperatorCondition(@NonNull BinaryOperatorCondition condition, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for expression type: " + condition.getClass().getName());
    }

    protected SqlQuery renderTernaryOperatorCondition(@NonNull TernaryOperatorCondition condition, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for expression type: " + condition.getClass().getName());
    }

    protected SqlQuery renderEmbeddableEqCondition(@NonNull EmbeddableEqCondition condition, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for expression type: " + condition.getClass().getName());
    }

    protected SqlQuery renderEmbeddableInCondition(@NonNull EmbeddableInCondition embeddableInCondition, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for expression type: " + embeddableInCondition.getClass().getName());
    }

    protected SqlQuery renderComplexCondition(@NonNull ComplexCondition condition, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for expression type: " + condition.getClass().getName());
    }

    protected SqlQuery renderPortableFunctionExpression(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        return switch (functionExpression.getFunction()) {
            case ASCII -> renderPortableFunctionExpressionAscii(functionExpression, columnContext);
            case BIT_LENGTH -> renderPortableFunctionExpressionBitLength(functionExpression, columnContext);
            case BTRIM -> renderPortableFunctionExpressionBtrim(functionExpression, columnContext);
            case CHR -> renderPortableFunctionExpressionChr(functionExpression, columnContext);
            case CONCAT -> renderPortableFunctionExpressionConcat(functionExpression, columnContext);
            case CONCAT_WS -> renderPortableFunctionExpressionConcatWs(functionExpression, columnContext);
            case CHAR_LENGTH -> renderPortableFunctionExpressionCharLength(functionExpression, columnContext);
            case LEFT -> renderPortableFunctionExpressionLeft(functionExpression, columnContext);
            case LOWER -> renderPortableFunctionExpressionLower(functionExpression, columnContext);
            case LPAD -> renderPortableFunctionExpressionLpad(functionExpression, columnContext);
            case LTRIM -> renderPortableFunctionExpressionLtrim(functionExpression, columnContext);
            case OCTET_LENGTH -> renderPortableFunctionExpressionOctetLength(functionExpression, columnContext);
            case POSITION -> renderPortableFunctionExpressionPosition(functionExpression, columnContext);
            case REPEAT -> renderPortableFunctionExpressionRepeat(functionExpression, columnContext);
            case RIGHT -> renderPortableFunctionExpressionRight(functionExpression, columnContext);
            case RPAD -> renderPortableFunctionExpressionRpad(functionExpression, columnContext);
            case RTRIM -> renderPortableFunctionExpressionRtrim(functionExpression, columnContext);
            case SUBSTRING -> renderPortableFunctionExpressionSubstring(functionExpression, columnContext);
            case TRIM -> renderPortableFunctionExpressionTrim(functionExpression, columnContext);
            case TO_HEX -> renderPortableFunctionExpressionToHex(functionExpression, columnContext);
            case UPPER -> renderPortableFunctionExpressionUpper(functionExpression, columnContext);
            case REPLACE -> renderPortableFunctionExpressionReplace(functionExpression, columnContext);
            case REGEXP_REPLACE -> renderPortableFunctionExpressionRegexReplace(functionExpression, columnContext);
            case REVERSE -> renderPortableFunctionExpressionReverse(functionExpression, columnContext);
            case SPACE -> renderPortableFunctionExpressionSpace(functionExpression, columnContext);
            case MD5 -> renderPortableFunctionExpressionMd5(functionExpression, columnContext);
            case JSON_OBJECT -> renderPortableFunctionExpressionJsonObject(functionExpression, columnContext);
            case JSON_ARRAY -> renderPortableFunctionExpressionJsonArray(functionExpression, columnContext);
            case JSON_ARRAY_LENGTH -> renderPortableFunctionExpressionJsonArrayLength(functionExpression, columnContext);
            case MOD -> renderPortableFunctionExpressionMod(functionExpression, columnContext);
            case POWER -> renderPortableFunctionExpressionPower(functionExpression, columnContext);
            case ROUND -> renderPortableFunctionExpressionRound(functionExpression, columnContext);
            case CEIL -> renderPortableFunctionExpressionCeil(functionExpression, columnContext);
            case FLOOR -> renderPortableFunctionExpressionFloor(functionExpression, columnContext);
            case TRUNCATE -> renderPortableFunctionExpressionTruncate(functionExpression, columnContext);
            case ABS -> renderPortableFunctionExpressionAbs(functionExpression, columnContext);
            case NEG -> renderPortableFunctionExpressionNeg(functionExpression, columnContext);
            case SIGN -> renderPortableFunctionExpressionSign(functionExpression, columnContext);
            case SQRT -> renderPortableFunctionExpressionSqrt(functionExpression, columnContext);
            case CBRT -> renderPortableFunctionExpressionCbrt(functionExpression, columnContext);
            case SQUARE -> renderPortableFunctionExpressionSquare(functionExpression, columnContext);
            case DEGREES -> renderPortableFunctionExpressionDegrees(functionExpression, columnContext);
            case RADIANS -> renderPortableFunctionExpressionRadians(functionExpression, columnContext);
            case E -> renderPortableFunctionExpressionE(functionExpression, columnContext);
            case EXP -> renderPortableFunctionExpressionExp(functionExpression, columnContext);
            case GREATEST -> renderPortableFunctionExpressionGreatest(functionExpression, columnContext);
            case LEAST -> renderPortableFunctionExpressionLeast(functionExpression, columnContext);
            case LN -> renderPortableFunctionExpressionLn(functionExpression, columnContext);
            case LOG -> renderPortableFunctionExpressionLog(functionExpression, columnContext);
            case LOG10 -> renderPortableFunctionExpressionLog10(functionExpression, columnContext);
            case PI -> renderPortableFunctionExpressionPi(functionExpression, columnContext);
            case ACOS -> renderPortableFunctionExpressionAcos(functionExpression, columnContext);
            case ASIN -> renderPortableFunctionExpressionAsin(functionExpression, columnContext);
            case ATAN -> renderPortableFunctionExpressionAtan(functionExpression, columnContext);
            case ATAN2 -> renderPortableFunctionExpressionAtan2(functionExpression, columnContext);
            case COS -> renderPortableFunctionExpressionCos(functionExpression, columnContext);
            case COT -> renderPortableFunctionExpressionCot(functionExpression, columnContext);
            case SIN -> renderPortableFunctionExpressionSin(functionExpression, columnContext);
            case TAN -> renderPortableFunctionExpressionTan(functionExpression, columnContext);
            case ACOSH -> renderPortableFunctionExpressionAcosh(functionExpression, columnContext);
            case ASINH -> renderPortableFunctionExpressionAsinh(functionExpression, columnContext);
            case ATANH -> renderPortableFunctionExpressionAtanh(functionExpression, columnContext);
            case COSH -> renderPortableFunctionExpressionCosh(functionExpression, columnContext);
            case COTH -> renderPortableFunctionExpressionCoth(functionExpression, columnContext);
            case SINH -> renderPortableFunctionExpressionSinh(functionExpression, columnContext);
            case TANH -> renderPortableFunctionExpressionTanh(functionExpression, columnContext);
            case COALESCE -> renderPortableFunctionExpressionCoalesce(functionExpression, columnContext);
            case NULLIF -> renderPortableFunctionExpressionNullIf(functionExpression, columnContext);
            case IFNULL -> renderPortableFunctionExpressionIfNull(functionExpression, columnContext);
            case NVL -> renderPortableFunctionExpressionNvl(functionExpression, columnContext);
            case NUM_NULLS -> renderPortableFunctionExpressionNumNulls(functionExpression, columnContext);
            case NUM_NON_NULLS -> renderPortableFunctionExpressionNumNonNulls(functionExpression, columnContext);
            case IIF -> renderPortableFunctionExpressionIif(functionExpression, columnContext);
            case EXISTS -> renderPortableFunctionExpressionExists(functionExpression, columnContext);
            case CURRENT_TIME -> renderPortableFunctionExpressionCurrentTime(functionExpression, columnContext);
            case CURRENT_DATE -> renderPortableFunctionExpressionCurrentDate(functionExpression, columnContext);
            case CURRENT_TIMESTAMP -> renderPortableFunctionExpressionCurrentTimestamp(functionExpression, columnContext);
            case CURRENT_LOCALDATETIME -> renderPortableFunctionExpressionCurrentLocalDateTime(functionExpression, columnContext);
            case DATE -> renderPortableFunctionExpressionDate(functionExpression, columnContext);
            case DATE_ADD -> renderPortableFunctionExpressionDateAdd(functionExpression, columnContext);
            case DATE_ADD_INTERVAL -> renderPortableFunctionExpressionDateAddInterval(functionExpression, columnContext);
            case DATE_SUB -> renderPortableFunctionExpressionDateSub(functionExpression, columnContext);
            case DATE_SUB_INTERVAL -> renderPortableFunctionExpressionDateSubInterval(functionExpression, columnContext);
            case DATE_DIFF -> renderPortableFunctionExpressionDateDiff(functionExpression, columnContext);
            case EXTRACT_DAY -> renderPortableFunctionExpressionExtractDay(functionExpression, columnContext);
            case EXTRACT_DAY_OF_YEAR -> renderPortableFunctionExpressionExtractDayOfYear(functionExpression, columnContext);
            case EXTRACT_ISO_DAY_OF_WEEK -> renderPortableFunctionExpressionExtractIsoDayOfWeek(functionExpression, columnContext);
            case EXTRACT_HOUR -> renderPortableFunctionExpressionExtractHour(functionExpression, columnContext);
            case EXTRACT_MINUTE -> renderPortableFunctionExpressionExtractMinute(functionExpression, columnContext);
            case EXTRACT_MONTH -> renderPortableFunctionExpressionExtractMonth(functionExpression, columnContext);
            case EXTRACT_QUARTER -> renderPortableFunctionExpressionExtractQuarter(functionExpression, columnContext);
            case EXTRACT_SECOND -> renderPortableFunctionExpressionExtractSecond(functionExpression, columnContext);
            case EXTRACT_YEAR -> renderPortableFunctionExpressionExtractYear(functionExpression, columnContext);
            case EPOCH -> renderPortableFunctionExpressionEpoch(functionExpression, columnContext);
            case DATE_TRUNC_YEAR, DATE_TRUNC_QUARTER, DATE_TRUNC_MONTH, DATE_TRUNC_WEEK, DATE_TRUNC_DAY, DATE_TRUNC_HOUR, DATE_TRUNC_MINUTE, DATE_TRUNC_SECOND, DATE_TRUNC_MILLISECOND,
                 DATE_TRUNC_MICROSECOND -> renderPortableFunctionExpressionDateTrunc(functionExpression, columnContext);
            case RANDOM -> renderPortableFunctionExpressionRandom(functionExpression, columnContext);
            case UUID -> renderPortableFunctionExpressionUuid(functionExpression, columnContext);
            case BIT_AND -> renderPortableFunctionExpressionBitAnd(functionExpression, columnContext);
            case BIT_GET -> renderPortableFunctionExpressionBitGet(functionExpression, columnContext);
            case BIT_NAND -> renderPortableFunctionExpressionBitNand(functionExpression, columnContext);
            case BIT_NOR -> renderPortableFunctionExpressionBitNor(functionExpression, columnContext);
            case BIT_NOT -> renderPortableFunctionExpressionBitNot(functionExpression, columnContext);
            case BIT_OR -> renderPortableFunctionExpressionBitOr(functionExpression, columnContext);
            case BIT_SET -> renderPortableFunctionExpressionBitSet(functionExpression, columnContext);
            case BIT_XNOR -> renderPortableFunctionExpressionBitXnor(functionExpression, columnContext);
            case BIT_XOR -> renderPortableFunctionExpressionBitXor(functionExpression, columnContext);
            case SHL -> renderPortableFunctionExpressionShl(functionExpression, columnContext);
            case SHR -> renderPortableFunctionExpressionShr(functionExpression, columnContext);
            case ROW_NUMBER -> renderPortableFunctionExpressionRowNumber(functionExpression, columnContext);
            case RANK -> renderPortableFunctionExpressionRank(functionExpression, columnContext);
            case DENSE_RANK -> renderPortableFunctionExpressionDenseRank(functionExpression, columnContext);
            case NTILE -> renderPortableFunctionExpressionNtile(functionExpression, columnContext);
            case PERCENT_RANK -> renderPortableFunctionExpressionPercentRank(functionExpression, columnContext);
            case CUME_DIST -> renderPortableFunctionExpressionCumeDist(functionExpression, columnContext);
            case LAG -> renderPortableFunctionExpressionLag(functionExpression, columnContext);
            case LEAD -> renderPortableFunctionExpressionLead(functionExpression, columnContext);
            case FIRST_VALUE -> renderPortableFunctionExpressionFirstValue(functionExpression, columnContext);
            case LAST_VALUE -> renderPortableFunctionExpressionLastValue(functionExpression, columnContext);
            case NTH_VALUE -> renderPortableFunctionExpressionNthValue(functionExpression, columnContext);
            case PERCENTILE_CONT -> renderPortableFunctionExpressionPercentileCont(functionExpression, columnContext);
            case PERCENTILE_DISC -> renderPortableFunctionExpressionPercentileDisc(functionExpression, columnContext);
        };
    }

    protected SqlQuery renderPortableFunctionExpressionAscii(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionBitLength(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionBtrim(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionChr(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionConcat(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionConcatWs(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionCharLength(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionLeft(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionLower(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionLpad(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionLtrim(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionOctetLength(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionPosition(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionRepeat(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionRight(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionRpad(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionRtrim(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionSubstring(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionTrim(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionToHex(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionUpper(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionReplace(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionRegexReplace(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionReverse(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionSpace(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionMd5(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionJsonObject(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionJsonArray(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionJsonArrayLength(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionMod(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionPower(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionRound(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionCeil(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionFloor(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionTruncate(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionAbs(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionNeg(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionSign(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionSqrt(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionCbrt(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionSquare(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionDegrees(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionRadians(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionE(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionExp(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionGreatest(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionLeast(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionLn(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionLog(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionLog10(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionPi(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionAcos(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionAsin(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionAtan(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionAtan2(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionCos(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionCot(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionSin(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionTan(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionAcosh(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionAsinh(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionAtanh(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionCosh(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionCoth(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionSinh(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionTanh(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionCoalesce(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionNullIf(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionIfNull(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionNvl(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionNumNulls(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionNumNonNulls(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionIif(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionExists(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionCurrentTime(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionCurrentDate(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionCurrentTimestamp(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionCurrentLocalDateTime(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionDate(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionDateAdd(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionDateAddInterval(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionDateSub(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionDateSubInterval(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionDateDiff(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionExtractDay(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionExtractDayOfYear(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionExtractIsoDayOfWeek(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionExtractHour(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionExtractMinute(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionExtractMonth(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionExtractQuarter(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionExtractSecond(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionExtractYear(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionEpoch(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionDateTrunc(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionRandom(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionUuid(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionBitAnd(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionBitGet(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionBitNand(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionBitNor(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionBitNot(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionBitOr(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionBitSet(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionBitXnor(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionBitXor(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionShl(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionShr(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionRowNumber(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionRank(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionDenseRank(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionNtile(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionPercentRank(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionCumeDist(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionLag(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionLead(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionFirstValue(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionLastValue(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionNthValue(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionPercentileCont(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderPortableFunctionExpressionPercentileDisc(@NonNull PortableFunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for function expression type: " + functionExpression.getClass().getName() + "/" + functionExpression.getFunction());
    }

    protected SqlQuery renderFunctionExpression(@NonNull FunctionExpression<?> functionExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for expression type: " + functionExpression.getClass().getName());
    }

    protected SqlQuery renderLiteralExpression(@NonNull LiteralExpression<?> literalExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for expression type: " + literalExpression.getClass().getName());
    }

    protected SqlQuery renderIntervalConstant(@NonNull IntervalConstant intervalConstant, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for expression type: " + intervalConstant.getClass().getName());
    }

    protected SqlQuery renderNameExpression(@NonNull NameExpression<?> nameExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for expression type: " + nameExpression.getClass().getName());
    }

    protected SqlQuery renderCastExpression(@NonNull CastExpression<?> castExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for expression type: " + castExpression.getClass().getName());
    }

    protected SqlQuery renderSelectExpression(@NonNull SelectExpression<?> selectExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for expression type: " + selectExpression.getClass().getName());
    }

    protected SqlQuery renderFluentSelectExpression(@NonNull FluentSelectExpression<?> selectExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for expression type: " + selectExpression.getClass().getName());
    }

    protected SqlQuery renderWindowExpression(@NonNull WindowExpression<?> windowExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for expression type: " + windowExpression.getClass().getName());
    }

    protected SqlQuery renderPortableAggregateExpression(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        return switch (aggregateExpression.getFunction()) {
            case ANY_VALUE -> renderPortableAggregateExpressionAnyValue(aggregateExpression, columnContext);
            case ARRAY_AGG -> renderPortableAggregateExpressionArrayAgg(aggregateExpression, columnContext);
            case AVG -> renderPortableAggregateExpressionAvg(aggregateExpression, columnContext);
            case BIT_AND_AGG -> renderPortableAggregateExpressionBitAndAgg(aggregateExpression, columnContext);
            case BIT_NAND_AGG -> renderPortableAggregateExpressionBitNandAgg(aggregateExpression, columnContext);
            case BIT_NOR_AGG -> renderPortableAggregateExpressionBitNorAgg(aggregateExpression, columnContext);
            case BIT_OR_AGG -> renderPortableAggregateExpressionBitOrAgg(aggregateExpression, columnContext);
            case BIT_XOR_AGG -> renderPortableAggregateExpressionBitXorAgg(aggregateExpression, columnContext);
            case BIT_XNOR_AGG -> renderPortableAggregateExpressionBitXnorAgg(aggregateExpression, columnContext);
            case BOOL_AND -> renderPortableAggregateExpressionBoolAnd(aggregateExpression, columnContext);
            case BOOL_OR -> renderPortableAggregateExpressionBoolOr(aggregateExpression, columnContext);
            case COUNT -> renderPortableAggregateExpressionCount(aggregateExpression, columnContext);
            case EVERY -> renderPortableAggregateExpressionEvery(aggregateExpression, columnContext);
            case GROUP_CONCAT -> renderPortableAggregateExpressionGroupConcat(aggregateExpression, columnContext);
            case JSON_ARRAY_AGG -> renderPortableAggregateExpressionJsonArrayAgg(aggregateExpression, columnContext);
            case JSON_OBJECT_AGG -> renderPortableAggregateExpressionJsonObjectAgg(aggregateExpression, columnContext);
            case MIN -> renderPortableAggregateExpressionMin(aggregateExpression, columnContext);
            case MAX -> renderPortableAggregateExpressionMax(aggregateExpression, columnContext);
            case STDDEV_POP -> renderPortableAggregateExpressionStddevPop(aggregateExpression, columnContext);
            case STDDEV_SAMP -> renderPortableAggregateExpressionStddevSamp(aggregateExpression, columnContext);
            case SUM -> renderPortableAggregateExpressionSum(aggregateExpression, columnContext);
            case VAR_POP -> renderPortableAggregateExpressionVarPop(aggregateExpression, columnContext);
            case VAR_SAMP -> renderPortableAggregateExpressionVarSamp(aggregateExpression, columnContext);
        };
    }

    protected SqlQuery renderPortableAggregateExpressionAnyValue(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for aggregate expression type: " + aggregateExpression.getClass().getName() + "/" + aggregateExpression.getFunction());
    }

    protected SqlQuery renderPortableAggregateExpressionArrayAgg(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for aggregate expression type: " + aggregateExpression.getClass().getName() + "/" + aggregateExpression.getFunction());
    }

    protected SqlQuery renderPortableAggregateExpressionAvg(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for aggregate expression type: " + aggregateExpression.getClass().getName() + "/" + aggregateExpression.getFunction());
    }

    protected SqlQuery renderPortableAggregateExpressionBitAndAgg(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for aggregate expression type: " + aggregateExpression.getClass().getName() + "/" + aggregateExpression.getFunction());
    }

    protected SqlQuery renderPortableAggregateExpressionBitNandAgg(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for aggregate expression type: " + aggregateExpression.getClass().getName() + "/" + aggregateExpression.getFunction());
    }

    protected SqlQuery renderPortableAggregateExpressionBitNorAgg(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for aggregate expression type: " + aggregateExpression.getClass().getName() + "/" + aggregateExpression.getFunction());
    }

    protected SqlQuery renderPortableAggregateExpressionBitOrAgg(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for aggregate expression type: " + aggregateExpression.getClass().getName() + "/" + aggregateExpression.getFunction());
    }

    protected SqlQuery renderPortableAggregateExpressionBitXorAgg(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for aggregate expression type: " + aggregateExpression.getClass().getName() + "/" + aggregateExpression.getFunction());
    }

    protected SqlQuery renderPortableAggregateExpressionBitXnorAgg(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for aggregate expression type: " + aggregateExpression.getClass().getName() + "/" + aggregateExpression.getFunction());
    }

    protected SqlQuery renderPortableAggregateExpressionBoolAnd(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for aggregate expression type: " + aggregateExpression.getClass().getName() + "/" + aggregateExpression.getFunction());
    }

    protected SqlQuery renderPortableAggregateExpressionBoolOr(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for aggregate expression type: " + aggregateExpression.getClass().getName() + "/" + aggregateExpression.getFunction());
    }

    protected SqlQuery renderPortableAggregateExpressionCount(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for aggregate expression type: " + aggregateExpression.getClass().getName() + "/" + aggregateExpression.getFunction());
    }

    protected SqlQuery renderPortableAggregateExpressionEvery(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for aggregate expression type: " + aggregateExpression.getClass().getName() + "/" + aggregateExpression.getFunction());
    }

    protected SqlQuery renderPortableAggregateExpressionGroupConcat(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for aggregate expression type: " + aggregateExpression.getClass().getName() + "/" + aggregateExpression.getFunction());
    }

    protected SqlQuery renderPortableAggregateExpressionJsonArrayAgg(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for aggregate expression type: " + aggregateExpression.getClass().getName() + "/" + aggregateExpression.getFunction());
    }

    protected SqlQuery renderPortableAggregateExpressionJsonObjectAgg(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for aggregate expression type: " + aggregateExpression.getClass().getName() + "/" + aggregateExpression.getFunction());
    }

    protected SqlQuery renderPortableAggregateExpressionMin(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for aggregate expression type: " + aggregateExpression.getClass().getName() + "/" + aggregateExpression.getFunction());
    }

    protected SqlQuery renderPortableAggregateExpressionMax(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for aggregate expression type: " + aggregateExpression.getClass().getName() + "/" + aggregateExpression.getFunction());
    }

    protected SqlQuery renderPortableAggregateExpressionStddevPop(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for aggregate expression type: " + aggregateExpression.getClass().getName() + "/" + aggregateExpression.getFunction());
    }

    protected SqlQuery renderPortableAggregateExpressionStddevSamp(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for aggregate expression type: " + aggregateExpression.getClass().getName() + "/" + aggregateExpression.getFunction());
    }

    protected SqlQuery renderPortableAggregateExpressionSum(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for aggregate expression type: " + aggregateExpression.getClass().getName() + "/" + aggregateExpression.getFunction());
    }

    protected SqlQuery renderPortableAggregateExpressionVarPop(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for aggregate expression type: " + aggregateExpression.getClass().getName() + "/" + aggregateExpression.getFunction());
    }

    protected SqlQuery renderPortableAggregateExpressionVarSamp(@NonNull PortableAggregateExpression<?> aggregateExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for aggregate expression type: " + aggregateExpression.getClass().getName() + "/" + aggregateExpression.getFunction());
    }

    protected SqlQuery renderMultisetAggExpression(@NonNull MultisetAggExpression multisetAggExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for expression type: " + multisetAggExpression.getClass().getName());
    }

    protected SqlQuery renderCaseWhenThenExpression(@NonNull CaseWhenThenExpression<?> cwtExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for expression type: " + cwtExpression.getClass().getName());
    }

    protected SqlQuery renderParenthesesExpression(@NonNull ParenthesesExpression<?> parenthesesExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for expression type: " + parenthesesExpression.getClass().getName());
    }

    protected SqlQuery renderJsonExpression(@NonNull JsonExpression jsonExpr, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for expression type: " + jsonExpr.getClass().getName());
    }

    protected SqlQuery renderJsonTextExpression(@NonNull JsonTextExpression jsonTextExpr, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for expression type: " + jsonTextExpr.getClass().getName());
    }

    protected SqlQuery renderJsonCondition(@NonNull JsonCondition jsonCondition, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for expression type: " + jsonCondition.getClass().getName());
    }

    protected SqlQuery renderNextvalExpression(@NonNull NextvalExpression nextvalExpression, ColumnContext columnContext) {
        throw new UnsupportedOperationException("Unable to create sql part for expression type: " + nextvalExpression.getClass().getName());
    }

    @Override
    public SqlQuery toSqlPart(@NonNull Order order) {
        return new SqlQueryBuilder()
                .append(toSqlPart(order.getExpression(), null))
                .append(order.isAsc() ? " ASC" : " DESC")
                .toSqlQuery();
    }

    @Override
    public SelectQueryData extractQueryData(SelectExpression<?> expression) {
        if (expression instanceof DefaultSelectExpression<?> selectExpression) {
            return selectExpression.toQueryData();
        }

        throw new IllegalArgumentException("Unsupported select expression type" + expression);
    }
}
