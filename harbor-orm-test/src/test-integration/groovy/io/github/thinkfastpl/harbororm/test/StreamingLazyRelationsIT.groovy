// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.test.domain.*
import io.github.thinkfastpl.harbororm.test.domain.mtm.CourseEntity
import io.github.thinkfastpl.harbororm.test.domain.mtm.QCourseEntity
import io.github.thinkfastpl.harbororm.test.domain.mtm.QStudentEntity
import io.github.thinkfastpl.harbororm.test.domain.mtm.StudentEntity

import java.util.stream.Stream

/**
 * Integration tests for accessing lazy relations while a streamAll() stream is still open.
 *
 * During streaming, entities are hydrated one at a time and the consumer may touch a lazy
 * collection before the next entity is hydrated. The lazy-load batch that already resolved
 * must not swallow later-streamed entities — each of them has to be resolvable on its own.
 *
 * Covers @ElementCollection, @OneToMany and @ManyToMany (the three lazy batch backends).
 */
class StreamingLazyRelationsIT extends AbstractHarborIT {

    def "should load element collection accessed per element during streaming"() {
        given:
            QArticleEntity qArticle = new QArticleEntity(null)
            session.insertEntity(qArticle, new ArticleEntity(1L, "Article 1", List.of("a1-tag1", "a1-tag2")))
            session.insertEntity(qArticle, new ArticleEntity(2L, "Article 2", List.of()))
            session.insertEntity(qArticle, new ArticleEntity(3L, "Article 3", List.of("a3-tag1")))

        when:
            Map<Long, List<String>> tagsById = [:]
            try (Stream<ArticleEntity> stream = session.selectEntity(qArticle).orderBy(qArticle.id.asc()).streamAll()) {
                stream.forEach { article -> tagsById[article.id] = List.copyOf(article.tags) }
            }

        then:
            tagsById.size() == 3
            tagsById[1L].toSorted() == ["a1-tag1", "a1-tag2"]
            tagsById[2L].isEmpty()
            tagsById[3L] == ["a3-tag1"]

        where:
            session << allSessions
    }

    def "should load one-to-many relation accessed per element during streaming"() {
        given:
            QAuthorEntity qAuthor = new QAuthorEntity(null)
            QPublicationEntity qPublication = new QPublicationEntity(null)
            session.insertEntity(qAuthor, new AuthorEntity(1L, "Author 1", "author1@example.com", []))
            session.insertEntity(qAuthor, new AuthorEntity(2L, "Author 2", "author2@example.com", []))
            session.insertEntity(qPublication, new PublicationEntity(1L, 1L, "Book A", (Integer) 2001))
            session.insertEntity(qPublication, new PublicationEntity(2L, 1L, "Book B", (Integer) 2002))
            session.insertEntity(qPublication, new PublicationEntity(3L, 2L, "Book C", (Integer) 2003))

        when:
            Map<Long, List<String>> titlesByAuthorId = [:]
            try (Stream<AuthorEntity> stream = session.selectEntity(qAuthor).orderBy(qAuthor.id.asc()).streamAll()) {
                stream.forEach { author -> titlesByAuthorId[author.id] = author.publications*.title }
            }

        then:
            titlesByAuthorId.size() == 2
            titlesByAuthorId[1L].toSorted() == ["Book A", "Book B"]
            titlesByAuthorId[2L] == ["Book C"]

        where:
            session << allSessions
    }

    def "should load many-to-many relation accessed per element during streaming"() {
        given:
            QCourseEntity qCourse = new QCourseEntity(null)
            QStudentEntity qStudent = new QStudentEntity(null)
            CourseEntity math = new CourseEntity(1L, "Math")
            CourseEntity physics = new CourseEntity(2L, "Physics")
            session.insertEntity(qCourse, math)
            session.insertEntity(qCourse, physics)
            session.insertEntity(qStudent, new StudentEntity(1L, "Alice", Set.of(math, physics)))
            session.insertEntity(qStudent, new StudentEntity(2L, "Bob", Set.of(physics)))
            session.insertEntity(qStudent, new StudentEntity(3L, "Carol", Set.of()))

        when:
            Map<Long, List<String>> courseTitlesByStudentId = [:]
            try (Stream<StudentEntity> stream = session.selectEntity(qStudent).orderBy(qStudent.id.asc()).streamAll()) {
                stream.forEach { student -> courseTitlesByStudentId[student.id] = student.courses*.title }
            }

        then:
            courseTitlesByStudentId.size() == 3
            courseTitlesByStudentId[1L].toSorted() == ["Math", "Physics"]
            courseTitlesByStudentId[2L] == ["Physics"]
            courseTitlesByStudentId[3L].isEmpty()

        where:
            session << allSessions
    }
}
