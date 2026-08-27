// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.CompilationSubject;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

public class RelationJoinColumnTableTest {

    @Test
    public void generatesRelationJoinColumnsInChildTableClasses() {
        Compilation compilation = Compiler.javac()
                .withProcessors(new HarborMetaModelProcessor())
                .withOptions("-Aio.github.thinkfastpl.harbororm.tables.package=entities")
                .compile(
                        JavaFileObjects.forResource("ShipmentEntity.java"),
                        JavaFileObjects.forResource("ShipmentLabelEntity.java"),
                        JavaFileObjects.forResource("ShipmentEventEntity.java"));

        CompilationSubject.assertThat(compilation)
                .generatedSourceFile("entities.ShipmentLabelsTable")
                .hasSourceEquivalentTo(JavaFileObjects.forResource("ShipmentLabelsTable.java"));

        CompilationSubject.assertThat(compilation)
                .generatedSourceFile("entities.ShipmentEventsTable")
                .hasSourceEquivalentTo(JavaFileObjects.forResource("ShipmentEventsTable.java"));
    }
}
