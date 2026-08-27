// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class DSLAndOrTest {

    @Test
    void andWithFourConditionsRetainsAll() {
        Condition c1 = new BooleanConstantExpression(true);
        Condition c2 = new BooleanConstantExpression(true);
        Condition c3 = new BooleanConstantExpression(true);
        Condition c4 = new BooleanConstantExpression(true);

        Condition result = DSL.and(List.of(c1, c2, c3, c4));

        assertInstanceOf(ComplexCondition.class, result);
        ComplexCondition complex = (ComplexCondition) result;

        assertSame(c1, complex.getFirstCondition());

        List<Condition> others = new ArrayList<>();
        complex.visitOtherConditions((op, cond) -> {
            assertEquals(ComplexCondition.Operator.AND, op);
            others.add(cond);
        });
        assertEquals(3, others.size(), "Expected 3 chained conditions (c2, c3, c4)");
        assertSame(c2, others.get(0));
        assertSame(c3, others.get(1));
        assertSame(c4, others.get(2));
    }

    @Test
    void orWithFourConditionsRetainsAll() {
        Condition c1 = new BooleanConstantExpression(false);
        Condition c2 = new BooleanConstantExpression(false);
        Condition c3 = new BooleanConstantExpression(false);
        Condition c4 = new BooleanConstantExpression(false);

        Condition result = DSL.or(List.of(c1, c2, c3, c4));

        assertInstanceOf(ComplexCondition.class, result);
        ComplexCondition complex = (ComplexCondition) result;

        assertSame(c1, complex.getFirstCondition());

        List<Condition> others = new ArrayList<>();
        complex.visitOtherConditions((op, cond) -> {
            assertEquals(ComplexCondition.Operator.OR, op);
            others.add(cond);
        });
        assertEquals(3, others.size(), "Expected 3 chained conditions (c2, c3, c4)");
        assertSame(c2, others.get(0));
        assertSame(c3, others.get(1));
        assertSame(c4, others.get(2));
    }

    @Test
    void andVarargsWithThreeConditionsRetainsAll() {
        Condition c1 = new BooleanConstantExpression(true);
        Condition c2 = new BooleanConstantExpression(true);
        Condition c3 = new BooleanConstantExpression(true);

        Condition result = DSL.and(c1, c2, c3);

        assertInstanceOf(ComplexCondition.class, result);
        ComplexCondition complex = (ComplexCondition) result;

        AtomicInteger count = new AtomicInteger();
        complex.visitOtherConditions((op, cond) -> count.incrementAndGet());
        assertEquals(2, count.get(), "Expected 2 chained conditions (c2, c3)");
    }

    @Test
    void orVarargsWithThreeConditionsRetainsAll() {
        Condition c1 = new BooleanConstantExpression(false);
        Condition c2 = new BooleanConstantExpression(false);
        Condition c3 = new BooleanConstantExpression(false);

        Condition result = DSL.or(c1, c2, c3);

        assertInstanceOf(ComplexCondition.class, result);
        ComplexCondition complex = (ComplexCondition) result;

        AtomicInteger count = new AtomicInteger();
        complex.visitOtherConditions((op, cond) -> count.incrementAndGet());
        assertEquals(2, count.get(), "Expected 2 chained conditions (c2, c3)");
    }
}
