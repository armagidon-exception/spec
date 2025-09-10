package revxrsal.spec;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class DataSocket {

    private final SocketSupplier<Reader> reader;
    private final SocketSupplier<Writer> writer;

    public DataSocket(SocketSupplier<Reader> reader, SocketSupplier<Writer> writer) {
        this.reader = reader;
        this.writer = writer;
    }

    public interface SocketSupplier<T> {

        T get() throws IOException;
    }

    public boolean canRead() {
        return reader != null;
    }

    public boolean canWrite() {
        return writer != null;
    }

    public Reader openReader() throws IOException {
        if (reader == null) {
            throw new IllegalStateException("This socket was not configured for reading");
        }
        return reader.get();
    }

    public Writer openWriter() throws IOException {
        if (writer == null) {
            throw new IllegalStateException("This socket was not configured for writing");
        }
        return writer.get();
    }

    public void writeStrings(Iterable<String> lines) throws IOException {
        try (BufferedWriter w = new BufferedWriter(openWriter())) {
            for (String line : lines) {
                w.append(line);
                w.newLine();
            }
        }
    }

    public static DataSocket fromPath(Path path) {
        return new DataSocket(() -> {
            if (Files.exists(path)) {
                return Files.newBufferedReader(path);
            } else {
                return new StringReader("");
            }
        }, () -> {
            if (path.getParent() != null)
                Files.createDirectories(path.getParent());
            return Files.newBufferedWriter(path, CREATE, TRUNCATE_EXISTING, WRITE);
        });
    }

    public static DataSocket readOnly(SocketSupplier<Reader> reader) {
        return new DataSocket(reader, null);
    }

    public static DataSocket writeOnly(SocketSupplier<Writer> writer) {
        return new DataSocket(null, writer);
    }
}
