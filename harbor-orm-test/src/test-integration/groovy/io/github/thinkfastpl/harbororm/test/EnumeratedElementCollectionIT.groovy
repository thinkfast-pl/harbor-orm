// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test


import io.github.thinkfastpl.harbororm.test.domain.QTaskEntity
import io.github.thinkfastpl.harbororm.test.domain.TaskEntity
import io.github.thinkfastpl.harbororm.test.domain.TaskPriority

class EnumeratedElementCollectionIT extends AbstractHarborIT {

    def "should insert and select entity with @Enumerated(ORDINAL) element collection"() {
        given:
            QTaskEntity qTask = new QTaskEntity(null)
            TaskEntity task = new TaskEntity(
                    1L,
                    "Important task",
                    List.of(TaskPriority.HIGH, TaskPriority.CRITICAL)
            )

        when:
            session.insertEntity(qTask, task)

        then:
            TaskEntity loaded = session.selectEntity(qTask).whereIdEq(1L).fetchSingle()
            loaded.id == 1L
            loaded.name == "Important task"
            loaded.priorities.size() == 2
            loaded.priorities.containsAll([TaskPriority.HIGH, TaskPriority.CRITICAL])

        where:
            session << allSessions
    }

    def "should update entity with @Enumerated(ORDINAL) element collection"() {
        given:
            QTaskEntity qTask = new QTaskEntity(null)
            session.insertEntity(qTask, new TaskEntity(1L, "Task", List.of(TaskPriority.LOW)))

        when:
            TaskEntity loaded = session.selectEntity(qTask).whereIdEq(1L).fetchSingle()
            loaded.setPriorities(List.of(TaskPriority.MEDIUM, TaskPriority.HIGH))
            session.updateEntity(qTask, loaded)

        then:
            with(session.selectEntity(qTask).whereIdEq(1L).fetchSingle()) { t ->
                t.priorities.size() == 2
                t.priorities.containsAll([TaskPriority.MEDIUM, TaskPriority.HIGH])
            }

        where:
            session << allSessions
    }

    def "should handle empty @Enumerated(ORDINAL) element collection"() {
        given:
            QTaskEntity qTask = new QTaskEntity(null)
            session.insertEntity(qTask, new TaskEntity(1L, "Empty task", List.of()))

        when:
            TaskEntity loaded = session.selectEntity(qTask).whereIdEq(1L).fetchSingle()

        then:
            loaded.priorities != null
            loaded.priorities.isEmpty()

        where:
            session << allSessions
    }
}
