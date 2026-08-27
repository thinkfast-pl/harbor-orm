// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.core.utils

import spock.lang.Specification

class HarborBeanUtilsTest extends Specification {

    static class Parent {
        private String parentField = "parentValue"
        private void parentMethod() { parentField = "called" }
    }

    static class Child extends Parent {
        private String childField = "childValue"
    }

    static class GrandChild extends Child {
        private String grandChildField = "grandChildValue"
    }

    def "tryConstruct with primitive parameter type"() {
        when:
            ValueBean object = HarborBeanUtils.tryConstruct(ValueBean.class, [String.class, long.class] as Class<?>[], ["aa", 11L] as Object[])

        then:
            object != null
            object.a == "aa"
            object.b == 11L
    }

    def "tryConstruct with wrapper parameter type"() {
        when:
            ValueBean object = HarborBeanUtils.tryConstruct(ValueBean.class, [String.class, Long.class] as Class<?>[], ["aa", Long.valueOf(11)] as Object[])

        then:
            object != null
            object.a == "aa"
            object.b == 11L
    }

    def "getPropertyValue reads field from direct class"() {
        given:
            def child = new Child()

        expect:
            HarborBeanUtils.getPropertyValue(child, "childField") == "childValue"
    }

    def "getPropertyValue reads inherited field from superclass"() {
        given:
            def child = new Child()

        expect:
            HarborBeanUtils.getPropertyValue(child, "parentField") == "parentValue"
    }

    def "getPropertyValue reads field from grandparent class"() {
        given:
            def grandChild = new GrandChild()

        expect:
            HarborBeanUtils.getPropertyValue(grandChild, "parentField") == "parentValue"
    }

    def "setPropertyValue sets inherited field on superclass"() {
        given:
            def child = new Child()

        when:
            HarborBeanUtils.setPropertyValue(child, "parentField", "newValue")

        then:
            HarborBeanUtils.getPropertyValue(child, "parentField") == "newValue"
    }

    def "setPropertyValue sets field on grandparent class"() {
        given:
            def grandChild = new GrandChild()

        when:
            HarborBeanUtils.setPropertyValue(grandChild, "parentField", "newValue")

        then:
            HarborBeanUtils.getPropertyValue(grandChild, "parentField") == "newValue"
    }

    def "callMethod invokes inherited method from superclass"() {
        given:
            def child = new Child()

        when:
            HarborBeanUtils.callMethod(child, "parentMethod")

        then:
            HarborBeanUtils.getPropertyValue(child, "parentField") == "called"
    }

    def "getPropertyValue throws for nonexistent field"() {
        given:
            def child = new Child()

        when:
            HarborBeanUtils.getPropertyValue(child, "nonExistent")

        then:
            thrown(RuntimeException)
    }
}
