// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

public class SpyingWriter extends Writer {
    private final Writer delegate;
    private final StringWriter stringWriter;

    public SpyingWriter(Writer writer, boolean spy) {
        if (spy) {
            stringWriter = new StringWriter();
            delegate = new MultiWriter(List.of(writer, stringWriter));
        } else {
            stringWriter = null;
            delegate = writer;
        }
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        delegate.write(cbuf, off, len);
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    public String getSpiedText() {
        if (stringWriter == null) {
            return "";
        }
        return stringWriter.toString();
    }
}
