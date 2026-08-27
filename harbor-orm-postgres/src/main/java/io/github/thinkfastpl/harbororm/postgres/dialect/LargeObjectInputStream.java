// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.postgres.dialect;

import lombok.NonNull;
import org.postgresql.largeobject.LargeObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;

class LargeObjectInputStream extends InputStream {

    @NonNull
    private final LargeObject largeObject;

    @NonNull
    private final InputStream inputStream;

    LargeObjectInputStream(@NonNull LargeObject largeObject) throws SQLException {
        this.largeObject = largeObject;

        try {
            this.inputStream = largeObject.getInputStream();
        } catch (Exception e) {
            try {
                this.largeObject.close();
            } catch (Exception ignored) {
            }
            throw e;
        }
    }

    @Override
    public int read() throws IOException {
        return this.inputStream.read();
    }

    @Override
    public int read(byte @NonNull [] b) throws IOException {
        return inputStream.read(b);
    }

    @Override
    public int read(byte @NonNull [] b, int off, int len) throws IOException {
        return inputStream.read(b, off, len);
    }

    @Override
    public byte @NonNull [] readAllBytes() throws IOException {
        return inputStream.readAllBytes();
    }

    @Override
    public byte @NonNull [] readNBytes(int len) throws IOException {
        return inputStream.readNBytes(len);
    }

    @Override
    public int readNBytes(byte @NonNull [] b, int off, int len) throws IOException {
        return inputStream.readNBytes(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return inputStream.skip(n);
    }

    @Override
    public void skipNBytes(long n) throws IOException {
        inputStream.skipNBytes(n);
    }

    @Override
    public int available() throws IOException {
        return inputStream.available();
    }

    @Override
    public void close() {
        try {
            this.inputStream.close();
        } catch (Exception ignored) {
        }

        try {
            this.largeObject.close();
        } catch (Exception ignored) {
        }
    }

    @Override
    public void mark(int readlimit) {
        inputStream.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        inputStream.reset();
    }

    @Override
    public boolean markSupported() {
        return inputStream.markSupported();
    }

    @Override
    public long transferTo(OutputStream out) throws IOException {
        return inputStream.transferTo(out);
    }
}
