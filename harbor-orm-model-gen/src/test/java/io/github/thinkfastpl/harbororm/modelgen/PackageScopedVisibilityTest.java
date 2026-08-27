// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.CompilationSubject;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

public class PackageScopedVisibilityTest {

    @Test
    public void packageScopedEmbeddable() {
        Compilation compilation = Compiler.javac()
                .withProcessors(new HarborMetaModelProcessor())
                .withOptions("-Aio.github.thinkfastpl.harbororm.tables.package=entities")
                .compile(JavaFileObjects.forResource("PackageScopedEmbeddable.java"));

        CompilationSubject.assertThat(compilation).succeeded();

        CompilationSubject.assertThat(compilation)
                .generatedSourceFile("entities.QPackageScopedEmbeddable")
                .contentsAsUtf8String()
                .contains("class QPackageScopedEmbeddable implements");

        CompilationSubject.assertThat(compilation)
                .generatedSourceFile("entities.QPackageScopedEmbeddable")
                .contentsAsUtf8String()
                .doesNotContain("public class QPackageScopedEmbeddable");
    }

    @Test
    public void packageScopedView() {
        Compilation compilation = Compiler.javac()
                .withProcessors(new HarborMetaModelProcessor())
                .withOptions("-Aio.github.thinkfastpl.harbororm.tables.package=entities")
                .compile(JavaFileObjects.forResource("PackageScopedView.java"));

        CompilationSubject.assertThat(compilation).succeeded();

        CompilationSubject.assertThat(compilation)
                .generatedSourceFile("entities.QPackageScopedView")
                .contentsAsUtf8String()
                .contains("class QPackageScopedView implements");

        CompilationSubject.assertThat(compilation)
                .generatedSourceFile("entities.QPackageScopedView")
                .contentsAsUtf8String()
                .doesNotContain("public class QPackageScopedView");
    }

    @Test
    public void publicEmbeddableStaysPublic() {
        Compilation compilation = Compiler.javac()
                .withProcessors(new HarborMetaModelProcessor())
                .withOptions("-Aio.github.thinkfastpl.harbororm.tables.package=entities")
                .compile(JavaFileObjects.forResource("AddressEmbeddable.java"));

        CompilationSubject.assertThat(compilation).succeeded();

        CompilationSubject.assertThat(compilation)
                .generatedSourceFile("entities.QAddressEmbeddable")
                .contentsAsUtf8String()
                .contains("public class QAddressEmbeddable implements");
    }
}
