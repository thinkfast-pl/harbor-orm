// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.CompilationSubject;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

public class EntityWithLargeEmbeddedTest {

    @Test
    public void entityWithLargeEmbedded() throws Exception {
        Compilation compilation = Compiler.javac()
                .withProcessors(new HarborMetaModelProcessor())
                .withOptions("-Aio.github.thinkfastpl.harbororm.tables.package=entities")
                .compile(
                        JavaFileObjects.forResource("LargeEmbeddable.java"),
                        JavaFileObjects.forResource("EntityWithLargeEmbedded.java")
                );

        CompilationSubject.assertThat(compilation).succeeded();

        // Verify Map.ofEntries is used (not Map.of) since there are 11 attribute overrides
        CompilationSubject.assertThat(compilation)
                .generatedSourceFile("entities.QEntityWithLargeEmbedded")
                .contentsAsUtf8String()
                .contains("Map.ofEntries(");

        // Verify full source equivalence
        CompilationSubject.assertThat(compilation)
                .generatedSourceFile("entities.QEntityWithLargeEmbedded")
                .hasSourceEquivalentTo(JavaFileObjects.forResource("QEntityWithLargeEmbedded.java"));
    }
}
