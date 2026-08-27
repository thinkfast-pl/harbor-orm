// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test


import io.github.thinkfastpl.harbororm.test.domain.*

class BatchOperationsIT extends AbstractHarborIT {

    def "should insert entities using batch with default batch size"() {
        given:
            QBasicEntity qEntity = new QBasicEntity(null)
            List<BasicEntity> entities = (1..10).collect { i ->
                new BasicEntity((long) i, "Entity ${i}", i * 10)
            }

        when:
            session.insertEntityBatch(qEntity, entities)

        then:
            with(session.selectEntity(qEntity).fetchAll()) { results ->
                results.size() == 10
                results.each { result ->
                    result.id != null
                    result.name.startsWith("Entity ")
                    result.numero % 10 == 0
                }
            }

        where:
            session << allSessions
    }

    def "should insert entities using batch with custom batch size"() {
        given:
            QBasicEntity qEntity = new QBasicEntity(null)
            List<BasicEntity> entities = (1..7).collect { i ->
                new BasicEntity((long) i, "BatchEntity ${i}", i * 100)
            }

        when:
            session.insertEntityBatch(qEntity, entities, 3)

        then:
            with(session.selectEntity(qEntity).orderBy(qEntity.id.asc()).fetchAll()) { results ->
                results.size() == 7
                with(results[0]) { e ->
                    e.id == 1
                    e.name == "BatchEntity 1"
                    e.numero == 100
                }
                with(results[3]) { e ->
                    e.id == 4
                    e.name == "BatchEntity 4"
                    e.numero == 400
                }
                with(results[6]) { e ->
                    e.id == 7
                    e.name == "BatchEntity 7"
                    e.numero == 700
                }
            }

        where:
            session << allSessions
    }

    def "should insert single entity using batch operation"() {
        given:
            QBasicEntity qEntity = new QBasicEntity(null)
            List<BasicEntity> entities = [new BasicEntity(1L, "Single Entity", 999)]

        when:
            session.insertEntityBatch(qEntity, entities)

        then:
            with(session.selectEntity(qEntity).fetchAll()) { results ->
                results.size() == 1
                with(results[0]) { e ->
                    e.id == 1
                    e.name == "Single Entity"
                    e.numero == 999
                }
            }

        where:
            session << allSessions
    }

    def "should handle empty collection in batch insert"() {
        given:
            QBasicEntity qEntity = new QBasicEntity(null)
            List<BasicEntity> entities = []

        when:
            session.insertEntityBatch(qEntity, entities)

        then:
            noExceptionThrown()
            session.selectEntity(qEntity).count() == 0

        where:
            session << allSessions
    }

    def "should insert all entities and verify data integrity"() {
        given:
            QBasicEntity qEntity = new QBasicEntity(null)
            List<BasicEntity> entities = [
                new BasicEntity(1L, "Alpha", 111),
                new BasicEntity(2L, "Beta", 222),
                new BasicEntity(3L, "Gamma", 333),
                new BasicEntity(4L, "Delta", 444),
                new BasicEntity(5L, "Epsilon", 555)
            ]

        when:
            session.insertEntityBatch(qEntity, entities, 2)

        then:
            session.selectEntity(qEntity).count() == 5
            with(session.selectEntity(qEntity).where(qEntity.id.eq(1L)).fetchSingle()) { e ->
                e.name == "Alpha"
                e.numero == 111
            }
            with(session.selectEntity(qEntity).where(qEntity.id.eq(3L)).fetchSingle()) { e ->
                e.name == "Gamma"
                e.numero == 333
            }
            with(session.selectEntity(qEntity).where(qEntity.id.eq(5L)).fetchSingle()) { e ->
                e.name == "Epsilon"
                e.numero == 555
            }

        where:
            session << allSessions
    }

    def "should throw exception when batch size is zero"() {
        given:
            QBasicEntity qEntity = new QBasicEntity(null)
            List<BasicEntity> entities = [new BasicEntity(1L, "Test", 100)]

        when:
            session.insertEntityBatch(qEntity, entities, 0)

        then:
            thrown(IllegalArgumentException)

        where:
            session << allSessions
    }

    def "should throw exception when batch size is negative"() {
        given:
            QBasicEntity qEntity = new QBasicEntity(null)
            List<BasicEntity> entities = [new BasicEntity(1L, "Test", 100)]

        when:
            session.insertEntityBatch(qEntity, entities, -1)

        then:
            thrown(IllegalArgumentException)

        where:
            session << allSessions
    }

    def "should insert large batch exceeding default batch size"() {
        given:
            QBasicEntity qEntity = new QBasicEntity(null)
            // Create more entities than the default batch size (50)
            List<BasicEntity> entities = (1..75).collect { i ->
                new BasicEntity((long) i, "LargeEntity ${i}", i)
            }

        when:
            session.insertEntityBatch(qEntity, entities)

        then:
            session.selectEntity(qEntity).count() == 75
            with(session.selectEntity(qEntity).where(qEntity.id.eq(1L)).fetchSingle()) { e ->
                e.name == "LargeEntity 1"
                e.numero == 1
            }
            with(session.selectEntity(qEntity).where(qEntity.id.eq(50L)).fetchSingle()) { e ->
                e.name == "LargeEntity 50"
                e.numero == 50
            }
            with(session.selectEntity(qEntity).where(qEntity.id.eq(75L)).fetchSingle()) { e ->
                e.name == "LargeEntity 75"
                e.numero == 75
            }

        where:
            session << allSessions
    }

