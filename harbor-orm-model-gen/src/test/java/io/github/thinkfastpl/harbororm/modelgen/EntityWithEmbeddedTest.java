// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.CompilationSubject;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

public class EntityWithEmbeddedTest {

    @Test
    public void entityWithEmbedded() {
        Compilation compilation = Compiler.javac()
                .withProcessors(new HarborMetaModelProcessor())
                .withOptions("-Aio.github.thinkfastpl.harbororm.tables.package=entities")
                .compile(
                        JavaFileObjects.forResource("AddressEmbeddable.java"),
                        JavaFileObjects.forResource("EntityWithEmbedded.java")
                );

        CompilationSubject.assertThat(compilation)
                .generatedSourceFile("entities.QEntityWithEmbedded")
                .hasSourceEquivalentTo(JavaFileObjects.forResource("QEntityWithEmbedded.java"));
    }
}
