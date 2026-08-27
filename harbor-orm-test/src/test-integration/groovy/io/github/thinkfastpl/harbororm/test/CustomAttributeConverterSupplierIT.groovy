// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.HarborSession
import io.github.thinkfastpl.harbororm.api.converter.AttributeConverter
import io.github.thinkfastpl.harbororm.api.converter.AttributeConverterSupplier
import io.github.thinkfastpl.harbororm.core.HarborSessionFactory
import io.github.thinkfastpl.harbororm.core.converter.DefaultAttributeConverterSupplier
import io.github.thinkfastpl.harbororm.core.sql.SqlConnectionAccessor
import io.github.thinkfastpl.harbororm.h2.dialect.H2RdbmsSupport
import io.github.thinkfastpl.harbororm.postgres.dialect.PostgreSqlRdbmsSupport
import io.github.thinkfastpl.harbororm.test.domain.CustomSupplierEntity
import io.github.thinkfastpl.harbororm.test.domain.QCustomSupplierEntity
import io.github.thinkfastpl.harbororm.test.domain.converter.PrefixStringConverter
import lombok.NonNull

class CustomAttributeConverterSupplierIT extends AbstractHarborIT {

    private static final String PREFIX = "PFX_"

    private static final AttributeConverterSupplier customSupplier = new AttributeConverterSupplier() {
        private final DefaultAttributeConverterSupplier fallback = new DefaultAttributeConverterSupplier()

        @Override
        AttributeConverter<?, ?> supply(@NonNull Class<? extends AttributeConverter<?, ?>> clazz) {
            if (clazz == PrefixStringConverter.class) {
                return new PrefixStringConverter(PREFIX)
            }
            return fallback.supply(clazz)
        }
    }

    private static final HarborSession customSessionH2 = HarborSessionFactory.builder()
            .connectionAccessor(SqlConnectionAccessor.of(connectionH2))
            .rdbmsSupport(new H2RdbmsSupport())
            .attributeConverterSupplier(customSupplier)
            .build()

    private static final HarborSession customSessionPostgres = HarborSessionFactory.builder()
            .connectionAccessor(SqlConnectionAccessor.of(connectionPostgres))
            .rdbmsSupport(new PostgreSqlRdbmsSupport())
            .attributeConverterSupplier(customSupplier)
            .build()

    private static final List<HarborSession> customSessions = [customSessionH2, customSessionPostgres]

    def "should insert and read entity using converter supplied by custom AttributeConverterSupplier"() {
        given:
            QCustomSupplierEntity qEntity = new QCustomSupplierEntity(null)
            CustomSupplierEntity entity = new CustomSupplierEntity(1L, "hello")

        when:
            session.insertEntity(qEntity, entity)

        then:
            with(session.selectEntity(qEntity).where(qEntity.id.eq(1L)).fetchSingle()) { loaded ->
                loaded.id == 1L
                loaded.value == "hello"
            }

        where:
            session << customSessions
    }

    def "should store prefixed value in database when using custom supplier"() {
        given:
            QCustomSupplierEntity qEntity = new QCustomSupplierEntity(null)
            CustomSupplierEntity entity = new CustomSupplierEntity(2L, "world")

        when:
            session.insertEntity(qEntity, entity)

        then:
            def stmt = connection.prepareStatement("SELECT \"value\" FROM custom_supplier_test WHERE id = 2")
            def rs = stmt.executeQuery()
            rs.next()
            rs.getString("value") == PREFIX + "world"

        cleanup:
            stmt?.close()

        where:
            session      | connection
            customSessionH2      | connectionH2
            customSessionPostgres | connectionPostgres
    }

    def "should fail with default supplier when converter has no no-arg constructor"() {
        given:
            HarborSession defaultSession = HarborSessionFactory.builder()
                    .connectionAccessor(SqlConnectionAccessor.of(connectionH2))
                    .rdbmsSupport(new H2RdbmsSupport())
                    .build()
            QCustomSupplierEntity qEntity = new QCustomSupplierEntity(null)
            CustomSupplierEntity entity = new CustomSupplierEntity(4L, "fail")

        when:
            defaultSession.insertEntity(qEntity, entity)

        then:
            thrown(RuntimeException)
    }

    def "should handle null value with custom supplier converter"() {
        given:
            QCustomSupplierEntity qEntity = new QCustomSupplierEntity(null)
            CustomSupplierEntity entity = new CustomSupplierEntity(3L, null)

        when:
            session.insertEntity(qEntity, entity)

        then:
            with(session.selectEntity(qEntity).where(qEntity.id.eq(3L)).fetchSingle()) { loaded ->
                loaded.id == 3L
                loaded.value == null
            }

        where:
            session << customSessions
    }
}
