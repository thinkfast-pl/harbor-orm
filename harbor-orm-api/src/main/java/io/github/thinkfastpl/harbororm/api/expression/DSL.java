// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import io.github.thinkfastpl.harbororm.api.interval.Interval;
import io.github.thinkfastpl.harbororm.api.metadata.QColumn;
import io.github.thinkfastpl.harbororm.api.query.result.Record;
import lombok.NonNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Main entry point providing static factory methods for building type-safe SQL expressions,
 * conditions, functions, aggregates, window specifications, and SELECT queries.
 */
public class DSL {
    private static final Expression<Long> COUNT = new LiteralExpression<>(Long.class, "COUNT(*)");
    private static final Expression<?> ASTERISK = new LiteralExpression<>(Record.class, "*");

    /**
     * A constant condition that always evaluates to SQL {@code TRUE}.
     */
    public static final Condition TRUE = constant(Boolean.TRUE);
    /**
     * A constant condition that always evaluates to SQL {@code FALSE}.
     */
    public static final Condition FALSE = constant(Boolean.FALSE);

    /**
     * Creates an ascending order by column index.
     *
     * @param columnIndex the 1-based index of the column in the SELECT clause
     * @return an {@link Order} object representing ascending order
     */
    public static Order asc(int columnIndex) {
        return order(columnIndex, true);
    }

    /**
     * Creates a descending order by column index.
     *
     * @param columnIndex the 1-based index of the column in the SELECT clause
     * @return an {@link Order} object representing descending order
     */
    public static Order desc(int columnIndex) {
        return order(columnIndex, false);
    }

    /**
     * Creates an order by column index with specified direction.
     *
     * @param columnIndex the 1-based index of the column in the SELECT clause
     * @param asc         {@code true} for ascending order, {@code false} for descending order
     * @return an {@link Order} object representing the specified ordering
     */
    public static Order order(int columnIndex, boolean asc) {
        return new Order(new LiteralExpression<>(Object.class, String.valueOf(columnIndex)), asc);
    }

    /**
     * Creates an ascending order by column name.
     *
     * @param columnName the name of the column to order by
     * @return an {@link Order} object representing ascending order
     */
    public static Order asc(@NonNull String columnName) {
        return order(columnName, true);
    }

    /**
     * Creates a descending order by column name.
     *
     * @param columnName the name of the column to order by
     * @return an {@link Order} object representing descending order
     */
    public static Order desc(@NonNull String columnName) {
        return order(columnName, false);
    }

    /**
     * Creates an order by column name with specified direction.
     *
     * @param columnName the name of the column to order by
     * @param asc        {@code true} for ascending order, {@code false} for descending order
     * @return an {@link Order} object representing the specified ordering
     */
    public static Order order(@NonNull String columnName, boolean asc) {
        return new Order(new NameExpression<>(Object.class, columnName), asc);
    }

    /**
     * Creates an expression for a column by name.
     * <p>
     * This is useful when working with raw SQL queries where you need to reference
     * columns by their string names rather than using generated metadata classes.
     *
     * @param columnName the name of the column
     * @param <T>        the type of the column value
     * @return an expression representing the column
     */
    @SuppressWarnings("unchecked")
    public static <T> Expression<T> name(@NonNull String columnName) {
        return new NameExpression<>((Class<T>) Object.class, columnName);
    }

    /**
     * Creates an expression for a column by name with a specific type.
     *
     * @param type       the Java type of the column value
     * @param columnName the name of the column
     * @param <T>        the type of the column value
     * @return an expression representing the column
     */
    public static <T> Expression<T> name(@NonNull Class<T> type, @NonNull String columnName) {
        return new NameExpression<>(type, columnName);
    }

