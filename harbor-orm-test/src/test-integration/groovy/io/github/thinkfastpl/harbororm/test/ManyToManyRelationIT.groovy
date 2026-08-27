// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test


import io.github.thinkfastpl.harbororm.test.domain.mtm.*

class ManyToManyRelationIT extends AbstractHarborIT {

    QStudentEntity qStudent = new QStudentEntity(null)
    QCourseEntity qCourse = new QCourseEntity(null)
    QCourseIdOnlyEntity qCourseIdOnly = new QCourseIdOnlyEntity(null)

    // === INSERT ===

    def "should insert entity with Set<Entity> and create join table rows"() {
        given:
            CourseEntity math = new CourseEntity(1L, "Math")
            CourseEntity physics = new CourseEntity(2L, "Physics")
            session.insertEntity(qCourse, math)
            session.insertEntity(qCourse, physics)

            StudentEntity student = new StudentEntity(1L, "Alice", Set.of(math, physics))

        when:
            session.insertEntity(qStudent, student)

        then:
            StudentEntity loaded = session.selectEntity(qStudent).whereIdEq(1L).fetchSingle()
            loaded.courses.size() == 2
            loaded.courses.any { it.title == "Math" }
            loaded.courses.any { it.title == "Physics" }

        where:
            session << allSessions
    }

    def "should insert entity with Set<ID> and create join table rows"() {
        given:
            StudentEntity student1 = new StudentEntity(1L, "Alice", Set.of())
            StudentEntity student2 = new StudentEntity(2L, "Bob", Set.of())
            session.insertEntity(qStudent, student1)
            session.insertEntity(qStudent, student2)

            CourseIdOnlyEntity tutor = new CourseIdOnlyEntity(1L, "Math Tutor", Set.of(1L, 2L))

        when:
            session.insertEntity(qCourseIdOnly, tutor)

        then:
            CourseIdOnlyEntity loaded = session.selectEntity(qCourseIdOnly).whereIdEq(1L).fetchSingle()
            loaded.studentIds.size() == 2
            loaded.studentIds.contains(1L)
            loaded.studentIds.contains(2L)

        where:
            session << allSessions
    }

    def "should insert entity with empty Set"() {
        given:
            StudentEntity student = new StudentEntity(1L, "Alice", Set.of())

        when:
            session.insertEntity(qStudent, student)

        then:
            StudentEntity loaded = session.selectEntity(qStudent).whereIdEq(1L).fetchSingle()
            loaded.courses.isEmpty()

        where:
            session << allSessions
    }

    def "should insert entity with null Set"() {
        given:
            StudentEntity student = new StudentEntity(1L, "Alice", null)

        when:
            session.insertEntity(qStudent, student)

        then:
            StudentEntity loaded = session.selectEntity(qStudent).whereIdEq(1L).fetchSingle()
            loaded.courses.isEmpty()

        where:
            session << allSessions
    }

    // === LAZY LOADING ===

    def "should lazily load Set<Entity> on first access"() {
        given:
            CourseEntity math = new CourseEntity(1L, "Math")
            session.insertEntity(qCourse, math)
            session.insertEntity(qStudent, new StudentEntity(1L, "Alice", Set.of(math)))

        when:
            StudentEntity loaded = session.selectEntity(qStudent).whereIdEq(1L).fetchSingle()

        then:
            loaded.courses.size() == 1
            loaded.courses.first().title == "Math"

        where:
            session << allSessions
    }

    def "should batch load many-to-many across multiple parents"() {
        given:
            CourseEntity math = new CourseEntity(1L, "Math")
            CourseEntity physics = new CourseEntity(2L, "Physics")
            session.insertEntity(qCourse, math)
            session.insertEntity(qCourse, physics)

            session.insertEntity(qStudent, new StudentEntity(1L, "Alice", Set.of(math, physics)))
            session.insertEntity(qStudent, new StudentEntity(2L, "Bob", Set.of(math)))

        when:
            List<StudentEntity> students = session.selectEntity(qStudent).fetchAll()

        then:
            students.size() == 2
            students.find { it.name == "Alice" }.courses.size() == 2
            students.find { it.name == "Bob" }.courses.size() == 1

        where:
            session << allSessions
    }

    // === UPDATE (diff-based) ===

    def "should add elements on update — only INSERTs issued"() {
        given:
            CourseEntity math = new CourseEntity(1L, "Math")
            CourseEntity physics = new CourseEntity(2L, "Physics")
            session.insertEntity(qCourse, math)
            session.insertEntity(qCourse, physics)
            session.insertEntity(qStudent, new StudentEntity(1L, "Alice", Set.of(math)))

        when:
            StudentEntity loaded = session.selectEntity(qStudent).whereIdEq(1L).fetchSingle()
            loaded.courses.add(physics)
            session.updateEntity(qStudent, loaded)

        then:
            StudentEntity reloaded = session.selectEntity(qStudent).whereIdEq(1L).fetchSingle()
            reloaded.courses.size() == 2

        where:
            session << allSessions
    }

