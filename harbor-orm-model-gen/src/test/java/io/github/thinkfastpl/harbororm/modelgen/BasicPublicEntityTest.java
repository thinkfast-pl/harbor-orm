// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.CompilationSubject;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

public class BasicPublicEntityTest {

    @Test
    public void testBasicEntity() {
        Compilation compilation = Compiler.javac()
                .withProcessors(new HarborMetaModelProcessor())
                .withOptions("-Aio.github.thinkfastpl.harbororm.tables.package=entities")
                .compile(
                        JavaFileObjects.forResource("BasicPublicEntity.java"),
                        JavaFileObjects.forResource("BooleanToStringConverter.java")
                );

        CompilationSubject.assertThat(compilation)
                .generatedSourceFile("entities.QBasicPublicEntity")
                .hasSourceEquivalentTo(JavaFileObjects.forResource("QBasicPublicEntity.java"));

//        CompilationSubject.assertThat(compilation)
//                .generatedSourceFile("entities.BasicTable")
//                .hasSourceEquivalentTo(JavaFileObjects.forResource("BasicTable.java"));
    }
}