    def "should insert batch with batch size of one"() {
        given:
            QBasicEntity qEntity = new QBasicEntity(null)
            List<BasicEntity> entities = [
                new BasicEntity(1L, "One", 1),
                new BasicEntity(2L, "Two", 2),
                new BasicEntity(3L, "Three", 3)
            ]

        when:
            session.insertEntityBatch(qEntity, entities, 1)

        then:
            session.selectEntity(qEntity).count() == 3
            with(session.selectEntity(qEntity).orderBy(qEntity.id.asc()).fetchAll()) { results ->
                results.size() == 3
                results[0].id == 1
                results[1].id == 2
                results[2].id == 3
            }

        where:
            session << allSessions
    }

    def "should insert batch with batch size larger than collection size"() {
        given:
            QBasicEntity qEntity = new QBasicEntity(null)
            List<BasicEntity> entities = [
                new BasicEntity(1L, "Small", 10),
                new BasicEntity(2L, "Batch", 20)
            ]

        when:
            session.insertEntityBatch(qEntity, entities, 100)

        then:
            session.selectEntity(qEntity).count() == 2
            with(session.selectEntity(qEntity).orderBy(qEntity.id.asc()).fetchAll()) { results ->
                results.size() == 2
                results[0].name == "Small"
                results[1].name == "Batch"
            }

        where:
            session << allSessions
    }

    def "should populate auto-generated IDs on batch insert"() {
        given:
            def qRole = new QRoleEntity(null)
            def roles = [
                new RoleEntity(null, "Admin", null),
                new RoleEntity(null, "Editor", null),
                new RoleEntity(null, "Viewer", null),
            ]

        when:
            session.insertEntityBatch(qRole, roles, 2)

        then:
            roles.every { it.id != null }
            roles.collect { it.id }.toSet().size() == 3
            session.selectEntity(qRole).count() == 3

        where:
            session << getSessionsExcept(DbType.MYSQL)
    }

    def "should insert element collections on batch insert"() {
        given:
            def qRole = new QRoleEntity(null)
            def roles = [
                new RoleEntity(null, "Admin", List.of(RolePermission.PRODUCT_ADD, RolePermission.PRODUCT_EDIT)),
                new RoleEntity(null, "Viewer", List.of(RolePermission.PRODUCT_ADD)),
            ]

        when:
            session.insertEntityBatch(qRole, roles, 2)

        then:
            roles.every { it.id != null }

            def admin = session.selectEntity(qRole).whereIdEq(roles[0].id).fetchSingle()
            admin.name == "Admin"
            admin.permissions.size() == 2
            admin.permissions.containsAll([RolePermission.PRODUCT_ADD, RolePermission.PRODUCT_EDIT])

            def viewer = session.selectEntity(qRole).whereIdEq(roles[1].id).fetchSingle()
            viewer.name == "Viewer"
            viewer.permissions.size() == 1
            viewer.permissions.contains(RolePermission.PRODUCT_ADD)

        where:
            session << getSessionsExcept(DbType.MYSQL)
    }

    def "should populate sequence-generated IDs on batch insert"() {
        given:
            def qProduct = new QProductEntity(null)
            def products = [
                new ProductEntity(null, "Widget", 10.00, 0.23, 12.30),
                new ProductEntity(null, "Gadget", 20.00, 0.23, 24.60),
                new ProductEntity(null, "Gizmo", 30.00, 0.23, 36.90),
            ]

        when:
            session.insertEntityBatch(qProduct, products, 2)

        then:
            products.every { it.id != null }
            products.collect { it.id }.toSet().size() == 3
            session.selectEntity(qProduct).count() == 3

            def saved = session.selectEntity(qProduct).orderBy(qProduct.id.asc()).fetchAll()
            saved.size() == 3
            saved[0].name == "Widget"
            saved[1].name == "Gadget"
            saved[2].name == "Gizmo"

        where:
            session << allSessions
    }

    def "should cascade insert OneToMany children on batch insert"() {
        given:
            def qAuthor = new QAuthorEntity(null)
            def qPublication = new QPublicationEntity(null)

            def author1 = new AuthorEntity(1L, "Author A", "a@test.com", [
                new PublicationEntity(10L, null, "Book 1", 2020),
                new PublicationEntity(11L, null, "Book 2", 2021),
            ])
            def author2 = new AuthorEntity(2L, "Author B", "b@test.com", [
                new PublicationEntity(20L, null, "Paper 1", 2022),
            ])

        when:
            session.insertEntityBatch(qAuthor, [author1, author2], 2)

        then:
            session.selectEntity(qAuthor).count() == 2
            session.selectEntity(qPublication).count() == 3

            def pubs1 = session.selectEntity(qPublication).where(qPublication.authorId.eq(1L)).fetchAll()
            pubs1.size() == 2
            pubs1.collect { it.title }.toSet() == ["Book 1", "Book 2"].toSet()

            def pubs2 = session.selectEntity(qPublication).where(qPublication.authorId.eq(2L)).fetchAll()
            pubs2.size() == 1
            pubs2[0].title == "Paper 1"

        where:
            session << allSessions
    }
}
