// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.utils

import spock.lang.Specification

class StringUtilsSpec extends Specification {

    def "isNotBlank"() {
        expect:
            StringUtils.isNotBlank("a")
            StringUtils.isNotBlank(" a ")
            StringUtils.isNotBlank("Typical string")
            !StringUtils.isNotBlank(null)
            !StringUtils.isNotBlank("")
            !StringUtils.isNotBlank(" ")
            !StringUtils.isNotBlank("\n")
            !StringUtils.isNotBlank("\t")
    }

    def "ucFirst"() {
        expect:
            StringUtils.ucFirst(null) == null
            StringUtils.ucFirst("") == ""
            StringUtils.ucFirst(" ") == " "
            StringUtils.ucFirst("a") == "A"
            StringUtils.ucFirst("cat") == "Cat"
            StringUtils.ucFirst(" cat") == " cat"
    }

    def "lcFirst"() {
        expect:
            StringUtils.lcFirst(null) == null
            StringUtils.lcFirst("") == ""
            StringUtils.lcFirst(" ") == " "
            StringUtils.lcFirst("A") == "a"
            StringUtils.lcFirst("Cat") == "cat"
            StringUtils.lcFirst(" Cat") == " Cat"
    }

    def "toPascalCase"() {
        expect:
            StringUtils.toPascalCase(null) == null
            StringUtils.toPascalCase("") == ""
            StringUtils.toPascalCase(" ") == ""
            StringUtils.toPascalCase("users table") == "UsersTable"
            StringUtils.toPascalCase("UsersTable") == "UsersTable"
            StringUtils.toPascalCase("users_table") == "UsersTable"
            StringUtils.toPascalCase("users_____table") == "UsersTable"
            StringUtils.toPascalCase(" users_____table ") == "UsersTable"
            StringUtils.toPascalCase("usersTable") == "UsersTable"
            StringUtils.toPascalCase(" usersTable") == "UsersTable"
    }

    def "toCamelCase"() {
        expect:
            StringUtils.toCamelCase(null) == null
            StringUtils.toCamelCase("") == ""
            StringUtils.toCamelCase(" ") == ""
            StringUtils.toCamelCase("users table") == "usersTable"
            StringUtils.toCamelCase("UsersTable") == "usersTable"
            StringUtils.toCamelCase("users_table") == "usersTable"
            StringUtils.toCamelCase("users_____table") == "usersTable"
            StringUtils.toCamelCase(" users_____table ") == "usersTable"
            StringUtils.toCamelCase("usersTable") == "usersTable"
            StringUtils.toCamelCase(" usersTable") == "usersTable"
    }

    def "escapeQuotationMarks"() {
        expect:
            StringUtils.escapeQuotationMarks(null) == null
            StringUtils.escapeQuotationMarks("") == ""
            StringUtils.escapeQuotationMarks(" ") == " "
            StringUtils.escapeQuotationMarks("\"users\"") == "\\\"users\\\""
    }
}