    /**
     * Returns the numeric code of the first character of the text.
     * <p>
     * In UTF8 encoding, returns the Unicode code point of the character.
     * In other multibyte encodings, the argument must be an ASCII character.
     * <p>
     * This is equivalent to the SQL {@code ASCII(text)} function.
     *
     * @param text the expression to get the ASCII code of
     * @return an expression representing the ASCII code of the first character
     */
    public static Expression<Integer> ascii(@NonNull Expression<String> text) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.ASCII, List.of(text), Integer.class);
    }

    /**
     * Returns the numeric code of the first character of the text.
     * <p>
     * In UTF8 encoding, returns the Unicode code point of the character.
     * In other multibyte encodings, the argument must be an ASCII character.
     * <p>
     * This is equivalent to the SQL {@code ASCII(text)} function.
     *
     * @param text the text to get the ASCII code of
     * @return an expression representing the ASCII code of the first character
     */
    public static Expression<Integer> ascii(String text) {
        return ascii(constant(text));
    }

    /**
     * Removes characters matching the given set from the start and end of the string.
     * <p>
     * Dialect behavior of the {@code characters} argument differs:
     * <ul>
     *   <li><b>PostgreSQL</b> and <b>H2</b> — rendered as {@code BTRIM(string, characters)};
     *       {@code characters} is treated as a <i>set</i>, any character in the set is stripped
     *       from both edges.</li>
     *   <li><b>MariaDB</b> — rendered as {@code TRIM(BOTH characters FROM string)}, so
     *       {@code characters} is treated as a <i>literal substring</i> that is repeatedly
     *       stripped from both edges.</li>
     * </ul>
     *
     * @param string     the expression representing the string to trim
     * @param characters the expression representing the characters to remove
     * @return an expression representing the trimmed string
     */
    public static Expression<String> btrim(@NonNull Expression<String> string, @NonNull Expression<String> characters) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.BTRIM, List.of(string, characters), String.class);
    }

    /**
     * Removes characters matching the given set from the start and end of the string.
     * <p>
     * See {@link #btrim(Expression, Expression)} for dialect-specific behavior of the
     * {@code characters} argument.
     *
     * @param string     the string to trim
     * @param characters the characters to remove
     * @return an expression representing the trimmed string
     */
    public static Expression<String> btrim(String string, String characters) {
        return btrim(constant(string), constant(characters));
    }

    /**
     * Removes characters matching the given set from the start and end of the string.
     * <p>
     * See {@link #btrim(Expression, Expression)} for dialect-specific behavior of the
     * {@code characters} argument.
     *
     * @param string     the expression representing the string to trim
     * @param characters the characters to remove
     * @return an expression representing the trimmed string
     */
    public static Expression<String> btrim(@NonNull Expression<String> string, String characters) {
        return btrim(string, constant(characters));
    }

    /**
     * Removes the longest string containing only spaces from the start and end of string.
     * <p>
     * This is equivalent to the SQL {@code BTRIM(string)} function.
     *
     * @param string the expression representing the string to trim
     * @return an expression representing the trimmed string
     */
    public static Expression<String> btrim(@NonNull Expression<String> string) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.BTRIM, List.of(string), String.class);
    }

    /**
     * Removes the longest string containing only spaces from the start and end of string.
     * <p>
     * This is equivalent to the SQL {@code BTRIM(string)} function.
     *
     * @param string the string to trim
     * @return an expression representing the trimmed string
     */
    public static Expression<String> btrim(String string) {
        return btrim(constant(string));
    }

    /**
     * Returns the number of bits in the string (8 times the octet_length).
     * <p>
     * This is equivalent to the SQL {@code BIT_LENGTH(text)} function.
     *
     * @param text the expression representing the string to get the bit length of
     * @return an expression representing the number of bits in the string
     */
    public static Expression<Integer> bitLength(@NonNull Expression<String> text) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.BIT_LENGTH, List.of(text), Integer.class);
    }

    /**
     * Returns the number of bits in the string (8 times the octet_length).
     * <p>
     * This is equivalent to the SQL {@code BIT_LENGTH(text)} function.
     *
     * @param text the string to get the bit length of
     * @return an expression representing the number of bits in the string
     */
    public static Expression<Integer> bitLength(String text) {
        return bitLength(constant(text));
    }

    public static Expression<Integer> octetLength(@NonNull Expression<String> text) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.OCTET_LENGTH, List.of(text), Integer.class);
    }

    public static Expression<Integer> octetLength(String text) {
        return octetLength(constant(text));
    }

    /**
     * Returns the character with the given code.
     * <p>
     * In UTF8 encoding the argument is treated as a Unicode code point.
     * In other multibyte encodings the argument must designate an ASCII character.
     * <p>
     * This is equivalent to the SQL {@code CHR(code)} function.
     *
     * @param code the expression representing the character code
     * @return an expression representing the character with the given code
     */
    public static Expression<String> chr(@NonNull Expression<Integer> code) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.CHR, List.of(code), String.class);
    }

    /**
     * Returns the character with the given code.
     * <p>
     * In UTF8 encoding the argument is treated as a Unicode code point.
     * In other multibyte encodings the argument must designate an ASCII character.
     * <p>
     * This is equivalent to the SQL {@code CHR(code)} function.
     *
     * @param code the character code
     * @return an expression representing the character with the given code
     */
    public static Expression<String> chr(Integer code) {
        return chr(constant(code));
    }

    /**
     * Returns the number of characters in the string.
     * <p>
     * This is equivalent to the SQL {@code CHAR_LENGTH(text)} function.
     *
     * @param text the expression representing the string to get the character length of
     * @return an expression representing the number of characters in the string
     */
    public static Expression<Integer> charLength(@NonNull Expression<String> text) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.CHAR_LENGTH, List.of(text), Integer.class);
    }

    /**
     * Returns the number of characters in the string.
     * <p>
     * This is equivalent to the SQL {@code CHAR_LENGTH(text)} function.
     *
     * @param text the string to get the character length of
     * @return an expression representing the number of characters in the string
     */
    public static Expression<Integer> charLength(String text) {
        return charLength(constant(text));
    }

    /**
     * Concatenates all the given string expressions.
     * <p>
     * This is equivalent to the SQL {@code CONCAT(string1, string2, ...)} function.
     *
     * @param expressions the string expressions to concatenate
     * @return an expression representing the concatenated string
     */
    @SafeVarargs
    public static Expression<String> concat(@NonNull Expression<String>... expressions) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.CONCAT, Arrays.asList(expressions), String.class);
    }

    /**
     * Concatenates all but the first argument, with separators.
     * <p>
     * The first argument is used as the separator string, and should not be NULL.
     * Other NULL arguments are ignored.
     * <p>
     * This is equivalent to the SQL {@code CONCAT_WS(separator, value1, value2, ...)} function.
     *
     * @param separator the separator expression to use between values
     * @param values    the expressions to concatenate with separators
     * @return an expression representing the concatenated string with separators
     */
    @SafeVarargs
    public static Expression<String> concatWs(@NonNull Expression<String> separator, @NonNull Expression<String>... values) {
        List<Expression<?>> allArgs = new ArrayList<>(values.length + 1);
        allArgs.add(separator);
        allArgs.addAll(Arrays.asList(values));
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.CONCAT_WS, allArgs, String.class);
    }

    /**
     * Concatenates all but the first argument, with separators.
     * <p>
     * The first argument is used as the separator string, and should not be NULL.
     * Other NULL arguments are ignored.
     * <p>
     * This is equivalent to the SQL {@code CONCAT_WS(separator, value1, value2, ...)} function.
     *
     * @param separator the separator expression to use between values
     * @param values    the expressions to concatenate with separators
     * @return an expression representing the concatenated string with separators
     */
    @SafeVarargs
    public static Expression<String> concatWs(@NonNull String separator, @NonNull Expression<String>... values) {
        return concatWs(constant(separator), values);
    }

    /**
     * Concatenates all but the first argument, with separators.
     * <p>
     * The first argument is used as the separator string, and should not be NULL.
     * Other NULL arguments are ignored.
     * <p>
     * This is equivalent to the SQL {@code CONCAT_WS(separator, value1, value2, ...)} function.
     *
     * @param separator the separator string to use between values
     * @param values    the values to concatenate with separators
     * @return an expression representing the concatenated string with separators
     */
    public static Expression<String> concatWs(String separator, String... values) {
        Expression<String>[] valueExpressions = new Expression[values.length];
        for (int i = 0; i < values.length; i++) {
            valueExpressions[i] = constant(values[i]);
        }
        return concatWs(constant(separator), valueExpressions);
    }

    /**
     * Returns the absolute value of a numeric expression.
     * <p>
     * This is equivalent to the SQL {@code ABS(expression)} function.
     *
     * @param <T>        the numeric type
     * @param expression the numeric expression to get the absolute value of
     * @return an expression representing the absolute value
     */
    public static <T extends Number> Expression<T> abs(@NonNull Expression<T> expression) {
        return new PortableFunctionExpression<T>(PortableFunctionExpression.Function.ABS, List.of(expression), expression.getJavaType());
    }

    /**
     * Returns the absolute value of a numeric value.
     * <p>
     * This is equivalent to the SQL {@code ABS(value)} function.
     *
     * @param <T>   the numeric type
     * @param clazz the class representing the numeric type
     * @param value the numeric value to get the absolute value of
     * @return an expression representing the absolute value
     */
    public static <T extends Number> Expression<T> abs(@NonNull Class<T> clazz, T value) {
        return abs(constant(clazz, value));
    }

    /**
     * Returns the absolute value of a Number.
     * <p>
     * This is equivalent to the SQL {@code ABS(value)} function.
     *
     * @param value the number to get the absolute value of
     * @return an expression representing the absolute value
     */
    public static Expression<Number> abs(Number value) {
        return abs(constant(value));
    }

    /**
     * Returns the absolute value of a Byte.
     * <p>
     * This is equivalent to the SQL {@code ABS(value)} function.
     *
     * @param value the byte value to get the absolute value of
     * @return an expression representing the absolute value
     */
    public static Expression<Byte> abs(Byte value) {
        return abs(constant(value));
    }

    /**
     * Returns the absolute value of a Short.
     * <p>
     * This is equivalent to the SQL {@code ABS(value)} function.
     *
     * @param value the short value to get the absolute value of
     * @return an expression representing the absolute value
     */
    public static Expression<Short> abs(Short value) {
        return abs(constant(value));
    }

    /**
     * Returns the absolute value of an Integer.
     * <p>
     * This is equivalent to the SQL {@code ABS(value)} function.
     *
     * @param value the integer value to get the absolute value of
     * @return an expression representing the absolute value
     */
    public static Expression<Integer> abs(Integer value) {
        return abs(constant(value));
    }

    /**
     * Returns the absolute value of a Long.
     * <p>
     * This is equivalent to the SQL {@code ABS(value)} function.
     *
     * @param value the long value to get the absolute value of
     * @return an expression representing the absolute value
     */
    public static Expression<Long> abs(Long value) {
        return abs(constant(value));
    }

    /**
     * Returns the absolute value of a Float.
     * <p>
     * This is equivalent to the SQL {@code ABS(value)} function.
     *
     * @param value the float value to get the absolute value of
     * @return an expression representing the absolute value
     */
    public static Expression<Float> abs(Float value) {
        return abs(constant(value));
    }

    /**
     * Returns the absolute value of a Double.
     * <p>
     * This is equivalent to the SQL {@code ABS(value)} function.
     *
     * @param value the double value to get the absolute value of
     * @return an expression representing the absolute value
     */
    public static Expression<Double> abs(Double value) {
        return abs(constant(value));
    }

    /**
     * Returns the absolute value of a BigDecimal.
     * <p>
     * This is equivalent to the SQL {@code ABS(value)} function.
     *
     * @param value the BigDecimal value to get the absolute value of
     * @return an expression representing the absolute value
     */
    public static Expression<BigDecimal> abs(BigDecimal value) {
        return abs(constant(value));
    }

    /**
     * Returns the absolute value of a BigInteger.
     * <p>
     * This is equivalent to the SQL {@code ABS(value)} function.
     *
     * @param value the BigInteger value to get the absolute value of
     * @return an expression representing the absolute value
     */
    public static Expression<BigInteger> abs(BigInteger value) {
        return abs(constant(value));
    }

    /**
     * Returns the arithmetic negation of a numeric expression.
     * <p>
     * Renders as the SQL unary minus operator: {@code (-expression)}.
     *
     * @param <T>        the numeric type
     * @param expression the numeric expression to negate
     * @return an expression representing the negated value
     */
    public static <T extends Number> Expression<T> neg(@NonNull Expression<T> expression) {
        return new PortableFunctionExpression<T>(PortableFunctionExpression.Function.NEG, List.of(expression), expression.getJavaType());
    }

    /**
     * Returns the arithmetic negation of a numeric value.
     * <p>
     * Renders as the SQL unary minus operator: {@code (-value)}.
     *
     * @param <T>   the numeric type
     * @param clazz the class representing the numeric type
     * @param value the numeric value to negate
     * @return an expression representing the negated value
     */
    public static <T extends Number> Expression<T> neg(@NonNull Class<T> clazz, T value) {
        return neg(constant(clazz, value));
    }

    /**
     * Returns the arithmetic negation of a Number.
     * <p>
     * Renders as the SQL unary minus operator: {@code (-value)}.
     *
     * @param value the number to negate
     * @return an expression representing the negated value
     */
    public static Expression<Number> neg(Number value) {
        return neg(constant(value));
    }

    /**
     * Returns the arithmetic negation of a Byte.
     * <p>
     * Renders as the SQL unary minus operator: {@code (-value)}.
     *
     * @param value the byte value to negate
     * @return an expression representing the negated value
     */
    public static Expression<Byte> neg(Byte value) {
        return neg(constant(value));
    }

    /**
     * Returns the arithmetic negation of a Short.
     * <p>
     * Renders as the SQL unary minus operator: {@code (-value)}.
     *
     * @param value the short value to negate
     * @return an expression representing the negated value
     */
    public static Expression<Short> neg(Short value) {
        return neg(constant(value));
    }

    /**
     * Returns the arithmetic negation of an Integer.
     * <p>
     * Renders as the SQL unary minus operator: {@code (-value)}.
     *
     * @param value the integer value to negate
     * @return an expression representing the negated value
     */
    public static Expression<Integer> neg(Integer value) {
        return neg(constant(value));
    }

    /**
     * Returns the arithmetic negation of a Long.
     * <p>
     * Renders as the SQL unary minus operator: {@code (-value)}.
     *
     * @param value the long value to negate
     * @return an expression representing the negated value
     */
    public static Expression<Long> neg(Long value) {
        return neg(constant(value));
    }

    /**
     * Returns the arithmetic negation of a Float.
     * <p>
     * Renders as the SQL unary minus operator: {@code (-value)}.
     *
     * @param value the float value to negate
     * @return an expression representing the negated value
     */
    public static Expression<Float> neg(Float value) {
        return neg(constant(value));
    }

    /**
     * Returns the arithmetic negation of a Double.
     * <p>
     * Renders as the SQL unary minus operator: {@code (-value)}.
     *
     * @param value the double value to negate
     * @return an expression representing the negated value
     */
    public static Expression<Double> neg(Double value) {
        return neg(constant(value));
    }

    /**
     * Returns the arithmetic negation of a BigDecimal.
     * <p>
     * Renders as the SQL unary minus operator: {@code (-value)}.
     *
     * @param value the BigDecimal value to negate
     * @return an expression representing the negated value
     */
    public static Expression<BigDecimal> neg(BigDecimal value) {
        return neg(constant(value));
    }

    /**
     * Returns the arithmetic negation of a BigInteger.
     * <p>
     * Renders as the SQL unary minus operator: {@code (-value)}.
     *
     * @param value the BigInteger value to negate
     * @return an expression representing the negated value
     */
    public static Expression<BigInteger> neg(BigInteger value) {
        return neg(constant(value));
    }

    /**
     * Returns the square of a numeric expression.
     * <p>
     * Renders as {@code (expression * expression)}.
     *
     * @param <T>        the numeric type
     * @param expression the numeric expression to square
     * @return an expression representing the squared value
     */
    public static <T extends Number> Expression<T> square(@NonNull Expression<T> expression) {
        return new PortableFunctionExpression<T>(PortableFunctionExpression.Function.SQUARE, List.of(expression), expression.getJavaType());
    }

    /**
     * Returns the square of a numeric value.
     * <p>
     * Renders as {@code (value * value)}.
     *
     * @param <T>   the numeric type
     * @param clazz the class representing the numeric type
     * @param value the numeric value to square
     * @return an expression representing the squared value
     */
    public static <T extends Number> Expression<T> square(@NonNull Class<T> clazz, T value) {
        return square(constant(clazz, value));
    }

    /**
     * Returns the square of a Number.
     * <p>
     * Renders as {@code (value * value)}.
     *
     * @param value the number to square
     * @return an expression representing the squared value
     */
    public static Expression<Number> square(Number value) {
        return square(constant(value));
    }

    /**
     * Returns the square of a Byte.
     * <p>
     * Renders as {@code (value * value)}.
     *
     * @param value the byte value to square
     * @return an expression representing the squared value
     */
    public static Expression<Byte> square(Byte value) {
        return square(constant(value));
    }

    /**
     * Returns the square of a Short.
     * <p>
     * Renders as {@code (value * value)}.
     *
     * @param value the short value to square
     * @return an expression representing the squared value
     */
    public static Expression<Short> square(Short value) {
        return square(constant(value));
    }

    /**
     * Returns the square of an Integer.
     * <p>
     * Renders as {@code (value * value)}.
     *
     * @param value the integer value to square
     * @return an expression representing the squared value
     */
    public static Expression<Integer> square(Integer value) {
        return square(constant(value));
    }

    /**
     * Returns the square of a Long.
     * <p>
     * Renders as {@code (value * value)}.
     *
     * @param value the long value to square
     * @return an expression representing the squared value
     */
    public static Expression<Long> square(Long value) {
        return square(constant(value));
    }

    /**
     * Returns the square of a Float.
     * <p>
     * Renders as {@code (value * value)}.
     *
     * @param value the float value to square
     * @return an expression representing the squared value
     */
    public static Expression<Float> square(Float value) {
        return square(constant(value));
    }

    /**
     * Returns the square of a Double.
     * <p>
     * Renders as {@code (value * value)}.
     *
     * @param value the double value to square
     * @return an expression representing the squared value
     */
    public static Expression<Double> square(Double value) {
        return square(constant(value));
    }

    /**
     * Returns the square of a BigDecimal.
     * <p>
     * Renders as {@code (value * value)}.
     *
     * @param value the BigDecimal value to square
     * @return an expression representing the squared value
     */
    public static Expression<BigDecimal> square(BigDecimal value) {
        return square(constant(value));
    }

    /**
     * Returns the square of a BigInteger.
     * <p>
     * Renders as {@code (value * value)}.
     *
     * @param value the BigInteger value to square
     * @return an expression representing the squared value
     */
    public static Expression<BigInteger> square(BigInteger value) {
        return square(constant(value));
    }

    /**
     * Adds a list of numeric expressions together using arithmetic addition.
     * <p>
     * This chains the expressions with the {@code +} operator.
     *
     * @param <T>         the numeric type
     * @param expressions the list of numeric expressions to add together
     * @return an expression representing the sum of all expressions
     * @throws IllegalArgumentException if no expressions are provided
     */
    public static <T extends Number> Expression<T> add(@NonNull List<Expression<T>> expressions) {
        if (expressions.isEmpty()) {
            throw new IllegalArgumentException("No expressions provided");
        }

        final Iterator<Expression<T>> iterator = expressions.iterator();
        Expression<T> expression = iterator.next();

        while (iterator.hasNext()) {
            expression = expression.add(iterator.next());
        }

        return expression;
    }

    /**
     * Aggregates values into an array.
     * <p>
     * This is equivalent to the SQL {@code ARRAY_AGG(expression)} aggregate function.
     *
     * @param <T>        the element type
     * @param expression the expression to aggregate into an array
     * @return an expression representing the aggregated array
     */
    public static <T> Expression<T[]> arrayAgg(@NonNull Expression<T> expression) {
        return new PortableAggregateExpression<>(createArrayClass(expression.getJavaType()), AggregateQuantifier.NONE, PortableAggregateExpression.Function.ARRAY_AGG, List.of(expression));
    }

    /**
     * Aggregates values into an array with a specified ordering.
     * <p>
     * This is equivalent to the SQL {@code ARRAY_AGG(expression ORDER BY ...)} aggregate function.
     *
     * @param <T>        the element type
     * @param expression the expression to aggregate into an array
     * @param orders     the ordering to apply within the aggregate
     * @return an expression representing the aggregated array
     */
    public static <T> Expression<T[]> arrayAgg(@NonNull Expression<T> expression, @NonNull List<Order> orders) {
        return arrayAggWhere(expression, Collections.emptyList(), orders);
    }

    /**
     * Aggregates values into an array with a filter condition.
     * <p>
     * This is equivalent to the SQL {@code ARRAY_AGG(expression) FILTER (WHERE ...)} aggregate function.
     *
     * @param <T>              the element type
     * @param expression       the expression to aggregate into an array
     * @param filterConditions the filter conditions to apply to the aggregate
     * @return an expression representing the filtered aggregated array
     */
    public static <T> Expression<T[]> arrayAggWhere(@NonNull Expression<T> expression, @NonNull List<Condition> filterConditions) {
        return arrayAggWhere(expression, filterConditions, Collections.emptyList());
    }

    /**
     * Aggregates values into an array with ordering and filter conditions.
     * <p>
     * This is equivalent to the SQL {@code ARRAY_AGG(expression ORDER BY ...) FILTER (WHERE ...)} aggregate function.
     *
     * @param <T>              the element type
     * @param expression       the expression to aggregate into an array
     * @param filterConditions the filter conditions to apply to the aggregate
     * @param orders           the ordering to apply within the aggregate
     * @return an expression representing the filtered and ordered aggregated array
     */
    public static <T> Expression<T[]> arrayAggWhere(@NonNull Expression<T> expression, @NonNull List<Condition> filterConditions, @NonNull List<Order> orders) {
        final PortableAggregateExpression<T[]> aggregateExpression = new PortableAggregateExpression<>(createArrayClass(expression.getJavaType()), AggregateQuantifier.NONE, PortableAggregateExpression.Function.ARRAY_AGG, List.of(expression));
        aggregateExpression.getOrders().addAll(orders);
        aggregateExpression.getFilter().addAll(filterConditions);
        return aggregateExpression;
    }

    /**
     * Concatenates the input values into a single string separated by the given separator.
     * <p>
     * This is equivalent to the SQL {@code GROUP_CONCAT(expression SEPARATOR separator)} function ({@code string_agg} on PostgreSQL).
     *
     * @param expression the expression whose values are concatenated
     * @param separator  the separator placed between concatenated values
     * @return an expression representing the concatenated string
     */
    public static Expression<String> stringAgg(@NonNull Expression<?> expression, @NonNull Expression<String> separator) {
        return new PortableAggregateExpression<>(String.class, AggregateQuantifier.NONE, PortableAggregateExpression.Function.GROUP_CONCAT, List.of(expression, separator));
    }

    /**
     * Concatenates the input values into a single string separated by the given separator.
     * <p>
     * This is equivalent to the SQL {@code GROUP_CONCAT(expression SEPARATOR separator)} function ({@code string_agg} on PostgreSQL).
     *
     * @param expression the expression whose values are concatenated
     * @param separator  the separator placed between concatenated values
     * @return an expression representing the concatenated string
     */
    public static Expression<String> stringAgg(@NonNull Expression<?> expression, @NonNull String separator) {
        return groupConcat(expression, constant(separator));
    }

    /**
     * Concatenates the input values into a single string separated by the given separator.
     * <p>
     * This is equivalent to the SQL {@code GROUP_CONCAT(expression SEPARATOR separator)} function ({@code string_agg} on PostgreSQL).
     *
     * @param expression the expression whose values are concatenated
     * @param separator  the separator placed between concatenated values
     * @param orderBy    the ordering applied to the concatenated values
     * @return an expression representing the concatenated string
     */
    public static Expression<String> stringAgg(@NonNull Expression<?> expression, @NonNull Expression<String> separator, @NonNull Order orderBy) {
        var expr = new PortableAggregateExpression<>(String.class, AggregateQuantifier.NONE, PortableAggregateExpression.Function.GROUP_CONCAT, List.of(expression, separator));
        expr.getOrders().add(orderBy);
        return expr;
    }

    /**
     * Concatenates the input values into a single string separated by the given separator.
     * <p>
     * This is equivalent to the SQL {@code GROUP_CONCAT(expression SEPARATOR separator)} function ({@code string_agg} on PostgreSQL).
     *
     * @param expression the expression whose values are concatenated
     * @param separator  the separator placed between concatenated values
     * @param orderBy    the ordering applied to the concatenated values
     * @return an expression representing the concatenated string
     */
    public static Expression<String> stringAgg(@NonNull Expression<?> expression, @NonNull String separator, @NonNull Order orderBy) {
        return groupConcat(expression, constant(separator), orderBy);
    }

    /**
     * Concatenates the distinct input values into a single string separated by the given separator.
     *
     * @param expression the expression whose distinct values are concatenated
     * @param separator  the separator placed between concatenated values
     * @return an expression representing the concatenated string
     */
    public static Expression<String> stringAggDistinct(@NonNull Expression<?> expression, @NonNull Expression<String> separator) {
        return new PortableAggregateExpression<>(String.class, AggregateQuantifier.DISTINCT, PortableAggregateExpression.Function.GROUP_CONCAT, List.of(expression, separator));
    }

    /**
     * Concatenates the distinct input values into a single string separated by the given separator.
     *
     * @param expression the expression whose distinct values are concatenated
     * @param separator  the separator placed between concatenated values
     * @return an expression representing the concatenated string
     */
    public static Expression<String> stringAggDistinct(@NonNull Expression<?> expression, @NonNull String separator) {
        return groupConcatDistinct(expression, constant(separator));
    }

    /**
     * Concatenates the distinct values from the rows matching the filter conditions into a single string separated by the given separator.
     *
     * @param expression the expression whose distinct values are concatenated
     * @param separator  the separator placed between concatenated values
     * @param condition  the filter condition
     * @return an expression representing the concatenated string
     */
    public static Expression<String> stringAggDistinctWhere(@NonNull Expression<?> expression, @NonNull String separator, @NonNull Condition condition) {
        return groupConcatDistinctWhere(expression, separator, List.of(condition));
    }

    /**
     * Concatenates the distinct values from the rows matching the filter conditions into a single string separated by the given separator.
     *
     * @param expression the expression whose distinct values are concatenated
     * @param separator  the separator placed between concatenated values
     * @param conditions the filter conditions combined with {@code AND}
     * @return an expression representing the concatenated string
     */
    public static Expression<String> stringAggDistinctWhere(@NonNull Expression<?> expression, @NonNull String separator, @NonNull Condition... conditions) {
        return groupConcatDistinctWhere(expression, separator, Arrays.asList(conditions));
    }

    /**
     * Concatenates the distinct values from the rows matching the filter conditions into a single string separated by the given separator.
     *
     * @param expression the expression whose distinct values are concatenated
     * @param separator  the separator placed between concatenated values
     * @param conditions the filter conditions combined with {@code AND}
     * @return an expression representing the concatenated string
     */
    public static Expression<String> stringAggDistinctWhere(@NonNull Expression<?> expression, @NonNull String separator, @NonNull List<Condition> conditions) {
        final var expr = new PortableAggregateExpression<>(String.class, AggregateQuantifier.DISTINCT, PortableAggregateExpression.Function.GROUP_CONCAT, List.of(expression, constant(separator)));
        expr.getFilter().addAll(conditions);
        return expr;
    }

    /**
     * Concatenates the values from the rows matching the filter conditions into a single string separated by the given separator.
     *
     * @param expression the expression whose values are concatenated
     * @param separator  the separator placed between concatenated values
     * @param condition  the filter condition
     * @return an expression representing the concatenated string
     */
    public static Expression<String> stringAggWhere(@NonNull Expression<?> expression, @NonNull String separator, @NonNull Condition condition) {
        return groupConcatWhere(expression, separator, List.of(condition));
    }

    /**
     * Concatenates the values from the rows matching the filter conditions into a single string separated by the given separator.
     *
     * @param expression the expression whose values are concatenated
     * @param separator  the separator placed between concatenated values
     * @param conditions the filter conditions combined with {@code AND}
     * @return an expression representing the concatenated string
     */
    public static Expression<String> stringAggWhere(@NonNull Expression<?> expression, @NonNull String separator, @NonNull Condition... conditions) {
        return groupConcatWhere(expression, separator, Arrays.asList(conditions));
    }

    /**
     * Concatenates the values from the rows matching the filter conditions into a single string separated by the given separator.
     *
     * @param expression the expression whose values are concatenated
     * @param separator  the separator placed between concatenated values
     * @param conditions the filter conditions combined with {@code AND}
     * @return an expression representing the concatenated string
     */
    public static Expression<String> stringAggWhere(@NonNull Expression<?> expression, @NonNull String separator, @NonNull List<Condition> conditions) {
        final var expr = new PortableAggregateExpression<>(String.class, AggregateQuantifier.NONE, PortableAggregateExpression.Function.GROUP_CONCAT, List.of(expression, constant(separator)));
        expr.getFilter().addAll(conditions);
        return expr;
    }

    /**
     * Returns the cube root of a numeric expression.
     * <p>
     * This is equivalent to the SQL {@code CBRT(value)} function.
     *
     * @param <T>   the numeric type
     * @param value the numeric expression to get the cube root of
     * @return an expression representing the cube root
     */
    public static <T extends Number> Expression<T> cbrt(@NonNull Expression<T> value) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.CBRT, List.of(value), value.getJavaType());
    }

    /**
     * Returns the cube root of a numeric value.
     * <p>
     * This is equivalent to the SQL {@code CBRT(value)} function.
     *
     * @param <T>   the numeric type
     * @param clazz the class representing the numeric type
     * @param value the numeric value to get the cube root of
     * @return an expression representing the cube root
     */
    public static <T extends Number> Expression<T> cbrt(@NonNull Class<T> clazz, T value) {
        return cbrt(constant(clazz, value));
    }

    /**
     * Returns the cube root of a Float.
     * <p>
     * This is equivalent to the SQL {@code CBRT(value)} function.
     *
     * @param value the float value to get the cube root of
     * @return an expression representing the cube root
     */
    public static Expression<Float> cbrt(Float value) {
        return cbrt(constant(value));
    }

    /**
     * Returns the cube root of a Double.
     * <p>
     * This is equivalent to the SQL {@code CBRT(value)} function.
     *
     * @param value the double value to get the cube root of
     * @return an expression representing the cube root
     */
    public static Expression<Double> cbrt(Double value) {
        return cbrt(constant(value));
    }

    /**
     * Returns the cube root of a BigDecimal.
     * <p>
     * This is equivalent to the SQL {@code CBRT(value)} function.
     *
     * @param value the BigDecimal value to get the cube root of
     * @return an expression representing the cube root
     */
    public static Expression<BigDecimal> cbrt(BigDecimal value) {
        return cbrt(constant(value));
    }

    /**
     * Returns the nearest integer greater than or equal to a numeric expression.
     * <p>
     * This is equivalent to the SQL {@code CEIL(value)} function.
     *
     * @param <T>   the numeric type
     * @param value the numeric expression to get the ceiling of
     * @return an expression representing the ceiling value
     */
    public static <T extends Number> Expression<T> ceil(@NonNull Expression<T> value) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.CEIL, List.of(value), value.getJavaType());
    }

    /**
     * Returns the nearest integer greater than or equal to a numeric value.
     * <p>
     * This is equivalent to the SQL {@code CEIL(value)} function.
     *
     * @param <T>   the numeric type
     * @param clazz the class representing the numeric type
     * @param value the numeric value to get the ceiling of
     * @return an expression representing the ceiling value
     */
    public static <T extends Number> Expression<T> ceil(@NonNull Class<T> clazz, T value) {
        return ceil(constant(clazz, value));
    }

    /**
     * Returns the nearest integer greater than or equal to a Float.
     * <p>
     * This is equivalent to the SQL {@code CEIL(value)} function.
     *
     * @param value the float value to get the ceiling of
     * @return an expression representing the ceiling value
     */
    public static Expression<Float> ceil(Float value) {
        return ceil(constant(value));
    }

    /**
     * Returns the nearest integer greater than or equal to a Double.
     * <p>
     * This is equivalent to the SQL {@code CEIL(value)} function.
     *
     * @param value the double value to get the ceiling of
     * @return an expression representing the ceiling value
     */
    public static Expression<Double> ceil(Double value) {
        return ceil(constant(value));
    }

    /**
     * Returns the nearest integer greater than or equal to a BigDecimal.
     * <p>
     * This is equivalent to the SQL {@code CEIL(value)} function.
     *
     * @param value the BigDecimal value to get the ceiling of
     * @return an expression representing the ceiling value
     */
    public static Expression<BigDecimal> ceil(BigDecimal value) {
        return ceil(constant(value));
    }

    /**
     * Begins building a SQL CASE expression with the first WHEN/THEN clause.
     * <p>
     * This is equivalent to starting a SQL {@code CASE WHEN condition THEN value ... END} expression.
     *
     * @param <T>  the result type
     * @param when the condition for the first WHEN clause
     * @param then the expression to return when the condition is true
     * @return a {@link CaseBuilder} for chaining additional WHEN/THEN clauses or an ELSE clause
     */
    public static <T> CaseBuilder<T> caseWhen(@NonNull Condition when, @NonNull Expression<T> then) {
        return new CaseBuilderImpl<T>(then.getJavaType()).whenThen(when, then);
    }

    @SuppressWarnings("unchecked")
    static <T> Class<T[]> createArrayClass(@NonNull Class<T> clazz) {
        return (Class<T[]>) java.lang.reflect.Array.newInstance(clazz, 0).getClass();
    }

    /**
     * Creates a typed SQL NULL expression.
     *
     * @param <T>   the type of the null expression
     * @param clazz the Java class representing the null expression's type
     * @return an expression representing SQL NULL
     */
    public static <T> Expression<T> nil(Class<T> clazz) {
        return new ConstantExpression<>(clazz, null);
    }

    /**
     * Creates an expression referencing a column from the {@code EXCLUDED} pseudo-table.
     *
     * <p>Used in {@code ON CONFLICT ... DO UPDATE SET} clauses to reference the values
     * from the row that was proposed for insertion but conflicted.
     *
     * <p>Example: {@code DSL.excluded(TABLE.name)} renders as {@code EXCLUDED."name"}.
     *
     * @param <T>    the column's Java type
     * @param column the column to reference from the excluded row
     * @return an expression representing {@code EXCLUDED."column_name"}
     */
    public static <T> Expression<T> excluded(@NonNull QColumn<T> column) {
        return new ExcludedColumnExpression<>(column);
    }

    /**
     * Creates a constant Number expression.
     *
     * @param value the Number value
     * @return an expression representing the constant value
     */
    public static Expression<Number> constant(Number value) {
        return new ConstantExpression<>(Number.class, value);
    }

    public static Expression<LocalDate> constant(LocalDate value) {
        return new ConstantExpression<>(LocalDate.class, value);
    }

    public static Expression<LocalDateTime> constant(LocalDateTime value) {
        return new ConstantExpression<>(LocalDateTime.class, value);
    }

    public static Expression<OffsetDateTime> constant(OffsetDateTime value) {
        return new ConstantExpression<>(OffsetDateTime.class, value);
    }

    public static Expression<LocalTime> constant(LocalTime value) {
        return new ConstantExpression<>(LocalTime.class, value);
    }

    /**
     * Creates a typed constant expression.
     *
     * @param <T>   the type of the constant
     * @param clazz the Java class representing the constant's type
     * @param value the constant value
     * @return an expression representing the constant value
     */
    public static <T> Expression<T> constant(Class<T> clazz, Object value) {
        return new ConstantExpression<>(clazz, clazz.cast(value));
    }

    /**
     * Creates a constant Byte expression.
     *
     * @param value the Byte value
     * @return an expression representing the constant value
     */
    public static Expression<Byte> constant(Byte value) {
        return new ConstantExpression<>(Byte.class, value);
    }

    /**
     * Creates a constant Short expression.
     *
     * @param value the Short value
     * @return an expression representing the constant value
     */
    public static Expression<Short> constant(Short value) {
        return new ConstantExpression<>(Short.class, value);
    }

    /**
     * Creates a constant Integer expression.
     *
     * @param value the Integer value
     * @return an expression representing the constant value
     */
    public static Expression<Integer> constant(Integer value) {
        return new ConstantExpression<>(Integer.class, value);
    }

    /**
     * Creates a constant Long expression.
     *
     * @param value the Long value
     * @return an expression representing the constant value
     */
    public static Expression<Long> constant(Long value) {
        return new ConstantExpression<>(Long.class, value);
    }

    /**
     * Creates a constant Float expression.
     *
     * @param value the Float value
     * @return an expression representing the constant value
     */
    public static Expression<Float> constant(Float value) {
        return new ConstantExpression<>(Float.class, value);
    }

    /**
     * Creates a constant Double expression.
     *
     * @param value the Double value
     * @return an expression representing the constant value
     */
    public static Expression<Double> constant(Double value) {
        return new ConstantExpression<>(Double.class, value);
    }

    /**
     * Creates a constant BigDecimal expression.
     *
     * @param value the BigDecimal value
     * @return an expression representing the constant value
     */
    public static Expression<BigDecimal> constant(BigDecimal value) {
        return new ConstantExpression<>(BigDecimal.class, value);
    }

    /**
     * Creates a constant BigInteger expression.
     *
     * @param value the BigInteger value
     * @return an expression representing the constant value
     */
    public static Expression<BigInteger> constant(BigInteger value) {
        return new ConstantExpression<>(BigInteger.class, value);
    }

    /**
     * Creates a constant Boolean condition.
     *
     * @param value the Boolean value
     * @return a condition representing the constant boolean value
     */
    public static Condition constant(Boolean value) {
        return new BooleanConstantExpression(value);
    }

    /**
     * Creates a constant Character expression.
     *
     * @param value the Character value
     * @return an expression representing the constant value
     */
    public static Expression<Character> constant(Character value) {
        return new ConstantExpression<>(Character.class, value);
    }

    /**
     * Creates a constant String expression.
     *
     * @param value the String value
     * @return an expression representing the constant value
     */
    public static Expression<String> constant(String value) {
        return new ConstantExpression<>(String.class, value);
    }

    /**
     * Returns the first of its arguments that is not null.
     * <p>
     * This is equivalent to the SQL {@code COALESCE(expression1, expression2)} function.
     *
     * @param <T>         the type of the expressions
     * @param expression1 the first expression to evaluate
     * @param expression2 the second expression to evaluate if the first is null
     * @return an expression representing the first non-null value
     */
    public static <T> Expression<T> coalesce(@NonNull Expression<T> expression1, @NonNull Expression<T> expression2) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.COALESCE, List.of(expression1, expression2), expression1.getJavaType());
    }

    /**
     * Returns the first of its arguments that is not null.
     * <p>
     * This is equivalent to the SQL {@code COALESCE(expression1, expression2, ...)} function.
     *
     * @param <T>              the type of the expressions
     * @param expression1      the first expression to evaluate
     * @param expression2      the second expression to evaluate if the first is null
     * @param otherExpressions additional expressions to evaluate if previous ones are null
     * @return an expression representing the first non-null value
     */
    @SafeVarargs
    public static <T> Expression<T> coalesce(@NonNull Expression<T> expression1, @NonNull Expression<T> expression2, @NonNull Expression<T>... otherExpressions) {
        List<Expression<?>> expressions = new ArrayList<>(otherExpressions.length + 2);
        expressions.add(expression1);
        expressions.add(expression2);
        expressions.addAll(Arrays.asList(otherExpressions));
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.COALESCE, expressions, expression1.getJavaType());
    }

    /**
     * Returns the first of its arguments that is not null.
     * <p>
     * This is equivalent to the SQL {@code COALESCE(expression1, expression2)} function.
     *
     * @param <T>         the type of the expressions
     * @param expression1 the first expression to evaluate
     * @param expression2 the constant value to use if the first expression is null
     * @return an expression representing the first non-null value
     */
    @SuppressWarnings("unchecked")
    public static <T> Expression<T> coalesce(@NonNull Expression<T> expression1, @NonNull T expression2) {
        return coalesce(expression1, new ConstantExpression<>((Class<T>) expression2.getClass(), expression2));
    }

    /**
     * Returns the first of its arguments that is not null.
     * <p>
     * This is equivalent to the SQL {@code COALESCE(expression1, expression2, ...)} function.
     *
     * @param <T>              the type of the expressions
     * @param expression1      the first expression to evaluate
     * @param expression2      the constant value to use if the first expression is null
     * @param otherExpressions additional constant values to use if previous values are null
     * @return an expression representing the first non-null value
     */
    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static <T> Expression<T> coalesce(@NonNull Expression<T> expression1, @NonNull T expression2, @NonNull T... otherExpressions) {
        List<Expression<?>> expressions = new ArrayList<>(otherExpressions.length + 2);
        expressions.add(expression1);
        expressions.add(new ConstantExpression<>((Class<T>) expression2.getClass(), expression2));
        for (T other : otherExpressions) {
            expressions.add(new ConstantExpression<>((Class<T>) other.getClass(), other));
        }
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.COALESCE, expressions, expression1.getJavaType());
    }

    // ==================== CONDITIONAL FUNCTIONS ====================

    /**
     * Returns null if the two values are equal, otherwise returns the first value.
     *
     * @param value1 the first value
     * @param value2 the second value
     * @param <T>    the value type
     * @return nullif expression
     */
    @SuppressWarnings("unchecked")
    public static <T> Expression<T> nullif(@NonNull Expression<T> value1, @NonNull Expression<T> value2) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.NULLIF, List.of(value1, value2), value1.getJavaType());
    }

    /**
     * Returns null if the two values are equal, otherwise returns the first value.
     *
     * @param value1 the first value expression
     * @param value2 the second value
     * @param <T>    the value type
     * @return nullif expression
     */
    @SuppressWarnings("unchecked")
    public static <T> Expression<T> nullif(@NonNull Expression<T> value1, @NonNull T value2) {
        return nullif(value1, new ConstantExpression<>((Class<T>) value2.getClass(), value2));
    }

    /**
     * Returns the first value if it is not null, otherwise returns the default value.
     *
     * @param value        the value to check
     * @param defaultValue the default value
     * @param <T>          the value type
     * @return ifnull expression
     */
    @SuppressWarnings("unchecked")
    public static <T> Expression<T> ifNull(@NonNull Expression<T> value, @NonNull Expression<T> defaultValue) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.IFNULL, List.of(value, defaultValue), value.getJavaType());
    }

    /**
     * Returns the first value if it is not null, otherwise returns the default value.
     *
     * @param value        the value to check
     * @param defaultValue the default value
     * @param <T>          the value type
     * @return ifnull expression
     */
    @SuppressWarnings("unchecked")
    public static <T> Expression<T> ifNull(@NonNull Expression<T> value, @NonNull T defaultValue) {
        return ifNull(value, new ConstantExpression<>((Class<T>) defaultValue.getClass(), defaultValue));
    }

    /**
     * Returns the first value if it is not null, otherwise returns the default value (alias for ifNull).
     *
     * @param value        the value to check
     * @param defaultValue the default value
     * @param <T>          the value type
     * @return nvl expression
     */
    @SuppressWarnings("unchecked")
    public static <T> Expression<T> nvl(@NonNull Expression<T> value, @NonNull Expression<T> defaultValue) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.NVL, List.of(value, defaultValue), value.getJavaType());
    }

    /**
     * Returns the first value if it is not null, otherwise returns the default value (alias for ifNull).
     *
     * @param value        the value to check
     * @param defaultValue the default value
     * @param <T>          the value type
     * @return nvl expression
     */
    @SuppressWarnings("unchecked")
    public static <T> Expression<T> nvl(@NonNull Expression<T> value, @NonNull T defaultValue) {
        return nvl(value, new ConstantExpression<>((Class<T>) defaultValue.getClass(), defaultValue));
    }

    /**
     * Returns {@code thenValue} when {@code condition} is true, otherwise {@code elseValue}.
     * <p>
     * Renders as {@code CASE WHEN condition THEN thenValue ELSE elseValue END} on H2 and PostgreSQL,
     * and as {@code IF(condition, thenValue, elseValue)} on MariaDB.
     *
     * @param condition the condition to evaluate
     * @param thenValue the value returned when the condition is true
     * @param elseValue the value returned when the condition is false
     * @param <T>       the result type
     * @return an expression representing the conditional value
     */
    public static <T> Expression<T> iif(@NonNull Condition condition, @NonNull Expression<T> thenValue, @NonNull Expression<T> elseValue) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.IIF, List.of(condition, thenValue, elseValue), thenValue.getJavaType());
    }

    /**
     * Returns {@code thenValue} when {@code condition} is true, otherwise {@code elseValue}.
     * <p>
     * Renders as {@code CASE WHEN condition THEN thenValue ELSE elseValue END} on H2 and PostgreSQL,
     * and as {@code IF(condition, thenValue, elseValue)} on MariaDB.
     *
     * @param condition the condition to evaluate
     * @param thenValue the expression returned when the condition is true
     * @param elseValue the constant value returned when the condition is false
     * @param <T>       the result type
     * @return an expression representing the conditional value
     */
    @SuppressWarnings("unchecked")
    public static <T> Expression<T> iif(@NonNull Condition condition, @NonNull Expression<T> thenValue, @NonNull T elseValue) {
        return iif(condition, thenValue, new ConstantExpression<>((Class<T>) elseValue.getClass(), elseValue));
    }

    /**
     * Returns {@code thenValue} when {@code condition} is true, otherwise {@code elseValue}.
     * <p>
     * Renders as {@code CASE WHEN condition THEN thenValue ELSE elseValue END} on H2 and PostgreSQL,
     * and as {@code IF(condition, thenValue, elseValue)} on MariaDB.
     *
     * @param condition the condition to evaluate
     * @param thenValue the constant value returned when the condition is true
     * @param elseValue the expression returned when the condition is false
     * @param <T>       the result type
     * @return an expression representing the conditional value
     */
    @SuppressWarnings("unchecked")
    public static <T> Expression<T> iif(@NonNull Condition condition, @NonNull T thenValue, @NonNull Expression<T> elseValue) {
        return iif(condition, new ConstantExpression<>((Class<T>) thenValue.getClass(), thenValue), elseValue);
    }

    // ==================== RANDOM ====================

    /**
     * Generates a random UUID server-side.
     * <p>
     * Renders as {@code random_uuid()} on H2, {@code gen_random_uuid()} on PostgreSQL,
     * and {@code uuid()} on MariaDB.
     *
     * @return an expression representing a freshly generated UUID
     */
    public static Expression<UUID> uuid() {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.UUID, List.of(), UUID.class);
    }

    /**
     * Converts radians to degrees.
     * <p>
     * This is equivalent to the SQL {@code DEGREES(value)} function.
     *
     * @param <T>   the numeric type
     * @param value the expression representing the value in radians
     * @return an expression representing the value in degrees
     */
    public static <T extends Number> Expression<T> degrees(@NonNull Expression<T> value) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.DEGREES, List.of(value), value.getJavaType());
    }

    /**
     * Converts radians to degrees.
     * <p>
     * This is equivalent to the SQL {@code DEGREES(value)} function.
     *
     * @param <T>   the numeric type
     * @param clazz the class representing the numeric type
     * @param value the value in radians
     * @return an expression representing the value in degrees
     */
    public static <T extends Number> Expression<T> degrees(@NonNull Class<T> clazz, T value) {
        return degrees(constant(clazz, value));
    }

    /**
     * Converts radians to degrees.
     * <p>
     * This is equivalent to the SQL {@code DEGREES(value)} function.
     *
     * @param value the float value in radians
     * @return an expression representing the value in degrees
     */
    public static Expression<Float> degrees(Float value) {
        return degrees(constant(value));
    }

    /**
     * Converts radians to degrees.
     * <p>
     * This is equivalent to the SQL {@code DEGREES(value)} function.
     *
     * @param value the double value in radians
     * @return an expression representing the value in degrees
     */
    public static Expression<Double> degrees(Double value) {
        return degrees(constant(value));
    }

    /**
     * Converts radians to degrees.
     * <p>
     * This is equivalent to the SQL {@code DEGREES(value)} function.
     *
     * @param value the BigDecimal value in radians
     * @return an expression representing the value in degrees
     */
    public static Expression<BigDecimal> degrees(BigDecimal value) {
        return degrees(constant(value));
    }

    /**
     * Returns Euler's number (approximately 2.71828182846).
     * <p>
     * Renders as {@code exp(1)}, which is supported on all dialects.
     *
     * @return an expression representing Euler's number
     */
    public static Expression<Double> e() {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.E, List.of(), Double.class);
    }

    /**
     * Returns the exponential of a numeric expression (e raised to the given power).
     * <p>
     * This is equivalent to the SQL {@code EXP(value)} function.
     *
     * @param <T>   the numeric type
     * @param value the numeric expression representing the exponent
     * @return an expression representing e raised to the given power
     */
    public static <T extends Number> Expression<T> exp(@NonNull Expression<T> value) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.EXP, List.of(value), value.getJavaType());
    }

    /**
     * Returns the exponential of a numeric value (e raised to the given power).
     * <p>
     * This is equivalent to the SQL {@code EXP(value)} function.
     *
     * @param <T>   the numeric type
     * @param clazz the class representing the numeric type
     * @param value the exponent value
     * @return an expression representing e raised to the given power
     */
    public static <T extends Number> Expression<T> exp(@NonNull Class<T> clazz, T value) {
        return exp(constant(clazz, value));
    }

    /**
     * Returns the exponential of a Float (e raised to the given power).
     * <p>
     * This is equivalent to the SQL {@code EXP(value)} function.
     *
     * @param value the float exponent value
     * @return an expression representing e raised to the given power
     */
    public static Expression<Float> exp(Float value) {
        return exp(constant(value));
    }

    /**
     * Returns the exponential of a Double (e raised to the given power).
     * <p>
     * This is equivalent to the SQL {@code EXP(value)} function.
     *
     * @param value the double exponent value
     * @return an expression representing e raised to the given power
     */
    public static Expression<Double> exp(Double value) {
        return exp(constant(value));
    }

    /**
     * Returns the exponential of a BigDecimal (e raised to the given power).
     * <p>
     * This is equivalent to the SQL {@code EXP(value)} function.
     *
     * @param value the BigDecimal exponent value
     * @return an expression representing e raised to the given power
     */
    public static Expression<BigDecimal> exp(BigDecimal value) {
        return exp(constant(value));
    }

    /**
     * Returns the nearest integer less than or equal to a numeric expression.
     * <p>
     * This is equivalent to the SQL {@code FLOOR(value)} function.
     *
     * @param <T>   the numeric type
     * @param value the numeric expression to get the floor of
     * @return an expression representing the floor value
     */
    public static <T extends Number> Expression<T> floor(@NonNull Expression<T> value) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.FLOOR, List.of(value), value.getJavaType());
    }

    /**
     * Returns the nearest integer less than or equal to a numeric value.
     * <p>
     * This is equivalent to the SQL {@code FLOOR(value)} function.
     *
     * @param <T>   the numeric type
     * @param clazz the class representing the numeric type
     * @param value the numeric value to get the floor of
     * @return an expression representing the floor value
     */
    public static <T extends Number> Expression<T> floor(@NonNull Class<T> clazz, T value) {
        return floor(constant(clazz, value));
    }

    /**
     * Returns the nearest integer less than or equal to a Float.
     * <p>
     * This is equivalent to the SQL {@code FLOOR(value)} function.
     *
     * @param value the float value to get the floor of
     * @return an expression representing the floor value
     */
    public static Expression<Float> floor(Float value) {
        return floor(constant(value));
    }

    /**
     * Returns the nearest integer less than or equal to a Double.
     * <p>
     * This is equivalent to the SQL {@code FLOOR(value)} function.
     *
     * @param value the double value to get the floor of
     * @return an expression representing the floor value
     */
    public static Expression<Double> floor(Double value) {
        return floor(constant(value));
    }

    /**
     * Returns the nearest integer less than or equal to a BigDecimal.
     * <p>
     * This is equivalent to the SQL {@code FLOOR(value)} function.
     *
     * @param value the BigDecimal value to get the floor of
     * @return an expression representing the floor value
     */
    public static Expression<BigDecimal> floor(BigDecimal value) {
        return floor(constant(value));
    }

    /**
     * Returns the natural logarithm of a numeric expression.
     * <p>
     * This is equivalent to the SQL {@code LN(value)} function.
     *
     * @param <T>   the numeric type
     * @param value the numeric expression to get the natural logarithm of
     * @return an expression representing the natural logarithm
     */
    public static <T extends Number> Expression<T> ln(@NonNull Expression<T> value) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.LN, List.of(value), value.getJavaType());
    }

    /**
     * Returns the natural logarithm of a numeric value.
     * <p>
     * This is equivalent to the SQL {@code LN(value)} function.
     *
     * @param <T>   the numeric type
     * @param clazz the class representing the numeric type
     * @param value the numeric value to get the natural logarithm of
     * @return an expression representing the natural logarithm
     */
    public static <T extends Number> Expression<T> ln(@NonNull Class<T> clazz, T value) {
        return ln(constant(clazz, value));
    }

    /**
     * Returns the natural logarithm of a Float.
     * <p>
     * This is equivalent to the SQL {@code LN(value)} function.
     *
     * @param value the float value to get the natural logarithm of
     * @return an expression representing the natural logarithm
     */
    public static Expression<Float> ln(Float value) {
        return ln(constant(value));
    }

    /**
     * Returns the natural logarithm of a Double.
     * <p>
     * This is equivalent to the SQL {@code LN(value)} function.
     *
     * @param value the double value to get the natural logarithm of
     * @return an expression representing the natural logarithm
     */
    public static Expression<Double> ln(Double value) {
        return ln(constant(value));
    }

    /**
     * Returns the natural logarithm of a BigDecimal.
     * <p>
     * This is equivalent to the SQL {@code LN(value)} function.
     *
     * @param value the BigDecimal value to get the natural logarithm of
     * @return an expression representing the natural logarithm
     */
    public static Expression<BigDecimal> ln(BigDecimal value) {
        return ln(constant(value));
    }

    /**
     * Returns the base 10 logarithm of a numeric expression.
     * <p>
     * This is equivalent to the SQL {@code LOG10(value)} function.
     *
     * @param <T>   the numeric type
     * @param value the numeric expression to get the base 10 logarithm of
     * @return an expression representing the base 10 logarithm
     */
    public static <T extends Number> Expression<T> log10(@NonNull Expression<T> value) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.LOG10, List.of(value), value.getJavaType());
    }

    /**
     * Returns the base 10 logarithm of a numeric value.
     * <p>
     * This is equivalent to the SQL {@code LOG10(value)} function.
     *
     * @param <T>   the numeric type
     * @param clazz the class representing the numeric type
     * @param value the numeric value to get the base 10 logarithm of
     * @return an expression representing the base 10 logarithm
     */
    public static <T extends Number> Expression<T> log10(@NonNull Class<T> clazz, T value) {
        return log10(constant(clazz, value));
    }

    /**
     * Returns the base 10 logarithm of a Float.
     * <p>
     * This is equivalent to the SQL {@code LOG10(value)} function.
     *
     * @param value the float value to get the base 10 logarithm of
     * @return an expression representing the base 10 logarithm
     */
    public static Expression<Float> log10(Float value) {
        return log10(constant(value));
    }

    /**
     * Returns the base 10 logarithm of a Double.
     * <p>
     * This is equivalent to the SQL {@code LOG10(value)} function.
     *
     * @param value the double value to get the base 10 logarithm of
     * @return an expression representing the base 10 logarithm
     */
    public static Expression<Double> log10(Double value) {
        return log10(constant(value));
    }

    /**
     * Returns the base 10 logarithm of a BigDecimal.
     * <p>
     * This is equivalent to the SQL {@code LOG10(value)} function.
     *
     * @param value the BigDecimal value to get the base 10 logarithm of
     * @return an expression representing the base 10 logarithm
     */
    public static Expression<BigDecimal> log10(BigDecimal value) {
        return log10(constant(value));
    }

    /**
     * Returns the logarithm of x to the given base.
     * <p>
     * This is equivalent to the SQL {@code LOG(base, x)} function.
     *
     * @param <T>  the numeric type
     * @param base the base of the logarithm
     * @param x    the value to get the logarithm of
     * @return an expression representing the logarithm of x to the given base
     */
    public static <T extends Number> Expression<T> log(@NonNull Expression<T> base, @NonNull Expression<T> x) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.LOG, List.of(base, x), base.getJavaType());
    }

    /**
     * Returns the logarithm of x to the given base.
     * <p>
     * This is equivalent to the SQL {@code LOG(base, x)} function.
     *
     * @param <T>   the numeric type
     * @param clazz the class representing the numeric type
     * @param base  the base of the logarithm
     * @param x     the value to get the logarithm of
     * @return an expression representing the logarithm of x to the given base
     */
    public static <T extends Number> Expression<T> log(@NonNull Class<T> clazz, T base, T x) {
        return log(constant(clazz, base), constant(clazz, x));
    }

    /**
     * Returns the logarithm of x to the given base (Float values).
     * <p>
     * This is equivalent to the SQL {@code LOG(base, x)} function.
     *
     * @param base the base of the logarithm
     * @param x    the value to get the logarithm of
     * @return an expression representing the logarithm of x to the given base
     */
    public static Expression<Float> log(Float base, Float x) {
        return log(constant(base), constant(x));
    }

    /**
     * Returns the logarithm of x to the given base (Float values).
     *
     * @param base the base of the logarithm as an expression
     * @param x    the value to get the logarithm of
     * @return an expression representing the logarithm of x to the given base
     */
    public static Expression<Float> log(@NonNull Expression<Float> base, Float x) {
        return log(base, constant(x));
    }

    /**
     * Returns the logarithm of x to the given base (Double values).
     * <p>
     * This is equivalent to the SQL {@code LOG(base, x)} function.
     *
     * @param base the base of the logarithm
     * @param x    the value to get the logarithm of
     * @return an expression representing the logarithm of x to the given base
     */
    public static Expression<Double> log(Double base, Double x) {
        return log(constant(base), constant(x));
    }

    /**
     * Returns the logarithm of x to the given base (Double values).
     *
     * @param base the base of the logarithm as an expression
     * @param x    the value to get the logarithm of
     * @return an expression representing the logarithm of x to the given base
     */
    public static Expression<Double> log(@NonNull Expression<Double> base, Double x) {
        return log(base, constant(x));
    }

    /**
     * Returns the logarithm of x to the given base (BigDecimal values).
     * <p>
     * This is equivalent to the SQL {@code LOG(base, x)} function.
     *
     * @param base the base of the logarithm
     * @param x    the value to get the logarithm of
     * @return an expression representing the logarithm of x to the given base
     */
    public static Expression<BigDecimal> log(BigDecimal base, BigDecimal x) {
        return log(constant(base), constant(x));
    }

    /**
     * Returns the logarithm of x to the given base (BigDecimal values).
     *
     * @param base the base of the logarithm as an expression
     * @param x    the value to get the logarithm of
     * @return an expression representing the logarithm of x to the given base
     */
    public static Expression<BigDecimal> log(@NonNull Expression<BigDecimal> base, BigDecimal x) {
        return log(base, constant(x));
    }

    /**
     * Returns the remainder of y divided by x.
     * <p>
     * This is equivalent to the SQL {@code MOD(y, x)} function.
     *
     * @param <T> the numeric type
     * @param y   the dividend expression
     * @param x   the divisor expression
     * @return an expression representing the remainder
     */
    public static <T extends Number> Expression<T> mod(@NonNull Expression<T> y, @NonNull Expression<T> x) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.MOD, List.of(y, x), y.getJavaType());
    }

    /**
     * Returns the remainder of y divided by x.
     * <p>
     * This is equivalent to the SQL {@code MOD(y, x)} function.
     *
     * @param <T>   the numeric type
     * @param clazz the class representing the numeric type
     * @param y     the dividend
     * @param x     the divisor
     * @return an expression representing the remainder
     */
    public static <T extends Number> Expression<T> mod(@NonNull Class<T> clazz, T y, T x) {
        return mod(constant(clazz, y), constant(clazz, x));
    }

    /**
     * Returns the remainder of y divided by x (Byte values).
     * <p>
     * This is equivalent to the SQL {@code MOD(y, x)} function.
     *
     * @param y the dividend
     * @param x the divisor
     * @return an expression representing the remainder
     */
    public static Expression<Byte> mod(Byte y, Byte x) {
        return mod(constant(y), constant(x));
    }

    /**
     * Returns the remainder of y divided by x (Byte values).
     *
     * @param y the dividend expression
     * @param x the divisor
     * @return an expression representing the remainder
     */
    public static Expression<Byte> mod(@NonNull Expression<Byte> y, Byte x) {
        return mod(y, constant(x));
    }

    /**
     * Returns the remainder of y divided by x (Short values).
     * <p>
     * This is equivalent to the SQL {@code MOD(y, x)} function.
     *
     * @param y the dividend
     * @param x the divisor
     * @return an expression representing the remainder
     */
    public static Expression<Short> mod(Short y, Short x) {
        return mod(constant(y), constant(x));
    }

    /**
     * Returns the remainder of y divided by x (Short values).
     *
     * @param y the dividend expression
     * @param x the divisor
     * @return an expression representing the remainder
     */
    public static Expression<Short> mod(@NonNull Expression<Short> y, Short x) {
        return mod(y, constant(x));
    }

    /**
     * Returns the remainder of y divided by x (Integer values).
     * <p>
     * This is equivalent to the SQL {@code MOD(y, x)} function.
     *
     * @param y the dividend
     * @param x the divisor
     * @return an expression representing the remainder
     */
    public static Expression<Integer> mod(Integer y, Integer x) {
        return mod(constant(y), constant(x));
    }

    /**
     * Returns the remainder of y divided by x (Integer values).
     *
     * @param y the dividend expression
     * @param x the divisor
     * @return an expression representing the remainder
     */
    public static Expression<Integer> mod(@NonNull Expression<Integer> y, Integer x) {
        return mod(y, constant(x));
    }

    /**
     * Returns the remainder of y divided by x (Long values).
     * <p>
     * This is equivalent to the SQL {@code MOD(y, x)} function.
     *
     * @param y the dividend
     * @param x the divisor
     * @return an expression representing the remainder
     */
    public static Expression<Long> mod(Long y, Long x) {
        return mod(constant(y), constant(x));
    }

    /**
     * Returns the remainder of y divided by x (Long values).
     *
     * @param y the dividend expression
     * @param x the divisor
     * @return an expression representing the remainder
     */
    public static Expression<Long> mod(@NonNull Expression<Long> y, Long x) {
        return mod(y, constant(x));
    }

    /**
     * Returns the remainder of y divided by x (BigInteger values).
     * <p>
     * This is equivalent to the SQL {@code MOD(y, x)} function.
     *
     * @param y the dividend
     * @param x the divisor
     * @return an expression representing the remainder
     */
    public static Expression<BigInteger> mod(BigInteger y, BigInteger x) {
        return mod(constant(y), constant(x));
    }

    /**
     * Returns the remainder of y divided by x (BigInteger values).
     *
     * @param y the dividend expression
     * @param x the divisor
     * @return an expression representing the remainder
     */
    public static Expression<BigInteger> mod(@NonNull Expression<BigInteger> y, BigInteger x) {
        return mod(y, constant(x));
    }

    /**
     * Returns the remainder of y divided by x (BigDecimal values).
     * <p>
     * This is equivalent to the SQL {@code MOD(y, x)} function.
     *
     * @param y the dividend
     * @param x the divisor
     * @return an expression representing the remainder
     */
    public static Expression<BigDecimal> mod(BigDecimal y, BigDecimal x) {
        return mod(constant(y), constant(x));
    }

    /**
     * Returns the remainder of y divided by x (BigDecimal values).
     *
     * @param y the dividend expression
     * @param x the divisor
     * @return an expression representing the remainder
     */
    public static Expression<BigDecimal> mod(@NonNull Expression<BigDecimal> y, BigDecimal x) {
        return mod(y, constant(x));
    }

    /**
     * Returns the bitwise AND of two integer expressions.
     * <p>
     * Renders as the {@code (x & y)} operator on PostgreSQL and MariaDB,
     * and as the {@code bitand(x, y)} function on H2.
     *
     * @param <T> the integer type
     * @param x   the first operand
     * @param y   the second operand
     * @return an expression representing the bitwise AND
     */
    public static <T extends Number> Expression<T> bitAnd(@NonNull Expression<T> x, @NonNull Expression<T> y) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.BIT_AND, List.of(x, y), x.getJavaType());
    }

    /**
     * Returns the bitwise AND of two integer values.
     * <p>
     * Renders as the {@code (x & y)} operator on PostgreSQL and MariaDB,
     * and as the {@code bitand(x, y)} function on H2.
     *
     * @param <T>   the integer type
     * @param clazz the class representing the integer type
     * @param x     the first operand
     * @param y     the second operand
     * @return an expression representing the bitwise AND
     */
    public static <T extends Number> Expression<T> bitAnd(@NonNull Class<T> clazz, T x, T y) {
        return bitAnd(constant(clazz, x), constant(clazz, y));
    }

    /**
     * Returns the bitwise AND of two Byte values.
     *
     * @param x the first operand
     * @param y the second operand
     * @return an expression representing the bitwise AND
     */
    public static Expression<Byte> bitAnd(Byte x, Byte y) {
        return bitAnd(constant(x), constant(y));
    }

    /**
     * Returns the bitwise AND of an expression and a Byte value.
     *
     * @param x the first operand as an expression
     * @param y the second operand
     * @return an expression representing the bitwise AND
     */
    public static Expression<Byte> bitAnd(@NonNull Expression<Byte> x, Byte y) {
        return bitAnd(x, constant(y));
    }

    /**
     * Returns the bitwise AND of two Short values.
     *
     * @param x the first operand
     * @param y the second operand
     * @return an expression representing the bitwise AND
     */
    public static Expression<Short> bitAnd(Short x, Short y) {
        return bitAnd(constant(x), constant(y));
    }

    /**
     * Returns the bitwise AND of an expression and a Short value.
     *
     * @param x the first operand as an expression
     * @param y the second operand
     * @return an expression representing the bitwise AND
     */
    public static Expression<Short> bitAnd(@NonNull Expression<Short> x, Short y) {
        return bitAnd(x, constant(y));
    }

    /**
     * Returns the bitwise AND of two Integer values.
     *
     * @param x the first operand
     * @param y the second operand
     * @return an expression representing the bitwise AND
     */
    public static Expression<Integer> bitAnd(Integer x, Integer y) {
        return bitAnd(constant(x), constant(y));
    }

    /**
     * Returns the bitwise AND of an expression and an Integer value.
     *
     * @param x the first operand as an expression
     * @param y the second operand
     * @return an expression representing the bitwise AND
     */
    public static Expression<Integer> bitAnd(@NonNull Expression<Integer> x, Integer y) {
        return bitAnd(x, constant(y));
    }

    /**
     * Returns the bitwise AND of two Long values.
     *
     * @param x the first operand
     * @param y the second operand
     * @return an expression representing the bitwise AND
     */
    public static Expression<Long> bitAnd(Long x, Long y) {
        return bitAnd(constant(x), constant(y));
    }

    /**
     * Returns the bitwise AND of an expression and a Long value.
     *
     * @param x the first operand as an expression
     * @param y the second operand
     * @return an expression representing the bitwise AND
     */
    public static Expression<Long> bitAnd(@NonNull Expression<Long> x, Long y) {
        return bitAnd(x, constant(y));
    }

    /**
     * Returns the bitwise AND of two BigInteger values.
     *
     * @param x the first operand
     * @param y the second operand
     * @return an expression representing the bitwise AND
     */
    public static Expression<BigInteger> bitAnd(BigInteger x, BigInteger y) {
        return bitAnd(constant(x), constant(y));
    }

    /**
     * Returns the bitwise OR of two integer expressions.
     * <p>
     * Renders as the {@code (x | y)} operator on PostgreSQL and MariaDB,
     * and as the {@code bitor(x, y)} function on H2.
     *
     * @param <T> the integer type
     * @param x   the first operand
     * @param y   the second operand
     * @return an expression representing the bitwise OR
     */
    public static <T extends Number> Expression<T> bitOr(@NonNull Expression<T> x, @NonNull Expression<T> y) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.BIT_OR, List.of(x, y), x.getJavaType());
    }

    /**
     * Returns the bitwise OR of two integer values.
     * <p>
     * Renders as the {@code (x | y)} operator on PostgreSQL and MariaDB,
     * and as the {@code bitor(x, y)} function on H2.
     *
     * @param <T>   the integer type
     * @param clazz the class representing the integer type
     * @param x     the first operand
     * @param y     the second operand
     * @return an expression representing the bitwise OR
     */
    public static <T extends Number> Expression<T> bitOr(@NonNull Class<T> clazz, T x, T y) {
        return bitOr(constant(clazz, x), constant(clazz, y));
    }

    /**
     * Returns the bitwise OR of two Byte values.
     *
     * @param x the first operand
     * @param y the second operand
     * @return an expression representing the bitwise OR
     */
    public static Expression<Byte> bitOr(Byte x, Byte y) {
        return bitOr(constant(x), constant(y));
    }

    /**
     * Returns the bitwise OR of an expression and a Byte value.
     *
     * @param x the first operand as an expression
     * @param y the second operand
     * @return an expression representing the bitwise OR
     */
    public static Expression<Byte> bitOr(@NonNull Expression<Byte> x, Byte y) {
        return bitOr(x, constant(y));
    }

    /**
     * Returns the bitwise OR of two Short values.
     *
     * @param x the first operand
     * @param y the second operand
     * @return an expression representing the bitwise OR
     */
    public static Expression<Short> bitOr(Short x, Short y) {
        return bitOr(constant(x), constant(y));
    }

    /**
     * Returns the bitwise OR of an expression and a Short value.
     *
     * @param x the first operand as an expression
     * @param y the second operand
     * @return an expression representing the bitwise OR
     */
    public static Expression<Short> bitOr(@NonNull Expression<Short> x, Short y) {
        return bitOr(x, constant(y));
    }

    /**
     * Returns the bitwise OR of two Integer values.
     *
     * @param x the first operand
     * @param y the second operand
     * @return an expression representing the bitwise OR
     */
    public static Expression<Integer> bitOr(Integer x, Integer y) {
        return bitOr(constant(x), constant(y));
    }

    /**
     * Returns the bitwise OR of an expression and an Integer value.
     *
     * @param x the first operand as an expression
     * @param y the second operand
     * @return an expression representing the bitwise OR
     */
    public static Expression<Integer> bitOr(@NonNull Expression<Integer> x, Integer y) {
        return bitOr(x, constant(y));
    }

    /**
     * Returns the bitwise OR of two Long values.
     *
     * @param x the first operand
     * @param y the second operand
     * @return an expression representing the bitwise OR
     */
    public static Expression<Long> bitOr(Long x, Long y) {
        return bitOr(constant(x), constant(y));
    }

    /**
     * Returns the bitwise OR of an expression and a Long value.
     *
     * @param x the first operand as an expression
     * @param y the second operand
     * @return an expression representing the bitwise OR
     */
    public static Expression<Long> bitOr(@NonNull Expression<Long> x, Long y) {
        return bitOr(x, constant(y));
    }

    /**
     * Returns the bitwise OR of two BigInteger values.
     *
     * @param x the first operand
     * @param y the second operand
     * @return an expression representing the bitwise OR
     */
    public static Expression<BigInteger> bitOr(BigInteger x, BigInteger y) {
        return bitOr(constant(x), constant(y));
    }

    /**
     * Returns the bit at position {@code y} of the integer expression {@code x}.
     * The result is {@code 1} if the bit is set, {@code 0} otherwise.
     * <p>
     * Renders as {@code ((x & (1 << y)) >> y)} on PostgreSQL and MariaDB,
     * and as {@code CASE bitget(x, y) WHEN TRUE THEN 1 WHEN FALSE THEN 0 END} on H2.
     *
     * @param <T> the integer type
     * @param x   the value to inspect
     * @param y   the zero-based bit position (counted from the least significant bit)
     * @return an expression representing the bit value at position {@code y}
     */
    public static <T extends Number> Expression<T> bitGet(@NonNull Expression<T> x, @NonNull Expression<Integer> y) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.BIT_GET, List.of(x, y), x.getJavaType());
    }

    /**
     * Returns the bit at position {@code y} of the integer value {@code x}.
     * The result is {@code 1} if the bit is set, {@code 0} otherwise.
     *
     * @param <T>   the integer type
     * @param clazz the class representing the integer type
     * @param x     the value to inspect
     * @param y     the zero-based bit position
     * @return an expression representing the bit value at position {@code y}
     */
    public static <T extends Number> Expression<T> bitGet(@NonNull Class<T> clazz, T x, Integer y) {
        return bitGet(constant(clazz, x), constant(y));
    }

    /**
     * Returns the bit at position {@code y} of the Byte value {@code x}.
     *
     * @param x the value to inspect
     * @param y the zero-based bit position
     * @return an expression representing the bit value at position {@code y}
     */
    public static Expression<Byte> bitGet(Byte x, Integer y) {
        return bitGet(constant(x), constant(y));
    }

    /**
     * Returns the bit at position {@code y} of the integer expression {@code x}.
     *
     * @param <T> the integer type
     * @param x   the value to inspect as an expression
     * @param y   the zero-based bit position
     * @return an expression representing the bit value at position {@code y}
     */
    public static <T extends Number> Expression<T> bitGet(@NonNull Expression<T> x, Integer y) {
        return bitGet(x, constant(y));
    }

    /**
     * Returns the bit at position {@code y} of the Short value {@code x}.
     *
     * @param x the value to inspect
     * @param y the zero-based bit position
     * @return an expression representing the bit value at position {@code y}
     */
    public static Expression<Short> bitGet(Short x, Integer y) {
        return bitGet(constant(x), constant(y));
    }

    /**
     * Returns the bit at position {@code y} of the Integer value {@code x}.
     *
     * @param x the value to inspect
     * @param y the zero-based bit position
     * @return an expression representing the bit value at position {@code y}
     */
    public static Expression<Integer> bitGet(Integer x, Integer y) {
        return bitGet(constant(x), constant(y));
    }

    /**
     * Returns the bit at position {@code y} of the Long value {@code x}.
     *
     * @param x the value to inspect
     * @param y the zero-based bit position
     * @return an expression representing the bit value at position {@code y}
     */
    public static Expression<Long> bitGet(Long x, Integer y) {
        return bitGet(constant(x), constant(y));
    }

    /**
     * Returns the bit at position {@code y} of the BigInteger value {@code x}.
     *
     * @param x the value to inspect
     * @param y the zero-based bit position
     * @return an expression representing the bit value at position {@code y}
     */
    public static Expression<BigInteger> bitGet(BigInteger x, Integer y) {
        return bitGet(constant(x), constant(y));
    }

    /**
     * Returns the integer value {@code x} with the bit at position {@code y} set to {@code 1}.
     * <p>
     * Renders as {@code (x | (1 << y))} on PostgreSQL and MariaDB,
     * and as {@code bitor(x, lshift(1, y))} on H2.
     *
     * @param <T> the integer type
     * @param x   the value to modify
     * @param y   the zero-based bit position (counted from the least significant bit)
     * @return an expression representing {@code x} with the bit at position {@code y} set
     */
    public static <T extends Number> Expression<T> bitSet(@NonNull Expression<T> x, @NonNull Expression<Integer> y) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.BIT_SET, List.of(x, y), x.getJavaType());
    }

    /**
     * Returns the integer value {@code x} with the bit at position {@code y} set to {@code 1}.
     *
     * @param <T>   the integer type
     * @param clazz the class representing the integer type
     * @param x     the value to modify
     * @param y     the zero-based bit position
     * @return an expression representing {@code x} with the bit at position {@code y} set
     */
    public static <T extends Number> Expression<T> bitSet(@NonNull Class<T> clazz, T x, Integer y) {
        return bitSet(constant(clazz, x), constant(y));
    }

    /**
     * Returns the Byte value {@code x} with the bit at position {@code y} set to {@code 1}.
     *
     * @param x the value to modify
     * @param y the zero-based bit position
     * @return an expression representing {@code x} with the bit at position {@code y} set
     */
    public static Expression<Byte> bitSet(Byte x, Integer y) {
        return bitSet(constant(x), constant(y));
    }

    /**
     * Returns the integer expression {@code x} with the bit at position {@code y} set to {@code 1}.
     *
     * @param <T> the integer type
     * @param x   the value to modify as an expression
     * @param y   the zero-based bit position
     * @return an expression representing {@code x} with the bit at position {@code y} set
     */
    public static <T extends Number> Expression<T> bitSet(@NonNull Expression<T> x, Integer y) {
        return bitSet(x, constant(y));
    }

    /**
     * Returns the Short value {@code x} with the bit at position {@code y} set to {@code 1}.
     *
     * @param x the value to modify
     * @param y the zero-based bit position
     * @return an expression representing {@code x} with the bit at position {@code y} set
     */
    public static Expression<Short> bitSet(Short x, Integer y) {
        return bitSet(constant(x), constant(y));
    }

    /**
     * Returns the Integer value {@code x} with the bit at position {@code y} set to {@code 1}.
     *
     * @param x the value to modify
     * @param y the zero-based bit position
     * @return an expression representing {@code x} with the bit at position {@code y} set
     */
    public static Expression<Integer> bitSet(Integer x, Integer y) {
        return bitSet(constant(x), constant(y));
    }

    /**
     * Returns the Long value {@code x} with the bit at position {@code y} set to {@code 1}.
     *
     * @param x the value to modify
     * @param y the zero-based bit position
     * @return an expression representing {@code x} with the bit at position {@code y} set
     */
    public static Expression<Long> bitSet(Long x, Integer y) {
        return bitSet(constant(x), constant(y));
    }

    /**
     * Returns the BigInteger value {@code x} with the bit at position {@code y} set to {@code 1}.
     *
     * @param x the value to modify
     * @param y the zero-based bit position
     * @return an expression representing {@code x} with the bit at position {@code y} set
     */
    public static Expression<BigInteger> bitSet(BigInteger x, Integer y) {
        return bitSet(constant(x), constant(y));
    }

    /**
     * Returns the bitwise left shift of an integer expression {@code x} by {@code y} positions.
     * <p>
     * Renders as {@code (x << y)} on PostgreSQL and MariaDB,
     * and as {@code lshift(x, y)} on H2.
     *
     * @param <T> the integer type
     * @param x   the value to shift
     * @param y   the number of bit positions to shift left
     * @return an expression representing {@code x << y}
     */
    public static <T extends Number> Expression<T> shl(@NonNull Expression<T> x, @NonNull Expression<Integer> y) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.SHL, List.of(x, y), x.getJavaType());
    }

    /**
     * Returns the bitwise left shift of an integer value {@code x} by {@code y} positions.
     *
     * @param <T>   the integer type
     * @param clazz the class representing the integer type
     * @param x     the value to shift
     * @param y     the number of bit positions to shift left
     * @return an expression representing {@code x << y}
     */
    public static <T extends Number> Expression<T> shl(@NonNull Class<T> clazz, T x, Integer y) {
        return shl(constant(clazz, x), constant(y));
    }

    /**
     * Returns the bitwise left shift of a Byte value {@code x} by {@code y} positions.
     *
     * @param x the value to shift
     * @param y the number of bit positions to shift left
     * @return an expression representing {@code x << y}
     */
    public static Expression<Byte> shl(Byte x, Integer y) {
        return shl(constant(x), constant(y));
    }

    /**
     * Returns the bitwise left shift of an integer expression {@code x} by {@code y} positions.
     *
     * @param <T> the integer type
     * @param x   the value to shift as an expression
     * @param y   the number of bit positions to shift left
     * @return an expression representing {@code x << y}
     */
    public static <T extends Number> Expression<T> shl(@NonNull Expression<T> x, Integer y) {
        return shl(x, constant(y));
    }

    /**
     * Returns the bitwise left shift of a Short value {@code x} by {@code y} positions.
     *
     * @param x the value to shift
     * @param y the number of bit positions to shift left
     * @return an expression representing {@code x << y}
     */
    public static Expression<Short> shl(Short x, Integer y) {
        return shl(constant(x), constant(y));
    }

    /**
     * Returns the bitwise left shift of an Integer value {@code x} by {@code y} positions.
     *
     * @param x the value to shift
     * @param y the number of bit positions to shift left
     * @return an expression representing {@code x << y}
     */
    public static Expression<Integer> shl(Integer x, Integer y) {
        return shl(constant(x), constant(y));
    }

    /**
     * Returns the bitwise left shift of a Long value {@code x} by {@code y} positions.
     *
     * @param x the value to shift
     * @param y the number of bit positions to shift left
     * @return an expression representing {@code x << y}
     */
    public static Expression<Long> shl(Long x, Integer y) {
        return shl(constant(x), constant(y));
    }

    /**
     * Returns the bitwise left shift of a BigInteger value {@code x} by {@code y} positions.
     *
     * @param x the value to shift
     * @param y the number of bit positions to shift left
     * @return an expression representing {@code x << y}
     */
    public static Expression<BigInteger> shl(BigInteger x, Integer y) {
        return shl(constant(x), constant(y));
    }

    /**
     * Returns the bitwise right shift of an integer expression {@code x} by {@code y} positions.
     * <p>
     * Renders as {@code (x >> y)} on PostgreSQL and MariaDB,
     * and as {@code rshift(x, y)} on H2.
     *
     * @param <T> the integer type
     * @param x   the value to shift
     * @param y   the number of bit positions to shift right
     * @return an expression representing {@code x >> y}
     */
    public static <T extends Number> Expression<T> shr(@NonNull Expression<T> x, @NonNull Expression<Integer> y) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.SHR, List.of(x, y), x.getJavaType());
    }

    /**
     * Returns the bitwise right shift of an integer value {@code x} by {@code y} positions.
     *
     * @param <T>   the integer type
     * @param clazz the class representing the integer type
     * @param x     the value to shift
     * @param y     the number of bit positions to shift right
     * @return an expression representing {@code x >> y}
     */
    public static <T extends Number> Expression<T> shr(@NonNull Class<T> clazz, T x, Integer y) {
        return shr(constant(clazz, x), constant(y));
    }

    /**
     * Returns the bitwise right shift of a Byte value {@code x} by {@code y} positions.
     *
     * @param x the value to shift
     * @param y the number of bit positions to shift right
     * @return an expression representing {@code x >> y}
     */
    public static Expression<Byte> shr(Byte x, Integer y) {
        return shr(constant(x), constant(y));
    }

    /**
     * Returns the bitwise right shift of an integer expression {@code x} by {@code y} positions.
     *
     * @param <T> the integer type
     * @param x   the value to shift as an expression
     * @param y   the number of bit positions to shift right
     * @return an expression representing {@code x >> y}
     */
    public static <T extends Number> Expression<T> shr(@NonNull Expression<T> x, Integer y) {
        return shr(x, constant(y));
    }

    /**
     * Returns the bitwise right shift of a Short value {@code x} by {@code y} positions.
     *
     * @param x the value to shift
     * @param y the number of bit positions to shift right
     * @return an expression representing {@code x >> y}
     */
    public static Expression<Short> shr(Short x, Integer y) {
        return shr(constant(x), constant(y));
    }

    /**
     * Returns the bitwise right shift of an Integer value {@code x} by {@code y} positions.
     *
     * @param x the value to shift
     * @param y the number of bit positions to shift right
     * @return an expression representing {@code x >> y}
     */
    public static Expression<Integer> shr(Integer x, Integer y) {
        return shr(constant(x), constant(y));
    }

    /**
     * Returns the bitwise right shift of a Long value {@code x} by {@code y} positions.
     *
     * @param x the value to shift
     * @param y the number of bit positions to shift right
     * @return an expression representing {@code x >> y}
     */
    public static Expression<Long> shr(Long x, Integer y) {
        return shr(constant(x), constant(y));
    }

    /**
     * Returns the bitwise right shift of a BigInteger value {@code x} by {@code y} positions.
     *
     * @param x the value to shift
     * @param y the number of bit positions to shift right
     * @return an expression representing {@code x >> y}
     */
    public static Expression<BigInteger> shr(BigInteger x, Integer y) {
        return shr(constant(x), constant(y));
    }

    /**
     * Returns the bitwise NAND of two integer expressions.
     * <p>
     * Renders as {@code ~((x & y))} on PostgreSQL and MariaDB,
     * and as {@code bitnot(bitand(x, y))} on H2.
     *
     * @param <T> the integer type
     * @param x   the first operand
     * @param y   the second operand
     * @return an expression representing the bitwise NAND
     */
    public static <T extends Number> Expression<T> bitNand(@NonNull Expression<T> x, @NonNull Expression<T> y) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.BIT_NAND, List.of(x, y), x.getJavaType());
    }

    /**
     * Returns the bitwise NAND of two integer values.
     * <p>
     * Renders as {@code ~((x & y))} on PostgreSQL and MariaDB,
     * and as {@code bitnot(bitand(x, y))} on H2.
     *
     * @param <T>   the integer type
     * @param clazz the class representing the integer type
     * @param x     the first operand
     * @param y     the second operand
     * @return an expression representing the bitwise NAND
     */
    public static <T extends Number> Expression<T> bitNand(@NonNull Class<T> clazz, T x, T y) {
        return bitNand(constant(clazz, x), constant(clazz, y));
    }

    /**
     * Returns the bitwise NAND of two Byte values.
     *
     * @param x the first operand
     * @param y the second operand
     * @return an expression representing the bitwise NAND
     */
    public static Expression<Byte> bitNand(Byte x, Byte y) {
        return bitNand(constant(x), constant(y));
    }

    /**
     * Returns the bitwise NAND of an expression and a Byte value.
     *
     * @param x the first operand as an expression
     * @param y the second operand
     * @return an expression representing the bitwise NAND
     */
    public static Expression<Byte> bitNand(@NonNull Expression<Byte> x, Byte y) {
        return bitNand(x, constant(y));
    }

    /**
     * Returns the bitwise NAND of two Short values.
     *
     * @param x the first operand
     * @param y the second operand
     * @return an expression representing the bitwise NAND
     */
    public static Expression<Short> bitNand(Short x, Short y) {
        return bitNand(constant(x), constant(y));
    }

    /**
     * Returns the bitwise NAND of an expression and a Short value.
     *
     * @param x the first operand as an expression
     * @param y the second operand
     * @return an expression representing the bitwise NAND
     */
    public static Expression<Short> bitNand(@NonNull Expression<Short> x, Short y) {
        return bitNand(x, constant(y));
    }

    /**
     * Returns the bitwise NAND of two Integer values.
     *
     * @param x the first operand
     * @param y the second operand
     * @return an expression representing the bitwise NAND
     */
    public static Expression<Integer> bitNand(Integer x, Integer y) {
        return bitNand(constant(x), constant(y));
    }

    /**
     * Returns the bitwise NAND of an expression and an Integer value.
     *
     * @param x the first operand as an expression
     * @param y the second operand
     * @return an expression representing the bitwise NAND
     */
    public static Expression<Integer> bitNand(@NonNull Expression<Integer> x, Integer y) {
        return bitNand(x, constant(y));
    }

    /**
     * Returns the bitwise NAND of two Long values.
     *
     * @param x the first operand
     * @param y the second operand
     * @return an expression representing the bitwise NAND
     */
    public static Expression<Long> bitNand(Long x, Long y) {
        return bitNand(constant(x), constant(y));
    }

    /**
     * Returns the bitwise NAND of an expression and a Long value.
     *
     * @param x the first operand as an expression
     * @param y the second operand
     * @return an expression representing the bitwise NAND
     */
    public static Expression<Long> bitNand(@NonNull Expression<Long> x, Long y) {
        return bitNand(x, constant(y));
    }

    /**
     * Returns the bitwise NAND of two BigInteger values.
     *
     * @param x the first operand
     * @param y the second operand
     * @return an expression representing the bitwise NAND
     */
    public static Expression<BigInteger> bitNand(BigInteger x, BigInteger y) {
        return bitNand(constant(x), constant(y));
    }

    /**
     * Returns the bitwise NOR of two integer expressions.
     * <p>
     * Renders as {@code ~((x | y))} on PostgreSQL and MariaDB,
     * and as {@code bitnot(bitor(x, y))} on H2.
     *
     * @param <T> the integer type
     * @param x   the first operand
     * @param y   the second operand
     * @return an expression representing the bitwise NOR
     */
    public static <T extends Number> Expression<T> bitNor(@NonNull Expression<T> x, @NonNull Expression<T> y) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.BIT_NOR, List.of(x, y), x.getJavaType());
    }

    /**
     * Returns the bitwise NOR of two integer values.
     * <p>
     * Renders as {@code ~((x | y))} on PostgreSQL and MariaDB,
     * and as {@code bitnot(bitor(x, y))} on H2.
     *
     * @param <T>   the integer type
     * @param clazz the class representing the integer type
     * @param x     the first operand
     * @param y     the second operand
     * @return an expression representing the bitwise NOR
     */
    public static <T extends Number> Expression<T> bitNor(@NonNull Class<T> clazz, T x, T y) {
        return bitNor(constant(clazz, x), constant(clazz, y));
    }

    /**
     * Returns the bitwise NOR of two Byte values.
     *
     * @param x the first operand
     * @param y the second operand
     * @return an expression representing the bitwise NOR
     */
    public static Expression<Byte> bitNor(Byte x, Byte y) {
        return bitNor(constant(x), constant(y));
    }

    /**
     * Returns the bitwise NOR of an expression and a Byte value.
     *
     * @param x the first operand as an expression
     * @param y the second operand
     * @return an expression representing the bitwise NOR
     */
    public static Expression<Byte> bitNor(@NonNull Expression<Byte> x, Byte y) {
        return bitNor(x, constant(y));
    }

    /**
     * Returns the bitwise NOR of two Short values.
     *
     * @param x the first operand
     * @param y the second operand
     * @return an expression representing the bitwise NOR
     */
    public static Expression<Short> bitNor(Short x, Short y) {
        return bitNor(constant(x), constant(y));
    }

    /**
     * Returns the bitwise NOR of an expression and a Short value.
     *
     * @param x the first operand as an expression
     * @param y the second operand
     * @return an expression representing the bitwise NOR
     */
    public static Expression<Short> bitNor(@NonNull Expression<Short> x, Short y) {
        return bitNor(x, constant(y));
    }

    /**
     * Returns the bitwise NOR of two Integer values.
     *
     * @param x the first operand
     * @param y the second operand
     * @return an expression representing the bitwise NOR
     */
    public static Expression<Integer> bitNor(Integer x, Integer y) {
        return bitNor(constant(x), constant(y));
    }

    /**
     * Returns the bitwise NOR of an expression and an Integer value.
     *
     * @param x the first operand as an expression
     * @param y the second operand
     * @return an expression representing the bitwise NOR
     */
    public static Expression<Integer> bitNor(@NonNull Expression<Integer> x, Integer y) {
        return bitNor(x, constant(y));
    }

    /**
     * Returns the bitwise NOR of two Long values.
     *
     * @param x the first operand
     * @param y the second operand
     * @return an expression representing the bitwise NOR
     */
    public static Expression<Long> bitNor(Long x, Long y) {
        return bitNor(constant(x), constant(y));
    }

    /**
     * Returns the bitwise NOR of an expression and a Long value.
     *
     * @param x the first operand as an expression
     * @param y the second operand
     * @return an expression representing the bitwise NOR
     */
    public static Expression<Long> bitNor(@NonNull Expression<Long> x, Long y) {
        return bitNor(x, constant(y));
    }

    /**
     * Returns the bitwise NOR of two BigInteger values.
     *
     * @param x the first operand
     * @param y the second operand
     * @return an expression representing the bitwise NOR
     */
    public static Expression<BigInteger> bitNor(BigInteger x, BigInteger y) {
        return bitNor(constant(x), constant(y));
    }

    /**
     * Returns the bitwise XNOR (equivalence) of two integer expressions.
     * <p>
     * Renders as {@code ~((x ^ y))} on MariaDB, {@code ~((x # y))} on PostgreSQL,
     * and as the {@code bitxnor(x, y)} function on H2.
     *
     * @param <T> the integer type
     * @param x   the first operand
     * @param y   the second operand
     * @return an expression representing the bitwise XNOR
     */
    public static <T extends Number> Expression<T> bitXNor(@NonNull Expression<T> x, @NonNull Expression<T> y) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.BIT_XNOR, List.of(x, y), x.getJavaType());
    }

    /**
     * Returns the bitwise XNOR (equivalence) of two integer values.
     * <p>
     * Renders as {@code ~((x ^ y))} on MariaDB, {@code ~((x # y))} on PostgreSQL,
     * and as the {@code bitxnor(x, y)} function on H2.
     *
     * @param <T>   the integer type
     * @param clazz the class representing the integer type
     * @param x     the first operand
     * @param y     the second operand
     * @return an expression representing the bitwise XNOR
     */
    public static <T extends Number> Expression<T> bitXNor(@NonNull Class<T> clazz, T x, T y) {
        return bitXNor(constant(clazz, x), constant(clazz, y));
    }

    /**
     * Returns the bitwise XNOR of two Byte values.
     *
     * @param x the first operand
     * @param y the second operand
     * @return an expression representing the bitwise XNOR
     */
    public static Expression<Byte> bitXNor(Byte x, Byte y) {
        return bitXNor(constant(x), constant(y));
    }

    /**
     * Returns the bitwise XNOR of an expression and a Byte value.
     *
     * @param x the first operand as an expression
     * @param y the second operand
     * @return an expression representing the bitwise XNOR
     */
    public static Expression<Byte> bitXNor(@NonNull Expression<Byte> x, Byte y) {
        return bitXNor(x, constant(y));
    }

    /**
     * Returns the bitwise XNOR of two Short values.
     *
     * @param x the first operand
     * @param y the second operand
     * @return an expression representing the bitwise XNOR
     */
    public static Expression<Short> bitXNor(Short x, Short y) {
        return bitXNor(constant(x), constant(y));
    }

    /**
     * Returns the bitwise XNOR of an expression and a Short value.
     *
     * @param x the first operand as an expression
     * @param y the second operand
     * @return an expression representing the bitwise XNOR
     */
    public static Expression<Short> bitXNor(@NonNull Expression<Short> x, Short y) {
        return bitXNor(x, constant(y));
    }

    /**
     * Returns the bitwise XNOR of two Integer values.
     *
     * @param x the first operand
     * @param y the second operand
     * @return an expression representing the bitwise XNOR
     */
    public static Expression<Integer> bitXNor(Integer x, Integer y) {
        return bitXNor(constant(x), constant(y));
    }

    /**
     * Returns the bitwise XNOR of an expression and an Integer value.
     *
     * @param x the first operand as an expression
     * @param y the second operand
     * @return an expression representing the bitwise XNOR
     */
    public static Expression<Integer> bitXNor(@NonNull Expression<Integer> x, Integer y) {
        return bitXNor(x, constant(y));
    }

    /**
     * Returns the bitwise XNOR of two Long values.
     *
     * @param x the first operand
     * @param y the second operand
     * @return an expression representing the bitwise XNOR
     */
    public static Expression<Long> bitXNor(Long x, Long y) {
        return bitXNor(constant(x), constant(y));
    }

    /**
     * Returns the bitwise XNOR of an expression and a Long value.
     *
     * @param x the first operand as an expression
     * @param y the second operand
     * @return an expression representing the bitwise XNOR
     */
    public static Expression<Long> bitXNor(@NonNull Expression<Long> x, Long y) {
        return bitXNor(x, constant(y));
    }

    /**
     * Returns the bitwise XNOR of two BigInteger values.
     *
     * @param x the first operand
     * @param y the second operand
     * @return an expression representing the bitwise XNOR
     */
    public static Expression<BigInteger> bitXNor(BigInteger x, BigInteger y) {
        return bitXNor(constant(x), constant(y));
    }

    /**
     * Returns the bitwise XOR of two integer expressions.
     * <p>
     * Renders as the {@code (x ^ y)} operator on MariaDB, the {@code (x # y)} operator on PostgreSQL,
     * and as the {@code bitxor(x, y)} function on H2.
     *
     * @param <T> the integer type
     * @param x   the first operand
     * @param y   the second operand
     * @return an expression representing the bitwise XOR
     */
    public static <T extends Number> Expression<T> bitXor(@NonNull Expression<T> x, @NonNull Expression<T> y) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.BIT_XOR, List.of(x, y), x.getJavaType());
    }

    /**
     * Returns the bitwise XOR of two integer values.
     * <p>
     * Renders as the {@code (x ^ y)} operator on MariaDB, the {@code (x # y)} operator on PostgreSQL,
     * and as the {@code bitxor(x, y)} function on H2.
     *
     * @param <T>   the integer type
     * @param clazz the class representing the integer type
     * @param x     the first operand
     * @param y     the second operand
     * @return an expression representing the bitwise XOR
     */
    public static <T extends Number> Expression<T> bitXor(@NonNull Class<T> clazz, T x, T y) {
        return bitXor(constant(clazz, x), constant(clazz, y));
    }

    /**
     * Returns the bitwise XOR of two Byte values.
     *
     * @param x the first operand
     * @param y the second operand
     * @return an expression representing the bitwise XOR
     */
    public static Expression<Byte> bitXor(Byte x, Byte y) {
        return bitXor(constant(x), constant(y));
    }

    /**
     * Returns the bitwise XOR of an expression and a Byte value.
     *
     * @param x the first operand as an expression
     * @param y the second operand
     * @return an expression representing the bitwise XOR
     */
    public static Expression<Byte> bitXor(@NonNull Expression<Byte> x, Byte y) {
        return bitXor(x, constant(y));
    }

    /**
     * Returns the bitwise XOR of two Short values.
     *
     * @param x the first operand
     * @param y the second operand
     * @return an expression representing the bitwise XOR
     */
    public static Expression<Short> bitXor(Short x, Short y) {
        return bitXor(constant(x), constant(y));
    }

    /**
     * Returns the bitwise XOR of an expression and a Short value.
     *
     * @param x the first operand as an expression
     * @param y the second operand
     * @return an expression representing the bitwise XOR
     */
    public static Expression<Short> bitXor(@NonNull Expression<Short> x, Short y) {
        return bitXor(x, constant(y));
    }

    /**
     * Returns the bitwise XOR of two Integer values.
     *
     * @param x the first operand
     * @param y the second operand
     * @return an expression representing the bitwise XOR
     */
    public static Expression<Integer> bitXor(Integer x, Integer y) {
        return bitXor(constant(x), constant(y));
    }

    /**
     * Returns the bitwise XOR of an expression and an Integer value.
     *
     * @param x the first operand as an expression
     * @param y the second operand
     * @return an expression representing the bitwise XOR
     */
    public static Expression<Integer> bitXor(@NonNull Expression<Integer> x, Integer y) {
        return bitXor(x, constant(y));
    }

    /**
     * Returns the bitwise XOR of two Long values.
     *
     * @param x the first operand
     * @param y the second operand
     * @return an expression representing the bitwise XOR
     */
    public static Expression<Long> bitXor(Long x, Long y) {
        return bitXor(constant(x), constant(y));
    }

    /**
     * Returns the bitwise XOR of an expression and a Long value.
     *
     * @param x the first operand as an expression
     * @param y the second operand
     * @return an expression representing the bitwise XOR
     */
    public static Expression<Long> bitXor(@NonNull Expression<Long> x, Long y) {
        return bitXor(x, constant(y));
    }

    /**
     * Returns the bitwise XOR of two BigInteger values.
     *
     * @param x the first operand
     * @param y the second operand
     * @return an expression representing the bitwise XOR
     */
    public static Expression<BigInteger> bitXor(BigInteger x, BigInteger y) {
        return bitXor(constant(x), constant(y));
    }

    /**
     * Returns the bitwise NOT (one's complement) of an integer expression.
     * <p>
     * Renders as the {@code (~x)} operator on PostgreSQL and MariaDB,
     * and as the {@code bitnot(x)} function on H2.
     *
     * @param <T> the integer type
     * @param x   the operand to invert
     * @return an expression representing the bitwise NOT
     */
    public static <T extends Number> Expression<T> bitNot(@NonNull Expression<T> x) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.BIT_NOT, List.of(x), x.getJavaType());
    }

    /**
     * Returns the bitwise NOT (one's complement) of an integer value.
     * <p>
     * Renders as the {@code (~x)} operator on PostgreSQL and MariaDB,
     * and as the {@code bitnot(x)} function on H2.
     *
     * @param <T>   the integer type
     * @param clazz the class representing the integer type
     * @param x     the operand to invert
     * @return an expression representing the bitwise NOT
     */
    public static <T extends Number> Expression<T> bitNot(@NonNull Class<T> clazz, T x) {
        return bitNot(constant(clazz, x));
    }

    /**
     * Returns the bitwise NOT of a Byte value.
     *
     * @param x the operand to invert
     * @return an expression representing the bitwise NOT
     */
    public static Expression<Byte> bitNot(Byte x) {
        return bitNot(constant(x));
    }

    /**
     * Returns the bitwise NOT of a Short value.
     *
     * @param x the operand to invert
     * @return an expression representing the bitwise NOT
     */
    public static Expression<Short> bitNot(Short x) {
        return bitNot(constant(x));
    }

    /**
     * Returns the bitwise NOT of an Integer value.
     *
     * @param x the operand to invert
     * @return an expression representing the bitwise NOT
     */
    public static Expression<Integer> bitNot(Integer x) {
        return bitNot(constant(x));
    }

    /**
     * Returns the bitwise NOT of a Long value.
     *
     * @param x the operand to invert
     * @return an expression representing the bitwise NOT
     */
    public static Expression<Long> bitNot(Long x) {
        return bitNot(constant(x));
    }

    /**
     * Returns the bitwise NOT of a BigInteger value.
     *
     * @param x the operand to invert
     * @return an expression representing the bitwise NOT
     */
    public static Expression<BigInteger> bitNot(BigInteger x) {
        return bitNot(constant(x));
    }

    /**
     * Returns the constant value of pi (approximately 3.14159...).
     * <p>
     * This is equivalent to the SQL {@code PI()} function.
     *
     * @return an expression representing the value of pi
     */
    public static Expression<Double> pi() {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.PI, List.of(), Double.class);
    }

    /**
     * Converts degrees to radians.
     * <p>
     * This is equivalent to the SQL {@code RADIANS(value)} function.
     *
     * @param <T>   the numeric type
     * @param value the expression representing the value in degrees
     * @return an expression representing the value in radians
     */
    public static <T extends Number> Expression<T> radians(@NonNull Expression<T> value) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.RADIANS, List.of(value), value.getJavaType());
    }

    /**
     * Converts degrees to radians.
     * <p>
     * This is equivalent to the SQL {@code RADIANS(value)} function.
     *
     * @param <T>   the numeric type
     * @param clazz the class representing the numeric type
     * @param value the value in degrees
     * @return an expression representing the value in radians
     */
    public static <T extends Number> Expression<T> radians(@NonNull Class<T> clazz, T value) {
        return radians(constant(clazz, value));
    }

    /**
     * Converts degrees to radians.
     * <p>
     * This is equivalent to the SQL {@code RADIANS(value)} function.
     *
     * @param value the float value in degrees
     * @return an expression representing the value in radians
     */
    public static Expression<Float> radians(Float value) {
        return radians(constant(value));
    }

    /**
     * Converts degrees to radians.
     * <p>
     * This is equivalent to the SQL {@code RADIANS(value)} function.
     *
     * @param value the double value in degrees
     * @return an expression representing the value in radians
     */
    public static Expression<Double> radians(Double value) {
        return radians(constant(value));
    }

    /**
     * Converts degrees to radians.
     * <p>
     * This is equivalent to the SQL {@code RADIANS(value)} function.
     *
     * @param value the BigDecimal value in degrees
     * @return an expression representing the value in radians
     */
    public static Expression<BigDecimal> radians(BigDecimal value) {
        return radians(constant(value));
    }

    /**
     * Rounds a numeric expression to the nearest integer.
     * <p>
     * This is equivalent to the SQL {@code ROUND(value)} function.
     *
     * @param <T>   the numeric type
     * @param value the numeric expression to round
     * @return an expression representing the rounded value
     */
    public static <T extends Number> Expression<T> round(@NonNull Expression<T> value) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.ROUND, List.of(value), value.getJavaType());
    }

    /**
     * Rounds a numeric value to the nearest integer.
     * <p>
     * This is equivalent to the SQL {@code ROUND(value)} function.
     *
     * @param <T>   the numeric type
     * @param clazz the class representing the numeric type
     * @param value the numeric value to round
     * @return an expression representing the rounded value
     */
    public static <T extends Number> Expression<T> round(@NonNull Class<T> clazz, T value) {
        return round(constant(clazz, value));
    }

    /**
     * Rounds a Float to the nearest integer.
     * <p>
     * This is equivalent to the SQL {@code ROUND(value)} function.
     *
     * @param value the float value to round
     * @return an expression representing the rounded value
     */
    public static Expression<Float> round(Float value) {
        return round(constant(value));
    }

    /**
     * Rounds a Double to the nearest integer.
     * <p>
     * This is equivalent to the SQL {@code ROUND(value)} function.
     *
     * @param value the double value to round
     * @return an expression representing the rounded value
     */
    public static Expression<Double> round(Double value) {
        return round(constant(value));
    }

    /**
     * Rounds a BigDecimal to the nearest integer.
     * <p>
     * This is equivalent to the SQL {@code ROUND(value)} function.
     *
     * @param value the BigDecimal value to round
     * @return an expression representing the rounded value
     */
    public static Expression<BigDecimal> round(BigDecimal value) {
        return round(constant(value));
    }

    /**
     * Rounds a numeric expression to s decimal places.
     * <p>
     * This is equivalent to the SQL {@code ROUND(value, s)} function.
     *
     * @param <T>   the numeric type
     * @param value the numeric expression to round
     * @param s     the number of decimal places to round to
     * @return an expression representing the rounded value
     */
    public static <T extends Number> Expression<T> round(@NonNull Expression<T> value, @NonNull Expression<Integer> s) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.ROUND, List.of(value, s), value.getJavaType());
    }

    /**
     * Rounds a numeric value to s decimal places.
     * <p>
     * This is equivalent to the SQL {@code ROUND(value, s)} function.
     *
     * @param <T>   the numeric type
     * @param clazz the class representing the numeric type
     * @param value the numeric value to round
     * @param s     the number of decimal places to round to
     * @return an expression representing the rounded value
     */
    public static <T extends Number> Expression<T> round(@NonNull Class<T> clazz, T value, @NonNull Integer s) {
        return round(constant(clazz, value), constant(s));
    }

    /**
     * Rounds a Float to s decimal places.
     * <p>
     * This is equivalent to the SQL {@code ROUND(value, s)} function.
     *
     * @param value the float value to round
     * @param s     the number of decimal places to round to
     * @return an expression representing the rounded value
     */
    public static Expression<Float> round(Float value, @NonNull Integer s) {
        return round(constant(value), constant(s));
    }

    /**
     * Rounds a numeric expression to s decimal places.
     * <p>
     * This is equivalent to the SQL {@code ROUND(value, s)} function.
     *
     * @param <T>   the numeric type
     * @param value the numeric expression to round
     * @param s     the number of decimal places to round to
     * @return an expression representing the rounded value
     */
    public static <T extends Number> Expression<T> round(@NonNull Expression<T> value, @NonNull Integer s) {
        return round(value, constant(s));
    }

    /**
     * Rounds a Double to s decimal places.
     * <p>
     * This is equivalent to the SQL {@code ROUND(value, s)} function.
     *
     * @param value the double value to round
     * @param s     the number of decimal places to round to
     * @return an expression representing the rounded value
     */
    public static Expression<Double> round(Double value, @NonNull Integer s) {
        return round(constant(value), constant(s));
    }

    /**
     * Rounds a BigDecimal to s decimal places.
     * <p>
     * This is equivalent to the SQL {@code ROUND(value, s)} function.
     *
     * @param value the BigDecimal value to round
     * @param s     the number of decimal places to round to
     * @return an expression representing the rounded value
     */
    public static Expression<BigDecimal> round(BigDecimal value, @NonNull Integer s) {
        return round(constant(value), constant(s));
    }

    /**
     * Returns the sign of a numeric expression (-1, 0, or +1).
     * <p>
     * This is equivalent to the SQL {@code SIGN(value)} function.
     *
     * @param value the numeric expression to get the sign of
     * @return an integer expression representing the sign (-1, 0, or +1)
     */
    public static Expression<Integer> sign(@NonNull Expression<? extends Number> value) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.SIGN, List.of(value), Integer.class);
    }

    /**
     * Returns the sign of a numeric value (-1, 0, or +1).
     * <p>
     * This is equivalent to the SQL {@code SIGN(value)} function.
     *
     * @param <T>   the numeric type
     * @param clazz the class representing the numeric type
     * @param value the numeric value to get the sign of
     * @return an integer expression representing the sign (-1, 0, or +1)
     */
    public static <T extends Number> Expression<Integer> sign(@NonNull Class<T> clazz, T value) {
        return sign(constant(clazz, value));
    }

    /**
     * Returns the sign of a Float (-1, 0, or +1).
     * <p>
     * This is equivalent to the SQL {@code SIGN(value)} function.
     *
     * @param value the float value to get the sign of
     * @return an integer expression representing the sign (-1, 0, or +1)
     */
    public static Expression<Integer> sign(Float value) {
        return sign(constant(value));
    }

    /**
     * Returns the sign of a Double (-1, 0, or +1).
     * <p>
     * This is equivalent to the SQL {@code SIGN(value)} function.
     *
     * @param value the double value to get the sign of
     * @return an integer expression representing the sign (-1, 0, or +1)
     */
    public static Expression<Integer> sign(Double value) {
        return sign(constant(value));
    }

    /**
     * Returns the sign of a BigDecimal (-1, 0, or +1).
     * <p>
     * This is equivalent to the SQL {@code SIGN(value)} function.
     *
     * @param value the BigDecimal value to get the sign of
     * @return an integer expression representing the sign (-1, 0, or +1)
     */
    public static Expression<Integer> sign(BigDecimal value) {
        return sign(constant(value));
    }

    /**
     * Returns the square root of a numeric expression.
     * <p>
     * This is equivalent to the SQL {@code SQRT(expression)} function.
     *
     * @param <T>   the numeric type
     * @param value the numeric expression to get the square root of
     * @return an expression representing the square root
     */
    public static <T extends Number> Expression<T> sqrt(@NonNull Expression<T> value) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.SQRT, List.of(value), value.getJavaType());
    }

    /**
     * Returns the square root of a numeric value.
     * <p>
     * This is equivalent to the SQL {@code SQRT(value)} function.
     *
     * @param <T>   the numeric type
     * @param clazz the class representing the numeric type
     * @param value the numeric value to get the square root of
     * @return an expression representing the square root
     */
    public static <T extends Number> Expression<T> sqrt(@NonNull Class<T> clazz, T value) {
        return sqrt(constant(clazz, value));
    }

    /**
     * Returns the square root of a Float.
     * <p>
     * This is equivalent to the SQL {@code SQRT(value)} function.
     *
     * @param value the float value to get the square root of
     * @return an expression representing the square root
     */
    public static Expression<Float> sqrt(Float value) {
        return sqrt(constant(value));
    }

    /**
     * Returns the square root of a Double.
     * <p>
     * This is equivalent to the SQL {@code SQRT(value)} function.
     *
     * @param value the double value to get the square root of
     * @return an expression representing the square root
     */
    public static Expression<Double> sqrt(Double value) {
        return sqrt(constant(value));
    }

    /**
     * Returns the square root of a BigDecimal.
     * <p>
     * This is equivalent to the SQL {@code SQRT(value)} function.
     *
     * @param value the BigDecimal value to get the square root of
     * @return an expression representing the square root
     */
    public static Expression<BigDecimal> sqrt(BigDecimal value) {
        return sqrt(constant(value));
    }

    /**
     * Truncates a numeric expression to integer (towards zero).
     * <p>
     * This is equivalent to the SQL {@code TRUNC(expression)} function.
     *
     * @param <T>   the numeric type
     * @param value the numeric expression to truncate
     * @return an expression representing the truncated value
     */
    public static <T extends Number> Expression<T> trunc(@NonNull Expression<T> value) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.TRUNCATE, List.of(value), value.getJavaType());
    }

    /**
     * Truncates a numeric value to integer (towards zero).
     * <p>
     * This is equivalent to the SQL {@code TRUNC(value)} function.
     *
     * @param <T>   the numeric type
     * @param clazz the class representing the numeric type
     * @param value the numeric value to truncate
     * @return an expression representing the truncated value
     */
    public static <T extends Number> Expression<T> trunc(@NonNull Class<T> clazz, T value) {
        return trunc(constant(clazz, value));
    }

    /**
     * Truncates a Float to integer (towards zero).
     * <p>
     * This is equivalent to the SQL {@code TRUNC(value)} function.
     *
     * @param value the float value to truncate
     * @return an expression representing the truncated value
     */
    public static Expression<Float> trunc(Float value) {
        return trunc(constant(value));
    }

    /**
     * Truncates a Double to integer (towards zero).
     * <p>
     * This is equivalent to the SQL {@code TRUNC(value)} function.
     *
     * @param value the double value to truncate
     * @return an expression representing the truncated value
     */
    public static Expression<Double> trunc(Double value) {
        return trunc(constant(value));
    }

    /**
     * Truncates a BigDecimal to integer (towards zero).
     * <p>
     * This is equivalent to the SQL {@code TRUNC(value)} function.
     *
     * @param value the BigDecimal value to truncate
     * @return an expression representing the truncated value
     */
    public static Expression<BigDecimal> trunc(BigDecimal value) {
        return trunc(constant(value));
    }


    /**
     * Returns the next value from a database sequence.
     * <p>
     * Renders as {@code nextval('sequence')} on H2 and PostgreSQL,
     * and as {@code NEXT VALUE FOR sequence} on MariaDB.
     *
     * @param sequence the name of the sequence
     * @return an expression representing the next value from the sequence
     */
    public static Expression<Long> nextval(@NonNull String sequence) {
        return new NextvalExpression(sequence);
    }

    /**
     * Returns the smallest value from the given expressions.
     * <p>
     * This is equivalent to the SQL {@code LEAST(expression1, expression2, ...)} function.
     *
     * @param <T>         the type of the expressions
     * @param expressions the expressions to compare
     * @return an expression representing the smallest value
     * @throws IllegalArgumentException if no expressions are provided
     */
    @SafeVarargs
    public static <T> Expression<T> least(@NonNull Expression<T>... expressions) {
        if (expressions.length == 0) {
            throw new IllegalArgumentException("No expressions provided");
        }
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.LEAST, Arrays.asList(expressions), expressions[0].getJavaType());
    }

    /**
     * Returns the largest value from the given expressions.
     * <p>
     * This is equivalent to the SQL {@code GREATEST(expression1, expression2, ...)} function.
     *
     * @param <T>         the type of the expressions
     * @param expressions the expressions to compare
     * @return an expression representing the largest value
     * @throws IllegalArgumentException if no expressions are provided
     */
    @SafeVarargs
    public static <T> Expression<T> greatest(@NonNull Expression<T>... expressions) {
        if (expressions.length == 0) {
            throw new IllegalArgumentException("No expressions provided");
        }
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.GREATEST, Arrays.asList(expressions), expressions[0].getJavaType());
    }


    /**
     * Returns an arbitrary value from the input values.
     * <p>
     * This is equivalent to the SQL {@code ANY_VALUE(expression)} aggregate function.
     *
     * @param <T>        the expression type
     * @param expression the expression to pick a value from
     * @return an expression representing an arbitrary input value
     */
    public static <T> Expression<T> anyValue(@NonNull Expression<T> expression) {
        return new PortableAggregateExpression<>(
                expression.getJavaType(),
                AggregateQuantifier.NONE,
                PortableAggregateExpression.Function.ANY_VALUE,
                List.of(expression)
        );
    }

    /**
     * Returns an arbitrary value from the rows matching the filter conditions.
     *
     * @param <T>        the expression type
     * @param expression the expression to pick a value from
     * @param condition  the filter condition
     * @return an expression representing an arbitrary value of the matching rows
     */
    public static <T> Expression<T> anyValueWhere(@NonNull Expression<T> expression, @NonNull Condition condition) {
        return anyValueWhere(expression, List.of(condition));
    }

    /**
     * Returns an arbitrary value from the rows matching the filter conditions.
     *
     * @param <T>        the expression type
     * @param expression the expression to pick a value from
     * @param conditions the filter conditions combined with {@code AND}
     * @return an expression representing an arbitrary value of the matching rows
     */
    public static <T> Expression<T> anyValueWhere(@NonNull Expression<T> expression, @NonNull Condition... conditions) {
        return anyValueWhere(expression, Arrays.asList(conditions));
    }

    /**
     * Returns an arbitrary value from the rows matching the filter conditions.
     *
     * @param <T>        the expression type
     * @param expression the expression to pick a value from
     * @param conditions the filter conditions combined with {@code AND}
     * @return an expression representing an arbitrary value of the matching rows
     */
    public static <T> Expression<T> anyValueWhere(@NonNull Expression<T> expression, @NonNull List<Condition> conditions) {
        final var expr = new PortableAggregateExpression<>(expression.getJavaType(), AggregateQuantifier.NONE, PortableAggregateExpression.Function.ANY_VALUE, List.of(expression));
        expr.getFilter().addAll(conditions);
        return expr;
    }

    /**
     * Returns the average of the values.
     *
     * @param expression the numeric expression
     * @param <T>        the numeric type
     * @return average expression
     */
    public static <T extends Number> Expression<Double> avg(@NonNull Expression<T> expression) {
        return new PortableAggregateExpression<>(Double.class, AggregateQuantifier.NONE, PortableAggregateExpression.Function.AVG, List.of(expression));
    }

    /**
     * Returns the average of the distinct values.
     *
     * @param expression the numeric expression
     * @param <T>        the numeric type
     * @return average expression over distinct values
     */
    public static <T extends Number> Expression<Double> avgDistinct(@NonNull Expression<T> expression) {
        return new PortableAggregateExpression<>(Double.class, AggregateQuantifier.DISTINCT, PortableAggregateExpression.Function.AVG, List.of(expression));
    }

    /**
     * Returns the average of the distinct values from the rows matching the filter conditions.
     *
     * @param <T>        the numeric type
     * @param expression the numeric expression
     * @param condition  the filter condition
     * @return average expression over the distinct values of the matching rows
     */
    public static <T extends Number> Expression<Double> avgDistinctWhere(@NonNull Expression<T> expression, @NonNull Condition condition) {
        return avgDistinctWhere(expression, List.of(condition));
    }

    /**
     * Returns the average of the distinct values from the rows matching the filter conditions.
     *
     * @param <T>        the numeric type
     * @param expression the numeric expression
     * @param conditions the filter conditions combined with {@code AND}
     * @return average expression over the distinct values of the matching rows
     */
    public static <T extends Number> Expression<Double> avgDistinctWhere(@NonNull Expression<T> expression, @NonNull Condition... conditions) {
        return avgDistinctWhere(expression, Arrays.asList(conditions));
    }

    /**
     * Returns the average of the distinct values from the rows matching the filter conditions.
     *
     * @param <T>        the numeric type
     * @param expression the numeric expression
     * @param conditions the filter conditions combined with {@code AND}
     * @return average expression over the distinct values of the matching rows
     */
    public static <T extends Number> Expression<Double> avgDistinctWhere(@NonNull Expression<T> expression, @NonNull List<Condition> conditions) {
        final var expr = new PortableAggregateExpression<>(Double.class, AggregateQuantifier.DISTINCT, PortableAggregateExpression.Function.AVG, List.of(expression));
        expr.getFilter().addAll(conditions);
        return expr;
    }

    /**
     * Returns the average of the values from the rows matching the filter conditions.
     *
     * @param <T>        the numeric type
     * @param expression the numeric expression
     * @param condition  the filter condition
     * @return average expression over the matching rows
     */
    public static <T extends Number> Expression<Double> avgWhere(@NonNull Expression<T> expression, @NonNull Condition condition) {
        return avgWhere(expression, List.of(condition));
    }

    /**
     * Returns the average of the values from the rows matching the filter conditions.
     *
     * @param <T>        the numeric type
     * @param expression the numeric expression
     * @param conditions the filter conditions combined with {@code AND}
     * @return average expression over the matching rows
     */
    public static <T extends Number> Expression<Double> avgWhere(@NonNull Expression<T> expression, @NonNull Condition... conditions) {
        return avgWhere(expression, Arrays.asList(conditions));
    }

    /**
     * Returns the average of the values from the rows matching the filter conditions.
     *
     * @param <T>        the numeric type
     * @param expression the numeric expression
     * @param conditions the filter conditions combined with {@code AND}
     * @return average expression over the matching rows
     */
    public static <T extends Number> Expression<Double> avgWhere(@NonNull Expression<T> expression, @NonNull List<Condition> conditions) {
        var expr = new PortableAggregateExpression<>(Double.class, AggregateQuantifier.NONE, PortableAggregateExpression.Function.AVG, List.of(expression));
        expr.getFilter().addAll(conditions);
        return expr;
    }

    /**
     * Returns the bitwise AND of all non-null input values.
     *
     * @param <T>        the numeric type
     * @param expression the numeric expression
     * @return bitwise AND aggregate expression
     */
    public static <T extends Number> Expression<T> bitAndAgg(@NonNull Expression<T> expression) {
        return new PortableAggregateExpression<>(expression.getJavaType(), AggregateQuantifier.NONE, PortableAggregateExpression.Function.BIT_AND_AGG, List.of(expression));
    }

    /**
     * Returns the bitwise AND of the non-null values from the rows matching the filter conditions.
     *
     * @param <T>        the numeric type
     * @param expression the numeric expression
     * @param condition  the filter condition
     * @return bitwise AND aggregate expression over the matching rows
     */
    public static <T extends Number> Expression<T> bitAndAggWhere(@NonNull Expression<T> expression, @NonNull Condition condition) {
        return bitAndAggWhere(expression, List.of(condition));
    }

    /**
     * Returns the bitwise AND of the non-null values from the rows matching the filter conditions.
     *
     * @param <T>        the numeric type
     * @param expression the numeric expression
     * @param conditions the filter conditions combined with {@code AND}
     * @return bitwise AND aggregate expression over the matching rows
     */
    public static <T extends Number> Expression<T> bitAndAggWhere(@NonNull Expression<T> expression, @NonNull Condition... conditions) {
        return bitAndAggWhere(expression, Arrays.asList(conditions));
    }

    /**
     * Returns the bitwise AND of the non-null values from the rows matching the filter conditions.
     *
     * @param <T>        the numeric type
     * @param expression the numeric expression
     * @param conditions the filter conditions combined with {@code AND}
     * @return bitwise AND aggregate expression over the matching rows
     */
    public static <T extends Number> Expression<T> bitAndAggWhere(@NonNull Expression<T> expression, @NonNull List<Condition> conditions) {
        final var expr = new PortableAggregateExpression<>(expression.getJavaType(), AggregateQuantifier.NONE, PortableAggregateExpression.Function.BIT_AND_AGG, List.of(expression));
        expr.getFilter().addAll(conditions);
        return expr;
    }

    /**
     * Returns the bitwise NAND (negated bitwise AND) of all non-null input values.
     *
     * @param <T>        the numeric type
     * @param expression the numeric expression
     * @return bitwise NAND aggregate expression
     */
    public static <T extends Number> Expression<T> bitNandAgg(@NonNull Expression<T> expression) {
        return new PortableAggregateExpression<>(expression.getJavaType(), AggregateQuantifier.NONE, PortableAggregateExpression.Function.BIT_NAND_AGG, List.of(expression));
    }

    /**
     * Returns the bitwise NAND (negated bitwise AND) of the non-null values from the rows matching the filter conditions.
     *
     * @param <T>        the numeric type
     * @param expression the numeric expression
     * @param condition  the filter condition
     * @return bitwise NAND aggregate expression over the matching rows
     */
    public static <T extends Number> Expression<T> bitNandAggWhere(@NonNull Expression<T> expression, @NonNull Condition condition) {
        return bitNandAggWhere(expression, List.of(condition));
    }

    /**
     * Returns the bitwise NAND (negated bitwise AND) of the non-null values from the rows matching the filter conditions.
     *
     * @param <T>        the numeric type
     * @param expression the numeric expression
     * @param conditions the filter conditions combined with {@code AND}
     * @return bitwise NAND aggregate expression over the matching rows
     */
    public static <T extends Number> Expression<T> bitNandAggWhere(@NonNull Expression<T> expression, @NonNull Condition... conditions) {
        return bitNandAggWhere(expression, Arrays.asList(conditions));
    }

    /**
     * Returns the bitwise NAND (negated bitwise AND) of the non-null values from the rows matching the filter conditions.
     *
     * @param <T>        the numeric type
     * @param expression the numeric expression
     * @param conditions the filter conditions combined with {@code AND}
     * @return bitwise NAND aggregate expression over the matching rows
     */
    public static <T extends Number> Expression<T> bitNandAggWhere(@NonNull Expression<T> expression, @NonNull List<Condition> conditions) {
        final var expr = new PortableAggregateExpression<>(expression.getJavaType(), AggregateQuantifier.NONE, PortableAggregateExpression.Function.BIT_NAND_AGG, List.of(expression));
        expr.getFilter().addAll(conditions);
        return expr;
    }

    /**
     * Returns the bitwise NOR (negated bitwise OR) of all non-null input values.
     *
     * @param <T>        the numeric type
     * @param expression the numeric expression
     * @return bitwise NOR aggregate expression
     */
    public static <T extends Number> Expression<T> bitNorAgg(@NonNull Expression<T> expression) {
        return new PortableAggregateExpression<>(expression.getJavaType(), AggregateQuantifier.NONE, PortableAggregateExpression.Function.BIT_NOR_AGG, List.of(expression));
    }

    /**
     * Returns the bitwise NOR (negated bitwise OR) of the non-null values from the rows matching the filter conditions.
     *
     * @param <T>        the numeric type
     * @param expression the numeric expression
     * @param condition  the filter condition
     * @return bitwise NOR aggregate expression over the matching rows
     */
    public static <T extends Number> Expression<T> bitNorAggWhere(@NonNull Expression<T> expression, @NonNull Condition condition) {
        return bitNorAggWhere(expression, List.of(condition));
    }

    /**
     * Returns the bitwise NOR (negated bitwise OR) of the non-null values from the rows matching the filter conditions.
     *
     * @param <T>        the numeric type
     * @param expression the numeric expression
     * @param conditions the filter conditions combined with {@code AND}
     * @return bitwise NOR aggregate expression over the matching rows
     */
    public static <T extends Number> Expression<T> bitNorAggWhere(@NonNull Expression<T> expression, @NonNull Condition... conditions) {
        return bitNorAggWhere(expression, Arrays.asList(conditions));
    }

    /**
     * Returns the bitwise NOR (negated bitwise OR) of the non-null values from the rows matching the filter conditions.
     *
     * @param <T>        the numeric type
     * @param expression the numeric expression
     * @param conditions the filter conditions combined with {@code AND}
     * @return bitwise NOR aggregate expression over the matching rows
     */
    public static <T extends Number> Expression<T> bitNorAggWhere(@NonNull Expression<T> expression, @NonNull List<Condition> conditions) {
        final var expr = new PortableAggregateExpression<>(expression.getJavaType(), AggregateQuantifier.NONE, PortableAggregateExpression.Function.BIT_NOR_AGG, List.of(expression));
        expr.getFilter().addAll(conditions);
        return expr;
    }

    /**
     * Returns the bitwise OR of all non-null input values.
     *
     * @param <T>        the numeric type
     * @param expression the numeric expression
     * @return bitwise OR aggregate expression
     */
    public static <T extends Number> Expression<T> bitOrAgg(@NonNull Expression<T> expression) {
        return new PortableAggregateExpression<>(expression.getJavaType(), AggregateQuantifier.NONE, PortableAggregateExpression.Function.BIT_OR_AGG, List.of(expression));
    }

    /**
     * Returns the bitwise OR of the non-null values from the rows matching the filter conditions.
     *
     * @param <T>        the numeric type
     * @param expression the numeric expression
     * @param condition  the filter condition
     * @return bitwise OR aggregate expression over the matching rows
     */
    public static <T extends Number> Expression<T> bitOrAggWhere(@NonNull Expression<T> expression, @NonNull Condition condition) {
        return bitOrAggWhere(expression, List.of(condition));
    }

    /**
     * Returns the bitwise OR of the non-null values from the rows matching the filter conditions.
     *
     * @param <T>        the numeric type
     * @param expression the numeric expression
     * @param conditions the filter conditions combined with {@code AND}
     * @return bitwise OR aggregate expression over the matching rows
     */
    public static <T extends Number> Expression<T> bitOrAggWhere(@NonNull Expression<T> expression, @NonNull Condition... conditions) {
        return bitOrAggWhere(expression, Arrays.asList(conditions));
    }

    /**
     * Returns the bitwise OR of the non-null values from the rows matching the filter conditions.
     *
     * @param <T>        the numeric type
     * @param expression the numeric expression
     * @param conditions the filter conditions combined with {@code AND}
     * @return bitwise OR aggregate expression over the matching rows
     */
    public static <T extends Number> Expression<T> bitOrAggWhere(@NonNull Expression<T> expression, @NonNull List<Condition> conditions) {
        final var expr = new PortableAggregateExpression<>(expression.getJavaType(), AggregateQuantifier.NONE, PortableAggregateExpression.Function.BIT_OR_AGG, List.of(expression));
        expr.getFilter().addAll(conditions);
        return expr;
    }

    /**
     * Returns the bitwise XOR of all non-null input values.
     *
     * @param <T>        the numeric type
     * @param expression the numeric expression
     * @return bitwise XOR aggregate expression
     */
    public static <T extends Number> Expression<T> bitXorAgg(@NonNull Expression<T> expression) {
        return new PortableAggregateExpression<>(expression.getJavaType(), AggregateQuantifier.NONE, PortableAggregateExpression.Function.BIT_XOR_AGG, List.of(expression));
    }

    /**
     * Returns the bitwise XOR of the non-null values from the rows matching the filter conditions.
     *
     * @param <T>        the numeric type
     * @param expression the numeric expression
     * @param condition  the filter condition
     * @return bitwise XOR aggregate expression over the matching rows
     */
    public static <T extends Number> Expression<T> bitXorAggWhere(@NonNull Expression<T> expression, @NonNull Condition condition) {
        return bitXorAggWhere(expression, List.of(condition));
    }

    /**
     * Returns the bitwise XOR of the non-null values from the rows matching the filter conditions.
     *
     * @param <T>        the numeric type
     * @param expression the numeric expression
     * @param conditions the filter conditions combined with {@code AND}
     * @return bitwise XOR aggregate expression over the matching rows
     */
    public static <T extends Number> Expression<T> bitXorAggWhere(@NonNull Expression<T> expression, @NonNull Condition... conditions) {
        return bitXorAggWhere(expression, Arrays.asList(conditions));
    }

    /**
     * Returns the bitwise XOR of the non-null values from the rows matching the filter conditions.
     *
     * @param <T>        the numeric type
     * @param expression the numeric expression
     * @param conditions the filter conditions combined with {@code AND}
     * @return bitwise XOR aggregate expression over the matching rows
     */
    public static <T extends Number> Expression<T> bitXorAggWhere(@NonNull Expression<T> expression, @NonNull List<Condition> conditions) {
        final var expr = new PortableAggregateExpression<>(expression.getJavaType(), AggregateQuantifier.NONE, PortableAggregateExpression.Function.BIT_XOR_AGG, List.of(expression));
        expr.getFilter().addAll(conditions);
        return expr;
    }

    /**
     * Returns the bitwise XNOR (negated bitwise XOR) of all non-null input values.
     *
     * @param <T>        the numeric type
     * @param expression the numeric expression
     * @return bitwise XNOR aggregate expression
     */
    public static <T extends Number> Expression<T> bitXnorAgg(@NonNull Expression<T> expression) {
        return new PortableAggregateExpression<>(expression.getJavaType(), AggregateQuantifier.NONE, PortableAggregateExpression.Function.BIT_XNOR_AGG, List.of(expression));
    }

    /**
     * Returns the bitwise XNOR (negated bitwise XOR) of the non-null values from the rows matching the filter conditions.
     *
     * @param <T>        the numeric type
     * @param expression the numeric expression
     * @param condition  the filter condition
     * @return bitwise XNOR aggregate expression over the matching rows
     */
    public static <T extends Number> Expression<T> bitXnorAggWhere(@NonNull Expression<T> expression, @NonNull Condition condition) {
        return bitXnorAggWhere(expression, List.of(condition));
    }

    /**
     * Returns the bitwise XNOR (negated bitwise XOR) of the non-null values from the rows matching the filter conditions.
     *
     * @param <T>        the numeric type
     * @param expression the numeric expression
     * @param conditions the filter conditions combined with {@code AND}
     * @return bitwise XNOR aggregate expression over the matching rows
     */
    public static <T extends Number> Expression<T> bitXnorAggWhere(@NonNull Expression<T> expression, @NonNull Condition... conditions) {
        return bitXnorAggWhere(expression, Arrays.asList(conditions));
    }

    /**
     * Returns the bitwise XNOR (negated bitwise XOR) of the non-null values from the rows matching the filter conditions.
     *
     * @param <T>        the numeric type
     * @param expression the numeric expression
     * @param conditions the filter conditions combined with {@code AND}
     * @return bitwise XNOR aggregate expression over the matching rows
     */
    public static <T extends Number> Expression<T> bitXnorAggWhere(@NonNull Expression<T> expression, @NonNull List<Condition> conditions) {
        final var expr = new PortableAggregateExpression<>(expression.getJavaType(), AggregateQuantifier.NONE, PortableAggregateExpression.Function.BIT_XNOR_AGG, List.of(expression));
        expr.getFilter().addAll(conditions);
        return expr;
    }

    /**
     * Returns true if all input values are true.
     *
     * @param expression the boolean expression
     * @return boolean and expression
     */
    public static Expression<Boolean> boolAnd(@NonNull Expression<Boolean> expression) {
        return new PortableAggregateExpression<>(expression.getJavaType(), AggregateQuantifier.NONE, PortableAggregateExpression.Function.BOOL_AND, List.of(expression));
    }

    /**
     * Returns true if all input values from the rows matching the filter conditions are true.
     *
     * @param expression the boolean expression
     * @param condition  the filter condition
     * @return boolean and expression over the matching rows
     */
    public static Expression<Boolean> boolAndWhere(@NonNull Expression<Boolean> expression, @NonNull Condition condition) {
        return boolAndWhere(expression, List.of(condition));
    }

    /**
     * Returns true if all input values from the rows matching the filter conditions are true.
     *
     * @param expression the boolean expression
     * @param conditions the filter conditions combined with {@code AND}
     * @return boolean and expression over the matching rows
     */
    public static Expression<Boolean> boolAndWhere(@NonNull Expression<Boolean> expression, @NonNull Condition... conditions) {
        return boolAndWhere(expression, Arrays.asList(conditions));
    }

    /**
     * Returns true if all input values from the rows matching the filter conditions are true.
     *
     * @param expression the boolean expression
     * @param conditions the filter conditions combined with {@code AND}
     * @return boolean and expression over the matching rows
     */
    public static Expression<Boolean> boolAndWhere(@NonNull Expression<Boolean> expression, @NonNull List<Condition> conditions) {
        final var expr = new PortableAggregateExpression<>(Boolean.class, AggregateQuantifier.NONE, PortableAggregateExpression.Function.BOOL_AND, List.of(expression));
        expr.getFilter().addAll(conditions);
        return expr;
    }

    /**
     * Returns true if any input value is true.
     *
     * @param expression the boolean expression
     * @return boolean or expression
     */
    public static Expression<Boolean> boolOr(@NonNull Expression<Boolean> expression) {
        return new PortableAggregateExpression<>(expression.getJavaType(), AggregateQuantifier.NONE, PortableAggregateExpression.Function.BOOL_OR, List.of(expression));
    }

    /**
     * Returns true if any input value from the rows matching the filter conditions is true.
     *
     * @param expression the boolean expression
     * @param condition  the filter condition
     * @return boolean or expression over the matching rows
     */
    public static Expression<Boolean> boolOrWhere(@NonNull Expression<Boolean> expression, @NonNull Condition condition) {
        return boolOrWhere(expression, List.of(condition));
    }

    /**
     * Returns true if any input value from the rows matching the filter conditions is true.
     *
     * @param expression the boolean expression
     * @param conditions the filter conditions combined with {@code AND}
     * @return boolean or expression over the matching rows
     */
    public static Expression<Boolean> boolOrWhere(@NonNull Expression<Boolean> expression, @NonNull Condition... conditions) {
        return boolOrWhere(expression, Arrays.asList(conditions));
    }

    /**
     * Returns true if any input value from the rows matching the filter conditions is true.
     *
     * @param expression the boolean expression
     * @param conditions the filter conditions combined with {@code AND}
     * @return boolean or expression over the matching rows
     */
    public static Expression<Boolean> boolOrWhere(@NonNull Expression<Boolean> expression, @NonNull List<Condition> conditions) {
        final var expr = new PortableAggregateExpression<>(Boolean.class, AggregateQuantifier.NONE, PortableAggregateExpression.Function.BOOL_OR, List.of(expression));
        expr.getFilter().addAll(conditions);
        return expr;
    }

    /**
     * Returns the count of all rows.
     * <p>
     * This is equivalent to the SQL {@code COUNT(*)} aggregate function.
     *
     * @return an expression representing the count of all rows
     */
    public static Expression<Long> count() {
        return COUNT;
    }

    /**
     * Returns the count of non-null values of an expression.
     * <p>
     * This is equivalent to the SQL {@code COUNT(expression)} function.
     *
     * @param expression the expression to count non-null values of
     * @return an expression representing the count of non-null values
     */
    public static Expression<Long> count(@NonNull Expression<?> expression) {
        return new PortableAggregateExpression<>(Long.class, AggregateQuantifier.NONE, PortableAggregateExpression.Function.COUNT, List.of(expression));
    }

    /**
     * Returns the count of the rows matching the filter conditions.
     *
     * @param where the filter condition
     * @return an expression representing the count of matching rows
     */
    public static Expression<Long> countWhere(@NonNull Condition where) {
        return countWhere(Collections.singletonList(where));
    }

    /**
     * Returns the count of the rows matching the filter conditions.
     *
     * @param where the filter conditions combined with {@code AND}
     * @return an expression representing the count of matching rows
     */
    public static Expression<Long> countWhere(@NonNull Condition... where) {
        return countWhere(Arrays.asList(where));
    }

    /**
     * Returns the count of the rows matching the filter conditions.
     *
     * @param where the filter conditions combined with {@code AND}
     * @return an expression representing the count of matching rows
     */
    public static Expression<Long> countWhere(@NonNull List<Condition> where) {
        final var expr = new PortableAggregateExpression<>(
                Long.class,
                AggregateQuantifier.NONE,
                PortableAggregateExpression.Function.COUNT,
                Collections.emptyList()
        );
        expr.getFilter().addAll(where);
        return expr;
    }

    /**
     * Returns the count of distinct non-null values of an expression.
     * <p>
     * This is equivalent to the SQL {@code COUNT(DISTINCT expression)} function.
     *
     * @param expression the expression to count distinct values of
     * @return an expression representing the count of distinct values
     */
    public static Expression<Long> countDistinct(@NonNull Expression<?> expression) {
        return new PortableAggregateExpression<>(Long.class, AggregateQuantifier.DISTINCT, PortableAggregateExpression.Function.COUNT, List.of(expression));
    }

    /**
     * Returns the count of distinct non-null values of an expression among the rows matching the filter conditions.
     *
     * @param expression the expression to count distinct values of
     * @param where      the filter condition
     * @return an expression representing the count of distinct values of the matching rows
     */
    public static Expression<Long> countDistinctWhere(@NonNull Expression<?> expression, @NonNull Condition where) {
        return countDistinctWhere(expression, Collections.singletonList(where));
    }

    /**
     * Returns the count of distinct non-null values of an expression among the rows matching the filter conditions.
     *
     * @param expression the expression to count distinct values of
     * @param where      the filter conditions combined with {@code AND}
     * @return an expression representing the count of distinct values of the matching rows
     */
    public static Expression<Long> countDistinctWhere(@NonNull Expression<?> expression, @NonNull Condition... where) {
        return countDistinctWhere(expression, Arrays.asList(where));
    }

    /**
     * Returns the count of distinct non-null values of an expression among the rows matching the filter conditions.
     *
     * @param expression the expression to count distinct values of
     * @param where      the filter conditions combined with {@code AND}
     * @return an expression representing the count of distinct values of the matching rows
     */
    public static Expression<Long> countDistinctWhere(@NonNull Expression<?> expression, @NonNull List<Condition> where) {
        final var expr = new PortableAggregateExpression<>(
                Long.class,
                AggregateQuantifier.DISTINCT,
                PortableAggregateExpression.Function.COUNT,
                List.of(expression)
        );
        expr.getFilter().addAll(where);
        return expr;
    }

    /**
     * Returns true if all input values are true (alias for boolAnd).
     *
     * @param expression the boolean expression
     * @return every expression
     */
    public static Expression<Boolean> every(@NonNull Expression<Boolean> expression) {
        return new PortableAggregateExpression<>(expression.getJavaType(), AggregateQuantifier.NONE, PortableAggregateExpression.Function.EVERY, List.of(expression));
    }

    /**
     * Returns true if all input values from the rows matching the filter conditions are true (alias for boolAndWhere).
     *
     * @param expression the boolean expression
     * @param condition  the filter condition
     * @return every expression over the matching rows
     */
    public static Expression<Boolean> everyWhere(@NonNull Expression<Boolean> expression, @NonNull Condition condition) {
        return everyWhere(expression, List.of(condition));
    }

    /**
     * Returns true if all input values from the rows matching the filter conditions are true (alias for boolAndWhere).
     *
     * @param expression the boolean expression
     * @param conditions the filter conditions combined with {@code AND}
     * @return every expression over the matching rows
     */
    public static Expression<Boolean> everyWhere(@NonNull Expression<Boolean> expression, @NonNull Condition... conditions) {
        return everyWhere(expression, Arrays.asList(conditions));
    }

    /**
     * Returns true if all input values from the rows matching the filter conditions are true (alias for boolAndWhere).
     *
     * @param expression the boolean expression
     * @param conditions the filter conditions combined with {@code AND}
     * @return every expression over the matching rows
     */
    public static Expression<Boolean> everyWhere(@NonNull Expression<Boolean> expression, @NonNull List<Condition> conditions) {
        final var expr = new PortableAggregateExpression<>(Boolean.class, AggregateQuantifier.NONE, PortableAggregateExpression.Function.EVERY, List.of(expression));
        expr.getFilter().addAll(conditions);
        return expr;
    }

    /**
     * Concatenates the input values into a single string separated by the given separator.
     * <p>
     * This is equivalent to the SQL {@code GROUP_CONCAT(expression SEPARATOR separator)} function ({@code string_agg} on PostgreSQL).
     *
     * @param expression the expression whose values are concatenated
     * @param separator  the separator placed between concatenated values
     * @return an expression representing the concatenated string
     */
    public static Expression<String> groupConcat(@NonNull Expression<?> expression, @NonNull Expression<String> separator) {
        return new PortableAggregateExpression<>(String.class, AggregateQuantifier.NONE, PortableAggregateExpression.Function.GROUP_CONCAT, List.of(expression, separator));
    }

    /**
     * Concatenates the input values into a single string separated by the given separator.
     * <p>
     * This is equivalent to the SQL {@code GROUP_CONCAT(expression SEPARATOR separator)} function ({@code string_agg} on PostgreSQL).
     *
     * @param expression the expression whose values are concatenated
     * @param separator  the separator placed between concatenated values
     * @return an expression representing the concatenated string
     */
    public static Expression<String> groupConcat(@NonNull Expression<?> expression, @NonNull String separator) {
        return groupConcat(expression, constant(separator));
    }

    /**
     * Concatenates the input values into a single string separated by the given separator.
     * <p>
     * This is equivalent to the SQL {@code GROUP_CONCAT(expression SEPARATOR separator)} function ({@code string_agg} on PostgreSQL).
     *
     * @param expression the expression whose values are concatenated
     * @param separator  the separator placed between concatenated values
     * @param orderBy    the ordering applied to the concatenated values
     * @return an expression representing the concatenated string
     */
    public static Expression<String> groupConcat(@NonNull Expression<?> expression, @NonNull Expression<String> separator, @NonNull Order orderBy) {
        var expr = new PortableAggregateExpression<>(String.class, AggregateQuantifier.NONE, PortableAggregateExpression.Function.GROUP_CONCAT, List.of(expression, separator));
        expr.getOrders().add(orderBy);
        return expr;
    }

    /**
     * Concatenates the input values into a single string separated by the given separator.
     * <p>
     * This is equivalent to the SQL {@code GROUP_CONCAT(expression SEPARATOR separator)} function ({@code string_agg} on PostgreSQL).
     *
     * @param expression the expression whose values are concatenated
     * @param separator  the separator placed between concatenated values
     * @param orderBy    the ordering applied to the concatenated values
     * @return an expression representing the concatenated string
     */
    public static Expression<String> groupConcat(@NonNull Expression<?> expression, @NonNull String separator, @NonNull Order orderBy) {
        return groupConcat(expression, constant(separator), orderBy);
    }

    /**
     * Concatenates the distinct input values into a single string separated by the given separator.
     *
     * @param expression the expression whose distinct values are concatenated
     * @param separator  the separator placed between concatenated values
     * @return an expression representing the concatenated string
     */
    public static Expression<String> groupConcatDistinct(@NonNull Expression<?> expression, @NonNull Expression<String> separator) {
        return new PortableAggregateExpression<>(String.class, AggregateQuantifier.DISTINCT, PortableAggregateExpression.Function.GROUP_CONCAT, List.of(expression, separator));
    }

    /**
     * Concatenates the distinct input values into a single string separated by the given separator.
     *
     * @param expression the expression whose distinct values are concatenated
     * @param separator  the separator placed between concatenated values
     * @return an expression representing the concatenated string
     */
    public static Expression<String> groupConcatDistinct(@NonNull Expression<?> expression, @NonNull String separator) {
        return groupConcatDistinct(expression, constant(separator));
    }

    /**
     * Concatenates the distinct values from the rows matching the filter conditions into a single string separated by the given separator.
     *
     * @param expression the expression whose distinct values are concatenated
     * @param separator  the separator placed between concatenated values
     * @param condition  the filter condition
     * @return an expression representing the concatenated string
     */
    public static Expression<String> groupConcatDistinctWhere(@NonNull Expression<?> expression, @NonNull String separator, @NonNull Condition condition) {
        return groupConcatDistinctWhere(expression, separator, List.of(condition));
    }

    /**
     * Concatenates the distinct values from the rows matching the filter conditions into a single string separated by the given separator.
     *
     * @param expression the expression whose distinct values are concatenated
     * @param separator  the separator placed between concatenated values
     * @param conditions the filter conditions combined with {@code AND}
     * @return an expression representing the concatenated string
     */
    public static Expression<String> groupConcatDistinctWhere(@NonNull Expression<?> expression, @NonNull String separator, @NonNull Condition... conditions) {
        return groupConcatDistinctWhere(expression, separator, Arrays.asList(conditions));
    }

    /**
     * Concatenates the distinct values from the rows matching the filter conditions into a single string separated by the given separator.
     *
     * @param expression the expression whose distinct values are concatenated
     * @param separator  the separator placed between concatenated values
     * @param conditions the filter conditions combined with {@code AND}
     * @return an expression representing the concatenated string
     */
    public static Expression<String> groupConcatDistinctWhere(@NonNull Expression<?> expression, @NonNull String separator, @NonNull List<Condition> conditions) {
        final var expr = new PortableAggregateExpression<>(String.class, AggregateQuantifier.DISTINCT, PortableAggregateExpression.Function.GROUP_CONCAT, List.of(expression, constant(separator)));
        expr.getFilter().addAll(conditions);
        return expr;
    }

    /**
     * Concatenates the values from the rows matching the filter conditions into a single string separated by the given separator.
     *
     * @param expression the expression whose values are concatenated
     * @param separator  the separator placed between concatenated values
     * @param condition  the filter condition
     * @return an expression representing the concatenated string
     */
    public static Expression<String> groupConcatWhere(@NonNull Expression<?> expression, @NonNull String separator, @NonNull Condition condition) {
        return groupConcatWhere(expression, separator, List.of(condition));
    }

    /**
     * Concatenates the values from the rows matching the filter conditions into a single string separated by the given separator.
     *
     * @param expression the expression whose values are concatenated
     * @param separator  the separator placed between concatenated values
     * @param conditions the filter conditions combined with {@code AND}
     * @return an expression representing the concatenated string
     */
    public static Expression<String> groupConcatWhere(@NonNull Expression<?> expression, @NonNull String separator, @NonNull Condition... conditions) {
        return groupConcatWhere(expression, separator, Arrays.asList(conditions));
    }

    /**
     * Concatenates the values from the rows matching the filter conditions into a single string separated by the given separator.
     *
     * @param expression the expression whose values are concatenated
     * @param separator  the separator placed between concatenated values
     * @param conditions the filter conditions combined with {@code AND}
     * @return an expression representing the concatenated string
     */
    public static Expression<String> groupConcatWhere(@NonNull Expression<?> expression, @NonNull String separator, @NonNull List<Condition> conditions) {
        final var expr = new PortableAggregateExpression<>(String.class, AggregateQuantifier.NONE, PortableAggregateExpression.Function.GROUP_CONCAT, List.of(expression, constant(separator)));
        expr.getFilter().addAll(conditions);
        return expr;
    }

    /**
     * Returns the minimum value of an expression across all rows.
     * <p>
     * This is equivalent to the SQL {@code MIN(expression)} aggregate function.
     *
     * @param <T>        the expression type
     * @param expression the expression to find the minimum of
     * @return an expression representing the minimum value
     */
    public static <T> Expression<T> min(@NonNull Expression<T> expression) {
        return new PortableAggregateExpression<>(expression.getJavaType(), AggregateQuantifier.NONE, PortableAggregateExpression.Function.MIN, List.of(expression));
    }

    /**
     * Returns the minimum value of an expression among the rows matching the filter conditions.
     *
     * @param <T>        the expression type
     * @param expression the expression to find the minimum of
     * @param condition  the filter condition
     * @return an expression representing the minimum value of the matching rows
     */
    public static <T> Expression<T> minWhere(@NonNull Expression<T> expression, @NonNull Condition condition) {
        return minWhere(expression, List.of(condition));
    }

    /**
     * Returns the minimum value of an expression among the rows matching the filter conditions.
     *
     * @param <T>        the expression type
     * @param expression the expression to find the minimum of
     * @param conditions the filter conditions combined with {@code AND}
     * @return an expression representing the minimum value of the matching rows
     */
    public static <T> Expression<T> minWhere(@NonNull Expression<T> expression, @NonNull Condition... conditions) {
        return minWhere(expression, Arrays.asList(conditions));
    }

    /**
     * Returns the minimum value of an expression among the rows matching the filter conditions.
     *
     * @param <T>        the expression type
     * @param expression the expression to find the minimum of
     * @param conditions the filter conditions combined with {@code AND}
     * @return an expression representing the minimum value of the matching rows
     */
    public static <T> Expression<T> minWhere(@NonNull Expression<T> expression, @NonNull List<Condition> conditions) {
        final var expr = new PortableAggregateExpression<>(expression.getJavaType(), AggregateQuantifier.NONE, PortableAggregateExpression.Function.MIN, List.of(expression));
        expr.getFilter().addAll(conditions);
        return expr;
    }

    /**
     * Returns the maximum value of an expression across all rows.
     * <p>
     * This is equivalent to the SQL {@code MAX(expression)} aggregate function.
     *
     * @param <T>        the expression type
     * @param expression the expression to find the maximum of
     * @return an expression representing the maximum value
     */
    public static <T> Expression<T> max(@NonNull Expression<T> expression) {
        return new PortableAggregateExpression<>(expression.getJavaType(), AggregateQuantifier.NONE, PortableAggregateExpression.Function.MAX, List.of(expression));
    }

    /**
     * Returns the maximum value of an expression among the rows matching the filter conditions.
     *
     * @param <T>        the expression type
     * @param expression the expression to find the maximum of
     * @param condition  the filter condition
     * @return an expression representing the maximum value of the matching rows
     */
    public static <T> Expression<T> maxWhere(@NonNull Expression<T> expression, @NonNull Condition condition) {
        return maxWhere(expression, List.of(condition));
    }

    /**
     * Returns the maximum value of an expression among the rows matching the filter conditions.
     *
     * @param <T>        the expression type
     * @param expression the expression to find the maximum of
     * @param conditions the filter conditions combined with {@code AND}
     * @return an expression representing the maximum value of the matching rows
     */
    public static <T> Expression<T> maxWhere(@NonNull Expression<T> expression, @NonNull Condition... conditions) {
        return maxWhere(expression, Arrays.asList(conditions));
    }

    /**
     * Returns the maximum value of an expression among the rows matching the filter conditions.
     *
     * @param <T>        the expression type
     * @param expression the expression to find the maximum of
     * @param conditions the filter conditions combined with {@code AND}
     * @return an expression representing the maximum value of the matching rows
     */
    public static <T> Expression<T> maxWhere(@NonNull Expression<T> expression, @NonNull List<Condition> conditions) {
        final var expr = new PortableAggregateExpression<>(expression.getJavaType(), AggregateQuantifier.NONE, PortableAggregateExpression.Function.MAX, List.of(expression));
        expr.getFilter().addAll(conditions);
        return expr;
    }

    /**
     * Returns the sum of a numeric expression across all rows.
     * <p>
     * This is equivalent to the SQL {@code SUM(expression)} aggregate function.
     *
     * @param <T>        the numeric type
     * @param expression the numeric expression to sum
     * @return an expression representing the sum
     */
    public static <T extends Number> Expression<T> sum(@NonNull Expression<T> expression) {
        return new PortableAggregateExpression<>(expression.getJavaType(), AggregateQuantifier.NONE, PortableAggregateExpression.Function.SUM, List.of(expression));
    }

    /**
     * Returns the sum of the distinct values of a numeric expression.
     *
     * @param <T>        the numeric type
     * @param expression the numeric expression to sum
     * @return an expression representing the sum of distinct values
     */
    public static <T extends Number> Expression<T> sumDistinct(@NonNull Expression<T> expression) {
        return new PortableAggregateExpression<>(expression.getJavaType(), AggregateQuantifier.DISTINCT, PortableAggregateExpression.Function.SUM, List.of(expression));
    }

    /**
     * Returns the sum of the distinct values from the rows matching the filter conditions.
     *
     * @param <T>        the numeric type
     * @param expression the numeric expression to sum
     * @param condition  the filter condition
     * @return an expression representing the sum of distinct values of the matching rows
     */
    public static <T extends Number> Expression<T> sumDistinctWhere(@NonNull Expression<T> expression, @NonNull Condition condition) {
        return sumDistinctWhere(expression, List.of(condition));
    }

    /**
     * Returns the sum of the distinct values from the rows matching the filter conditions.
     *
     * @param <T>        the numeric type
     * @param expression the numeric expression to sum
     * @param conditions the filter conditions combined with {@code AND}
     * @return an expression representing the sum of distinct values of the matching rows
     */
    public static <T extends Number> Expression<T> sumDistinctWhere(@NonNull Expression<T> expression, @NonNull Condition... conditions) {
        return sumDistinctWhere(expression, Arrays.asList(conditions));
    }

    /**
     * Returns the sum of the distinct values from the rows matching the filter conditions.
     *
     * @param <T>        the numeric type
     * @param expression the numeric expression to sum
     * @param conditions the filter conditions combined with {@code AND}
     * @return an expression representing the sum of distinct values of the matching rows
     */
    public static <T extends Number> Expression<T> sumDistinctWhere(@NonNull Expression<T> expression, @NonNull List<Condition> conditions) {
        final var expr = new PortableAggregateExpression<>(expression.getJavaType(), AggregateQuantifier.DISTINCT, PortableAggregateExpression.Function.SUM, List.of(expression));
        expr.getFilter().addAll(conditions);
        return expr;
    }

    /**
     * Returns the sum of the values from the rows matching the filter conditions.
     *
     * @param <T>        the numeric type
     * @param expression the numeric expression to sum
     * @param condition  the filter condition
     * @return an expression representing the sum over the matching rows
     */
    public static <T extends Number> Expression<T> sumWhere(@NonNull Expression<T> expression, @NonNull Condition condition) {
        return sumWhere(expression, List.of(condition));
    }

    /**
     * Returns the sum of the values from the rows matching the filter conditions.
     *
     * @param <T>        the numeric type
     * @param expression the numeric expression to sum
     * @param conditions the filter conditions combined with {@code AND}
     * @return an expression representing the sum over the matching rows
     */
    public static <T extends Number> Expression<T> sumWhere(@NonNull Expression<T> expression, @NonNull Condition... conditions) {
        return sumWhere(expression, Arrays.asList(conditions));
    }

    /**
     * Returns the sum of the values from the rows matching the filter conditions.
     *
     * @param <T>        the numeric type
     * @param expression the numeric expression to sum
     * @param conditions the filter conditions combined with {@code AND}
     * @return an expression representing the sum over the matching rows
     */
    public static <T extends Number> Expression<T> sumWhere(@NonNull Expression<T> expression, @NonNull List<Condition> conditions) {
        final var expr = new PortableAggregateExpression<>(expression.getJavaType(), AggregateQuantifier.NONE, PortableAggregateExpression.Function.SUM, List.of(expression));
        expr.getFilter().addAll(conditions);
        return expr;
    }

    /**
     * Returns the population standard deviation.
     *
     * @param expression the numeric expression
     * @param <T>        the numeric type
     * @return standard deviation expression
     */
    public static <T extends Number> Expression<Double> stddevPop(@NonNull Expression<T> expression) {
        return new PortableAggregateExpression<>(Double.class, AggregateQuantifier.NONE, PortableAggregateExpression.Function.STDDEV_POP, List.of(expression));
    }

    /**
     * Returns the sample standard deviation.
     *
     * @param expression the numeric expression
     * @param <T>        the numeric type
     * @return standard deviation expression
     */
    public static <T extends Number> Expression<Double> stddevSamp(@NonNull Expression<T> expression) {
        return new PortableAggregateExpression<>(Double.class, AggregateQuantifier.NONE, PortableAggregateExpression.Function.STDDEV_SAMP, List.of(expression));
    }

    /**
     * Returns the population variance.
     *
     * @param expression the numeric expression
     * @param <T>        the numeric type
     * @return variance expression
     */
    public static <T extends Number> Expression<Double> varPop(@NonNull Expression<T> expression) {
        return new PortableAggregateExpression<>(Double.class, AggregateQuantifier.NONE, PortableAggregateExpression.Function.VAR_POP, List.of(expression));
    }

    /**
     * Returns the sample variance.
     *
     * @param expression the numeric expression
     * @param <T>        the numeric type
     * @return variance expression
     */
    public static <T extends Number> Expression<Double> varSamp(@NonNull Expression<T> expression) {
        return new PortableAggregateExpression<>(Double.class, AggregateQuantifier.NONE, PortableAggregateExpression.Function.VAR_SAMP, List.of(expression));
    }

    /**
     * Returns true if the subquery returns at least one row.
     * <p>
     * This is equivalent to the SQL {@code EXISTS(subquery)} function.
     *
     * @param expression the subquery expression to check for existence
     * @return an expression representing whether any rows exist
     */
    public static Condition exists(@NonNull Expression<?> expression) {
        return condition(new PortableFunctionExpression<>(PortableFunctionExpression.Function.EXISTS, List.of(expression), Boolean.class));
    }

    /**
     * Converts an Expression<Boolean> to a Condition.
     * <p>
     * This is useful for boolean expressions like EXISTS, or other expressions
     * that return boolean values but need to be used in WHERE clauses.
     * <p>
     * Most users won't need to call this directly - the .where() method
     * accepts Expression<Boolean> and converts automatically.
     *
     * @param expression the boolean expression to convert
     * @return a Condition wrapping the expression
     */
    public static Condition condition(@NonNull Expression<Boolean> expression) {
        if (expression instanceof Condition) {
            return (Condition) expression;
        }
        return new ExpressionBooleanCondition(expression);
    }

    /**
     * Converts a string expression to lower case.
     * <p>
     * This is equivalent to the SQL {@code LOWER(string)} function.
     *
     * @param expression the string expression to convert
     * @return an expression representing the string in lower case
     */
    public static Expression<String> lower(@NonNull Expression<String> expression) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.LOWER, List.of(expression), String.class);
    }

    /**
     * Converts a string to lower case.
     * <p>
     * This is equivalent to the SQL {@code LOWER(string)} function.
     *
     * @param text the string to convert
     * @return an expression representing the string in lower case
     */
    public static Expression<String> lower(@NonNull String text) {
        return lower(constant(text));
    }

    /**
     * Converts a string expression to upper case.
     * <p>
     * This is equivalent to the SQL {@code UPPER(string)} function.
     *
     * @param expression the string expression to convert
     * @return an expression representing the string in upper case
     */
    public static Expression<String> upper(@NonNull Expression<String> expression) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.UPPER, List.of(expression), String.class);
    }

    /**
     * Converts a string to upper case.
     * <p>
     * This is equivalent to the SQL {@code UPPER(string)} function.
     *
     * @param text the string to convert
     * @return an expression representing the string in upper case
     */
    public static Expression<String> upper(@NonNull String text) {
        return upper(constant(text));
    }

    /**
     * Removes leading and trailing whitespace from a string expression.
     * <p>
     * This is equivalent to the SQL {@code TRIM(string)} function.
     *
     * @param expression the string expression to trim
     * @return an expression representing the trimmed string
     */
    public static Expression<String> trim(@NonNull Expression<String> expression) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.TRIM, List.of(expression), String.class);
    }

    /**
     * Removes leading and trailing whitespace from a string.
     * <p>
     * This is equivalent to the SQL {@code TRIM(string)} function.
     *
     * @param text the string to trim
     * @return an expression representing the trimmed string
     */
    public static Expression<String> trim(@NonNull String text) {
        return trim(constant(text));
    }

    /**
     * Removes the longest string consisting only of characters in {@code characters}
     * from both the start and the end of {@code string}.
     * <p>
     * Renders as the standard SQL {@code TRIM(BOTH characters FROM string)}.
     *
     * @param string     the source string expression
     * @param characters the expression representing the characters to remove
     * @return an expression representing the trimmed string
     */
    public static Expression<String> trim(@NonNull Expression<String> string, @NonNull Expression<String> characters) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.TRIM, List.of(string, characters), String.class);
    }

    /**
     * Removes the longest string consisting only of characters in {@code characters}
     * from both the start and the end of {@code string}.
     * <p>
     * Renders as the standard SQL {@code TRIM(BOTH characters FROM string)}.
     *
     * @param string     the source string expression
     * @param characters the characters to remove
     * @return an expression representing the trimmed string
     */
    public static Expression<String> trim(@NonNull Expression<String> string, @NonNull String characters) {
        return trim(string, constant(characters));
    }

    /**
     * Returns first n characters in the string, or when n is negative, returns all but last |n| characters.
     * <p>
     * This is equivalent to the SQL {@code LEFT(string, n)} function.
     *
     * @param text the expression representing the string to extract from
     * @param n    the expression representing the number of characters to extract
     * @return an expression representing the extracted substring
     */
    public static Expression<String> left(@NonNull Expression<String> text, @NonNull Expression<Integer> n) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.LEFT, List.of(text, n), String.class);
    }

    /**
     * Returns first n characters in the string, or when n is negative, returns all but last |n| characters.
     * <p>
     * This is equivalent to the SQL {@code LEFT(string, n)} function.
     *
     * @param text the string to extract from
     * @param n    the number of characters to extract
     * @return an expression representing the extracted substring
     */
    public static Expression<String> left(String text, Integer n) {
        return left(constant(text), constant(n));
    }

    /**
     * Returns first n characters in the string, or when n is negative, returns all but last |n| characters.
     * <p>
     * This is equivalent to the SQL {@code LEFT(string, n)} function.
     *
     * @param text the expression representing the string to extract from
     * @param n    the number of characters to extract
     * @return an expression representing the extracted substring
     */
    public static Expression<String> left(@NonNull Expression<String> text, @NonNull Integer n) {
        return left(text, constant(n));
    }

    /**
     * Returns the rightmost n characters of the string.
     *
     * @param string the source string expression
     * @param n      the number of characters
     * @return right substring expression
     */
    public static Expression<String> right(@NonNull Expression<String> string, @NonNull Expression<Integer> n) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.RIGHT, List.of(string, n), String.class);
    }

    /**
     * Returns the rightmost n characters of the string.
     *
     * @param string the source string expression
     * @param n      the number of characters
     * @return right substring expression
     */
    public static Expression<String> right(@NonNull Expression<String> string, int n) {
        return right(string, constant(n));
    }

    /**
     * Returns the rightmost n characters of the string.
     *
     * @param string the source string
     * @param n      the number of characters
     * @return right substring expression
     */
    public static Expression<String> right(@NonNull String string, int n) {
        return right(constant(string), constant(n));
    }

    /**
     * Returns the rightmost n characters of the string.
     *
     * @param string the source string expression
     * @param n      the number of characters
     * @return right substring expression
     */
    public static Expression<String> right(@NonNull Expression<String> string, @NonNull Integer n) {
        return right(string, constant(n));
    }

    /**
     * Removes leading whitespace from the string.
     *
     * @param string the source string expression
     * @return trimmed string expression
     */
    public static Expression<String> ltrim(@NonNull Expression<String> string) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.LTRIM, List.of(string), String.class);
    }

    /**
     * Removes leading whitespace from the string.
     *
     * @param string the source string
     * @return trimmed string expression
     */
    public static Expression<String> ltrim(@NonNull String string) {
        return ltrim(constant(string));
    }

    /**
     * Removes leading characters matching the given set from the string.
     * <p>
     * Dialect behavior of the {@code characters} argument differs:
     * <ul>
     *   <li><b>PostgreSQL</b> and <b>H2</b> — {@code characters} is treated as a <i>set</i>;
     *       any character in the set is stripped from the leading edge.</li>
     *   <li><b>MariaDB</b> — rendered as {@code TRIM(LEADING characters FROM string)}, so
     *       {@code characters} is treated as a <i>literal substring</i> that is repeatedly
     *       stripped from the leading edge.</li>
     * </ul>
     *
     * @param string     the source string expression
     * @param characters the characters to remove
     * @return trimmed string expression
     */
    public static Expression<String> ltrim(@NonNull Expression<String> string, @NonNull Expression<String> characters) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.LTRIM, List.of(string, characters), String.class);
    }

    /**
     * Removes leading characters matching the given set from the string.
     * <p>
     * See {@link #ltrim(Expression, Expression)} for dialect-specific behavior of the
     * {@code characters} argument.
     *
     * @param string     the source string expression
     * @param characters the characters to remove
     * @return trimmed string expression
     */
    public static Expression<String> ltrim(@NonNull Expression<String> string, @NonNull String characters) {
        return ltrim(string, constant(characters));
    }

    /**
     * Removes trailing whitespace from the string.
     *
     * @param string the source string expression
     * @return trimmed string expression
     */
    public static Expression<String> rtrim(@NonNull Expression<String> string) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.RTRIM, List.of(string), String.class);
    }

    /**
     * Removes trailing whitespace from the string.
     *
     * @param string the source string
     * @return trimmed string expression
     */
    public static Expression<String> rtrim(@NonNull String string) {
        return rtrim(constant(string));
    }

    /**
     * Removes trailing characters matching the given set from the string.
     * <p>
     * Dialect behavior of the {@code characters} argument differs:
     * <ul>
     *   <li><b>PostgreSQL</b> and <b>H2</b> — {@code characters} is treated as a <i>set</i>;
     *       any character in the set is stripped from the trailing edge.</li>
     *   <li><b>MariaDB</b> — rendered as {@code TRIM(TRAILING characters FROM string)}, so
     *       {@code characters} is treated as a <i>literal substring</i> that is repeatedly
     *       stripped from the trailing edge.</li>
     * </ul>
     *
     * @param string     the source string expression
     * @param characters the characters to remove
     * @return trimmed string expression
     */
    public static Expression<String> rtrim(@NonNull Expression<String> string, @NonNull Expression<String> characters) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.RTRIM, List.of(string, characters), String.class);
    }

    /**
     * Removes trailing characters matching the given set from the string.
     * <p>
     * See {@link #rtrim(Expression, Expression)} for dialect-specific behavior of the
     * {@code characters} argument.
     *
     * @param string     the source string expression
     * @param characters the characters to remove
     * @return trimmed string expression
     */
    public static Expression<String> rtrim(@NonNull Expression<String> string, @NonNull String characters) {
        return rtrim(string, constant(characters));
    }

    /**
     * Returns the number of characters in a string expression.
     * <p>
     * This is equivalent to the SQL {@code LENGTH(string)} function.
     *
     * @param expression the string expression to get the length of
     * @return an expression representing the length of the string
     */
    public static Expression<Integer> length(@NonNull Expression<String> expression) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.CHAR_LENGTH, List.of(expression), Integer.class);
    }

    /**
     * Returns the number of characters in a string.
     * <p>
     * This is equivalent to the SQL {@code LENGTH(string)} function.
     *
     * @param text the string to get the length of
     * @return an expression representing the length of the string
     */
    public static Expression<Integer> length(@NonNull String text) {
        return length(constant(text));
    }

    /**
     * Pads the string on the left to the specified length with the fill string.
     *
     * @param string the source string expression
     * @param length the target length
     * @param fill   the padding string
     * @return padded string expression
     */
    public static Expression<String> lpad(@NonNull Expression<String> string, @NonNull Expression<Integer> length, @NonNull Expression<String> fill) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.LPAD, List.of(string, length, fill), String.class);
    }

    /**
     * Pads the string on the left to the specified length with the fill string.
     *
     * @param string the source string expression
     * @param length the target length
     * @param fill   the padding string
     * @return padded string expression
     */
    public static Expression<String> lpad(@NonNull Expression<String> string, int length, @NonNull String fill) {
        return lpad(string, constant(length), constant(fill));
    }

    /**
     * Pads the string on the left to the specified length with the fill string.
     *
     * @param string the source string expression
     * @param length the target length
     * @param fill   the padding string expression
     * @return padded string expression
     */
    public static Expression<String> lpad(@NonNull Expression<String> string, int length, @NonNull Expression<String> fill) {
        return lpad(string, constant(length), fill);
    }

    /**
     * Pads the string on the left to the specified length with the fill string.
     *
     * @param string the source string
     * @param length the target length
     * @param fill   the padding string
     * @return padded string expression
     */
    public static Expression<String> lpad(@NonNull String string, int length, @NonNull String fill) {
        return lpad(constant(string), constant(length), constant(fill));
    }

    /**
     * Aggregates multiple expressions into a multiset (array of records).
     * <p>
     * This produces a nested collection of records, useful for collecting
     * related rows into a single result column.
     *
     * @param expressions the expressions to include in each record of the multiset
     * @return an expression representing the aggregated multiset
     */
    public static Expression<Record[]> multisetAgg(@NonNull List<Expression<?>> expressions) {
        return new MultisetAggExpression(expressions);
    }

    /**
     * Replaces all occurrences of a substring with another substring.
     * <p>
     * This is equivalent to the SQL {@code REPLACE(text, from, to)} function.
     *
     * @param text the string to search in
     * @param from the substring to search for
     * @param to   the replacement substring
     * @return an expression representing the string with replacements
     */
    public static Expression<String> replace(@NonNull String text, @NonNull String from, @NonNull String to) {
        return replace(constant(text), constant(from), constant(to));
    }

    /**
     * Replaces all occurrences of a substring with another substring.
     * <p>
     * This is equivalent to the SQL {@code REPLACE(text, from, to)} function.
     *
     * @param text the string expression to search in
     * @param from the substring expression to search for
     * @param to   the replacement substring expression
     * @return an expression representing the string with replacements
     */
    public static Expression<String> replace(@NonNull Expression<String> text, @NonNull Expression<String> from, @NonNull Expression<String> to) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.REPLACE, List.of(text, from, to), String.class);
    }

    /**
     * Replaces all occurrences of a substring with another substring.
     * <p>
     * This is equivalent to the SQL {@code REPLACE(text, from, to)} function.
     *
     * @param text the string expression to search in
     * @param from the substring to search for
     * @param to   the replacement substring expression
     * @return an expression representing the string with replacements
     */
    public static Expression<String> replace(@NonNull Expression<String> text, @NonNull String from, @NonNull Expression<String> to) {
        return replace(text, constant(from), to);
    }

    /**
     * Replaces all occurrences of a substring with another substring.
     * <p>
     * This is equivalent to the SQL {@code REPLACE(text, from, to)} function.
     *
     * @param text the string expression to search in
     * @param from the substring expression to search for
     * @param to   the replacement substring
     * @return an expression representing the string with replacements
     */
    public static Expression<String> replace(@NonNull Expression<String> text, @NonNull Expression<String> from, @NonNull String to) {
        return replace(text, from, constant(to));
    }

    /**
     * Replaces all matches of a regular expression pattern with the replacement string.
     * <p>
     * This is equivalent to the SQL {@code REGEXP_REPLACE(text, pattern, replacement)} function,
     * configured to replace all occurrences (PostgreSQL receives the {@code 'g'} flag automatically).
     *
     * @param text        the string to search in
     * @param pattern     the regular expression pattern
     * @param replacement the replacement substring
     * @return an expression representing the string with all matches replaced
     */
    public static Expression<String> regexpReplaceAll(@NonNull String text, @NonNull String pattern, @NonNull String replacement) {
        return regexpReplaceAll(constant(text), constant(pattern), constant(replacement));
    }

    /**
     * Replaces all matches of a regular expression pattern with the replacement string.
     * <p>
     * This is equivalent to the SQL {@code REGEXP_REPLACE(text, pattern, replacement)} function,
     * configured to replace all occurrences (PostgreSQL receives the {@code 'g'} flag automatically).
     *
     * @param text        the string expression to search in
     * @param pattern     the regular expression pattern expression
     * @param replacement the replacement substring expression
     * @return an expression representing the string with all matches replaced
     */
    public static Expression<String> regexpReplaceAll(@NonNull Expression<String> text, @NonNull Expression<String> pattern, @NonNull Expression<String> replacement) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.REGEXP_REPLACE, List.of(text, pattern, replacement), String.class);
    }

    /**
     * Replaces all matches of a regular expression pattern with the replacement string.
     * <p>
     * This is equivalent to the SQL {@code REGEXP_REPLACE(text, pattern, replacement)} function,
     * configured to replace all occurrences (PostgreSQL receives the {@code 'g'} flag automatically).
     *
     * @param text        the string expression to search in
     * @param pattern     the regular expression pattern
     * @param replacement the replacement substring expression
     * @return an expression representing the string with all matches replaced
     */
    public static Expression<String> regexpReplaceAll(@NonNull Expression<String> text, @NonNull String pattern, @NonNull Expression<String> replacement) {
        return regexpReplaceAll(text, constant(pattern), replacement);
    }

    /**
     * Replaces all matches of a regular expression pattern with the replacement string.
     * <p>
     * This is equivalent to the SQL {@code REGEXP_REPLACE(text, pattern, replacement)} function,
     * configured to replace all occurrences (PostgreSQL receives the {@code 'g'} flag automatically).
     *
     * @param text        the string expression to search in
     * @param pattern     the regular expression pattern expression
     * @param replacement the replacement substring
     * @return an expression representing the string with all matches replaced
     */
    public static Expression<String> regexpReplaceAll(@NonNull Expression<String> text, @NonNull Expression<String> pattern, @NonNull String replacement) {
        return regexpReplaceAll(text, pattern, constant(replacement));
    }

    /**
     * Pads the string on the right to the specified length with the fill string.
     *
     * @param string the source string expression
     * @param length the target length
     * @param fill   the padding string
     * @return padded string expression
     */
    public static Expression<String> rpad(@NonNull Expression<String> string, @NonNull Expression<Integer> length, @NonNull Expression<String> fill) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.RPAD, List.of(string, length, fill), String.class);
    }

    /**
     * Pads the string on the right to the specified length with the fill string.
     *
     * @param string the source string expression
     * @param length the target length
     * @param fill   the padding string
     * @return padded string expression
     */
    public static Expression<String> rpad(@NonNull Expression<String> string, int length, @NonNull String fill) {
        return rpad(string, constant(length), constant(fill));
    }

    /**
     * Pads the string on the right to the specified length with the fill string.
     *
     * @param string the source string expression
     * @param length the target length
     * @param fill   the padding string expression
     * @return padded string expression
     */
    public static Expression<String> rpad(@NonNull Expression<String> string, int length, @NonNull Expression<String> fill) {
        return rpad(string, constant(length), fill);
    }

    /**
     * Pads the string on the right to the specified length with the fill string.
     *
     * @param string the source string
     * @param length the target length
     * @param fill   the padding string
     * @return padded string expression
     */
    public static Expression<String> rpad(@NonNull String string, int length, @NonNull String fill) {
        return rpad(constant(string), constant(length), constant(fill));
    }

    /**
     * Returns the position of the first occurrence of substring in string (1-based).
     * Note: POSITION uses special SQL syntax "POSITION(substring IN string)" handled by dialect.
     *
     * @param substring the substring to find
     * @param string    the string to search in
     * @return position expression (0 if not found)
     */
    public static Expression<Integer> position(@NonNull Expression<String> substring, @NonNull Expression<String> string) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.POSITION, List.of(substring, string), Integer.class);
    }

    /**
     * Returns the position of the first occurrence of substring in string (1-based).
     *
     * @param substring the substring to find
     * @param string    the string expression to search in
     * @return position expression (0 if not found)
     */
    public static Expression<Integer> position(@NonNull String substring, @NonNull Expression<String> string) {
        return position(constant(substring), string);
    }

    /**
     * Returns the position of the first occurrence of substring in string (1-based).
     *
     * @param substring the substring to find
     * @param string    the string to search in
     * @return position expression (0 if not found)
     */
    public static Expression<Integer> position(@NonNull String substring, @NonNull String string) {
        return position(constant(substring), constant(string));
    }

    /**
     * Repeats the string the specified number of times.
     *
     * @param string the string expression to repeat
     * @param count  the number of repetitions
     * @return repeated string expression
     */
    public static Expression<String> repeat(@NonNull Expression<String> string, @NonNull Expression<Integer> count) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.REPEAT, List.of(string, count), String.class);
    }

    /**
     * Repeats the string the specified number of times.
     *
     * @param string the string expression to repeat
     * @param count  the number of repetitions
     * @return repeated string expression
     */
    public static Expression<String> repeat(@NonNull Expression<String> string, int count) {
        return repeat(string, constant(count));
    }

    /**
     * Repeats the string the specified number of times.
     *
     * @param string the string to repeat
     * @param count  the number of repetitions
     * @return repeated string expression
     */
    public static Expression<String> repeat(@NonNull String string, int count) {
        return repeat(constant(string), constant(count));
    }

    /**
     * Reverses the string.
     *
     * @param string the string expression to reverse
     * @return reversed string expression
     */
    public static Expression<String> reverse(@NonNull Expression<String> string) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.REVERSE, List.of(string), String.class);
    }

    /**
     * Returns a string consisting of the given number of space characters.
     *
     * @param count the number of spaces
     * @return space-string expression
     */
    public static Expression<String> space(@NonNull Expression<Integer> count) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.SPACE, List.of(count), String.class);
    }

    /**
     * Returns a string consisting of the given number of space characters.
     *
     * @param count the number of spaces
     * @return space-string expression
     */
    public static Expression<String> space(int count) {
        return space(constant(count));
    }

    /**
     * Computes the MD5 hash of the string and returns it as a 32-character lowercase
     * hexadecimal string.
     * <p>
     * Renders as {@code md5(string)} on PostgreSQL and MariaDB. Not supported on H2 —
     * throws {@link UnsupportedOperationException} at query render time.
     *
     * @param string the string expression to hash
     * @return MD5 hash expression
     */
    public static Expression<String> md5(@NonNull Expression<String> string) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.MD5, List.of(string), String.class);
    }

    /**
     * Computes the MD5 hash of the string and returns it as a 32-character lowercase
     * hexadecimal string.
     * <p>
     * Renders as {@code md5(string)} on PostgreSQL and MariaDB. Not supported on H2 —
     * throws {@link UnsupportedOperationException} at query render time.
     *
     * @param string the string to hash
     * @return MD5 hash expression
     */
    public static Expression<String> md5(@NonNull String string) {
        return md5(constant(string));
    }

    /**
     * Reverses the string.
     *
     * @param string the string to reverse
     * @return reversed string expression
     */
    public static Expression<String> reverse(@NonNull String string) {
        return reverse(constant(string));
    }

    /**
     * Returns the value of a raised to the power of b.
     * <p>
     * This is equivalent to the SQL {@code POWER(a, b)} function.
     *
     * @param <T> the numeric type
     * @param a   the base expression
     * @param b   the exponent expression
     * @return an expression representing a raised to the power of b
     */
    public static <T extends Number> Expression<BigDecimal> power(@NonNull Expression<T> a, @NonNull Expression<T> b) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.POWER, List.of(a, b), BigDecimal.class);
    }

    /**
     * Returns the value of a raised to the power of b.
     * <p>
     * This is equivalent to the SQL {@code POWER(a, b)} function.
     *
     * @param <T> the numeric type
     * @param a   the base value
     * @param b   the exponent expression
     * @return an expression representing a raised to the power of b
     */
    public static <T extends Number> Expression<BigDecimal> power(@NonNull T a, @NonNull Expression<T> b) {
        return power(constant(b.getJavaType(), a), b);
    }

    /**
     * Returns the value of a raised to the power of b.
     * <p>
     * This is equivalent to the SQL {@code POWER(a, b)} function.
     *
     * @param <T> the numeric type
     * @param a   the base expression
     * @param b   the exponent value
     * @return an expression representing a raised to the power of b
     */
    public static <T extends Number> Expression<BigDecimal> power(@NonNull Expression<T> a, @NonNull T b) {
        return power(a, constant(a.getJavaType(), b));
    }

    /**
     * Returns the value of a raised to the power of b.
     * <p>
     * This is equivalent to the SQL {@code POWER(a, b)} function.
     *
     * @param <T> the numeric type
     * @param a   the base value
     * @param b   the exponent value
     * @return an expression representing a raised to the power of b
     */
    @SuppressWarnings("unchecked")
    public static <T extends Number> Expression<BigDecimal> power(@NonNull T a, @NonNull T b) {
        Class<T> type = (Class<T>) a.getClass();
        return power(constant(type, a), constant(type, b));
    }

    /**
     * Returns the current date and time with time zone.
     * <p>
     * Renders as the SQL {@code CURRENT_TIMESTAMP} keyword on H2, PostgreSQL, and MariaDB.
     *
     * @return an expression representing the current timestamp with time zone
     */
    public static Expression<OffsetDateTime> currentDateTime() {
        return currentTimestamp();
    }

    /**
     * Returns the current date according to the database server.
     * <p>
     * Renders as the SQL {@code CURRENT_DATE} keyword on H2 and PostgreSQL,
     * and as {@code current_date()} on MariaDB.
     *
     * @return an expression representing the current date
     */
    public static Expression<LocalDate> currentDate() {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.CURRENT_DATE, List.of(), LocalDate.class);
    }

    /**
     * Creates a SQL {@code DATE} literal expression from a date string.
     * <p>
     * Renders as the standard SQL date literal {@code DATE '<value>'}, which is
     * supported on H2, PostgreSQL, and MariaDB. The database is responsible for
     * validating the date format.
     *
     * @param dateStr the date literal value
     * @return an expression representing the SQL {@code DATE} literal
     */
    public static Expression<LocalDate> date(@NonNull String dateStr) {
        return new PortableFunctionExpression<>(
                PortableFunctionExpression.Function.DATE,
                List.of(constant(dateStr)),
                LocalDate.class
        );
    }

    /**
     * Adds an integer number of days to a temporal expression.
     * <p>
     * For {@link LocalDate}, renders as {@code (<date> + <days>)} on H2/PostgreSQL and
     * {@code date_add(<date>, INTERVAL <days> DAY)} on MariaDB.
     * For {@link LocalDateTime} / {@link OffsetDateTime}, renders as
     * {@code (<dt> + (<days> * interval '1 day'))} on PostgreSQL,
     * {@code DATEADD(DAY, <days>, <dt>)} on H2, and
     * {@code date_add(<dt>, INTERVAL <days> DAY)} on MariaDB.
     *
     * @param <T>  the temporal type ({@link LocalDate}, {@link LocalDateTime}, or {@link OffsetDateTime})
     * @param date the temporal expression
     * @param days the number of days to add (may be negative)
     * @return an expression representing the resulting temporal value
     */
    public static <T> Expression<T> dateAdd(@NonNull Expression<T> date, @NonNull Expression<Integer> days) {
        return new PortableFunctionExpression<>(
                PortableFunctionExpression.Function.DATE_ADD,
                List.of(date, days),
                date.getJavaType()
        );
    }

    /**
     * Adds an integer number of days to a temporal expression.
     *
     * @param <T>  the temporal type
     * @param date the temporal expression
     * @param days the constant number of days to add (may be negative)
     * @return an expression representing the resulting temporal value
     */
    public static <T> Expression<T> dateAdd(@NonNull Expression<T> date, @NonNull Integer days) {
        return dateAdd(date, constant(days));
    }

    /**
     * Adds an integer number of days to a date.
     *
     * @param date the constant date value
     * @param days the number of days to add (may be negative)
     * @return an expression representing the resulting date
     */
    public static Expression<LocalDate> dateAdd(@NonNull LocalDate date, @NonNull Expression<Integer> days) {
        return dateAdd(constant(date), days);
    }

    /**
     * Adds an integer number of days to a date.
     *
     * @param date the constant date value
     * @param days the constant number of days to add (may be negative)
     * @return an expression representing the resulting date
     */
    public static Expression<LocalDate> dateAdd(@NonNull LocalDate date, @NonNull Integer days) {
        return dateAdd(constant(date), constant(days));
    }

    /**
     * Adds an {@link Interval} (year/month/week/day/hour/minute/second/millisecond/microsecond) to a temporal expression.
     * <p>
     * Renders as {@code (<date> + INTERVAL '<v>' <UNIT>)} on H2, {@code (<date> + interval '<v> <unit>')} on PostgreSQL,
     * and {@code date_add(<date>, INTERVAL <v> <UNIT>)} on MariaDB.
     * <p>
     * The return type tracks the input. Pick a temporal type whose precision matches the interval grain
     * (e.g. {@code LocalDateTime} when adding sub-day intervals) — otherwise the database returns a wider
     * type than the declared {@code T} and JDBC mapping will fail.
     *
     * @param <T>      the temporal type
     * @param date     the temporal expression to shift
     * @param interval the interval to add
     * @return an expression of the same temporal type representing the shifted value
     */
    public static <T> Expression<T> dateAdd(@NonNull Expression<T> date, @NonNull Interval interval) {
        return new PortableFunctionExpression<>(
                PortableFunctionExpression.Function.DATE_ADD_INTERVAL,
                List.of(date, new IntervalConstant(interval)),
                date.getJavaType()
        );
    }

    /**
     * Adds an integer number of days to a constant {@link LocalDateTime}.
     *
     * @param dt   the constant date-time value
     * @param days the number of days to add (may be negative)
     * @return an expression representing the resulting date-time
     */
    public static Expression<LocalDateTime> dateAdd(@NonNull LocalDateTime dt, @NonNull Expression<Integer> days) {
        return dateAdd(constant(dt), days);
    }

    /**
     * Adds an integer number of days to a constant {@link LocalDateTime}.
     *
     * @param dt   the constant date-time value
     * @param days the constant number of days to add (may be negative)
     * @return an expression representing the resulting date-time
     */
    public static Expression<LocalDateTime> dateAdd(@NonNull LocalDateTime dt, @NonNull Integer days) {
        return dateAdd(constant(dt), constant(days));
    }

    /**
     * Adds an {@link Interval} to a constant {@link LocalDateTime}.
     *
     * @param dt       the constant date-time value
     * @param interval the interval to add
     * @return an expression representing the shifted date-time
     */
    public static Expression<LocalDateTime> dateAdd(@NonNull LocalDateTime dt, @NonNull Interval interval) {
        return dateAdd(constant(dt), interval);
    }

    /**
     * Adds an integer number of days to a constant {@link OffsetDateTime}.
     *
     * @param dt   the constant date-time value
     * @param days the number of days to add (may be negative)
     * @return an expression representing the resulting date-time
     */
    public static Expression<OffsetDateTime> dateAdd(@NonNull OffsetDateTime dt, @NonNull Expression<Integer> days) {
        return dateAdd(constant(dt), days);
    }

    /**
     * Adds an integer number of days to a constant {@link OffsetDateTime}.
     *
     * @param dt   the constant date-time value
     * @param days the constant number of days to add (may be negative)
     * @return an expression representing the resulting date-time
     */
    public static Expression<OffsetDateTime> dateAdd(@NonNull OffsetDateTime dt, @NonNull Integer days) {
        return dateAdd(constant(dt), constant(days));
    }

    /**
     * Adds an {@link Interval} to a constant {@link OffsetDateTime}.
     *
     * @param dt       the constant date-time value
     * @param interval the interval to add
     * @return an expression representing the shifted date-time
     */
    public static Expression<OffsetDateTime> dateAdd(@NonNull OffsetDateTime dt, @NonNull Interval interval) {
        return dateAdd(constant(dt), interval);
    }

    /**
     * Subtracts an integer number of days from a temporal expression.
     * <p>
     * For {@link LocalDate}, renders as {@code (<date> - <days>)} on H2,
     * {@code (<date> + -<days>)} on PostgreSQL, and
     * {@code date_add(<date>, INTERVAL -<days> DAY)} on MariaDB.
     * For {@link LocalDateTime} / {@link OffsetDateTime}, renders as
     * {@code (<dt> - (<days> * interval '1 day'))} on PostgreSQL,
     * {@code DATEADD(DAY, -<days>, <dt>)} on H2, and
     * {@code date_add(<dt>, INTERVAL -<days> DAY)} on MariaDB.
     *
     * @param <T>  the temporal type ({@link LocalDate}, {@link LocalDateTime}, or {@link OffsetDateTime})
     * @param date the temporal expression
     * @param days the number of days to subtract (may be negative)
     * @return an expression representing the resulting temporal value
     */
    public static <T> Expression<T> dateSub(@NonNull Expression<T> date, @NonNull Expression<Integer> days) {
        return new PortableFunctionExpression<>(
                PortableFunctionExpression.Function.DATE_SUB,
                List.of(date, days),
                date.getJavaType()
        );
    }

    /**
     * Subtracts an integer number of days from a temporal expression.
     *
     * @param <T>  the temporal type
     * @param date the temporal expression
     * @param days the constant number of days to subtract (may be negative)
     * @return an expression representing the resulting temporal value
     */
    public static <T> Expression<T> dateSub(@NonNull Expression<T> date, @NonNull Integer days) {
        return dateSub(date, constant(days));
    }

    /**
     * Subtracts an integer number of days from a date.
     *
     * @param date the constant date value
     * @param days the number of days to subtract (may be negative)
     * @return an expression representing the resulting date
     */
    public static Expression<LocalDate> dateSub(@NonNull LocalDate date, @NonNull Expression<Integer> days) {
        return dateSub(constant(date), days);
    }

    /**
     * Subtracts an integer number of days from a date.
     *
     * @param date the constant date value
     * @param days the constant number of days to subtract (may be negative)
     * @return an expression representing the resulting date
     */
    public static Expression<LocalDate> dateSub(@NonNull LocalDate date, @NonNull Integer days) {
        return dateSub(constant(date), constant(days));
    }

    /**
     * Subtracts an {@link Interval} (year/month/week/day/hour/minute/second/millisecond/microsecond) from a temporal expression.
     * <p>
     * Renders as {@code (<date> - INTERVAL '<v>' <UNIT>)} on H2, {@code (<date> + interval '-<v> <unit>')} on PostgreSQL,
     * and {@code date_add(<date>, INTERVAL -<v> <UNIT>)} on MariaDB.
     * <p>
     * The return type tracks the input. Pick a temporal type whose precision matches the interval grain
     * (e.g. {@code LocalDateTime} when subtracting sub-day intervals) — otherwise the database returns a wider
     * type than the declared {@code T} and JDBC mapping will fail.
     *
     * @param <T>      the temporal type
     * @param date     the temporal expression to shift
     * @param interval the interval to subtract
     * @return an expression of the same temporal type representing the shifted value
     */
    public static <T> Expression<T> dateSub(@NonNull Expression<T> date, @NonNull Interval interval) {
        return new PortableFunctionExpression<>(
                PortableFunctionExpression.Function.DATE_SUB_INTERVAL,
                List.of(date, new IntervalConstant(interval)),
                date.getJavaType()
        );
    }

    /**
     * Subtracts an integer number of days from a constant {@link LocalDateTime}.
     *
     * @param dt   the constant date-time value
     * @param days the number of days to subtract (may be negative)
     * @return an expression representing the resulting date-time
     */
    public static Expression<LocalDateTime> dateSub(@NonNull LocalDateTime dt, @NonNull Expression<Integer> days) {
        return dateSub(constant(dt), days);
    }

    /**
     * Subtracts an integer number of days from a constant {@link LocalDateTime}.
     *
     * @param dt   the constant date-time value
     * @param days the constant number of days to subtract (may be negative)
     * @return an expression representing the resulting date-time
     */
    public static Expression<LocalDateTime> dateSub(@NonNull LocalDateTime dt, @NonNull Integer days) {
        return dateSub(constant(dt), constant(days));
    }

    /**
     * Subtracts an {@link Interval} from a constant {@link LocalDateTime}.
     *
     * @param dt       the constant date-time value
     * @param interval the interval to subtract
     * @return an expression representing the shifted date-time
     */
    public static Expression<LocalDateTime> dateSub(@NonNull LocalDateTime dt, @NonNull Interval interval) {
        return dateSub(constant(dt), interval);
    }

    /**
     * Subtracts an integer number of days from a constant {@link OffsetDateTime}.
     *
     * @param dt   the constant date-time value
     * @param days the number of days to subtract (may be negative)
     * @return an expression representing the resulting date-time
     */
    public static Expression<OffsetDateTime> dateSub(@NonNull OffsetDateTime dt, @NonNull Expression<Integer> days) {
        return dateSub(constant(dt), days);
    }

    /**
     * Subtracts an integer number of days from a constant {@link OffsetDateTime}.
     *
     * @param dt   the constant date-time value
     * @param days the constant number of days to subtract (may be negative)
     * @return an expression representing the resulting date-time
     */
    public static Expression<OffsetDateTime> dateSub(@NonNull OffsetDateTime dt, @NonNull Integer days) {
        return dateSub(constant(dt), constant(days));
    }

    /**
     * Subtracts an {@link Interval} from a constant {@link OffsetDateTime}.
     *
     * @param dt       the constant date-time value
     * @param interval the interval to subtract
     * @return an expression representing the shifted date-time
     */
    public static Expression<OffsetDateTime> dateSub(@NonNull OffsetDateTime dt, @NonNull Interval interval) {
        return dateSub(constant(dt), interval);
    }

    /**
     * Returns the difference in days between two dates ({@code date1 - date2}).
     * <p>
     * Renders as {@code datediff(DAY, <date2>, <date1>)} on H2,
     * as {@code (<date1> - <date2>)} on PostgreSQL,
     * and as {@code datediff(<date1>, <date2>)} on MariaDB.
     *
     * @param date1 the later date expression (minuend)
     * @param date2 the earlier date expression (subtrahend)
     * @return an expression representing the number of days between the two dates
     */
    public static Expression<Integer> dateDiff(@NonNull Expression<LocalDate> date1, @NonNull Expression<LocalDate> date2) {
        return new PortableFunctionExpression<>(
                PortableFunctionExpression.Function.DATE_DIFF,
                List.of(date1, date2),
                Integer.class
        );
    }

    /**
     * Returns the difference in days between two dates ({@code date1 - date2}).
     *
     * @param date1 the later date expression (minuend)
     * @param date2 the earlier constant date value (subtrahend)
     * @return an expression representing the number of days between the two dates
     */
    public static Expression<Integer> dateDiff(@NonNull Expression<LocalDate> date1, @NonNull LocalDate date2) {
        return dateDiff(date1, constant(date2));
    }

    /**
     * Returns the difference in days between two dates ({@code date1 - date2}).
     *
     * @param date1 the later constant date value (minuend)
     * @param date2 the earlier date expression (subtrahend)
     * @return an expression representing the number of days between the two dates
     */
    public static Expression<Integer> dateDiff(@NonNull LocalDate date1, @NonNull Expression<LocalDate> date2) {
        return dateDiff(constant(date1), date2);
    }

    /**
     * Returns the difference in days between two dates ({@code date1 - date2}).
     *
     * @param date1 the later constant date value (minuend)
     * @param date2 the earlier constant date value (subtrahend)
     * @return an expression representing the number of days between the two dates
     */
    public static Expression<Integer> dateDiff(@NonNull LocalDate date1, @NonNull LocalDate date2) {
        return dateDiff(constant(date1), constant(date2));
    }

    /**
     * Returns the current local time (without time zone) according to the database server.
     * <p>
     * Renders as the SQL {@code LOCALTIME} keyword on H2,
     * as {@code cast(CURRENT_TIME AS time)} on PostgreSQL (to drop the time zone),
     * and as {@code current_time()} on MariaDB.
     *
     * @return an expression representing the current local time
     */
    public static Expression<LocalTime> currentTime() {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.CURRENT_TIME, List.of(), LocalTime.class);
    }

    public static Expression<OffsetDateTime> currentTimestamp() {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.CURRENT_TIMESTAMP, List.of(), OffsetDateTime.class);
    }

    /**
     * Returns the current local date-time (without time zone) according to the database server.
     * <p>
     * Renders as the SQL {@code LOCALTIMESTAMP} keyword on H2, MariaDB, and PostgreSQL.
     *
     * @return an expression representing the current local date-time
     */
    public static Expression<LocalDateTime> currentLocalDateTime() {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.CURRENT_LOCALDATETIME, List.of(), LocalDateTime.class);
    }

    // ==================== DATE/TIME FUNCTIONS ====================

    /**
     * Extracts a date/time part from a temporal expression.
     * Note: EXTRACT uses special SQL syntax "EXTRACT(part FROM source)" handled by dialect.
     *
     * @param part   the date/time part to extract
     * @param source the source temporal expression
     * @return the extracted value as integer
     */
    public static Expression<Integer> extract(@NonNull DateTimePart part, @NonNull Expression<?> source) {
        return new FunctionExpression<>("EXTRACT_" + part.name(), List.of(source), Integer.class);
    }

    /**
     * Extracts the year from a date.
     *
     * @param date the date expression
     * @return the year
     */
    public static Expression<Integer> year(@NonNull Expression<?> date) {
        return new PortableFunctionExpression<>(
                PortableFunctionExpression.Function.EXTRACT_YEAR,
                List.of(date),
                Integer.class
        );
    }

    /**
     * Extracts the year from a temporal expression.
     * <p>
     * Accepts {@link Expression} of {@link LocalDate}, {@link LocalDateTime}, or {@link OffsetDateTime}.
     * Renders as {@code extract(YEAR FROM <date>)} on all supported dialects.
     *
     * @param <T>  the temporal type ({@link LocalDate}, {@link LocalDateTime}, or {@link OffsetDateTime})
     * @param date the date or date-time expression
     * @return the year
     */
    public static <T> Expression<Integer> extractYear(@NonNull Expression<T> date) {
        return year(date);
    }

    /**
     * Extracts the year from a constant {@link LocalDate}.
     *
     * @param date the constant date value
     * @return the year
     */
    public static Expression<Integer> extractYear(@NonNull LocalDate date) {
        return year(constant(date));
    }

    /**
     * Extracts the year from a constant {@link LocalDateTime}.
     *
     * @param date the constant date-time value
     * @return the year
     */
    public static Expression<Integer> extractYear(@NonNull LocalDateTime date) {
        return year(constant(date));
    }

    /**
     * Extracts the year from a constant {@link OffsetDateTime}.
     *
     * @param date the constant date-time value
     * @return the year
     */
    public static Expression<Integer> extractYear(@NonNull OffsetDateTime date) {
        return year(constant(date));
    }

    /**
     * Extracts the month from a date (1-12).
     *
     * @param date the date expression
     * @return the month
     */
    public static Expression<Integer> month(@NonNull Expression<?> date) {
        return new PortableFunctionExpression<>(
                PortableFunctionExpression.Function.EXTRACT_MONTH,
                List.of(date),
                Integer.class
        );
    }

    /**
     * Extracts the month from a temporal expression (1-12).
     * <p>
     * Accepts {@link Expression} of {@link LocalDate}, {@link LocalDateTime}, or {@link OffsetDateTime}.
     * Renders as {@code extract(MONTH FROM <date>)} on all supported dialects.
     *
     * @param <T>  the temporal type ({@link LocalDate}, {@link LocalDateTime}, or {@link OffsetDateTime})
     * @param date the date or date-time expression
     * @return the month
     */
    public static <T> Expression<Integer> extractMonth(@NonNull Expression<T> date) {
        return month(date);
    }

    /**
     * Extracts the month from a constant {@link LocalDate} (1-12).
     *
     * @param date the constant date value
     * @return the month
     */
    public static Expression<Integer> extractMonth(@NonNull LocalDate date) {
        return month(constant(date));
    }

    /**
     * Extracts the month from a constant {@link LocalDateTime} (1-12).
     *
     * @param date the constant date-time value
     * @return the month
     */
    public static Expression<Integer> extractMonth(@NonNull LocalDateTime date) {
        return month(constant(date));
    }

    /**
     * Extracts the month from a constant {@link OffsetDateTime} (1-12).
     *
     * @param date the constant date-time value
     * @return the month
     */
    public static Expression<Integer> extractMonth(@NonNull OffsetDateTime date) {
        return month(constant(date));
    }

    /**
     * Extracts the quarter of the year from a date (1-4).
     *
     * @param date the date expression
     * @return the quarter
     */
    public static Expression<Integer> quarter(@NonNull Expression<?> date) {
        return new PortableFunctionExpression<>(
                PortableFunctionExpression.Function.EXTRACT_QUARTER,
                List.of(date),
                Integer.class
        );
    }

    /**
     * Extracts the quarter of the year from a temporal expression (1-4).
     * <p>
     * Accepts {@link Expression} of {@link LocalDate}, {@link LocalDateTime}, or {@link OffsetDateTime}.
     * Renders as {@code extract(QUARTER FROM <date>)} on PostgreSQL/H2 and as {@code quarter(<date>)} on MariaDB.
     *
     * @param <T>  the temporal type ({@link LocalDate}, {@link LocalDateTime}, or {@link OffsetDateTime})
     * @param date the date or date-time expression
     * @return the quarter
     */
    public static <T> Expression<Integer> extractQuarter(@NonNull Expression<T> date) {
        return quarter(date);
    }

    /**
     * Extracts the quarter of the year from a constant {@link LocalDate} (1-4).
     *
     * @param date the constant date value
     * @return the quarter
     */
    public static Expression<Integer> extractQuarter(@NonNull LocalDate date) {
        return quarter(constant(date));
    }

    /**
     * Extracts the quarter of the year from a constant {@link LocalDateTime} (1-4).
     *
     * @param date the constant date-time value
     * @return the quarter
     */
    public static Expression<Integer> extractQuarter(@NonNull LocalDateTime date) {
        return quarter(constant(date));
    }

    /**
     * Extracts the quarter of the year from a constant {@link OffsetDateTime} (1-4).
     *
     * @param date the constant date-time value
     * @return the quarter
     */
    public static Expression<Integer> extractQuarter(@NonNull OffsetDateTime date) {
        return quarter(constant(date));
    }

    /**
     * Extracts the day of month from a date (1-31).
     *
     * @param date the date expression
     * @return the day
     */
    public static Expression<Integer> day(@NonNull Expression<?> date) {
        return new PortableFunctionExpression<>(
                PortableFunctionExpression.Function.EXTRACT_DAY,
                List.of(date),
                Integer.class
        );
    }

    /**
     * Extracts the day of month from a temporal expression (1-31).
     * <p>
     * Accepts {@link Expression} of {@link LocalDate}, {@link LocalDateTime}, or {@link OffsetDateTime}.
     * Renders as {@code extract(DAY FROM <date>)} on all supported dialects.
     *
     * @param <T>  the temporal type ({@link LocalDate}, {@link LocalDateTime}, or {@link OffsetDateTime})
     * @param date the date or date-time expression
     * @return the day of month
     */
    public static <T> Expression<Integer> extractDay(@NonNull Expression<T> date) {
        return day(date);
    }

    /**
     * Extracts the day of month from a constant {@link LocalDate} (1-31).
     *
     * @param date the constant date value
     * @return the day
     */
    public static Expression<Integer> extractDay(@NonNull LocalDate date) {
        return day(constant(date));
    }

    /**
     * Extracts the day of month from a constant {@link LocalDateTime} (1-31).
     *
     * @param date the constant date-time value
     * @return the day
     */
    public static Expression<Integer> extractDay(@NonNull LocalDateTime date) {
        return day(constant(date));
    }

    /**
     * Extracts the day of month from a constant {@link OffsetDateTime} (1-31).
     *
     * @param date the constant date-time value
     * @return the day
     */
    public static Expression<Integer> extractDay(@NonNull OffsetDateTime date) {
        return day(constant(date));
    }

    /**
     * Extracts the day of year from a date (1-366).
     *
     * @param date the date expression
     * @return the day of year
     */
    public static Expression<Integer> dayOfYear(@NonNull Expression<?> date) {
        return new PortableFunctionExpression<>(
                PortableFunctionExpression.Function.EXTRACT_DAY_OF_YEAR,
                List.of(date),
                Integer.class
        );
    }

    /**
     * Extracts the day of year from a temporal expression (1-366).
     * <p>
     * Accepts {@link Expression} of {@link LocalDate}, {@link LocalDateTime}, or {@link OffsetDateTime}.
     *
     * @param <T>  the temporal type ({@link LocalDate}, {@link LocalDateTime}, or {@link OffsetDateTime})
     * @param date the date or date-time expression
     * @return the day of year
     */
    public static <T> Expression<Integer> extractDayOfYear(@NonNull Expression<T> date) {
        return dayOfYear(date);
    }

    /**
     * Extracts the day of year from a constant {@link LocalDate} (1-366).
     *
     * @param date the constant date value
     * @return the day of year
     */
    public static Expression<Integer> extractDayOfYear(@NonNull LocalDate date) {
        return dayOfYear(constant(date));
    }

    /**
     * Extracts the day of year from a constant {@link LocalDateTime} (1-366).
     *
     * @param date the constant date-time value
     * @return the day of year
     */
    public static Expression<Integer> extractDayOfYear(@NonNull LocalDateTime date) {
        return dayOfYear(constant(date));
    }

    /**
     * Extracts the day of year from a constant {@link OffsetDateTime} (1-366).
     *
     * @param date the constant date-time value
     * @return the day of year
     */
    public static Expression<Integer> extractDayOfYear(@NonNull OffsetDateTime date) {
        return dayOfYear(constant(date));
    }

    /**
     * Extracts the ISO day of week from a temporal expression (1-7, Monday-Sunday).
     * <p>
     * Accepts {@link Expression} of {@link LocalDate}, {@link LocalDateTime}, or {@link OffsetDateTime}.
     *
     * @param <T>  the temporal type ({@link LocalDate}, {@link LocalDateTime}, or {@link OffsetDateTime})
     * @param date the date or date-time expression
     * @return the ISO day of week
     */
    public static <T> Expression<Integer> extractIsoDayOfWeek(@NonNull Expression<T> date) {
        return new PortableFunctionExpression<>(
                PortableFunctionExpression.Function.EXTRACT_ISO_DAY_OF_WEEK,
                List.of(date),
                Integer.class
        );
    }

    /**
     * Extracts the ISO day of week from a constant {@link LocalDate} (1-7, Monday-Sunday).
     *
     * @param date the constant date value
     * @return the ISO day of week
     */
    public static Expression<Integer> extractIsoDayOfWeek(@NonNull LocalDate date) {
        return extractIsoDayOfWeek(constant(date));
    }

    /**
     * Extracts the ISO day of week from a constant {@link LocalDateTime} (1-7, Monday-Sunday).
     *
     * @param date the constant date-time value
     * @return the ISO day of week
     */
    public static Expression<Integer> extractIsoDayOfWeek(@NonNull LocalDateTime date) {
        return extractIsoDayOfWeek(constant(date));
    }

    /**
     * Extracts the ISO day of week from a constant {@link OffsetDateTime} (1-7, Monday-Sunday).
     *
     * @param date the constant date-time value
     * @return the ISO day of week
     */
    public static Expression<Integer> extractIsoDayOfWeek(@NonNull OffsetDateTime date) {
        return extractIsoDayOfWeek(constant(date));
    }

    /**
     * Extracts the hour from a time or timestamp (0-23).
     *
     * @param time the time expression
     * @return the hour
     */
    public static Expression<Integer> hour(@NonNull Expression<?> time) {
        return new PortableFunctionExpression<>(
                PortableFunctionExpression.Function.EXTRACT_HOUR,
                List.of(time),
                Integer.class
        );
    }

    /**
     * Extracts the hour from a temporal expression (0-23).
     * <p>
     * Accepts {@link Expression} of {@link LocalDateTime} or {@link OffsetDateTime}.
     * Renders as {@code extract(HOUR FROM <date>)} on all supported dialects.
     *
     * @param <T>  the temporal type ({@link LocalDateTime} or {@link OffsetDateTime})
     * @param date the date-time expression
     * @return the hour
     */
    public static <T> Expression<Integer> extractHour(@NonNull Expression<T> date) {
        return hour(date);
    }

    /**
     * Extracts the hour from a constant {@link LocalDateTime} (0-23).
     *
     * @param date the constant date-time value
     * @return the hour
     */
    public static Expression<Integer> extractHour(@NonNull LocalDateTime date) {
        return hour(constant(date));
    }

    /**
     * Extracts the hour from a constant {@link OffsetDateTime} (0-23).
     *
     * @param date the constant date-time value
     * @return the hour
     */
    public static Expression<Integer> extractHour(@NonNull OffsetDateTime date) {
        return hour(constant(date));
    }

    /**
     * Extracts the minute from a time or timestamp (0-59).
     *
     * @param time the time expression
     * @return the minute
     */
    public static Expression<Integer> minute(@NonNull Expression<?> time) {
        return new PortableFunctionExpression<>(
                PortableFunctionExpression.Function.EXTRACT_MINUTE,
                List.of(time),
                Integer.class
        );
    }

    /**
     * Extracts the minute from a temporal expression (0-59).
     * <p>
     * Accepts {@link Expression} of {@link LocalDateTime} or {@link OffsetDateTime}.
     * Renders as {@code extract(MINUTE FROM <date>)} on all supported dialects.
     *
     * @param <T>  the temporal type ({@link LocalDateTime} or {@link OffsetDateTime})
     * @param date the date-time expression
     * @return the minute
     */
    public static <T> Expression<Integer> extractMinute(@NonNull Expression<T> date) {
        return minute(date);
    }

    /**
     * Extracts the minute from a constant {@link LocalDateTime} (0-59).
     *
     * @param date the constant date-time value
     * @return the minute
     */
    public static Expression<Integer> extractMinute(@NonNull LocalDateTime date) {
        return minute(constant(date));
    }

    /**
     * Extracts the minute from a constant {@link OffsetDateTime} (0-59).
     *
     * @param date the constant date-time value
     * @return the minute
     */
    public static Expression<Integer> extractMinute(@NonNull OffsetDateTime date) {
        return minute(constant(date));
    }

    /**
     * Extracts the second from a time or timestamp (0-59).
     *
     * @param time the time expression
     * @return the second
     */
    public static Expression<Integer> second(@NonNull Expression<?> time) {
        return new PortableFunctionExpression<>(
                PortableFunctionExpression.Function.EXTRACT_SECOND,
                List.of(time),
                Integer.class
        );
    }

    /**
     * Extracts the second from a temporal expression (0-59).
     * <p>
     * Accepts {@link Expression} of {@link LocalDateTime} or {@link OffsetDateTime}.
     * Renders as {@code extract(SECOND FROM <date>)} on all supported dialects.
     *
     * @param <T>  the temporal type ({@link LocalDateTime} or {@link OffsetDateTime})
     * @param date the date-time expression
     * @return the second
     */
    public static <T> Expression<Integer> extractSecond(@NonNull Expression<T> date) {
        return second(date);
    }

    /**
     * Extracts the second from a constant {@link LocalDateTime} (0-59).
     *
     * @param date the constant date-time value
     * @return the second
     */
    public static Expression<Integer> extractSecond(@NonNull LocalDateTime date) {
        return second(constant(date));
    }

    /**
     * Extracts the second from a constant {@link OffsetDateTime} (0-59).
     *
     * @param date the constant date-time value
     * @return the second
     */
    public static Expression<Integer> extractSecond(@NonNull OffsetDateTime date) {
        return second(constant(date));
    }

    /**
     * Returns the Unix epoch (seconds since 1970-01-01T00:00:00Z) of a temporal expression.
     * <p>
     * Accepts {@link Expression} of {@link LocalDate}, {@link LocalDateTime}, or {@link OffsetDateTime}.
     * Renders as {@code extract(EPOCH FROM <date>)} on H2 and PostgreSQL, and as
     * {@code UNIX_TIMESTAMP(<date>)} on MariaDB.
     * <p>
     * On MariaDB, naive temporal values (no time zone) are interpreted in the session's time zone,
     * so the result of a {@link LocalDateTime} input depends on the database session time zone.
     * Use {@link OffsetDateTime} when you need a deterministic, time-zone-independent epoch.
     *
     * @param <T>  the temporal type ({@link LocalDate}, {@link LocalDateTime}, or {@link OffsetDateTime})
     * @param date the date or date-time expression
     * @return the epoch as seconds since 1970-01-01T00:00:00Z
     */
    public static <T> Expression<Long> epoch(@NonNull Expression<T> date) {
        return new PortableFunctionExpression<>(
                PortableFunctionExpression.Function.EPOCH,
                List.of(date),
                Long.class
        );
    }

    /**
     * Returns the Unix epoch (seconds since 1970-01-01T00:00:00Z) of a constant {@link LocalDate}.
     *
     * @param date the constant date value
     * @return the epoch as seconds since 1970-01-01T00:00:00Z
     */
    public static Expression<Long> epoch(@NonNull LocalDate date) {
        return epoch(constant(date));
    }

    /**
     * Returns the Unix epoch (seconds since 1970-01-01T00:00:00Z) of a constant {@link LocalDateTime}.
     *
     * @param date the constant date-time value
     * @return the epoch as seconds since 1970-01-01T00:00:00Z
     */
    public static Expression<Long> epoch(@NonNull LocalDateTime date) {
        return epoch(constant(date));
    }

    /**
     * Returns the Unix epoch (seconds since 1970-01-01T00:00:00Z) of a constant {@link OffsetDateTime}.
     *
     * @param date the constant date-time value
     * @return the epoch as seconds since 1970-01-01T00:00:00Z
     */
    public static Expression<Long> epoch(@NonNull OffsetDateTime date) {
        return epoch(constant(date));
    }

    /**
     * Truncates a timestamp to the specified precision.
     * This is equivalent to the SQL {@code DATE_TRUNC(precision, timestamp)} function.
     * <p>
     * Examples:
     * <ul>
     *   <li>{@code dateTrunc(DateTimePrecision.HOUR, timestamp)} - truncates to the start of the hour</li>
     *   <li>{@code dateTrunc(DateTimePrecision.DAY, timestamp)} - truncates to the start of the day</li>
     *   <li>{@code dateTrunc(DateTimePrecision.MONTH, timestamp)} - truncates to the start of the month</li>
     * </ul>
     *
     * @param precision the precision level for truncation (e.g., SECOND, MINUTE, HOUR, DAY, MONTH, YEAR)
     * @param source    the timestamp expression to truncate
     * @param <T>       the temporal type (typically Timestamp or LocalDateTime)
     * @return an expression representing the truncated timestamp
     */
    public static <T> Expression<T> dateTrunc(@NonNull DateTimePrecision precision, @NonNull Expression<T> source) {
        return switch (precision) {
            case YEAR -> new PortableFunctionExpression<>(PortableFunctionExpression.Function.DATE_TRUNC_YEAR, List.of(source), source.getJavaType());
            case QUARTER -> new PortableFunctionExpression<>(PortableFunctionExpression.Function.DATE_TRUNC_QUARTER, List.of(source), source.getJavaType());
            case MONTH -> new PortableFunctionExpression<>(PortableFunctionExpression.Function.DATE_TRUNC_MONTH, List.of(source), source.getJavaType());
            case WEEK -> new PortableFunctionExpression<>(PortableFunctionExpression.Function.DATE_TRUNC_WEEK, List.of(source), source.getJavaType());
            case DAY -> new PortableFunctionExpression<>(PortableFunctionExpression.Function.DATE_TRUNC_DAY, List.of(source), source.getJavaType());
            case HOUR -> new PortableFunctionExpression<>(PortableFunctionExpression.Function.DATE_TRUNC_HOUR, List.of(source), source.getJavaType());
            case MINUTE -> new PortableFunctionExpression<>(PortableFunctionExpression.Function.DATE_TRUNC_MINUTE, List.of(source), source.getJavaType());
            case SECOND -> new PortableFunctionExpression<>(PortableFunctionExpression.Function.DATE_TRUNC_SECOND, List.of(source), source.getJavaType());
            case MILLISECOND -> new PortableFunctionExpression<>(PortableFunctionExpression.Function.DATE_TRUNC_MILLISECOND, List.of(source), source.getJavaType());
            case MICROSECOND -> new PortableFunctionExpression<>(PortableFunctionExpression.Function.DATE_TRUNC_MICROSECOND, List.of(source), source.getJavaType());
        };
    }

    /**
     * Combines a list of conditions with logical AND.
     * <p>
     * Returns {@code TRUE} if the list is empty, the single condition if it contains one element,
     * or a compound AND condition otherwise.
     *
     * @param conditions the conditions to combine
     * @return a condition representing the logical AND of all conditions
     */
    public static Condition and(@NonNull List<Condition> conditions) {
        return switch (conditions.size()) {
            case 0 -> new BooleanConstantExpression(true);
            case 1 -> conditions.get(0);
            default -> {
                Condition result = new ComplexCondition(conditions.get(0), ComplexCondition.Operator.AND, conditions.get(1));
                for (int i = 2; i < conditions.size(); i++) {
                    result = result.and(conditions.get(i));
                }
                yield result;
            }
        };
    }

    /**
     * Combines multiple conditions with logical AND.
     *
     * @param conditions the conditions to combine
     * @return a condition representing the logical AND of all conditions
     */
    public static Condition and(@NonNull Condition... conditions) {
        return and(Arrays.asList(conditions));
    }

    /**
     * Combines two conditions with logical AND.
     *
     * @param condition1 the first condition
     * @param condition2 the second condition
     * @return a condition representing {@code condition1 AND condition2}
     */
    public static Condition and(@NonNull Condition condition1, @NonNull Condition condition2) {
        return and(List.of(condition1, condition2));
    }

    /**
     * Combines multiple conditions with logical OR.
     *
     * @param conditions the conditions to combine
     * @return a condition representing the logical OR of all conditions
     */
    public static Condition or(@NonNull Condition... conditions) {
        return or(Arrays.asList(conditions));
    }

    /**
     * Combines two conditions with logical OR.
     *
     * @param condition1 the first condition
     * @param condition2 the second condition
     * @return a condition representing {@code condition1 OR condition2}
     */
    public static Condition or(@NonNull Condition condition1, @NonNull Condition condition2) {
        return or(List.of(condition1, condition2));
    }

    /**
     * Combines a list of conditions with logical OR.
     * <p>
     * Returns {@code FALSE} if the list is empty, the single condition if it contains one element,
     * or a compound OR condition otherwise.
     *
     * @param conditions the conditions to combine
     * @return a condition representing the logical OR of all conditions
     */
    public static Condition or(@NonNull List<Condition> conditions) {
        return switch (conditions.size()) {
            case 0 -> new BooleanConstantExpression(false);
            case 1 -> conditions.get(0);
            default -> {
                Condition result = new ComplexCondition(conditions.get(0), ComplexCondition.Operator.OR, conditions.get(1));
                for (int i = 2; i < conditions.size(); i++) {
                    result = result.or(conditions.get(i));
                }
                yield result;
            }
        };
    }

    /**
     * Tests whether string {@code a} contains string {@code b}, ignoring case.
     * <p>
     * This generates a case-insensitive LIKE expression with proper escaping of {@code %} and {@code _} wildcards.
     *
     * @param a the string to search in
     * @param b the substring to search for
     * @return a condition that is true if {@code a} contains {@code b} (case-insensitive)
     */
    public static Condition containsIgnoreCase(@NonNull String a, @NonNull String b) {
        return containsIgnoreCase(constant(a), constant(b));
    }

    /**
     * Tests whether expression {@code a} contains expression {@code b}, ignoring case.
     * <p>
     * This generates a case-insensitive LIKE expression with proper escaping of {@code %} and {@code _} wildcards.
     *
     * @param a the expression to search in
     * @param b the expression representing the substring to search for
     * @return a condition that is true if {@code a} contains {@code b} (case-insensitive)
     */
    public static Condition containsIgnoreCase(@NonNull Expression<String> a, @NonNull Expression<String> b) {
        return new TernaryOperatorCondition(
                DSL.lower(a),
                DSL.concat(
                        DSL.constant("%"),
                        DSL.replace(
                                DSL.replace(DSL.lower(b), DSL.constant("%"), DSL.constant("\\%")),
                                DSL.constant("_"),
                                DSL.constant("\\_")
                        ),
                        DSL.constant("%")
                ),
                DSL.constant("\\"),
                TernaryOperatorCondition.Operator.LIKE_ESCAPE
        );
    }

    /**
     * Tests whether expression {@code a} contains string {@code b}, ignoring case.
     * <p>
     * This generates a case-insensitive LIKE expression with proper escaping of {@code %} and {@code _} wildcards.
     *
     * @param a the expression to search in
     * @param b the substring to search for
     * @return a condition that is true if {@code a} contains {@code b} (case-insensitive)
     */
    public static Condition containsIgnoreCase(@NonNull Expression<String> a, @NonNull String b) {
        return containsIgnoreCase(a, constant(b));
    }

    /**
     * Returns the number of null arguments.
     * <p>
     * This is equivalent to the SQL {@code NUM_NULLS(expression1, expression2, ...)} function.
     * For example, {@code num_nulls(1, NULL, 2)} returns 1.
     *
     * @param expressions the expressions to check for null values
     * @return an expression representing the count of null arguments
     */
    public static Expression<Integer> numNulls(@NonNull List<Expression<?>> expressions) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.NUM_NULLS, expressions, Integer.class);
    }

    /**
     * Returns the number of null arguments.
     * <p>
     * This is equivalent to the SQL {@code NUM_NULLS(expression1, expression2, ...)} function.
     * For example, {@code num_nulls(1, NULL, 2)} returns 1.
     *
     * @param expression1      the first expression to check for null values
     * @param otherExpressions additional expressions to check for null values
     * @return an expression representing the count of null arguments
     */
    public static Expression<Integer> numNulls(@NonNull Expression<?> expression1, @NonNull Expression<?>... otherExpressions) {
        List<Expression<?>> expressions = new ArrayList<>(otherExpressions.length + 1);
        expressions.add(expression1);
        expressions.addAll(Arrays.asList(otherExpressions));
        return numNulls(expressions);
    }

    /**
     * Returns the number of non-null arguments.
     * <p>
     * This is equivalent to the SQL {@code NUM_NONNULLS(expression1, expression2, ...)} function.
     * For example, {@code num_nonnulls(1, NULL, 2)} returns 2.
     *
     * @param expressions the expressions to check for non-null values
     * @return an expression representing the count of non-null arguments
     */
    public static Expression<Integer> numNonNulls(@NonNull List<Expression<?>> expressions) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.NUM_NON_NULLS, expressions, Integer.class);
    }

    /**
     * Returns the number of non-null arguments.
     * <p>
     * This is equivalent to the SQL {@code NUM_NONNULLS(expression1, expression2, ...)} function.
     * For example, {@code num_nonnulls(1, NULL, 2)} returns 2.
     *
     * @param expression1      the first expression to check for non-null values
     * @param otherExpressions additional expressions to check for non-null values
     * @return an expression representing the count of non-null arguments
     */
    public static Expression<Integer> numNonNulls(@NonNull Expression<?> expression1, @NonNull Expression<?>... otherExpressions) {
        List<Expression<?>> expressions = new ArrayList<>(otherExpressions.length + 1);
        expressions.add(expression1);
        expressions.addAll(Arrays.asList(otherExpressions));
        return numNonNulls(expressions);
    }

    /**
     * Wraps an expression in parentheses to control evaluation order.
     *
     * @param <T>        the expression type
     * @param expression the expression to wrap in parentheses
     * @return an expression wrapped in parentheses
     */
    public static <T> Expression<T> parentheses(@NonNull Expression<T> expression) {
        return new ParenthesesExpression<>(expression);
    }

    /**
     * Returns a random value in the range 0.0 <= x < 1.0.
     * <p>
     * Renders as {@code rand()} on H2 and MariaDB, and {@code random()} on PostgreSQL.
     *
     * @return an expression representing a random double value
     */
    public static Expression<Double> random() {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.RANDOM, List.of(), Double.class);
    }

    /**
     * Returns the inverse cosine (arc cosine) of a value, with the result in radians.
     * <p>
     * This is equivalent to the SQL {@code ACOS(value)} function.
     *
     * @param value the expression to get the arc cosine of
     * @return an expression representing the arc cosine in radians
     */
    public static Expression<Double> acos(@NonNull Expression<Double> value) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.ACOS, List.of(value), Double.class);
    }

    /**
     * Returns the inverse cosine (arc cosine) of a value, with the result in radians.
     * <p>
     * This is equivalent to the SQL {@code ACOS(value)} function.
     *
     * @param value the value to get the arc cosine of
     * @return an expression representing the arc cosine in radians
     */
    public static Expression<Double> acos(Double value) {
        return acos(constant(value));
    }

    /**
     * Returns the inverse sine (arc sine) of a value, with the result in radians.
     * <p>
     * This is equivalent to the SQL {@code ASIN(value)} function.
     *
     * @param value the expression to get the arc sine of
     * @return an expression representing the arc sine in radians
     */
    public static Expression<Double> asin(@NonNull Expression<Double> value) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.ASIN, List.of(value), Double.class);
    }

    /**
     * Returns the inverse sine (arc sine) of a value, with the result in radians.
     * <p>
     * This is equivalent to the SQL {@code ASIN(value)} function.
     *
     * @param value the value to get the arc sine of
     * @return an expression representing the arc sine in radians
     */
    public static Expression<Double> asin(Double value) {
        return asin(constant(value));
    }

    /**
     * Returns the inverse tangent (arc tangent) of a value, with the result in radians.
     * <p>
     * This is equivalent to the SQL {@code ATAN(value)} function.
     *
     * @param value the expression to get the arc tangent of
     * @return an expression representing the arc tangent in radians
     */
    public static Expression<Double> atan(@NonNull Expression<Double> value) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.ATAN, List.of(value), Double.class);
    }

    /**
     * Returns the inverse tangent (arc tangent) of a value, with the result in radians.
     * <p>
     * This is equivalent to the SQL {@code ATAN(value)} function.
     *
     * @param value the value to get the arc tangent of
     * @return an expression representing the arc tangent in radians
     */
    public static Expression<Double> atan(Double value) {
        return atan(constant(value));
    }

    /**
     * Returns the inverse tangent of y/x, with the result in radians.
     * <p>
     * This is equivalent to the SQL {@code ATAN2(y, x)} function.
     *
     * @param y the y coordinate
     * @param x the x coordinate
     * @return an expression representing the arc tangent of y/x in radians
     */
    public static Expression<Double> atan2(@NonNull Expression<Double> y, @NonNull Expression<Double> x) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.ATAN2, List.of(y, x), Double.class);
    }

    /**
     * Returns the inverse tangent of y/x, with the result in radians.
     * <p>
     * This is equivalent to the SQL {@code ATAN2(y, x)} function.
     *
     * @param y the y coordinate
     * @param x the x coordinate
     * @return an expression representing the arc tangent of y/x in radians
     */
    public static Expression<Double> atan2(Double y, Double x) {
        return atan2(constant(y), constant(x));
    }

    /**
     * Returns the inverse tangent of y/x, with the result in radians.
     * <p>
     * This is equivalent to the SQL {@code ATAN2(y, x)} function.
     *
     * @param y the y coordinate expression
     * @param x the x coordinate
     * @return an expression representing the arc tangent of y/x in radians
     */
    public static Expression<Double> atan2(@NonNull Expression<Double> y, @NonNull Double x) {
        return atan2(y, constant(x));
    }

    /**
     * Returns the cosine of a value, with the argument in radians.
     * <p>
     * This is equivalent to the SQL {@code COS(value)} function.
     *
     * @param value the expression representing the angle in radians
     * @return an expression representing the cosine of the value
     */
    public static Expression<Double> cos(@NonNull Expression<Double> value) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.COS, List.of(value), Double.class);
    }

    /**
     * Returns the cosine of a value, with the argument in radians.
     * <p>
     * This is equivalent to the SQL {@code COS(value)} function.
     *
     * @param value the angle in radians
     * @return an expression representing the cosine of the value
     */
    public static Expression<Double> cos(Double value) {
        return cos(constant(value));
    }

    /**
     * Returns the cotangent of a value, with the argument in radians.
     * <p>
     * This is equivalent to the SQL {@code COT(value)} function.
     *
     * @param value the expression representing the angle in radians
     * @return an expression representing the cotangent of the value
     */
    public static Expression<Double> cot(@NonNull Expression<Double> value) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.COT, List.of(value), Double.class);
    }

    /**
     * Returns the cotangent of a value, with the argument in radians.
     * <p>
     * This is equivalent to the SQL {@code COT(value)} function.
     *
     * @param value the angle in radians
     * @return an expression representing the cotangent of the value
     */
    public static Expression<Double> cot(Double value) {
        return cot(constant(value));
    }

    /**
     * Returns the sine of a value, with the argument in radians.
     * <p>
     * This is equivalent to the SQL {@code SIN(value)} function.
     *
     * @param value the expression representing the angle in radians
     * @return an expression representing the sine of the value
     */
    public static Expression<Double> sin(@NonNull Expression<Double> value) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.SIN, List.of(value), Double.class);
    }

    /**
     * Returns the sine of a value, with the argument in radians.
     * <p>
     * This is equivalent to the SQL {@code SIN(value)} function.
     *
     * @param value the angle in radians
     * @return an expression representing the sine of the value
     */
    public static Expression<Double> sin(Double value) {
        return sin(constant(value));
    }

    /**
     * Returns the tangent of a value, with the argument in radians.
     * <p>
     * This is equivalent to the SQL {@code TAN(value)} function.
     *
     * @param value the expression representing the angle in radians
     * @return an expression representing the tangent of the value
     */
    public static Expression<Double> tan(@NonNull Expression<Double> value) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.TAN, List.of(value), Double.class);
    }

    /**
     * Returns the tangent of a value, with the argument in radians.
     * <p>
     * This is equivalent to the SQL {@code TAN(value)} function.
     *
     * @param value the angle in radians
     * @return an expression representing the tangent of the value
     */
    public static Expression<Double> tan(Double value) {
        return tan(constant(value));
    }

    /**
     * Returns the hyperbolic sine of a value.
     * <p>
     * This is equivalent to the SQL {@code SINH(value)} function.
     *
     * @param value the expression to get the hyperbolic sine of
     * @return an expression representing the hyperbolic sine of the value
     */
    public static Expression<Double> sinh(@NonNull Expression<Double> value) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.SINH, List.of(value), Double.class);
    }

    /**
     * Returns the hyperbolic sine of a value.
     * <p>
     * This is equivalent to the SQL {@code SINH(value)} function.
     *
     * @param value the value to get the hyperbolic sine of
     * @return an expression representing the hyperbolic sine of the value
     */
    public static Expression<Double> sinh(Double value) {
        return sinh(constant(value));
    }

    /**
     * Returns the hyperbolic cosine of a value.
     * <p>
     * This is equivalent to the SQL {@code COSH(value)} function.
     *
     * @param value the expression to get the hyperbolic cosine of
     * @return an expression representing the hyperbolic cosine of the value
     */
    public static Expression<Double> cosh(@NonNull Expression<Double> value) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.COSH, List.of(value), Double.class);
    }

    /**
     * Returns the hyperbolic cosine of a value.
     * <p>
     * This is equivalent to the SQL {@code COSH(value)} function.
     *
     * @param value the value to get the hyperbolic cosine of
     * @return an expression representing the hyperbolic cosine of the value
     */
    public static Expression<Double> cosh(Double value) {
        return cosh(constant(value));
    }

    /**
     * Returns the hyperbolic tangent of a value.
     * <p>
     * This is equivalent to the SQL {@code TANH(value)} function.
     *
     * @param value the expression to get the hyperbolic tangent of
     * @return an expression representing the hyperbolic tangent of the value
     */
    public static Expression<Double> tanh(@NonNull Expression<Double> value) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.TANH, List.of(value), Double.class);
    }

    /**
     * Returns the hyperbolic tangent of a value.
     * <p>
     * This is equivalent to the SQL {@code TANH(value)} function.
     *
     * @param value the value to get the hyperbolic tangent of
     * @return an expression representing the hyperbolic tangent of the value
     */
    public static Expression<Double> tanh(Double value) {
        return tanh(constant(value));
    }

    /**
     * Returns the inverse hyperbolic sine of a value.
     * <p>
     * This is equivalent to the SQL {@code ASINH(value)} function.
     *
     * @param value the expression to get the inverse hyperbolic sine of
     * @return an expression representing the inverse hyperbolic sine of the value
     */
    public static Expression<Double> asinh(@NonNull Expression<Double> value) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.ASINH, List.of(value), Double.class);
    }

    /**
     * Returns the inverse hyperbolic sine of a value.
     * <p>
     * This is equivalent to the SQL {@code ASINH(value)} function.
     *
     * @param value the value to get the inverse hyperbolic sine of
     * @return an expression representing the inverse hyperbolic sine of the value
     */
    public static Expression<Double> asinh(Double value) {
        return asinh(constant(value));
    }

    /**
     * Returns the inverse hyperbolic cosine of a value.
     * <p>
     * This is equivalent to the SQL {@code ACOSH(value)} function.
     *
     * @param value the expression to get the inverse hyperbolic cosine of
     * @return an expression representing the inverse hyperbolic cosine of the value
     */
    public static Expression<Double> acosh(@NonNull Expression<Double> value) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.ACOSH, List.of(value), Double.class);
    }

    /**
     * Returns the inverse hyperbolic cosine of a value.
     * <p>
     * This is equivalent to the SQL {@code ACOSH(value)} function.
     *
     * @param value the value to get the inverse hyperbolic cosine of
     * @return an expression representing the inverse hyperbolic cosine of the value
     */
    public static Expression<Double> acosh(Double value) {
        return acosh(constant(value));
    }

    /**
     * Returns the inverse hyperbolic tangent of a value.
     * <p>
     * This is equivalent to the SQL {@code ATANH(value)} function.
     *
     * @param value the expression to get the inverse hyperbolic tangent of
     * @return an expression representing the inverse hyperbolic tangent of the value
     */
    public static Expression<Double> atanh(@NonNull Expression<Double> value) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.ATANH, List.of(value), Double.class);
    }

    /**
     * Returns the inverse hyperbolic tangent of a value.
     * <p>
     * This is equivalent to the SQL {@code ATANH(value)} function.
     *
     * @param value the value to get the inverse hyperbolic tangent of
     * @return an expression representing the inverse hyperbolic tangent of the value
     */
    public static Expression<Double> atanh(Double value) {
        return atanh(constant(value));
    }

    /**
     * Returns the hyperbolic cotangent of a value.
     * <p>
     * This is equivalent to the SQL {@code COTH(value)} function.
     *
     * @param value the expression to get the hyperbolic cotangent of
     * @return an expression representing the hyperbolic cotangent of the value
     */
    public static Expression<Double> coth(@NonNull Expression<Double> value) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.COTH, List.of(value), Double.class);
    }

    /**
     * Returns the hyperbolic cotangent of a value.
     * <p>
     * This is equivalent to the SQL {@code COTH(value)} function.
     *
     * @param value the value to get the hyperbolic cotangent of
     * @return an expression representing the hyperbolic cotangent of the value
     */
    public static Expression<Double> coth(Double value) {
        return coth(constant(value));
    }

    /**
     * Begins building a SELECT query that returns {@link Record} results.
     * <p>
     * Columns to select are specified in subsequent {@code .select()} calls.
     *
     * @return a {@link SelectExpression.FromStep} for specifying the FROM clause
     */
    public static SelectExpression.FromStep<Record> select() {
        return new FluentSelectExpression<>(new DefaultSelectExpression<>(false, r -> r, Record.class));
    }

    /**
     * Begins building a SELECT query for a single typed expression.
     * <p>
     * The result type is determined by the expression's Java type.
     *
     * @param <T>        the result type
     * @param expression the expression to select
     * @return a {@link SelectExpression.FromStep} for specifying the FROM clause
     */
    public static <T> SelectExpression.FromStep<T> select(@NonNull Expression<T> expression) {
        return new FluentSelectExpression<>(new DefaultSelectExpression<>(false, r -> r.get(expression), expression.getJavaType()))
                .select(expression);
    }

    /**
     * Begins building a SELECT query for multiple expressions.
     *
     * @param expressions the expressions to select
     * @return a {@link SelectExpression.FromStep} for specifying the FROM clause
     */
    public static SelectExpression.FromStep<Record> select(@NonNull Expression<?>... expressions) {
        return new FluentSelectExpression<>(new DefaultSelectExpression<>(false, r -> r, Record.class))
                .select(Arrays.asList(expressions));
    }

    /**
     * Begins building a {@code SELECT *} query.
     *
     * @return a {@link SelectExpression.FromStep} for specifying the FROM clause
     */
    public static SelectExpression.FromStep<Record> selectAsterisk() {
        return new FluentSelectExpression<>(new DefaultSelectExpression<>(false, r -> r, Record.class))
                .select(ASTERISK);
    }

    /**
     * Begins building a {@code SELECT DISTINCT} query that returns {@link Record} results.
     * <p>
     * Columns to select are specified in subsequent {@code .select()} calls.
     *
     * @return a {@link SelectExpression.FromStep} for specifying the FROM clause
     */
    public static SelectExpression.FromStep<Record> selectDistinct() {
        return new FluentSelectExpression<>(new DefaultSelectExpression<>(true, r -> r, Record.class));
    }

    /**
     * Begins building a {@code SELECT DISTINCT} query for a single typed expression.
     *
     * @param <T>        the result type
     * @param expression the expression to select distinctly
     * @return a {@link SelectExpression.FromStep} for specifying the FROM clause
     */
    public static <T> SelectExpression.FromStep<T> selectDistinct(@NonNull Expression<T> expression) {
        return new FluentSelectExpression<>(new DefaultSelectExpression<>(true, r -> r.get(expression), expression.getJavaType()))
                .select(expression);
    }

    /**
     * Begins building a {@code SELECT DISTINCT} query for multiple expressions.
     *
     * @param expressions the expressions to select distinctly
     * @return a {@link SelectExpression.FromStep} for specifying the FROM clause
     */
    public static SelectExpression.FromStep<Record> selectDistinct(@NonNull Expression<?>... expressions) {
        return new FluentSelectExpression<>(new DefaultSelectExpression<>(true, r -> r, Record.class))
                .select(Arrays.asList(expressions));
    }

    /**
     * Begins building a {@code SELECT DISTINCT *} query.
     *
     * @return a {@link SelectExpression.FromStep} for specifying the FROM clause
     */
    public static SelectExpression.FromStep<Record> selectDistinctAsterisk() {
        return new FluentSelectExpression<>(new DefaultSelectExpression<>(true, r -> r, Record.class))
                .select(ASTERISK);
    }

    // ==================== WINDOW FUNCTIONS ====================

    /**
     * Returns the number of the current row within its partition, counting from 1.
     *
     * @return a WindowExpression for ROW_NUMBER()
     */
    public static WindowExpression<Long> rowNumber() {
        return new DefaultWindowExpression<>(
                new PortableFunctionExpression<>(PortableFunctionExpression.Function.ROW_NUMBER, List.of(), Long.class),
                Long.class
        );
    }

    /**
     * Returns the rank of the current row, with gaps.
     *
     * @return a WindowExpression for RANK()
     */
    public static WindowExpression<Long> rank() {
        return new DefaultWindowExpression<>(
                new PortableFunctionExpression<>(PortableFunctionExpression.Function.RANK, List.of(), Long.class),
                Long.class
        );
    }

    /**
     * Returns the rank of the current row, without gaps.
     *
     * @return a WindowExpression for DENSE_RANK()
     */
    public static WindowExpression<Long> denseRank() {
        return new DefaultWindowExpression<>(
                new PortableFunctionExpression<>(PortableFunctionExpression.Function.DENSE_RANK, List.of(), Long.class),
                Long.class
        );
    }

    /**
     * Divides the partition into the given number of buckets and returns the bucket number
     * (1..buckets) for the current row.
     *
     * @param buckets the number of buckets to divide the partition into
     * @return a WindowExpression for NTILE()
     */
    public static WindowExpression<Integer> ntile(int buckets) {
        return new DefaultWindowExpression<>(
                new PortableFunctionExpression<>(PortableFunctionExpression.Function.NTILE, List.of(constant(buckets)), Integer.class),
                Integer.class
        );
    }

    /**
     * Returns the relative rank of the current row within its partition:
     * {@code (rank - 1) / (total_rows - 1)}.
     *
     * @return a WindowExpression for PERCENT_RANK()
     */
    public static WindowExpression<Double> percentRank() {
        return new DefaultWindowExpression<>(
                new PortableFunctionExpression<>(PortableFunctionExpression.Function.PERCENT_RANK, List.of(), Double.class),
                Double.class
        );
    }

    /**
     * Returns the cumulative distribution of the current row within its partition:
     * the number of rows with values less than or equal to the current row divided by the total number of rows.
     *
     * @return a WindowExpression for CUME_DIST()
     */
    public static WindowExpression<Double> cumeDist() {
        return new DefaultWindowExpression<>(
                new PortableFunctionExpression<>(PortableFunctionExpression.Function.CUME_DIST, List.of(), Double.class),
                Double.class
        );
    }

    /**
     * Returns the value of the expression at the first row of the window frame.
     *
     * @param expression the expression to evaluate
     * @param <T>        the type of the expression
     * @return a WindowExpression for FIRST_VALUE()
     */
    public static <T> WindowExpression<T> firstValue(@NonNull Expression<T> expression) {
        return new DefaultWindowExpression<>(
                new PortableFunctionExpression<>(PortableFunctionExpression.Function.FIRST_VALUE,
                        List.of(expression), expression.getJavaType()),
                expression.getJavaType()
        );
    }

    /**
     * Returns the value of the expression at the last row of the window frame.
     * <p>
     * Note: the default window frame is {@code RANGE BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW},
     * which makes {@code LAST_VALUE} return the current row's value. To get the actual last value
     * of the partition, combine with {@link WindowExpression#rowsBetween(FrameBound, FrameBound)}
     * using {@link FrameBound#UNBOUNDED_PRECEDING} and {@link FrameBound#UNBOUNDED_FOLLOWING}.
     *
     * @param expression the expression to evaluate
     * @param <T>        the type of the expression
     * @return a WindowExpression for LAST_VALUE()
     */
    public static <T> WindowExpression<T> lastValue(@NonNull Expression<T> expression) {
        return new DefaultWindowExpression<>(
                new PortableFunctionExpression<>(PortableFunctionExpression.Function.LAST_VALUE,
                        List.of(expression), expression.getJavaType()),
                expression.getJavaType()
        );
    }

    /**
     * Returns the value of the expression at the nth row of the window frame (1-based).
     *
     * @param expression the expression to evaluate
     * @param n          the 1-based row index within the frame
     * @param <T>        the type of the expression
     * @return a WindowExpression for NTH_VALUE()
     */
    public static <T> WindowExpression<T> nthValue(@NonNull Expression<T> expression, int n) {
        return new DefaultWindowExpression<>(
                new PortableFunctionExpression<>(PortableFunctionExpression.Function.NTH_VALUE,
                        List.of(expression, constant(n)), expression.getJavaType()),
                expression.getJavaType()
        );
    }

    /**
     * Returns value evaluated at the row that is offset rows before the current row.
     *
     * @param expression the expression to evaluate
     * @param <T>        the type of the expression
     * @return a WindowExpression for LAG()
     */
    public static <T> WindowExpression<T> lag(@NonNull Expression<T> expression) {
        return lag(expression, 1);
    }

    /**
     * Returns value evaluated at the row that is offset rows before the current row.
     *
     * @param expression the expression to evaluate
     * @param offset     the number of rows back
     * @param <T>        the type of the expression
     * @return a WindowExpression for LAG()
     */
    @SuppressWarnings("unchecked")
    public static <T> WindowExpression<T> lag(@NonNull Expression<T> expression, int offset) {
        return new DefaultWindowExpression<>(
                new PortableFunctionExpression<>(PortableFunctionExpression.Function.LAG,
                        List.of(expression, constant(offset)),
                        expression.getJavaType()),
                expression.getJavaType()
        );
    }

    /**
     * Returns value evaluated at the row that is offset rows before the current row,
     * with a default value if no such row exists.
     *
     * @param expression   the expression to evaluate
     * @param offset       the number of rows back
     * @param defaultValue the default value
     * @param <T>          the type of the expression
     * @return a WindowExpression for LAG()
     */
    @SuppressWarnings("unchecked")
    public static <T> WindowExpression<T> lag(@NonNull Expression<T> expression, int offset, @NonNull T defaultValue) {
        Class<T> type = (Class<T>) defaultValue.getClass();
        return new DefaultWindowExpression<>(
                new PortableFunctionExpression<>(PortableFunctionExpression.Function.LAG,
                        List.of(expression, constant(offset), constant(type, defaultValue)),
                        type),
                type
        );
    }

    /**
     * Returns value evaluated at the row that is offset rows after the current row.
     *
     * @param expression the expression to evaluate
     * @param <T>        the type of the expression
     * @return a WindowExpression for LEAD()
     */
    public static <T> WindowExpression<T> lead(@NonNull Expression<T> expression) {
        return lead(expression, 1);
    }

    /**
     * Returns value evaluated at the row that is offset rows after the current row.
     *
     * @param expression the expression to evaluate
     * @param offset     the number of rows forward
     * @param <T>        the type of the expression
     * @return a WindowExpression for LEAD()
     */
    @SuppressWarnings("unchecked")
    public static <T> WindowExpression<T> lead(@NonNull Expression<T> expression, int offset) {
        return new DefaultWindowExpression<>(
                new PortableFunctionExpression<>(PortableFunctionExpression.Function.LEAD,
                        List.of(expression, constant(offset)),
                        expression.getJavaType()),
                expression.getJavaType()
        );
    }

    /**
     * Returns value evaluated at the row that is offset rows after the current row,
     * with a default value if no such row exists.
     *
     * @param expression   the expression to evaluate
     * @param offset       the number of rows forward
     * @param defaultValue the default value
     * @param <T>          the type of the expression
     * @return a WindowExpression for LEAD()
     */
    @SuppressWarnings("unchecked")
    public static <T> WindowExpression<T> lead(@NonNull Expression<T> expression, int offset, @NonNull T defaultValue) {
        Class<T> type = (Class<T>) defaultValue.getClass();
        return new DefaultWindowExpression<>(
                new PortableFunctionExpression<>(PortableFunctionExpression.Function.LEAD,
                        List.of(expression, constant(offset), constant(type, defaultValue)),
                        type),
                type
        );
    }

    /**
     * Computes a value corresponding to the specified fraction within the ordered set
     * of values, interpolating between adjacent values if needed.
     * Requires {@code WITHIN GROUP (ORDER BY ...)} syntax.
     *
     * @param fraction         a value between 0 and 1 specifying the percentile
     * @param withinGroupOrder the ORDER BY clause for WITHIN GROUP
     * @return a WindowExpression for PERCENTILE_CONT()
     */
    public static WindowExpression<Double> percentileCont(double fraction, @NonNull Order withinGroupOrder) {
        return percentileCont(constant(fraction), withinGroupOrder);
    }

    /**
     * Computes a value corresponding to the fraction expression within the ordered set
     * of values, interpolating between adjacent values if needed.
     * Requires {@code WITHIN GROUP (ORDER BY ...)} syntax.
     *
     * @param fraction          an expression evaluating to a value between 0 and 1
     * @param withinGroupOrders the ORDER BY clauses for WITHIN GROUP
     * @return a WindowExpression for PERCENTILE_CONT()
     */
    public static WindowExpression<Double> percentileCont(@NonNull Expression<Double> fraction, @NonNull Order... withinGroupOrders) {
        final PortableFunctionExpression<Double> expression = new PortableFunctionExpression<>(
                PortableFunctionExpression.Function.PERCENTILE_CONT, List.of(fraction), Double.class);
        expression.getWithinGroupOrders().addAll(Arrays.asList(withinGroupOrders));
        return new DefaultWindowExpression<>(expression, Double.class);
    }

    /**
     * Returns the first value in the ordered set whose position in the ordering equals or
     * exceeds the specified fraction.
     * Requires {@code WITHIN GROUP (ORDER BY ...)} syntax.
     *
     * @param fraction         a value between 0 and 1 specifying the percentile
     * @param withinGroupOrder the ORDER BY clause for WITHIN GROUP
     * @return a WindowExpression for PERCENTILE_DISC()
     */
    public static WindowExpression<Double> percentileDisc(double fraction, @NonNull Order withinGroupOrder) {
        return percentileDisc(constant(fraction), withinGroupOrder);
    }

    /**
     * Returns the first value in the ordered set whose position in the ordering equals or
     * exceeds the fraction expression.
     * Requires {@code WITHIN GROUP (ORDER BY ...)} syntax.
     *
     * @param fraction          an expression evaluating to a value between 0 and 1
     * @param withinGroupOrders the ORDER BY clauses for WITHIN GROUP
     * @return a WindowExpression for PERCENTILE_DISC()
     */
    public static WindowExpression<Double> percentileDisc(@NonNull Expression<Double> fraction, @NonNull Order... withinGroupOrders) {
        final PortableFunctionExpression<Double> expression = new PortableFunctionExpression<>(
                PortableFunctionExpression.Function.PERCENTILE_DISC, List.of(fraction), Double.class);
        expression.getWithinGroupOrders().addAll(Arrays.asList(withinGroupOrders));
        return new DefaultWindowExpression<>(expression, Double.class);
    }

    // ==================== STRING FUNCTIONS ====================

    /**
     * Extracts a substring starting at the given position.
     *
     * @param string the source string expression
     * @param start  the 1-based start position
     * @return substring expression
     */
    public static Expression<String> substring(@NonNull Expression<String> string, @NonNull Expression<Integer> start) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.SUBSTRING, List.of(string, start), String.class);
    }

    /**
     * Extracts a substring starting at the given position.
     *
     * @param string the source string expression
     * @param start  the 1-based start position
     * @return substring expression
     */
    public static Expression<String> substring(@NonNull Expression<String> string, int start) {
        return substring(string, constant(start));
    }

    /**
     * Extracts a substring starting at the given position.
     *
     * @param string the source string
     * @param start  the 1-based start position
     * @return substring expression
     */
    public static Expression<String> substring(@NonNull String string, int start) {
        return substring(constant(string), constant(start));
    }

    /**
     * Extracts a substring of specified length starting at the given position.
     *
     * @param string the source string expression
     * @param start  the 1-based start position
     * @param length the number of characters to extract
     * @return substring expression
     */
    public static Expression<String> substring(@NonNull Expression<String> string, @NonNull Expression<Integer> start, @NonNull Expression<Integer> length) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.SUBSTRING, List.of(string, start, length), String.class);
    }

    /**
     * Extracts a substring of specified length starting at the given position.
     *
     * @param string the source string expression
     * @param start  the 1-based start position
     * @param length the number of characters to extract
     * @return substring expression
     */
    public static Expression<String> substring(@NonNull Expression<String> string, int start, int length) {
        return substring(string, constant(start), constant(length));
    }

    /**
     * Extracts a substring of specified length starting at the given position.
     *
     * @param string the source string
     * @param start  the 1-based start position
     * @param length the number of characters to extract
     * @return substring expression
     */
    public static Expression<String> substring(@NonNull String string, int start, int length) {
        return substring(constant(string), constant(start), constant(length));
    }

    public static <T extends Number> Expression<String> toHex(@NonNull Expression<T> expression) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.TO_HEX, List.of(expression), String.class);
    }

    public static Expression<String> toHex(@NonNull Integer value) {
        return toHex(constant(value));
    }

    public static Expression<String> toHex(@NonNull Long value) {
        return toHex(constant(value));
    }

    // --- JSON ---

    /**
     * Creates a JSON expression from a literal JSON string.
     *
     * @param jsonLiteral the JSON string literal
     * @return a {@link JsonExpression} wrapping the literal
     */
    public static JsonExpression json(@NonNull String jsonLiteral) {
        return new JsonExpression(
                new ConstantExpression<>(String.class, jsonLiteral),
                JsonExpression.Operator.LITERAL,
                null, null
        );
    }

    /**
     * Aggregates values into a JSON array (PostgreSQL json_agg).
     *
     * @param expression the expression to aggregate
     * @return a JSON array aggregation expression
     */
    public static <T> Expression<String> jsonArrayAgg(@NonNull Expression<T> expression) {
        return new PortableAggregateExpression<>(String.class, AggregateQuantifier.NONE, PortableAggregateExpression.Function.JSON_ARRAY_AGG, List.of(expression));
    }

    /**
     * Aggregates values into a JSON array with a specified ordering.
     * <p>
     * This is equivalent to the SQL {@code JSON_ARRAYAGG(expression ORDER BY ...)} aggregate function ({@code json_agg} on PostgreSQL).
     *
     * @param <T>        the element type
     * @param expression the expression to aggregate into a JSON array
     * @param orderBy    the ordering to apply within the aggregate
     * @return an expression representing the aggregated JSON array
     */
    public static <T> Expression<String> jsonArrayAgg(@NonNull Expression<T> expression, @NonNull Order orderBy) {
        return jsonArrayAgg(expression, List.of(orderBy));
    }

    /**
     * Aggregates values into a JSON array with a specified ordering.
     * <p>
     * This is equivalent to the SQL {@code JSON_ARRAYAGG(expression ORDER BY ...)} aggregate function ({@code json_agg} on PostgreSQL).
     *
     * @param <T>        the element type
     * @param expression the expression to aggregate into a JSON array
     * @param orders     the ordering to apply within the aggregate
     * @return an expression representing the aggregated JSON array
     */
    public static <T> Expression<String> jsonArrayAgg(@NonNull Expression<T> expression, @NonNull List<Order> orders) {
        return jsonArrayAggWhere(expression, Collections.emptyList(), orders);
    }

    /**
     * Aggregates values into a JSON array with a filter condition.
     * <p>
     * This is equivalent to the SQL {@code JSON_ARRAYAGG(expression) FILTER (WHERE ...)} aggregate function ({@code json_agg} on PostgreSQL).
     *
     * @param <T>             the element type
     * @param expression      the expression to aggregate into a JSON array
     * @param filterCondition the filter condition to apply to the aggregate
     * @return an expression representing the filtered aggregated JSON array
     */
    public static <T> Expression<String> jsonArrayAggWhere(@NonNull Expression<T> expression, @NonNull Condition filterCondition) {
        return jsonArrayAggWhere(expression, List.of(filterCondition), Collections.emptyList());
    }

    /**
     * Aggregates values into a JSON array with filter conditions.
     * <p>
     * This is equivalent to the SQL {@code JSON_ARRAYAGG(expression) FILTER (WHERE ...)} aggregate function ({@code json_agg} on PostgreSQL).
     *
     * @param <T>              the element type
     * @param expression       the expression to aggregate into a JSON array
     * @param filterConditions the filter conditions to apply to the aggregate
     * @return an expression representing the filtered aggregated JSON array
     */
    public static <T> Expression<String> jsonArrayAggWhere(@NonNull Expression<T> expression, @NonNull List<Condition> filterConditions) {
        return jsonArrayAggWhere(expression, filterConditions, Collections.emptyList());
    }

    /**
     * Aggregates values into a JSON array with a filter condition and ordering.
     * <p>
     * This is equivalent to the SQL {@code JSON_ARRAYAGG(expression ORDER BY ...) FILTER (WHERE ...)} aggregate function ({@code json_agg} on PostgreSQL).
     *
     * @param <T>             the element type
     * @param expression      the expression to aggregate into a JSON array
     * @param filterCondition the filter condition to apply to the aggregate
     * @param orderBy         the ordering to apply within the aggregate
     * @return an expression representing the filtered and ordered aggregated JSON array
     */
    public static <T> Expression<String> jsonArrayAggWhere(@NonNull Expression<T> expression, @NonNull Condition filterCondition, @NonNull Order orderBy) {
        return jsonArrayAggWhere(expression, List.of(filterCondition), List.of(orderBy));
    }

    /**
     * Aggregates values into a JSON array with a filter condition and ordering.
     * <p>
     * This is equivalent to the SQL {@code JSON_ARRAYAGG(expression ORDER BY ...) FILTER (WHERE ...)} aggregate function ({@code json_agg} on PostgreSQL).
     *
     * @param <T>             the element type
     * @param expression      the expression to aggregate into a JSON array
     * @param filterCondition the filter condition to apply to the aggregate
     * @param orders          the ordering to apply within the aggregate
     * @return an expression representing the filtered and ordered aggregated JSON array
     */
    public static <T> Expression<String> jsonArrayAggWhere(@NonNull Expression<T> expression, @NonNull Condition filterCondition, @NonNull List<Order> orders) {
        return jsonArrayAggWhere(expression, List.of(filterCondition), orders);
    }

    /**
     * Aggregates values into a JSON array with filter conditions and ordering.
     * <p>
     * This is equivalent to the SQL {@code JSON_ARRAYAGG(expression ORDER BY ...) FILTER (WHERE ...)} aggregate function ({@code json_agg} on PostgreSQL).
     *
     * @param <T>              the element type
     * @param expression       the expression to aggregate into a JSON array
     * @param filterConditions the filter conditions to apply to the aggregate
     * @param orderBy          the ordering to apply within the aggregate
     * @return an expression representing the filtered and ordered aggregated JSON array
     */
    public static <T> Expression<String> jsonArrayAggWhere(@NonNull Expression<T> expression, @NonNull List<Condition> filterConditions, @NonNull Order orderBy) {
        return jsonArrayAggWhere(expression, filterConditions, List.of(orderBy));
    }

    /**
     * Aggregates values into a JSON array with filter conditions and ordering.
     * <p>
     * This is equivalent to the SQL {@code JSON_ARRAYAGG(expression ORDER BY ...) FILTER (WHERE ...)} aggregate function ({@code json_agg} on PostgreSQL).
     *
     * @param <T>              the element type
     * @param expression       the expression to aggregate into a JSON array
     * @param filterConditions the filter conditions to apply to the aggregate
     * @param orders           the ordering to apply within the aggregate
     * @return an expression representing the filtered and ordered aggregated JSON array
     */
    public static <T> Expression<String> jsonArrayAggWhere(@NonNull Expression<T> expression, @NonNull List<Condition> filterConditions, @NonNull List<Order> orders) {
        final PortableAggregateExpression<String> aggregateExpression = new PortableAggregateExpression<>(String.class, AggregateQuantifier.NONE, PortableAggregateExpression.Function.JSON_ARRAY_AGG, List.of(expression));
        aggregateExpression.getOrders().addAll(orders);
        aggregateExpression.getFilter().addAll(filterConditions);
        return aggregateExpression;
    }

    /**
     * Aggregates key-value pairs into a JSON object (PostgreSQL json_object_agg).
     *
     * @param key   the key expression
     * @param value the value expression
     * @return a JSON object aggregation expression
     */
    public static Expression<String> jsonObjectAgg(@NonNull Expression<?> key, @NonNull Expression<?> value) {
        return new PortableAggregateExpression<>(String.class, AggregateQuantifier.NONE, PortableAggregateExpression.Function.JSON_OBJECT_AGG, List.of(key, value));
    }

    /**
     * Builds a JSON object from alternating key/value expressions.
     * <p>
     * Renders as {@code json_object(...)} on MariaDB, {@code json_build_object(...)} on PostgreSQL,
     * and {@code json_object(KEY ... VALUE ...)} on H2.
     *
     * @param keyValuePairs alternating key and value expressions; must contain an even number of arguments
     * @return a JSON object expression
     */
    public static Expression<String> jsonObject(@NonNull Expression<?>... keyValuePairs) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.JSON_OBJECT, List.of(keyValuePairs), String.class);
    }

    /**
     * Builds a JSON object from alternating key/value expressions.
     * <p>
     * Renders as {@code json_object(...)} on MariaDB, {@code json_build_object(...)} on PostgreSQL,
     * and {@code json_object(KEY ... VALUE ...)} on H2.
     *
     * @param keyValuePairs alternating key and value expressions; must contain an even number of arguments
     * @return a JSON object expression
     */
    public static Expression<String> jsonObject(@NonNull List<Expression<?>> keyValuePairs) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.JSON_OBJECT, keyValuePairs, String.class);
    }

    /**
     * Builds a JSON array from elements.
     * <p>
     * Renders as {@code json_array(...)} on MariaDB, {@code json_array(... NULL ON NULL)} on H2,
     * and {@code json_build_array(...)} on PostgreSQL. Null elements are preserved as JSON
     * {@code null} on all databases.
     *
     * @param elements the elements to include in the array
     * @return a JSON array expression
     */
    public static Expression<String> jsonArray(@NonNull Expression<?>... elements) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.JSON_ARRAY, List.of(elements), String.class);
    }

    /**
     * Builds a JSON array from elements.
     * <p>
     * Renders as {@code json_array(...)} on MariaDB, {@code json_array(... NULL ON NULL)} on H2,
     * and {@code json_build_array(...)} on PostgreSQL. Null elements are preserved as JSON
     * {@code null} on all databases.
     *
     * @param elements the elements to include in the array
     * @return a JSON array expression
     */
    public static Expression<String> jsonArray(@NonNull List<Expression<?>> elements) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.JSON_ARRAY, elements, String.class);
    }

    /**
     * Returns the number of elements in the top-level JSON array.
     * <p>
     * Renders as {@code json_array_length(CAST(... AS json))} on PostgreSQL,
     * {@code CARDINALITY(... FORMAT JSON)} on H2 and {@code json_length(...)} on MariaDB.
     * For non-array input the behavior is database-specific: H2 returns SQL {@code NULL},
     * PostgreSQL raises an error, and MariaDB returns the number of members (objects)
     * or {@code 1} (scalars).
     *
     * @param jsonExpr the JSON array expression
     * @return an integer expression representing the array length
     */
    public static Expression<Integer> jsonArrayLength(@NonNull Expression<?> jsonExpr) {
        return new PortableFunctionExpression<>(PortableFunctionExpression.Function.JSON_ARRAY_LENGTH, List.of(jsonExpr), Integer.class);
    }

    /**
     * Creates a user-defined function call expression.
     * The function can be used anywhere expressions are accepted: SELECT, WHERE, ORDER BY, HAVING.
     *
     * @param name       the SQL function name
     * @param returnType the Java type of the function's return value
     * @param args       expressions to pass as function arguments
     * @param <T>        the return type
     * @return a typed expression representing the function call
     */
    public static <T> Expression<T> function(@NonNull String name, @NonNull Class<T> returnType, @NonNull Expression<?>... args) {
        return new FunctionExpression<>(name, List.of(args), returnType);
    }

    /**
     * Creates a user-defined function call expression from a list of arguments.
     * The function can be used anywhere expressions are accepted: SELECT, WHERE, ORDER BY, HAVING.
     *
     * @param name       the SQL function name
     * @param returnType the Java type of the function's return value
     * @param args       list of expressions to pass as function arguments
     * @param <T>        the return type
     * @return a typed expression representing the function call
     */
    public static <T> Expression<T> function(@NonNull String name, @NonNull Class<T> returnType, @NonNull List<Expression<?>> args) {
        return new FunctionExpression<>(name, args, returnType);
    }
}
