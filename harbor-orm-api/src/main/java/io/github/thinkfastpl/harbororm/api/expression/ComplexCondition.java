// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.api.expression;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * A compound condition that chains multiple conditions with {@code AND}/{@code OR} operators.
 * Produced by {@link Condition#and(Condition)} and {@link Condition#or(Condition)}.
 */
public class ComplexCondition implements Condition {

    public enum Operator {
        AND,
        OR,
    }

    private record Node(
            Operator operator,
            Condition condition
    ) {
    }

    @Getter
    private final Condition firstCondition;
    private final List<Node> otherConditions;

    public ComplexCondition(Condition conditionLeft, Operator operator, Condition conditionRight) {
        this.firstCondition = conditionLeft;
        this.otherConditions = List.of(new Node(operator, conditionRight));
    }

    private ComplexCondition(Condition firstCondition, List<Node> otherConditions) {
        this.firstCondition = firstCondition;
        this.otherConditions = otherConditions;
    }

    @Override
    public Condition and(Condition other) {
        return new ComplexCondition(firstCondition, append(otherConditions, new Node(Operator.AND, other)));
    }

    @Override
    public Condition or(Condition other) {
        return new ComplexCondition(firstCondition, append(otherConditions, new Node(Operator.OR, other)));
    }

    public void visitOtherConditions(BiConsumer<Operator, Condition> consumer) {
        otherConditions.forEach(node ->
                consumer.accept(node.operator(), node.condition())
        );
    }

    private static List<Node> append(List<Node> nodes, Node node) {
        final List<Node> newList = new ArrayList<>(nodes.size() + 1);
        newList.addAll(nodes);
        newList.add(node);
        return Collections.unmodifiableList(newList);
    }
}
