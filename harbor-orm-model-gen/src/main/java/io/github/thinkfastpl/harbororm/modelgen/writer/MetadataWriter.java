// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.writer;

import io.github.thinkfastpl.harbororm.modelgen.metadata.*;
import io.github.thinkfastpl.harbororm.modelgen.utils.SpyingWriter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.annotation.processing.Filer;
import javax.tools.JavaFileObject;
import java.io.*;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public class MetadataWriter {

    @NonNull
    private final Filer filer;
    private final boolean logGeneratedSources;

    public void writeEmbeddable(@NonNull EmbeddableMetadata metadata) {
        try {
            JavaFileObject sourceFile = filer.createSourceFile(metadata.qClassFullName());

            try (Writer writer = new OutputStreamWriter(new BufferedOutputStream(sourceFile.openOutputStream()), StandardCharsets.UTF_8)) {
                SpyingWriter spyingWriter = new SpyingWriter(writer, logGeneratedSources);
                new QEmbeddableWriter(new PrintWriter(spyingWriter), metadata).write();
                if (logGeneratedSources) {
                    System.out.println(spyingWriter.getSpiedText());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeEntity(@NonNull EntityMetadata metadata) {
        try {
            JavaFileObject sourceFile = filer.createSourceFile(metadata.qClassFullName());

            try (Writer writer = new OutputStreamWriter(new BufferedOutputStream(sourceFile.openOutputStream()), StandardCharsets.UTF_8)) {
                SpyingWriter spyingWriter = new SpyingWriter(writer, logGeneratedSources);
                new QEntityWriter(new PrintWriter(spyingWriter), metadata).write();
                if (logGeneratedSources) {
                    System.out.println(spyingWriter.getSpiedText());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeTable(@NonNull TableMetadata metadata) {
        try {
            JavaFileObject sourceFile = filer.createSourceFile(metadata.metaClassFullName());

            try (Writer writer = new OutputStreamWriter(new BufferedOutputStream(sourceFile.openOutputStream()), StandardCharsets.UTF_8)) {
                SpyingWriter spyingWriter = new SpyingWriter(writer, logGeneratedSources);
                new QTableWriter(new PrintWriter(spyingWriter), metadata).write();
                if (logGeneratedSources) {
                    System.out.println(spyingWriter.getSpiedText());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeStoredFunction(@NonNull StoredFunctionMetadata metadata) {
        try {
            JavaFileObject sourceFile = filer.createSourceFile(metadata.generatedClassFullName());

            try (Writer writer = new OutputStreamWriter(new BufferedOutputStream(sourceFile.openOutputStream()), StandardCharsets.UTF_8)) {
                SpyingWriter spyingWriter = new SpyingWriter(writer, logGeneratedSources);
                new QStoredFunctionWriter(new PrintWriter(spyingWriter), metadata).write();
                if (logGeneratedSources) {
                    System.out.println(spyingWriter.getSpiedText());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeView(@NonNull ViewMetadata metadata) {
        try {
            JavaFileObject sourceFile = filer.createSourceFile(metadata.qClassFullName());

            try (Writer writer = new OutputStreamWriter(new BufferedOutputStream(sourceFile.openOutputStream()), StandardCharsets.UTF_8)) {
                SpyingWriter spyingWriter = new SpyingWriter(writer, logGeneratedSources);
                new QViewWriter(new PrintWriter(spyingWriter), metadata).write();
                if (logGeneratedSources) {
                    System.out.println(spyingWriter.getSpiedText());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
