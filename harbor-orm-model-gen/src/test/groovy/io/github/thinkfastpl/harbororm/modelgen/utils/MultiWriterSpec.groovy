// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.modelgen.utils

import spock.lang.Specification

class MultiWriterSpec extends Specification {

    def "close closes all writers"() {
        given:
            def writer1 = new StringWriter()
            def writer2 = new StringWriter()
            def multi = new MultiWriter([writer1, writer2])

        when:
            multi.write("hello" as char[], 0, 5)
            multi.close()

        then:
            writer1.toString() == "hello"
            writer2.toString() == "hello"
    }

    def "close propagates IOException"() {
        given:
            def failingWriter = new FailingWriter()
            def multi = new MultiWriter([failingWriter])

        when:
            multi.close()

        then:
            def e = thrown(IOException)
            e.message == "close failed"
    }

    def "close closes all writers even when one fails and attaches suppressed exceptions"() {
        given:
            def failingWriter1 = new FailingWriter("first failure")
            def goodWriter = new StringWriter()
            def failingWriter2 = new FailingWriter("second failure")
            def multi = new MultiWriter([failingWriter1, goodWriter, failingWriter2])

        when:
            multi.close()

        then:
            def e = thrown(IOException)
            e.message == "first failure"
            e.suppressed.length == 1
            e.suppressed[0].message == "second failure"
    }

    private static class FailingWriter extends Writer {
        private final String message

        FailingWriter(String message = "close failed") {
            this.message = message
        }

        @Override
        void write(char[] cbuf, int off, int len) throws IOException {}

        @Override
        void flush() throws IOException {}

        @Override
        void close() throws IOException {
            throw new IOException(message)
        }
    }
}
