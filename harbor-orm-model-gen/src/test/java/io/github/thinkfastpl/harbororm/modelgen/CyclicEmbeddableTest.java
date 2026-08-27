// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;

public class CyclicEmbeddableTest {

    @Test
    public void cyclicEmbeddedReferenceDetected() {
        Compilation compilation = Compiler.javac()
                .withProcessors(new HarborMetaModelProcessor())
                .withOptions("-Aio.github.thinkfastpl.harbororm.tables.package=entities")
                .compile(
                        JavaFileObjects.forResource("CyclicEmbeddableA.java"),
                        JavaFileObjects.forResource("CyclicEmbeddableB.java")
                );

        // Cycle detection should produce a compiler error with a descriptive message
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("Circular @Embedded reference detected");
    }
}