    def "should remove elements on update — only DELETEs issued"() {
        given:
            CourseEntity math = new CourseEntity(1L, "Math")
            CourseEntity physics = new CourseEntity(2L, "Physics")
            session.insertEntity(qCourse, math)
            session.insertEntity(qCourse, physics)
            session.insertEntity(qStudent, new StudentEntity(1L, "Alice", Set.of(math, physics)))

        when:
            StudentEntity loaded = session.selectEntity(qStudent).whereIdEq(1L).fetchSingle()
            loaded.courses.removeIf { it.title == "Math" }
            session.updateEntity(qStudent, loaded)

        then:
            StudentEntity reloaded = session.selectEntity(qStudent).whereIdEq(1L).fetchSingle()
            reloaded.courses.size() == 1
            reloaded.courses.first().title == "Physics"

        where:
            session << allSessions
    }

    def "should handle mixed add and remove on update"() {
        given:
            CourseEntity math = new CourseEntity(1L, "Math")
            CourseEntity physics = new CourseEntity(2L, "Physics")
            CourseEntity chemistry = new CourseEntity(3L, "Chemistry")
            session.insertEntity(qCourse, math)
            session.insertEntity(qCourse, physics)
            session.insertEntity(qCourse, chemistry)
            session.insertEntity(qStudent, new StudentEntity(1L, "Alice", Set.of(math, physics)))

        when:
            StudentEntity loaded = session.selectEntity(qStudent).whereIdEq(1L).fetchSingle()
            loaded.courses.removeIf { it.title == "Math" }
            loaded.courses.add(chemistry)
            session.updateEntity(qStudent, loaded)

        then:
            StudentEntity reloaded = session.selectEntity(qStudent).whereIdEq(1L).fetchSingle()
            reloaded.courses.size() == 2
            reloaded.courses.any { it.title == "Physics" }
            reloaded.courses.any { it.title == "Chemistry" }

        where:
            session << allSessions
    }

    def "should not touch join table if set was never accessed"() {
        given:
            CourseEntity math = new CourseEntity(1L, "Math")
            session.insertEntity(qCourse, math)
            session.insertEntity(qStudent, new StudentEntity(1L, "Alice", Set.of(math)))

        when:
            StudentEntity loaded = session.selectEntity(qStudent).whereIdEq(1L).fetchSingle()
            loaded.setName("Alice Updated")
            // DO NOT access loaded.courses
            session.updateEntity(qStudent, loaded)

        then:
            StudentEntity reloaded = session.selectEntity(qStudent).whereIdEq(1L).fetchSingle()
            reloaded.name == "Alice Updated"
            reloaded.courses.size() == 1
            reloaded.courses.first().title == "Math"

        where:
            session << allSessions
    }

    def "should handle update with Set<ID> variant"() {
        given:
            session.insertEntity(qStudent, new StudentEntity(1L, "Alice", Set.of()))
            session.insertEntity(qStudent, new StudentEntity(2L, "Bob", Set.of()))
            session.insertEntity(qStudent, new StudentEntity(3L, "Charlie", Set.of()))
            session.insertEntity(qCourseIdOnly, new CourseIdOnlyEntity(1L, "Math Tutor", Set.of(1L, 2L)))

        when:
            CourseIdOnlyEntity loaded = session.selectEntity(qCourseIdOnly).whereIdEq(1L).fetchSingle()
            loaded.studentIds.remove(1L)
            loaded.studentIds.add(3L)
            session.updateEntity(qCourseIdOnly, loaded)

        then:
            CourseIdOnlyEntity reloaded = session.selectEntity(qCourseIdOnly).whereIdEq(1L).fetchSingle()
            reloaded.studentIds.size() == 2
            reloaded.studentIds.contains(2L)
            reloaded.studentIds.contains(3L)

        where:
            session << allSessions
    }

    // === DELETE ===

    def "should delete join table rows when parent is deleted, but related entities remain"() {
        given:
            CourseEntity math = new CourseEntity(1L, "Math")
            session.insertEntity(qCourse, math)
            session.insertEntity(qStudent, new StudentEntity(1L, "Alice", Set.of(math)))

        when:
            StudentEntity loaded = session.selectEntity(qStudent).whereIdEq(1L).fetchSingle()
            session.deleteEntity(qStudent, loaded)

        then:
            session.selectEntity(qStudent).count() == 0
            // Course still exists — not cascade-deleted
            session.selectEntity(qCourse).count() == 1

        where:
            session << allSessions
    }

    def "should delete join table rows for entity with no relations"() {
        given:
            session.insertEntity(qStudent, new StudentEntity(1L, "Alice", Set.of()))

        when:
            StudentEntity loaded = session.selectEntity(qStudent).whereIdEq(1L).fetchSingle()
            session.deleteEntity(qStudent, loaded)

        then:
            session.selectEntity(qStudent).count() == 0

        where:
            session << allSessions
    }
}
